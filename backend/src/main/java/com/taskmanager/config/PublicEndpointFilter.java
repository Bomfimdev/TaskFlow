package com.taskmanager.config;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class PublicEndpointFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(PublicEndpointFilter.class);

    private final List<String> publicEndpoints;

    public PublicEndpointFilter(@Value("${app.public-endpoints}") String publicEndpoints) {
        this.publicEndpoints = Arrays.asList(publicEndpoints.split(","));
        logger.info("Endpoints públicos configurados: {}", this.publicEndpoints);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        logger.info("Aplicando filtro de endpoints públicos para: {} {}", method, requestURI);

        // Verificar se o endpoint é público
        boolean isPublic = publicEndpoints.stream()
                .anyMatch(endpoint -> {
                    String[] parts = endpoint.trim().split(":");
                    String path = parts[0];
                    String allowedMethod = parts.length > 1 ? parts[1] : null;
                    boolean pathMatches = requestURI.matches(path.replace("**", ".*"));
                    boolean methodMatches = allowedMethod == null || method.equalsIgnoreCase(allowedMethod);
                    logger.debug("Verificando endpoint: {} (método: {}). Path matches: {}, Method matches: {}", path, allowedMethod, pathMatches, methodMatches);
                    return pathMatches && methodMatches;
                });

        if (isPublic) {
            logger.info("Endpoint {} {} é público, permitindo acesso sem autenticação.", method, requestURI);
        } else {
            logger.info("Endpoint {} {} não é público, passando para o próximo filtro.", method, requestURI);
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        // Não aplicar o filtro para requisições autenticadas
        return request.getRequestURI().startsWith("/api/tasks");
    }
}