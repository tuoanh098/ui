package com.trohub.ui.guest;

import android.app.AlertDialog;
import android.os.Bundle;
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

import androidx.appcompat.app.AppCompatActivity;
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
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GuestEntriesActivity extends AppCompatActivity implements GuestEntriesAdapter.GuestActionListener {

    private EditText etGuestName;
    private EditText etGuestCmnd;
    private EditText etGuestPhone;
    private AutoCompleteTextView etRoomId;
    private EditText etNote;
    private Spinner spType;
    private Button btnCreate;
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

        adapter = new GuestEntriesAdapter(this, canManageGuests);
        rvGuestEntries.setLayoutManager(new LinearLayoutManager(this));
        rvGuestEntries.setAdapter(adapter);

        btnCreate.setVisibility(canCreateRequests ? View.VISIBLE : View.GONE);
        btnCreate.setOnClickListener(v -> createGuestEntry());
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
                if (response.isSuccessful() && response.body() != null) {
                    for (Phong room : response.body()) {
                        if (room == null || room.getId() == null) continue;
                        roomOptions.add(new IdLabelOption(room.getId(),
                                safe(room.getMaPhong()) + " | " + safe(room.getTrangThai())));
                    }
                }
                SelectionHelper.bindOptions(etRoomId, roomOptions);
                resolveTenantRoom();
            }

            @Override
            public void onFailure(Call<List<Phong>> call, Throwable t) {
                roomOptions.clear();
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
                        if (tenant.getTaiKhoanId() != null && tenant.getTaiKhoanId().equals(accountId)) {
                            myRoomId = tenant.getSophong();
                            break;
                        }
                    }
                }
                if (myRoomId != null) {
                    String label = SelectionHelper.findLabelById(roomOptions, myRoomId);
                    etRoomId.setText(label.isEmpty() ? "ID " + myRoomId : label, false);
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
        payload.setSdt(sdt);
        payload.setPhongId(roomId);
        payload.setLoai(loai);
        payload.setGhiChu(note);

        btnCreate.setEnabled(false);
        apiService.createGuestEntry(payload).enqueue(new Callback<GuestEntry>() {
            @Override
            public void onResponse(Call<GuestEntry> call, Response<GuestEntry> response) {
                btnCreate.setEnabled(true);
                if (response.isSuccessful()) {
                    Toast.makeText(GuestEntriesActivity.this, "Khai báo thành công", Toast.LENGTH_SHORT).show();
                    etGuestName.setText("");
                    etGuestCmnd.setText("");
                    etGuestPhone.setText("");
                    etNote.setText("");
                    loadGuestEntries(false);
                } else {
                    Toast.makeText(GuestEntriesActivity.this, "Khai báo thất bại", Toast.LENGTH_SHORT).show();
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
        if (!canManageGuests) return;
        if (item == null || item.getId() == null) return;

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_guest_entry_form, null, false);
        EditText etName = view.findViewById(R.id.etGuestName);
        EditText etCmnd = view.findViewById(R.id.etGuestCmnd);
        EditText etPhone = view.findViewById(R.id.etGuestPhone);
        AutoCompleteTextView etRoom = view.findViewById(R.id.etGuestRoomId);
        EditText etType = view.findViewById(R.id.etGuestType);
        EditText etNote = view.findViewById(R.id.etGuestNote);

        etName.setText(safeEditable(item.getTen()));
        etCmnd.setText(safeEditable(item.getCmnd()));
        etPhone.setText(safeEditable(item.getSdt()));
        String roomLabel = SelectionHelper.findLabelById(roomOptions, item.getPhongId());
        etRoom.setText(roomLabel.isEmpty() && item.getPhongId() != null ? "ID " + item.getPhongId() : roomLabel, false);
        etType.setText(safeEditable(item.getLoai()));
        etNote.setText(safeEditable(item.getGhiChu()));
        SelectionHelper.bindOptions(etRoom, roomOptions);

        if (isTenantOnly && myRoomId != null) {
            String myRoomLabel = SelectionHelper.findLabelById(roomOptions, myRoomId);
            etRoom.setText(myRoomLabel.isEmpty() ? "ID " + myRoomId : myRoomLabel, false);
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
            etRoom.setError("Phòng ID bắt buộc");
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
        payload.setSdt(phone);
        payload.setPhongId(roomId);
        payload.setLoai(type.isEmpty() ? "IN" : type);
        payload.setGhiChu(note);
        return payload;
    }

    private String safe(String value) {
        return value == null || value.trim().isEmpty() ? "N/A" : value;
    }

    private String safeEditable(String value) {
        return value == null ? "" : value;
    }
}
