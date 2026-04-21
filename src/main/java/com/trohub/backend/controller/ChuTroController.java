package com.trohub.backend.controller;

import com.trohub.backend.dto.ChuTroDto;
import com.trohub.backend.service.ChuTroService;
import com.trohub.backend.service.ReportService;
import com.trohub.backend.repository.TaiKhoanRepository;
import com.trohub.backend.repository.ChuTroRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/landlords")
public class ChuTroController {

    private final ChuTroService chuTroService;
    private final ReportService reportService;
    private final TaiKhoanRepository taiKhoanRepository;
    private final ChuTroRepository chuTroRepository;

    public ChuTroController(ChuTroService chuTroService, ReportService reportService, TaiKhoanRepository taiKhoanRepository, ChuTroRepository chuTroRepository) {
        this.chuTroService = chuTroService;
        this.reportService = reportService;
        this.taiKhoanRepository = taiKhoanRepository;
        this.chuTroRepository = chuTroRepository;
    }

    @GetMapping
    public ResponseEntity<List<ChuTroDto>> listAll() {
        return ResponseEntity.ok(chuTroService.listAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ChuTroDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(chuTroService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ChuTroDto> create(@RequestBody ChuTroDto dto) {
        return ResponseEntity.ok(chuTroService.create(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ChuTroDto> update(@PathVariable Long id, @RequestBody ChuTroDto dto) {
        return ResponseEntity.ok(chuTroService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        chuTroService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Return summary for the landlord account currently authenticated.
     * Query params: from, to (ISO date yyyy-MM-dd). Allowed for ROLE_LANDLORD, ROLE_BILLING_STAFF, ROLE_ADMIN.
     */
    @PreAuthorize("hasAnyAuthority('ROLE_LANDLORD','ROLE_BILLING_STAFF','ROLE_ADMIN')")
    @GetMapping("/me/summary")
    public ResponseEntity<com.trohub.backend.dto.report.LandlordSummaryDto> mySummary(
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam LocalDate from,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam LocalDate to) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        String username = auth.getName();
        var tk = taiKhoanRepository.findByUsername(username).orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN));
        var landlord = chuTroRepository.findByTaiKhoanId(tk.getId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        return ResponseEntity.ok(reportService.landlordSummary(landlord.getId(), from, to));
    }
}

