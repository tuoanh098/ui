package com.trohub.ui.reports;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.trohub.ui.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RoomRevenueAdapter extends RecyclerView.Adapter<RoomRevenueAdapter.RoomRevenueViewHolder> {
    private List<RoomRevenueItem> items = new ArrayList<>();

    public void setItems(List<RoomRevenueItem> items) {
        this.items = items == null ? new ArrayList<>() : items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RoomRevenueViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_room_revenue, parent, false);
        return new RoomRevenueViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RoomRevenueViewHolder holder, int position) {
        RoomRevenueItem item = items.get(position);
        holder.tvRoomTitle.setText("Phòng: " + safe(item.getRoomCode()) + " (ID " + item.getRoomId() + ")");
        holder.tvRoomBuilding.setText("Tòa: " + safe(item.getBuildingName()));
        holder.tvRoomRevenue.setText("Đã thu: " + formatMoney(item.getRevenue()) + " VND | Phải thu: " + formatMoney(item.getExpectedAmount()) + " VND");
        holder.tvRoomStatus.setText("HĐ: " + item.getInvoiceCount()
                + " | PAID: " + item.getPaidInvoices()
                + " | UNPAID: " + item.getUnpaidInvoices()
                + " | PARTIAL: " + item.getPartialInvoices()
                + " | DRAFT: " + item.getDraftInvoices()
                + " | OVERDUE: " + item.getOverdueInvoices());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private String formatMoney(double value) {
        return String.format(Locale.US, "%,d", Math.round(value));
    }

    private String safe(String value) {
        return value == null || value.trim().isEmpty() ? "N/A" : value;
    }

    static class RoomRevenueViewHolder extends RecyclerView.ViewHolder {
        TextView tvRoomTitle;
        TextView tvRoomBuilding;
        TextView tvRoomRevenue;
        TextView tvRoomStatus;

        RoomRevenueViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRoomTitle = itemView.findViewById(R.id.tvRoomRevenueTitle);
            tvRoomBuilding = itemView.findViewById(R.id.tvRoomRevenueBuilding);
            tvRoomRevenue = itemView.findViewById(R.id.tvRoomRevenueAmount);
            tvRoomStatus = itemView.findViewById(R.id.tvRoomRevenueStatus);
        }
    }
}
