package com.trohub.ui.tenants;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.trohub.ui.R;
import com.trohub.ui.api.ApiService;
import com.trohub.ui.api.NetworkClient;
import com.trohub.ui.api.models.Phong;
import com.trohub.ui.api.models.TaiKhoanDto;
import com.trohub.ui.api.models.Tenant;
import com.trohub.ui.auth.SessionManager;
import com.trohub.ui.common.IdLabelOption;
import com.trohub.ui.common.SelectionHelper;
import com.trohub.ui.tenants.detail.TenantDetailActivity;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TenantsActivity extends AppCompatActivity implements TenantsAdapter.TenantActionListener {

    private RecyclerView rvTenants;
    private TenantsAdapter adapter;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private SwipeRefreshLayout swipeRefresh;
    private Button btnAddTenant;

    private ApiService apiService;
    private boolean canManageTenants;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tenants);

        rvTenants = findViewById(R.id.rvTenants);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        btnAddTenant = findViewById(R.id.btnAddTenant);
        rvTenants.setLayoutManager(new LinearLayoutManager(this));

        SessionManager sessionManager = new SessionManager(this);
        canManageTenants = sessionManager.isAdminOrLandlord();
        apiService = NetworkClient.getRetrofitClient().create(ApiService.class);

        adapter = new TenantsAdapter(this, canManageTenants);
        adapter.setTenantItemClickListener(this::openTenantDetail);
        rvTenants.setAdapter(adapter);

        btnAddTenant.setVisibility(canManageTenants ? View.VISIBLE : View.GONE);
        btnAddTenant.setOnClickListener(v -> showCreateOrEditDialog(null));

        swipeRefresh.setOnRefreshListener(() -> loadTenants(false));
        loadTenants(true);
    }

    private void loadTenants(boolean firstLoad) {
        if (firstLoad) {
            progressBar.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.GONE);
        }

        apiService.getTenants().enqueue(new Callback<List<Tenant>>() {
            @Override
            public void onResponse(Call<List<Tenant>> call, Response<List<Tenant>> response) {
                progressBar.setVisibility(View.GONE);
                swipeRefresh.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null) {
                    adapter.setTenants(response.body());
                    boolean isEmpty = response.body().isEmpty();
                    tvEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
                } else {
                    tvEmpty.setVisibility(View.VISIBLE);
                    tvEmpty.setText("Không tải được danh sách người thuê");
                    Toast.makeText(TenantsActivity.this, "Lỗi khi lấy danh sách người thuê", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Tenant>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                swipeRefresh.setRefreshing(false);
                tvEmpty.setVisibility(View.VISIBLE);
                tvEmpty.setText("Không có kết nối mạng");
                Toast.makeText(TenantsActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onEditTenant(Tenant tenant) {
        showCreateOrEditDialog(tenant);
    }

    @Override
    public void onDeleteTenant(Tenant tenant) {
        if (tenant == null || tenant.getId() == null) return;
        new AlertDialog.Builder(this)
                .setTitle("Xóa người thuê")
                .setMessage("Xóa người thuê " + safe(tenant.getHoTen()) + "?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    apiService.deleteTenant(tenant.getId()).enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(TenantsActivity.this, "Đã xóa người thuê", Toast.LENGTH_SHORT).show();
                                loadTenants(false);
                            } else {
                                Toast.makeText(TenantsActivity.this, "Xóa thất bại: " + response.code(), Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            Toast.makeText(TenantsActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showCreateOrEditDialog(Tenant editing) {
        apiService.getPhongs().enqueue(new Callback<List<Phong>>() {
            @Override
            public void onResponse(Call<List<Phong>> call, Response<List<Phong>> roomResp) {
                List<Phong> rooms = roomResp.isSuccessful() && roomResp.body() != null
                        ? roomResp.body() : new ArrayList<>();
                apiService.getAuthUsers(null, "ROLE_USER").enqueue(new Callback<List<TaiKhoanDto>>() {
                    @Override
                    public void onResponse(Call<List<TaiKhoanDto>> call, Response<List<TaiKhoanDto>> userResp) {
                        List<TaiKhoanDto> users = userResp.isSuccessful() && userResp.body() != null
                                ? userResp.body() : new ArrayList<>();
                        showCreateOrEditDialogInternal(editing, rooms, users);
                    }

                    @Override
                    public void onFailure(Call<List<TaiKhoanDto>> call, Throwable t) {
                        showCreateOrEditDialogInternal(editing, rooms, new ArrayList<>());
                    }
                });
            }

            @Override
            public void onFailure(Call<List<Phong>> call, Throwable t) {
                apiService.getAuthUsers(null, "ROLE_USER").enqueue(new Callback<List<TaiKhoanDto>>() {
                    @Override
                    public void onResponse(Call<List<TaiKhoanDto>> call, Response<List<TaiKhoanDto>> userResp) {
                        List<TaiKhoanDto> users = userResp.isSuccessful() && userResp.body() != null
                                ? userResp.body() : new ArrayList<>();
                        showCreateOrEditDialogInternal(editing, new ArrayList<>(), users);
                    }

                    @Override
                    public void onFailure(Call<List<TaiKhoanDto>> call, Throwable t2) {
                        showCreateOrEditDialogInternal(editing, new ArrayList<>(), new ArrayList<>());
                    }
                });
            }
        });
    }

    private void showCreateOrEditDialogInternal(Tenant editing, List<Phong> rooms, List<TaiKhoanDto> users) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_tenant_form, null, false);
        EditText etName = view.findViewById(R.id.etTenantName);
        EditText etCccd = view.findViewById(R.id.etTenantCccd);
        EditText etPhone = view.findViewById(R.id.etTenantPhone);
        EditText etBirth = view.findViewById(R.id.etTenantBirth);
        EditText etGender = view.findViewById(R.id.etTenantGender);
        EditText etAddress = view.findViewById(R.id.etTenantAddress);
        AutoCompleteTextView etRoom = view.findViewById(R.id.etTenantRoomId);
        AutoCompleteTextView etAccount = view.findViewById(R.id.etTenantAccountId);

        List<IdLabelOption> roomOptions = new ArrayList<>();
        for (Phong room : rooms) {
            if (room == null || room.getId() == null) continue;
            roomOptions.add(new IdLabelOption(room.getId(), safe(room.getMaPhong()) + " | " + safe(room.getTrangThai())));
        }
        SelectionHelper.bindOptions(etRoom, roomOptions);

        List<IdLabelOption> accountOptions = new ArrayList<>();
        for (TaiKhoanDto user : users) {
            if (user == null || user.getId() == null) continue;
            accountOptions.add(new IdLabelOption(user.getId(),
                    safe(user.getUsername()) + " | " + safe(user.getFullName())));
        }
        SelectionHelper.bindOptions(etAccount, accountOptions);

        if (editing != null) {
            etName.setText(safeEditable(editing.getHoTen()));
            etCccd.setText(safeEditable(editing.getCccd()));
            etPhone.setText(safeEditable(editing.getSdt()));
            etBirth.setText(safeEditable(editing.getNgaySinh()));
            etGender.setText(safeEditable(editing.getGioiTinh()));
            etAddress.setText(safeEditable(editing.getDiaChi()));
            String roomLabel = SelectionHelper.findLabelById(roomOptions, editing.getSophong());
            String accountLabel = SelectionHelper.findLabelById(accountOptions, editing.getTaiKhoanId());
            etRoom.setText(roomLabel.isEmpty() && editing.getSophong() != null ? "ID " + editing.getSophong() : roomLabel, false);
            etAccount.setText(accountLabel.isEmpty() && editing.getTaiKhoanId() != null ? "ID " + editing.getTaiKhoanId() : accountLabel, false);
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(editing == null ? "Thêm người thuê" : "Sửa người thuê")
                .setView(view)
                .setPositiveButton(editing == null ? "Thêm" : "Lưu", null)
                .setNegativeButton("Hủy", null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String cccd = etCccd.getText().toString().trim();
            if (name.isEmpty()) {
                etName.setError("Họ tên bắt buộc");
                return;
            }
            if (cccd.isEmpty()) {
                etCccd.setError("CCCD bắt buộc");
                return;
            }

            Tenant payload = new Tenant();
            payload.setHoTen(name);
            payload.setCccd(cccd);
            payload.setSdt(etPhone.getText().toString().trim());
            payload.setNgaySinh(etBirth.getText().toString().trim());
            payload.setGioiTinh(etGender.getText().toString().trim());
            payload.setDiaChi(etAddress.getText().toString().trim());
            if (editing != null) {
                payload.setQueQuan(editing.getQueQuan());
                payload.setNgheNghiep(editing.getNgheNghiep());
                payload.setThongTinLienLac(editing.getThongTinLienLac());
            }

            String roomRaw = etRoom.getText().toString().trim();
            if (!roomRaw.isEmpty()) {
                Long selectedRoomId = SelectionHelper.findIdByText(roomOptions, roomRaw);
                if (selectedRoomId == null) {
                    etRoom.setError("Vui lòng chọn phòng từ danh sách");
                    return;
                }
                payload.setSophong(selectedRoomId);
            }

            String accountRaw = etAccount.getText().toString().trim();
            if (!accountRaw.isEmpty()) {
                Long selectedAccountId = SelectionHelper.findIdByText(accountOptions, accountRaw);
                if (selectedAccountId == null) {
                    etAccount.setError("Vui lòng chọn tài khoản từ danh sách");
                    return;
                }
                payload.setTaiKhoanId(selectedAccountId);
            }

            Call<Tenant> call = editing == null
                    ? apiService.createTenant(payload)
                    : apiService.updateTenant(editing.getId(), mergeTenant(editing, payload));

            call.enqueue(new Callback<Tenant>() {
                @Override
                public void onResponse(Call<Tenant> call, Response<Tenant> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(TenantsActivity.this, editing == null ? "Đã thêm người thuê" : "Đã cập nhật người thuê", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        loadTenants(false);
                    } else {
                        Toast.makeText(TenantsActivity.this, "Lưu thất bại: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Tenant> call, Throwable t) {
                    Toast.makeText(TenantsActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }));

        dialog.show();
    }

    private Tenant mergeTenant(Tenant original, Tenant patch) {
        Tenant t = new Tenant();
        t.setId(original.getId());
        t.setHoTen(patch.getHoTen());
        t.setCccd(patch.getCccd());
        t.setNgaySinh(patch.getNgaySinh() == null ? original.getNgaySinh() : patch.getNgaySinh());
        t.setGioiTinh(patch.getGioiTinh());
        t.setSdt(patch.getSdt());
        t.setDiaChi(patch.getDiaChi());
        t.setQueQuan(original.getQueQuan());
        t.setNgheNghiep(original.getNgheNghiep());
        t.setThongTinLienLac(original.getThongTinLienLac());
        t.setTaiKhoanId(patch.getTaiKhoanId() == null ? original.getTaiKhoanId() : patch.getTaiKhoanId());
        t.setSophong(patch.getSophong() == null ? original.getSophong() : patch.getSophong());
        return t;
    }

    private String safe(String value) {
        return value == null || value.trim().isEmpty() ? "N/A" : value;
    }

    private String safeEditable(String value) {
        return value == null ? "" : value;
    }

    private void openTenantDetail(Tenant tenant) {
        if (tenant == null || tenant.getId() == null) return;
        android.content.Intent intent = new android.content.Intent(this, TenantDetailActivity.class);
        intent.putExtra("TENANT_ID", tenant.getId());
        startActivity(intent);
    }
}
