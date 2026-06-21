package com.example.multitenancy.autoconfigure;

import com.example.multitenancy.datasource.TenantAwareRoutingDataSource;
import com.example.multitenancy.interceptor.TenantInterceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableConfigurationProperties(MultiTenancyProperties.class)
@ConditionalOnProperty(prefix = "multitenancy", name = "enabled", havingValue = "true", matchIfMissing = true)
public class MultiTenancyAutoConfiguration {

    private final MultiTenancyProperties properties;

    public MultiTenancyAutoConfiguration(MultiTenancyProperties properties) {
        this.properties = properties;
    }

    @Bean
    @Primary
    public DataSource dataSource() {
        TenantAwareRoutingDataSource customDataSource = new TenantAwareRoutingDataSource();
        Map<Object, Object> targetDataSources = new HashMap<>();

        for (MultiTenancyProperties.TenantProperties tenant : properties.getTenants()) {
            DataSource dataSource = DataSourceBuilder.create()
                    .driverClassName(tenant.getDriverClassName())
                    .url(tenant.getUrl())
                    .username(tenant.getUsername())
                    .password(tenant.getPassword())
                    .build();
            targetDataSources.put(tenant.getId(), dataSource);
        }

        customDataSource.setTargetDataSources(targetDataSources);
        
        // If there's no default data source, you could set one here if needed,
        // but for multi-tenancy we strictly route by header. 
        // We'll just set a default to the first one if present to avoid errors on startup 
        // before any request comes in, e.g., for Hibernate initialization.
        if (!properties.getTenants().isEmpty()) {
            DataSource defaultDataSource = DataSourceBuilder.create()
                    .driverClassName(properties.getTenants().get(0).getDriverClassName())
                    .url(properties.getTenants().get(0).getUrl())
                    .username(properties.getTenants().get(0).getUsername())
                    .password(properties.getTenants().get(0).getPassword())
                    .build();
            customDataSource.setDefaultTargetDataSource(defaultDataSource);
        }

        customDataSource.afterPropertiesSet();
        return customDataSource;
    }

    @Bean
    public TenantInterceptor tenantInterceptor() {
        return new TenantInterceptor(properties);
    }

    @Bean
    public WebMvcConfigurer webMvcConfigurer(TenantInterceptor tenantInterceptor) {
        return new WebMvcConfigurer() {
            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                // Apply interceptor to all API endpoints, we assume /api/** here
                // as per standard setup. Or we can just apply to all and exclude /actuator/**.
                registry.addInterceptor(tenantInterceptor)
                        .addPathPatterns("/api/**");
            }
        };
    }
}
