package com.trohub.backend.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonFormat;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NguoiThueDto {
    private Long id;

    @NotBlank(message = "cccd is required")
    @Size(max = 64, message = "cccd must be at most 64 characters")
    private String cccd;

    @NotBlank(message = "hoTen is required")
    @Size(max = 255, message = "hoTen must be at most 255 characters")
    private String hoTen;

    @Past(message = "ngaySinh must be in the past")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    private LocalDate ngaySinh;

    @Size(max = 32)
    private String gioiTinh;

    @Size(max = 512)
    private String diaChi;

    @Pattern(regexp = "^[0-9+\\- ]{7,20}$", message = "sdt must contain only digits, spaces, + or - and be 7-20 chars")
    private String sdt;

    @Positive(message = "taiKhoanId must be a positive number")
    private Long taiKhoanId;

    @PositiveOrZero(message = "sophong must be zero or positive")
    private Long sophong;
}

