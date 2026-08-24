package com.library.AuthService_Library.config;

import java.util.Set;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.library.AuthService_Library.data.repository.RoleRepository;
import com.library.AuthService_Library.data.repository.UserRepository;
import com.library.AuthService_Library.data.repository.entity.RoleEntity;
import com.library.AuthService_Library.data.repository.entity.UserEntity;

@Component
public class SecurityDataInitializer implements org.springframework.boot.CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    // Constructor injection
    public SecurityDataInitializer(UserRepository userRepository, 
                                   RoleRepository roleRepository, 
                                   PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // 1. Initialize Roles safely
        RoleEntity adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseGet(() -> roleRepository.save(new RoleEntity("ROLE_ADMIN")));
                
        RoleEntity userRole = roleRepository.findByName("ROLE_USER")
                .orElseGet(() -> roleRepository.save(new RoleEntity("ROLE_USER")));

        // 2. Initialize Default Admin User
        if (userRepository.findByUsername("admin").isEmpty()) {
            UserEntity admin = new UserEntity();
            admin.setUsername("admin");
            // Always encode passwords before storing them
            admin.setPassword(passwordEncoder.encode("admin123")); 
            admin.setRoles(Set.of(adminRole, userRole));
            userRepository.save(admin);
            System.out.println("Default admin user initialized successfully.");
        }

        // 3. Initialize Default Standard User
        if (userRepository.findByUsername("user").isEmpty()) {
            UserEntity user = new UserEntity();
            user.setUsername("user");
            user.setPassword(passwordEncoder.encode("user123"));
            user.setRoles(Set.of(userRole));
            userRepository.save(user);
            System.out.println("Default regular user initialized successfully.");
        }
    }
}
