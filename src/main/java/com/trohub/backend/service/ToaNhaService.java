package com.trohub.backend.service;

import com.trohub.backend.dto.ToaNhaDto;

import java.util.List;

public interface ToaNhaService {
    ToaNhaDto create(ToaNhaDto dto);
    ToaNhaDto update(Long id, ToaNhaDto dto);
    void delete(Long id);
    ToaNhaDto getById(Long id);
    List<ToaNhaDto> listAll();
    ToaNhaDto stats(Long id);
}

