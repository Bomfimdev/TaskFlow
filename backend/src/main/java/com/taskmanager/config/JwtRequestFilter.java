package com.taskmanager.config;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.taskmanager.service.JwtUtilService;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtRequestFilter.class);

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtUtilService jwtUtilService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        final String requestTokenHeader = request.getHeader("Authorization");
        logger.debug("Processando requisição: {} {}", request.getMethod(), request.getRequestURI());
        logger.debug("Cabeçalho Authorization: {}", requestTokenHeader);

        String username = null;
        String jwtToken = null;

        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            jwtToken = requestTokenHeader.substring(7);
            logger.debug("Token JWT extraído: {}", jwtToken);
            try {
                username = jwtUtilService.extractUsername(jwtToken);
                logger.debug("Username extraído do token: {}", username);
            } catch (ExpiredJwtException e) {
                logger.warn("Token JWT expirado: {}", e.getMessage());
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token expirado");
                return;
            } catch (SignatureException | MalformedJwtException e) {
                logger.warn("Token JWT inválido: {}", e.getMessage());
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token inválido");
                return;
            } catch (Exception e) {
                logger.error("Erro ao extrair username do token: {}", e.getMessage(), e);
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Erro ao processar token");
                return;
            }
        } else {
            logger.debug("Nenhum token JWT encontrado ou formato inválido no cabeçalho Authorization.");
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            logger.debug("Carregando detalhes do usuário: {}", username);
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
            if (userDetails == null) {
                logger.error("Usuário não encontrado no banco de dados: {}", username);
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Usuário não encontrado");
                return;
            }
            logger.debug("Detalhes do usuário carregados: {}", userDetails.getUsername());

            logger.debug("Validando token JWT...");
            if (jwtUtilService.validateToken(jwtToken, userDetails)) {
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                logger.debug("Autenticação configurada para o usuário: {}", username);
            } else {
                logger.warn("Token JWT inválido para o usuário: {}", username);
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token inválido");
                return;
            }
        } else if (username == null && requestTokenHeader != null) {
            logger.warn("Nenhum username extraído do token para a requisição: {} {}", request.getMethod(), request.getRequestURI());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token inválido ou ausente");
            return;
        }

        logger.debug("Prosseguindo com a requisição...");
        chain.doFilter(request, response);
    }
}