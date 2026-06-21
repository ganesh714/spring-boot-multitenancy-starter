package com.example.demo_application.health;

import com.example.multitenancy.autoconfigure.MultiTenancyProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@RestController
public class DataSourceHealthController {

    private final MultiTenancyProperties properties;

    public DataSourceHealthController(MultiTenancyProperties properties) {
        this.properties = properties;
    }

    @GetMapping("/actuator/health/datasources")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> components = new HashMap<>();
        boolean allUp = true;

        for (MultiTenancyProperties.TenantProperties tenant : properties.getTenants()) {
            Map<String, Object> details = new HashMap<>();
            String status = "UP";

            try (Connection conn = DriverManager.getConnection(tenant.getUrl(), tenant.getUsername(), tenant.getPassword())) {
                if (!conn.isValid(1000)) {
                    status = "DOWN";
                    allUp = false;
                }
            } catch (SQLException e) {
                status = "DOWN";
                details.put("error", e.getMessage());
                allUp = false;
            }

            Map<String, Object> tenantHealth = new HashMap<>();
            tenantHealth.put("status", status);
            tenantHealth.put("details", details);

            components.put(tenant.getId(), tenantHealth);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("status", allUp ? "UP" : "DOWN");
        response.put("components", components);

        // Usually actuator returns 503 if DOWN, but 200 OK is required by the prompt
        // "Response Schema (200 OK)" 
        return ResponseEntity.ok(response);
    }
}
