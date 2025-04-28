package com.taskmanager.config;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class PublicEndpointFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(PublicEndpointFilter.class);

    private final JwtRequestFilter jwtRequestFilter;
    private final List<String> publicEndpoints = Arrays.asList(
        "/api/auth/login",
        "/api/users"
    );

    public PublicEndpointFilter(JwtRequestFilter jwtRequestFilter) {
        this.jwtRequestFilter = jwtRequestFilter;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain chain)
            throws ServletException, IOException {
        String requestUri = request.getRequestURI();
        String method = request.getMethod();

        logger.debug("Processando requisição: {} {}", method, requestUri);

        boolean isPublicEndpoint = publicEndpoints.stream()
            .anyMatch(endpoint -> requestUri.startsWith(endpoint) && "POST".equalsIgnoreCase(method));

        if (isPublicEndpoint) {
            logger.info("Ignorando filtro JWT para endpoint público: {} {}", method, requestUri);
            chain.doFilter(request, response);
        } else {
            logger.info("Aplicando filtro JWT para: {} {}", method, requestUri);
            jwtRequestFilter.doFilter(request, response, chain);
        }
    }
}