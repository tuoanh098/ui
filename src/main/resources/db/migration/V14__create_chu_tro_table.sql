-- Create landlord (chu_tro) table and link to toa_nha
CREATE TABLE IF NOT EXISTS chu_tro (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  ten VARCHAR(255) NOT NULL,
  email VARCHAR(255),
  sdt VARCHAR(50),
  dia_chi VARCHAR(1024),
  tai_khoan_id BIGINT,
  created_at DATETIME,
  updated_at DATETIME
);

-- Add chu_tro_id column to toa_nha if missing
-- add column if missing using information_schema + dynamic SQL
SET @col_exists = (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'toa_nha' AND COLUMN_NAME = 'chu_tro_id'
);
SELECT IF(@col_exists = 0, 'ALTER TABLE toa_nha ADD COLUMN chu_tro_id BIGINT;', 'SELECT 1;') INTO @s;
PREPARE stmt FROM @s;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- try to add foreign key if not exists (guarded)
SET @fk_name = 'fk_toa_nha_chu_tro';
SET @exists = (
  SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS tc
  JOIN information_schema.KEY_COLUMN_USAGE kcu ON tc.CONSTRAINT_NAME = kcu.CONSTRAINT_NAME AND tc.TABLE_SCHEMA = kcu.TABLE_SCHEMA
  WHERE tc.CONSTRAINT_TYPE = 'FOREIGN KEY' AND tc.TABLE_NAME = 'toa_nha' AND kcu.COLUMN_NAME = 'chu_tro_id'
);
-- If no existing FK, add it
SELECT IF(@exists = 0, 'ALTER TABLE toa_nha ADD CONSTRAINT fk_toa_nha_chu_tro FOREIGN KEY (chu_tro_id) REFERENCES chu_tro(id) ON DELETE SET NULL', 'SELECT 1;') INTO @s;
PREPARE stmt FROM @s;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;


-- add tai_khoan_id FK from chu_tro to tai_khoan if column exists and FK missing
SET @col_exists = (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'chu_tro' AND COLUMN_NAME = 'tai_khoan_id'
);
-- only try to add FK when column exists
SELECT IF(@col_exists = 1, (
  SELECT IF(
    (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS tc JOIN information_schema.KEY_COLUMN_USAGE kcu ON tc.CONSTRAINT_NAME = kcu.CONSTRAINT_NAME AND tc.TABLE_SCHEMA = kcu.TABLE_SCHEMA WHERE tc.CONSTRAINT_TYPE = 'FOREIGN KEY' AND tc.TABLE_NAME = 'chu_tro' AND kcu.COLUMN_NAME = 'tai_khoan_id') = 0,
    'ALTER TABLE chu_tro ADD CONSTRAINT fk_chu_tro_tai_khoan FOREIGN KEY (tai_khoan_id) REFERENCES tai_khoan(id) ON DELETE SET NULL',
    'SELECT 1;'
  )
), 'SELECT 1;') INTO @s;
PREPARE stmt FROM @s;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;


