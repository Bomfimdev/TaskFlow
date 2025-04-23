package com.taskmanager.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Desativa CSRF (para testes)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/users").permitAll() // Libera o endpoint /api/users
                .anyRequest().authenticated() // Outros endpoints exigem autenticação
            );
        return http.build();
    }
}