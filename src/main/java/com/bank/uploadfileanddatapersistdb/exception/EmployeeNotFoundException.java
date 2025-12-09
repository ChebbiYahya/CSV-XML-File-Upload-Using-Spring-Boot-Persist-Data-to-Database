package com.bank.uploadfileanddatapersistdb.exception;

public class EmployeeNotFoundException extends RuntimeException {
    public EmployeeNotFoundException(String message){
        super(message);
    }
}
