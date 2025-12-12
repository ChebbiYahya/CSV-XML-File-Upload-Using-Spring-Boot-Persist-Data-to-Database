package com.bank.uploadfileanddatapersistdb.interfaces;

import com.bank.uploadfileanddatapersistdb.entity.LogChargement;
import com.bank.uploadfileanddatapersistdb.entity.enums.LineStatus;
import com.bank.uploadfileanddatapersistdb.entity.enums.LogStatus;

import java.util.List;

public interface LogChargementService {
    LogChargement startLog(String fileName);

    void addLine(LogChargement log,
                 int lineNumber,
                 LineStatus status,
                 String detailProblem);

    void finalizeLog(LogChargement log,
                     int totalLines,
                     int successLines,
                     int failedLines);

    List<LogChargement> getAllLogs();

    LogChargement getLogById(Long id);

    List<LogChargement> searchLogs(String fileName, LogStatus status);
}
