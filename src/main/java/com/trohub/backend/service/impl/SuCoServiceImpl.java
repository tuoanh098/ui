package com.trohub.backend.service.impl;

import com.trohub.backend.dto.SuCoDto;
import com.trohub.backend.exception.ResourceNotFoundException;
import com.trohub.backend.mapper.SuCoMapper;
import com.trohub.backend.modal.SuCo;
import com.trohub.backend.repository.SuCoRepository;
import com.trohub.backend.service.SuCoService;
import com.trohub.backend.util.ServiceUtils;
import com.trohub.backend.exception.BadRequestException;
import com.trohub.backend.config.UploadProperties;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.Arrays;

@Service
public class SuCoServiceImpl implements SuCoService {

    private final SuCoRepository suCoRepository;
    private final UploadProperties uploadProperties;

    public SuCoServiceImpl(SuCoRepository suCoRepository, UploadProperties uploadProperties) {
        this.suCoRepository = suCoRepository;
        this.uploadProperties = uploadProperties;
    }

    @Override
    public SuCoDto create(SuCoDto dto) {
        return ServiceUtils.exec(() -> doCreate(dto), "create SuCo");
    }

    private SuCoDto doCreate(SuCoDto dto) {
        SuCo e = SuCoMapper.toEntity(dto);
        SuCo saved = suCoRepository.save(e);
        return SuCoMapper.toDto(saved);
    }

    @Override
    public SuCoDto update(Long id, SuCoDto dto) {
        return ServiceUtils.exec(() -> doUpdate(id, dto), "update SuCo id=" + id);
    }

    private SuCoDto doUpdate(Long id, SuCoDto dto) {
        SuCo exist = suCoRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("SuCo not found"));
        exist.setLoai(dto.getLoai());
        exist.setMoTa(dto.getMoTa());
        exist.setToaNhaId(dto.getToaNhaId());
        exist.setPhongId(dto.getPhongId());
        exist.setReportedBy(dto.getReportedBy());
        exist.setStatus(dto.getStatus());
        SuCo saved = suCoRepository.save(exist);
        return SuCoMapper.toDto(saved);
    }

    @Override
    public void delete(Long id) {
        ServiceUtils.exec(() -> { suCoRepository.deleteById(id); return null; }, "delete SuCo id=" + id);
    }

    @Override
    public SuCoDto getById(Long id) {
        return ServiceUtils.exec(() -> suCoRepository.findById(id).map(SuCoMapper::toDto).orElseThrow(() -> new ResourceNotFoundException("SuCo not found")), "get SuCo id=" + id);
    }

    @Override
    public List<SuCoDto> listAll() {
        return ServiceUtils.exec(() -> suCoRepository.findAll().stream().map(SuCoMapper::toDto).collect(Collectors.toList()), "list all SuCo");
    }

    @Override
    public SuCoDto resolve(Long id) {
        return ServiceUtils.exec(() -> doResolve(id), "resolve SuCo id=" + id);
    }

    private SuCoDto doResolve(Long id) {
        SuCo exist = suCoRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("SuCo not found"));
        exist.setStatus("RESOLVED");
        exist.setResolvedAt(LocalDateTime.now());
        SuCo saved = suCoRepository.save(exist);
        return SuCoMapper.toDto(saved);
    }

    @Override
    public SuCoDto addAttachment(Long id, MultipartFile file) {
        return ServiceUtils.exec(() -> doAddAttachment(id, file), "add attachment to SuCo id=" + id);
    }

    private SuCoDto doAddAttachment(Long id, MultipartFile file) {
        SuCo exist = suCoRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("SuCo not found"));
        // validate file
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("No file uploaded");
        }

        // allowed content types and max size (from UploadProperties)
        final java.util.Set<String> ALLOWED_TYPES = uploadProperties.getAllowedTypes().stream()
                .map(String::trim)
                .map(String::toLowerCase)
                .collect(java.util.stream.Collectors.toSet());

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType.toLowerCase())) {
            String allowed = String.join(", ", ALLOWED_TYPES);
            throw new BadRequestException("Unsupported file type: " + contentType + ". Allowed types: " + allowed);
        }

        if (file.getSize() > uploadProperties.getMaxSize()) {
            throw new BadRequestException("File too large: " + file.getSize() + " bytes. Maximum allowed is " + uploadProperties.getMaxSize() + " bytes");
        }

        // ensure uploads dir
        Path baseDir = Paths.get(uploadProperties.getBaseDir(), uploadProperties.getIncidentsDir(), String.valueOf(id));
        try {
            Files.createDirectories(baseDir);
            String filename = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path target = baseDir.resolve(filename);
            Files.copy(file.getInputStream(), target);

            String relPath = "/" + uploadProperties.getBaseDir() + "/" + uploadProperties.getIncidentsDir() + "/" + id + "/" + filename;
            String existing = exist.getImagePaths();
            String updated;
            if (existing == null || existing.isBlank()) updated = relPath;
            else updated = existing + "," + relPath;
            exist.setImagePaths(updated);
            SuCo saved = suCoRepository.save(exist);
            return SuCoMapper.toDto(saved);
        } catch (java.io.IOException e) {
            throw new RuntimeException("Failed to store uploaded file: " + e.getMessage(), e);
        }
    }
}

