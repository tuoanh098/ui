package com.trohub.backend.config;

import com.trohub.backend.modal.TaiKhoan;
import com.trohub.backend.modal.VaiTro;
import com.trohub.backend.repository.TaiKhoanRepository;
import com.trohub.backend.repository.VaiTroRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import com.trohub.backend.modal.NguoiThue;
import com.trohub.backend.modal.Phong;
import com.trohub.backend.modal.HopDong;
import com.trohub.backend.repository.NguoiThueRepository;
import com.trohub.backend.repository.PhongRepository;
import com.trohub.backend.repository.HopDongRepository;
import java.math.BigDecimal;
import java.time.LocalDate;

@Component
public class DataInitializer implements CommandLineRunner {

    private final VaiTroRepository vaiTroRepository;
    private final TaiKhoanRepository taiKhoanRepository;
    private final PasswordEncoder passwordEncoder;
    private final NguoiThueRepository nguoiThueRepository;
    private final PhongRepository phongRepository;
    private final HopDongRepository hopDongRepository;

    public DataInitializer(VaiTroRepository vaiTroRepository, TaiKhoanRepository taiKhoanRepository, PasswordEncoder passwordEncoder, NguoiThueRepository nguoiThueRepository, PhongRepository phongRepository, HopDongRepository hopDongRepository) {
        this.vaiTroRepository = vaiTroRepository;
        this.taiKhoanRepository = taiKhoanRepository;
        this.passwordEncoder = passwordEncoder;
        this.nguoiThueRepository = nguoiThueRepository;
        this.phongRepository = phongRepository;
        this.hopDongRepository = hopDongRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        VaiTro adminRole = vaiTroRepository.findByName("ROLE_ADMIN").orElseGet(() -> vaiTroRepository.save(VaiTro.builder().name("ROLE_ADMIN").build()));
        VaiTro userRole = vaiTroRepository.findByName("ROLE_USER").orElseGet(() -> vaiTroRepository.save(VaiTro.builder().name("ROLE_USER").build()));
        VaiTro billingRole = vaiTroRepository.findByName("ROLE_BILLING_STAFF").orElseGet(() -> vaiTroRepository.save(VaiTro.builder().name("ROLE_BILLING_STAFF").build()));

        if (!taiKhoanRepository.existsByUsername("admin")) {
            TaiKhoan admin = TaiKhoan.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("Admin@123"))
                    .email("admin@example.com")
                    .fullName("Administrator")
                    .active(true)
                    .build();
            Set<VaiTro> roles = new HashSet<>();
            roles.add(adminRole);
            roles.add(userRole);
            admin.setRoles(roles);
            taiKhoanRepository.save(admin);
        }

        if (!taiKhoanRepository.existsByUsername("billing")) {
            TaiKhoan billing = TaiKhoan.builder()
                    .username("billing")
                    .password(passwordEncoder.encode("Billing@123"))
                    .email("billing@example.com")
                    .fullName("Billing Staff")
                    .active(true)
                    .build();
            Set<VaiTro> roles2 = new HashSet<>();
            roles2.add(billingRole);
            roles2.add(userRole);
            billing.setRoles(roles2);
            taiKhoanRepository.save(billing);
        }

        // create a sample tenant account and nguoithue for testing
        if (!taiKhoanRepository.existsByUsername("tenant1")) {
            TaiKhoan tenant = TaiKhoan.builder()
                    .username("tenant1")
                    .password(passwordEncoder.encode("Tenant@123"))
                    .email("tenant1@example.com")
                    .fullName("Tenant One")
                    .active(true)
                    .build();
            Set<VaiTro> roles3 = new HashSet<>();
            roles3.add(userRole);
            tenant.setRoles(roles3);
            TaiKhoan savedTenant = taiKhoanRepository.save(tenant);

            NguoiThue nt = NguoiThue.builder()
                    .cccd("123456789")
                    .hoTen("Nguyen Van A")
                    .ngaySinh(null)
                    .gioiTinh("Nam")
                    .diaChi("Some address")
                    .sdt("0123456789")
                    .taiKhoan(savedTenant)
                    .sophong(null)
                    .build();
            NguoiThue savedNt = nguoiThueRepository.save(nt);

            // create a sample Phong for testing if none exist
            Phong phong = phongRepository.findAll().stream().findFirst().orElseGet(() -> {
                Phong p = Phong.builder()
                        .maPhong("P-101")
                        .toaNhaId(1L)
                        .khuId(1L)
                        .loaiPhongId(1L)
                        .soGiuong(1)
                        .trangThai("TRONG")
                        .moTa("Phòng mẫu cho test")
                        .build();
                return phongRepository.save(p);
            });

            // update tenant's sophong to point to the created room id
            if (savedNt.getSophong() == null) {
                savedNt.setSophong(phong.getId());
                nguoiThueRepository.save(savedNt);
            }

            // create a sample HopDong linking the tenant and the room if none exist
            if (hopDongRepository.findAll().isEmpty()) {
                HopDong hd = HopDong.builder()
                        .maHopDong("HD-" + System.currentTimeMillis())
                        .phongId(phong.getId())
                        .nguoiId(savedNt.getId())
                        .ngayBatDau(LocalDate.now())
                        .ngayKetThuc(null)
                        .tienCoc(BigDecimal.ZERO)
                        .tienThue(new BigDecimal("1000000"))
                        .trangThai("ACTIVE")
                        .build();
                hopDongRepository.save(hd);
            }
        }
    }
}

