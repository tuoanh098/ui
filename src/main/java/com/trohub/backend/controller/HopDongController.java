package com.trohub.backend.controller;

import com.trohub.backend.dto.HopDongDto;
import com.trohub.backend.service.HopDongService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/contracts")
public class HopDongController {

    private final HopDongService hopDongService;

    public HopDongController(HopDongService hopDongService) {
        this.hopDongService = hopDongService;
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping
    public ResponseEntity<HopDongDto> create(@jakarta.validation.Valid @RequestBody HopDongDto dto) {
        HopDongDto created = hopDongService.create(dto);
        return ResponseEntity.created(URI.create("/api/contracts/" + created.getId())).body(created);
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<HopDongDto> update(@PathVariable Long id, @jakarta.validation.Valid @RequestBody HopDongDto dto) {
        HopDongDto updated = hopDongService.update(id, dto);
        return ResponseEntity.ok(updated);
    }

    @GetMapping
    public ResponseEntity<List<HopDongDto>> listAll() {
        return ResponseEntity.ok(hopDongService.listAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<HopDongDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(hopDongService.getById(id));
    }

    @GetMapping("/tenant/{tenantId}")
    public ResponseEntity<List<HopDongDto>> listByTenant(@PathVariable Long tenantId) {
        return ResponseEntity.ok(hopDongService.listByNguoiId(tenantId));
    }
}

