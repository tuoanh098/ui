package com.trohub.backend.service.impl;

import com.trohub.backend.dto.HopDongDto;
import com.trohub.backend.mapper.HopDongMapper;
import com.trohub.backend.modal.HopDong;
import com.trohub.backend.repository.HopDongRepository;
import com.trohub.backend.repository.PhongRepository;
import com.trohub.backend.service.HopDongService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import com.trohub.backend.exception.BadRequestException;
import com.trohub.backend.exception.ResourceNotFoundException;
import com.trohub.backend.util.ServiceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import java.util.concurrent.CompletableFuture;

@Service
public class HopDongServiceImpl implements HopDongService {

    private static final Logger logger = LoggerFactory.getLogger(HopDongServiceImpl.class);

    private final HopDongRepository hopDongRepository;
    private final PhongRepository phongRepository;

    public HopDongServiceImpl(HopDongRepository hopDongRepository, PhongRepository phongRepository) {
        this.hopDongRepository = hopDongRepository;
        this.phongRepository = phongRepository;
    }

    @Override
    public HopDongDto create(HopDongDto dto) {
        return ServiceUtils.exec(() -> doCreate(dto), "create HopDong");
    }

    private HopDongDto doCreate(HopDongDto dto) {
        // business validations
        // 1) phong must exist
        if (dto.getPhongId() == null || phongRepository.findById(dto.getPhongId()).isEmpty()) {
            throw new BadRequestException("Phong not found: " + dto.getPhongId());
        }

        // 2) ngayKetThuc must be >= ngayBatDau if present
        if (dto.getNgayKetThuc() != null && dto.getNgayKetThuc().isBefore(dto.getNgayBatDau())) {
            throw new BadRequestException("ngayKetThuc must be >= ngayBatDau");
        }

        HopDong e = HopDongMapper.toEntity(dto);
        HopDong saved = hopDongRepository.save(e);
        return HopDongMapper.toDto(saved);
    }

    /**
     * Example async variant for long running contract creation (keeps original semantics).
     */
    @Async
    public CompletableFuture<HopDongDto> createAsync(HopDongDto dto) {
        return CompletableFuture.completedFuture(create(dto));
    }

    @Override
    public List<HopDongDto> listAll() {
        return ServiceUtils.exec(() -> hopDongRepository.findAll().stream().map(HopDongMapper::toDto).collect(Collectors.toList()), "list all HopDong");
    }

    @Override
    public List<HopDongDto> listByNguoiId(Long nguoiId) {
        return ServiceUtils.exec(() -> hopDongRepository.findByNguoiId(nguoiId).stream().map(HopDongMapper::toDto).collect(Collectors.toList()), "list HopDong by nguoiId=" + nguoiId);
    }

    @Override
    public HopDongDto getById(Long id) {
        return ServiceUtils.exec(() -> hopDongRepository.findById(id).map(HopDongMapper::toDto).orElseThrow(() -> new ResourceNotFoundException("HopDong not found")), "get HopDong id=" + id);
    }

    @Override
    public HopDongDto update(Long id, HopDongDto dto) {
        return ServiceUtils.exec(() -> doUpdate(id, dto), "update HopDong id=" + id);
    }

    private HopDongDto doUpdate(Long id, HopDongDto dto) {
        HopDong existing = hopDongRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("HopDong not found"));
        // update allowed fields
        if (dto.getMaHopDong() != null) existing.setMaHopDong(dto.getMaHopDong());
        if (dto.getPhongId() != null) existing.setPhongId(dto.getPhongId());
        if (dto.getNguoiId() != null) existing.setNguoiId(dto.getNguoiId());
        if (dto.getNgayBatDau() != null) existing.setNgayBatDau(dto.getNgayBatDau());
        if (dto.getNgayKetThuc() != null) existing.setNgayKetThuc(dto.getNgayKetThuc());
        if (dto.getTienCoc() != null) existing.setTienCoc(dto.getTienCoc());
        if (dto.getTienThue() != null) existing.setTienThue(dto.getTienThue());
        if (dto.getTienDienPerUnit() != null) existing.setTienDienPerUnit(dto.getTienDienPerUnit());
        if (dto.getTienNuocFixed() != null) existing.setTienNuocFixed(dto.getTienNuocFixed());
        if (dto.getTrangThai() != null) existing.setTrangThai(dto.getTrangThai());
        HopDong saved = hopDongRepository.save(existing);
        return HopDongMapper.toDto(saved);
    }
}

