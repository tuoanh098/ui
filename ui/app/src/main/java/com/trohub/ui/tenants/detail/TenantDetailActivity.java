package com.trohub.ui.tenants.detail;

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
import com.trohub.ui.api.models.Contract;
import com.trohub.ui.api.models.Invoice;
import com.trohub.ui.api.models.PaymentDto;
import com.trohub.ui.api.models.Tenant;
import com.trohub.ui.auth.SessionManager;
import com.trohub.ui.contracts.ContractsAdapter;
import com.trohub.ui.rooms.detail.RoomDetailActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TenantDetailActivity extends AppCompatActivity {

    private TextView tvTitle;
    private TextView tvTenantName;
    private TextView tvTenantMeta;
    private TextView tvTenantContact;
    private TextView tvTenantAddress;
    private TextView tvRoomLink;
    private TextView tvContractCount;
    private TextView tvPaymentCount;
    private TextView tvContractsEmpty;
    private TextView tvPaymentsEmpty;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView rvContracts;
    private RecyclerView rvPaymentHistory;

    private ApiService apiService;
    private ContractsAdapter contractsAdapter;
    private PaymentHistoryAdapter paymentHistoryAdapter;

    private Long tenantId;
    private Tenant currentTenant;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tenant_detail);

        SessionManager sessionManager = new SessionManager(this);
        NetworkClient.setAuthToken(sessionManager.getToken());
        apiService = NetworkClient.getRetrofitClient().create(ApiService.class);

        long id = getIntent().getLongExtra("TENANT_ID", -1L);
        if (id <= 0) {
            Toast.makeText(this, "Thiếu TENANT_ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        tenantId = id;

        bindViews();
        setupLists();
        setupActions();
        loadTenantDetail(true);
    }

    private void bindViews() {
        tvTitle = findViewById(R.id.tvTitle);
        tvTenantName = findViewById(R.id.tvTenantName);
        tvTenantMeta = findViewById(R.id.tvTenantMeta);
        tvTenantContact = findViewById(R.id.tvTenantContact);
        tvTenantAddress = findViewById(R.id.tvTenantAddress);
        tvRoomLink = findViewById(R.id.tvRoomLink);
        tvContractCount = findViewById(R.id.tvContractCount);
        tvPaymentCount = findViewById(R.id.tvPaymentCount);
        tvContractsEmpty = findViewById(R.id.tvContractsEmpty);
        tvPaymentsEmpty = findViewById(R.id.tvPaymentsEmpty);
        progressBar = findViewById(R.id.progressBar);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        rvContracts = findViewById(R.id.rvContracts);
        rvPaymentHistory = findViewById(R.id.rvPaymentHistory);

        tvTitle.setText("Chi tiết khách thuê");
        tvRoomLink.setPaintFlags(tvRoomLink.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
    }

    private void setupLists() {
        contractsAdapter = new ContractsAdapter();
        paymentHistoryAdapter = new PaymentHistoryAdapter();

        rvContracts.setLayoutManager(new LinearLayoutManager(this));
        rvContracts.setAdapter(contractsAdapter);
        rvPaymentHistory.setLayoutManager(new LinearLayoutManager(this));
        rvPaymentHistory.setAdapter(paymentHistoryAdapter);
    }

    private void setupActions() {
        swipeRefresh.setOnRefreshListener(() -> loadTenantDetail(false));
        tvRoomLink.setOnClickListener(v -> openRoomDetail());
    }

    private void loadTenantDetail(boolean firstLoad) {
        if (firstLoad) {
            progressBar.setVisibility(View.VISIBLE);
        }
        apiService.getTenants().enqueue(new Callback<List<Tenant>>() {
            @Override
            public void onResponse(Call<List<Tenant>> call, Response<List<Tenant>> response) {
                progressBar.setVisibility(View.GONE);
                swipeRefresh.setRefreshing(false);
                if (!(response.isSuccessful() && response.body() != null)) {
                    Toast.makeText(TenantDetailActivity.this, "Không tải được thông tin khách thuê", Toast.LENGTH_SHORT).show();
                    return;
                }

                currentTenant = null;
                for (Tenant tenant : response.body()) {
                    if (tenant.getId() != null && tenant.getId().equals(tenantId)) {
                        currentTenant = tenant;
                        break;
                    }
                }

                if (currentTenant == null) {
                    Toast.makeText(TenantDetailActivity.this, "Không tìm thấy khách thuê", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                renderTenantInfo();
                loadContracts();
                loadPaymentHistory();
            }

            @Override
            public void onFailure(Call<List<Tenant>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                swipeRefresh.setRefreshing(false);
                Toast.makeText(TenantDetailActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void renderTenantInfo() {
        tvTenantName.setText(safe(currentTenant.getHoTen()) + " (ID " + currentTenant.getId() + ")");
        tvTenantMeta.setText("CCCD: " + safe(currentTenant.getCccd())
                + " | Giới tính: " + safe(currentTenant.getGioiTinh())
                + " | Ngày sinh: " + safe(currentTenant.getNgaySinh()));
        tvTenantContact.setText("SĐT: " + safe(currentTenant.getSdt())
                + " | Tài khoản ID: " + (currentTenant.getTaiKhoanId() == null ? "N/A" : currentTenant.getTaiKhoanId()));
        tvTenantAddress.setText("Địa chỉ: " + safe(currentTenant.getDiaChi()));

        if (currentTenant.getSophong() == null) {
            tvRoomLink.setText("Phòng: N/A");
            tvRoomLink.setEnabled(false);
        } else {
            tvRoomLink.setText("Phòng: ID " + currentTenant.getSophong() + " (bấm để xem chi tiết)");
            tvRoomLink.setEnabled(true);
        }
    }

    private void loadContracts() {
        tvContractCount.setText("Hợp đồng: đang tải...");
        apiService.getContractsByTenant(tenantId).enqueue(new Callback<List<Contract>>() {
            @Override
            public void onResponse(Call<List<Contract>> call, Response<List<Contract>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    applyContracts(response.body());
                } else {
                    loadContractsFallback();
                }
            }

            @Override
            public void onFailure(Call<List<Contract>> call, Throwable t) {
                loadContractsFallback();
            }
        });
    }

    private void loadContractsFallback() {
        apiService.getContracts().enqueue(new Callback<List<Contract>>() {
            @Override
            public void onResponse(Call<List<Contract>> call, Response<List<Contract>> response) {
                List<Contract> filtered = new ArrayList<>();
                if (response.isSuccessful() && response.body() != null) {
                    for (Contract contract : response.body()) {
                        if (contract.getNguoiId() != null && contract.getNguoiId().equals(tenantId)) {
                            filtered.add(contract);
                        }
                    }
                }
                applyContracts(filtered);
            }

            @Override
            public void onFailure(Call<List<Contract>> call, Throwable t) {
                applyContracts(new ArrayList<>());
            }
        });
    }

    private void applyContracts(List<Contract> contracts) {
        List<Contract> safeList = contracts == null ? new ArrayList<>() : contracts;
        contractsAdapter.setContracts(safeList);
        tvContractCount.setText("Hợp đồng: " + safeList.size());
        tvContractsEmpty.setVisibility(safeList.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void loadPaymentHistory() {
        if (currentTenant == null) return;
        tvPaymentCount.setText("Lịch sử thanh toán: đang tải...");
        tvPaymentsEmpty.setVisibility(View.GONE);
        paymentHistoryAdapter.setItems(new ArrayList<>());

        apiService.getInvoices(null, null).enqueue(new Callback<List<Invoice>>() {
            @Override
            public void onResponse(Call<List<Invoice>> call, Response<List<Invoice>> response) {
                if (!(response.isSuccessful() && response.body() != null)) {
                    applyPaymentHistory(new ArrayList<>(), 0);
                    return;
                }

                List<Invoice> matched = new ArrayList<>();
                for (Invoice invoice : response.body()) {
                    if (invoice == null || invoice.getTenantId() == null) continue;
                    boolean byTenantId = currentTenant.getId() != null && invoice.getTenantId().equals(currentTenant.getId());
                    boolean byAccountId = currentTenant.getTaiKhoanId() != null && invoice.getTenantId().equals(currentTenant.getTaiKhoanId());
                    if (byTenantId || byAccountId) {
                        matched.add(invoice);
                    }
                }

                if (matched.isEmpty()) {
                    applyPaymentHistory(new ArrayList<>(), 0);
                    return;
                }

                List<PaymentHistoryAdapter.PaymentHistoryItem> merged = new ArrayList<>();
                int[] remaining = {matched.size()};

                for (Invoice invoice : matched) {
                    if (invoice.getId() == null) {
                        remaining[0]--;
                        if (remaining[0] == 0) {
                            applyPaymentHistory(merged, matched.size());
                        }
                        continue;
                    }

                    apiService.getInvoicePayments(invoice.getId()).enqueue(new Callback<List<PaymentDto>>() {
                        @Override
                        public void onResponse(Call<List<PaymentDto>> call, Response<List<PaymentDto>> paymentResponse) {
                            if (paymentResponse.isSuccessful() && paymentResponse.body() != null) {
                                String invoiceLabel = buildInvoiceLabel(invoice);
                                for (PaymentDto payment : paymentResponse.body()) {
                                    merged.add(new PaymentHistoryAdapter.PaymentHistoryItem(
                                            invoiceLabel,
                                            payment.getPaymentDate(),
                                            payment.getPaymentMethod(),
                                            payment.getAmount(),
                                            payment.getExternalTxnId()
                                    ));
                                }
                            }
                            remaining[0]--;
                            if (remaining[0] == 0) {
                                applyPaymentHistory(merged, matched.size());
                            }
                        }

                        @Override
                        public void onFailure(Call<List<PaymentDto>> call, Throwable t) {
                            remaining[0]--;
                            if (remaining[0] == 0) {
                                applyPaymentHistory(merged, matched.size());
                            }
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<List<Invoice>> call, Throwable t) {
                applyPaymentHistory(new ArrayList<>(), 0);
            }
        });
    }

    private void applyPaymentHistory(List<PaymentHistoryAdapter.PaymentHistoryItem> payments, int invoiceCount) {
        Collections.sort(payments, (a, b) -> compareDateDesc(a.paymentDate, b.paymentDate));
        paymentHistoryAdapter.setItems(payments);
        tvPaymentCount.setText("Lịch sử thanh toán: " + payments.size() + " giao dịch | " + invoiceCount + " hóa đơn");
        tvPaymentsEmpty.setVisibility(payments.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private int compareDateDesc(String left, String right) {
        String l = left == null ? "" : left.trim();
        String r = right == null ? "" : right.trim();
        return r.compareToIgnoreCase(l);
    }

    private String buildInvoiceLabel(Invoice invoice) {
        String invoiceNo = safe(invoice.getInvoiceNumber());
        String period = String.format(Locale.US, "%02d/%04d",
                invoice.getPeriodMonth() == null ? 0 : invoice.getPeriodMonth(),
                invoice.getPeriodYear() == null ? 0 : invoice.getPeriodYear());
        return "Hóa đơn " + invoiceNo + " | kỳ " + period + " | " + safe(invoice.getStatus());
    }

    private void openRoomDetail() {
        if (currentTenant == null || currentTenant.getSophong() == null) return;
        Intent intent = new Intent(this, RoomDetailActivity.class);
        intent.putExtra("ROOM_ID", currentTenant.getSophong());
        startActivity(intent);
    }

    private String safe(String value) {
        return value == null || value.trim().isEmpty() ? "N/A" : value;
    }
}
