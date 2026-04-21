package com.trohub.backend.service.impl;

import com.trohub.backend.dto.ChuTroDto;
import com.trohub.backend.modal.ChuTro;
import com.trohub.backend.repository.ChuTroRepository;
import com.trohub.backend.service.ChuTroService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChuTroServiceImpl implements ChuTroService {

    private final ChuTroRepository chuTroRepository;

    public ChuTroServiceImpl(ChuTroRepository chuTroRepository) {
        this.chuTroRepository = chuTroRepository;
    }

    @Override
    public ChuTroDto create(ChuTroDto dto) {
        ChuTro c = ChuTro.builder().ten(dto.getTen()).email(dto.getEmail()).sdt(dto.getSdt()).diaChi(dto.getDiaChi()).taiKhoanId(dto.getTaiKhoanId()).build();
        ChuTro saved = chuTroRepository.save(c);
        return map(saved);
    }

    @Override
    public ChuTroDto update(Long id, ChuTroDto dto) {
        ChuTro c = chuTroRepository.findById(id).orElseThrow(() -> new RuntimeException("ChuTro not found"));
        c.setTen(dto.getTen());
        c.setEmail(dto.getEmail());
        c.setSdt(dto.getSdt());
        c.setDiaChi(dto.getDiaChi());
        c.setTaiKhoanId(dto.getTaiKhoanId());
        ChuTro saved = chuTroRepository.save(c);
        return map(saved);
    }

    @Override
    public void delete(Long id) {
        chuTroRepository.deleteById(id);
    }

    @Override
    public ChuTroDto getById(Long id) {
        return map(chuTroRepository.findById(id).orElseThrow(() -> new RuntimeException("ChuTro not found")));
    }

    @Override
    public List<ChuTroDto> listAll() {
        return chuTroRepository.findAll().stream().map(this::map).collect(Collectors.toList());
    }

    private ChuTroDto map(ChuTro c) {
        return ChuTroDto.builder().id(c.getId()).ten(c.getTen()).email(c.getEmail()).sdt(c.getSdt()).diaChi(c.getDiaChi()).taiKhoanId(c.getTaiKhoanId()).createdAt(c.getCreatedAt()).updatedAt(c.getUpdatedAt()).build();
    }
}

