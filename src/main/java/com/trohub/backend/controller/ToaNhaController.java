package com.trohub.backend.controller;

import com.trohub.backend.dto.ToaNhaDto;
import com.trohub.backend.service.ToaNhaService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/buildings")
public class ToaNhaController {

    private final ToaNhaService toaNhaService;
    private final com.trohub.backend.repository.TaiKhoanRepository taiKhoanRepository;
    private final com.trohub.backend.repository.ChuTroRepository chuTroRepository;

    public ToaNhaController(ToaNhaService toaNhaService, com.trohub.backend.repository.TaiKhoanRepository taiKhoanRepository, com.trohub.backend.repository.ChuTroRepository chuTroRepository) {
        this.toaNhaService = toaNhaService;
        this.taiKhoanRepository = taiKhoanRepository;
        this.chuTroRepository = chuTroRepository;
    }

    @GetMapping
    public ResponseEntity<List<ToaNhaDto>> listAll() {
        return ResponseEntity.ok(toaNhaService.listAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ToaNhaDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(toaNhaService.getById(id));
    }

    @PostMapping
    public ResponseEntity<ToaNhaDto> create(@RequestBody ToaNhaDto dto) {
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isLandlord = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_LANDLORD"));
        if (!isAdmin && !isLandlord) {
            return ResponseEntity.status(403).build();
        }

        // if landlord, ensure they can only create building for their own chuTro
        if (isLandlord && !isAdmin) {
            String username = auth.getName();
            var tk = taiKhoanRepository.findByUsername(username).orElse(null);
            if (tk == null) return ResponseEntity.status(403).build();
            var landlord = chuTroRepository.findByTaiKhoanId(tk.getId()).orElse(null);
            if (landlord == null) return ResponseEntity.status(403).build();
            // enforce dto.chuTroId to landlord id
            dto.setChuTroId(landlord.getId());
        }

        ToaNhaDto created = toaNhaService.create(dto);
        return ResponseEntity.created(URI.create("/api/buildings/" + created.getId())).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ToaNhaDto> update(@PathVariable Long id, @RequestBody ToaNhaDto dto) {
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isLandlord = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_LANDLORD"));
        if (!isAdmin && !isLandlord) {
            return ResponseEntity.status(403).build();
        }
        if (isLandlord && !isAdmin) {
            String username = auth.getName();
            var tk = taiKhoanRepository.findByUsername(username).orElse(null);
            if (tk == null) return ResponseEntity.status(403).build();
            var landlord = chuTroRepository.findByTaiKhoanId(tk.getId()).orElse(null);
            if (landlord == null) return ResponseEntity.status(403).build();
            // only allow update if the building belongs to this landlord
            var existing = toaNhaService.getById(id);
            if (existing.getChuTroId() == null || !existing.getChuTroId().equals(landlord.getId())) {
                return ResponseEntity.status(403).build();
            }
            dto.setChuTroId(landlord.getId());
        }
        return ResponseEntity.ok(toaNhaService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isLandlord = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_LANDLORD"));
        if (!isAdmin && !isLandlord) {
            return ResponseEntity.status(403).build();
        }
        if (isLandlord && !isAdmin) {
            String username = auth.getName();
            var tk = taiKhoanRepository.findByUsername(username).orElse(null);
            if (tk == null) return ResponseEntity.status(403).build();
            var landlord = chuTroRepository.findByTaiKhoanId(tk.getId()).orElse(null);
            if (landlord == null) return ResponseEntity.status(403).build();
            var existing = toaNhaService.getById(id);
            if (existing.getChuTroId() == null || !existing.getChuTroId().equals(landlord.getId())) {
                return ResponseEntity.status(403).build();
            }
        }
        toaNhaService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/stats")
    public ResponseEntity<ToaNhaDto> stats(@PathVariable Long id) {
        return ResponseEntity.ok(toaNhaService.stats(id));
    }
}

