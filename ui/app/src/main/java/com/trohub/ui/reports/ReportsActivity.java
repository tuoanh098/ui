package com.trohub.ui.reports;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.trohub.ui.R;
import com.trohub.ui.api.ApiService;
import com.trohub.ui.api.NetworkClient;
import com.trohub.ui.api.models.Contract;
import com.trohub.ui.api.models.Invoice;
import com.trohub.ui.api.models.Phong;
import com.trohub.ui.api.models.Tenant;
import com.trohub.ui.api.models.ToaNha;
import com.trohub.ui.auth.SessionManager;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReportsActivity extends AppCompatActivity {
    private Spinner spBuilding;
    private EditText etYear;
    private EditText etMonth;
    private EditText etRoomKeyword;
    private CheckBox cbOverdueOnly;
    private Button btnApply;
    private ProgressBar progressBar;
    private TextView tvSummaryRevenue;
    private TextView tvSummaryCounts;
    private TextView tvEmpty;
    private RecyclerView rvReport;

    private ApiService apiService;
    private SessionManager sessionManager;
    private RoomRevenueAdapter adapter;

    private final List<ToaNha> availableBuildings = new ArrayList<>();
    private final List<Phong> allRooms = new ArrayList<>();
    private final List<Tenant> allTenants = new ArrayList<>();
    private final List<Contract> allContracts = new ArrayList<>();
    private final List<Long> spinnerBuildingIds = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);

        sessionManager = new SessionManager(this);
        NetworkClient.setAuthToken(sessionManager.getToken());
        apiService = NetworkClient.getRetrofitClient().create(ApiService.class);

        bindViews();
        setupDefaults();

        adapter = new RoomRevenueAdapter();
        rvReport.setLayoutManager(new LinearLayoutManager(this));
        rvReport.setAdapter(adapter);

        btnApply.setOnClickListener(v -> loadInvoicesAndRender());
        loadBaseReferenceData();
    }

    private void bindViews() {
        spBuilding = findViewById(R.id.spBuildingFilter);
        etYear = findViewById(R.id.etReportYear);
        etMonth = findViewById(R.id.etReportMonth);
        etRoomKeyword = findViewById(R.id.etRoomKeyword);
        cbOverdueOnly = findViewById(R.id.cbOverdueOnly);
        btnApply = findViewById(R.id.btnApplyFilter);
        progressBar = findViewById(R.id.progressBar);
        tvSummaryRevenue = findViewById(R.id.tvSummaryRevenue);
        tvSummaryCounts = findViewById(R.id.tvSummaryCounts);
        tvEmpty = findViewById(R.id.tvEmpty);
        rvReport = findViewById(R.id.rvReportRooms);
    }

    private void setupDefaults() {
        Calendar c = Calendar.getInstance();
        etYear.setText(String.valueOf(c.get(Calendar.YEAR)));
        etMonth.setText(String.valueOf(c.get(Calendar.MONTH) + 1));
    }

    private void loadBaseReferenceData() {
        showLoading(true);
        apiService.getBuildings().enqueue(new Callback<List<ToaNha>>() {
            @Override
            public void onResponse(Call<List<ToaNha>> call, Response<List<ToaNha>> response) {
                if (!(response.isSuccessful() && response.body() != null)) {
                    failLoad("Không tải được danh sách tòa nhà");
                    return;
                }
                availableBuildings.clear();
                availableBuildings.addAll(response.body());
                loadRoomsTenantsContracts();
            }

            @Override
            public void onFailure(Call<List<ToaNha>> call, Throwable t) {
                failLoad("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    private void loadRoomsTenantsContracts() {
        apiService.getPhongs().enqueue(new Callback<List<Phong>>() {
            @Override
            public void onResponse(Call<List<Phong>> call, Response<List<Phong>> response) {
                if (!(response.isSuccessful() && response.body() != null)) {
                    failLoad("Không tải được danh sách phòng");
                    return;
                }

                Set<Long> buildingIds = new HashSet<>();
                for (ToaNha b : availableBuildings) buildingIds.add(b.getId());

                allRooms.clear();
                for (Phong room : response.body()) {
                    if (room.getToaNhaId() != null && buildingIds.contains(room.getToaNhaId())) {
                        allRooms.add(room);
                    }
                }

                apiService.getTenants().enqueue(new Callback<List<Tenant>>() {
                    @Override
                    public void onResponse(Call<List<Tenant>> call, Response<List<Tenant>> response) {
                        allTenants.clear();
                        if (response.isSuccessful() && response.body() != null) {
                            allTenants.addAll(response.body());
                        }
                        apiService.getContracts().enqueue(new Callback<List<Contract>>() {
                            @Override
                            public void onResponse(Call<List<Contract>> call, Response<List<Contract>> response) {
                                allContracts.clear();
                                if (response.isSuccessful() && response.body() != null) {
                                    allContracts.addAll(response.body());
                                }
                                bindBuildingSpinner();
                                loadInvoicesAndRender();
                            }

                            @Override
                            public void onFailure(Call<List<Contract>> call, Throwable t) {
                                bindBuildingSpinner();
                                loadInvoicesAndRender();
                            }
                        });
                    }

                    @Override
                    public void onFailure(Call<List<Tenant>> call, Throwable t) {
                        allTenants.clear();
                        bindBuildingSpinner();
                        loadInvoicesAndRender();
                    }
                });
            }

            @Override
            public void onFailure(Call<List<Phong>> call, Throwable t) {
                failLoad("Lỗi tải phòng: " + t.getMessage());
            }
        });
    }

    private void bindBuildingSpinner() {
        List<String> labels = new ArrayList<>();
        spinnerBuildingIds.clear();

        labels.add("Tất cả tòa");
        spinnerBuildingIds.add(null);

        for (ToaNha b : availableBuildings) {
            labels.add(safe(b.getTen()) + " (ID " + b.getId() + ")");
            spinnerBuildingIds.add(b.getId());
        }

        ArrayAdapter<String> spAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, labels);
        spAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spBuilding.setAdapter(spAdapter);
    }

    private void loadInvoicesAndRender() {
        Integer year = parseInt(etYear.getText().toString().trim());
        Integer month = parseInt(etMonth.getText().toString().trim());
        if (year == null || month == null || month < 1 || month > 12) {
            Toast.makeText(this, "Nhập tháng/năm hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);
        apiService.getInvoices(year, month).enqueue(new Callback<List<Invoice>>() {
            @Override
            public void onResponse(Call<List<Invoice>> call, Response<List<Invoice>> response) {
                showLoading(false);
                if (!(response.isSuccessful() && response.body() != null)) {
                    tvEmpty.setVisibility(View.VISIBLE);
                    tvEmpty.setText("Không tải được dữ liệu hóa đơn");
                    adapter.setItems(new ArrayList<>());
                    return;
                }
                renderReport(response.body(), year, month);
            }

            @Override
            public void onFailure(Call<List<Invoice>> call, Throwable t) {
                showLoading(false);
                tvEmpty.setVisibility(View.VISIBLE);
                tvEmpty.setText("Lỗi kết nối: " + t.getMessage());
                adapter.setItems(new ArrayList<>());
            }
        });
    }

    private void renderReport(List<Invoice> invoices, int year, int month) {
        Long selectedBuildingId = getSelectedBuildingId();
        String roomKeyword = etRoomKeyword.getText().toString().trim().toLowerCase(Locale.US);
        boolean overdueOnly = cbOverdueOnly.isChecked();

        Map<Long, Phong> roomById = new HashMap<>();
        Map<Long, String> buildingNameById = new HashMap<>();
        for (Phong room : allRooms) roomById.put(room.getId(), room);
        for (ToaNha b : availableBuildings) buildingNameById.put(b.getId(), b.getTen());

        Map<Long, Tenant> tenantByAccount = new HashMap<>();
        for (Tenant tenant : allTenants) {
            if (tenant.getTaiKhoanId() != null) tenantByAccount.put(tenant.getTaiKhoanId(), tenant);
        }

        Map<Long, Long> roomByAccountId = new HashMap<>();
        for (Tenant tenant : allTenants) {
            if (tenant.getTaiKhoanId() == null) continue;
            Long roomId = findRoomForTenant(tenant);
            if (roomId != null) roomByAccountId.put(tenant.getTaiKhoanId(), roomId);
        }

        class Agg {
            double revenue = 0;
            int paid = 0;
            int overdue = 0;
            String roomCode = "N/A";
            String buildingName = "N/A";
        }

        Map<Long, Agg> aggByRoom = new LinkedHashMap<>();
        int totalOverdueInvoices = 0;

        for (Invoice invoice : invoices) {
            if (invoice.getTenantId() == null) continue;
            Long roomId = roomByAccountId.get(invoice.getTenantId());
            if (roomId == null) continue;

            Phong room = roomById.get(roomId);
            if (room == null) continue;

            if (selectedBuildingId != null && (room.getToaNhaId() == null || !selectedBuildingId.equals(room.getToaNhaId()))) {
                continue;
            }

            String roomCode = safe(room.getMaPhong());
            String roomIdText = String.valueOf(room.getId());
            if (!roomKeyword.isEmpty()
                    && !roomCode.toLowerCase(Locale.US).contains(roomKeyword)
                    && !roomIdText.contains(roomKeyword)) {
                continue;
            }

            String status = normalizeStatus(invoice.getStatus());
            Agg agg = aggByRoom.containsKey(roomId) ? aggByRoom.get(roomId) : new Agg();
            agg.roomCode = roomCode;
            agg.buildingName = safe(buildingNameById.get(room.getToaNhaId()));

            if (isPaidLike(status)) {
                agg.revenue += safeAmount(invoice.getTotalAmount());
                agg.paid++;
            }
            if ("OVERDUE".equals(status)) {
                agg.overdue++;
                totalOverdueInvoices++;
            }
            aggByRoom.put(roomId, agg);
        }

        List<RoomRevenueItem> result = new ArrayList<>();
        double totalRevenue = 0;
        int overdueRooms = 0;
        int paidInvoices = 0;

        for (Map.Entry<Long, Agg> entry : aggByRoom.entrySet()) {
            Agg agg = entry.getValue();
            if (overdueOnly && agg.overdue <= 0) continue;
            RoomRevenueItem item = new RoomRevenueItem(
                    entry.getKey(),
                    agg.roomCode,
                    agg.buildingName,
                    agg.revenue,
                    agg.paid,
                    agg.overdue
            );
            result.add(item);
            totalRevenue += agg.revenue;
            paidInvoices += agg.paid;
            if (agg.overdue > 0) overdueRooms++;
        }

        Collections.sort(result, (a, b) -> {
            int cmp = Double.compare(b.getRevenue(), a.getRevenue());
            if (cmp != 0) return cmp;
            return Integer.compare(b.getOverdueInvoices(), a.getOverdueInvoices());
        });

        adapter.setItems(result);
        tvEmpty.setVisibility(result.isEmpty() ? View.VISIBLE : View.GONE);
        if (result.isEmpty()) {
            tvEmpty.setText("Không có dữ liệu phù hợp bộ lọc");
        }

        String monthText = String.format(Locale.US, "%02d/%04d", month, year);
        tvSummaryRevenue.setText("Doanh thu " + monthText + ": " + formatMoney(totalRevenue) + " VND");
        tvSummaryCounts.setText(
                "Phòng hiển thị: " + result.size()
                        + " | Hóa đơn đã thanh toán: " + paidInvoices
                        + " | Phòng trễ hạn: " + overdueRooms
                        + " | HĐ trễ hạn: " + totalOverdueInvoices
        );
    }

    private Long findRoomForTenant(Tenant tenant) {
        Long tenantEntityId = tenant.getId();
        Contract best = null;
        LocalDate bestDate = null;
        for (Contract c : allContracts) {
            if (tenantEntityId == null || c.getNguoiId() == null || !tenantEntityId.equals(c.getNguoiId())) continue;
            LocalDate endDate = parseDate(c.getNgayKetThuc());
            if ("ACTIVE".equalsIgnoreCase(c.getTrangThai())) {
                if (best == null || !"ACTIVE".equalsIgnoreCase(best.getTrangThai()) || compareDate(endDate, bestDate) > 0) {
                    best = c;
                    bestDate = endDate;
                }
            } else if (best == null || compareDate(endDate, bestDate) > 0) {
                best = c;
                bestDate = endDate;
            }
        }
        if (best != null && best.getPhongId() != null) return best.getPhongId();
        return tenant.getSophong();
    }

    private int compareDate(LocalDate a, LocalDate b) {
        if (a == null && b == null) return 0;
        if (a == null) return -1;
        if (b == null) return 1;
        return a.compareTo(b);
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        String[] patterns = new String[]{"yyyy-MM-dd", "dd-MM-yyyy", "yyyy/MM/dd", "dd/MM/yyyy"};
        for (String p : patterns) {
            try {
                return LocalDate.parse(value, DateTimeFormatter.ofPattern(p));
            } catch (Exception ignored) {
            }
        }
        try {
            return LocalDate.parse(value);
        } catch (Exception ignored) {
            return null;
        }
    }

    private Long getSelectedBuildingId() {
        int index = spBuilding.getSelectedItemPosition();
        if (index < 0 || index >= spinnerBuildingIds.size()) return null;
        return spinnerBuildingIds.get(index);
    }

    private String normalizeStatus(String status) {
        if (status == null) return "";
        if ("PENDING".equalsIgnoreCase(status)) return "UNPAID";
        return status.toUpperCase(Locale.US);
    }

    private boolean isPaidLike(String status) {
        return "PAID".equals(status) || "PARTIALLY_PAID".equals(status);
    }

    private double safeAmount(Double value) {
        return value == null ? 0 : value;
    }

    private Integer parseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (Exception ignored) {
            return null;
        }
    }

    private String formatMoney(double value) {
        return String.format(Locale.US, "%,d", Math.round(value));
    }

    private String safe(String value) {
        return value == null || value.trim().isEmpty() ? "N/A" : value;
    }

    private void showLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnApply.setEnabled(!loading);
    }

    private void failLoad(String message) {
        showLoading(false);
        tvEmpty.setVisibility(View.VISIBLE);
        tvEmpty.setText(message);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
