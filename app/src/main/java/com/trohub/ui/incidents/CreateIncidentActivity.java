package com.trohub.ui.incidents;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.trohub.ui.R;
import com.trohub.ui.api.ApiService;
import com.trohub.ui.api.NetworkClient;
import com.trohub.ui.api.models.Incident;
import com.trohub.ui.api.models.Phong;
import com.trohub.ui.api.models.Tenant;
import com.trohub.ui.common.IdLabelOption;
import com.trohub.ui.common.SelectionHelper;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateIncidentActivity extends AppCompatActivity {

    private Spinner spLoai;
    private AutoCompleteTextView acRoom;
    private AutoCompleteTextView acReportedBy;
    private EditText etDesc;
    private Button btnSelectImage, btnSubmit;
    private TextView tvFilePath;
    private ApiService apiService;

    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri selectedImageUri;
    private byte[] selectedImageBytes;
    private String selectedImageName;
    private final List<IdLabelOption> roomOptions = new ArrayList<>();
    private final List<Phong> roomEntities = new ArrayList<>();
    private final List<IdLabelOption> reporterOptions = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_incident);

        spLoai = findViewById(R.id.spLoai);
        acRoom = findViewById(R.id.acRoom);
        acReportedBy = findViewById(R.id.acReportedBy);
        etDesc = findViewById(R.id.etDesc);
        btnSelectImage = findViewById(R.id.btnSelectImage);
        btnSubmit = findViewById(R.id.btnSubmit);
        tvFilePath = findViewById(R.id.tvFilePath);
        apiService = NetworkClient.getRetrofitClient().create(ApiService.class);

        setupIncidentTypeSpinner();
        loadRooms();
        loadReporters();
        btnSelectImage.setOnClickListener(v -> openGallery());
        btnSubmit.setOnClickListener(v -> submitIncident());
    }

    private void setupIncidentTypeSpinner() {
        String[] types = new String[] {"DIEN", "NUOC", "THIET_BI", "KHAC"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, types);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spLoai.setAdapter(adapter);
    }

    private void loadRooms() {
        apiService.getPhongs().enqueue(new Callback<List<Phong>>() {
            @Override
            public void onResponse(Call<List<Phong>> call, Response<List<Phong>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    roomEntities.clear();
                    roomEntities.addAll(response.body());
                    roomOptions.clear();
                    for (Phong room : roomEntities) {
                        if (room == null || room.getId() == null) continue;
                        String code = room.getMaPhong() != null ? room.getMaPhong() : ("Phòng " + room.getId());
                        roomOptions.add(new IdLabelOption(room.getId(), code + " | " + safe(room.getTrangThai())));
                    }
                    SelectionHelper.bindOptions(acRoom, roomOptions);
                } else {
                    Toast.makeText(CreateIncidentActivity.this, "Không tải được danh sách phòng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Phong>> call, Throwable t) {
                Toast.makeText(CreateIncidentActivity.this, "Lỗi tải phòng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadReporters() {
        apiService.getTenants().enqueue(new Callback<List<Tenant>>() {
            @Override
            public void onResponse(Call<List<Tenant>> call, Response<List<Tenant>> response) {
                reporterOptions.clear();
                if (response.isSuccessful() && response.body() != null) {
                    for (Tenant tenant : response.body()) {
                        if (tenant == null || tenant.getId() == null) continue;
                        reporterOptions.add(new IdLabelOption(tenant.getId(),
                                safe(tenant.getHoTen()) + " | CCCD: " + safe(tenant.getCccd())));
                    }
                }
                SelectionHelper.bindOptions(acReportedBy, reporterOptions);
            }

            @Override
            public void onFailure(Call<List<Tenant>> call, Throwable t) {
                reporterOptions.clear();
                SelectionHelper.bindOptions(acReportedBy, reporterOptions);
            }
        });
    }

    private void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Chọn hình ảnh sự cố"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();

            try {
                InputStream is = getContentResolver().openInputStream(selectedImageUri);
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                int nRead;
                byte[] dataData = new byte[16384];
                while ((nRead = is.read(dataData, 0, dataData.length)) != -1) {
                    buffer.write(dataData, 0, nRead);
                }
                selectedImageBytes = buffer.toByteArray();

                Cursor cursor = getContentResolver().query(selectedImageUri, null, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    selectedImageName = cursor.getString(nameIndex);
                    cursor.close();
                }

                tvFilePath.setText(selectedImageName != null ? selectedImageName : "Đã chọn file");

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Lỗi đọc file!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void submitIncident() {
        String desc = etDesc.getText().toString();
        String selectedRoomText = acRoom.getText().toString().trim();
        String selectedReporterText = acReportedBy.getText().toString().trim();
        String loai = spLoai.getSelectedItem() != null ? spLoai.getSelectedItem().toString() : "KHAC";

        if (roomOptions.isEmpty()) {
            Toast.makeText(this, "Danh sách phòng đang trống", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedRoomText.isEmpty() || selectedReporterText.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đủ thông tin bắt buộc", Toast.LENGTH_SHORT).show();
            return;
        }

        Long roomId = SelectionHelper.findIdByText(roomOptions, selectedRoomText);
        if (roomId == null) {
            acRoom.setError("Vui lòng chọn phòng từ danh sách");
            return;
        }

        Long reportedById = SelectionHelper.findIdByText(reporterOptions, selectedReporterText);
        if (reportedById == null) {
            acReportedBy.setError("Vui lòng chọn người báo từ danh sách");
            return;
        }

        Phong room = null;
        for (Phong p : roomEntities) {
            if (p != null && p.getId() != null && p.getId().equals(roomId)) {
                room = p;
                break;
            }
        }
        if (room == null) {
            Toast.makeText(this, "Không tìm thấy phòng đã chọn", Toast.LENGTH_SHORT).show();
            return;
        }

        Incident newIncident = new Incident(loai, desc, room.getToaNhaId(), room.getId(), reportedById, "OPEN");
        Call<Incident> call = apiService.createIncident(newIncident);

        call.enqueue(new Callback<Incident>() {
            @Override
            public void onResponse(Call<Incident> call, Response<Incident> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Long incidentId = response.body().getId();
                    if (selectedImageBytes != null) {
                        uploadAttachment(incidentId, apiService);
                    } else {
                        Toast.makeText(CreateIncidentActivity.this, "Tạo sự cố thành công!", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } else {
                    Toast.makeText(CreateIncidentActivity.this, "Lỗi khi tạo sự cố", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Incident> call, Throwable t) {
                Toast.makeText(CreateIncidentActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void uploadAttachment(Long incidentId, ApiService apiService) {
        RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), selectedImageBytes);
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", selectedImageName != null ? selectedImageName : "image.jpg", requestFile);

        Call<ResponseBody> call = apiService.uploadAttachment(incidentId, body);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Toast.makeText(CreateIncidentActivity.this, "Tạo sự cố và upload ảnh thành công!", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(CreateIncidentActivity.this, "Đã tạo sự cố nhưng lỗi upload ảnh", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private String safe(String value) {
        return value == null || value.trim().isEmpty() ? "N/A" : value;
    }
}

