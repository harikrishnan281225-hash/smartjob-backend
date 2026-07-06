package com.smartjob.security;

import com.smartjob.entity.User;
import com.smartjob.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        String password = (user.getPassword() != null) ? user.getPassword() : "";

        // Robust role formatting: handle nulls, trim whitespace, and enforce "ROLE_" prefix
        String rawRole = (user.getRole() != null) ? user.getRole().trim().toUpperCase() : "USER";
        String formattedRole = rawRole.startsWith("ROLE_") ? rawRole : "ROLE_" + rawRole;

        List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority(formattedRole)
        );

        logger.info("DEBUG: User '{}' loaded with authority: {}", username, formattedRole);

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                password,
                authorities
        );
    }
}