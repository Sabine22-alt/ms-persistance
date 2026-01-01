package com.springbootTemplate.univ.soa.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "repas_plannifies")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RepasPlannifie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "planification_jour_id", nullable = false)
    private PlanificationJour planificationJour;

    @Column(nullable = false)
    private String nomRepas;
}
