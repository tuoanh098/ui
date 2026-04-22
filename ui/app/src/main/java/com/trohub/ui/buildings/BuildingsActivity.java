package com.trohub.ui.buildings;

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
import com.trohub.ui.api.models.Landlord;
import com.trohub.ui.api.models.ToaNha;
import com.trohub.ui.auth.SessionManager;
import com.trohub.ui.common.IdLabelOption;
import com.trohub.ui.common.SelectionHelper;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BuildingsActivity extends AppCompatActivity implements BuildingsAdapter.BuildingActionListener {

    private RecyclerView rvBuildings;
    private BuildingsAdapter adapter;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private SwipeRefreshLayout swipeRefresh;
    private Button btnAddBuilding;

    private SessionManager sessionManager;
    private ApiService apiService;
    private boolean canManageBuildings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buildings);

        rvBuildings = findViewById(R.id.rvBuildings);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        btnAddBuilding = findViewById(R.id.btnAddBuilding);

        rvBuildings.setLayoutManager(new LinearLayoutManager(this));

        sessionManager = new SessionManager(this);
        apiService = NetworkClient.getRetrofitClient().create(ApiService.class);
        canManageBuildings = sessionManager.isAdminOrLandlord();

        adapter = new BuildingsAdapter(this, canManageBuildings);
        rvBuildings.setAdapter(adapter);

        btnAddBuilding.setVisibility(canManageBuildings ? View.VISIBLE : View.GONE);
        btnAddBuilding.setOnClickListener(v -> showCreateOrEditDialog(null));

        swipeRefresh.setOnRefreshListener(() -> loadBuildings(false));
        loadBuildings(true);
    }

    private void loadBuildings(boolean firstLoad) {
        if (firstLoad) {
            progressBar.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.GONE);
        }

        apiService.getBuildings().enqueue(new Callback<List<ToaNha>>() {
            @Override
            public void onResponse(Call<List<ToaNha>> call, Response<List<ToaNha>> response) {
                progressBar.setVisibility(View.GONE);
                swipeRefresh.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null) {
                    applyBuildings(response.body());
                } else {
                    tvEmpty.setVisibility(View.VISIBLE);
                    tvEmpty.setText("Không tải được danh sách tòa nhà");
                    Toast.makeText(BuildingsActivity.this, "Lỗi khi lấy danh sách tòa nhà", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<ToaNha>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                swipeRefresh.setRefreshing(false);
                tvEmpty.setVisibility(View.VISIBLE);
                tvEmpty.setText("Không có kết nối mạng");
                Toast.makeText(BuildingsActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void applyBuildings(List<ToaNha> buildings) {
        adapter.setBuildings(buildings);
        boolean isEmpty = buildings == null || buildings.isEmpty();
        tvEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        if (isEmpty) {
            tvEmpty.setText("Không có tòa nhà phù hợp quyền truy cập");
        }
    }

    @Override
    public void onEdit(ToaNha building) {
        showCreateOrEditDialog(building);
    }

    @Override
    public void onDelete(ToaNha building) {
        if (building == null || building.getId() == null) return;
        new AlertDialog.Builder(this)
                .setTitle("Xóa tòa nhà")
                .setMessage("Bạn chắc chắn muốn xóa tòa \"" + safe(building.getTen()) + "\"?")
                .setPositiveButton("Xóa", (dialog, which) -> doDelete(building.getId()))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void doDelete(Long id) {
        apiService.deleteBuilding(id).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(BuildingsActivity.this, "Đã xóa tòa nhà", Toast.LENGTH_SHORT).show();
                    loadBuildings(false);
                } else {
                    Toast.makeText(BuildingsActivity.this, "Xóa thất bại: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(BuildingsActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showCreateOrEditDialog(ToaNha editing) {
        apiService.getLandlords().enqueue(new Callback<List<Landlord>>() {
            @Override
            public void onResponse(Call<List<Landlord>> call, Response<List<Landlord>> response) {
                List<Landlord> landlords = response.isSuccessful() && response.body() != null
                        ? response.body() : new ArrayList<>();
                showCreateOrEditDialogInternal(editing, landlords);
            }

            @Override
            public void onFailure(Call<List<Landlord>> call, Throwable t) {
                showCreateOrEditDialogInternal(editing, new ArrayList<>());
            }
        });
    }

    private void showCreateOrEditDialogInternal(ToaNha editing, List<Landlord> landlords) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_building_form, null, false);
        EditText etName = view.findViewById(R.id.etBuildingName);
        EditText etAddress = view.findViewById(R.id.etBuildingAddress);
        AutoCompleteTextView etManagerId = view.findViewById(R.id.etBuildingManagerId);

        List<IdLabelOption> managerOptions = new ArrayList<>();
        for (Landlord landlord : landlords) {
            if (landlord == null || landlord.getId() == null) continue;
            String label = safe(landlord.getTen()) + " | SĐT: " + safe(landlord.getSdt())
                    + " | Email: " + safe(landlord.getEmail());
            managerOptions.add(new IdLabelOption(landlord.getId(), label));
        }
        SelectionHelper.bindOptions(etManagerId, managerOptions);

        if (editing != null) {
            etName.setText(safeEditable(editing.getTen()));
            etAddress.setText(safeEditable(editing.getDiaChi()));
            String managerLabel = SelectionHelper.findLabelById(managerOptions, editing.getChuTroId());
            if (managerLabel.isEmpty() && editing.getChuTroId() != null) {
                managerLabel = "ID " + editing.getChuTroId();
            }
            etManagerId.setText(managerLabel, false);
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(editing == null ? "Thêm tòa nhà" : "Sửa tòa nhà")
                .setView(view)
                .setPositiveButton(editing == null ? "Thêm" : "Lưu", null)
                .setNegativeButton("Hủy", null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String address = etAddress.getText().toString().trim();
            String managerRaw = etManagerId.getText().toString().trim();

            if (name.isEmpty()) {
                etName.setError("Tên tòa bắt buộc");
                return;
            }

            ToaNha payload = new ToaNha();
            payload.setTen(name);
            payload.setDiaChi(address);
            Long selectedManagerId = SelectionHelper.findIdByText(managerOptions, managerRaw);
            if (!managerRaw.isEmpty() && selectedManagerId == null) {
                etManagerId.setError("Vui lòng chọn chủ trọ từ danh sách");
                return;
            }
            if (selectedManagerId != null) {
                payload.setChuTroId(selectedManagerId);
            } else if (editing != null) {
                payload.setChuTroId(editing.getChuTroId());
            }

            Call<ToaNha> call = editing == null
                    ? apiService.createBuilding(payload)
                    : apiService.updateBuilding(editing.getId(), payload);

            call.enqueue(new Callback<ToaNha>() {
                @Override
                public void onResponse(Call<ToaNha> call, Response<ToaNha> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(BuildingsActivity.this, editing == null ? "Đã thêm tòa nhà" : "Đã cập nhật tòa nhà", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        loadBuildings(false);
                    } else {
                        Toast.makeText(BuildingsActivity.this, "Lưu thất bại: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ToaNha> call, Throwable t) {
                    Toast.makeText(BuildingsActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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
