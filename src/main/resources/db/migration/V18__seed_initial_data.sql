-- Seed initial demo data: 2 owners, 4 tenants, 2 buildings, 4 rooms, 4 contracts
-- Password for all accounts is '123' (bcrypt hash)

INSERT INTO vai_tro (name)
SELECT 'ROLE_USER' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM vai_tro WHERE name = 'ROLE_USER');

-- bcrypt hash for '123' (generated once)
SET @pwd_hash = '$2b$12$2krAsFFDflCu0pFc0sRm/eRGQMJsflIeF4f6ugnDua7psHP2ta6X6';

-- Insert accounts: 2 owners (ids 1-2) and 4 tenants (ids 3-6)
INSERT INTO tai_khoan (id, username, password, email, full_name, active)
VALUES
  (1, 'ongchu1', @pwd_hash, 'ongchu1@example.com', 'Ong Chu 1', TRUE),
  (2, 'ongchu2', @pwd_hash, 'ongchu2@example.com', 'Ong Chu 2', TRUE),
  (3, 'khachthue1', @pwd_hash, 'kh1@example.com', 'Khach Thue 1', TRUE),
  (4, 'khachthue2', @pwd_hash, 'kh2@example.com', 'Khach Thue 2', TRUE),
  (5, 'khachthue3', @pwd_hash, 'kh3@example.com', 'Khach Thue 3', TRUE),
  (6, 'khachthue4', @pwd_hash, 'kh4@example.com', 'Khach Thue 4', TRUE)
ON DUPLICATE KEY UPDATE username = VALUES(username);

INSERT INTO tai_khoan_vai_tro (tai_khoan_id, vai_tro_id)
SELECT t.id, v.id FROM tai_khoan t
JOIN vai_tro v ON v.name = 'ROLE_USER'
WHERE t.id IN (1,2,3,4,5,6)
ON DUPLICATE KEY UPDATE tai_khoan_id = tai_khoan_id;

-- Insert landlords (chu_tro) linked to owner accounts
INSERT INTO chu_tro (id, ten, email, sdt, dia_chi, tai_khoan_id, created_at, updated_at)
VALUES
  (1, 'Ong Chu 1', 'ongchu1@example.com', '0900000001', 'Dia chi Ong Chu 1', 1, NOW(), NOW()),
  (2, 'Ong Chu 2', 'ongchu2@example.com', '0900000002', 'Dia chi Ong Chu 2', 2, NOW(), NOW())
ON DUPLICATE KEY UPDATE ten = VALUES(ten);

-- Insert buildings (toa_nha)
INSERT INTO toa_nha (id, ten, dia_chi, chu_tro_id, created_at, updated_at)
VALUES
  (1, 'Toa A', 'Dia chi Toa A', 1, NOW(), NOW()),
  (2, 'Toa B', 'Dia chi Toa B', 2, NOW(), NOW())
ON DUPLICATE KEY UPDATE ten = VALUES(ten);

-- Insert rooms (phong) - 3 rooms in Toa A, 1 room in Toa B
INSERT INTO phong (id, ma_phong, toa_nha_id, so_giuong, trang_thai, mo_ta, created_at, updated_at)
VALUES
  (1, 'A101', 1, 2, 'DA_THUE', 'Phong A101', NOW(), NOW()),
  (2, 'A102', 1, 2, 'DA_THUE', 'Phong A102', NOW(), NOW()),
  (3, 'A103', 1, 2, 'TRONG', 'Phong A103', NOW(), NOW()),
  (4, 'B201', 2, 1, 'DA_THUE', 'Phong B201', NOW(), NOW())
ON DUPLICATE KEY UPDATE ma_phong = VALUES(ma_phong);

-- Insert tenants (nguoithue) and link to tai_khoan. sophong stores the room id here.
INSERT INTO nguoithue (id, cccd, ho_ten, ngay_sinh, gioi_tinh, dia_chi, sdt, que_quan, nghe_nghiep, thong_tin_lien_lac, tai_khoan_id, sophong, created_at, updated_at)
VALUES
  (1, 'CITIZEN001', 'Khach Thue 1', NULL, NULL, 'Dia chi KT1', '0910000001', NULL, NULL, NULL, 3, 1, NOW(), NOW()),
  (2, 'CITIZEN002', 'Khach Thue 2', NULL, NULL, 'Dia chi KT2', '0910000002', NULL, NULL, NULL, 4, 1, NOW(), NOW()),
  (3, 'CITIZEN003', 'Khach Thue 3', NULL, NULL, 'Dia chi KT3', '0910000003', NULL, NULL, NULL, 5, 2, NOW(), NOW()),
  (4, 'CITIZEN004', 'Khach Thue 4', NULL, NULL, 'Dia chi KT4', '0910000004', NULL, NULL, NULL, 6, 4, NOW(), NOW())
ON DUPLICATE KEY UPDATE ho_ten = VALUES(ho_ten);

-- Insert contracts (hop_dong) — 4 contracts, two tenants share room 1
INSERT INTO hop_dong (id, ma_hop_dong, phong_id, nguoi_id, ngay_bat_dau, ngay_ket_thuc, tien_coc, tien_thue, tien_dien_per_unit, tien_nuoc_fixed, trang_thai, created_at, updated_at)
VALUES
  (1, 'HD-001', 1, 1, CURDATE(), NULL, 0.00, 2000000.00, 3000.00, 100000.00, 'ACTIVE', NOW(), NOW()),
  (2, 'HD-002', 1, 2, CURDATE(), NULL, 0.00, 2000000.00, 3000.00, 100000.00, 'ACTIVE', NOW(), NOW()),
  (3, 'HD-003', 2, 3, CURDATE(), NULL, 0.00, 2500000.00, 3000.00, 100000.00, 'ACTIVE', NOW(), NOW()),
  (4, 'HD-004', 4, 4, CURDATE(), NULL, 0.00, 2200000.00, 3000.00, 100000.00, 'ACTIVE', NOW(), NOW())
ON DUPLICATE KEY UPDATE ma_hop_dong = VALUES(ma_hop_dong);

-- Mark unused room (A103) as TRONG (empty) above; rented rooms remain DA_THUE

