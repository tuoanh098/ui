package com.trohub.ui.buildings;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
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
import com.trohub.ui.rooms.detail.RoomDetailActivity;
import com.trohub.ui.rooms.RoomsAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BuildingDetailActivity extends AppCompatActivity {

    private static final int PAGE_SIZE = 10;

    private TextView tvTitle;
    private TextView tvBuildingName;
    private TextView tvBuildingAddress;
    private TextView tvBuildingMeta;
    private TextView tvPage;
    private TextView tvEmpty;
    private EditText etSearch;
    private Button btnPrev;
    private Button btnNext;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView rvRooms;

    private RoomsAdapter roomsAdapter;
    private ApiService apiService;

    private Long buildingId;
    private String buildingName;
    private String buildingAddress;
    private long buildingRoomCount;
    private long buildingOccupiedCount;
    private long buildingManagerId;

    private final List<Phong> allRoomsInBuilding = new ArrayList<>();
    private final List<Phong> filteredRooms = new ArrayList<>();
    private int currentPage = 1;
    private int totalPages = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_building_detail);

        apiService = NetworkClient.getRetrofitClient().create(ApiService.class);
        bindViews();

        if (!readIntentData()) {
            Toast.makeText(this, "Thiếu dữ liệu tòa nhà", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        roomsAdapter = new RoomsAdapter();
        roomsAdapter.setRoomItemClickListener(this::openRoomDetail);
        rvRooms.setLayoutManager(new LinearLayoutManager(this));
        rvRooms.setAdapter(roomsAdapter);

        renderBuildingHeader();
        setupActions();
        loadRooms(true);
    }

    private void bindViews() {
        tvTitle = findViewById(R.id.tvTitle);
        tvBuildingName = findViewById(R.id.tvBuildingName);
        tvBuildingAddress = findViewById(R.id.tvBuildingAddress);
        tvBuildingMeta = findViewById(R.id.tvBuildingMeta);
        tvPage = findViewById(R.id.tvPage);
        tvEmpty = findViewById(R.id.tvEmpty);
        etSearch = findViewById(R.id.etSearchRoom);
        btnPrev = findViewById(R.id.btnPrevPage);
        btnNext = findViewById(R.id.btnNextPage);
        progressBar = findViewById(R.id.progressBar);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        rvRooms = findViewById(R.id.rvRooms);
    }

    private boolean readIntentData() {
        long id = getIntent().getLongExtra("BUILDING_ID", -1L);
        if (id <= 0) return false;
        buildingId = id;
        buildingName = getIntent().getStringExtra("BUILDING_NAME");
        buildingAddress = getIntent().getStringExtra("BUILDING_ADDRESS");
        buildingRoomCount = getIntent().getLongExtra("BUILDING_ROOM_COUNT", 0L);
        buildingOccupiedCount = getIntent().getLongExtra("BUILDING_OCCUPIED_COUNT", 0L);
        buildingManagerId = getIntent().getLongExtra("BUILDING_MANAGER_ID", -1L);
        return true;
    }

    private void renderBuildingHeader() {
        tvTitle.setText("Chi tiết tòa nhà");
        tvBuildingName.setText("Tên tòa: " + safe(buildingName));
        tvBuildingAddress.setText("Địa chỉ: " + safe(buildingAddress));

        String managerText = buildingManagerId > 0 ? String.valueOf(buildingManagerId) : "N/A";
        tvBuildingMeta.setText("ID: " + buildingId
                + " | Chủ trọ ID: " + managerText
                + " | Phòng: " + buildingRoomCount
                + " | Đang thuê: " + buildingOccupiedCount);
    }

    private void setupActions() {
        swipeRefresh.setOnRefreshListener(() -> loadRooms(false));

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                applyFilter(s == null ? "" : s.toString());
            }
        });

        btnPrev.setOnClickListener(v -> {
            if (currentPage > 1) {
                currentPage--;
                renderPage();
            }
        });

        btnNext.setOnClickListener(v -> {
            if (currentPage < totalPages) {
                currentPage++;
                renderPage();
            }
        });
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
                    allRoomsInBuilding.clear();
                    for (Phong phong : response.body()) {
                        if (phong.getToaNhaId() != null && phong.getToaNhaId().equals(buildingId)) {
                            allRoomsInBuilding.add(phong);
                        }
                    }
                    if (buildingRoomCount <= 0) {
                        buildingRoomCount = allRoomsInBuilding.size();
                        renderBuildingHeader();
                    }
                    applyFilter(etSearch.getText().toString());
                } else {
                    roomsAdapter.setRooms(new ArrayList<>());
                    tvEmpty.setVisibility(View.VISIBLE);
                    tvEmpty.setText("Không tải được danh sách phòng");
                    updatePageUi(0, 0, 0);
                    Toast.makeText(BuildingDetailActivity.this, "Lỗi lấy danh sách phòng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Phong>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                swipeRefresh.setRefreshing(false);
                roomsAdapter.setRooms(new ArrayList<>());
                tvEmpty.setVisibility(View.VISIBLE);
                tvEmpty.setText("Không có kết nối mạng");
                updatePageUi(0, 0, 0);
                Toast.makeText(BuildingDetailActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void applyFilter(String rawQuery) {
        String query = rawQuery == null ? "" : rawQuery.trim().toLowerCase(Locale.US);
        filteredRooms.clear();

        if (query.isEmpty()) {
            filteredRooms.addAll(allRoomsInBuilding);
        } else {
            for (Phong room : allRoomsInBuilding) {
                String maPhong = safeLower(room.getMaPhong());
                String roomId = room.getId() == null ? "" : String.valueOf(room.getId());
                String moTa = safeLower(room.getMoTa());
                if (maPhong.contains(query) || roomId.contains(query) || moTa.contains(query)) {
                    filteredRooms.add(room);
                }
            }
        }

        currentPage = 1;
        renderPage();
    }

    private void renderPage() {
        int total = filteredRooms.size();
        if (total == 0) {
            roomsAdapter.setRooms(new ArrayList<>());
            tvEmpty.setVisibility(View.VISIBLE);
            tvEmpty.setText("Không có phòng phù hợp");
            updatePageUi(0, 0, 0);
            return;
        }

        tvEmpty.setVisibility(View.GONE);
        totalPages = (int) Math.ceil((double) total / PAGE_SIZE);
        if (currentPage < 1) currentPage = 1;
        if (currentPage > totalPages) currentPage = totalPages;

        int fromIndex = (currentPage - 1) * PAGE_SIZE;
        int toIndex = Math.min(fromIndex + PAGE_SIZE, total);
        List<Phong> currentPageList = new ArrayList<>(filteredRooms.subList(fromIndex, toIndex));
        roomsAdapter.setRooms(currentPageList);
        updatePageUi(currentPage, totalPages, total);
    }

    private void updatePageUi(int page, int maxPage, int totalItems) {
        if (maxPage <= 0) {
            tvPage.setText("Trang 0/0 | 0 phòng");
            btnPrev.setEnabled(false);
            btnNext.setEnabled(false);
            return;
        }
        tvPage.setText("Trang " + page + "/" + maxPage + " | " + totalItems + " phòng");
        btnPrev.setEnabled(page > 1);
        btnNext.setEnabled(page < maxPage);
    }

    private String safe(String value) {
        return value == null || value.trim().isEmpty() ? "N/A" : value;
    }

    private String safeLower(String value) {
        return value == null ? "" : value.toLowerCase(Locale.US);
    }

    private void openRoomDetail(Phong room) {
        if (room == null || room.getId() == null) return;
        android.content.Intent intent = new android.content.Intent(this, RoomDetailActivity.class);
        intent.putExtra("ROOM_ID", room.getId());
        startActivity(intent);
    }
}
