package com.mahmoud.maalflow.security;

import com.mahmoud.maalflow.security.entity.RefreshToken;
import com.mahmoud.maalflow.security.repo.RefreshTokenRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;


@Component
@AllArgsConstructor
public class RefreshTokenRevocationService
{
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected void revokeFamily(String familyId) {
        List<RefreshToken> tokens = refreshTokenRepository.findAllByFamilyId(familyId);
        LocalDateTime now = LocalDateTime.now();
        for (RefreshToken token : tokens) {
            token.setReuseDetected(true);
            if (token.getRevokedAt() == null) {
                token.setRevokedAt(now);
            }
        }
        refreshTokenRepository.saveAll(tokens);
    }

}
