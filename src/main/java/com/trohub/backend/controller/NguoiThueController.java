package com.trohub.backend.controller;

import com.trohub.backend.dto.NguoiThueDto;
import com.trohub.backend.service.NguoiThueService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tenants")
public class NguoiThueController {

    private final NguoiThueService nguoiThueService;

    public NguoiThueController(NguoiThueService nguoiThueService) {
        this.nguoiThueService = nguoiThueService;
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_LANDLORD')")
    @PostMapping
    public ResponseEntity<NguoiThueDto> create(@jakarta.validation.Valid @RequestBody NguoiThueDto dto) {
        NguoiThueDto created = nguoiThueService.create(dto);
        return ResponseEntity.created(URI.create("/api/tenants/" + created.getId())).body(created);
    }

    @GetMapping
    public ResponseEntity<List<NguoiThueDto>> listAll(@RequestParam(value = "q", required = false) String q) {
        List<NguoiThueDto> all = nguoiThueService.listAll();
        if (q == null || q.trim().isEmpty()) {
            return ResponseEntity.ok(all);
        }
        String keyword = q.trim().toLowerCase(Locale.ROOT);
        List<NguoiThueDto> filtered = all.stream()
                .filter(item -> contains(item.getHoTen(), keyword)
                        || contains(item.getCccd(), keyword)
                        || contains(item.getSdt(), keyword)
                        || contains(String.valueOf(item.getId()), keyword))
                .collect(Collectors.toList());
        return ResponseEntity.ok(filtered);
    }

    @GetMapping("/{id}")
    public ResponseEntity<NguoiThueDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(nguoiThueService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<NguoiThueDto> update(@PathVariable Long id, @jakarta.validation.Valid @RequestBody NguoiThueDto dto) {
        return ResponseEntity.ok(nguoiThueService.update(id, dto));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_LANDLORD')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        nguoiThueService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private boolean contains(String value, String keyword) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(keyword);
    }
}

