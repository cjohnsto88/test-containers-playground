package com.example.testcontainers.dao;

import com.example.testcontainers.domain.Employee;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EmployeeDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmployeeDao.class);

    private final JdbcTemplate jdbcTemplate;

    public EmployeeDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Employee> getAllEmployees() {
        LOGGER.info("Getting all employees from DB");

        return jdbcTemplate.query("SELECT firstName, lastName FROM employee", (rs, rowNum) -> {
            Employee employee = new Employee();
            employee.setFirstName(rs.getString("firstName"));
            employee.setLastName(rs.getString("lastName"));

            return employee;
        });
    }

    public void addEmployee(Employee employee) {
        LOGGER.info("Adding employee to DB: {}", employee);

        jdbcTemplate.update("INSERT INTO employee (firstName, lastName) VALUES (?, ?)", ps -> {
            ps.setString(1, employee.getFirstName());
            ps.setString(2, employee.getLastName());
        });
    }
}
