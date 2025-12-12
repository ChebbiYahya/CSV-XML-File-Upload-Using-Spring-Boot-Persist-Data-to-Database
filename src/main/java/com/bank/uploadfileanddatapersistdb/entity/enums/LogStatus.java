package com.bank.uploadfileanddatapersistdb.entity.enums;

public enum LogStatus {
    SUCCESS,          // tout le fichier traité
    FAILED,           // aucune ligne insérée
    PARTIALLY_TRAITED,// au moins 1 succès et 1 échec
    IN_PROGRESS       // pendant le traitement
}
