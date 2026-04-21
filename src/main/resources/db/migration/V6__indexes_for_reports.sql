-- Add indexes to support report queries
-- Create indexes only if they do not already exist (portable check)
SET @schema_name = DATABASE();

SET @exists = (SELECT COUNT(1) FROM information_schema.STATISTICS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='hoa_don' AND INDEX_NAME='idx_hoa_don_issue_date');
SET @sql = IF(@exists=0, 'CREATE INDEX idx_hoa_don_issue_date ON hoa_don(issue_date);', 'SELECT 1;');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @exists = (SELECT COUNT(1) FROM information_schema.STATISTICS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='hoa_don' AND INDEX_NAME='idx_hoa_don_tenant');
SET @sql = IF(@exists=0, 'CREATE INDEX idx_hoa_don_tenant ON hoa_don(tenant_id);', 'SELECT 1;');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @exists = (SELECT COUNT(1) FROM information_schema.STATISTICS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='hoa_don' AND INDEX_NAME='idx_hoa_don_status_due');
SET @sql = IF(@exists=0, 'CREATE INDEX idx_hoa_don_status_due ON hoa_don(status, due_date);', 'SELECT 1;');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

