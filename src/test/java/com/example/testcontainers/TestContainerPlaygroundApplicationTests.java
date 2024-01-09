package com.example.testcontainers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.MariaDBContainer;
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
    static MariaDBContainer<?> mariaDBContainer = new MariaDBContainer<>(DockerImageName.parse("mariadb:latest"));

    @Container
    static WireMockContainer wireMockContainer = new WireMockContainer("wiremock/wiremock:2.35.0")
            .withMappingFromResource("example", "wiremock/stub.json");

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
        RestTemplate restTemplate = new RestTemplateBuilder().rootUri("http://localhost:" + wireMockContainer.getPort())
                .build();

        String responseBody = restTemplate.getForObject("/some/thing", String.class);

        assertThat(responseBody).isEqualTo("Hello, world!");
    }

    private void postEmployee(String requestJson) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		restTemplate.exchange("/employees", HttpMethod.POST, new HttpEntity<>(requestJson, headers), Void.class);
	}
}
