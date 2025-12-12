package com.bank.uploadfileanddatapersistdb.DTO;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LogChargementWithDetailsDto {
    private Long id;
    private String fileName;
    private String status;
    private String createdAt;

    private Integer totalLines;
    private Integer successLines;
    private Integer failedLines;

    private List<LogChargementDetailDto> details;
}
