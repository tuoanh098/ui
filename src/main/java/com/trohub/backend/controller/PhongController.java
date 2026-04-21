package com.trohub.backend.controller;

import com.trohub.backend.dto.PhongDto;
import com.trohub.backend.service.PhongService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/phongs")
public class PhongController {

    private final PhongService phongService;

    public PhongController(PhongService phongService) {
        this.phongService = phongService;
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping
    public ResponseEntity<PhongDto> create(@jakarta.validation.Valid @RequestBody PhongDto dto) {
        PhongDto created = phongService.create(dto);
        return ResponseEntity.created(URI.create("/api/phongs/" + created.getId())).body(created);
    }

    @GetMapping
    public ResponseEntity<List<PhongDto>> listAll() {
        return ResponseEntity.ok(phongService.listAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PhongDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(phongService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PhongDto> update(@PathVariable Long id, @jakarta.validation.Valid @RequestBody PhongDto dto) {
        return ResponseEntity.ok(phongService.update(id, dto));
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        phongService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

