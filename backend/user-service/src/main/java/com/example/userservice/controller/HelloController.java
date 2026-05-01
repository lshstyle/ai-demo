package com.example.userservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/hello")
public class HelloController {

    @GetMapping
    public String hello() {
        return "Hello from Spring Cloud Backend!";
    }

    @GetMapping("/time")
    public String time() {
        return "Current time: " + java.time.LocalDateTime.now();
    }
}
