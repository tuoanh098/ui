package com.trohub.ui.billing;

import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.trohub.ui.R;
import com.trohub.ui.api.ApiService;
import com.trohub.ui.api.NetworkClient;
import com.trohub.ui.api.models.ApplyDailyFeeRequest;
import com.trohub.ui.api.models.Invoice;
import com.trohub.ui.api.models.PaymentDto;
import com.trohub.ui.api.models.PriceRequest;
import com.trohub.ui.api.models.QrRequest;
import com.trohub.ui.api.models.QrResponse;
import com.trohub.ui.api.models.RegenerateInvoicesRequest;
import com.trohub.ui.api.models.SimulateQrRequest;
import com.trohub.ui.auth.SessionManager;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BillingActivity extends AppCompatActivity implements InvoiceAdapter.InvoiceActionListener {

    private EditText etYear;
    private EditText etMonth;
    private EditText etLateFeePerDay;
    private TextView tvBillingHint;
    private TextView tvEmpty;
    private Button btnCheck;
    private Button btnGenerateSync;
    private Button btnGenerateAsync;
    private Button btnToggleQa;
    private Button btnCreatePrice;
    private View generateContainer;
    private View qaContainer;
    private RecyclerView rvList;

    private ApiService apiService;
    private SessionManager sessionManager;
    private InvoiceAdapter adapter;

    private boolean adminOrStaff;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_billing);

        sessionManager = new SessionManager(this);
        NetworkClient.setAuthToken(sessionManager.getToken());
        apiService = NetworkClient.getRetrofitClient().create(ApiService.class);
        adminOrStaff = sessionManager.hasAnyRole("ROLE_ADMIN", "ROLE_LANDLORD", "ROLE_BILLING_STAFF");

        bindViews();
        setupRecycler();
        setupDefaults();
        setupActions();

        if (!adminOrStaff) {
            generateContainer.setVisibility(View.GONE);
            btnToggleQa.setVisibility(View.GONE);
            qaContainer.setVisibility(View.GONE);
        } else {
            btnGenerateSync.setText("Tạo kỳ mới (Regenerate)");
            btnGenerateAsync.setVisibility(View.GONE);
        }

        updateBillingHint();
        loadInvoices();
    }

    private void bindViews() {
        tvBillingHint = findViewById(R.id.tvBillingHint);
        etYear = findViewById(R.id.etYear);
        etMonth = findViewById(R.id.etMonth);
        etLateFeePerDay = findViewById(R.id.etLateFeePerDay);
        tvEmpty = findViewById(R.id.tvEmpty);
        btnCheck = findViewById(R.id.btnCheck);
        btnGenerateSync = findViewById(R.id.btnGenerateSync);
        btnGenerateAsync = findViewById(R.id.btnGenerateAsync);
        btnToggleQa = findViewById(R.id.btnToggleQa);
        btnCreatePrice = findViewById(R.id.btnCreatePrice);
        generateContainer = findViewById(R.id.generateContainer);
        qaContainer = findViewById(R.id.qaContainer);
        rvList = findViewById(R.id.rvList);
    }

    private void setupRecycler() {
        rvList.setLayoutManager(new LinearLayoutManager(this));
        adapter = new InvoiceAdapter(this, adminOrStaff);
        rvList.setAdapter(adapter);
    }

    private void setupDefaults() {
        Calendar c = Calendar.getInstance();
        etYear.setText(String.valueOf(c.get(Calendar.YEAR)));
        etMonth.setText(String.valueOf(c.get(Calendar.MONTH) + 1));
    }

    private void setupActions() {
        btnCheck.setOnClickListener(v -> {
            updateBillingHint();
            if (adminOrStaff) {
                regenerateAndLoad(false);
            } else {
                loadInvoices();
            }
        });
        btnGenerateSync.setOnClickListener(v -> regenerateAndLoad(true));
        btnToggleQa.setOnClickListener(v -> {
            boolean show = qaContainer.getVisibility() != View.VISIBLE;
            qaContainer.setVisibility(show ? View.VISIBLE : View.GONE);
        });
        btnCreatePrice.setOnClickListener(v -> createPriceQA());
    }

    private void loadInvoices() {
        Integer year = parseInt(etYear.getText().toString().trim());
        Integer month = parseInt(etMonth.getText().toString().trim());
        if (year == null || month == null || month < 1 || month > 12) {
            Toast.makeText(this, "Vui lòng nhập tháng/năm hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        apiService.getInvoices(year, month).enqueue(new Callback<List<Invoice>>() {
            @Override
            public void onResponse(Call<List<Invoice>> call, Response<List<Invoice>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapter.setInvoices(response.body());
                    tvEmpty.setVisibility(response.body().isEmpty() ? View.VISIBLE : View.GONE);
                } else {
                    tvEmpty.setVisibility(View.VISIBLE);
                    Toast.makeText(BillingActivity.this, "Không tải được danh sách hóa đơn", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Invoice>> call, Throwable t) {
                tvEmpty.setVisibility(View.VISIBLE);
                Toast.makeText(BillingActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void regenerateAndLoad(boolean showSuccessToast) {
        if (!adminOrStaff) return;

        Integer year = parseInt(etYear.getText().toString().trim());
        Integer month = parseInt(etMonth.getText().toString().trim());
        if (year == null || month == null || month < 1 || month > 12) {
            Toast.makeText(this, "Vui lòng nhập tháng/năm hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        RegenerateInvoicesRequest req = new RegenerateInvoicesRequest(null, year, month);
        apiService.regenerateInvoices(req).enqueue(new Callback<List<Invoice>>() {
            @Override
            public void onResponse(Call<List<Invoice>> call, Response<List<Invoice>> response) {
                if (response.isSuccessful()) {
                    if (showSuccessToast) {
                        Toast.makeText(BillingActivity.this, "Regenerate hóa đơn thành công", Toast.LENGTH_SHORT).show();
                    }
                    loadInvoices();
                } else {
                    Toast.makeText(BillingActivity.this, "Regenerate thất bại: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Invoice>> call, Throwable t) {
                Toast.makeText(BillingActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createPriceQA() {
        PriceRequest req = new PriceRequest("ELECTRIC", 4000.0, "2026-01-01", "2099-12-31");
        apiService.createPrice(req).enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(BillingActivity.this, "Đã tạo/cập nhật đơn giá điện", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(BillingActivity.this, "Create price thất bại: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                Toast.makeText(BillingActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onPayQr(Invoice invoice) {
        if (invoice == null || invoice.getId() == null) return;
        apiService.createQrCode(new QrRequest(invoice.getId())).enqueue(new Callback<QrResponse>() {
            @Override
            public void onResponse(Call<QrResponse> call, Response<QrResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    showQrDialog(response.body(), invoice);
                } else {
                    Toast.makeText(BillingActivity.this, "Không tạo được QR", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<QrResponse> call, Throwable t) {
                Toast.makeText(BillingActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onApplyLateFee(Invoice invoice) {
        if (!adminOrStaff || invoice == null || invoice.getId() == null) return;
        int perDay = readLateFeePerDay();
        apiService.applyDailyFee(new ApplyDailyFeeRequest(invoice.getId(), perDay)).enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(BillingActivity.this, "Đã áp phí trễ hạn", Toast.LENGTH_SHORT).show();
                    loadInvoices();
                } else {
                    Toast.makeText(BillingActivity.this, "Áp phí thất bại: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                Toast.makeText(BillingActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onSimulatePaid(Invoice invoice) {
        if (!adminOrStaff || invoice == null || invoice.getId() == null) return;
        apiService.simulateInvoicePaid(invoice.getId()).enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(BillingActivity.this, "Simulate paid thành công", Toast.LENGTH_SHORT).show();
                    loadInvoices();
                } else {
                    Toast.makeText(BillingActivity.this, "Simulate paid thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                Toast.makeText(BillingActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onViewPayments(Invoice invoice) {
        if (invoice == null || invoice.getId() == null) return;
        apiService.getInvoicePayments(invoice.getId()).enqueue(new Callback<List<PaymentDto>>() {
            @Override
            public void onResponse(Call<List<PaymentDto>> call, Response<List<PaymentDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    showPaymentsDialog(invoice, response.body());
                } else {
                    Toast.makeText(BillingActivity.this, "Không tải được lịch sử thu", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<PaymentDto>> call, Throwable t) {
                Toast.makeText(BillingActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void simulateQrPayment(QrResponse qr, Invoice invoice) {
        if (qr == null || TextUtils.isEmpty(qr.getQrCode())) {
            Toast.makeText(this, "QR không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        Double amount = qr.getExpectedAmount() != null ? qr.getExpectedAmount() : invoice.getTotalAmount();
        SimulateQrRequest payload = new SimulateQrRequest(
                qr.getQrCode(),
                "txn-app-" + System.currentTimeMillis(),
                amount
        );
        apiService.simulateQrPayment(payload).enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(BillingActivity.this, "Thanh toán QR mô phỏng thành công", Toast.LENGTH_SHORT).show();
                    loadInvoices();
                } else {
                    Toast.makeText(BillingActivity.this, "Thanh toán QR thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                Toast.makeText(BillingActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showQrDialog(QrResponse qr, Invoice invoice) {
        View view = getLayoutInflater().inflate(R.layout.dialog_qr_code, null);
        ImageView qrImage = view.findViewById(R.id.qr_code_image);

        Bitmap bitmap = decodeBase64Image(qr.getQrImageDataUrl());
        if (bitmap != null) {
            qrImage.setImageBitmap(bitmap);
        } else {
            qrImage.setVisibility(View.GONE);
        }

        TextView info = new TextView(this);
        info.setPadding(24, 16, 24, 0);
        info.setText(buildQrInfo(qr, invoice));
        ((LinearLayout) view).addView(info);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("QR thanh toán - " + safeInvoiceNumber(invoice));
        builder.setView(view);
        builder.setPositiveButton("Xác nhận đã thanh toán", (dialog, which) -> simulateQrPayment(qr, invoice));
        builder.setNegativeButton("Đóng", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void showPaymentsDialog(Invoice invoice, List<PaymentDto> payments) {
        if (payments == null || payments.isEmpty()) {
            new AlertDialog.Builder(this)
                    .setTitle("Lịch sử thu - " + safeInvoiceNumber(invoice))
                    .setMessage("Hóa đơn chưa có giao dịch thanh toán.")
                    .setPositiveButton("Đóng", null)
                    .show();
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (PaymentDto p : payments) {
            sb.append("- ").append(safe(p.getPaymentDate()))
                    .append(" | ").append(formatAmount(p.getAmount())).append(" VND")
                    .append(" | ").append(safe(p.getPaymentMethod()));
            if (!TextUtils.isEmpty(p.getExternalTxnId())) {
                sb.append(" | txn: ").append(p.getExternalTxnId());
            }
            sb.append("\n");
        }

        new AlertDialog.Builder(this)
                .setTitle("Lịch sử thu - " + safeInvoiceNumber(invoice))
                .setMessage(sb.toString().trim())
                .setPositiveButton("Đóng", null)
                .show();
    }

    private int readLateFeePerDay() {
        Integer parsed = parseInt(etLateFeePerDay.getText().toString().trim());
        return parsed == null || parsed <= 0 ? 100000 : parsed;
    }

    private Bitmap decodeBase64Image(String dataUrl) {
        if (TextUtils.isEmpty(dataUrl)) return null;
        try {
            String base64 = dataUrl;
            if (dataUrl.startsWith("data:image")) {
                int comma = dataUrl.indexOf(',');
                if (comma > 0 && comma < dataUrl.length() - 1) {
                    base64 = dataUrl.substring(comma + 1);
                }
            }
            byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } catch (Exception e) {
            return null;
        }
    }

    private String buildQrInfo(QrResponse qr, Invoice invoice) {
        String expected = formatAmount(qr.getExpectedAmount() != null ? qr.getExpectedAmount() : invoice.getTotalAmount());
        StringBuilder sb = new StringBuilder();
        sb.append("Số tiền cần thanh toán: ").append(expected).append(" VND");
        if (!TextUtils.isEmpty(qr.getExpiresAt())) {
            sb.append("\nHết hạn QR: ").append(qr.getExpiresAt());
        }
        if (!TextUtils.isEmpty(qr.getQrCode())) {
            sb.append("\nMã tham chiếu QR: ").append(qr.getQrCode());
        }
        return sb.toString();
    }

    private void updateBillingHint() {
        Integer year = parseInt(etYear.getText().toString().trim());
        Integer month = parseInt(etMonth.getText().toString().trim());
        if (year == null || month == null || month < 1 || month > 12) {
            tvBillingHint.setText("Luồng billing chuẩn: bấm Tải dữ liệu/Tạo kỳ để regenerate invoice theo hợp đồng -> thanh toán QR -> áp phí trễ hạn sau grace.");
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            YearMonth ym = YearMonth.of(year, month);
            LocalDate start = ym.atDay(4);
            LocalDate grace = ym.atDay(8);
            tvBillingHint.setText("Kỳ " + month + "/" + year
                    + " | Regenerate theo hợp đồng"
                    + " | Thu từ " + start.format(DateTimeFormatter.ISO_DATE)
                    + " | Grace đến " + grace.format(DateTimeFormatter.ISO_DATE)
                    + " | quá hạn dùng nút Áp phí trễ hạn.");
            return;
        }

        tvBillingHint.setText("Kỳ " + month + "/" + year + " | Regenerate theo hợp đồng | Thu từ ngày 4, grace đến ngày 8, sau đó áp phí trễ hạn.");
    }

    private Integer parseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return null;
        }
    }

    private String safe(String value) {
        return value == null || value.trim().isEmpty() ? "N/A" : value;
    }

    private String formatAmount(Double value) {
        long amount = value == null ? 0L : Math.round(value);
        return String.format(Locale.US, "%,d", amount);
    }

    private String safeInvoiceNumber(Invoice invoice) {
        if (invoice == null) return "N/A";
        if (!TextUtils.isEmpty(invoice.getInvoiceNumber())) return invoice.getInvoiceNumber();
        return "ID " + invoice.getId();
    }
}
