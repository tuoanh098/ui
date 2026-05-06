package com.trohub.ui.auth;

import android.os.Bundle;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.trohub.ui.common.TroHubActivity;

import com.trohub.ui.R;
import com.trohub.ui.api.ApiService;
import com.trohub.ui.api.NetworkClient;
import com.trohub.ui.api.RegisterRequest;
import com.trohub.ui.api.models.Phong;
import com.trohub.ui.api.models.TaiKhoanDto;
import com.trohub.ui.common.IdLabelOption;
import com.trohub.ui.common.SelectionHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateTenantAccountActivity extends TroHubActivity {

    private EditText etUsername;
    private EditText etPassword;
    private EditText etFullName;
    private EditText etEmail;
    private EditText etPhone;
    private EditText etCccd;
    private EditText etAddress;
    private AutoCompleteTextView etRoomId;
    private Button btnCreate;
    private TextView tvResult;
    private final List<IdLabelOption> roomOptions = new ArrayList<>();

    private ApiService apiService;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_tenant_account);

        sessionManager = new SessionManager(this);
        if (!sessionManager.isAdminOrLandlord()) {
            Toast.makeText(this, "Bạn không có quyền tạo tài khoản khách thuê", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        NetworkClient.setAuthToken(sessionManager.getToken());
        apiService = NetworkClient.getRetrofitClient().create(ApiService.class);
        bindViews();
        loadRoomOptions();
        setupActions();
    }

    private void bindViews() {
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etCccd = findViewById(R.id.etCccd);
        etAddress = findViewById(R.id.etAddress);
        etRoomId = findViewById(R.id.etRoomId);
        btnCreate = findViewById(R.id.btnCreateAccount);
        tvResult = findViewById(R.id.tvResult);
    }

    private void loadRoomOptions() {
        apiService.getPhongs().enqueue(new Callback<List<Phong>>() {
            @Override
            public void onResponse(Call<List<Phong>> call, Response<List<Phong>> response) {
                roomOptions.clear();
                if (response.isSuccessful() && response.body() != null) {
                    for (Phong room : response.body()) {
                        if (room == null || room.getId() == null) continue;
                        roomOptions.add(new IdLabelOption(room.getId(),
                                safe(room.getMaPhong()) + " | " + safe(room.getTrangThai())));
                    }
                }
                SelectionHelper.bindOptions(etRoomId, roomOptions);
            }

            @Override
            public void onFailure(Call<List<Phong>> call, Throwable t) {
                roomOptions.clear();
                SelectionHelper.bindOptions(etRoomId, roomOptions);
            }
        });
    }

    private void setupActions() {
        btnCreate.setOnClickListener(v -> submitCreateAccount());
    }

    private void submitCreateAccount() {
        String username = text(etUsername);
        String password = text(etPassword);
        String fullName = text(etFullName);
        String email = text(etEmail);
        String phone = text(etPhone);
        String cccd = text(etCccd);
        String address = text(etAddress);
        String roomRaw = text(etRoomId);

        if (username.length() < 3) {
            etUsername.setError("Username tối thiểu 3 ký tự");
            return;
        }
        if (password.length() < 8) {
            etPassword.setError("Mật khẩu tối thiểu 8 ký tự");
            return;
        }
        if (fullName.isEmpty()) {
            etFullName.setError("Họ tên là bắt buộc");
            return;
        }

        Long roomId = null;
        if (!roomRaw.isEmpty()) {
            roomId = SelectionHelper.findIdByText(roomOptions, roomRaw);
            if (roomId == null) {
                etRoomId.setError("Vui lòng chọn phòng từ danh sách");
                return;
            }
        }

        RegisterRequest payload = new RegisterRequest();
        payload.setUsername(username);
        payload.setPassword(password);
        payload.setFullName(fullName);
        payload.setEmail(emptyToNull(email));
        payload.setSdt(emptyToNull(phone));
        payload.setCccd(emptyToNull(cccd));
        payload.setDiaChi(emptyToNull(address));
        payload.setSophong(roomId);

        setSubmitting(true);
        tvResult.setVisibility(View.GONE);

        apiService.register(payload).enqueue(new Callback<TaiKhoanDto>() {
            @Override
            public void onResponse(Call<TaiKhoanDto> call, Response<TaiKhoanDto> response) {
                setSubmitting(false);
                if (response.isSuccessful() && response.body() != null) {
                    TaiKhoanDto user = response.body();
                    String msg = String.format(Locale.US,
                            "Đã tạo tài khoản thành công\nTên đăng nhập: %s\nQuyền: Khách thuê",
                            safe(user.getUsername()));
                    tvResult.setText(msg);
                    tvResult.setVisibility(View.VISIBLE);
                    Toast.makeText(CreateTenantAccountActivity.this, "Tạo tài khoản thành công", Toast.LENGTH_SHORT).show();
                    clearFormAfterSuccess();
                } else {
                    String serverMessage = readError(response);
                    Toast.makeText(CreateTenantAccountActivity.this,
                            "Tạo tài khoản thất bại: " + serverMessage, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<TaiKhoanDto> call, Throwable t) {
                setSubmitting(false);
                Toast.makeText(CreateTenantAccountActivity.this,
                        "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void clearFormAfterSuccess() {
        etUsername.setText("");
        etPassword.setText("");
    }

    private void setSubmitting(boolean submitting) {
        btnCreate.setEnabled(!submitting);
        btnCreate.setText(submitting ? "Đang tạo..." : "Tạo tài khoản");
    }

    private String readError(Response<?> response) {
        if (response == null || response.errorBody() == null) {
            return "HTTP " + (response == null ? 0 : response.code());
        }
        try {
            String raw = response.errorBody().string();
            if (raw == null || raw.trim().isEmpty()) {
                return "HTTP " + response.code();
            }
            return raw;
        } catch (IOException e) {
            return "HTTP " + response.code();
        }
    }

    private String text(EditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }

    private String emptyToNull(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }

    private String safe(String value) {
        return value == null || value.trim().isEmpty() ? "N/A" : value;
    }
}
