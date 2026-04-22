package com.trohub.ui.contracts;

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
import com.trohub.ui.api.models.Contract;
import com.trohub.ui.api.models.Phong;
import com.trohub.ui.api.models.Tenant;
import com.trohub.ui.auth.SessionManager;
import com.trohub.ui.common.IdLabelOption;
import com.trohub.ui.common.SelectionHelper;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ContractsActivity extends AppCompatActivity implements ContractsAdapter.ContractActionListener {

    private RecyclerView rvContracts;
    private ContractsAdapter adapter;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private TextView tvSummary;
    private SwipeRefreshLayout swipeRefresh;
    private Button btnAddContract;

    private SessionManager sessionManager;
    private ApiService apiService;
    private Long accountId;
    private boolean isTenantOnly;
    private boolean canManageContracts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contracts);

        rvContracts = findViewById(R.id.rvContracts);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);
        tvSummary = findViewById(R.id.tvSummary);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        btnAddContract = findViewById(R.id.btnAddContract);

        rvContracts.setLayoutManager(new LinearLayoutManager(this));
        sessionManager = new SessionManager(this);
        apiService = NetworkClient.getRetrofitClient().create(ApiService.class);
        accountId = sessionManager.getUserIdFromToken();
        canManageContracts = sessionManager.isAdminOrLandlord();
        isTenantOnly = sessionManager.hasAnyRole("ROLE_USER")
                && !sessionManager.hasAnyRole("ROLE_ADMIN", "ROLE_BILLING_STAFF", "ROLE_LANDLORD");

        adapter = new ContractsAdapter(this, canManageContracts);
        rvContracts.setAdapter(adapter);

        btnAddContract.setVisibility(canManageContracts ? View.VISIBLE : View.GONE);
        btnAddContract.setOnClickListener(v -> showCreateOrEditDialog(null));

        swipeRefresh.setOnRefreshListener(() -> loadContracts(false));
        loadContracts(true);
    }

    private void loadContracts(boolean firstLoad) {
        if (firstLoad) {
            progressBar.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.GONE);
        }

        apiService.getContracts().enqueue(new Callback<List<Contract>>() {
            @Override
            public void onResponse(Call<List<Contract>> call, Response<List<Contract>> response) {
                progressBar.setVisibility(View.GONE);
                swipeRefresh.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null) {
                    handleRoleFilter(response.body());
                } else {
                    tvEmpty.setVisibility(View.VISIBLE);
                    tvEmpty.setText("Không tải được danh sách hợp đồng");
                    Toast.makeText(ContractsActivity.this, "Lỗi lấy danh sách hợp đồng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Contract>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                swipeRefresh.setRefreshing(false);
                tvEmpty.setVisibility(View.VISIBLE);
                tvEmpty.setText("Không có kết nối mạng");
                Toast.makeText(ContractsActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleRoleFilter(List<Contract> allContracts) {
        if (isTenantOnly) {
            filterForTenant(allContracts);
            return;
        }
        applyContracts(allContracts);
    }

    private void filterForTenant(List<Contract> allContracts) {
        apiService.getTenants().enqueue(new Callback<List<Tenant>>() {
            @Override
            public void onResponse(Call<List<Tenant>> call, Response<List<Tenant>> response) {
                if (!(response.isSuccessful() && response.body() != null)) {
                    applyContracts(new ArrayList<>());
                    return;
                }
                Long tenantId = null;
                for (Tenant tenant : response.body()) {
                    if (tenant.getTaiKhoanId() != null && tenant.getTaiKhoanId().equals(accountId)) {
                        tenantId = tenant.getId();
                        break;
                    }
                }
                List<Contract> filtered = new ArrayList<>();
                for (Contract contract : allContracts) {
                    if (tenantId != null && contract.getNguoiId() != null && contract.getNguoiId().equals(tenantId)) {
                        filtered.add(contract);
                    }
                }
                applyContracts(filtered);
            }

            @Override
            public void onFailure(Call<List<Tenant>> call, Throwable t) {
                applyContracts(new ArrayList<>());
            }
        });
    }

    private void applyContracts(List<Contract> contracts) {
        adapter.setContracts(contracts);
        int total = contracts == null ? 0 : contracts.size();
        tvSummary.setText(total + " hợp đồng");
        tvEmpty.setVisibility(total == 0 ? View.VISIBLE : View.GONE);
        if (total == 0) {
            tvEmpty.setText("Không có hợp đồng phù hợp quyền truy cập");
        }
    }

    @Override
    public void onEditContract(Contract contract) {
        showCreateOrEditDialog(contract);
    }

    private void showCreateOrEditDialog(Contract editing) {
        apiService.getPhongs().enqueue(new Callback<List<Phong>>() {
            @Override
            public void onResponse(Call<List<Phong>> call, Response<List<Phong>> roomResp) {
                List<Phong> rooms = roomResp.isSuccessful() && roomResp.body() != null
                        ? roomResp.body() : new ArrayList<>();
                apiService.getTenants().enqueue(new Callback<List<Tenant>>() {
                    @Override
                    public void onResponse(Call<List<Tenant>> call, Response<List<Tenant>> tenantResp) {
                        List<Tenant> tenants = tenantResp.isSuccessful() && tenantResp.body() != null
                                ? tenantResp.body() : new ArrayList<>();
                        showCreateOrEditDialogInternal(editing, rooms, tenants);
                    }

                    @Override
                    public void onFailure(Call<List<Tenant>> call, Throwable t) {
                        showCreateOrEditDialogInternal(editing, rooms, new ArrayList<>());
                    }
                });
            }

            @Override
            public void onFailure(Call<List<Phong>> call, Throwable t) {
                apiService.getTenants().enqueue(new Callback<List<Tenant>>() {
                    @Override
                    public void onResponse(Call<List<Tenant>> call, Response<List<Tenant>> tenantResp) {
                        List<Tenant> tenants = tenantResp.isSuccessful() && tenantResp.body() != null
                                ? tenantResp.body() : new ArrayList<>();
                        showCreateOrEditDialogInternal(editing, new ArrayList<>(), tenants);
                    }

                    @Override
                    public void onFailure(Call<List<Tenant>> call, Throwable t2) {
                        showCreateOrEditDialogInternal(editing, new ArrayList<>(), new ArrayList<>());
                    }
                });
            }
        });
    }

    private void showCreateOrEditDialogInternal(Contract editing, List<Phong> rooms, List<Tenant> tenants) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_contract_form, null, false);
        EditText etCode = view.findViewById(R.id.etContractCode);
        AutoCompleteTextView etRoomId = view.findViewById(R.id.etContractRoomId);
        AutoCompleteTextView etTenantId = view.findViewById(R.id.etContractTenantId);
        EditText etStart = view.findViewById(R.id.etContractStart);
        EditText etEnd = view.findViewById(R.id.etContractEnd);
        EditText etRent = view.findViewById(R.id.etContractRent);
        EditText etElectric = view.findViewById(R.id.etContractElectric);
        EditText etWater = view.findViewById(R.id.etContractWater);
        EditText etStatus = view.findViewById(R.id.etContractStatus);

        List<IdLabelOption> roomOptions = new ArrayList<>();
        for (Phong room : rooms) {
            if (room == null || room.getId() == null) continue;
            String label = safe(room.getMaPhong()) + " | " + safe(room.getTrangThai());
            roomOptions.add(new IdLabelOption(room.getId(), label));
        }
        SelectionHelper.bindOptions(etRoomId, roomOptions);

        List<IdLabelOption> tenantOptions = new ArrayList<>();
        for (Tenant tenant : tenants) {
            if (tenant == null || tenant.getId() == null) continue;
            String label = safe(tenant.getHoTen()) + " | CCCD: " + safe(tenant.getCccd());
            tenantOptions.add(new IdLabelOption(tenant.getId(), label));
        }
        SelectionHelper.bindOptions(etTenantId, tenantOptions);

        if (editing != null) {
            etCode.setText(safeEditable(editing.getMaHopDong()));
            String roomLabel = SelectionHelper.findLabelById(roomOptions, editing.getPhongId());
            String tenantLabel = SelectionHelper.findLabelById(tenantOptions, editing.getNguoiId());
            etRoomId.setText(roomLabel.isEmpty() && editing.getPhongId() != null ? "ID " + editing.getPhongId() : roomLabel, false);
            etTenantId.setText(tenantLabel.isEmpty() && editing.getNguoiId() != null ? "ID " + editing.getNguoiId() : tenantLabel, false);
            etStart.setText(safeEditable(editing.getNgayBatDau()));
            etEnd.setText(safeEditable(editing.getNgayKetThuc()));
            etRent.setText(editing.getTienThue() == null ? "" : String.valueOf(editing.getTienThue()));
            etElectric.setText(editing.getTienDienPerUnit() == null ? "" : String.valueOf(editing.getTienDienPerUnit()));
            etWater.setText(editing.getTienNuocFixed() == null ? "" : String.valueOf(editing.getTienNuocFixed()));
            etStatus.setText(safeEditable(editing.getTrangThai()));
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(editing == null ? "Thêm hợp đồng" : "Sửa hợp đồng")
                .setView(view)
                .setPositiveButton(editing == null ? "Thêm" : "Lưu", null)
                .setNegativeButton("Hủy", null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String code = etCode.getText().toString().trim();
            String roomRaw = etRoomId.getText().toString().trim();
            String tenantRaw = etTenantId.getText().toString().trim();
            String start = etStart.getText().toString().trim();
            String end = etEnd.getText().toString().trim();
            String rentRaw = etRent.getText().toString().trim();
            String electricRaw = etElectric.getText().toString().trim();
            String waterRaw = etWater.getText().toString().trim();
            String status = etStatus.getText().toString().trim();

            if (code.isEmpty()) {
                etCode.setError("Mã hợp đồng bắt buộc");
                return;
            }
            if (roomRaw.isEmpty()) {
                etRoomId.setError("Phòng bắt buộc");
                return;
            }
            if (tenantRaw.isEmpty()) {
                etTenantId.setError("Người thuê bắt buộc");
                return;
            }
            if (start.isEmpty()) {
                etStart.setError("Ngày bắt đầu bắt buộc (yyyy-MM-dd)");
                return;
            }
            if (rentRaw.isEmpty()) {
                etRent.setError("Tiền thuê bắt buộc");
                return;
            }

            Long selectedRoomId = SelectionHelper.findIdByText(roomOptions, roomRaw);
            Long selectedTenantId = SelectionHelper.findIdByText(tenantOptions, tenantRaw);
            if (selectedRoomId == null) {
                etRoomId.setError("Vui lòng chọn phòng từ danh sách");
                return;
            }
            if (selectedTenantId == null) {
                etTenantId.setError("Vui lòng chọn người thuê từ danh sách");
                return;
            }

            Contract payload = new Contract();
            payload.setMaHopDong(code);
            payload.setNgayBatDau(start);
            payload.setNgayKetThuc(end.isEmpty() ? null : end);
            payload.setTrangThai(status);
            try {
                payload.setPhongId(selectedRoomId);
                payload.setNguoiId(selectedTenantId);
                payload.setTienThue(Double.parseDouble(rentRaw));
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Tiền thuê không hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!electricRaw.isEmpty()) {
                try {
                    payload.setTienDienPerUnit(Double.parseDouble(electricRaw));
                } catch (NumberFormatException e) {
                    etElectric.setError("Giá điện không hợp lệ");
                    return;
                }
            }
            if (!waterRaw.isEmpty()) {
                try {
                    payload.setTienNuocFixed(Double.parseDouble(waterRaw));
                } catch (NumberFormatException e) {
                    etWater.setError("Tiền nước không hợp lệ");
                    return;
                }
            }

            Call<Contract> call = editing == null
                    ? apiService.createContract(payload)
                    : apiService.updateContract(editing.getId(), payload);
            call.enqueue(new Callback<Contract>() {
                @Override
                public void onResponse(Call<Contract> call, Response<Contract> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(ContractsActivity.this, editing == null ? "Đã thêm hợp đồng" : "Đã cập nhật hợp đồng", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        loadContracts(false);
                    } else {
                        Toast.makeText(ContractsActivity.this, "Lưu thất bại: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Contract> call, Throwable t) {
                    Toast.makeText(ContractsActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }));

        dialog.show();
    }

    private String safe(String value) {
        return value == null || value.trim().isEmpty() ? "N/A" : value;
    }

    private String safeEditable(String value) {
        return value == null ? "" : value;
    }
}
