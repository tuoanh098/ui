package com.trohub.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.trohub.ui.common.TroHubActivity;

import com.trohub.ui.R;
import com.trohub.ui.api.ApiService;
import com.trohub.ui.api.NetworkClient;
import com.trohub.ui.api.models.BankInfo;
import com.trohub.ui.api.models.BankInfoRequest;
import com.trohub.ui.api.models.Landlord;
import com.trohub.ui.api.models.Phong;
import com.trohub.ui.api.models.Tenant;
import com.trohub.ui.auth.LoginActivity;
import com.trohub.ui.auth.SessionManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends TroHubActivity {

    private SessionManager sessionManager;
    private ApiService apiService;
    private Long accountId;
    private boolean canManageBank;

    private ProgressBar progressBar;
    private TextView tvHeader;
    private TextView tvMode;
    private TextView tvStatus;

    private View landlordSection;
    private TextView tvLandlordName;
    private TextView tvLandlordEmail;
    private TextView tvLandlordPhone;
    private TextView tvLandlordAddress;
    private EditText etBankName;
    private EditText etBankOwner;
    private EditText etBankAccount;
    private Button btnSaveBank;

    private View tenantSection;
    private TextView tvTenantName;
    private TextView tvTenantCccd;
    private TextView tvTenantRoom;
    private EditText etTenantPhone;
    private EditText etTenantAddress;
    private EditText etTenantQueQuan;
    private EditText etTenantJob;
    private EditText etTenantContact;
    private Button btnSaveTenant;
    private Button btnLogoutProfile;

    private Landlord currentLandlord;
    private Tenant currentTenant;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        sessionManager = new SessionManager(this);
        NetworkClient.setAuthToken(sessionManager.getToken());
        apiService = NetworkClient.getRetrofitClient().create(ApiService.class);
        accountId = sessionManager.getUserIdFromToken();
        canManageBank = sessionManager.isAdminOrLandlord();

        bindViews();
        tvHeader.setText("Tài khoản: " + sessionManager.getUsername());

        btnSaveTenant.setOnClickListener(v -> saveTenantProfile());
        btnSaveBank.setOnClickListener(v -> saveBankInfo());
        btnLogoutProfile.setOnClickListener(v -> {
            NetworkClient.setAuthToken(null);
            sessionManager.clear();
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        loadProfile();
    }

    private void bindViews() {
        progressBar = findViewById(R.id.progressBar);
        tvHeader = findViewById(R.id.tvHeader);
        tvMode = findViewById(R.id.tvMode);
        tvStatus = findViewById(R.id.tvStatus);

        landlordSection = findViewById(R.id.landlordSection);
        tvLandlordName = findViewById(R.id.tvLandlordName);
        tvLandlordEmail = findViewById(R.id.tvLandlordEmail);
        tvLandlordPhone = findViewById(R.id.tvLandlordPhone);
        tvLandlordAddress = findViewById(R.id.tvLandlordAddress);
        etBankName = findViewById(R.id.etBankName);
        etBankOwner = findViewById(R.id.etBankOwner);
        etBankAccount = findViewById(R.id.etBankAccount);
        btnSaveBank = findViewById(R.id.btnSaveBank);

        tenantSection = findViewById(R.id.tenantSection);
        tvTenantName = findViewById(R.id.tvTenantName);
        tvTenantCccd = findViewById(R.id.tvTenantCccd);
        tvTenantRoom = findViewById(R.id.tvTenantRoom);
        etTenantPhone = findViewById(R.id.etTenantPhone);
        etTenantAddress = findViewById(R.id.etTenantAddress);
        etTenantQueQuan = findViewById(R.id.etTenantQueQuan);
        etTenantJob = findViewById(R.id.etTenantJob);
        etTenantContact = findViewById(R.id.etTenantContact);
        btnSaveTenant = findViewById(R.id.btnSaveTenant);
        btnLogoutProfile = findViewById(R.id.btnLogoutProfile);
    }

    private void loadProfile() {
        showLoading(true);
        tvStatus.setText("Đang tải profile...");
        apiService.getLandlords().enqueue(new Callback<List<Landlord>>() {
            @Override
            public void onResponse(Call<List<Landlord>> call, Response<List<Landlord>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Landlord landlord = findLandlord(response.body(), accountId);
                    if (landlord != null) {
                        showLandlordProfile(landlord);
                        loadBankInfo();
                        showLoading(false);
                        return;
                    }
                }
                loadTenantProfile();
            }

            @Override
            public void onFailure(Call<List<Landlord>> call, Throwable t) {
                loadTenantProfile();
            }
        });
    }

    private void loadTenantProfile() {
        apiService.getTenants().enqueue(new Callback<List<Tenant>>() {
            @Override
            public void onResponse(Call<List<Tenant>> call, Response<List<Tenant>> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    Tenant tenant = findTenant(response.body(), accountId);
                    if (tenant != null) {
                        showTenantProfile(tenant);
                        return;
                    }
                }
                landlordSection.setVisibility(View.GONE);
                tenantSection.setVisibility(View.GONE);
                tvMode.setText("Không xác định profile");
                tvStatus.setText("Không tìm thấy dữ liệu profile gắn với tài khoản hiện tại.");
            }

            @Override
            public void onFailure(Call<List<Tenant>> call, Throwable t) {
                showLoading(false);
                landlordSection.setVisibility(View.GONE);
                tenantSection.setVisibility(View.GONE);
                tvMode.setText("Không xác định profile");
                tvStatus.setText("Lỗi tải profile: " + t.getMessage());
            }
        });
    }

    private void showLandlordProfile(Landlord landlord) {
        currentLandlord = landlord;
        currentTenant = null;
        landlordSection.setVisibility(View.VISIBLE);
        tenantSection.setVisibility(View.GONE);

        tvMode.setText("Profile Chủ trọ");
        if (canManageBank) {
            tvStatus.setText("Bạn có thể cấu hình thông tin ngân hàng cho QR thanh toán.");
        } else {
            tvStatus.setText("Tài khoản này không có quyền cấu hình bank info.");
        }
        tvLandlordName.setText("Tên: " + safe(landlord.getTen()));
        tvLandlordEmail.setText("Email: " + safe(landlord.getEmail()));
        tvLandlordPhone.setText("SĐT: " + safe(landlord.getSdt()));
        tvLandlordAddress.setText("Địa chỉ: " + safe(landlord.getDiaChi()));
        etBankName.setEnabled(canManageBank);
        etBankOwner.setEnabled(canManageBank);
        etBankAccount.setEnabled(canManageBank);
        btnSaveBank.setEnabled(canManageBank);
    }

    private void showTenantProfile(Tenant tenant) {
        currentTenant = tenant;
        currentLandlord = null;
        landlordSection.setVisibility(View.GONE);
        tenantSection.setVisibility(View.VISIBLE);

        tvMode.setText("Profile Khách thuê");
        tvStatus.setText("Tên và CCCD là thông tin khóa, chỉ cập nhật thông tin liên hệ.");
        tvTenantName.setText("Tên: " + safe(tenant.getHoTen()));
        tvTenantCccd.setText("CCCD: " + safe(tenant.getCccd()));
        renderTenantRoom(tenant.getSophong());
        etTenantPhone.setText(safeEditable(tenant.getSdt()));
        etTenantAddress.setText(safeEditable(tenant.getDiaChi()));
        etTenantQueQuan.setText(safeEditable(tenant.getQueQuan()));
        etTenantJob.setText(safeEditable(tenant.getNgheNghiep()));
        etTenantContact.setText(safeEditable(tenant.getThongTinLienLac()));
    }

    private void renderTenantRoom(Long roomId) {
        if (roomId == null) {
            tvTenantRoom.setText("Phòng: Chưa gán");
            return;
        }
        tvTenantRoom.setText("Phòng: Đang tải...");
        apiService.getPhongs().enqueue(new Callback<List<Phong>>() {
            @Override
            public void onResponse(Call<List<Phong>> call, Response<List<Phong>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (Phong room : response.body()) {
                        if (room != null && room.getId() != null && room.getId().equals(roomId)) {
                            tvTenantRoom.setText("Phòng: " + safe(room.getMaPhong()));
                            return;
                        }
                    }
                }
                tvTenantRoom.setText("Phòng: Chưa có tên");
            }

            @Override
            public void onFailure(Call<List<Phong>> call, Throwable t) {
                tvTenantRoom.setText("Phòng: Chưa có tên");
            }
        });
    }

    private void loadBankInfo() {
        apiService.getBankInfo().enqueue(new Callback<BankInfo>() {
            @Override
            public void onResponse(Call<BankInfo> call, Response<BankInfo> response) {
                if (response.isSuccessful() && response.body() != null) {
                    etBankName.setText(safeEditable(response.body().getBankName()));
                    etBankOwner.setText(safeEditable(response.body().getOwnerName()));
                    etBankAccount.setText(safeEditable(response.body().getAccountNumber()));
                } else if (response.code() == 403 || response.code() == 500) {
                    tvStatus.setText("Tài khoản hiện chưa có quyền xem bank info.");
                }
            }

            @Override
            public void onFailure(Call<BankInfo> call, Throwable t) {
                tvStatus.setText("Không tải được bank info: " + t.getMessage());
            }
        });
    }

    private void saveBankInfo() {
        if (!canManageBank) {
            Toast.makeText(this, "Bạn không có quyền cập nhật bank info", Toast.LENGTH_SHORT).show();
            return;
        }

        String bankName = etBankName.getText().toString().trim();
        String ownerName = etBankOwner.getText().toString().trim();
        String accountNumber = etBankAccount.getText().toString().trim();

        if (bankName.isEmpty() || ownerName.isEmpty() || accountNumber.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đủ thông tin ngân hàng", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSaveBank.setEnabled(false);
        apiService.upsertBankInfo(new BankInfoRequest(accountNumber, ownerName, bankName, null)).enqueue(new Callback<BankInfo>() {
            @Override
            public void onResponse(Call<BankInfo> call, Response<BankInfo> response) {
                btnSaveBank.setEnabled(true);
                if (response.isSuccessful()) {
                    Toast.makeText(ProfileActivity.this, "Cập nhật thông tin ngân hàng thành công", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ProfileActivity.this, "Không có quyền cập nhật bank info", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<BankInfo> call, Throwable t) {
                btnSaveBank.setEnabled(true);
                Toast.makeText(ProfileActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveTenantProfile() {
        if (currentTenant == null || currentTenant.getId() == null) {
            Toast.makeText(this, "Không có profile tenant để cập nhật", Toast.LENGTH_SHORT).show();
            return;
        }

        String phone = etTenantPhone.getText().toString().trim();
        if (!phone.isEmpty() && !phone.matches("^[0-9+\\- ]{7,20}$")) {
            Toast.makeText(this, "SĐT không hợp lệ (7-20 ký tự số, +, -, khoảng trắng)", Toast.LENGTH_LONG).show();
            return;
        }

        // Tenant chỉ được sửa thông tin liên hệ. Các trường định danh/quan hệ phải giữ nguyên.
        Tenant payload = new Tenant();
        payload.setId(currentTenant.getId());
        payload.setCccd(currentTenant.getCccd());
        payload.setHoTen(currentTenant.getHoTen());
        payload.setNgaySinh(currentTenant.getNgaySinh());
        payload.setGioiTinh(currentTenant.getGioiTinh());
        payload.setTaiKhoanId(currentTenant.getTaiKhoanId());
        payload.setSophong(currentTenant.getSophong());
        payload.setSdt(phone);
        payload.setDiaChi(etTenantAddress.getText().toString().trim());
        payload.setQueQuan(etTenantQueQuan.getText().toString().trim());
        payload.setNgheNghiep(etTenantJob.getText().toString().trim());
        payload.setThongTinLienLac(etTenantContact.getText().toString().trim());

        btnSaveTenant.setEnabled(false);
        apiService.updateTenant(currentTenant.getId(), payload).enqueue(new Callback<Tenant>() {
            @Override
            public void onResponse(Call<Tenant> call, Response<Tenant> response) {
                btnSaveTenant.setEnabled(true);
                if (response.isSuccessful() && response.body() != null) {
                    currentTenant = response.body();
                    showTenantProfile(currentTenant);
                    Toast.makeText(ProfileActivity.this, "Cập nhật profile thành công", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ProfileActivity.this, "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Tenant> call, Throwable t) {
                btnSaveTenant.setEnabled(true);
                Toast.makeText(ProfileActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private Landlord findLandlord(List<Landlord> landlords, Long accountId) {
        if (accountId == null) return null;
        for (Landlord landlord : landlords) {
            if (landlord.getTaiKhoanId() != null && landlord.getTaiKhoanId().equals(accountId)) {
                return landlord;
            }
        }
        return null;
    }

    private Tenant findTenant(List<Tenant> tenants, Long accountId) {
        if (accountId == null) return null;
        for (Tenant tenant : tenants) {
            if (tenant.getTaiKhoanId() != null && tenant.getTaiKhoanId().equals(accountId)) {
                return tenant;
            }
        }
        return null;
    }

    private String safe(String value) {
        return value == null || value.trim().isEmpty() ? "N/A" : value;
    }

    private String safeEditable(String value) {
        return value == null ? "" : value;
    }

    private void showLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }
}
