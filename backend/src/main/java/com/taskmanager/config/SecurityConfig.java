package com.taskmanager.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
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

    private final JwtRequestFilter jwtRequestFilter;
    private final ApplicationContext applicationContext;

    public SecurityConfig(JwtRequestFilter jwtRequestFilter, ApplicationContext applicationContext) {
        this.jwtRequestFilter = jwtRequestFilter;
        this.applicationContext = applicationContext;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        logger.info("Configurando regras de segurança para HttpSecurity...");
        http
                .csrf(csrf -> {
                    logger.debug("Desativando CSRF...");
                    csrf.disable();
                })
                .sessionManagement(session -> {
                    logger.debug("Configurando sessão como STATELESS...");
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
                })
                .authorizeHttpRequests(auth -> {
                    logger.debug("Definindo regras de autorização...");
                    auth
                            .requestMatchers("/api/auth/**").permitAll()
                            .requestMatchers("/api/users").permitAll()
                            .requestMatchers("/api/tasks/**").authenticated()
                            .requestMatchers("/api/tags/**").authenticated()
                            .anyRequest().denyAll();
                    logger.debug("Regras de autorização definidas: /api/auth/** (permitAll), /api/users (permitAll), /api/tasks/** (authenticated), /api/tags/** (authenticated), anyRequest (denyAll)");
                })
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        logger.info("SecurityFilterChain configurado com sucesso.");
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        logger.info("Configurando AuthenticationManager...");
        UserDetailsService userDetailsService = applicationContext.getBean(UserDetailsService.class);
        AuthenticationManagerBuilder authenticationManagerBuilder =
                http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder());
        logger.info("AuthenticationManager configurado com sucesso.");
        return authenticationManagerBuilder.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        logger.info("Configurando PasswordEncoder como BCryptPasswordEncoder...");
        return new BCryptPasswordEncoder();
    }
}