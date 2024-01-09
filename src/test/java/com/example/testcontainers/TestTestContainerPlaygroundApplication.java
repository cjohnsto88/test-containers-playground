package com.example.testcontainers;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.boot.web.client.RootUriTemplateHandler;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.MSSQLServerContainer;
import org.testcontainers.utility.DockerImageName;
import org.wiremock.integrations.testcontainers.WireMockContainer;

@TestConfiguration(proxyBeanMethods = false)
public class TestTestContainerPlaygroundApplication {

    @Bean
    @ServiceConnection
    public MSSQLServerContainer<?> mariaDbContainer() {
        return new MSSQLServerContainer<>(DockerImageName.parse("mcr.microsoft.com/mssql/server:2017-CU12")).acceptLicense();
    }

    @Bean
    public WireMockContainer wireMockContainer() {
        return new WireMockContainer("wiremock/wiremock:2.35.0")
                .withMappingFromResource("example", "wiremock/stub.json");
    }

    @Bean
    public RestTemplateCustomizer restTemplateCustomizer(WireMockContainer wireMockContainer) {
        return restTemplate -> RootUriTemplateHandler.addTo(restTemplate, String.format("http://localhost:%d", wireMockContainer.getPort()));
    }

    public static void main(String[] args) {
        SpringApplication.from(TestContainerPlaygroundApplication::main).with(TestTestContainerPlaygroundApplication.class).run(args);
    }

}
