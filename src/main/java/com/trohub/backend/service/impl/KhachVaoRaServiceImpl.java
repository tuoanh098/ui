package com.trohub.backend.service.impl;

import com.trohub.backend.dto.KhachVaoRaDto;
import com.trohub.backend.exception.ResourceNotFoundException;
import com.trohub.backend.mapper.KhachVaoRaMapper;
import com.trohub.backend.modal.KhachVaoRa;
import com.trohub.backend.repository.KhachVaoRaRepository;
import com.trohub.backend.service.KhachVaoRaService;
import com.trohub.backend.util.ServiceUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class KhachVaoRaServiceImpl implements KhachVaoRaService {

    private final KhachVaoRaRepository khachVaoRaRepository;

    public KhachVaoRaServiceImpl(KhachVaoRaRepository khachVaoRaRepository) {
        this.khachVaoRaRepository = khachVaoRaRepository;
    }

    @Override
    public KhachVaoRaDto create(KhachVaoRaDto dto) {
        return ServiceUtils.exec(() -> doCreate(dto), "create KhachVaoRa");
    }

    private KhachVaoRaDto doCreate(KhachVaoRaDto dto) {
        KhachVaoRa e = KhachVaoRaMapper.toEntity(dto);
        if (e.getApprovalStatus() == null || e.getApprovalStatus().isBlank()) {
            e.setApprovalStatus("PENDING");
        }
        KhachVaoRa saved = khachVaoRaRepository.save(e);
        return KhachVaoRaMapper.toDto(saved);
    }

    @Override
    public KhachVaoRaDto update(Long id, KhachVaoRaDto dto) {
        return ServiceUtils.exec(() -> doUpdate(id, dto), "update KhachVaoRa id=" + id);
    }

    private KhachVaoRaDto doUpdate(Long id, KhachVaoRaDto dto) {
        KhachVaoRa exist = khachVaoRaRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("KhachVaoRa not found"));
        exist.setTen(dto.getTen());
        exist.setCmnd(dto.getCmnd());
        exist.setSdt(dto.getSdt());
        exist.setPhongId(dto.getPhongId());
        exist.setLoai(dto.getLoai());
        exist.setGhiChu(dto.getGhiChu());
        KhachVaoRa saved = khachVaoRaRepository.save(exist);
        return KhachVaoRaMapper.toDto(saved);
    }

    @Override
    public void delete(Long id) {
        ServiceUtils.exec(() -> { khachVaoRaRepository.deleteById(id); return null; }, "delete KhachVaoRa id=" + id);
    }

    @Override
    public KhachVaoRaDto getById(Long id) {
        return ServiceUtils.exec(() -> khachVaoRaRepository.findById(id).map(KhachVaoRaMapper::toDto).orElseThrow(() -> new ResourceNotFoundException("KhachVaoRa not found")), "get KhachVaoRa id=" + id);
    }

    @Override
    public List<KhachVaoRaDto> listAll() {
        return ServiceUtils.exec(() -> khachVaoRaRepository.findAll().stream().map(KhachVaoRaMapper::toDto).collect(Collectors.toList()), "list all KhachVaoRa");
    }

    @Override
    public KhachVaoRaDto approve(Long id) {
        return ServiceUtils.exec(() -> doApprove(id), "approve KhachVaoRa id=" + id);
    }

    private KhachVaoRaDto doApprove(Long id) {
        KhachVaoRa exist = khachVaoRaRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("KhachVaoRa not found"));
        exist.setApprovalStatus("APPROVED");
        KhachVaoRa saved = khachVaoRaRepository.save(exist);
        return KhachVaoRaMapper.toDto(saved);
    }

    @Override
    public KhachVaoRaDto reject(Long id) {
        return ServiceUtils.exec(() -> doReject(id), "reject KhachVaoRa id=" + id);
    }

    private KhachVaoRaDto doReject(Long id) {
        KhachVaoRa exist = khachVaoRaRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("KhachVaoRa not found"));
        exist.setApprovalStatus("REJECTED");
        KhachVaoRa saved = khachVaoRaRepository.save(exist);
        return KhachVaoRaMapper.toDto(saved);
    }

    @Override
    public KhachVaoRaDto requestInfo(Long id, String note) {
        return ServiceUtils.exec(() -> doRequestInfo(id, note), "request info KhachVaoRa id=" + id);
    }

    private KhachVaoRaDto doRequestInfo(Long id, String note) {
        KhachVaoRa exist = khachVaoRaRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("KhachVaoRa not found"));
        exist.setApprovalStatus("NEED_INFO");
        String trimmed = note == null ? "" : note.trim();
        if (!trimmed.isEmpty()) {
            String old = exist.getGhiChu() == null ? "" : exist.getGhiChu().trim();
            String prefix = "Yêu cầu bổ sung: " + trimmed;
            exist.setGhiChu(old.isEmpty() ? prefix : old + "\n" + prefix);
        }
        KhachVaoRa saved = khachVaoRaRepository.save(exist);
        return KhachVaoRaMapper.toDto(saved);
    }
}

