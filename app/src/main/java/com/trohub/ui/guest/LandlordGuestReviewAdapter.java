package com.trohub.ui.guest;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.trohub.ui.R;
import com.trohub.ui.api.models.GuestEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LandlordGuestReviewAdapter extends RecyclerView.Adapter<LandlordGuestReviewAdapter.VH> {

    public interface ReviewActionListener {
        void onApprove(GuestEntry item);
        void onRequestInfo(GuestEntry item);
        void onReject(GuestEntry item);
    }

    private final ReviewActionListener listener;
    private final Map<Long, String> roomCodeById;
    private List<GuestEntry> items = new ArrayList<>();

    public LandlordGuestReviewAdapter(ReviewActionListener listener, Map<Long, String> roomCodeById) {
        this.listener = listener;
        this.roomCodeById = roomCodeById;
    }

    public void setItems(List<GuestEntry> items) {
        this.items = items == null ? new ArrayList<>() : items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_guest_review, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        GuestEntry item = items.get(position);
        holder.tvName.setText(safe(item.getTen()));
        holder.tvDoc.setText("CCCD/CMND: " + safe(item.getCmnd()) + " | SĐT: " + safe(item.getSdt()));
        String roomCode = item.getPhongId() == null ? "N/A" : roomCodeById.get(item.getPhongId());
        if (roomCode == null || roomCode.trim().isEmpty()) {
            roomCode = item.getPhongId() == null ? "N/A" : ("ID " + item.getPhongId());
        }
        holder.tvRoom.setText("Phòng: " + roomCode);
        holder.tvMeta.setText("Loại: " + safe(item.getLoai()) + " | Trạng thái: " + safe(item.getApprovalStatus()));
        holder.tvNote.setText("Yêu cầu/Ghi chú: " + safe(item.getGhiChu()));
        holder.tvTime.setText("Thời gian gửi: " + safe(item.getTimestamp()));

        String status = safe(item.getApprovalStatus()).toUpperCase();
        boolean isClosed = "APPROVED".equals(status) || "REJECTED".equals(status);
        int actionVisibility = isClosed ? View.GONE : View.VISIBLE;
        holder.btnApprove.setVisibility(actionVisibility);
        holder.btnNeedInfo.setVisibility(actionVisibility);
        holder.btnReject.setVisibility(actionVisibility);

        if (isClosed) {
            holder.btnApprove.setOnClickListener(null);
            holder.btnNeedInfo.setOnClickListener(null);
            holder.btnReject.setOnClickListener(null);
        } else {
            holder.btnApprove.setOnClickListener(v -> listener.onApprove(item));
            holder.btnNeedInfo.setOnClickListener(v -> listener.onRequestInfo(item));
            holder.btnReject.setOnClickListener(v -> listener.onReject(item));
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private String safe(String value) {
        return value == null || value.trim().isEmpty() ? "N/A" : value;
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvName;
        TextView tvDoc;
        TextView tvRoom;
        TextView tvMeta;
        TextView tvNote;
        TextView tvTime;
        Button btnApprove;
        Button btnNeedInfo;
        Button btnReject;

        VH(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvGuestName);
            tvDoc = itemView.findViewById(R.id.tvGuestDoc);
            tvRoom = itemView.findViewById(R.id.tvGuestRoom);
            tvMeta = itemView.findViewById(R.id.tvGuestMeta);
            tvNote = itemView.findViewById(R.id.tvGuestNote);
            tvTime = itemView.findViewById(R.id.tvGuestTime);
            btnApprove = itemView.findViewById(R.id.btnApproveGuest);
            btnNeedInfo = itemView.findViewById(R.id.btnNeedInfoGuest);
            btnReject = itemView.findViewById(R.id.btnRejectGuest);
        }
    }
}
