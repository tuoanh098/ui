-- Add tai_khoan_id column to chu_tro if missing (idempotent)
-- Uses INFORMATION_SCHEMA guard + dynamic SQL so it's safe on different MySQL versions

SET @col_exists = (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'chu_tro' AND COLUMN_NAME = 'tai_khoan_id'
);

SELECT IF(@col_exists = 0, 'ALTER TABLE chu_tro ADD COLUMN tai_khoan_id BIGINT;', 'SELECT 1;') INTO @s;
PREPARE stmt FROM @s;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Optionally create an index to speed up lookups
SET @idx_exists = (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'chu_tro' AND INDEX_NAME = 'idx_chu_tro_tai_khoan'
);
SELECT IF(@idx_exists = 0, 'CREATE INDEX idx_chu_tro_tai_khoan ON chu_tro(tai_khoan_id);', 'SELECT 1;') INTO @s;
PREPARE stmt FROM @s;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

