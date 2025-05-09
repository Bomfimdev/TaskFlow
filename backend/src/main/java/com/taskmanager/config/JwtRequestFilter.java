package com.taskmanager.config;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.taskmanager.service.JwtUtilService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtRequestFilter.class);

    private final ApplicationContext applicationContext;

    @Autowired
    private JwtUtilService jwtUtilService;

    public JwtRequestFilter(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        final String requestURI = request.getRequestURI();
        final String method = request.getMethod();
        logger.info("Processando requisição: {} {}", method, requestURI);

        final String authorizationHeader = request.getHeader("Authorization");
        logger.debug("Cabeçalho Authorization: {}", authorizationHeader);

        String username = null;
        String jwt = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            logger.debug("Token JWT extraído: {}", jwt);
            try {
                username = jwtUtilService.extractUsername(jwt);
                logger.debug("Usuário extraído do token: {}", username);
            } catch (Exception e) {
                logger.error("Erro ao extrair usuário do token JWT: {}", e.getMessage(), e);
            }
        } else {
            logger.debug("Cabeçalho Authorization não contém 'Bearer ' ou está ausente.");
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            logger.debug("Carregando UserDetails para o usuário: {}", username);
            UserDetailsService userDetailsService = applicationContext.getBean(UserDetailsService.class);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            logger.debug("UserDetails carregado: {}", userDetails.getUsername());
            logger.debug("Autoridades do usuário: {}", userDetails.getAuthorities());

            try {
                boolean isTokenValid = jwtUtilService.validateToken(jwt, username);
                logger.debug("Resultado da validação do token: {}", isTokenValid);
                if (isTokenValid) {
                    logger.debug("Token JWT validado com sucesso para o usuário: {}", username);
                    UsernamePasswordAuthenticationToken authenticationToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                    logger.info("Contexto de segurança preenchido para o usuário: {}", username);
                    logger.debug("Autenticação no SecurityContextHolder após preenchimento: {}", SecurityContextHolder.getContext().getAuthentication());
                    if (SecurityContextHolder.getContext().getAuthentication() == null) {
                        logger.error("Falha ao preencher o SecurityContextHolder para o usuário: {}", username);
                    }
                } else {
                    logger.warn("Token JWT inválido para o usuário: {}", username);
                }
            } catch (Exception e) {
                logger.error("Erro ao validar o token JWT: {}", e.getMessage(), e);
            }
        } else if (username == null && authorizationHeader != null) {
            logger.warn("Não foi possível extrair o usuário do token JWT para a requisição: {} {}", method, requestURI);
        } else {
            logger.debug("Nenhum usuário autenticado ou token ausente para a requisição: {} {}", method, requestURI);
        }

        logger.debug("Estado final do SecurityContextHolder antes de prosseguir: {}", SecurityContextHolder.getContext().getAuthentication());
        chain.doFilter(request, response);
    }
}