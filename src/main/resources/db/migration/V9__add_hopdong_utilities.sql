-- Add per-unit electricity and fixed water columns to hop_dong
-- Use information_schema checks and conditional PREPARE/EXECUTE to be idempotent
-- Avoid top-level IF ... END IF and avoid SQL syntax not supported by some MySQL versions

-- Add tien_dien_per_unit if missing
SET @col_exists = (
  SELECT COUNT(*)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'hop_dong'
    AND column_name = 'tien_dien_per_unit'
);
SET @sql = IF(@col_exists = 0,
  'ALTER TABLE hop_dong ADD COLUMN tien_dien_per_unit DECIMAL(15,2) NULL',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Add tien_nuoc_fixed if missing
SET @col_exists = (
  SELECT COUNT(*)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'hop_dong'
    AND column_name = 'tien_nuoc_fixed'
);
SET @sql = IF(@col_exists = 0,
  'ALTER TABLE hop_dong ADD COLUMN tien_nuoc_fixed DECIMAL(15,2) NULL',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
