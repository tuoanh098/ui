package com.trohub.backend.service.impl;

import com.trohub.backend.dto.NguoiThueDto;
import com.trohub.backend.mapper.NguoiThueMapper;
import com.trohub.backend.modal.NguoiThue;
import com.trohub.backend.modal.TaiKhoan;
import com.trohub.backend.repository.NguoiThueRepository;
import com.trohub.backend.repository.TaiKhoanRepository;
import com.trohub.backend.service.NguoiThueService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import com.trohub.backend.exception.ResourceNotFoundException;
import com.trohub.backend.exception.ConflictException;
import com.trohub.backend.util.ServiceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class NguoiThueServiceImpl implements NguoiThueService {

    private static final Logger logger = LoggerFactory.getLogger(NguoiThueServiceImpl.class);

    private final NguoiThueRepository nguoiThueRepository;
    private final TaiKhoanRepository taiKhoanRepository;

    public NguoiThueServiceImpl(NguoiThueRepository nguoiThueRepository, TaiKhoanRepository taiKhoanRepository) {
        this.nguoiThueRepository = nguoiThueRepository;
        this.taiKhoanRepository = taiKhoanRepository;
    }

    @Override
    public NguoiThueDto create(NguoiThueDto dto) {
        return ServiceUtils.exec(() -> doCreate(dto), "create NguoiThue");
    }

    private NguoiThueDto doCreate(NguoiThueDto dto) {
        if (dto.getTaiKhoanId() != null && nguoiThueRepository.findByTaiKhoanId(dto.getTaiKhoanId()).isPresent()) {
            throw new ConflictException("taiKhoan already linked to another NguoiThue: " + dto.getTaiKhoanId());
        }

        NguoiThue e = NguoiThueMapper.toEntity(dto);

        if (dto.getTaiKhoanId() != null) {
            TaiKhoan tk = taiKhoanRepository.findById(dto.getTaiKhoanId()).orElseThrow(() -> new ResourceNotFoundException("TaiKhoan not found id=" + dto.getTaiKhoanId()));
            e.setTaiKhoan(tk);
        }

        NguoiThue saved = nguoiThueRepository.save(e);
        return NguoiThueMapper.toDto(saved);
    }

    @Override
    public NguoiThueDto update(Long id, NguoiThueDto dto) {
        return ServiceUtils.exec(() -> doUpdate(id, dto), "update NguoiThue id=" + id);
    }

    private NguoiThueDto doUpdate(Long id, NguoiThueDto dto) {
        NguoiThue exist = nguoiThueRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("NguoiThue not found"));

        // handle taiKhoan link change
        if (dto.getTaiKhoanId() != null) {
            // if changing to a new taiKhoan, ensure it's not already linked
            nguoiThueRepository.findByTaiKhoanId(dto.getTaiKhoanId()).ifPresent(nt -> {
                if (!nt.getId().equals(id)) throw new ConflictException("taiKhoan already linked to another NguoiThue: " + dto.getTaiKhoanId());
            });
            TaiKhoan tk = taiKhoanRepository.findById(dto.getTaiKhoanId()).orElseThrow(() -> new ResourceNotFoundException("TaiKhoan not found id=" + dto.getTaiKhoanId()));
            exist.setTaiKhoan(tk);
        } else {
            exist.setTaiKhoan(null);
        }

        exist.setCccd(dto.getCccd());
        exist.setHoTen(dto.getHoTen());
        exist.setNgaySinh(dto.getNgaySinh());
        exist.setGioiTinh(dto.getGioiTinh());
        exist.setDiaChi(dto.getDiaChi());
        exist.setSdt(dto.getSdt());
        exist.setQueQuan(dto.getQueQuan());
        exist.setNgheNghiep(dto.getNgheNghiep());
        exist.setThongTinLienLac(dto.getThongTinLienLac());
        exist.setSophong(dto.getSophong());

        NguoiThue saved = nguoiThueRepository.save(exist);
        return NguoiThueMapper.toDto(saved);
    }

    @Override
    public void delete(Long id) {
        ServiceUtils.exec(() -> {
            nguoiThueRepository.deleteById(id);
            return null;
        }, "delete NguoiThue id=" + id);
    }

    @Override
    public NguoiThueDto getById(Long id) {
        return ServiceUtils.exec(() -> nguoiThueRepository.findById(id).map(NguoiThueMapper::toDto).orElseThrow(() -> new ResourceNotFoundException("NguoiThue not found")), "get NguoiThue id=" + id);
    }

    @Override
    public List<NguoiThueDto> listAll() {
        return ServiceUtils.exec(() -> nguoiThueRepository.findAll().stream().map(NguoiThueMapper::toDto).collect(Collectors.toList()), "list all NguoiThue");
    }
}

