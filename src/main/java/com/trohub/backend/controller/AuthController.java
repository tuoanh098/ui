package com.trohub.backend.controller;

import com.trohub.backend.dto.LoginRequest;
import com.trohub.backend.dto.LoginResponse;
import com.trohub.backend.dto.TaiKhoanDto;
import com.trohub.backend.service.AuthService;
import com.trohub.backend.dto.RegisterRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@jakarta.validation.Valid @RequestBody LoginRequest request) {
        LoginResponse resp = authService.login(request);
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/register")
    public ResponseEntity<com.trohub.backend.dto.TaiKhoanDto> register(@jakarta.validation.Valid @RequestBody RegisterRequest req) {
        com.trohub.backend.dto.TaiKhoanDto created = authService.register(req);
        return ResponseEntity.ok(created);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_LANDLORD')")
    @GetMapping("/users")
    public ResponseEntity<List<TaiKhoanDto>> users(
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "role", required = false) String role) {
        return ResponseEntity.ok(authService.listUsers(q, role));
    }
}

