package com.trohub.backend.service;

import com.trohub.backend.dto.PhongDto;

import java.util.List;

public interface PhongService {
    PhongDto create(PhongDto dto);
    PhongDto update(Long id, PhongDto dto);
    void delete(Long id);
    PhongDto getById(Long id);
    List<PhongDto> listAll();
}

