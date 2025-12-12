package com.bank.uploadfileanddatapersistdb.service;


import com.bank.uploadfileanddatapersistdb.DTO.EmployeeDto;
import com.bank.uploadfileanddatapersistdb.entity.Employee;
import com.bank.uploadfileanddatapersistdb.exception.EmployeeNotFoundException;
import com.bank.uploadfileanddatapersistdb.mapper.EmployeeMapper;
import com.bank.uploadfileanddatapersistdb.repository.EmployeeRepository;
import com.bank.uploadfileanddatapersistdb.interfaces.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository repository;
    private final EmployeeMapper mapper;



//    @Transactional :
//    soit toute la liste est sauvegardée
//    soit rien du tout si une erreur arrive
//    → ça garantit la cohérence des données.

    @Override
    @Transactional
    public void saveAll(List<EmployeeDto> employees) {
        List<Employee> entities = employees.stream()
                .map(mapper::toEntity)
                .toList();
        repository.saveAll(entities);
    }

    @Override
    public List<Employee> getAll() {
        return repository.findAll();
    }

    @Override
    public Employee getEmployeeById(Long id) {
        return repository.findById(id).orElseThrow(()-> new EmployeeNotFoundException("Employee not found with id: " + id));
    }

    @Override
    public boolean existsById(Long id) {
        return repository.existsById(id);
    }
}
