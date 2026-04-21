package com.trohub.backend.controller;

import com.trohub.backend.dto.KhachVaoRaDto;
import com.trohub.backend.service.KhachVaoRaService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/guest-entries")
public class KhachVaoRaController {

    private final KhachVaoRaService khachVaoRaService;

    public KhachVaoRaController(KhachVaoRaService khachVaoRaService) {
        this.khachVaoRaService = khachVaoRaService;
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_USER')")
    @PostMapping
    public ResponseEntity<KhachVaoRaDto> create(@jakarta.validation.Valid @RequestBody KhachVaoRaDto dto) {
        KhachVaoRaDto created = khachVaoRaService.create(dto);
        return ResponseEntity.created(URI.create("/api/guest-entries/" + created.getId())).body(created);
    }

    @GetMapping
    public ResponseEntity<List<KhachVaoRaDto>> listAll() {
        return ResponseEntity.ok(khachVaoRaService.listAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<KhachVaoRaDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(khachVaoRaService.getById(id));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_USER')")
    @PutMapping("/{id}")
    public ResponseEntity<KhachVaoRaDto> update(@PathVariable Long id, @jakarta.validation.Valid @RequestBody KhachVaoRaDto dto) {
        return ResponseEntity.ok(khachVaoRaService.update(id, dto));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_USER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        khachVaoRaService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping("/{id}/approve")
    public ResponseEntity<KhachVaoRaDto> approve(@PathVariable Long id) {
        return ResponseEntity.ok(khachVaoRaService.approve(id));
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping("/{id}/reject")
    public ResponseEntity<KhachVaoRaDto> reject(@PathVariable Long id) {
        return ResponseEntity.ok(khachVaoRaService.reject(id));
    }
}

