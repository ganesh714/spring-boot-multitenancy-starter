package com.example.multitenancy.interceptor;

import com.example.multitenancy.autoconfigure.MultiTenancyProperties;
import com.example.multitenancy.context.TenantContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

public class TenantInterceptor implements HandlerInterceptor {

    private static final String TENANT_HEADER = "X-Tenant-ID";
    private final MultiTenancyProperties properties;

    public TenantInterceptor(MultiTenancyProperties properties) {
        this.properties = properties;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Skip actuator endpoints if needed, but the requirements just say "Requests made to tenant-aware endpoints"
        // For simplicity we will apply it to /api/** endpoints via WebMvcConfigurer
        String tenantId = request.getHeader(TENANT_HEADER);
        if (tenantId == null || tenantId.trim().isEmpty()) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Bad Request", "X-Tenant-ID header is missing");
            return false;
        }

        boolean isValidTenant = properties.getTenants().stream()
                .anyMatch(t -> t.getId().equals(tenantId));

        if (!isValidTenant) {
            sendError(response, HttpServletResponse.SC_NOT_FOUND, "Not Found", "Tenant not found: " + tenantId);
            return false;
        }

        TenantContext.setCurrentTenant(tenantId);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        TenantContext.clear();
    }

    private void sendError(HttpServletResponse response, int status, String error, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        String json = String.format("{\"error\": \"%s\", \"message\": \"%s\"}", error, message);
        response.getWriter().write(json);
    }
}
