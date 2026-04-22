package com.trohub.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.trohub.ui.R;
import com.trohub.ui.api.ApiService;
import com.trohub.ui.api.LoginRequest;
import com.trohub.ui.api.LoginResponse;
import com.trohub.ui.api.NetworkClient;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsername;
    private EditText etPassword;
    private Button btnLogin;
    private SessionManager sessionManager;
    private boolean navigationTriggered = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        sessionManager = new SessionManager(this);

        if (sessionManager.isLoggedIn()) {
            NetworkClient.setAuthToken(sessionManager.getToken());
            navigateToMainDeferred();
            return;
        }

        btnLogin.setOnClickListener(v -> performLogin());
    }

    private void performLogin() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if(username.isEmpty() || password.isEmpty()){
            Toast.makeText(this, "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        btnLogin.setEnabled(false);
        btnLogin.setText("Đang đăng nhập...");

        ApiService apiService = NetworkClient.getRetrofitClient().create(ApiService.class);
        Call<LoginResponse> call = apiService.login(new LoginRequest(username, password));

        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if(response.isSuccessful() && response.body() != null){
                    String token = response.body().getAccessToken();
                    NetworkClient.setAuthToken(token);
                    ApiService authedApi = NetworkClient.getRetrofitClient().create(ApiService.class);
                    fetchProfileAndContinue(authedApi, token, username);
                } else {
                    resetLoginButton();
                    Toast.makeText(LoginActivity.this, "Đăng nhập thất bại!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                resetLoginButton();
                Toast.makeText(LoginActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchProfileAndContinue(ApiService apiService, String token, String fallbackUsername) {
        apiService.whoAmI().enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                String username = fallbackUsername;
                String rolesCsv = "";

                if (response.isSuccessful() && response.body() != null) {
                    Object nameObj = response.body().get("name");
                    if (nameObj instanceof String) {
                        username = (String) nameObj;
                    }

                    Object authoritiesObj = response.body().get("authorities");
                    if (authoritiesObj instanceof List) {
                        List<String> roles = new ArrayList<>();
                        for (Object item : (List<?>) authoritiesObj) {
                            if (item != null) roles.add(item.toString());
                        }
                        rolesCsv = android.text.TextUtils.join(",", roles);
                    }
                }

                sessionManager.saveSession(token, username, rolesCsv);
                Toast.makeText(LoginActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                navigateToMainDeferred();
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                sessionManager.saveSession(token, fallbackUsername, "");
                Toast.makeText(LoginActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                navigateToMainDeferred();
            }
        });
    }

    private void resetLoginButton() {
        btnLogin.setEnabled(true);
        btnLogin.setText("Đăng nhập");
    }

    private void navigateToMainDeferred() {
        if (navigationTriggered || isFinishing() || isDestroyed()) return;
        navigationTriggered = true;
        getWindow().getDecorView().post(this::goMain);
    }

    private void goMain() {
        if (isFinishing() || isDestroyed()) return;
        Intent intent = new Intent(LoginActivity.this, com.trohub.ui.MainActivity.class);
        startActivity(intent);
        finish();
    }
}

