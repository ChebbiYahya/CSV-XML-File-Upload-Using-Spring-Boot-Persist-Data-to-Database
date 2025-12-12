package com.bank.uploadfileanddatapersistdb.controller;

import com.bank.uploadfileanddatapersistdb.DTO.LogChargementDetailDto;
import com.bank.uploadfileanddatapersistdb.DTO.LogChargementSummaryDto;
import com.bank.uploadfileanddatapersistdb.DTO.LogChargementWithDetailsDto;
import com.bank.uploadfileanddatapersistdb.entity.LogChargement;
import com.bank.uploadfileanddatapersistdb.entity.LogChargementDetail;
import com.bank.uploadfileanddatapersistdb.entity.enums.LogStatus;
import com.bank.uploadfileanddatapersistdb.interfaces.LogChargementService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/logs")
@RequiredArgsConstructor
public class LogChargementController {
    private final LogChargementService logChargementService;

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");



    @GetMapping
    public List<LogChargementSummaryDto> getAllLogs(
            @RequestParam(name = "fileName", required = false) String fileName,
            @RequestParam(name = "status", required = false) LogStatus status
    ) {
        return logChargementService.searchLogs(fileName, status).stream()
                .sorted(Comparator.comparing(LogChargement::getCreatedAt).reversed())
                .map(this::toSummaryDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public LogChargementWithDetailsDto getLogById(@PathVariable Long id) {
        LogChargement log = logChargementService.getLogById(id);
        return toWithDetailsDto(log);
    }

    // ================== MAPPERS DTO ==================

    private LogChargementSummaryDto toSummaryDto(LogChargement log) {
        return LogChargementSummaryDto.builder()
                .id(log.getId())
                .fileName(log.getFileName())
                .status(log.getStatus())
                .createdAt(log.getCreatedAt())
                .totalLines(log.getTotalLines())
                .successLines(log.getSuccessLines())
                .failedLines(log.getFailedLines())
                .build();
    }

    private LogChargementWithDetailsDto toWithDetailsDto(LogChargement log) {
        List<LogChargementDetailDto> detailDtos = log.getDetails().stream()
                .sorted(Comparator.comparing(LogChargementDetail::getLineNumber))
                .map(this::toDetailDto)
                .collect(Collectors.toList());

        return LogChargementWithDetailsDto.builder()
                .id(log.getId())
                .fileName(log.getFileName())
                .status(log.getStatus() != null ? log.getStatus().name() : null)
                .createdAt(log.getCreatedAt() != null
                        ? log.getCreatedAt().format(DATE_TIME_FORMATTER)
                        : null)
                .totalLines(log.getTotalLines())
                .successLines(log.getSuccessLines())
                .failedLines(log.getFailedLines())
                .details(detailDtos)
                .build();
    }

    private LogChargementDetailDto toDetailDto(LogChargementDetail detail) {
        return LogChargementDetailDto.builder()
                .lineNumber(detail.getLineNumber())
                .status(detail.getStatus())
                .detailProblem(detail.getDetailProblem())
                .build();
    }
}
