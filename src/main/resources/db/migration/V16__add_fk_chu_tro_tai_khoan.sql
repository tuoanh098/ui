-- Add foreign key from chu_tro.tai_khoan_id -> tai_khoan(id) if missing
-- Idempotent: checks information_schema for existing FK

SET @fk_exists = (
  SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS tc
  JOIN information_schema.KEY_COLUMN_USAGE kcu ON tc.CONSTRAINT_NAME = kcu.CONSTRAINT_NAME AND tc.TABLE_SCHEMA = kcu.TABLE_SCHEMA
  WHERE tc.CONSTRAINT_TYPE = 'FOREIGN KEY' AND tc.TABLE_NAME = 'chu_tro' AND kcu.COLUMN_NAME = 'tai_khoan_id'
);

SELECT IF(@fk_exists = 0, 'ALTER TABLE chu_tro ADD CONSTRAINT fk_chu_tro_tai_khoan FOREIGN KEY (tai_khoan_id) REFERENCES tai_khoan(id) ON DELETE SET NULL ON UPDATE CASCADE;', 'SELECT 1;') INTO @s;
PREPARE stmt FROM @s;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

