package com.bank.uploadfileanddatapersistdb.entity;

import com.bank.uploadfileanddatapersistdb.entity.enums.LineStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "log_chargement_detail")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LogChargementDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Numéro de ligne dans le fichier (ou index d’enregistrement pour XML)
    @Column(name = "line_number")
    private Integer lineNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private LineStatus status;

    @Column(name = "detail_problem", length = 2000)
    private String detailProblem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "log_chargement_id", nullable = false)
    private LogChargement logChargement;
}
