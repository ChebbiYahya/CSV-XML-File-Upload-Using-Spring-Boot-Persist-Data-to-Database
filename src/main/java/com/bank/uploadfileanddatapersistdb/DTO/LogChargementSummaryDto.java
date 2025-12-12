package com.bank.uploadfileanddatapersistdb.DTO;

import com.bank.uploadfileanddatapersistdb.entity.enums.LogStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LogChargementSummaryDto {
    private Long id;
    private String fileName;
    private LogStatus status;
    private LocalDateTime createdAt;
    private Integer totalLines;
    private Integer successLines;
    private Integer failedLines;
}
