package com.trohub.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.trohub.ui.api.ApiService;
import com.trohub.ui.api.NetworkClient;
import com.trohub.ui.api.models.Contract;
import com.trohub.ui.api.models.GuestEntry;
import com.trohub.ui.api.models.Incident;
import com.trohub.ui.api.models.Invoice;
import com.trohub.ui.api.models.Landlord;
import com.trohub.ui.api.models.Phong;
import com.trohub.ui.api.models.Tenant;
import com.trohub.ui.api.models.ToaNha;
import com.trohub.ui.auth.CreateTenantAccountActivity;
import com.trohub.ui.auth.LoginActivity;
import com.trohub.ui.auth.SessionManager;
import com.trohub.ui.billing.BillingActivity;
import com.trohub.ui.buildings.BuildingsActivity;
import com.trohub.ui.contracts.ContractsActivity;
import com.trohub.ui.dashboard.RecentActivityAdapter;
import com.trohub.ui.dashboard.RecentActivityItem;
import com.trohub.ui.guest.GuestEntriesActivity;
import com.trohub.ui.guest.LandlordGuestReviewActivity;
import com.trohub.ui.incidents.IncidentsActivity;
import com.trohub.ui.profile.ProfileActivity;
import com.trohub.ui.reports.ReportsActivity;
import com.trohub.ui.rooms.RoomsActivity;
import com.trohub.ui.tenants.TenantsActivity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private SessionManager sessionManager;
    private ApiService apiService;
    private Long accountId;
    private boolean isTenantOnly;
    private boolean navigationTriggered = false;

    private RecentActivityAdapter recentActivityAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            navigateToLoginDeferred();
            return;
        }
        NetworkClient.setAuthToken(sessionManager.getToken());
        apiService = NetworkClient.getRetrofitClient().create(ApiService.class);
        accountId = sessionManager.getUserIdFromToken();
        isTenantOnly = sessionManager.hasAnyRole("ROLE_USER")
                && !sessionManager.hasAnyRole("ROLE_ADMIN", "ROLE_BILLING_STAFF", "ROLE_LANDLORD");

        setContentView(R.layout.activity_main);

        TextView tvUser = findViewById(R.id.tvUser);
        TextView tvRole = findViewById(R.id.tvRole);
        LinearLayout tenantSummarySection = findViewById(R.id.tenantSummarySection);
        LinearLayout landlordOverviewSection = findViewById(R.id.landlordOverviewSection);

        TextView tvLandlordInfo = findViewById(R.id.tvLandlordInfo);
        TextView tvContractInfo = findViewById(R.id.tvContractInfo);
        TextView tvRoomInfo = findViewById(R.id.tvRoomInfo);
        TextView tvBuildingInfo = findViewById(R.id.tvBuildingInfo);

        TextView tvOverviewRevenue = findViewById(R.id.tvOverviewRevenue);
        TextView tvOverviewRooms = findViewById(R.id.tvOverviewRooms);
        TextView tvOverviewPaid = findViewById(R.id.tvOverviewPaid);
        TextView tvOverviewOverdue = findViewById(R.id.tvOverviewOverdue);
        TextView tvRecentEmpty = findViewById(R.id.tvRecentEmpty);
        RecyclerView rvRecent = findViewById(R.id.rvRecentActivities);

        Button btnProfile = findViewById(R.id.btnProfile);
        Button btnRooms = findViewById(R.id.btnRooms);
        Button btnBuildings = findViewById(R.id.btnBuildings);
        Button btnTenants = findViewById(R.id.btnTenants);
        Button btnIncidents = findViewById(R.id.btnIncidents);
        Button btnContracts = findViewById(R.id.btnContracts);
        Button btnBilling = findViewById(R.id.btnBilling);
        Button btnReports = findViewById(R.id.btnReports);
        Button btnOverviewReports = findViewById(R.id.btnOverviewReports);
        Button btnGuestEntries = findViewById(R.id.btnGuestEntries);
        LinearLayout layoutOtherFunctions = findViewById(R.id.layoutOtherFunctions);
        Button btnCreateTenantAccount = findViewById(R.id.btnCreateTenantAccount);
        Button btnLogout = findViewById(R.id.btnLogout);
        boolean canCreateTenantAccount = sessionManager.isAdminOrLandlord();

        rvRecent.setLayoutManager(new LinearLayoutManager(this));
        recentActivityAdapter = new RecentActivityAdapter();
        rvRecent.setAdapter(recentActivityAdapter);

        tvUser.setText("Tài khoản: " + sessionManager.getUsername());
        List<String> roles = sessionManager.getRoles();
        String roleLabel = roles.isEmpty() ? "ROLE_USER" : TextUtils.join(", ", roles);
        tvRole.setText("Vai trò: " + roleLabel);

        if (isTenantOnly) {
            landlordOverviewSection.setVisibility(View.GONE);
            tenantSummarySection.setVisibility(View.VISIBLE);
            btnReports.setVisibility(View.GONE);

            btnBuildings.setVisibility(View.GONE);
            btnTenants.setVisibility(View.GONE);
            btnProfile.setText("Hồ sơ của tôi");
            btnRooms.setText("Phòng của tôi");
            btnIncidents.setText("Báo cáo sự cố");
            btnContracts.setText("Hợp đồng của tôi");
            btnBilling.setText("Đóng tiền thuê phòng");
            btnGuestEntries.setText("Đăng ký khách ra/vào");
            btnGuestEntries.setVisibility(View.VISIBLE);
            layoutOtherFunctions.setVisibility(View.GONE);

            loadTenantSummary(tvLandlordInfo, tvContractInfo, tvRoomInfo, tvBuildingInfo);
        } else {
            tenantSummarySection.setVisibility(View.GONE);
            landlordOverviewSection.setVisibility(View.VISIBLE);
            btnReports.setVisibility(View.VISIBLE);
            btnGuestEntries.setVisibility(View.VISIBLE);
            btnGuestEntries.setText("Duyệt khách ra/vào");
            layoutOtherFunctions.setVisibility(canCreateTenantAccount ? View.VISIBLE : View.GONE);

            loadLandlordOverview(tvOverviewRevenue, tvOverviewRooms, tvOverviewPaid, tvOverviewOverdue, tvRecentEmpty);
        }

        btnProfile.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, ProfileActivity.class)));
        btnRooms.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, RoomsActivity.class)));
        btnBuildings.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, BuildingsActivity.class)));
        btnTenants.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, TenantsActivity.class)));
        btnIncidents.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, IncidentsActivity.class)));
        btnContracts.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, ContractsActivity.class)));
        btnBilling.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, BillingActivity.class)));
        btnGuestEntries.setOnClickListener(v -> {
            if (isTenantOnly) {
                startActivity(new Intent(MainActivity.this, GuestEntriesActivity.class));
            } else {
                startActivity(new Intent(MainActivity.this, LandlordGuestReviewActivity.class));
            }
        });
        btnCreateTenantAccount.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, CreateTenantAccountActivity.class)));

        View.OnClickListener reportClick = v -> startActivity(new Intent(MainActivity.this, ReportsActivity.class));
        btnReports.setOnClickListener(reportClick);
        btnOverviewReports.setOnClickListener(reportClick);

        btnLogout.setOnClickListener(v -> {
            NetworkClient.setAuthToken(null);
            sessionManager.clear();
            goLogin();
        });
    }

    private void goLogin() {
        if (isFinishing() || isDestroyed()) return;
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void navigateToLoginDeferred() {
        if (navigationTriggered || isFinishing() || isDestroyed()) return;
        navigationTriggered = true;
        if (getWindow() != null && getWindow().getDecorView() != null) {
            getWindow().getDecorView().post(this::goLogin);
        } else {
            goLogin();
        }
    }

    private void loadLandlordOverview(
            TextView tvOverviewRevenue,
            TextView tvOverviewRooms,
            TextView tvOverviewPaid,
            TextView tvOverviewOverdue,
            TextView tvRecentEmpty
    ) {
        LocalDate now = LocalDate.now();
        int year = now.getYear();
        int month = now.getMonthValue();

        tvOverviewRevenue.setText("Doanh thu tháng " + month + "/" + year + ": đang tải...");
        tvOverviewRooms.setText("Phòng: ...");
        tvOverviewPaid.setText("Đã thanh toán: ...");
        tvOverviewOverdue.setText("Trễ hạn: ...");
        tvRecentEmpty.setVisibility(View.GONE);

        apiService.getBuildings().enqueue(new Callback<List<ToaNha>>() {
            @Override
            public void onResponse(Call<List<ToaNha>> call, Response<List<ToaNha>> response) {
                List<ToaNha> allBuildings = response.isSuccessful() && response.body() != null ? response.body() : new ArrayList<>();
                applyBuildingScope(allBuildings, scopedBuildings -> continueLandlordOverview(
                        scopedBuildings, year, month, tvOverviewRevenue, tvOverviewRooms, tvOverviewPaid, tvOverviewOverdue, tvRecentEmpty
                ));
            }

            @Override
            public void onFailure(Call<List<ToaNha>> call, Throwable t) {
                tvOverviewRevenue.setText("Doanh thu tháng " + month + "/" + year + ": không tải được");
                tvOverviewRooms.setText("Phòng: N/A");
                tvOverviewPaid.setText("Đã thanh toán: N/A");
                tvOverviewOverdue.setText("Trễ hạn: N/A");
                tvRecentEmpty.setVisibility(View.VISIBLE);
                tvRecentEmpty.setText("Không tải được tổng quan chủ trọ");
            }
        });
    }

    private void continueLandlordOverview(
            List<ToaNha> scopedBuildings,
            int year,
            int month,
            TextView tvOverviewRevenue,
            TextView tvOverviewRooms,
            TextView tvOverviewPaid,
            TextView tvOverviewOverdue,
            TextView tvRecentEmpty
    ) {
        Set<Long> buildingIds = new HashSet<>();
        for (ToaNha b : scopedBuildings) {
            if (b.getId() != null) buildingIds.add(b.getId());
        }

        apiService.getPhongs().enqueue(new Callback<List<Phong>>() {
            @Override
            public void onResponse(Call<List<Phong>> call, Response<List<Phong>> response) {
                List<Phong> scopedRooms = new ArrayList<>();
                Map<Long, Phong> roomById = new HashMap<>();

                if (response.isSuccessful() && response.body() != null) {
                    for (Phong room : response.body()) {
                        if (room.getToaNhaId() != null && buildingIds.contains(room.getToaNhaId())) {
                            scopedRooms.add(room);
                            if (room.getId() != null) roomById.put(room.getId(), room);
                        }
                    }
                }

                tvOverviewRooms.setText("Phòng: " + scopedRooms.size());
                Set<Long> scopedRoomIds = new HashSet<>();
                for (Phong room : scopedRooms) {
                    if (room.getId() != null) scopedRoomIds.add(room.getId());
                }

                apiService.getTenants().enqueue(new Callback<List<Tenant>>() {
                    @Override
                    public void onResponse(Call<List<Tenant>> call, Response<List<Tenant>> tenantResp) {
                        List<Tenant> tenants = tenantResp.isSuccessful() && tenantResp.body() != null ? tenantResp.body() : new ArrayList<>();
                        apiService.getContracts().enqueue(new Callback<List<Contract>>() {
                            @Override
                            public void onResponse(Call<List<Contract>> call, Response<List<Contract>> contractResp) {
                                List<Contract> contracts = contractResp.isSuccessful() && contractResp.body() != null ? contractResp.body() : new ArrayList<>();
                                Map<Long, Long> roomByAccountId = mapAccountToRoom(tenants, contracts, roomById);

                                apiService.getInvoices(year, month).enqueue(new Callback<List<Invoice>>() {
                                    @Override
                                    public void onResponse(Call<List<Invoice>> call, Response<List<Invoice>> invoiceResp) {
                                        List<Invoice> invoices = invoiceResp.isSuccessful() && invoiceResp.body() != null ? invoiceResp.body() : new ArrayList<>();
                                        double revenue = 0;
                                        int paidCount = 0;
                                        int overdueCount = 0;

                                        for (Invoice invoice : invoices) {
                                            if (invoice.getTenantId() == null) continue;
                                            Long roomId = roomByAccountId.get(invoice.getTenantId());
                                            if (roomId == null || !scopedRoomIds.contains(roomId)) continue;

                                            String status = normalizeStatus(invoice.getStatus());
                                            if (isPaidLike(status)) {
                                                revenue += safeAmount(invoice.getTotalAmount());
                                                paidCount++;
                                            }
                                            if ("OVERDUE".equals(status)) {
                                                overdueCount++;
                                            }
                                        }

                                        tvOverviewRevenue.setText("Doanh thu tháng " + month + "/" + year + ": " + formatMoney(revenue) + " VND");
                                        tvOverviewPaid.setText("Đã thanh toán: " + paidCount);
                                        tvOverviewOverdue.setText("Trễ hạn: " + overdueCount);

                                        loadRecentActivities(invoices, roomById, roomByAccountId, scopedRoomIds, tvRecentEmpty, month, year);
                                    }

                                    @Override
                                    public void onFailure(Call<List<Invoice>> call, Throwable t) {
                                        tvOverviewRevenue.setText("Doanh thu tháng " + month + "/" + year + ": không tải được");
                                        tvOverviewPaid.setText("Đã thanh toán: N/A");
                                        tvOverviewOverdue.setText("Trễ hạn: N/A");
                                        tvRecentEmpty.setVisibility(View.VISIBLE);
                                        tvRecentEmpty.setText("Không tải được hoạt động gần đây");
                                    }
                                });
                            }

                            @Override
                            public void onFailure(Call<List<Contract>> call, Throwable t) {
                                tvOverviewRevenue.setText("Doanh thu tháng " + month + "/" + year + ": không tải được");
                                tvOverviewPaid.setText("Đã thanh toán: N/A");
                                tvOverviewOverdue.setText("Trễ hạn: N/A");
                            }
                        });
                    }

                    @Override
                    public void onFailure(Call<List<Tenant>> call, Throwable t) {
                        tvOverviewRevenue.setText("Doanh thu tháng " + month + "/" + year + ": không tải được");
                        tvOverviewPaid.setText("Đã thanh toán: N/A");
                        tvOverviewOverdue.setText("Trễ hạn: N/A");
                    }
                });
            }

            @Override
            public void onFailure(Call<List<Phong>> call, Throwable t) {
                tvOverviewRooms.setText("Phòng: N/A");
                tvOverviewRevenue.setText("Doanh thu tháng " + month + "/" + year + ": không tải được");
                tvOverviewPaid.setText("Đã thanh toán: N/A");
                tvOverviewOverdue.setText("Trễ hạn: N/A");
            }
        });
    }

    private void applyBuildingScope(List<ToaNha> allBuildings, BuildingScopeCallback callback) {
        callback.onReady(allBuildings);
    }

    private Map<Long, Long> mapAccountToRoom(List<Tenant> tenants, List<Contract> contracts, Map<Long, Phong> roomById) {
        Map<Long, Long> result = new HashMap<>();
        for (Tenant tenant : tenants) {
            if (tenant.getTaiKhoanId() == null) continue;
            Long roomId = findRoomForTenant(tenant, contracts);
            if (roomId != null && roomById.containsKey(roomId)) {
                result.put(tenant.getTaiKhoanId(), roomId);
            }
        }
        return result;
    }

    private Long findRoomForTenant(Tenant tenant, List<Contract> contracts) {
        Contract best = null;
        LocalDate bestEnd = null;
        for (Contract c : contracts) {
            if (tenant.getId() == null || c.getNguoiId() == null || !tenant.getId().equals(c.getNguoiId())) continue;
            LocalDate end = parseDateFlex(c.getNgayKetThuc());
            if ("ACTIVE".equalsIgnoreCase(c.getTrangThai())) {
                if (best == null || !"ACTIVE".equalsIgnoreCase(best.getTrangThai()) || compareDate(end, bestEnd) > 0) {
                    best = c;
                    bestEnd = end;
                }
            } else if (best == null || compareDate(end, bestEnd) > 0) {
                best = c;
                bestEnd = end;
            }
        }
        if (best != null && best.getPhongId() != null) return best.getPhongId();
        return tenant.getSophong();
    }

    private void loadRecentActivities(
            List<Invoice> invoices,
            Map<Long, Phong> roomById,
            Map<Long, Long> roomByAccountId,
            Set<Long> scopedRoomIds,
            TextView tvRecentEmpty,
            int month,
            int year
    ) {
        List<ActivitySeed> seeds = new ArrayList<>();

        for (Invoice invoice : invoices) {
            Long roomId = invoice.getTenantId() == null ? null : roomByAccountId.get(invoice.getTenantId());
            if (roomId == null) continue;
            if (!scopedRoomIds.contains(roomId)) continue;
            Phong room = roomById.get(roomId);
            String roomCode = room == null ? "N/A" : safe(room.getMaPhong());
            String status = normalizeStatus(invoice.getStatus());

            if (isPaidLike(status)) {
                seeds.add(new ActivitySeed(
                        "Phòng " + roomCode + " đã đóng tiền",
                        "Hóa đơn " + safe(invoice.getInvoiceNumber()) + " | kỳ " + month + "/" + year,
                        parseTimeToMillis(invoice.getIssueDate())
                ));
            } else if ("OVERDUE".equals(status)) {
                seeds.add(new ActivitySeed(
                        "Phòng " + roomCode + " đang trễ hạn",
                        "Hóa đơn " + safe(invoice.getInvoiceNumber()) + " chưa thanh toán",
                        parseTimeToMillis(invoice.getDueDate())
                ));
            }
        }

        apiService.getIncidents().enqueue(new Callback<List<Incident>>() {
            @Override
            public void onResponse(Call<List<Incident>> call, Response<List<Incident>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (Incident incident : response.body()) {
                        if (incident.getPhongId() != null && scopedRoomIds.contains(incident.getPhongId())) {
                            Phong room = roomById.get(incident.getPhongId());
                            String roomCode = room == null ? "N/A" : safe(room.getMaPhong());
                            seeds.add(new ActivitySeed(
                                    "Sự cố mới ở phòng " + roomCode,
                                    safe(incident.getLoai()) + " | " + safe(incident.getStatus()),
                                    parseTimeToMillis(incident.getReportedAt())
                            ));
                        }
                    }
                }
                loadGuestActivities(seeds, roomById, scopedRoomIds, tvRecentEmpty);
            }

            @Override
            public void onFailure(Call<List<Incident>> call, Throwable t) {
                loadGuestActivities(seeds, roomById, scopedRoomIds, tvRecentEmpty);
            }
        });
    }

    private void loadGuestActivities(
            List<ActivitySeed> seeds,
            Map<Long, Phong> roomById,
            Set<Long> scopedRoomIds,
            TextView tvRecentEmpty
    ) {
        apiService.getGuestEntries().enqueue(new Callback<List<GuestEntry>>() {
            @Override
            public void onResponse(Call<List<GuestEntry>> call, Response<List<GuestEntry>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (GuestEntry entry : response.body()) {
                        if (entry.getPhongId() != null && scopedRoomIds.contains(entry.getPhongId())) {
                            Phong room = roomById.get(entry.getPhongId());
                            String roomCode = room == null ? "N/A" : safe(room.getMaPhong());
                            seeds.add(new ActivitySeed(
                                    "Yêu cầu khách phòng " + roomCode + ": " + safe(entry.getApprovalStatus()),
                                    safe(entry.getTen()) + " | " + safe(entry.getLoai()),
                                    parseTimeToMillis(entry.getTimestamp())
                            ));
                        }
                    }
                }
                publishRecentActivities(seeds, tvRecentEmpty);
            }

            @Override
            public void onFailure(Call<List<GuestEntry>> call, Throwable t) {
                publishRecentActivities(seeds, tvRecentEmpty);
            }
        });
    }

    private void publishRecentActivities(List<ActivitySeed> seeds, TextView tvRecentEmpty) {
        Collections.sort(seeds, (a, b) -> Long.compare(b.epochMs, a.epochMs));
        List<RecentActivityItem> items = new ArrayList<>();
        int limit = Math.min(10, seeds.size());
        for (int i = 0; i < limit; i++) {
            ActivitySeed seed = seeds.get(i);
            items.add(new RecentActivityItem(seed.title, seed.subtitle, toRelativeLabel(seed.epochMs)));
        }
        recentActivityAdapter.setItems(items);
        tvRecentEmpty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private String toRelativeLabel(long epochMs) {
        if (epochMs <= 0) return "Không rõ thời gian";
        long diffMs = System.currentTimeMillis() - epochMs;
        if (diffMs < 0) return "Vừa xong";
        long minutes = diffMs / (60 * 1000);
        if (minutes < 1) return "Vừa xong";
        if (minutes < 60) return minutes + " phút trước";
        long hours = minutes / 60;
        if (hours < 24) return hours + " giờ trước";
        long days = hours / 24;
        return days + " ngày trước";
    }

    private long parseTimeToMillis(String raw) {
        if (raw == null || raw.trim().isEmpty()) return 0;
        String value = raw.trim();
        String[] dateTimePatterns = new String[]{
                "yyyy-MM-dd'T'HH:mm:ss",
                "yyyy-MM-dd'T'HH:mm:ss.SSS",
                "yyyy-MM-dd HH:mm:ss",
                "dd-MM-yyyy HH:mm:ss",
                "dd/MM/yyyy HH:mm:ss"
        };
        for (String p : dateTimePatterns) {
            try {
                LocalDateTime dt = LocalDateTime.parse(value, DateTimeFormatter.ofPattern(p));
                return dt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            } catch (Exception ignored) {
            }
        }
        LocalDate date = parseDateFlex(value);
        if (date != null) {
            return date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        }
        return 0;
    }

    private void loadTenantSummary(TextView tvLandlordInfo, TextView tvContractInfo, TextView tvRoomInfo, TextView tvBuildingInfo) {
        tvLandlordInfo.setText("Chủ trọ: đang tải...");
        tvContractInfo.setText("Hợp đồng: đang tải...");
        tvRoomInfo.setText("Phòng: đang tải...");
        tvBuildingInfo.setText("Tòa nhà: đang tải...");

        apiService.getTenants().enqueue(new Callback<List<Tenant>>() {
            @Override
            public void onResponse(Call<List<Tenant>> call, Response<List<Tenant>> response) {
                if (!(response.isSuccessful() && response.body() != null)) {
                    tvLandlordInfo.setText("Chủ trọ: N/A");
                    tvContractInfo.setText("Hợp đồng: không tải được");
                    tvRoomInfo.setText("Phòng: không tải được");
                    tvBuildingInfo.setText("Tòa nhà: không tải được");
                    return;
                }

                Tenant me = null;
                for (Tenant tenant : response.body()) {
                    if (tenant.getTaiKhoanId() != null && tenant.getTaiKhoanId().equals(accountId)) {
                        me = tenant;
                        break;
                    }
                }
                if (me == null) {
                    tvContractInfo.setText("Hợp đồng: chưa gắn tenant cho tài khoản này");
                    tvRoomInfo.setText("Phòng: N/A");
                    tvBuildingInfo.setText("Tòa nhà: N/A");
                    tvLandlordInfo.setText("Chủ trọ: N/A");
                    return;
                }

                Tenant currentTenant = me;
                Long roomId = currentTenant.getSophong();

                apiService.getContracts().enqueue(new Callback<List<Contract>>() {
                    @Override
                    public void onResponse(Call<List<Contract>> call, Response<List<Contract>> contractResp) {
                        List<Contract> myContracts = new ArrayList<>();
                        if (contractResp.isSuccessful() && contractResp.body() != null) {
                            for (Contract contract : contractResp.body()) {
                                if (contract.getNguoiId() != null && contract.getNguoiId().equals(currentTenant.getId())) {
                                    myContracts.add(contract);
                                }
                            }
                        }

                        Contract latest = pickBestContract(myContracts);
                        if (latest == null) {
                            tvContractInfo.setText("Hợp đồng: chưa có dữ liệu");
                        } else {
                            tvContractInfo.setText("Hợp đồng: " + buildContractSummary(latest));
                        }

                        apiService.getPhongs().enqueue(new Callback<List<Phong>>() {
                            @Override
                            public void onResponse(Call<List<Phong>> call, Response<List<Phong>> roomResp) {
                                if (!(roomResp.isSuccessful() && roomResp.body() != null)) return;

                                Phong myRoom = null;
                                for (Phong room : roomResp.body()) {
                                    if (roomId != null && room.getId() != null && room.getId().equals(roomId)) {
                                        myRoom = room;
                                        break;
                                    }
                                }
                                if (myRoom == null) {
                                    tvRoomInfo.setText("Phòng: N/A");
                                    tvBuildingInfo.setText("Tòa nhà: N/A");
                                    tvLandlordInfo.setText("Chủ trọ: N/A");
                                    return;
                                }

                                tvRoomInfo.setText("Phòng: " + safe(myRoom.getMaPhong()) + " | trạng thái " + safe(myRoom.getTrangThai()));

                                Long roomBuildingId = myRoom.getToaNhaId();
                                apiService.getBuildings().enqueue(new Callback<List<ToaNha>>() {
                                    @Override
                                    public void onResponse(Call<List<ToaNha>> call, Response<List<ToaNha>> buildingResp) {
                                        if (!(buildingResp.isSuccessful() && buildingResp.body() != null)) return;
                                        ToaNha myBuilding = null;
                                        for (ToaNha building : buildingResp.body()) {
                                            if (roomBuildingId != null && building.getId() != null && building.getId().equals(roomBuildingId)) {
                                                myBuilding = building;
                                                break;
                                            }
                                        }
                                        if (myBuilding == null) {
                                            tvBuildingInfo.setText("Tòa nhà: N/A");
                                            tvLandlordInfo.setText("Chủ trọ: N/A");
                                            return;
                                        }

                                        tvBuildingInfo.setText("Tòa nhà: " + safe(myBuilding.getTen()) + " | " + safe(myBuilding.getDiaChi()));

                                        Long landlordId = myBuilding.getChuTroId();
                                        apiService.getLandlords().enqueue(new Callback<List<Landlord>>() {
                                            @Override
                                            public void onResponse(Call<List<Landlord>> call, Response<List<Landlord>> landlordResp) {
                                                if (!(landlordResp.isSuccessful() && landlordResp.body() != null)) {
                                                    tvLandlordInfo.setText("Chủ trọ: N/A");
                                                    return;
                                                }
                                                boolean found = false;
                                                for (Landlord landlord : landlordResp.body()) {
                                                    if (landlordId != null && landlord.getId() != null && landlord.getId().equals(landlordId)) {
                                                        tvLandlordInfo.setText("Chủ trọ: " + safe(landlord.getTen()) + " | SĐT: " + safe(landlord.getSdt()));
                                                        found = true;
                                                        break;
                                                    }
                                                }
                                                if (!found) tvLandlordInfo.setText("Chủ trọ: N/A");
                                            }

                                            @Override
                                            public void onFailure(Call<List<Landlord>> call, Throwable t) {
                                                tvLandlordInfo.setText("Chủ trọ: không tải được");
                                            }
                                        });
                                    }

                                    @Override
                                    public void onFailure(Call<List<ToaNha>> call, Throwable t) {
                                        tvBuildingInfo.setText("Tòa nhà: không tải được");
                                        tvLandlordInfo.setText("Chủ trọ: không tải được");
                                    }
                                });
                            }

                            @Override
                            public void onFailure(Call<List<Phong>> call, Throwable t) {
                                tvRoomInfo.setText("Phòng: không tải được");
                                tvBuildingInfo.setText("Tòa nhà: không tải được");
                                tvLandlordInfo.setText("Chủ trọ: không tải được");
                            }
                        });
                    }

                    @Override
                    public void onFailure(Call<List<Contract>> call, Throwable t) {
                        tvContractInfo.setText("Hợp đồng: không tải được");
                    }
                });
            }

            @Override
            public void onFailure(Call<List<Tenant>> call, Throwable t) {
                tvLandlordInfo.setText("Chủ trọ: không tải được");
                tvContractInfo.setText("Hợp đồng: không tải được");
                tvRoomInfo.setText("Phòng: không tải được");
                tvBuildingInfo.setText("Tòa nhà: không tải được");
            }
        });
    }

    private String buildContractSummary(Contract contract) {
        String status = safe(contract.getTrangThai());
        LocalDate endDate = parseDateFlex(contract.getNgayKetThuc());
        String rent = contract.getTienThue() == null ? "N/A" : formatMoney(contract.getTienThue()) + " VND";
        if (endDate == null) {
            return safe(contract.getMaHopDong()) + " | " + status + " | thuê: " + rent;
        }
        long days = ChronoUnit.DAYS.between(LocalDate.now(), endDate);
        if (days >= 0) {
            return safe(contract.getMaHopDong()) + " | " + status + " | còn " + days + " ngày | thuê: " + rent;
        }
        return safe(contract.getMaHopDong()) + " | " + status + " | quá hạn " + Math.abs(days) + " ngày | thuê: " + rent;
    }

    private Contract pickBestContract(List<Contract> contracts) {
        if (contracts == null || contracts.isEmpty()) return null;

        Contract activeBest = null;
        LocalDate activeBestEnd = null;
        Contract latestAny = null;
        LocalDate latestAnyEnd = null;

        for (Contract c : contracts) {
            LocalDate end = parseDateFlex(c.getNgayKetThuc());
            boolean isActive = "ACTIVE".equalsIgnoreCase(c.getTrangThai());
            if (isActive) {
                if (activeBest == null || compareDate(end, activeBestEnd) > 0) {
                    activeBest = c;
                    activeBestEnd = end;
                }
            }
            if (latestAny == null || compareDate(end, latestAnyEnd) > 0) {
                latestAny = c;
                latestAnyEnd = end;
            }
        }
        return activeBest != null ? activeBest : latestAny;
    }

    private int compareDate(LocalDate a, LocalDate b) {
        if (a == null && b == null) return 0;
        if (a == null) return -1;
        if (b == null) return 1;
        return a.compareTo(b);
    }

    private LocalDate parseDateFlex(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        String v = value.trim();
        String[] patterns = new String[]{"yyyy-MM-dd", "dd-MM-yyyy", "yyyy/MM/dd", "dd/MM/yyyy"};
        for (String p : patterns) {
            try {
                return LocalDate.parse(v, DateTimeFormatter.ofPattern(p));
            } catch (Exception ignored) {
            }
        }
        try {
            return LocalDate.parse(v);
        } catch (Exception ignored) {
            return null;
        }
    }

    private boolean isPaidLike(String status) {
        return "PAID".equals(status) || "PARTIALLY_PAID".equals(status);
    }

    private String normalizeStatus(String status) {
        if (status == null) return "";
        if ("PENDING".equalsIgnoreCase(status)) return "UNPAID";
        return status.toUpperCase(Locale.US);
    }

    private double safeAmount(Double value) {
        return value == null ? 0 : value;
    }

    private String formatMoney(Double value) {
        if (value == null) return "N/A";
        return String.format(Locale.US, "%,d", Math.round(value));
    }

    private String formatMoney(double value) {
        return String.format(Locale.US, "%,d", Math.round(value));
    }

    private String safe(String value) {
        return value == null || value.trim().isEmpty() ? "N/A" : value;
    }

    private interface BuildingScopeCallback {
        void onReady(List<ToaNha> buildings);
    }

    private static class ActivitySeed {
        final String title;
        final String subtitle;
        final long epochMs;

        ActivitySeed(String title, String subtitle, long epochMs) {
            this.title = title;
            this.subtitle = subtitle;
            this.epochMs = epochMs;
        }
    }
}
