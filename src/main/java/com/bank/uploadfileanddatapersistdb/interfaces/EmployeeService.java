package com.bank.uploadfileanddatapersistdb.interfaces;

import com.bank.uploadfileanddatapersistdb.DTO.EmployeeDto;
import com.bank.uploadfileanddatapersistdb.entity.Employee;

import java.util.List;

public interface EmployeeService {

    void saveAll(List<EmployeeDto> employees);
    List<Employee> getAll();
    Employee getEmployeeById(Long id);
    boolean existsById(Long id);
}
