package com.trohub.ui.incidents;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.trohub.ui.R;
import com.trohub.ui.api.ApiService;
import com.trohub.ui.api.NetworkClient;
import com.trohub.ui.api.models.Incident;
import com.trohub.ui.api.models.Phong;
import com.trohub.ui.api.models.Tenant;
import com.trohub.ui.api.models.ToaNha;
import com.trohub.ui.auth.SessionManager;
import com.trohub.ui.common.IdLabelOption;
import com.trohub.ui.common.SelectionHelper;

import java.util.ArrayList;
import java.util.List;
import java.net.URL;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class IncidentsActivity extends AppCompatActivity implements IncidentsAdapter.IncidentActionListener {

    private RecyclerView rvIncidents;
    private IncidentsAdapter adapter;
    private Button btnCreateIncident;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private SwipeRefreshLayout swipeRefresh;

    private ApiService apiService;
    private boolean canManageIncidents;
    private boolean canEditOwnIncidents;
    private boolean canCreateIncident;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incidents);

        rvIncidents = findViewById(R.id.rvIncidents);
        btnCreateIncident = findViewById(R.id.btnCreateIncident);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);
        swipeRefresh = findViewById(R.id.swipeRefresh);

        SessionManager sessionManager = new SessionManager(this);
        canManageIncidents = sessionManager.hasAnyRole("ROLE_ADMIN", "ROLE_LANDLORD", "ROLE_BILLING_STAFF");
        canEditOwnIncidents = sessionManager.hasAnyRole("ROLE_USER") || canManageIncidents;
        canCreateIncident = sessionManager.hasAnyRole("ROLE_USER", "ROLE_ADMIN", "ROLE_LANDLORD", "ROLE_BILLING_STAFF");
        apiService = NetworkClient.getRetrofitClient().create(ApiService.class);

        rvIncidents.setLayoutManager(new LinearLayoutManager(this));
        adapter = new IncidentsAdapter(this, canEditOwnIncidents, canManageIncidents);
        rvIncidents.setAdapter(adapter);
        swipeRefresh.setOnRefreshListener(() -> loadIncidents(false));

        btnCreateIncident.setVisibility(canCreateIncident ? View.VISIBLE : View.GONE);
        btnCreateIncident.setOnClickListener(v -> {
            Intent intent = new Intent(IncidentsActivity.this, CreateIncidentActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public void onViewIncident(Incident incident) {
        if (incident == null) return;
        ScrollView scrollView = new ScrollView(this);
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        int pad = dp(16);
        content.setPadding(pad, pad, pad, pad);
        scrollView.addView(content);

        StringBuilder sb = new StringBuilder();
        sb.append("Loại: ").append(safe(incident.getLoai())).append("\n");
        sb.append("Trạng thái: ").append(safe(incident.getStatus())).append("\n");
        sb.append("Phòng ID: ").append(incident.getPhongId() != null ? incident.getPhongId() : "N/A").append("\n");
        sb.append("Người báo ID: ").append(incident.getReportedBy() != null ? incident.getReportedBy() : "N/A").append("\n");
        sb.append("Thời gian báo: ").append(safe(incident.getReportedAt())).append("\n");
        if (incident.getResolvedAt() != null) {
            sb.append("Đã xử lý: ").append(incident.getResolvedAt()).append("\n");
        }
        sb.append("\nMô tả:\n").append(safe(incident.getMoTa()));

        TextView info = new TextView(this);
        info.setText(sb.toString());
        info.setTextSize(15);
        content.addView(info);

        if (incident.getImagePaths() != null && !incident.getImagePaths().isEmpty()) {
            TextView imageTitle = new TextView(this);
            imageTitle.setText("\nẢnh đính kèm:");
            imageTitle.setTypeface(null, android.graphics.Typeface.BOLD);
            content.addView(imageTitle);
            for (String path : incident.getImagePaths()) {
                addIncidentImage(content, path);
            }
        }

        new AlertDialog.Builder(this)
                .setTitle("Chi tiết sự cố #" + (incident.getId() != null ? incident.getId() : ""))
                .setView(scrollView)
                .setPositiveButton("Đóng", null)
                .show();
    }

    private void addIncidentImage(LinearLayout content, String rawPath) {
        String url = buildUploadUrl(rawPath);
        TextView label = new TextView(this);
        label.setText(url);
        label.setTextSize(12);
        label.setPadding(0, dp(8), 0, dp(4));
        content.addView(label);

        ImageView imageView = new ImageView(this);
        imageView.setAdjustViewBounds(true);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(180)
        );
        imageView.setLayoutParams(params);
        content.addView(imageView);

        new Thread(() -> {
            try {
                Bitmap bitmap = BitmapFactory.decodeStream(new URL(url).openStream());
                new Handler(Looper.getMainLooper()).post(() -> imageView.setImageBitmap(bitmap));
            } catch (Exception e) {
                new Handler(Looper.getMainLooper()).post(() -> label.setText(url + "\nKhông tải được ảnh"));
            }
        }).start();
    }

    private String buildUploadUrl(String rawPath) {
        if (rawPath == null || rawPath.trim().isEmpty()) return "";
        String path = rawPath.trim();
        if (path.startsWith("http://") || path.startsWith("https://")) return path;
        String base = NetworkClient.getBaseUrl();
        if (path.startsWith("/")) path = path.substring(1);
        return base + path;
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadIncidents(true);
    }

    private void loadIncidents(boolean firstLoad) {
        if (firstLoad) {
            progressBar.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.GONE);
        }

        apiService.getIncidents().enqueue(new Callback<List<Incident>>() {
            @Override
            public void onResponse(Call<List<Incident>> call, Response<List<Incident>> response) {
                progressBar.setVisibility(View.GONE);
                swipeRefresh.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null) {
                    adapter.setIncidents(response.body());
                    boolean isEmpty = response.body().isEmpty();
                    tvEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
                } else {
                    tvEmpty.setVisibility(View.VISIBLE);
                    tvEmpty.setText("Không tải được danh sách sự cố");
                    Toast.makeText(IncidentsActivity.this, "Lỗi khi lấy danh sách sự cố", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Incident>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                swipeRefresh.setRefreshing(false);
                tvEmpty.setVisibility(View.VISIBLE);
                tvEmpty.setText("Không có kết nối mạng");
                Toast.makeText(IncidentsActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onEditIncident(Incident incident) {
        showEditDialog(incident);
    }

    @Override
    public void onDeleteIncident(Incident incident) {
        if (incident == null || incident.getId() == null) return;
        new AlertDialog.Builder(this)
                .setTitle("Xóa sự cố")
                .setMessage("Xóa sự cố #" + incident.getId() + "?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    apiService.deleteIncident(incident.getId()).enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(IncidentsActivity.this, "Đã xóa sự cố", Toast.LENGTH_SHORT).show();
                                loadIncidents(false);
                            } else {
                                Toast.makeText(IncidentsActivity.this, "Xóa thất bại: " + response.code(), Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            Toast.makeText(IncidentsActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    public void onResolveIncident(Incident incident) {
        if (!canManageIncidents || incident == null || incident.getId() == null) return;
        apiService.resolveIncident(incident.getId()).enqueue(new Callback<Incident>() {
            @Override
            public void onResponse(Call<Incident> call, Response<Incident> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(IncidentsActivity.this, "Đã xác nhận xử lý sự cố", Toast.LENGTH_SHORT).show();
                    loadIncidents(false);
                } else {
                    Toast.makeText(IncidentsActivity.this, "Xác nhận thất bại: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Incident> call, Throwable t) {
                Toast.makeText(IncidentsActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showEditDialog(Incident incident) {
        apiService.getBuildings().enqueue(new Callback<List<ToaNha>>() {
            @Override
            public void onResponse(Call<List<ToaNha>> call, Response<List<ToaNha>> buildingResp) {
                List<ToaNha> buildings = buildingResp.isSuccessful() && buildingResp.body() != null
                        ? buildingResp.body() : new ArrayList<>();
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
                                showEditDialogInternal(incident, buildings, rooms, tenants);
                            }

                            @Override
                            public void onFailure(Call<List<Tenant>> call, Throwable t) {
                                showEditDialogInternal(incident, buildings, rooms, new ArrayList<>());
                            }
                        });
                    }

                    @Override
                    public void onFailure(Call<List<Phong>> call, Throwable t) {
                        showEditDialogInternal(incident, buildings, new ArrayList<>(), new ArrayList<>());
                    }
                });
            }

            @Override
            public void onFailure(Call<List<ToaNha>> call, Throwable t) {
                showEditDialogInternal(incident, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
            }
        });
    }

    private void showEditDialogInternal(Incident incident, List<ToaNha> buildings, List<Phong> rooms, List<Tenant> tenants) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_incident_form, null, false);
        EditText etLoai = view.findViewById(R.id.etIncidentLoai);
        EditText etMoTa = view.findViewById(R.id.etIncidentMoTa);
        AutoCompleteTextView etToa = view.findViewById(R.id.etIncidentToaId);
        AutoCompleteTextView etPhong = view.findViewById(R.id.etIncidentPhongId);
        AutoCompleteTextView etReportedBy = view.findViewById(R.id.etIncidentReportedBy);
        EditText etStatus = view.findViewById(R.id.etIncidentStatus);

        List<IdLabelOption> buildingOptions = new ArrayList<>();
        for (ToaNha building : buildings) {
            if (building == null || building.getId() == null) continue;
            buildingOptions.add(new IdLabelOption(building.getId(),
                    safe(building.getTen()) + " | " + safe(building.getDiaChi())));
        }
        SelectionHelper.bindOptions(etToa, buildingOptions);

        List<IdLabelOption> roomOptions = new ArrayList<>();
        for (Phong room : rooms) {
            if (room == null || room.getId() == null) continue;
            roomOptions.add(new IdLabelOption(room.getId(),
                    safe(room.getMaPhong()) + " | " + safe(room.getTrangThai())));
        }
        SelectionHelper.bindOptions(etPhong, roomOptions);

        List<IdLabelOption> reporterOptions = new ArrayList<>();
        for (Tenant tenant : tenants) {
            if (tenant == null || tenant.getId() == null) continue;
            reporterOptions.add(new IdLabelOption(tenant.getId(),
                    safe(tenant.getHoTen()) + " | CCCD: " + safe(tenant.getCccd())));
        }
        SelectionHelper.bindOptions(etReportedBy, reporterOptions);

        etLoai.setText(safeEditable(incident.getLoai()));
        etMoTa.setText(safeEditable(incident.getMoTa()));
        etToa.setText(SelectionHelper.findLabelById(buildingOptions, incident.getToaNhaId()), false);
        etPhong.setText(SelectionHelper.findLabelById(roomOptions, incident.getPhongId()), false);
        etReportedBy.setText(SelectionHelper.findLabelById(reporterOptions, incident.getReportedBy()), false);
        etStatus.setText(safeEditable(incident.getStatus()));

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Sửa sự cố")
                .setView(view)
                .setPositiveButton("Lưu", null)
                .setNegativeButton("Hủy", null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String loai = etLoai.getText().toString().trim();
            String moTa = etMoTa.getText().toString().trim();
            String toaRaw = etToa.getText().toString().trim();
            String phongRaw = etPhong.getText().toString().trim();
            String reporterRaw = etReportedBy.getText().toString().trim();
            String status = etStatus.getText().toString().trim();

            if (loai.isEmpty()) {
                etLoai.setError("Loại sự cố bắt buộc");
                return;
            }
            if (phongRaw.isEmpty()) {
                etPhong.setError("Phòng bắt buộc");
                return;
            }
            if (reporterRaw.isEmpty()) {
                etReportedBy.setError("Người báo bắt buộc");
                return;
            }
            Long selectedToaId = SelectionHelper.findIdByText(buildingOptions, toaRaw);
            Long selectedPhongId = SelectionHelper.findIdByText(roomOptions, phongRaw);
            Long selectedReporterId = SelectionHelper.findIdByText(reporterOptions, reporterRaw);
            if (!toaRaw.isEmpty() && selectedToaId == null) {
                etToa.setError("Vui lòng chọn tòa nhà từ danh sách");
                return;
            }
            if (selectedPhongId == null) {
                etPhong.setError("Vui lòng chọn phòng từ danh sách");
                return;
            }
            if (selectedReporterId == null) {
                etReportedBy.setError("Vui lòng chọn người báo từ danh sách");
                return;
            }

            Incident payload = new Incident();
            payload.setLoai(loai);
            payload.setMoTa(moTa);
            String defaultStatus = safeEditable(incident.getStatus());
            payload.setStatus(status.isEmpty() ? (defaultStatus.isEmpty() ? "PENDING" : defaultStatus) : status);
            payload.setToaNhaId(selectedToaId);
            payload.setPhongId(selectedPhongId);
            payload.setReportedBy(selectedReporterId);

            apiService.updateIncident(incident.getId(), payload).enqueue(new Callback<Incident>() {
                @Override
                public void onResponse(Call<Incident> call, Response<Incident> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(IncidentsActivity.this, "Đã cập nhật sự cố", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        loadIncidents(false);
                    } else {
                        Toast.makeText(IncidentsActivity.this, "Cập nhật thất bại: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Incident> call, Throwable t) {
                    Toast.makeText(IncidentsActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }));

        dialog.show();
    }

    private String safeEditable(String value) {
        return value == null ? "" : value;
    }

    private String safe(String value) {
        return value == null || value.trim().isEmpty() ? "N/A" : value;
    }
}
