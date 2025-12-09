package com.bank.uploadfileanddatapersistdb.DTO;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDto{
    private Long id;
    private String firstName;
    private String lastName;
    private String position;
    private String department;
    private LocalDate hireDate;
    private BigDecimal salary;
}
