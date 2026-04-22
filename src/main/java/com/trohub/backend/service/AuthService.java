package com.trohub.backend.service;

import com.trohub.backend.dto.LoginRequest;
import com.trohub.backend.dto.LoginResponse;
import com.trohub.backend.dto.RegisterRequest;
import com.trohub.backend.dto.TaiKhoanDto;
import com.trohub.backend.modal.TaiKhoan;
import com.trohub.backend.repository.NguoiThueRepository;
import com.trohub.backend.repository.TaiKhoanRepository;
import com.trohub.backend.repository.VaiTroRepository;
import com.trohub.backend.security.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import com.trohub.backend.exception.BadRequestException;
import com.trohub.backend.util.ServiceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final TaiKhoanRepository taiKhoanRepository;
    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final VaiTroRepository vaiTroRepository;
    private final NguoiThueRepository nguoiThueRepository;

    public AuthService(AuthenticationManager authenticationManager, JwtTokenProvider tokenProvider, TaiKhoanRepository taiKhoanRepository, UserDetailsService userDetailsService, PasswordEncoder passwordEncoder, VaiTroRepository vaiTroRepository, NguoiThueRepository nguoiThueRepository) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.taiKhoanRepository = taiKhoanRepository;
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.vaiTroRepository = vaiTroRepository;
        this.nguoiThueRepository = nguoiThueRepository;
    }

    public LoginResponse login(LoginRequest request) {
        return ServiceUtils.exec(() -> doLogin(request), "authenticate user '" + request.getUsername() + "'");
    }

    private LoginResponse doLogin(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            TaiKhoan taiKhoan = taiKhoanRepository.findByUsername(userDetails.getUsername()).orElseThrow(() -> new BadRequestException("User not found: " + userDetails.getUsername()));

            String token = tokenProvider.generateToken(userDetails, taiKhoan.getId());

            TaiKhoanDto userDto = TaiKhoanDto.builder()
                    .id(taiKhoan.getId())
                    .username(taiKhoan.getUsername())
                    .email(taiKhoan.getEmail())
                    .fullName(taiKhoan.getFullName())
                    .active(taiKhoan.getActive())
                    .roles(taiKhoan.getRoles().stream().map(r -> r.getName()).collect(Collectors.toSet()))
                    .build();

            return new LoginResponse(token, "Bearer", tokenProvider.getValidityInMs(), userDto);
        } catch (BadCredentialsException bce) {
            logger.warn("Authentication failed for user {}: {}", request.getUsername(), bce.getMessage());
            throw new BadRequestException("Invalid username or password");
        }
    }

    public TaiKhoanDto register(RegisterRequest req) {
        return ServiceUtils.exec(() -> doRegister(req), "register user '" + req.getUsername() + "'");
    }

    private TaiKhoanDto doRegister(RegisterRequest req) {
        if (req.getUsername() == null || req.getPassword() == null) {
            throw new BadRequestException("username and password are required");
        }
        if (taiKhoanRepository.existsByUsername(req.getUsername())) {
            throw new BadRequestException("Username already exists: " + req.getUsername());
        }

        // ensure ROLE_USER exists
        var userRole = vaiTroRepository.findByName("ROLE_USER").orElseGet(() -> vaiTroRepository.save(com.trohub.backend.modal.VaiTro.builder().name("ROLE_USER").build()));

        TaiKhoan tk = TaiKhoan.builder()
                .username(req.getUsername())
                .password(passwordEncoder.encode(req.getPassword()))
                .email(req.getEmail())
                .fullName(req.getFullName())
                .active(true)
                .build();
        java.util.Set<com.trohub.backend.modal.VaiTro> roles = new java.util.HashSet<>();
        roles.add(userRole);
        tk.setRoles(roles);
        TaiKhoan saved = taiKhoanRepository.save(tk);

        // optionally create NguoiThue record if tenant details provided
        if (req.getCccd() != null || req.getSdt() != null || req.getDiaChi() != null || req.getSophong() != null) {
            com.trohub.backend.modal.NguoiThue nt = com.trohub.backend.modal.NguoiThue.builder()
                    .cccd(req.getCccd())
                    .hoTen(req.getFullName())
                    .ngaySinh(null)
                    .gioiTinh(null)
                    .diaChi(req.getDiaChi())
                    .sdt(req.getSdt())
                    .taiKhoan(saved)
                    .sophong(req.getSophong())
                    .build();
            nguoiThueRepository.save(nt);
        }

        return TaiKhoanDto.builder()
                .id(saved.getId())
                .username(saved.getUsername())
                .email(saved.getEmail())
                .fullName(saved.getFullName())
                .active(saved.getActive())
                .roles(saved.getRoles().stream().map(r -> r.getName()).collect(Collectors.toSet()))
                .build();
    }

    public List<TaiKhoanDto> listUsers(String q, String role) {
        String keyword = q == null ? "" : q.trim().toLowerCase(Locale.ROOT);
        String roleKeyword = role == null ? "" : role.trim().toUpperCase(Locale.ROOT);

        return taiKhoanRepository.findAll().stream()
                .filter(tk -> keyword.isEmpty()
                        || contains(tk.getUsername(), keyword)
                        || contains(tk.getFullName(), keyword)
                        || contains(tk.getEmail(), keyword)
                        || contains(String.valueOf(tk.getId()), keyword))
                .filter(tk -> roleKeyword.isEmpty()
                        || tk.getRoles().stream().anyMatch(r -> roleKeyword.equalsIgnoreCase(r.getName())))
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private TaiKhoanDto toDto(TaiKhoan tk) {
        return TaiKhoanDto.builder()
                .id(tk.getId())
                .username(tk.getUsername())
                .email(tk.getEmail())
                .fullName(tk.getFullName())
                .active(tk.getActive())
                .roles(tk.getRoles().stream().map(r -> r.getName()).collect(Collectors.toSet()))
                .build();
    }

    private boolean contains(String value, String keyword) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(keyword);
    }
}


