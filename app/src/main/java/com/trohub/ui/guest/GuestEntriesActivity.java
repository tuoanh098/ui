package com.trohub.ui.guest;

import android.app.AlertDialog;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.trohub.ui.common.TroHubActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.trohub.ui.R;
import com.trohub.ui.api.ApiService;
import com.trohub.ui.api.NetworkClient;
import com.trohub.ui.api.models.GuestEntry;
import com.trohub.ui.api.models.Phong;
import com.trohub.ui.api.models.Tenant;
import com.trohub.ui.auth.SessionManager;
import com.trohub.ui.common.IdLabelOption;
import com.trohub.ui.common.SelectionHelper;

import java.util.ArrayList;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GuestEntriesActivity extends TroHubActivity implements GuestEntriesAdapter.GuestActionListener {

    private EditText etGuestName;
    private EditText etGuestCmnd;
    private EditText etGuestPhone;
    private AutoCompleteTextView etRoomId;
    private EditText etNote;
    private Spinner spType;
    private Button btnCreate;
    private Button btnSelectGuestImage;
    private TextView tvGuestImageFile;
    private RecyclerView rvGuestEntries;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private SwipeRefreshLayout swipeRefresh;

    private ApiService apiService;
    private SessionManager sessionManager;
    private GuestEntriesAdapter adapter;
    private Long accountId;
    private Long myRoomId;
    private boolean isTenantOnly;
    private boolean canManageGuests;
    private boolean canCreateRequests;
    private final List<IdLabelOption> roomOptions = new ArrayList<>();
    private final Map<Long, String> roomLabels = new HashMap<>();
    private static final int PICK_CREATE_GUEST_IMAGE = 41;
    private static final int PICK_EDIT_GUEST_IMAGE = 42;
    private SelectedImage selectedCreateImage;
    private Long pendingEditImageGuestId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guest_entries);

        apiService = NetworkClient.getRetrofitClient().create(ApiService.class);
        sessionManager = new SessionManager(this);
        accountId = sessionManager.getUserIdFromToken();
        isTenantOnly = sessionManager.hasAnyRole("ROLE_USER")
                && !sessionManager.hasAnyRole("ROLE_ADMIN", "ROLE_BILLING_STAFF", "ROLE_LANDLORD");
        canManageGuests = sessionManager.hasAnyRole("ROLE_ADMIN", "ROLE_LANDLORD");
        canCreateRequests = isTenantOnly;

        bindViews();
        setupTypeSpinner();

        adapter = new GuestEntriesAdapter(this, canManageGuests, isTenantOnly);
        adapter.setRoomLabels(roomLabels);
        rvGuestEntries.setLayoutManager(new LinearLayoutManager(this));
        rvGuestEntries.setAdapter(adapter);

        btnCreate.setVisibility(canCreateRequests ? View.VISIBLE : View.GONE);
        btnSelectGuestImage.setVisibility(canCreateRequests ? View.VISIBLE : View.GONE);
        tvGuestImageFile.setVisibility(canCreateRequests ? View.VISIBLE : View.GONE);
        btnCreate.setOnClickListener(v -> createGuestEntry());
        btnSelectGuestImage.setOnClickListener(v -> openGuestImagePicker(PICK_CREATE_GUEST_IMAGE));
        swipeRefresh.setOnRefreshListener(() -> loadGuestEntries(false));

        resolveTenantRoomAndLoad();
    }

    private void bindViews() {
        etGuestName = findViewById(R.id.etGuestName);
        etGuestCmnd = findViewById(R.id.etGuestCmnd);
        etGuestPhone = findViewById(R.id.etGuestPhone);
        etRoomId = findViewById(R.id.etRoomId);
        etNote = findViewById(R.id.etNote);
        spType = findViewById(R.id.spType);
        btnCreate = findViewById(R.id.btnCreate);
        btnSelectGuestImage = findViewById(R.id.btnSelectGuestImage);
        tvGuestImageFile = findViewById(R.id.tvGuestImageFile);
        rvGuestEntries = findViewById(R.id.rvGuestEntries);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);
        swipeRefresh = findViewById(R.id.swipeRefresh);
    }

    private void setupTypeSpinner() {
        String[] types = new String[] {"IN", "OUT"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, types);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spType.setAdapter(adapter);
    }

    private void resolveTenantRoomAndLoad() {
        apiService.getPhongs().enqueue(new Callback<List<Phong>>() {
            @Override
            public void onResponse(Call<List<Phong>> call, Response<List<Phong>> response) {
                roomOptions.clear();
                roomLabels.clear();
                if (response.isSuccessful() && response.body() != null) {
                    for (Phong room : response.body()) {
                        if (room == null || room.getId() == null) continue;
                        String roomLabel = safe(room.getMaPhong());
                        roomOptions.add(new IdLabelOption(room.getId(), roomLabel + " | " + safe(room.getTrangThai())));
                        roomLabels.put(room.getId(), roomLabel);
                    }
                }
                adapter.setRoomLabels(roomLabels);
                SelectionHelper.bindOptions(etRoomId, roomOptions);
                resolveTenantRoom();
            }

            @Override
            public void onFailure(Call<List<Phong>> call, Throwable t) {
                roomOptions.clear();
                roomLabels.clear();
                adapter.setRoomLabels(roomLabels);
                SelectionHelper.bindOptions(etRoomId, roomOptions);
                resolveTenantRoom();
            }
        });
    }

    private void resolveTenantRoom() {
        if (!isTenantOnly) {
            loadGuestEntries(true);
            return;
        }
        apiService.getTenants().enqueue(new Callback<List<Tenant>>() {
            @Override
            public void onResponse(Call<List<Tenant>> call, Response<List<Tenant>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (Tenant tenant : response.body()) {
                        boolean isMine = accountId == null
                                || (tenant.getTaiKhoanId() != null && tenant.getTaiKhoanId().equals(accountId));
                        if (isMine) {
                            myRoomId = tenant.getSophong();
                            break;
                        }
                    }
                }
                if (myRoomId != null) {
                    String label = SelectionHelper.findLabelById(roomOptions, myRoomId);
                    etRoomId.setText(label, false);
                    etRoomId.setEnabled(false);
                }
                loadGuestEntries(true);
            }

            @Override
            public void onFailure(Call<List<Tenant>> call, Throwable t) {
                loadGuestEntries(true);
            }
        });
    }

    private void createGuestEntry() {
        String ten = etGuestName.getText().toString().trim();
        String cmnd = etGuestCmnd.getText().toString().trim();
        String sdt = etGuestPhone.getText().toString().trim();
        String roomIdRaw = etRoomId.getText().toString().trim();
        String note = etNote.getText().toString().trim();
        String loai = spType.getSelectedItem() != null ? spType.getSelectedItem().toString() : "IN";

        if (ten.isEmpty() || cmnd.isEmpty() || roomIdRaw.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tên, CMND và phòng", Toast.LENGTH_SHORT).show();
            return;
        }

        Long roomId = SelectionHelper.findIdByText(roomOptions, roomIdRaw);
        if (roomId == null) {
            etRoomId.setError("Vui lòng chọn phòng từ danh sách");
            return;
        }
        if (isTenantOnly && myRoomId != null && !myRoomId.equals(roomId)) {
            Toast.makeText(this, "Bạn chỉ được khai báo khách cho phòng của mình", Toast.LENGTH_SHORT).show();
            return;
        }

        GuestEntry payload = new GuestEntry();
        payload.setTen(ten);
        payload.setCmnd(cmnd);
        payload.setSdt(blankToNull(sdt));
        payload.setPhongId(roomId);
        payload.setLoai(loai);
        payload.setGhiChu(note);

        btnCreate.setEnabled(false);
        apiService.createGuestEntry(payload).enqueue(new Callback<GuestEntry>() {
            @Override
            public void onResponse(Call<GuestEntry> call, Response<GuestEntry> response) {
                btnCreate.setEnabled(true);
                if (response.isSuccessful() && response.body() != null) {
                    GuestEntry created = response.body();
                    if (selectedCreateImage != null && created.getId() != null) {
                        uploadGuestImage(created.getId(), selectedCreateImage, () -> {
                            Toast.makeText(GuestEntriesActivity.this, "Khai báo và tải ảnh thành công", Toast.LENGTH_SHORT).show();
                            clearCreateForm();
                            loadGuestEntries(false);
                        });
                    } else {
                        Toast.makeText(GuestEntriesActivity.this, "Khai báo thành công", Toast.LENGTH_SHORT).show();
                        clearCreateForm();
                        loadGuestEntries(false);
                    }
                } else {
                    Toast.makeText(GuestEntriesActivity.this, "Khai báo thất bại: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<GuestEntry> call, Throwable t) {
                btnCreate.setEnabled(true);
                Toast.makeText(GuestEntriesActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadGuestEntries(boolean firstLoad) {
        if (firstLoad) {
            progressBar.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.GONE);
        }
        apiService.getGuestEntries().enqueue(new Callback<List<GuestEntry>>() {
            @Override
            public void onResponse(Call<List<GuestEntry>> call, Response<List<GuestEntry>> response) {
                progressBar.setVisibility(View.GONE);
                swipeRefresh.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null) {
                    List<GuestEntry> list = response.body();
                    if (isTenantOnly) {
                        List<GuestEntry> filtered = new ArrayList<>();
                        if (myRoomId != null) {
                            for (GuestEntry item : list) {
                                if (item.getPhongId() != null && item.getPhongId().equals(myRoomId)) {
                                    filtered.add(item);
                                }
                            }
                        }
                        list = filtered;
                    }
                    adapter.setItems(list);
                    boolean isEmpty = list.isEmpty();
                    tvEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
                    if (isEmpty) {
                        tvEmpty.setText("Không có dữ liệu khách");
                    }
                } else {
                    tvEmpty.setVisibility(View.VISIBLE);
                    tvEmpty.setText("Không tải được danh sách khách");
                }
            }

            @Override
            public void onFailure(Call<List<GuestEntry>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                swipeRefresh.setRefreshing(false);
                tvEmpty.setVisibility(View.VISIBLE);
                tvEmpty.setText("Không có kết nối mạng");
            }
        });
    }

    @Override
    public void onEditGuest(GuestEntry item) {
        if (!canManageGuests && !canTenantUpdate(item)) return;
        if (item == null || item.getId() == null) return;

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_guest_entry_form, null, false);
        EditText etName = view.findViewById(R.id.etGuestName);
        EditText etCmnd = view.findViewById(R.id.etGuestCmnd);
        EditText etPhone = view.findViewById(R.id.etGuestPhone);
        AutoCompleteTextView etRoom = view.findViewById(R.id.etGuestRoomId);
        EditText etType = view.findViewById(R.id.etGuestType);
        EditText etNote = view.findViewById(R.id.etGuestNote);
        Button btnSelectImage = view.findViewById(R.id.btnSelectGuestImage);
        TextView tvImageFile = view.findViewById(R.id.tvGuestImageFile);

        etName.setText(safeEditable(item.getTen()));
        etCmnd.setText(safeEditable(item.getCmnd()));
        etPhone.setText(safeEditable(item.getSdt()));
        String roomLabel = SelectionHelper.findLabelById(roomOptions, item.getPhongId());
        etRoom.setText(roomLabel, false);
        etType.setText(safeEditable(item.getLoai()));
        etNote.setText(safeEditable(item.getGhiChu()));
        int imageCount = item.getImagePaths() == null ? 0 : item.getImagePaths().size();
        tvImageFile.setText(imageCount > 0 ? "Đã lưu " + imageCount + " ảnh" : "Chưa có ảnh");
        btnSelectImage.setOnClickListener(v -> {
            pendingEditImageGuestId = item.getId();
            openGuestImagePicker(PICK_EDIT_GUEST_IMAGE);
        });
        SelectionHelper.bindOptions(etRoom, roomOptions);

        if (isTenantOnly && myRoomId != null) {
            String myRoomLabel = SelectionHelper.findLabelById(roomOptions, myRoomId);
            etRoom.setText(myRoomLabel, false);
            etRoom.setEnabled(false);
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Sửa khai báo khách")
                .setView(view)
                .setPositiveButton("Lưu", null)
                .setNegativeButton("Hủy", null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            GuestEntry payload = buildGuestPayload(etName, etCmnd, etPhone, etRoom, etType, etNote);
            if (payload == null) {
                return;
            }
            apiService.updateGuestEntry(item.getId(), payload).enqueue(new Callback<GuestEntry>() {
                @Override
                public void onResponse(Call<GuestEntry> call, Response<GuestEntry> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(GuestEntriesActivity.this, "Đã cập nhật khai báo", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        loadGuestEntries(false);
                    } else {
                        Toast.makeText(GuestEntriesActivity.this, "Cập nhật thất bại: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<GuestEntry> call, Throwable t) {
                    Toast.makeText(GuestEntriesActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }));

        dialog.show();
    }

    @Override
    public void onDeleteGuest(GuestEntry item) {
        if (!canManageGuests) return;
        if (item == null || item.getId() == null) return;
        new AlertDialog.Builder(this)
                .setTitle("Xóa khai báo")
                .setMessage("Xóa khai báo khách \"" + safe(item.getTen()) + "\"?")
                .setPositiveButton("Xóa", (dialog, which) -> apiService.deleteGuestEntry(item.getId()).enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(GuestEntriesActivity.this, "Đã xóa khai báo", Toast.LENGTH_SHORT).show();
                            loadGuestEntries(false);
                        } else {
                            Toast.makeText(GuestEntriesActivity.this, "Xóa thất bại: " + response.code(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Toast.makeText(GuestEntriesActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private GuestEntry buildGuestPayload(EditText etName, EditText etCmnd, EditText etPhone, AutoCompleteTextView etRoom, EditText etType, EditText etNote) {
        String name = etName.getText().toString().trim();
        String cmnd = etCmnd.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String roomRaw = etRoom.getText().toString().trim();
        String type = etType.getText().toString().trim();
        String note = etNote.getText().toString().trim();

        if (name.isEmpty()) {
            etName.setError("Tên khách bắt buộc");
            return null;
        }
        if (cmnd.isEmpty()) {
            etCmnd.setError("CMND/CCCD bắt buộc");
            return null;
        }
        if (roomRaw.isEmpty()) {
            etRoom.setError("Phòng bắt buộc");
            return null;
        }

        Long roomId = SelectionHelper.findIdByText(roomOptions, roomRaw);
        if (roomId == null) {
            etRoom.setError("Vui lòng chọn phòng từ danh sách");
            return null;
        }
        if (isTenantOnly && myRoomId != null && !myRoomId.equals(roomId)) {
            Toast.makeText(this, "Bạn chỉ được thao tác phòng của mình", Toast.LENGTH_SHORT).show();
            return null;
        }

        GuestEntry payload = new GuestEntry();
        payload.setTen(name);
        payload.setCmnd(cmnd);
        payload.setSdt(blankToNull(phone));
        payload.setPhongId(roomId);
        payload.setLoai(type.isEmpty() ? "IN" : type);
        payload.setGhiChu(note);
        return payload;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK || data == null || data.getData() == null) return;

        SelectedImage image = readSelectedImage(data.getData());
        if (image == null) return;

        if (requestCode == PICK_CREATE_GUEST_IMAGE) {
            selectedCreateImage = image;
            tvGuestImageFile.setText(image.name == null ? "Đã chọn ảnh" : image.name);
            return;
        }

        if (requestCode == PICK_EDIT_GUEST_IMAGE && pendingEditImageGuestId != null) {
            Long guestId = pendingEditImageGuestId;
            pendingEditImageGuestId = null;
            uploadGuestImage(guestId, image, () -> {
                Toast.makeText(GuestEntriesActivity.this, "Đã tải ảnh khách", Toast.LENGTH_SHORT).show();
                loadGuestEntries(false);
            });
        }
    }

    private void openGuestImagePicker(int requestCode) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Chọn ảnh khách"), requestCode);
    }

    private SelectedImage readSelectedImage(Uri uri) {
        try {
            InputStream is = getContentResolver().openInputStream(uri);
            if (is == null) return null;
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] data = new byte[16384];
            int nRead;
            while ((nRead = is.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            is.close();

            String name = null;
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor != null) {
                try {
                    if (cursor.moveToFirst()) {
                        int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                        if (nameIndex >= 0) name = cursor.getString(nameIndex);
                    }
                } finally {
                    cursor.close();
                }
            }
            String mimeType = getContentResolver().getType(uri);
            if (mimeType == null || mimeType.trim().isEmpty()) mimeType = "image/jpeg";
            return new SelectedImage(buffer.toByteArray(), name == null ? "guest.jpg" : name, mimeType);
        } catch (Exception e) {
            Toast.makeText(this, "Không đọc được ảnh đã chọn", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    private void uploadGuestImage(Long guestId, SelectedImage image, Runnable onSuccess) {
        if (guestId == null || image == null || image.bytes == null) return;
        RequestBody requestFile = RequestBody.create(MediaType.parse(image.mimeType), image.bytes);
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", image.name, requestFile);
        apiService.uploadGuestEntryAttachment(guestId, body).enqueue(new Callback<GuestEntry>() {
            @Override
            public void onResponse(Call<GuestEntry> call, Response<GuestEntry> response) {
                if (response.isSuccessful()) {
                    if (onSuccess != null) onSuccess.run();
                } else {
                    Toast.makeText(GuestEntriesActivity.this, "Tải ảnh khách thất bại: " + response.code(), Toast.LENGTH_SHORT).show();
                    loadGuestEntries(false);
                }
            }

            @Override
            public void onFailure(Call<GuestEntry> call, Throwable t) {
                Toast.makeText(GuestEntriesActivity.this, "Lỗi tải ảnh: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                loadGuestEntries(false);
            }
        });
    }

    private void clearCreateForm() {
        etGuestName.setText("");
        etGuestCmnd.setText("");
        etGuestPhone.setText("");
        etNote.setText("");
        selectedCreateImage = null;
        tvGuestImageFile.setText("Chưa chọn ảnh");
    }

    private String blankToNull(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }

    private boolean canTenantUpdate(GuestEntry item) {
        return isTenantOnly
                && item != null
                && item.getApprovalStatus() != null
                && "NEED_INFO".equalsIgnoreCase(item.getApprovalStatus().trim());
    }

    private String safe(String value) {
        return value == null || value.trim().isEmpty() ? "N/A" : value;
    }

    private String safeEditable(String value) {
        return value == null ? "" : value;
    }

    private static class SelectedImage {
        final byte[] bytes;
        final String name;
        final String mimeType;

        SelectedImage(byte[] bytes, String name, String mimeType) {
            this.bytes = bytes;
            this.name = name;
            this.mimeType = mimeType;
        }
    }
}
