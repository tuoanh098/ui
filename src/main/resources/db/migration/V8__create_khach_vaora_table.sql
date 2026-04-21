-- Create khach_vaora (guest entries/exits) table
CREATE TABLE IF NOT EXISTS khach_vaora (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  ten VARCHAR(255),
  cmnd VARCHAR(64),
  sdt VARCHAR(64),
  phong_id BIGINT NULL,
  loai VARCHAR(32),
  timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  ghi_chu TEXT
);

