-- Create tai_khoan, vai_tro and join table for user/role mapping
CREATE TABLE IF NOT EXISTS vai_tro (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(128) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS tai_khoan (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(255) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL,
  email VARCHAR(255),
  full_name VARCHAR(255),
  active BOOLEAN DEFAULT TRUE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS tai_khoan_vai_tro (
  tai_khoan_id BIGINT NOT NULL,
  vai_tro_id BIGINT NOT NULL,
  PRIMARY KEY (tai_khoan_id, vai_tro_id),
  CONSTRAINT fk_tkv_taikhoan FOREIGN KEY (tai_khoan_id) REFERENCES tai_khoan(id) ON DELETE CASCADE,
  CONSTRAINT fk_tkv_vaitro FOREIGN KEY (vai_tro_id) REFERENCES vai_tro(id) ON DELETE CASCADE
);
