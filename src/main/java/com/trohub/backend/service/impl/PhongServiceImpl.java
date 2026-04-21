package com.trohub.backend.service.impl;

import com.trohub.backend.dto.PhongDto;
import com.trohub.backend.mapper.PhongMapper;
import com.trohub.backend.modal.Phong;
import com.trohub.backend.repository.PhongRepository;
import com.trohub.backend.service.PhongService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import com.trohub.backend.exception.ResourceNotFoundException;
import com.trohub.backend.exception.ConflictException;
import com.trohub.backend.util.ServiceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class PhongServiceImpl implements PhongService {

    private static final Logger logger = LoggerFactory.getLogger(PhongServiceImpl.class);

    private final PhongRepository phongRepository;

    public PhongServiceImpl(PhongRepository phongRepository) {
        this.phongRepository = phongRepository;
    }

    @Override
    public PhongDto create(PhongDto dto) {
        return ServiceUtils.exec(() -> doCreate(dto), "create Phong") ;
    }

    private PhongDto doCreate(PhongDto dto) {
        // business check: maPhong must be unique
        if (dto.getMaPhong() != null && phongRepository.findByMaPhong(dto.getMaPhong()).isPresent()) {
            throw new ConflictException("maPhong already exists: " + dto.getMaPhong());
        }
        Phong e = PhongMapper.toEntity(dto);
        Phong saved = phongRepository.save(e);
        return PhongMapper.toDto(saved);
    }

    @Override
    public PhongDto update(Long id, PhongDto dto) {
        return ServiceUtils.exec(() -> doUpdate(id, dto), "update Phong id=" + id);
    }

    private PhongDto doUpdate(Long id, PhongDto dto) {
        Phong exist = phongRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Phong not found"));
        exist.setMaPhong(dto.getMaPhong());
        exist.setToaNhaId(dto.getToaNhaId());
        exist.setKhuId(dto.getKhuId());
        exist.setLoaiPhongId(dto.getLoaiPhongId());
        exist.setSoGiuong(dto.getSoGiuong());
        exist.setTrangThai(dto.getTrangThai());
        exist.setMoTa(dto.getMoTa());
        Phong saved = phongRepository.save(exist);
        return PhongMapper.toDto(saved);
    }

    @Override
    public void delete(Long id) {
        ServiceUtils.exec(() -> {
            phongRepository.deleteById(id);
            return null;
        }, "delete Phong id=" + id);
    }

    @Override
    public PhongDto getById(Long id) {
        return ServiceUtils.exec(() -> phongRepository.findById(id).map(PhongMapper::toDto).orElseThrow(() -> new ResourceNotFoundException("Phong not found")), "get Phong id=" + id);
    }

    @Override
    public List<PhongDto> listAll() {
        return ServiceUtils.exec(() -> phongRepository.findAll().stream().map(PhongMapper::toDto).collect(Collectors.toList()), "list all Phong");
    }
}

