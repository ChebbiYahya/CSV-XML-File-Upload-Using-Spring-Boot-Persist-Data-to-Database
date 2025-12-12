package com.bank.uploadfileanddatapersistdb.service;

import com.bank.uploadfileanddatapersistdb.entity.LogChargement;
import com.bank.uploadfileanddatapersistdb.entity.LogChargementDetail;
import com.bank.uploadfileanddatapersistdb.entity.enums.LineStatus;
import com.bank.uploadfileanddatapersistdb.entity.enums.LogStatus;
import com.bank.uploadfileanddatapersistdb.exception.LogChargementNotFoundException;
import com.bank.uploadfileanddatapersistdb.interfaces.LogChargementService;
import com.bank.uploadfileanddatapersistdb.repository.LogChargementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LogChargementServiceImpl implements LogChargementService {


    private final LogChargementRepository logChargementRepository;

    @Override
    @Transactional
    public LogChargement startLog(String fileName) {
        LogChargement log = LogChargement.builder()
                .fileName(fileName)
                .status(LogStatus.IN_PROGRESS)
                .createdAt(LocalDateTime.now())
                .details(new ArrayList<>())          // 👈 explicite
                .build();
        return logChargementRepository.save(log);
    }

    @Override
    @Transactional
    public void addLine(LogChargement log,
                        int lineNumber,
                        LineStatus status,
                        String detailProblem) {

        LogChargementDetail detail = LogChargementDetail.builder()
                .lineNumber(lineNumber)
                .status(status)
                .detailProblem(detailProblem)
                .logChargement(log)
                .build();

        log.addDetail(detail);
        logChargementRepository.save(log);
    }

    @Override
    @Transactional
    public void finalizeLog(LogChargement log,
                            int totalLines,
                            int successLines,
                            int failedLines) {

        log.setTotalLines(totalLines);
        log.setSuccessLines(successLines);
        log.setFailedLines(failedLines);

        if (successLines > 0 && failedLines == 0) {
            log.setStatus(LogStatus.SUCCESS);
        } else if (successLines == 0 && failedLines > 0) {
            log.setStatus(LogStatus.FAILED);
        } else if (successLines > 0 && failedLines > 0) {
            log.setStatus(LogStatus.PARTIALLY_TRAITED);
        } else {
            // aucun enregistrement ? on peut considérer FAILED ou SUCCESS selon ton besoin
            log.setStatus(LogStatus.FAILED);
        }

        logChargementRepository.save(log);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LogChargement> getAllLogs() {
        return logChargementRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public LogChargement getLogById(Long id) {
        return logChargementRepository.findById(id)
                .orElseThrow(() ->
                        new LogChargementNotFoundException("LogChargement not found with id: " + id)
                );
    }

    @Override
    @Transactional(readOnly = true)
    public List<LogChargement> searchLogs(String fileName, LogStatus status) {
        return logChargementRepository.findAll().stream()
                .filter(log -> {
                    if (fileName == null || fileName.isBlank()) return true;
                    String fn = log.getFileName();
                    return fn != null && fn.toLowerCase().contains(fileName.toLowerCase());
                })
                .filter(log -> status == null || status.equals(log.getStatus()))
                .toList();
    }
}