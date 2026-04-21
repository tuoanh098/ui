package com.trohub.backend.controller;

import com.trohub.backend.dto.NguoiThueDto;
import com.trohub.backend.service.NguoiThueService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/tenants")
public class NguoiThueController {

    private final NguoiThueService nguoiThueService;

    public NguoiThueController(NguoiThueService nguoiThueService) {
        this.nguoiThueService = nguoiThueService;
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping
    public ResponseEntity<NguoiThueDto> create(@jakarta.validation.Valid @RequestBody NguoiThueDto dto) {
        NguoiThueDto created = nguoiThueService.create(dto);
        return ResponseEntity.created(URI.create("/api/tenants/" + created.getId())).body(created);
    }

    @GetMapping
    public ResponseEntity<List<NguoiThueDto>> listAll() {
        return ResponseEntity.ok(nguoiThueService.listAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<NguoiThueDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(nguoiThueService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<NguoiThueDto> update(@PathVariable Long id, @jakarta.validation.Valid @RequestBody NguoiThueDto dto) {
        return ResponseEntity.ok(nguoiThueService.update(id, dto));
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        nguoiThueService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

