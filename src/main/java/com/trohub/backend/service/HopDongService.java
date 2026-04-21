package com.trohub.backend.service;

import com.trohub.backend.dto.HopDongDto;

import java.util.List;

public interface HopDongService {
    HopDongDto create(HopDongDto dto);
    HopDongDto update(Long id, HopDongDto dto);
    List<HopDongDto> listAll();
    HopDongDto getById(Long id);
}

