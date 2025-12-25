package com.springbootTemplate.univ.soa.repository;

import com.springbootTemplate.univ.soa.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUtilisateurIdOrderByDateCreationDesc(Long utilisateurId);

    List<Notification> findByUtilisateurIdAndLueOrderByDateCreationDesc(Long utilisateurId, Boolean lue);

    long countByUtilisateurIdAndLue(Long utilisateurId, Boolean lue);
}

