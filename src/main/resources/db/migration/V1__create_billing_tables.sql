-- Billing tables migration
-- Create invoice (hoa_don) and related tables
CREATE TABLE IF NOT EXISTS don_gia (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  meter_type VARCHAR(32) NOT NULL,
  effective_from DATE NOT NULL,
  effective_to DATE NULL,
  price_per_unit DECIMAL(15,2) NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
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
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_chi_so_meter_period (meter_id, period_year, period_month)
);

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
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_hoa_don_tenant (tenant_id),
  INDEX idx_hoa_don_status_due (status, due_date)
);

CREATE TABLE IF NOT EXISTS hoa_don_dien (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  hoa_don_id BIGINT NOT NULL,
  meter_id VARCHAR(128),
  start_reading BIGINT,
  end_reading BIGINT,
  consumption BIGINT,
  unit_price DECIMAL(15,2),
  amount DECIMAL(15,2),
  period_year INT NOT NULL,
  period_month INT NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_hd_dien_hoa_don (hoa_don_id)
);

CREATE TABLE IF NOT EXISTS hoa_don_nuoc (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  hoa_don_id BIGINT NOT NULL,
  meter_id VARCHAR(128),
  start_reading BIGINT,
  end_reading BIGINT,
  consumption BIGINT,
  unit_price DECIMAL(15,2),
  amount DECIMAL(15,2),
  period_year INT NOT NULL,
  period_month INT NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_hd_nuoc_hoa_don (hoa_don_id)
);

CREATE TABLE IF NOT EXISTS phieu_thu (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  hoa_don_id BIGINT NOT NULL,
  amount_paid DECIMAL(15,2) NOT NULL,
  payment_method VARCHAR(32) NOT NULL,
  payment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  transaction_id VARCHAR(128),
  created_by VARCHAR(128),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_phieu_thu_hoa_don (hoa_don_id),
  UNIQUE KEY uk_phieu_thu_txn (transaction_id)
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
  transaction_id VARCHAR(128),
  INDEX idx_qr_code (qr_code),
  INDEX idx_qr_status_expires (status, expires_at)
);

-- Foreign keys optional (not enforced to avoid cross-schema issues). Add as needed later.

