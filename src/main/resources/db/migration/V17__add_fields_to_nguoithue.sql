-- Add new columns to nguoithue: que_quan, nghe_nghiep, thong_tin_lien_lac
SET @schema = DATABASE();

-- que_quan
SET @col_exists = (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @schema AND TABLE_NAME = 'nguoithue' AND COLUMN_NAME = 'que_quan'
);
SELECT IF(@col_exists = 0, 'ALTER TABLE nguoithue ADD COLUMN que_quan VARCHAR(255);', 'SELECT 1;') INTO @s;
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- nghe_nghiep
SET @col_exists = (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @schema AND TABLE_NAME = 'nguoithue' AND COLUMN_NAME = 'nghe_nghiep'
);
SELECT IF(@col_exists = 0, 'ALTER TABLE nguoithue ADD COLUMN nghe_nghiep VARCHAR(255);', 'SELECT 1;') INTO @s;
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- thong_tin_lien_lac
SET @col_exists = (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @schema AND TABLE_NAME = 'nguoithue' AND COLUMN_NAME = 'thong_tin_lien_lac'
);
SELECT IF(@col_exists = 0, 'ALTER TABLE nguoithue ADD COLUMN thong_tin_lien_lac VARCHAR(1024);', 'SELECT 1;') INTO @s;
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

