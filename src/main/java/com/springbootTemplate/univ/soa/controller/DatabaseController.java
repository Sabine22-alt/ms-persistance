package com.springbootTemplate.univ.soa.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/database")
public class DatabaseController {

    @Autowired
    private DataSource dataSource;

    @GetMapping("/test")
    public Map<String, Object> testDatabaseConnections() {
        Map<String, Object> result = new HashMap<>();

        // Test MySQL connection
        try {
            Connection connection = dataSource.getConnection();
            String dbName = connection.getCatalog();
            connection.close();
            result.put("mysql", "✅ MySQL connection successful");
            result.put("database", dbName);
            result.put("status", "ready");
        } catch (Exception e) {
            result.put("mysql", "❌ MySQL connection failed: " + e.getMessage());
            result.put("status", "error");
        }

        return result;
    }
}
