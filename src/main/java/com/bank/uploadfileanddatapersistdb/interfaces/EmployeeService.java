package com.bank.uploadfileanddatapersistdb.interfaces;

import com.bank.uploadfileanddatapersistdb.DTO.EmployeeDto;

import java.util.List;

public interface EmployeeService {

    void saveAll(List<EmployeeDto> employees);
}
