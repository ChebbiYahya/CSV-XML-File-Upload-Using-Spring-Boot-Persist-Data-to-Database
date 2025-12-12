package com.bank.uploadfileanddatapersistdb.entity;

import com.bank.uploadfileanddatapersistdb.entity.enums.LogStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "log_chargement")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LogChargement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30, nullable = false)
    private LogStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "total_lines")
    private Integer totalLines;

    @Column(name = "success_lines")
    private Integer successLines;

    @Column(name = "failed_lines")
    private Integer failedLines;

    @OneToMany(mappedBy = "logChargement", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<LogChargementDetail> details = new ArrayList<>();

    public void addDetail(LogChargementDetail detail) {
        if (this.details == null) {
            this.details = new ArrayList<>();
        }
        detail.setLogChargement(this);
        this.details.add(detail);
    }
}
