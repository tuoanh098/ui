package com.trohub.ui.buildings;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.trohub.ui.common.TroHubActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.trohub.ui.R;
import com.trohub.ui.api.ApiService;
import com.trohub.ui.api.NetworkClient;
import com.trohub.ui.api.models.Phong;
import com.trohub.ui.rooms.detail.RoomDetailActivity;
import com.trohub.ui.rooms.RoomsAdapter; // Re-use RoomAdapter!

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BuildingRoomsActivity extends TroHubActivity {

    private RecyclerView rvRooms;
    private RoomsAdapter adapter;
    private Long buildingId;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private SwipeRefreshLayout swipeRefresh;
    private String buildingName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rooms);

        TextView tvTitle = findViewById(R.id.tvTitle);
        rvRooms = findViewById(R.id.rvRooms);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);
        swipeRefresh = findViewById(R.id.swipeRefresh);

        buildingId = getIntent().getLongExtra("BUILDING_ID", -1);
        buildingName = getIntent().getStringExtra("BUILDING_NAME");

        if (buildingId == -1) {
            Toast.makeText(this, "Không có thông tin tòa nhà", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        tvTitle.setText("Phòng của tòa: " + (buildingName != null ? buildingName : "N/A"));

        rvRooms.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RoomsAdapter();
        Map<Long, String> buildingLabels = new HashMap<>();
        buildingLabels.put(buildingId, buildingName != null ? buildingName : "N/A");
        adapter.setBuildingLabels(buildingLabels);
        adapter.setRoomItemClickListener(this::openRoomDetail);
        rvRooms.setAdapter(adapter);
        swipeRefresh.setOnRefreshListener(() -> loadRooms(false));

        loadRooms(true);
    }

    private void loadRooms(boolean firstLoad) {
        if (firstLoad) {
            progressBar.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.GONE);
        }

        ApiService apiService = NetworkClient.getRetrofitClient().create(ApiService.class);
        Call<List<Phong>> call = apiService.getPhongs();

        call.enqueue(new Callback<List<Phong>>() {
            @Override
            public void onResponse(Call<List<Phong>> call, Response<List<Phong>> response) {
                progressBar.setVisibility(View.GONE);
                swipeRefresh.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null) {
                    List<Phong> allRooms = response.body();
                    List<Phong> buildingRooms = new ArrayList<>();

                    for (Phong room : allRooms) {
                        if (room.getToaNhaId() != null && room.getToaNhaId().equals(buildingId)) {
                            buildingRooms.add(room);
                        }
                    }
                    adapter.setRooms(buildingRooms);
                    boolean isEmpty = buildingRooms.isEmpty();
                    tvEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
                } else {
                    tvEmpty.setVisibility(View.VISIBLE);
                    tvEmpty.setText("Không tải được danh sách phòng");
                    Toast.makeText(BuildingRoomsActivity.this, "Lỗi lấy danh sách phòng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Phong>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                swipeRefresh.setRefreshing(false);
                tvEmpty.setVisibility(View.VISIBLE);
                tvEmpty.setText("Không có kết nối mạng");
                Toast.makeText(BuildingRoomsActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openRoomDetail(Phong room) {
        if (room == null || room.getId() == null) return;
        android.content.Intent intent = new android.content.Intent(this, RoomDetailActivity.class);
        intent.putExtra("ROOM_ID", room.getId());
        startActivity(intent);
    }
}

