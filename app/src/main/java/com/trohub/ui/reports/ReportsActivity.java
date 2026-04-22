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
    private Spinner spStatus;
    private CheckBox cbOverdueOnly;
    private Button btnApply;
    private ProgressBar progressBar;
    private TextView tvSummaryRevenue;
    private TextView tvSummaryCounts;
    private TextView tvEmpty;
    private RecyclerView rvReport;
    private RevenueChartView chartRevenue;

    private ApiService apiService;
    private SessionManager sessionManager;
    private RoomRevenueAdapter adapter;

    private final List<ToaNha> availableBuildings = new ArrayList<>();
    private final List<Phong> allRooms = new ArrayList<>();
    private final List<Tenant> allTenants = new ArrayList<>();
    private final List<Contract> allContracts = new ArrayList<>();
    private final List<Long> spinnerBuildingIds = new ArrayList<>();
    private final List<String> statusValues = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);

        sessionManager = new SessionManager(this);
        NetworkClient.setAuthToken(sessionManager.getToken());
        apiService = NetworkClient.getRetrofitClient().create(ApiService.class);

        bindViews();
        setupDefaults();
        setupStatusSpinner();

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
        spStatus = findViewById(R.id.spStatusFilter);
        cbOverdueOnly = findViewById(R.id.cbOverdueOnly);
        btnApply = findViewById(R.id.btnApplyFilter);
        progressBar = findViewById(R.id.progressBar);
        tvSummaryRevenue = findViewById(R.id.tvSummaryRevenue);
        tvSummaryCounts = findViewById(R.id.tvSummaryCounts);
        tvEmpty = findViewById(R.id.tvEmpty);
        rvReport = findViewById(R.id.rvReportRooms);
        chartRevenue = findViewById(R.id.chartRevenue);
    }

    private void setupDefaults() {
        Calendar c = Calendar.getInstance();
        etYear.setText(String.valueOf(c.get(Calendar.YEAR)));
        etMonth.setText(String.valueOf(c.get(Calendar.MONTH) + 1));
    }

    private void setupStatusSpinner() {
        statusValues.clear();
        statusValues.add("ALL");
        statusValues.add("PAID");
        statusValues.add("UNPAID");
        statusValues.add("OVERDUE");
        statusValues.add("PARTIALLY_PAID");
        statusValues.add("DRAFT");

        List<String> labels = new ArrayList<>();
        labels.add("Tất cả trạng thái");
        labels.add("PAID - đã thanh toán");
        labels.add("UNPAID - chưa thanh toán");
        labels.add("OVERDUE - trễ hạn");
        labels.add("PARTIALLY_PAID - thanh toán một phần");
        labels.add("DRAFT - hóa đơn nháp");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, labels);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spStatus.setAdapter(adapter);
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
                    chartRevenue.setEntries(new ArrayList<>());
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
                chartRevenue.setEntries(new ArrayList<>());
            }
        });
    }

    private void renderReport(List<Invoice> invoices, int year, int month) {
        Long selectedBuildingId = getSelectedBuildingId();
        String roomKeyword = etRoomKeyword.getText().toString().trim().toLowerCase(Locale.US);
        String selectedStatus = getSelectedStatus();
        boolean overdueOnly = cbOverdueOnly.isChecked();

        Map<Long, Phong> roomById = new HashMap<>();
        Map<Long, String> buildingNameById = new HashMap<>();
        for (Phong room : allRooms) roomById.put(room.getId(), room);
        for (ToaNha b : availableBuildings) buildingNameById.put(b.getId(), b.getTen());

        Map<Long, Long> roomByTenantId = new HashMap<>();
        for (Tenant tenant : allTenants) {
            if (tenant.getId() == null) continue;
            Long roomId = findRoomForTenant(tenant);
            if (roomId != null) roomByTenantId.put(tenant.getId(), roomId);
        }

        class Agg {
            double revenue = 0;
            double expected = 0;
            int invoiceCount = 0;
            int paid = 0;
            int unpaid = 0;
            int draft = 0;
            int partial = 0;
            int overdue = 0;
            String roomCode = "N/A";
            String buildingName = "N/A";
        }

        Map<Long, Agg> aggByRoom = new LinkedHashMap<>();
        int totalOverdueInvoices = 0;

        for (Invoice invoice : invoices) {
            String status = normalizeStatus(invoice.getStatus());
            if (!matchesStatus(selectedStatus, status)) continue;

            Long roomId = invoice.getRoomId();
            if (roomId == null && invoice.getTenantId() != null) {
                roomId = roomByTenantId.get(invoice.getTenantId());
            }
            if (roomId == null) continue;

            Phong room = roomById.get(roomId);
            Long buildingId = invoice.getBuildingId();
            String roomCode = safe(invoice.getRoomCode());
            String buildingName = safe(invoice.getBuildingName());
            if (room != null) {
                buildingId = room.getToaNhaId();
                roomCode = safe(room.getMaPhong());
                buildingName = safe(buildingNameById.get(room.getToaNhaId()));
            }

            if (selectedBuildingId != null && (buildingId == null || !selectedBuildingId.equals(buildingId))) {
                continue;
            }

            String roomIdText = String.valueOf(roomId);
            if (!roomKeyword.isEmpty()
                    && !roomCode.toLowerCase(Locale.US).contains(roomKeyword)
                    && !roomIdText.contains(roomKeyword)) {
                continue;
            }

            Agg agg = aggByRoom.containsKey(roomId) ? aggByRoom.get(roomId) : new Agg();
            agg.roomCode = roomCode;
            agg.buildingName = buildingName;
            agg.invoiceCount++;
            agg.expected += safeAmount(invoice.getTotalAmount());

            if (isPaidLike(status)) {
                agg.revenue += safeAmount(invoice.getTotalAmount());
            }
            if ("PAID".equals(status)) {
                agg.paid++;
            }
            if ("UNPAID".equals(status)) agg.unpaid++;
            if ("DRAFT".equals(status)) agg.draft++;
            if ("PARTIALLY_PAID".equals(status)) agg.partial++;
            if ("OVERDUE".equals(status)) {
                agg.overdue++;
                totalOverdueInvoices++;
            }
            aggByRoom.put(roomId, agg);
        }

        List<RoomRevenueItem> result = new ArrayList<>();
        double totalRevenue = 0;
        double totalExpected = 0;
        int overdueRooms = 0;
        int paidInvoices = 0;
        int unpaidInvoices = 0;
        int draftInvoices = 0;
        int partialInvoices = 0;
        int totalInvoices = 0;

        for (Map.Entry<Long, Agg> entry : aggByRoom.entrySet()) {
            Agg agg = entry.getValue();
            if (overdueOnly && agg.overdue <= 0) continue;
            RoomRevenueItem item = new RoomRevenueItem(
                    entry.getKey(),
                    agg.roomCode,
                    agg.buildingName,
                    agg.revenue,
                    agg.expected,
                    agg.invoiceCount,
                    agg.paid,
                    agg.unpaid,
                    agg.draft,
                    agg.partial,
                    agg.overdue
            );
            result.add(item);
            totalRevenue += agg.revenue;
            totalExpected += agg.expected;
            totalInvoices += agg.invoiceCount;
            paidInvoices += agg.paid;
            unpaidInvoices += agg.unpaid;
            draftInvoices += agg.draft;
            partialInvoices += agg.partial;
            if (agg.overdue > 0) overdueRooms++;
        }

        Collections.sort(result, (a, b) -> {
            int cmp = Double.compare(b.getRevenue(), a.getRevenue());
            if (cmp != 0) return cmp;
            return Integer.compare(b.getOverdueInvoices(), a.getOverdueInvoices());
        });

        adapter.setItems(result);
        renderChart(result);
        tvEmpty.setVisibility(result.isEmpty() ? View.VISIBLE : View.GONE);
        if (result.isEmpty()) {
            tvEmpty.setText("Không có dữ liệu phù hợp bộ lọc");
        }

        String monthText = String.format(Locale.US, "%02d/%04d", month, year);
        tvSummaryRevenue.setText("Đã thu " + monthText + ": " + formatMoney(totalRevenue)
                + " VND | Phải thu: " + formatMoney(totalExpected) + " VND");
        tvSummaryCounts.setText(
                "Phòng hiển thị: " + result.size()
                        + " | HĐ: " + totalInvoices
                        + " | PAID: " + paidInvoices
                        + " | UNPAID: " + unpaidInvoices
                        + " | PARTIAL: " + partialInvoices
                        + " | DRAFT: " + draftInvoices
                        + " | OVERDUE: " + totalOverdueInvoices
                        + " | Phòng trễ hạn: " + overdueRooms
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

    private String getSelectedStatus() {
        int index = spStatus.getSelectedItemPosition();
        if (index < 0 || index >= statusValues.size()) return "ALL";
        return statusValues.get(index);
    }

    private boolean matchesStatus(String selected, String actual) {
        if (selected == null || "ALL".equals(selected)) return true;
        return selected.equalsIgnoreCase(actual);
    }

    private void renderChart(List<RoomRevenueItem> items) {
        List<RevenueChartView.Entry> entries = new ArrayList<>();
        int count = Math.min(8, items == null ? 0 : items.size());
        for (int i = 0; i < count; i++) {
            RoomRevenueItem item = items.get(i);
            entries.add(new RevenueChartView.Entry(item.getRoomCode(), item.getRevenue()));
        }
        chartRevenue.setEntries(entries);
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
