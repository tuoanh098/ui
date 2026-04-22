package com.trohub.ui.rooms;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.trohub.ui.R;
import com.trohub.ui.api.ApiService;
import com.trohub.ui.api.NetworkClient;
import com.trohub.ui.api.models.Phong;
import com.trohub.ui.api.models.Tenant;
import com.trohub.ui.api.models.ToaNha;
import com.trohub.ui.auth.LoginActivity;
import com.trohub.ui.auth.SessionManager;
import com.trohub.ui.common.IdLabelOption;
import com.trohub.ui.common.SelectionHelper;
import com.trohub.ui.rooms.detail.RoomDetailActivity;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RoomsActivity extends AppCompatActivity implements RoomsAdapter.RoomActionListener {

    private RecyclerView rvRooms;
    private RoomsAdapter adapter;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private SwipeRefreshLayout swipeRefresh;
    private Button btnAddRoom;

    private SessionManager sessionManager;
    private ApiService apiService;
    private Long accountId;
    private boolean isTenantOnly;
    private boolean canCreateRooms;
    private boolean canEditRooms;
    private boolean canDeleteRooms;
    private AlertDialog roomDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rooms);

        rvRooms = findViewById(R.id.rvRooms);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        btnAddRoom = findViewById(R.id.btnAddRoom);

        rvRooms.setLayoutManager(new LinearLayoutManager(this));
        sessionManager = new SessionManager(this);
        NetworkClient.setAuthToken(sessionManager.getToken());
        apiService = NetworkClient.getRetrofitClient().create(ApiService.class);
        accountId = sessionManager.getUserIdFromToken();

        isTenantOnly = sessionManager.hasAnyRole("ROLE_USER")
                && !sessionManager.hasAnyRole("ROLE_ADMIN", "ROLE_BILLING_STAFF", "ROLE_LANDLORD");
        boolean canManageRooms = !isTenantOnly;
        canCreateRooms = canManageRooms;
        canEditRooms = canManageRooms;
        canDeleteRooms = canManageRooms;

        adapter = new RoomsAdapter(this, canEditRooms, canDeleteRooms);
        adapter.setRoomItemClickListener(this::openRoomDetail);
        rvRooms.setAdapter(adapter);

        btnAddRoom.setVisibility(canCreateRooms ? View.VISIBLE : View.GONE);
        btnAddRoom.setEnabled(canCreateRooms);
        btnAddRoom.setOnClickListener(v -> {
            try {
                showCreateOrEditDialog(null);
            } catch (Exception e) {
                Toast.makeText(RoomsActivity.this, "Không mở được form thêm phòng: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        swipeRefresh.setOnRefreshListener(() -> loadRooms(false));
        loadRooms(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        NetworkClient.setAuthToken(sessionManager.getToken());
    }

    @Override
    protected void onPause() {
        super.onPause();
        dismissRoomDialog();
    }

    @Override
    protected void onDestroy() {
        dismissRoomDialog();
        super.onDestroy();
    }

    private void loadRooms(boolean firstLoad) {
        if (firstLoad) {
            progressBar.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.GONE);
        }

        apiService.getPhongs().enqueue(new Callback<List<Phong>>() {
            @Override
            public void onResponse(Call<List<Phong>> call, Response<List<Phong>> response) {
                progressBar.setVisibility(View.GONE);
                swipeRefresh.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null) {
                    handleRoleFilter(response.body());
                } else {
                    tvEmpty.setVisibility(View.VISIBLE);
                    int code = response.code();
                    tvEmpty.setText("Không tải được danh sách phòng (" + code + ")");
                    if (code == 401) {
                        Toast.makeText(RoomsActivity.this, "Phiên đăng nhập hết hạn, vui lòng đăng nhập lại", Toast.LENGTH_LONG).show();
                        goLogin();
                        return;
                    }
                    Toast.makeText(RoomsActivity.this, "Lỗi khi lấy danh sách phòng: HTTP " + code, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Phong>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                swipeRefresh.setRefreshing(false);
                tvEmpty.setVisibility(View.VISIBLE);
                tvEmpty.setText("Không có kết nối mạng");
                Toast.makeText(RoomsActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleRoleFilter(List<Phong> allRooms) {
        if (isTenantOnly) {
            filterForTenant(allRooms);
            return;
        }
        applyRooms(allRooms);
    }

    private void filterForTenant(List<Phong> allRooms) {
        apiService.getTenants().enqueue(new Callback<List<Tenant>>() {
            @Override
            public void onResponse(Call<List<Tenant>> call, Response<List<Tenant>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Long roomId = null;
                    for (Tenant tenant : response.body()) {
                        if (tenant.getTaiKhoanId() != null && tenant.getTaiKhoanId().equals(accountId)) {
                            roomId = tenant.getSophong();
                            break;
                        }
                    }
                    List<Phong> filtered = new ArrayList<>();
                    for (Phong room : allRooms) {
                        if (roomId != null && room.getId() != null && room.getId().equals(roomId)) {
                            filtered.add(room);
                        }
                    }
                    applyRooms(filtered);
                } else {
                    applyRooms(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(Call<List<Tenant>> call, Throwable t) {
                applyRooms(new ArrayList<>());
            }
        });
    }

    private void applyRooms(List<Phong> rooms) {
        adapter.setRooms(rooms);
        boolean isEmpty = rooms == null || rooms.isEmpty();
        tvEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        if (isEmpty) {
            tvEmpty.setText("Không có phòng phù hợp quyền truy cập");
        }
    }

    @Override
    public void onEditRoom(Phong room) {
        try {
            showCreateOrEditDialog(room);
        } catch (Exception e) {
            Toast.makeText(this, "Không mở được form sửa phòng: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onDeleteRoom(Phong room) {
        if (!canDeleteRooms) {
            Toast.makeText(this, "Bạn không có quyền xóa phòng", Toast.LENGTH_SHORT).show();
            return;
        }
        if (room == null || room.getId() == null) return;
        new AlertDialog.Builder(this)
                .setTitle("Xóa phòng")
                .setMessage("Xóa phòng " + safe(room.getMaPhong()) + "?")
                .setPositiveButton("Xóa", (dialog, which) -> doDelete(room.getId()))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void doDelete(Long id) {
        apiService.deletePhong(id).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(RoomsActivity.this, "Đã xóa phòng", Toast.LENGTH_SHORT).show();
                    loadRooms(false);
                } else {
                    Toast.makeText(RoomsActivity.this, "Xóa thất bại: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(RoomsActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showCreateOrEditDialog(Phong editing) {
        apiService.getBuildings().enqueue(new Callback<List<ToaNha>>() {
            @Override
            public void onResponse(Call<List<ToaNha>> call, Response<List<ToaNha>> response) {
                List<ToaNha> buildings = response.isSuccessful() && response.body() != null
                        ? response.body() : new ArrayList<>();
                showCreateOrEditDialogInternal(editing, buildings);
            }

            @Override
            public void onFailure(Call<List<ToaNha>> call, Throwable t) {
                showCreateOrEditDialogInternal(editing, new ArrayList<>());
            }
        });
    }

    private void showCreateOrEditDialogInternal(Phong editing, List<ToaNha> buildings) {
        if (isFinishing() || isDestroyed()) {
            Toast.makeText(this, "Màn hình đã đóng, không thể mở form", Toast.LENGTH_SHORT).show();
            return;
        }
        dismissRoomDialog();
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_room_form, null, false);
        EditText etMaPhong = view.findViewById(R.id.etRoomCode);
        AutoCompleteTextView etBuildingId = view.findViewById(R.id.etRoomBuildingId);
        EditText etBeds = view.findViewById(R.id.etRoomBeds);
        EditText etStatus = view.findViewById(R.id.etRoomStatus);
        EditText etDesc = view.findViewById(R.id.etRoomDesc);

        List<IdLabelOption> buildingOptions = new ArrayList<>();
        for (ToaNha building : buildings) {
            if (building == null || building.getId() == null) continue;
            String label = safe(building.getTen()) + " | " + safe(building.getDiaChi());
            buildingOptions.add(new IdLabelOption(building.getId(), label));
        }
        SelectionHelper.bindOptions(etBuildingId, buildingOptions);

        if (editing != null) {
            etMaPhong.setText(safeEditable(editing.getMaPhong()));
            String buildingLabel = SelectionHelper.findLabelById(buildingOptions, editing.getToaNhaId());
            if (buildingLabel.isEmpty() && editing.getToaNhaId() != null) {
                buildingLabel = "ID " + editing.getToaNhaId();
            }
            etBuildingId.setText(buildingLabel, false);
            etBeds.setText(editing.getSoGiuong() == null ? "" : String.valueOf(editing.getSoGiuong()));
            etStatus.setText(safeEditable(editing.getTrangThai()));
            etDesc.setText(safeEditable(editing.getMoTa()));
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(editing == null ? "Thêm phòng" : "Sửa phòng")
                .setView(view)
                .setPositiveButton(editing == null ? "Thêm" : "Lưu", null)
                .setNegativeButton("Hủy", null)
                .create();
        roomDialog = dialog;
        dialog.setOnDismissListener(d -> {
            if (roomDialog == dialog) {
                roomDialog = null;
            }
        });

        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String maPhong = etMaPhong.getText().toString().trim();
            String buildingRaw = etBuildingId.getText().toString().trim();
            String bedsRaw = etBeds.getText().toString().trim();
            String status = etStatus.getText().toString().trim();
            String desc = etDesc.getText().toString().trim();

            if (maPhong.isEmpty()) {
                etMaPhong.setError("Mã phòng bắt buộc");
                return;
            }
            if (buildingRaw.isEmpty()) {
                etBuildingId.setError("Tòa nhà bắt buộc");
                return;
            }
            if (bedsRaw.isEmpty()) {
                etBeds.setError("Số giường bắt buộc");
                return;
            }

            Long selectedBuildingId = SelectionHelper.findIdByText(buildingOptions, buildingRaw);
            if (selectedBuildingId == null) {
                etBuildingId.setError("Vui lòng chọn tòa nhà từ danh sách");
                return;
            }

            Phong payload = new Phong();
            payload.setMaPhong(maPhong);
            payload.setTrangThai(status.isEmpty() ? (editing != null ? safeEditable(editing.getTrangThai()) : "TRONG") : status);
            payload.setMoTa(desc);
            payload.setToaNhaId(selectedBuildingId);
            try {
                int soGiuong = Integer.parseInt(bedsRaw);
                if (soGiuong < 1) {
                    etBeds.setError("Số giường phải >= 1");
                    return;
                }
                payload.setSoGiuong(soGiuong);
            } catch (NumberFormatException e) {
                etBeds.setError("Số giường không hợp lệ");
                return;
            }

            Call<Phong> call = editing == null
                    ? apiService.createPhong(payload)
                    : apiService.updatePhong(editing.getId(), payload);
            call.enqueue(new Callback<Phong>() {
                @Override
                public void onResponse(Call<Phong> call, Response<Phong> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(RoomsActivity.this, editing == null ? "Đã thêm phòng" : "Đã cập nhật phòng", Toast.LENGTH_SHORT).show();
                        dismissRoomDialog();
                        loadRooms(false);
                    } else {
                        Toast.makeText(RoomsActivity.this, "Lưu thất bại: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Phong> call, Throwable t) {
                    Toast.makeText(RoomsActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }));

        if (isFinishing() || isDestroyed()) {
            return;
        }
        dialog.show();
    }

    private String safe(String value) {
        return value == null || value.trim().isEmpty() ? "N/A" : value;
    }

    private String safeEditable(String value) {
        return value == null ? "" : value;
    }

    private void openRoomDetail(Phong room) {
        if (room == null || room.getId() == null) return;
        android.content.Intent intent = new android.content.Intent(this, RoomDetailActivity.class);
        intent.putExtra("ROOM_ID", room.getId());
        startActivity(intent);
    }

    private void goLogin() {
        sessionManager.clear();
        NetworkClient.setAuthToken(null);
        startActivity(new android.content.Intent(this, LoginActivity.class)
                .addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                        | android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK));
        finish();
    }

    private void dismissRoomDialog() {
        if (roomDialog != null) {
            try {
                if (roomDialog.isShowing()) {
                    roomDialog.dismiss();
                }
            } catch (Exception ignored) {
            } finally {
                roomDialog = null;
            }
        }
    }
}
