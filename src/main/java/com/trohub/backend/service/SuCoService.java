package com.trohub.backend.service;

import com.trohub.backend.dto.SuCoDto;

import java.util.List;

public interface SuCoService {
    SuCoDto create(SuCoDto dto);
    SuCoDto update(Long id, SuCoDto dto);
    void delete(Long id);
    SuCoDto getById(Long id);
    List<SuCoDto> listAll();
    SuCoDto resolve(Long id);
    SuCoDto addAttachment(Long id, org.springframework.web.multipart.MultipartFile file);
}

