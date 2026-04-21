package com.trohub.backend.dto;

import lombok.*;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaiKhoanDto {
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private Boolean active;
    private Set<String> roles;
}

