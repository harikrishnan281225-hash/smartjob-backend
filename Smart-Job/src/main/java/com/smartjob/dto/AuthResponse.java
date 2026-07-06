package com.smartjob.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor // Added for better compatibility with JSON serializers
@Schema(description = "Response containing the authentication token and user role")
public class AuthResponse {

    @Schema(description = "The JWT access token", example = "eyJhbGciOiJIUzI1...")
    private String token;

    @Schema(description = "The role assigned to the user", example = "ROLE_USER")
    private String role;
}