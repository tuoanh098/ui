-- Create su_co (incidents) table
CREATE TABLE IF NOT EXISTS su_co (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  loai VARCHAR(64),
  mo_ta TEXT,
  toa_nha_id BIGINT NULL,
  phong_id BIGINT NULL,
  reported_by BIGINT NULL,
  reported_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  resolved_at TIMESTAMP NULL,
  status VARCHAR(32) DEFAULT 'OPEN'
);

