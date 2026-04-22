package com.trohub.backend.controller;

import com.trohub.backend.dto.GuestEntryReviewRequest;
import com.trohub.backend.dto.KhachVaoRaDto;
import com.trohub.backend.mapper.KhachVaoRaMapper;
import com.trohub.backend.modal.KhachVaoRa;
import com.trohub.backend.repository.ChuTroRepository;
import com.trohub.backend.repository.KhachVaoRaRepository;
import com.trohub.backend.repository.PhongRepository;
import com.trohub.backend.repository.TaiKhoanRepository;
import com.trohub.backend.repository.ToaNhaRepository;
import com.trohub.backend.service.KhachVaoRaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/guest-entries")
public class KhachVaoRaController {

    private final KhachVaoRaService khachVaoRaService;
    private final TaiKhoanRepository taiKhoanRepository;
    private final ChuTroRepository chuTroRepository;
    private final ToaNhaRepository toaNhaRepository;
    private final PhongRepository phongRepository;
    private final KhachVaoRaRepository khachVaoRaRepository;

    public KhachVaoRaController(
            KhachVaoRaService khachVaoRaService,
            TaiKhoanRepository taiKhoanRepository,
            ChuTroRepository chuTroRepository,
            ToaNhaRepository toaNhaRepository,
            PhongRepository phongRepository,
            KhachVaoRaRepository khachVaoRaRepository
    ) {
        this.khachVaoRaService = khachVaoRaService;
        this.taiKhoanRepository = taiKhoanRepository;
        this.chuTroRepository = chuTroRepository;
        this.toaNhaRepository = toaNhaRepository;
        this.phongRepository = phongRepository;
        this.khachVaoRaRepository = khachVaoRaRepository;
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_LANDLORD','ROLE_USER')")
    @PostMapping
    public ResponseEntity<KhachVaoRaDto> create(@jakarta.validation.Valid @RequestBody KhachVaoRaDto dto) {
        KhachVaoRaDto created = khachVaoRaService.create(dto);
        return ResponseEntity.created(URI.create("/api/guest-entries/" + created.getId())).body(created);
    }

    @GetMapping
    public ResponseEntity<List<KhachVaoRaDto>> listAll() {
        return ResponseEntity.ok(khachVaoRaService.listAll());
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_LANDLORD')")
    @GetMapping("/review-items")
    public ResponseEntity<List<KhachVaoRaDto>> reviewItems() {
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        if (isAdmin) {
            return ResponseEntity.ok(khachVaoRaService.listAll());
        }

        String username = auth.getName();
        var tk = taiKhoanRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN));
        var landlord = chuTroRepository.findByTaiKhoanId(tk.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        List<Long> buildingIds = toaNhaRepository.findAllByChuTroId(landlord.getId())
                .stream()
                .map(b -> b.getId())
                .filter(id -> id != null)
                .collect(Collectors.toList());
        if (buildingIds.isEmpty()) {
            return ResponseEntity.ok(new ArrayList<>());
        }

        Set<Long> roomIds = phongRepository.findAllByToaNhaIdIn(buildingIds)
                .stream()
                .map(r -> r.getId())
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        if (roomIds.isEmpty()) {
            return ResponseEntity.ok(new ArrayList<>());
        }

        List<KhachVaoRa> entries = khachVaoRaRepository.findAllByPhongIdIn(roomIds);
        List<KhachVaoRaDto> dtos = entries.stream().map(KhachVaoRaMapper::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<KhachVaoRaDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(khachVaoRaService.getById(id));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_LANDLORD')")
    @PutMapping("/{id}")
    public ResponseEntity<KhachVaoRaDto> update(@PathVariable Long id, @jakarta.validation.Valid @RequestBody KhachVaoRaDto dto) {
        return ResponseEntity.ok(khachVaoRaService.update(id, dto));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_LANDLORD')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        khachVaoRaService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_LANDLORD')")
    @PostMapping("/{id}/approve")
    public ResponseEntity<KhachVaoRaDto> approve(@PathVariable Long id) {
        return ResponseEntity.ok(khachVaoRaService.approve(id));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_LANDLORD')")
    @PostMapping("/{id}/reject")
    public ResponseEntity<KhachVaoRaDto> reject(@PathVariable Long id) {
        return ResponseEntity.ok(khachVaoRaService.reject(id));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_LANDLORD')")
    @PostMapping("/{id}/request-info")
    public ResponseEntity<KhachVaoRaDto> requestInfo(@PathVariable Long id, @RequestBody(required = false) GuestEntryReviewRequest req) {
        String note = req == null ? null : req.getNote();
        return ResponseEntity.ok(khachVaoRaService.requestInfo(id, note));
    }
}

