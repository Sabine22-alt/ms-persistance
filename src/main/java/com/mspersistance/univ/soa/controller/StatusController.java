package com.mspersistance.univ.soa.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class StatusController {

    @Value("${spring.application.name:application}")
    private String appName;

    @GetMapping("/")
    public ResponseEntity<String> home() {
        return ResponseEntity.ok("API is running");
    }

    @GetMapping("/api/status")
    public ResponseEntity<Map<String, Object>> status() {
        Map<String, Object> map = new HashMap<>();
        map.put("applicationName", appName);
        map.put("version", "0.0.1-SNAPSHOT");
        map.put("status", "running");
        return ResponseEntity.ok(map);
    }
}

