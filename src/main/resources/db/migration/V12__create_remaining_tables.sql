-- Catch-all migration to create remaining entity tables if they do not exist
-- This migration intentionally avoids strict foreign key constraints to be resilient

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

CREATE TABLE IF NOT EXISTS vai_tro (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(128) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS tai_khoan_vai_tro (
  tai_khoan_id BIGINT NOT NULL,
  vai_tro_id BIGINT NOT NULL,
  PRIMARY KEY (tai_khoan_id, vai_tro_id)
  -- Foreign keys omitted to avoid ordering issues during catch-all migration
);

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
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS su_co (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  loai VARCHAR(64),
  mo_ta TEXT,
  toa_nha_id BIGINT NULL,
  phong_id BIGINT NULL,
  reported_by BIGINT NULL,
  reported_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  resolved_at TIMESTAMP NULL,
  status VARCHAR(32) DEFAULT 'OPEN',
  image_paths TEXT
);

CREATE TABLE IF NOT EXISTS khach_vaora (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  ten VARCHAR(255),
  cmnd VARCHAR(64),
  sdt VARCHAR(64),
  phong_id BIGINT NULL,
  loai VARCHAR(32),
  timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  ghi_chu TEXT,
  approval_status VARCHAR(32)
);

-- Hoa don and related tables are created in earlier migrations (V1), but include safe CREATE IF NOT EXISTS
CREATE TABLE IF NOT EXISTS hoa_don (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  invoice_number VARCHAR(128) UNIQUE,
  tenant_id BIGINT NULL,
  period_year INT NOT NULL,
  period_month INT NOT NULL,
  issue_date DATE,
  due_date DATE,
  total_amount DECIMAL(15,2) DEFAULT 0,
  penalty_amount DECIMAL(15,2) DEFAULT 0,
  status VARCHAR(32) DEFAULT 'UNPAID',
  created_by VARCHAR(128),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS hoa_don_dien (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  hoa_don_id BIGINT,
  meter_id VARCHAR(128),
  start_reading BIGINT,
  end_reading BIGINT,
  consumption BIGINT,
  unit_price DECIMAL(15,2),
  amount DECIMAL(15,2),
  period_year INT NOT NULL,
  period_month INT NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS hoa_don_nuoc (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  hoa_don_id BIGINT,
  meter_id VARCHAR(128),
  start_reading BIGINT,
  end_reading BIGINT,
  consumption BIGINT,
  unit_price DECIMAL(15,2),
  amount DECIMAL(15,2),
  period_year INT NOT NULL,
  period_month INT NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS phieu_thu (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  hoa_don_id BIGINT,
  amount_paid DECIMAL(15,2) NOT NULL,
  payment_method VARCHAR(32) NOT NULL,
  payment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  transaction_id VARCHAR(128),
  created_by VARCHAR(128),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS qr_payment_log (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  hoa_don_id BIGINT NOT NULL,
  qr_code VARCHAR(256) NOT NULL,
  qr_payload TEXT,
  expected_amount DECIMAL(15,2) NOT NULL,
  status VARCHAR(32) DEFAULT 'CREATED',
  generated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  expires_at TIMESTAMP NULL,
  paid_at TIMESTAMP NULL,
  transaction_id VARCHAR(128)
);

CREATE TABLE IF NOT EXISTS bank_info (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  account_number VARCHAR(255),
  owner_name VARCHAR(255),
  bank_name VARCHAR(255),
  image_base64 LONGTEXT,
  image_url VARCHAR(1000),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS don_gia (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  meter_type VARCHAR(32) NOT NULL,
  effective_from DATE NOT NULL,
  effective_to DATE NULL,
  price_per_unit DECIMAL(15,2) NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS chi_so (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  meter_type VARCHAR(32) NOT NULL,
  meter_id VARCHAR(128) NULL,
  tenant_id BIGINT NULL,
  reading_value BIGINT NOT NULL,
  recorded_at DATE NOT NULL,
  period_year INT NOT NULL,
  period_month INT NOT NULL,
  recorded_by VARCHAR(128),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

