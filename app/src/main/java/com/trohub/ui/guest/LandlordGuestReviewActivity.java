package com.trohub.ui.guest;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
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
import com.trohub.ui.api.models.GuestEntry;
import com.trohub.ui.api.models.GuestReviewRequest;
import com.trohub.ui.api.models.Phong;
import com.trohub.ui.auth.SessionManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LandlordGuestReviewActivity extends TroHubActivity implements LandlordGuestReviewAdapter.ReviewActionListener {

    private SwipeRefreshLayout swipeRefresh;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private RecyclerView rvItems;

    private ApiService apiService;
    private LandlordGuestReviewAdapter adapter;
    private final Map<Long, String> roomCodeById = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landlord_guest_review);

        SessionManager session = new SessionManager(this);
        if (!session.hasAnyRole("ROLE_ADMIN", "ROLE_LANDLORD")) {
            Toast.makeText(this, "Bạn không có quyền duyệt yêu cầu khách ra/vào", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        NetworkClient.setAuthToken(session.getToken());
        apiService = NetworkClient.getRetrofitClient().create(ApiService.class);

        swipeRefresh = findViewById(R.id.swipeRefresh);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);
        rvItems = findViewById(R.id.rvItems);

        adapter = new LandlordGuestReviewAdapter(this, roomCodeById);
        rvItems.setLayoutManager(new LinearLayoutManager(this));
        rvItems.setAdapter(adapter);

        swipeRefresh.setOnRefreshListener(() -> loadData(false));
        loadData(true);
    }

    private void loadData(boolean firstLoad) {
        if (firstLoad) {
            progressBar.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.GONE);
        }
        apiService.getPhongs().enqueue(new Callback<List<Phong>>() {
            @Override
            public void onResponse(Call<List<Phong>> call, Response<List<Phong>> response) {
                roomCodeById.clear();
                if (response.isSuccessful() && response.body() != null) {
                    for (Phong room : response.body()) {
                        if (room == null || room.getId() == null) continue;
                        roomCodeById.put(room.getId(), room.getMaPhong() == null ? "Chưa có tên phòng" : room.getMaPhong());
                    }
                }
                loadReviewItems();
            }

            @Override
            public void onFailure(Call<List<Phong>> call, Throwable t) {
                loadReviewItems();
            }
        });
    }

    private void loadReviewItems() {
        apiService.getGuestEntriesForReview().enqueue(new Callback<List<GuestEntry>>() {
            @Override
            public void onResponse(Call<List<GuestEntry>> call, Response<List<GuestEntry>> response) {
                progressBar.setVisibility(View.GONE);
                swipeRefresh.setRefreshing(false);
                List<GuestEntry> items = response.isSuccessful() && response.body() != null ? response.body() : new ArrayList<>();
                adapter.setItems(items);
                tvEmpty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onFailure(Call<List<GuestEntry>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                swipeRefresh.setRefreshing(false);
                tvEmpty.setVisibility(View.VISIBLE);
                tvEmpty.setText("Không tải được danh sách yêu cầu");
                adapter.setItems(new ArrayList<>());
            }
        });
    }

    @Override
    public void onApprove(GuestEntry item) {
        if (item == null || item.getId() == null) return;
        apiService.approveGuestEntry(item.getId()).enqueue(new ActionCallback("Đã chấp thuận yêu cầu"));
    }

    @Override
    public void onRequestInfo(GuestEntry item) {
        if (item == null || item.getId() == null) return;
        EditText etNote = new EditText(this);
        etNote.setHint("Ví dụ: Vui lòng gửi CCCD 2 mặt và số điện thoại");

        new AlertDialog.Builder(this)
                .setTitle("Yêu cầu bổ sung thông tin")
                .setView(etNote)
                .setPositiveButton("Gửi yêu cầu", (dialog, which) -> {
                    String note = etNote.getText() == null ? "" : etNote.getText().toString().trim();
                    apiService.requestGuestInfo(item.getId(), new GuestReviewRequest(note))
                            .enqueue(new ActionCallback("Đã gửi yêu cầu bổ sung"));
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    public void onReject(GuestEntry item) {
        if (item == null || item.getId() == null) return;
        apiService.rejectGuestEntry(item.getId()).enqueue(new ActionCallback("Đã từ chối yêu cầu"));
    }

    private class ActionCallback implements Callback<GuestEntry> {
        private final String successMessage;

        ActionCallback(String successMessage) {
            this.successMessage = successMessage;
        }

        @Override
        public void onResponse(Call<GuestEntry> call, Response<GuestEntry> response) {
            if (response.isSuccessful()) {
                Toast.makeText(LandlordGuestReviewActivity.this, successMessage, Toast.LENGTH_SHORT).show();
                loadData(false);
            } else {
                Toast.makeText(LandlordGuestReviewActivity.this, "Thao tác thất bại: " + response.code(), Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onFailure(Call<GuestEntry> call, Throwable t) {
            Toast.makeText(LandlordGuestReviewActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
