package com.trohub.ui.guest;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.trohub.ui.R;
import com.trohub.ui.api.models.GuestEntry;

import java.util.ArrayList;
import java.util.List;

public class GuestEntriesAdapter extends RecyclerView.Adapter<GuestEntriesAdapter.GuestViewHolder> {
    private List<GuestEntry> items = new ArrayList<>();
    private final GuestActionListener actionListener;
    private final boolean canManage;

    public interface GuestActionListener {
        void onEditGuest(GuestEntry item);
        void onDeleteGuest(GuestEntry item);
    }

    public GuestEntriesAdapter(GuestActionListener actionListener, boolean canManage) {
        this.actionListener = actionListener;
        this.canManage = canManage;
    }

    public GuestEntriesAdapter() {
        this(null, false);
    }

    public void setItems(List<GuestEntry> entries) {
        this.items = entries == null ? new ArrayList<>() : entries;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public GuestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_guest_entry, parent, false);
        return new GuestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GuestViewHolder holder, int position) {
        GuestEntry item = items.get(position);
        holder.tvName.setText(item.getTen() == null ? "N/A" : item.getTen());
        holder.tvDoc.setText("CMND: " + safe(item.getCmnd()) + " | SĐT: " + safe(item.getSdt()));
        holder.tvRoom.setText("Phòng ID: " + (item.getPhongId() == null ? "N/A" : item.getPhongId()));
        holder.tvMeta.setText("Loại: " + safe(item.getLoai()) + " | Trạng thái: " + safe(item.getApprovalStatus()));
        holder.tvTime.setText("Thời gian: " + safe(item.getTimestamp()));

        boolean showActions = canManage && actionListener != null;
        holder.layoutActions.setVisibility(showActions ? View.VISIBLE : View.GONE);
        if (showActions) {
            holder.btnEdit.setOnClickListener(v -> actionListener.onEditGuest(item));
            holder.btnDelete.setOnClickListener(v -> actionListener.onDeleteGuest(item));
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private String safe(String value) {
        return value == null || value.trim().isEmpty() ? "N/A" : value;
    }

    static class GuestViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        TextView tvDoc;
        TextView tvRoom;
        TextView tvMeta;
        TextView tvTime;
        LinearLayout layoutActions;
        Button btnEdit;
        Button btnDelete;

        public GuestViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvGuestName);
            tvDoc = itemView.findViewById(R.id.tvGuestDoc);
            tvRoom = itemView.findViewById(R.id.tvGuestRoom);
            tvMeta = itemView.findViewById(R.id.tvGuestMeta);
            tvTime = itemView.findViewById(R.id.tvGuestTime);
            layoutActions = itemView.findViewById(R.id.layoutGuestActions);
            btnEdit = itemView.findViewById(R.id.btnEditGuest);
            btnDelete = itemView.findViewById(R.id.btnDeleteGuest);
        }
    }
}
