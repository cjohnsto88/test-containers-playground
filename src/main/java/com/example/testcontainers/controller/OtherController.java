package com.example.testcontainers.controller;

import com.example.testcontainers.service.SomeRemoteService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/remote-service")
public class OtherController {

    private final SomeRemoteService someRemoteService;

    public OtherController(SomeRemoteService someRemoteService) {
        this.someRemoteService = someRemoteService;
    }

    @GetMapping
    public String calRemoteService() {
        return someRemoteService.callApi();
    }
}
