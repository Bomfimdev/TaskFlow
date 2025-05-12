package com.taskmanager.service;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.taskmanager.entity.User;
import com.taskmanager.repository.UserRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.debug("Tentando carregar usuário: {}", username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    logger.error("Usuário não encontrado: {}", username);
                    return new UsernameNotFoundException("Usuário não encontrado: " + username);
                });
        logger.debug("Usuário encontrado: {}", user.getUsername());
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                Arrays.asList(new SimpleGrantedAuthority("ROLE_USER")) // Adiciona a autoridade ROLE_USER
        );
    }
}