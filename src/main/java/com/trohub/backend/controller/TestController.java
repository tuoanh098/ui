package com.trohub.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping("/whoami")
    public ResponseEntity<?> whoami() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return ResponseEntity.ok("No authentication in context");
        }
        var map = new java.util.HashMap<String, Object>();
        map.put("principal", auth.getPrincipal());
        map.put("name", auth.getName());
        List<String> roles = auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList());
        map.put("authorities", roles);
        map.put("authenticated", auth.isAuthenticated());
        return ResponseEntity.ok(map);
    }
}

