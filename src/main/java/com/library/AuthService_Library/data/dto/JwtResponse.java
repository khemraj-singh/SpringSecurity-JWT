package com.library.AuthService_Library.data.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Data
@AllArgsConstructor
@Getter

public class JwtResponse {
    private final String token;
    private final String type = "Bearer";
}

