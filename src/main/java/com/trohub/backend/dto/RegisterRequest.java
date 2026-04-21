package com.trohub.backend.dto;

import lombok.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequest {
    @NotBlank(message = "username is required")
    @Size(min = 3, max = 64, message = "username must be between 3 and 64 characters")
    private String username;

    @NotBlank(message = "password is required")
    @Size(min = 8, message = "password must be at least 8 characters")
    private String password;

    @Email(message = "email invalid")
    private String email;

    @NotBlank(message = "fullName is required")
    @Size(max = 255)
    private String fullName;

    // optional tenant info
    @Size(max = 64)
    private String cccd;

    @Pattern(regexp = "^[0-9+\\- ]{7,20}$", message = "sdt must contain only digits, spaces, + or - and be 7-20 chars")
    private String sdt;

    private String diaChi;

    @Positive(message = "sophong must be positive")
    private Long sophong; // optional room id to link
}

