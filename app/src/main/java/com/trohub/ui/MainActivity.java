package com.trohub.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
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

import java.io.InputStream;
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
    private final List<String> overdueRoomLines = new ArrayList<>();
    private final List<RecentActivityItem> allRecentActivityItems = new ArrayList<>();
    private boolean recentExpanded = false;
    private Button btnShowMoreRecent;

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
        boolean canUseLandlordDashboard = sessionManager.hasAnyRole("ROLE_ADMIN", "ROLE_LANDLORD", "ROLE_BILLING_STAFF");
        isTenantOnly = !canUseLandlordDashboard;

        setContentView(R.layout.activity_main);

        TextView tvUser = findViewById(R.id.tvUser);
        TextView tvRole = findViewById(R.id.tvRole);
        TextView tvCurrentMonth = findViewById(R.id.tvCurrentMonth);
        LinearLayout tenantSummarySection = findViewById(R.id.tenantSummarySection);
        LinearLayout landlordOverviewSection = findViewById(R.id.landlordOverviewSection);

        TextView tvLandlordInfo = findViewById(R.id.tvLandlordInfo);
        TextView tvContractInfo = findViewById(R.id.tvContractInfo);
        TextView tvRoomInfo = findViewById(R.id.tvRoomInfo);
        TextView tvBuildingInfo = findViewById(R.id.tvBuildingInfo);

        TextView tvOverviewRevenue = findViewById(R.id.tvOverviewRevenue);
        TextView tvOverviewBuildings = findViewById(R.id.tvOverviewBuildings);
        TextView tvOverviewRooms = findViewById(R.id.tvOverviewRooms);
        TextView tvOverviewTenants = findViewById(R.id.tvOverviewTenants);
        TextView tvOverviewPaid = findViewById(R.id.tvOverviewPaid);
        TextView tvOverviewOverdue = findViewById(R.id.tvOverviewOverdue);
        TextView tvOverviewIncidents = findViewById(R.id.tvOverviewIncidents);
        TextView tvOverviewGuestRequests = findViewById(R.id.tvOverviewGuestRequests);
        TextView tvRecentEmpty = findViewById(R.id.tvRecentEmpty);
        RecyclerView rvRecent = findViewById(R.id.rvRecentActivities);
        LinearLayout alertOverdue = findViewById(R.id.alertOverdue);
        btnShowMoreRecent = findViewById(R.id.btnShowMoreRecent);

        GridLayout gridLandlordFunctions = findViewById(R.id.gridLandlordFunctions);
        GridLayout gridTenantFunctions = findViewById(R.id.gridTenantFunctions);
        Button btnLogout = findViewById(R.id.btnLogout);
        Button btnLogoutVisible = findViewById(R.id.btnLogoutVisible);
        boolean canCreateTenantAccount = sessionManager.isAdminOrLandlord();

        rvRecent.setLayoutManager(new LinearLayoutManager(this));
        recentActivityAdapter = new RecentActivityAdapter();
        rvRecent.setAdapter(recentActivityAdapter);
        btnShowMoreRecent.setOnClickListener(v -> {
            recentExpanded = !recentExpanded;
            applyRecentActivityLimit();
        });

        tvUser.setText("Chào, " + sessionManager.getUsername());
        List<String> roles = sessionManager.getRoles();
        String roleLabel = roles.isEmpty() ? "ROLE_USER" : TextUtils.join(", ", roles);
        tvRole.setText("Vai trò: " + roleLabel);
        tvRole.setVisibility(View.GONE);
        LocalDate currentDate = LocalDate.now();
        tvCurrentMonth.setText("Tháng " + currentDate.getMonthValue() + ", " + currentDate.getYear());

        if (isTenantOnly) {
            landlordOverviewSection.setVisibility(View.GONE);
            tenantSummarySection.setVisibility(View.VISIBLE);
            setupTenantFunctionGrid(gridTenantFunctions);

            loadTenantSummary(tvLandlordInfo, tvContractInfo, tvRoomInfo, tvBuildingInfo);
        } else {
            tenantSummarySection.setVisibility(View.GONE);
            landlordOverviewSection.setVisibility(View.VISIBLE);
            setupLandlordFunctionGrid(gridLandlordFunctions, canCreateTenantAccount);

            loadLandlordOverview(
                    tvOverviewRevenue,
                    tvOverviewBuildings,
                    tvOverviewRooms,
                    tvOverviewTenants,
                    tvOverviewPaid,
                    tvOverviewOverdue,
                    tvOverviewIncidents,
                    tvOverviewGuestRequests,
                    tvRecentEmpty
            );
        }

        alertOverdue.setOnClickListener(v -> showOverdueRoomsDialog());

        btnLogout.setOnClickListener(v -> {
            NetworkClient.setAuthToken(null);
            sessionManager.clear();
            goLogin();
        });
        btnLogoutVisible.setOnClickListener(v -> {
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

    private void setupLandlordFunctionGrid(GridLayout grid, boolean canCreateTenantAccount) {
        grid.removeAllViews();
        addFunctionGridItem(grid, "P", "Hồ sơ", "IconHoSo.png", ProfileActivity.class);
        addFunctionGridItem(grid, "B", "Tòa nhà", "IconToaNha.png", BuildingsActivity.class);
        addFunctionGridItem(grid, "R", "Phòng", "IconPhong.png", RoomsActivity.class);
        addFunctionGridItem(grid, "T", "Khách thuê", "IconKhachThue.png", TenantsActivity.class);
        addFunctionGridItem(grid, "C", "Hợp đồng", "IconHopDong.png", ContractsActivity.class);
        addFunctionGridItem(grid, "H", "Hóa đơn", "IconHoaDon.png", BillingActivity.class);
        addFunctionGridItem(grid, "S", "Báo cáo", "IconBaoCao.png", ReportsActivity.class);
        addFunctionGridItem(grid, "I", "Sự cố", "IconSuCo.png", IncidentsActivity.class);
        addFunctionGridItem(grid, "G", "Duyệt khách", "IconDuyetKhach.png", LandlordGuestReviewActivity.class);
        if (canCreateTenantAccount) {
            addFunctionGridItem(grid, "+", "Tạo tài khoản", "IconHoSo.png", CreateTenantAccountActivity.class);
        }
    }

    private void setupTenantFunctionGrid(GridLayout grid) {
        grid.removeAllViews();
        addFunctionGridItem(grid, "P", "Hồ sơ", "IconHoSo.png", ProfileActivity.class);
        addFunctionGridItem(grid, "R", "Phòng của tôi", "IconToaNha.png", RoomsActivity.class);
        addFunctionGridItem(grid, "C", "Hợp đồng", "IconHopDong.png", ContractsActivity.class);
        addFunctionGridItem(grid, "H", "Thanh toán", "IconHoaDon.png", BillingActivity.class);
        addFunctionGridItem(grid, "I", "Báo sự cố", "IconSuCo.png", IncidentsActivity.class);
        addFunctionGridItem(grid, "G", "Khách ra/vào", "IconDuyetKhach.png", GuestEntriesActivity.class);
    }

    private void addFunctionGridItem(GridLayout grid, String iconText, String titleText, String logoFileName, Class<?> activityClass) {
        View view = LayoutInflater.from(this).inflate(R.layout.item_function_grid, grid, false);
        TextView icon = view.findViewById(R.id.tvFunctionIcon);
        ImageView logo = view.findViewById(R.id.ivFunctionIcon);
        TextView title = view.findViewById(R.id.tvFunctionTitle);
        icon.setText(iconText);
        if (!applyCategoryLogo(logo, logoFileName)) {
            logo.setVisibility(View.GONE);
            icon.setVisibility(View.VISIBLE);
        }
        title.setText(titleText);
        view.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, activityClass)));

        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = 0;
        params.height = dp(104);
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        params.setMargins(dp(4), dp(4), dp(4), dp(4));
        view.setLayoutParams(params);
        grid.addView(view);
    }

    private boolean applyCategoryLogo(ImageView logo, String fileName) {
        if (logo == null || fileName == null || fileName.trim().isEmpty()) return false;
        try (InputStream input = getAssets().open("category_logos/" + fileName)) {
            Bitmap bitmap = BitmapFactory.decodeStream(input);
            if (bitmap == null) return false;
            logo.setImageBitmap(bitmap);
            logo.setVisibility(View.VISIBLE);
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    private void loadLandlordOverview(
            TextView tvOverviewRevenue,
            TextView tvOverviewBuildings,
            TextView tvOverviewRooms,
            TextView tvOverviewTenants,
            TextView tvOverviewPaid,
            TextView tvOverviewOverdue,
            TextView tvOverviewIncidents,
            TextView tvOverviewGuestRequests,
            TextView tvRecentEmpty
    ) {
        LocalDate now = LocalDate.now();
        int year = now.getYear();
        int month = now.getMonthValue();

        tvOverviewRevenue.setText("Đang tải...");
        tvOverviewBuildings.setText("...");
        tvOverviewRooms.setText("...");
        tvOverviewTenants.setText("...");
        tvOverviewPaid.setText("...");
        tvOverviewOverdue.setText("Đang tải phòng chưa thanh toán...");
        tvOverviewIncidents.setText("...");
        tvOverviewGuestRequests.setText("...");
        tvRecentEmpty.setVisibility(View.GONE);

        apiService.getBuildings().enqueue(new Callback<List<ToaNha>>() {
            @Override
            public void onResponse(Call<List<ToaNha>> call, Response<List<ToaNha>> response) {
                List<ToaNha> allBuildings = response.isSuccessful() && response.body() != null ? response.body() : new ArrayList<>();
                applyBuildingScope(allBuildings, scopedBuildings -> continueLandlordOverview(
                        scopedBuildings,
                        year,
                        month,
                        tvOverviewRevenue,
                        tvOverviewBuildings,
                        tvOverviewRooms,
                        tvOverviewTenants,
                        tvOverviewPaid,
                        tvOverviewOverdue,
                        tvOverviewIncidents,
                        tvOverviewGuestRequests,
                        tvRecentEmpty
                ));
            }

            @Override
            public void onFailure(Call<List<ToaNha>> call, Throwable t) {
                tvOverviewRevenue.setText("Không tải được");
                tvOverviewBuildings.setText("N/A");
                tvOverviewRooms.setText("N/A");
                tvOverviewTenants.setText("N/A");
                tvOverviewPaid.setText("N/A");
                tvOverviewOverdue.setText("Không tải được dữ liệu trễ hạn");
                tvOverviewIncidents.setText("N/A");
                tvOverviewGuestRequests.setText("N/A");
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
            TextView tvOverviewBuildings,
            TextView tvOverviewRooms,
            TextView tvOverviewTenants,
            TextView tvOverviewPaid,
            TextView tvOverviewOverdue,
            TextView tvOverviewIncidents,
            TextView tvOverviewGuestRequests,
            TextView tvRecentEmpty
    ) {
        tvOverviewBuildings.setText(String.valueOf(scopedBuildings.size()));
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

                tvOverviewRooms.setText(String.valueOf(scopedRooms.size()));
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
                                Map<Long, Long> roomByTenantId = mapTenantToRoom(tenants, contracts, roomById);
                                int tenantCount = 0;
                                for (Long roomId : roomByTenantId.values()) {
                                    if (roomId != null && scopedRoomIds.contains(roomId)) tenantCount++;
                                }
                                tvOverviewTenants.setText(String.valueOf(tenantCount));

                                apiService.getInvoices(year, month).enqueue(new Callback<List<Invoice>>() {
                                    @Override
                                    public void onResponse(Call<List<Invoice>> call, Response<List<Invoice>> invoiceResp) {
                                        List<Invoice> invoices = invoiceResp.isSuccessful() && invoiceResp.body() != null ? invoiceResp.body() : new ArrayList<>();
                                        double revenue = 0;
                                        int paidCount = 0;
                                        java.util.LinkedHashMap<Long, String> unpaidRooms = new java.util.LinkedHashMap<>();
                                        overdueRoomLines.clear();

                                        for (Invoice invoice : invoices) {
                                            Long roomId = invoice.getRoomId();
                                            if (roomId == null && invoice.getTenantId() != null) {
                                                roomId = roomByTenantId.get(invoice.getTenantId());
                                            }
                                            if (roomId == null || !scopedRoomIds.contains(roomId)) continue;

                                            String status = normalizeStatus(invoice.getStatus());
                                            if (isPaidLike(status)) {
                                                revenue += safeAmount(invoice.getTotalAmount());
                                                paidCount++;
                                            }
                                            if (isUnpaidLike(status)) {
                                                Phong room = roomById.get(roomId);
                                                String roomCode = room == null ? safe(invoice.getRoomCode()) : safe(room.getMaPhong());
                                                String line = "Phòng " + roomCode
                                                        + " | " + safe(invoice.getTenantName())
                                                        + " | " + safe(invoice.getInvoiceNumber())
                                                        + " | " + statusLabel(status)
                                                        + " | " + formatMoney(safeAmount(invoice.getTotalAmount())) + " VND";
                                                unpaidRooms.put(roomId, "Phòng " + roomCode);
                                                overdueRoomLines.add(line);
                                            }
                                        }

                                        tvOverviewRevenue.setText(formatMoney(revenue) + " VND");
                                        tvOverviewPaid.setText(String.valueOf(paidCount));
                                        tvOverviewOverdue.setText(unpaidRooms.size() + " phòng chưa thanh toán trong tháng " + month + "/" + year);

                                        loadRecentActivities(
                                                invoices,
                                                roomById,
                                                roomByTenantId,
                                                scopedRoomIds,
                                                tvRecentEmpty,
                                                tvOverviewIncidents,
                                                tvOverviewGuestRequests,
                                                month,
                                                year
                                        );
                                    }

                                    @Override
                                    public void onFailure(Call<List<Invoice>> call, Throwable t) {
                                        tvOverviewRevenue.setText("Không tải được");
                                        tvOverviewPaid.setText("N/A");
                                        tvOverviewOverdue.setText("Không tải được dữ liệu trễ hạn");
                                        tvRecentEmpty.setVisibility(View.VISIBLE);
                                        tvRecentEmpty.setText("Không tải được hoạt động gần đây");
                                    }
                                });
                            }

                            @Override
                            public void onFailure(Call<List<Contract>> call, Throwable t) {
                                tvOverviewRevenue.setText("Không tải được");
                                tvOverviewTenants.setText("N/A");
                                tvOverviewPaid.setText("N/A");
                                tvOverviewOverdue.setText("Không tải được dữ liệu trễ hạn");
                            }
                        });
                    }

                    @Override
                    public void onFailure(Call<List<Tenant>> call, Throwable t) {
                        tvOverviewRevenue.setText("Không tải được");
                        tvOverviewTenants.setText("N/A");
                        tvOverviewPaid.setText("N/A");
                        tvOverviewOverdue.setText("Không tải được dữ liệu trễ hạn");
                    }
                });
            }

            @Override
            public void onFailure(Call<List<Phong>> call, Throwable t) {
                tvOverviewRooms.setText("N/A");
                tvOverviewRevenue.setText("Không tải được");
                tvOverviewTenants.setText("N/A");
                tvOverviewPaid.setText("N/A");
                tvOverviewOverdue.setText("Không tải được dữ liệu trễ hạn");
            }
        });
    }

    private void applyBuildingScope(List<ToaNha> allBuildings, BuildingScopeCallback callback) {
        callback.onReady(allBuildings);
    }

    private Map<Long, Long> mapTenantToRoom(List<Tenant> tenants, List<Contract> contracts, Map<Long, Phong> roomById) {
        Map<Long, Long> result = new HashMap<>();
        for (Tenant tenant : tenants) {
            if (tenant.getId() == null) continue;
            Long roomId = findRoomForTenant(tenant, contracts);
            if (roomId != null && roomById.containsKey(roomId)) {
                result.put(tenant.getId(), roomId);
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
            Map<Long, Long> roomByTenantId,
            Set<Long> scopedRoomIds,
            TextView tvRecentEmpty,
            TextView tvOverviewIncidents,
            TextView tvOverviewGuestRequests,
            int month,
            int year
    ) {
        List<ActivitySeed> seeds = new ArrayList<>();

        for (Invoice invoice : invoices) {
            Long roomId = invoice.getRoomId();
            if (roomId == null && invoice.getTenantId() != null) {
                roomId = roomByTenantId.get(invoice.getTenantId());
            }
            if (roomId == null) continue;
            if (!scopedRoomIds.contains(roomId)) continue;
            Phong room = roomById.get(roomId);
            String roomCode = room == null ? safe(invoice.getRoomCode()) : safe(room.getMaPhong());
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
                int openIncidentCount = 0;
                if (response.isSuccessful() && response.body() != null) {
                    for (Incident incident : response.body()) {
                        if (incident.getPhongId() != null && scopedRoomIds.contains(incident.getPhongId())) {
                            if (!"RESOLVED".equalsIgnoreCase(safe(incident.getStatus()))
                                    && !"DONE".equalsIgnoreCase(safe(incident.getStatus()))) {
                                openIncidentCount++;
                            }
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
                tvOverviewIncidents.setText(String.valueOf(openIncidentCount));
                loadGuestActivities(seeds, roomById, scopedRoomIds, tvRecentEmpty, tvOverviewGuestRequests);
            }

            @Override
            public void onFailure(Call<List<Incident>> call, Throwable t) {
                tvOverviewIncidents.setText("N/A");
                loadGuestActivities(seeds, roomById, scopedRoomIds, tvRecentEmpty, tvOverviewGuestRequests);
            }
        });
    }

    private void loadGuestActivities(
            List<ActivitySeed> seeds,
            Map<Long, Phong> roomById,
            Set<Long> scopedRoomIds,
            TextView tvRecentEmpty,
            TextView tvOverviewGuestRequests
    ) {
        apiService.getGuestEntries().enqueue(new Callback<List<GuestEntry>>() {
            @Override
            public void onResponse(Call<List<GuestEntry>> call, Response<List<GuestEntry>> response) {
                int pendingGuests = 0;
                if (response.isSuccessful() && response.body() != null) {
                    for (GuestEntry entry : response.body()) {
                        if (entry.getPhongId() != null && scopedRoomIds.contains(entry.getPhongId())) {
                            String approvalStatus = safe(entry.getApprovalStatus());
                            if ("PENDING".equalsIgnoreCase(approvalStatus) || "NEED_INFO".equalsIgnoreCase(approvalStatus)) {
                                pendingGuests++;
                            }
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
                tvOverviewGuestRequests.setText(String.valueOf(pendingGuests));
                publishRecentActivities(seeds, tvRecentEmpty);
            }

            @Override
            public void onFailure(Call<List<GuestEntry>> call, Throwable t) {
                tvOverviewGuestRequests.setText("N/A");
                publishRecentActivities(seeds, tvRecentEmpty);
            }
        });
    }

    private void publishRecentActivities(List<ActivitySeed> seeds, TextView tvRecentEmpty) {
        Collections.sort(seeds, (a, b) -> Long.compare(b.epochMs, a.epochMs));
        allRecentActivityItems.clear();
        int limit = Math.min(30, seeds.size());
        for (int i = 0; i < limit; i++) {
            ActivitySeed seed = seeds.get(i);
            allRecentActivityItems.add(new RecentActivityItem(seed.title, seed.subtitle, toRelativeLabel(seed.epochMs)));
        }
        recentExpanded = false;
        applyRecentActivityLimit();
        tvRecentEmpty.setVisibility(allRecentActivityItems.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void applyRecentActivityLimit() {
        int limit = recentExpanded ? allRecentActivityItems.size() : Math.min(5, allRecentActivityItems.size());
        recentActivityAdapter.setItems(new ArrayList<>(allRecentActivityItems.subList(0, limit)));
        if (btnShowMoreRecent != null) {
            boolean hasMoreThanInitial = allRecentActivityItems.size() > 5;
            btnShowMoreRecent.setVisibility(hasMoreThanInitial ? View.VISIBLE : View.GONE);
            btnShowMoreRecent.setText(recentExpanded ? "Thu gọn" : "Xem thêm hoạt động");
        }
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

    private boolean isUnpaidLike(String status) {
        return "UNPAID".equals(status)
                || "OVERDUE".equals(status)
                || "DRAFT".equals(status)
                || "PENDING".equals(status)
                || "PARTIALLY_PAID".equals(status);
    }

    private String normalizeStatus(String status) {
        if (status == null) return "";
        if ("PENDING".equalsIgnoreCase(status)) return "UNPAID";
        return status.toUpperCase(Locale.US);
    }

    private String statusLabel(String status) {
        if ("DRAFT".equals(status)) return "nháp";
        if ("UNPAID".equals(status)) return "chưa thanh toán";
        if ("PARTIALLY_PAID".equals(status)) return "thanh toán một phần";
        if ("OVERDUE".equals(status)) return "trễ hạn";
        return status;
    }

    private void showOverdueRoomsDialog() {
        if (overdueRoomLines.isEmpty()) {
            new AlertDialog.Builder(this)
                    .setTitle("Phòng chưa thanh toán")
                    .setMessage("Không có phòng chưa thanh toán trong kỳ hiện tại.")
                    .setPositiveButton("Đóng", null)
                    .show();
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (String line : overdueRoomLines) {
            sb.append("- ").append(line).append("\n");
        }
        new AlertDialog.Builder(this)
                .setTitle("Phòng chưa thanh toán")
                .setMessage(sb.toString().trim())
                .setPositiveButton("Đóng", null)
                .setNegativeButton("Mở hóa đơn", (dialog, which) -> startActivity(new Intent(MainActivity.this, BillingActivity.class)))
                .show();
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

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
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
