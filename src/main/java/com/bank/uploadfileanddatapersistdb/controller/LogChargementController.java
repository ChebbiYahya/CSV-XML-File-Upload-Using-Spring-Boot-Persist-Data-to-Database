package com.bank.uploadfileanddatapersistdb.controller;

import com.bank.uploadfileanddatapersistdb.DTO.LogChargementDetailDto;
import com.bank.uploadfileanddatapersistdb.DTO.LogChargementSummaryDto;
import com.bank.uploadfileanddatapersistdb.DTO.LogChargementWithDetailsDto;
import com.bank.uploadfileanddatapersistdb.entity.LogChargement;
import com.bank.uploadfileanddatapersistdb.entity.LogChargementDetail;
import com.bank.uploadfileanddatapersistdb.entity.enums.LogStatus;
import com.bank.uploadfileanddatapersistdb.interfaces.LogChargementService;
import com.bank.uploadfileanddatapersistdb.mapper.LogChargementMapper;
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
    private final LogChargementMapper logChargementMapper;

    @GetMapping
    public List<LogChargementSummaryDto> getAllLogs(
            @RequestParam(name = "fileName", required = false) String fileName,
            @RequestParam(name = "status", required = false) LogStatus status
    ) {
        return logChargementService.searchLogs(fileName, status).stream()
                .sorted(Comparator.comparing(LogChargement::getCreatedAt).reversed())
                .map(logChargementMapper::toSummaryDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public LogChargementWithDetailsDto getLogById(@PathVariable Long id) {
        LogChargement log = logChargementService.getLogById(id);
        return logChargementMapper.toWithDetailsDto(log);
    }
}
