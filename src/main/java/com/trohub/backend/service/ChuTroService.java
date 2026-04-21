package com.trohub.backend.service;

import com.trohub.backend.dto.ChuTroDto;

import java.util.List;

public interface ChuTroService {
    ChuTroDto create(ChuTroDto dto);
    ChuTroDto update(Long id, ChuTroDto dto);
    void delete(Long id);
    ChuTroDto getById(Long id);
    List<ChuTroDto> listAll();
}

