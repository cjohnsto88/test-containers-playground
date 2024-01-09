package com.example.testcontainers.controller;

import com.example.testcontainers.dao.EmployeeDao;
import com.example.testcontainers.domain.Employee;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/employees")
public class EmployeeController {

    private final EmployeeDao employeeDao;

    public EmployeeController(EmployeeDao employeeDao) {
        this.employeeDao = employeeDao;
    }

    @GetMapping
    public List<Employee> getAllEmployees() {
        return employeeDao.getAllEmployees();
    }

    @PostMapping
    public void addEmployee(@RequestBody Employee employee) {
        employeeDao.addEmployee(employee);
    }



}
