package com.trohub.backend.service;

import com.trohub.backend.dto.KhachVaoRaDto;

import java.util.List;

public interface KhachVaoRaService {
    KhachVaoRaDto create(KhachVaoRaDto dto);
    KhachVaoRaDto update(Long id, KhachVaoRaDto dto);
    void delete(Long id);
    KhachVaoRaDto getById(Long id);
    List<KhachVaoRaDto> listAll();
    KhachVaoRaDto approve(Long id);
    KhachVaoRaDto reject(Long id);
}

