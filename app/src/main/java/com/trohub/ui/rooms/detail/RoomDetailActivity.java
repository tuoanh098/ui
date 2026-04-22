package com.trohub.ui.rooms.detail;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
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
import com.trohub.ui.api.models.Phong;
import com.trohub.ui.api.models.Tenant;
import com.trohub.ui.api.models.ToaNha;
import com.trohub.ui.auth.SessionManager;
import com.trohub.ui.buildings.BuildingDetailActivity;
import com.trohub.ui.tenants.TenantsAdapter;
import com.trohub.ui.tenants.detail.TenantDetailActivity;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RoomDetailActivity extends AppCompatActivity {

    private TextView tvTitle;
    private TextView tvRoomCode;
    private TextView tvRoomStatus;
    private TextView tvRoomBeds;
    private TextView tvRoomDesc;
    private TextView tvBuildingLink;
    private TextView tvOwnerInfo;
    private TextView tvTenantCount;
    private TextView tvTenantEmpty;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView rvTenants;

    private ApiService apiService;
    private TenantsAdapter tenantsAdapter;
    private boolean tenantMode;

    private Long roomId;
    private Phong currentRoom;
    private ToaNha currentBuilding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_detail);

        SessionManager sessionManager = new SessionManager(this);
        NetworkClient.setAuthToken(sessionManager.getToken());
        tenantMode = sessionManager.hasAnyRole("ROLE_USER") && !sessionManager.hasAnyRole("ROLE_ADMIN", "ROLE_LANDLORD", "ROLE_BILLING_STAFF");
        apiService = NetworkClient.getRetrofitClient().create(ApiService.class);

        roomId = getIntent().getLongExtra("ROOM_ID", -1L);
        if (roomId == null || roomId <= 0) {
            Toast.makeText(this, "Thiếu ROOM_ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        bindViews();
        setupRecycler();
        setupActions();
        loadRoomDetail(true);
    }

    private void bindViews() {
        tvTitle = findViewById(R.id.tvTitle);
        tvRoomCode = findViewById(R.id.tvRoomCode);
        tvRoomStatus = findViewById(R.id.tvRoomStatus);
        tvRoomBeds = findViewById(R.id.tvRoomBeds);
        tvRoomDesc = findViewById(R.id.tvRoomDesc);
        tvBuildingLink = findViewById(R.id.tvBuildingLink);
        tvOwnerInfo = findViewById(R.id.tvOwnerInfo);
        tvTenantCount = findViewById(R.id.tvTenantCount);
        tvTenantEmpty = findViewById(R.id.tvTenantEmpty);
        progressBar = findViewById(R.id.progressBar);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        rvTenants = findViewById(R.id.rvTenants);

        tvTitle.setText("Chi tiết phòng");
        if (!tenantMode) {
            tvBuildingLink.setPaintFlags(tvBuildingLink.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        }
    }

    private void setupRecycler() {
        tenantsAdapter = new TenantsAdapter();
        if (!tenantMode) {
            tenantsAdapter.setTenantItemClickListener(this::openTenantDetail);
        }
        rvTenants.setLayoutManager(new LinearLayoutManager(this));
        rvTenants.setAdapter(tenantsAdapter);
    }

    private void setupActions() {
        swipeRefresh.setOnRefreshListener(() -> loadRoomDetail(false));
        tvBuildingLink.setOnClickListener(v -> {
            if (!tenantMode) openBuildingDetail();
        });
    }

    private void loadRoomDetail(boolean firstLoad) {
        if (firstLoad) {
            progressBar.setVisibility(View.VISIBLE);
        }
        apiService.getPhongs().enqueue(new Callback<List<Phong>>() {
            @Override
            public void onResponse(Call<List<Phong>> call, Response<List<Phong>> response) {
                progressBar.setVisibility(View.GONE);
                swipeRefresh.setRefreshing(false);
                if (!(response.isSuccessful() && response.body() != null)) {
                    Toast.makeText(RoomDetailActivity.this, "Không tải được thông tin phòng", Toast.LENGTH_SHORT).show();
                    return;
                }

                currentRoom = null;
                for (Phong room : response.body()) {
                    if (room.getId() != null && room.getId().equals(roomId)) {
                        currentRoom = room;
                        break;
                    }
                }

                if (currentRoom == null) {
                    Toast.makeText(RoomDetailActivity.this, "Không tìm thấy phòng", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                renderRoomInfo();
                loadBuildingAndOwner();
                loadTenantsInRoom();
            }

            @Override
            public void onFailure(Call<List<Phong>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                swipeRefresh.setRefreshing(false);
                Toast.makeText(RoomDetailActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void renderRoomInfo() {
        tvRoomCode.setText("Mã phòng: " + safe(currentRoom.getMaPhong()) + " (ID " + currentRoom.getId() + ")");
        tvRoomStatus.setText("Trạng thái: " + safe(currentRoom.getTrangThai()));
        tvRoomBeds.setText("Số giường: " + (currentRoom.getSoGiuong() == null ? "N/A" : currentRoom.getSoGiuong()));
        tvRoomDesc.setText("Mô tả: " + safe(currentRoom.getMoTa()));
        tvBuildingLink.setText("Tòa nhà: đang tải...");
        tvOwnerInfo.setText("Chủ trọ: đang tải...");
    }

    private void loadBuildingAndOwner() {
        if (currentRoom.getToaNhaId() == null) {
            tvBuildingLink.setText("Tòa nhà: N/A");
            tvOwnerInfo.setText("Chủ trọ: N/A");
            return;
        }

        apiService.getBuildings().enqueue(new Callback<List<ToaNha>>() {
            @Override
            public void onResponse(Call<List<ToaNha>> call, Response<List<ToaNha>> response) {
                if (!(response.isSuccessful() && response.body() != null)) {
                    tvBuildingLink.setText("Tòa nhà: không tải được");
                    tvOwnerInfo.setText("Chủ trọ: không tải được");
                    return;
                }

                currentBuilding = null;
                for (ToaNha b : response.body()) {
                    if (b.getId() != null && b.getId().equals(currentRoom.getToaNhaId())) {
                        currentBuilding = b;
                        break;
                    }
                }

                if (currentBuilding == null) {
                    tvBuildingLink.setText("Tòa nhà: N/A");
                    tvOwnerInfo.setText("Chủ trọ: N/A");
                    return;
                }

                tvBuildingLink.setText("Tòa nhà: " + safe(currentBuilding.getTen()) + " (ID " + currentBuilding.getId() + ")");
                tvBuildingLink.setEnabled(!tenantMode);
                loadOwnerInfo(currentBuilding.getChuTroId());
            }

            @Override
            public void onFailure(Call<List<ToaNha>> call, Throwable t) {
                tvBuildingLink.setText("Tòa nhà: không tải được");
                tvOwnerInfo.setText("Chủ trọ: không tải được");
            }
        });
    }

    private void loadOwnerInfo(Long ownerId) {
        if (ownerId == null) {
            tvOwnerInfo.setText("Chủ trọ: N/A");
            return;
        }

        apiService.getLandlords().enqueue(new Callback<List<Landlord>>() {
            @Override
            public void onResponse(Call<List<Landlord>> call, Response<List<Landlord>> response) {
                if (!(response.isSuccessful() && response.body() != null)) {
                    tvOwnerInfo.setText("Chủ trọ: không tải được");
                    return;
                }
                for (Landlord landlord : response.body()) {
                    if (landlord.getId() != null && landlord.getId().equals(ownerId)) {
                        tvOwnerInfo.setText("Chủ trọ: " + safe(landlord.getTen()) + " | SĐT: " + safe(landlord.getSdt()));
                        return;
                    }
                }
                tvOwnerInfo.setText("Chủ trọ: N/A");
            }

            @Override
            public void onFailure(Call<List<Landlord>> call, Throwable t) {
                tvOwnerInfo.setText("Chủ trọ: không tải được");
            }
        });
    }

    private void loadTenantsInRoom() {
        apiService.getTenants().enqueue(new Callback<List<Tenant>>() {
            @Override
            public void onResponse(Call<List<Tenant>> call, Response<List<Tenant>> response) {
                if (!(response.isSuccessful() && response.body() != null)) {
                    tvTenantCount.setText("Khách thuê: N/A");
                    tvTenantEmpty.setVisibility(View.VISIBLE);
                    tvTenantEmpty.setText("Không tải được danh sách khách thuê");
                    tenantsAdapter.setTenants(new ArrayList<>());
                    return;
                }

                List<Tenant> tenants = new ArrayList<>();
                for (Tenant tenant : response.body()) {
                    if (tenant.getSophong() != null && tenant.getSophong().equals(roomId)) {
                        tenants.add(tenant);
                    }
                }
                tvTenantCount.setText("Khách thuê trong phòng: " + tenants.size());
                tvTenantEmpty.setVisibility(tenants.isEmpty() ? View.VISIBLE : View.GONE);
                tvTenantEmpty.setText("Phòng này chưa có khách thuê");
                tenantsAdapter.setTenants(tenants);
            }

            @Override
            public void onFailure(Call<List<Tenant>> call, Throwable t) {
                tvTenantCount.setText("Khách thuê: N/A");
                tvTenantEmpty.setVisibility(View.VISIBLE);
                tvTenantEmpty.setText("Lỗi kết nối khi tải khách thuê");
                tenantsAdapter.setTenants(new ArrayList<>());
            }
        });
    }

    private void openTenantDetail(Tenant tenant) {
        if (tenant == null || tenant.getId() == null) return;
        Intent intent = new Intent(this, TenantDetailActivity.class);
        intent.putExtra("TENANT_ID", tenant.getId());
        startActivity(intent);
    }

    private void openBuildingDetail() {
        if (currentBuilding == null || currentBuilding.getId() == null) return;
        Intent intent = new Intent(this, BuildingDetailActivity.class);
        intent.putExtra("BUILDING_ID", currentBuilding.getId());
        intent.putExtra("BUILDING_NAME", currentBuilding.getTen());
        intent.putExtra("BUILDING_ADDRESS", currentBuilding.getDiaChi());
        intent.putExtra("BUILDING_ROOM_COUNT", currentBuilding.getRoomCount());
        intent.putExtra("BUILDING_OCCUPIED_COUNT", currentBuilding.getOccupiedCount());
        intent.putExtra("BUILDING_MANAGER_ID", currentBuilding.getChuTroId());
        startActivity(intent);
    }

    private String safe(String value) {
        return value == null || value.trim().isEmpty() ? "N/A" : value;
    }
}
