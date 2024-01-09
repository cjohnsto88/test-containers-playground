package com.example.testcontainers.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class SomeRemoteService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SomeRemoteService.class);

    private final RestTemplate restTemplate;

    public SomeRemoteService(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    public String callApi() {
        String responseBody = restTemplate.getForObject("/some/thing", String.class);

        LOGGER.info("The response body is: {}", responseBody);

        return responseBody;
    }

}
