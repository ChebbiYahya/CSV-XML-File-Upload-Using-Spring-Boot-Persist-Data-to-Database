package com.bank.uploadfileanddatapersistdb.DTO;

import com.bank.uploadfileanddatapersistdb.entity.enums.LineStatus;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LogChargementDetailDto {
    private Integer lineNumber;
    private LineStatus status;
    private String detailProblem;
}
