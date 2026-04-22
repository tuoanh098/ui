package com.trohub.backend.controller;

import com.trohub.backend.dto.SuCoDto;
import com.trohub.backend.service.SuCoService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/incidents")
public class SuCoController {

    private final SuCoService suCoService;

    public SuCoController(SuCoService suCoService) {
        this.suCoService = suCoService;
    }

    @PreAuthorize("hasAnyAuthority('ROLE_USER','ROLE_BILLING_STAFF','ROLE_ADMIN','ROLE_LANDLORD')")
    @PostMapping
    public ResponseEntity<SuCoDto> create(@jakarta.validation.Valid @RequestBody SuCoDto dto) {
        SuCoDto created = suCoService.create(dto);
        return ResponseEntity.created(URI.create("/api/incidents/" + created.getId())).body(created);
    }

    @GetMapping
    public ResponseEntity<List<SuCoDto>> listAll() {
        return ResponseEntity.ok(suCoService.listAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SuCoDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(suCoService.getById(id));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_BILLING_STAFF','ROLE_ADMIN','ROLE_LANDLORD')")
    @PutMapping("/{id}")
    public ResponseEntity<SuCoDto> update(@PathVariable Long id, @jakarta.validation.Valid @RequestBody SuCoDto dto) {
        return ResponseEntity.ok(suCoService.update(id, dto));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_BILLING_STAFF','ROLE_ADMIN','ROLE_LANDLORD')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        suCoService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyAuthority('ROLE_BILLING_STAFF','ROLE_ADMIN','ROLE_LANDLORD')")
    @PostMapping("/{id}/resolve")
    public ResponseEntity<SuCoDto> resolve(@PathVariable Long id) {
        return ResponseEntity.ok(suCoService.resolve(id));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_USER','ROLE_BILLING_STAFF','ROLE_ADMIN','ROLE_LANDLORD')")
    @PostMapping(path = "/{id}/attachments", consumes = "multipart/form-data")
    public ResponseEntity<SuCoDto> uploadAttachment(@PathVariable Long id, @RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        SuCoDto dto = suCoService.addAttachment(id, file);
        return ResponseEntity.ok(dto);
    }
}

