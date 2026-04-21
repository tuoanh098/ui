-- Create hop_dong (contract) table
CREATE TABLE IF NOT EXISTS hop_dong (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  ma_hop_dong VARCHAR(128) NOT NULL UNIQUE,
  phong_id BIGINT NULL,
  nguoi_id BIGINT NULL,
  ngay_bat_dau DATE NULL,
  ngay_ket_thuc DATE NULL,
  tien_coc DECIMAL(15,2) NULL,
  tien_thue DECIMAL(15,2) NULL,
  tien_dien_per_unit DECIMAL(15,2) NULL,
  tien_nuoc_fixed DECIMAL(15,2) NULL,
  trang_thai VARCHAR(32) NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

