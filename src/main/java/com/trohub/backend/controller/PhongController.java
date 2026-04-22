package com.trohub.backend.controller;

import com.trohub.backend.dto.PhongDto;
import com.trohub.backend.service.PhongService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/phongs")
public class PhongController {

    private final PhongService phongService;

    public PhongController(PhongService phongService) {
        this.phongService = phongService;
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_LANDLORD')")
    @PostMapping
    public ResponseEntity<PhongDto> create(@jakarta.validation.Valid @RequestBody PhongDto dto) {
        PhongDto created = phongService.create(dto);
        return ResponseEntity.created(URI.create("/api/phongs/" + created.getId())).body(created);
    }

    @GetMapping
    public ResponseEntity<List<PhongDto>> listAll(@RequestParam(value = "q", required = false) String q) {
        List<PhongDto> all = phongService.listAll();
        if (q == null || q.trim().isEmpty()) {
            return ResponseEntity.ok(all);
        }
        String keyword = q.trim().toLowerCase(Locale.ROOT);
        List<PhongDto> filtered = all.stream()
                .filter(item -> contains(item.getMaPhong(), keyword)
                        || contains(item.getMoTa(), keyword)
                        || contains(String.valueOf(item.getId()), keyword))
                .collect(Collectors.toList());
        return ResponseEntity.ok(filtered);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PhongDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(phongService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PhongDto> update(@PathVariable Long id, @jakarta.validation.Valid @RequestBody PhongDto dto) {
        return ResponseEntity.ok(phongService.update(id, dto));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_LANDLORD')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        phongService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private boolean contains(String value, String keyword) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(keyword);
    }
}

