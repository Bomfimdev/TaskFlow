package com.taskmanager.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.taskmanager.dto.AuthRequestDTO;
import com.taskmanager.dto.AuthResponseDTO;
import com.taskmanager.service.JwtUtilService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtUtilService jwtUtilService;

    public AuthController(AuthenticationManager authenticationManager,
                          UserDetailsService userDetailsService,
                          JwtUtilService jwtUtilService) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtUtilService = jwtUtilService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody AuthRequestDTO authRequest) {
        logger.info("Recebendo solicitação de login para o usuário: {}", authRequest.getUsername());
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword())
            );
            logger.debug("Autenticação bem-sucedida para o usuário: {}", authRequest.getUsername());

            final UserDetails userDetails = userDetailsService.loadUserByUsername(authRequest.getUsername());
            final String jwt = jwtUtilService.generateToken(userDetails.getUsername());

            logger.info("Token JWT gerado com sucesso para o usuário: {}", authRequest.getUsername());
            return ResponseEntity.ok(new AuthResponseDTO(jwt));
        } catch (AuthenticationException e) {
            logger.error("Falha na autenticação para o usuário {}: {}", authRequest.getUsername(), e.getMessage());
            return ResponseEntity.status(401).body("Usuário ou senha incorretos");
        }
    }

    @PostMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String token) {
        logger.info("Validando token JWT: {}", token);
        try {
            String jwt = token.startsWith("Bearer ") ? token.substring(7) : token;
            UserDetails userDetails = userDetailsService.loadUserByUsername(jwtUtilService.extractUsername(jwt));
            if (jwtUtilService.validateToken(jwt, userDetails.getUsername())) {
                logger.info("Token válido para o usuário: {}", userDetails.getUsername());
                return ResponseEntity.ok("Token válido");
            } else {
                logger.warn("Token inválido ou expirado para o usuário: {}", userDetails.getUsername());
                return ResponseEntity.status(401).body("Token inválido ou expirado");
            }
        } catch (Exception e) {
            logger.error("Erro ao validar o token: {}", e.getMessage());
            return ResponseEntity.status(401).body("Erro ao validar o token");
        }
    }
}