package com.taskmanager.controller;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.taskmanager.dto.LoginRequest;
import com.taskmanager.service.JwtUtilService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtUtilService jwtUtilService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            logger.info("Tentando autenticar usuário: {}", loginRequest.getUsername());
            logger.debug("Credenciais recebidas: username={}, password={}", loginRequest.getUsername(), loginRequest.getPassword());

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
            );

            logger.debug("Autenticação bem-sucedida: {}", authentication.getName());

            UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getUsername());
            logger.debug("UserDetails carregado: username={}, authorities={}", userDetails.getUsername(), userDetails.getAuthorities());

            String jwt = jwtUtilService.generateToken(userDetails);
            logger.debug("Token JWT gerado: {}", jwt);

            Map<String, String> response = new HashMap<>();
            response.put("token", jwt);

            logger.info("Usuário autenticado com sucesso: {}", loginRequest.getUsername());
            return ResponseEntity.ok(response);
        } catch (AuthenticationException e) {
            logger.error("Falha na autenticação para o usuário {}: {}", loginRequest.getUsername(), e.getMessage());
            return ResponseEntity.status(401).body("Credenciais inválidas");
        } catch (Exception e) {
            logger.error("Erro inesperado ao processar login para o usuário {}: {}", loginRequest.getUsername(), e.getMessage(), e);
            return ResponseEntity.status(500).body("Erro interno no servidor");
        }
    }
}