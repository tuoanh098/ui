-- Create nguoithue (tenant) table and FK to tai_khoan
CREATE TABLE IF NOT EXISTS nguoithue (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  cccd VARCHAR(64),
  ho_ten VARCHAR(255),
  ngay_sinh DATE,
  gioi_tinh VARCHAR(32),
  dia_chi VARCHAR(512),
  sdt VARCHAR(64),
  tai_khoan_id BIGINT UNIQUE,
  sophong BIGINT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_nguoithue_taikhoan FOREIGN KEY (tai_khoan_id) REFERENCES tai_khoan(id) ON DELETE SET NULL
);

