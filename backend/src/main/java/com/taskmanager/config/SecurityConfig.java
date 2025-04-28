package com.taskmanager.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtRequestFilter jwtRequestFilter;

    @Bean
    public PublicEndpointFilter publicEndpointFilter() {
        return new PublicEndpointFilter(jwtRequestFilter);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        logger.info("Iniciando configuração do Spring Security...");

        http
            .csrf(csrf -> {
                logger.debug("Desativando CSRF...");
                csrf.disable();
            })
            .sessionManagement(session -> {
                logger.debug("Configurando sessão como stateless...");
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
            })
            .authorizeHttpRequests(auth -> {
                logger.debug("Configurando regras de autorização...");
                auth
                    .requestMatchers(HttpMethod.POST, "/api/auth/**").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/users").permitAll()
                    .anyRequest().authenticated();
            })
            .addFilterBefore(publicEndpointFilter(), UsernamePasswordAuthenticationFilter.class);

        logger.info("CSRF desativado.");
        logger.info("Sessão configurada como stateless.");
        logger.info("Regras de autorização configuradas: /api/auth/** (POST) e /api/users (POST) liberados, outros endpoints exigem autenticação.");
        logger.info("Filtro personalizado (PublicEndpointFilter) configurado com exclusão de endpoints públicos.");
        logger.info("Configuração do Spring Security concluída.");
        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        logger.info("Configurando AuthenticationProvider...");
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        logger.debug("AuthenticationProvider configurado com UserDetailsService e PasswordEncoder.");
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        logger.info("Configurando AuthenticationManager...");
        AuthenticationManager manager = authenticationConfiguration.getAuthenticationManager();
        logger.debug("AuthenticationManager configurado com sucesso.");
        return manager;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        logger.info("Configurando BCryptPasswordEncoder...");
        return new BCryptPasswordEncoder();
    }
}