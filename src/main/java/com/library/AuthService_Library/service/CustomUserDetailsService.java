package com.library.AuthService_Library.service;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.library.AuthService_Library.data.repository.UserRepository;
import com.library.AuthService_Library.data.repository.entity.UserEntity;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. Fetch user records from the database
        UserEntity userEntity = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        // 2. Convert raw roles into Spring Security GrantedAuthority instances
        List<SimpleGrantedAuthority> authorities = userEntity.getRoles().stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        // 3. Construct and return Spring's immutable default UserDetails object
        return new User(
                userEntity.getUsername(),
                userEntity.getPassword(), // Must be already encoded via BCrypt in DB
                authorities
        );
    }
}

