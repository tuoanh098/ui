package com.trohub.backend.service.impl;

import com.trohub.backend.dto.ToaNhaDto;
import com.trohub.backend.modal.ToaNha;
import com.trohub.backend.repository.ToaNhaRepository;
import com.trohub.backend.repository.PhongRepository;
import com.trohub.backend.service.ToaNhaService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ToaNhaServiceImpl implements ToaNhaService {

    private final ToaNhaRepository toaNhaRepository;
    private final PhongRepository phongRepository;

    public ToaNhaServiceImpl(ToaNhaRepository toaNhaRepository, PhongRepository phongRepository) {
        this.toaNhaRepository = toaNhaRepository;
        this.phongRepository = phongRepository;
    }

    @Override
    public ToaNhaDto create(ToaNhaDto dto) {
        ToaNha t = ToaNha.builder().ten(dto.getTen()).diaChi(dto.getDiaChi()).chuTroId(dto.getChuTroId()).build();
        ToaNha saved = toaNhaRepository.save(t);
        return map(saved);
    }

    @Override
    public ToaNhaDto update(Long id, ToaNhaDto dto) {
        ToaNha t = toaNhaRepository.findById(id).orElseThrow(() -> new RuntimeException("Building not found"));
        t.setTen(dto.getTen());
        t.setDiaChi(dto.getDiaChi());
        t.setChuTroId(dto.getChuTroId());
        ToaNha saved = toaNhaRepository.save(t);
        return map(saved);
    }

    @Override
    public void delete(Long id) {
        toaNhaRepository.deleteById(id);
    }

    @Override
    public ToaNhaDto getById(Long id) {
        ToaNha t = toaNhaRepository.findById(id).orElseThrow(() -> new RuntimeException("Building not found"));
        ToaNhaDto dto = map(t);
        dto.setRoomCount(phongRepository.countByToaNhaId(id));
        dto.setOccupiedCount(phongRepository.countByToaNhaIdAndTrangThai(id, "DA_THUE"));
        return dto;
    }

    @Override
    public List<ToaNhaDto> listAll() {
        return toaNhaRepository.findAll().stream().map(this::mapWithStats).collect(Collectors.toList());
    }

    @Override
    public ToaNhaDto stats(Long id) {
        return getById(id);
    }

    private ToaNhaDto map(ToaNha t) {
        return ToaNhaDto.builder().id(t.getId()).ten(t.getTen()).diaChi(t.getDiaChi()).chuTroId(t.getChuTroId()).build();
    }

    private ToaNhaDto mapWithStats(ToaNha t) {
        ToaNhaDto d = map(t);
        d.setRoomCount(phongRepository.countByToaNhaId(t.getId()));
        d.setOccupiedCount(phongRepository.countByToaNhaIdAndTrangThai(t.getId(), "DA_THUE"));
        return d;
    }
}

