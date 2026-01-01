package com.springbootTemplate.univ.soa.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonBackReference;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "planifications_jours", indexes = {
    @Index(name = "idx_plan_jour", columnList = "planification_id,jour")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlanificationJour {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "planification_id", nullable = false)
    @JsonBackReference
    private PlanificationRepas planification;

    @Column(nullable = false)
    private Integer jour;

    @OneToMany(mappedBy = "planificationJour", cascade = CascadeType.ALL, orphanRemoval = true)
    @Fetch(FetchMode.SUBSELECT)
    private List<RepasPlannifie> repas = new ArrayList<>();
}
