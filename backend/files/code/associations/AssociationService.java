package com.mahmoud.maalflow.modules.associations.service;

import com.mahmoud.maalflow.exception.BusinessException;
import com.mahmoud.maalflow.exception.ObjectNotFoundException;
import com.mahmoud.maalflow.modules.associations.dto.*;
import com.mahmoud.maalflow.modules.associations.entity.*;
import com.mahmoud.maalflow.modules.associations.enums.*;
import com.mahmoud.maalflow.modules.associations.repo.*;
import com.mahmoud.maalflow.modules.shared.user.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing Associations (جمعيات / ROSCA).
 * Copy to: src/main/java/com/mahmoud/maalflow/modules/associations/service/
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AssociationService {

    private final AssociationRepository associationRepo;
    private final AssociationMemberRepository memberRepo;
    private final AssociationPaymentRepository paymentRepo;
    private final UserRepository userRepository;
    // TODO: Replace with SecurityUtils.getCurrentUser() after security impl
    // private final SecurityUtils securityUtils;

    @Transactional
    public AssociationResponse createAssociation(AssociationRequest request) {
        log.info("Creating association: {}", request.getName());

        Association association = Association.builder()
                .name(request.getName())
                .description(request.getDescription())
                .monthlyAmount(request.getMonthlyAmount())
                .totalMembers(request.getTotalMembers())
                .durationMonths(request.getTotalMembers()) // duration = number of members (each gets one turn)
                .startDate(request.getStartDate())
                .status(AssociationStatus.ACTIVE)
                .createdBy(userRepository.findById(1L).orElse(null)) // TODO: securityUtils.getCurrentUser()
                .build();

        Association saved = associationRepo.save(association);
        log.info("Created association ID: {}", saved.getId());
        return toResponse(saved);
    }

    @Transactional
    public AssociationMemberResponse addMember(AssociationMemberRequest request) {
        Association association = associationRepo.findById(request.getAssociationId())
                .orElseThrow(() -> new ObjectNotFoundException("Association not found", request.getAssociationId()));

        long currentCount = memberRepo.countByAssociationId(association.getId());
        if (currentCount >= association.getTotalMembers()) {
            throw new BusinessException("Association is full. Max members: " + association.getTotalMembers());
        }
        if (memberRepo.existsByAssociationIdAndTurnOrder(association.getId(), request.getTurnOrder())) {
            throw new BusinessException("Turn order " + request.getTurnOrder() + " is already taken");
        }

        AssociationMember member = AssociationMember.builder()
                .association(association)
                .memberName(request.getMemberName())
                .phone(request.getPhone())
                .turnOrder(request.getTurnOrder())
                .hasReceived(false)
                .notes(request.getNotes())
                .build();

        AssociationMember saved = memberRepo.save(member);
        log.info("Added member {} to association {}", saved.getMemberName(), association.getId());
        return toMemberResponse(saved);
    }

    @Transactional
    public void recordPayment(AssociationPaymentRequest request) {
        Association association = associationRepo.findById(request.getAssociationId())
                .orElseThrow(() -> new ObjectNotFoundException("Association not found", request.getAssociationId()));
        AssociationMember member = memberRepo.findById(request.getMemberId())
                .orElseThrow(() -> new ObjectNotFoundException("Member not found", request.getMemberId()));

        if (paymentRepo.existsByMemberIdAndPaymentMonth(member.getId(), request.getPaymentMonth())) {
            throw new BusinessException("Payment already recorded for this member in " + request.getPaymentMonth());
        }

        AssociationPayment payment = AssociationPayment.builder()
                .association(association)
                .member(member)
                .amount(request.getAmount())
                .paymentMonth(request.getPaymentMonth())
                .paymentDate(request.getPaymentDate())
                .status(MemberPaymentStatus.PAID)
                .notes(request.getNotes())
                .build();

        paymentRepo.save(payment);
        log.info("Recorded payment for member {} in association {} for month {}",
                member.getMemberName(), association.getId(), request.getPaymentMonth());
    }

    @Transactional
    public void markMemberReceived(Long memberId) {
        AssociationMember member = memberRepo.findById(memberId)
                .orElseThrow(() -> new ObjectNotFoundException("Member not found", memberId));
        if (member.getHasReceived()) {
            throw new BusinessException("Member has already received their turn");
        }
        member.setHasReceived(true);
        member.setReceivedDate(LocalDate.now());
        memberRepo.save(member);
        log.info("Marked member {} as received in association {}", member.getMemberName(), member.getAssociation().getId());

        // Check if all members have received -> complete the association
        List<AssociationMember> allMembers = memberRepo.findByAssociationIdOrderByTurnOrderAsc(member.getAssociation().getId());
        boolean allReceived = allMembers.stream().allMatch(AssociationMember::getHasReceived);
        if (allReceived) {
            Association association = member.getAssociation();
            association.setStatus(AssociationStatus.COMPLETED);
            association.setEndDate(LocalDate.now());
            associationRepo.save(association);
            log.info("Association {} completed - all members received", association.getId());
        }
    }

    @Transactional(readOnly = true)
    public AssociationResponse getById(Long id) {
        Association a = associationRepo.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("Association not found", id));
        return toResponse(a);
    }

    @Transactional(readOnly = true)
    public Page<AssociationResponse> list(int page, int size) {
        return associationRepo.findAllByOrderByCreatedAtDesc(PageRequest.of(page, size))
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<AssociationMemberResponse> getMembers(Long associationId) {
        return memberRepo.findByAssociationIdOrderByTurnOrderAsc(associationId)
                .stream().map(this::toMemberResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AssociationPayment> getPaymentsForMonth(Long associationId, String month) {
        return paymentRepo.findByAssociationIdAndPaymentMonth(associationId, month);
    }

    // ========== Mappers ==========

    private AssociationResponse toResponse(Association a) {
        List<AssociationMember> members = memberRepo.findByAssociationIdOrderByTurnOrderAsc(a.getId());
        int currentRound = (int) members.stream().filter(AssociationMember::getHasReceived).count() + 1;

        return AssociationResponse.builder()
                .id(a.getId())
                .name(a.getName())
                .description(a.getDescription())
                .monthlyAmount(a.getMonthlyAmount())
                .totalMembers(a.getTotalMembers())
                .durationMonths(a.getDurationMonths())
                .startDate(a.getStartDate())
                .endDate(a.getEndDate())
                .status(a.getStatus())
                .totalPoolAmount(a.getTotalPoolAmount())
                .currentRound(Math.min(currentRound, a.getTotalMembers()))
                .createdAt(a.getCreatedAt())
                .members(members.stream().map(this::toMemberResponse).collect(Collectors.toList()))
                .build();
    }

    private AssociationMemberResponse toMemberResponse(AssociationMember m) {
        int paidCount = paymentRepo.countPaidByMemberId(m.getId());
        return AssociationMemberResponse.builder()
                .id(m.getId())
                .memberName(m.getMemberName())
                .phone(m.getPhone())
                .turnOrder(m.getTurnOrder())
                .hasReceived(m.getHasReceived())
                .receivedDate(m.getReceivedDate())
                .notes(m.getNotes())
                .paymentsMade(paidCount)
                .paymentsExpected(m.getAssociation().getDurationMonths())
                .build();
    }
}

