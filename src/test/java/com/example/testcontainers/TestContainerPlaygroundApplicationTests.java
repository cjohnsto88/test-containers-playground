package com.example.testcontainers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.boot.web.client.RootUriTemplateHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.testcontainers.containers.MSSQLServerContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.wiremock.integrations.testcontainers.WireMockContainer;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class TestContainerPlaygroundApplicationTests {

    @Container
    @ServiceConnection
    static MSSQLServerContainer<?> msSqlContainer = new MSSQLServerContainer<>(DockerImageName.parse("mcr.microsoft.com/mssql/server:2017-CU12")).acceptLicense();

    @Container
    static WireMockContainer wireMockContainer = new WireMockContainer("wiremock/wiremock:2.35.0")
            .withMappingFromResource("example", "wiremock/stub.json");

    @TestConfiguration
    static class TestContainerPlaygroundApplicationTestsConfig {

        @Bean
        public RestTemplateCustomizer restTemplateCustomizer() {
            return restTemplate -> RootUriTemplateHandler.addTo(restTemplate, String.format("http://localhost:%d", wireMockContainer.getPort()));
        }

    }

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private TestRestTemplate restTemplate;

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
    void wiremockStubReturnsExpectedResponse() {
        ResponseEntity<String> response = restTemplate.getForEntity("/remote-service", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("Hello, world!");
    }

    private void postEmployee(String requestJson) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		restTemplate.exchange("/employees", HttpMethod.POST, new HttpEntity<>(requestJson, headers), Void.class);
	}
}
