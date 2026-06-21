package com.example.demo_application.config;

import com.example.multitenancy.autoconfigure.MultiTenancyProperties;
import com.example.multitenancy.context.TenantContext;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

@Component
public class TenantSchemaInitializer implements CommandLineRunner {

    private final MultiTenancyProperties properties;
    private final DataSource dataSource;

    public TenantSchemaInitializer(MultiTenancyProperties properties, DataSource dataSource) {
        this.properties = properties;
        this.dataSource = dataSource;
    }

    @Override
    public void run(String... args) throws Exception {
        for (MultiTenancyProperties.TenantProperties tenant : properties.getTenants()) {
            TenantContext.setCurrentTenant(tenant.getId());
            try (Connection conn = dataSource.getConnection();
                 Statement stmt = conn.createStatement()) {
                 
                String sql = "CREATE TABLE IF NOT EXISTS users (" +
                             "id BIGSERIAL PRIMARY KEY, " +
                             "name VARCHAR(255), " +
                             "email VARCHAR(255) UNIQUE" +
                             ")";
                stmt.execute(sql);
                System.out.println("Initialized schema for tenant: " + tenant.getId());
            } finally {
                TenantContext.clear();
            }
        }
    }
}
