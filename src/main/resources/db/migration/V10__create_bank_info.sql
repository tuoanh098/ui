-- Flyway migration: create bank_info table for storing admin bank account and optional image
CREATE TABLE IF NOT EXISTS bank_info (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  account_number VARCHAR(255),
  owner_name VARCHAR(255),
  bank_name VARCHAR(255),
  image_base64 LONGTEXT,
  created_at DATETIME,
  updated_at DATETIME,
  image_url VARCHAR(1000)
);

