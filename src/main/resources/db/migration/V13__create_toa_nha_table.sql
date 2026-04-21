-- Create buildings table (toa_nha)
CREATE TABLE IF NOT EXISTS toa_nha (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  ten VARCHAR(255) NOT NULL,
  dia_chi VARCHAR(1024),
  created_at DATETIME,
  updated_at DATETIME
);

-- if phong.toa_nha_id column exists add FK constraint (guard with exists check)
-- ensure the column exists (MySQL doesn't support ALTER TABLE IF EXISTS / ADD COLUMN IF NOT EXISTS in all versions)
SET @col_exists = (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'phong' AND COLUMN_NAME = 'toa_nha_id'
);
SELECT IF(@col_exists = 0, 'ALTER TABLE phong ADD COLUMN toa_nha_id BIGINT;', 'SELECT 1;') INTO @s;
PREPARE stmt FROM @s;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Clean up any invalid references before adding FK: set toa_nha_id to NULL where it points to non-existing toa_nha
UPDATE phong p
LEFT JOIN toa_nha t ON p.toa_nha_id = t.id
SET p.toa_nha_id = NULL
WHERE p.toa_nha_id IS NOT NULL AND t.id IS NULL;

-- try to add foreign key if not exists (MySQL doesn't support IF NOT EXISTS for FK, so guard by checking information_schema)
SET @fk_exists = (
  SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
  WHERE CONSTRAINT_SCHEMA = DATABASE()
    AND TABLE_NAME = 'phong'
    AND CONSTRAINT_TYPE = 'FOREIGN KEY'
    AND CONSTRAINT_NAME = 'fk_phong_toa_nha'
);
-- only add if fk doesn't exist
-- The following dynamic SQL will only run if @fk_exists = 0
SELECT IF(@fk_exists = 0, CONCAT('ALTER TABLE phong ADD CONSTRAINT fk_phong_toa_nha FOREIGN KEY (toa_nha_id) REFERENCES toa_nha(id) ON DELETE SET NULL ON UPDATE CASCADE;'), 'SELECT 1;') INTO @s;
PREPARE stmt FROM @s;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

