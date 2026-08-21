package com.library.AuthService_Library.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.library.AuthService_Library.data.dto.JwtResponse;
import com.library.AuthService_Library.data.dto.LoginRequest;
import com.library.AuthService_Library.util.JwtTokenUtil;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtil jwtTokenUtil;

    // Constructor injection is the cleanest practice for dependencies
    public AuthenticationController(AuthenticationManager authenticationManager, JwtTokenUtil jwtTokenUtil) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        
        // 1. Verify credentials using standard Spring Security core systems
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                loginRequest.getUsername(),
                loginRequest.getPassword()
            )
        );

        // 2. Commit the successfully validated context to the execution thread
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 3. Issue and package your custom token signature
        String jwtToken = jwtTokenUtil.generateToken(authentication.getName());

        // 4. Dispatch the string back to the user client
        return ResponseEntity.ok(new JwtResponse(jwtToken));
    }
}

