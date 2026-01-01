package com.springbootTemplate.univ.soa.repository;

import com.springbootTemplate.univ.soa.model.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    @Query("SELECT p FROM PasswordResetToken p WHERE p.token = :token")
    Optional<PasswordResetToken> findByToken(@Param("token") String token);

    @Query("SELECT p FROM PasswordResetToken p WHERE p.utilisateurId = :utilisateurId AND p.used = false")
    Optional<PasswordResetToken> findValidTokenByUtilisateurId(@Param("utilisateurId") Long utilisateurId);
}

