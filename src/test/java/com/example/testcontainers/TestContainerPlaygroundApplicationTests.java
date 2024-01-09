package com.example.testcontainers;

import com.example.testcontainers.domain.Employee;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class TestContainerPlaygroundApplicationTests {

    @Container
    @ServiceConnection
    static MariaDBContainer<?> mariaDBContainer = new MariaDBContainer<>(DockerImageName.parse("mariadb:latest"));

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private TestRestTemplate restTemplate;

	@Test
    void noEmployeesWhenNoneAdded() {
        Integer rowCount = JdbcTestUtils.countRowsInTable(jdbcTemplate, "employees");

        assertThat(rowCount).isEqualTo(0);
    }

    @Test
    void noEmployeesFromAPIWhenNoneAdded() {
        ResponseEntity<List<Employee>> response = getAllEmployees();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    void employeeSavedWhenSubmitted() {
        // language=json
        String requestJson = """
                {
                    "firstName": "Craig",
                    "lastName": "Johnston"
                }
                    """;

		postEmployee(requestJson);

		int rowCount = JdbcTestUtils.countRowsInTableWhere(jdbcTemplate, "employee", "firstName = 'Craig' and lastName = 'Johnston'");

        assertThat(rowCount).isEqualTo(1);
    }

	@Test
    void employeeSavedAndRetrievable() {
        // language=json
        String requestJson = """
                {
                    "firstName": "Craig",
                    "lastName": "Johnston"
                }
                    """;

		postEmployee(requestJson);

        ResponseEntity<List<Employee>> allEmployees = getAllEmployees();

        assertThat(allEmployees.getBody()).hasSize(1)
                .allSatisfy(e -> {
                    assertThat(e.getFirstName()).isEqualTo("Craig");
                    assertThat(e.getLastName()).isEqualTo("Johnston");
                });
    }

    private ResponseEntity<List<Employee>> getAllEmployees() {
        return restTemplate.exchange("/employees", HttpMethod.GET, HttpEntity.EMPTY, new ParameterizedTypeReference<>() {
        });
    }

	private void postEmployee(String requestJson) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		restTemplate.exchange("/employees", HttpMethod.POST, new HttpEntity<>(requestJson, headers), Void.class);
	}
}
