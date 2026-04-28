package com.mahmoud.maalflow.security.repo;

import com.mahmoud.maalflow.security.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByJti(String jti);

    List<RefreshToken> findAllByFamilyId(String familyId);

    List<RefreshToken> findAllByUser_Id(Long userId);

    Optional<RefreshToken> findByUser_IdAndJti(Long userId, String jti);
}

