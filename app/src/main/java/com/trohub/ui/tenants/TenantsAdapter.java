package com.trohub.ui.tenants;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.trohub.ui.R;
import com.trohub.ui.api.models.Tenant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TenantsAdapter extends RecyclerView.Adapter<TenantsAdapter.TenantViewHolder> {

    private List<Tenant> tenantList = new ArrayList<>();
    private final TenantActionListener actionListener;
    private final boolean canManage;
    private TenantItemClickListener itemClickListener;
    private Map<Long, String> roomLabels = new HashMap<>();

    public interface TenantActionListener {
        void onEditTenant(Tenant tenant);
        void onDeleteTenant(Tenant tenant);
    }

    public interface TenantItemClickListener {
        void onTenantClick(Tenant tenant);
    }

    public TenantsAdapter(TenantActionListener listener, boolean canManage) {
        this.actionListener = listener;
        this.canManage = canManage;
    }

    public TenantsAdapter() {
        this(null, false);
    }

    public void setTenants(List<Tenant> tenants) {
        this.tenantList = tenants == null ? new ArrayList<>() : tenants;
        notifyDataSetChanged();
    }

    public void setRoomLabels(Map<Long, String> labels) {
        this.roomLabels = labels == null ? new HashMap<>() : new HashMap<>(labels);
        notifyDataSetChanged();
    }

    public void setTenantItemClickListener(TenantItemClickListener listener) {
        this.itemClickListener = listener;
    }

    @NonNull
    @Override
    public TenantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tenant, parent, false);
        return new TenantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TenantViewHolder holder, int position) {
        Tenant tenant = tenantList.get(position);
        holder.tvTenantName.setText(tenant.getHoTen() != null ? tenant.getHoTen() : "N/A");
        holder.tvTenantPhone.setText("SĐT: " + (tenant.getSdt() != null ? tenant.getSdt() : ""));
        holder.tvTenantIdCard.setText("CCCD: " + (tenant.getCccd() != null ? tenant.getCccd() : ""));
        holder.tvTenantRoom.setText("Phòng: " + labelOrUnset(roomLabels, tenant.getSophong()));

        boolean showActions = canManage && actionListener != null;
        holder.layoutActions.setVisibility(showActions ? View.VISIBLE : View.GONE);
        if (showActions) {
            holder.btnEdit.setOnClickListener(v -> actionListener.onEditTenant(tenant));
            holder.btnDelete.setOnClickListener(v -> actionListener.onDeleteTenant(tenant));
        }

        holder.itemView.setOnClickListener(v -> {
            if (itemClickListener != null) {
                itemClickListener.onTenantClick(tenant);
            }
        });
    }

    @Override
    public int getItemCount() {
        return tenantList.size();
    }

    static class TenantViewHolder extends RecyclerView.ViewHolder {
        TextView tvTenantName;
        TextView tvTenantPhone;
        TextView tvTenantIdCard;
        TextView tvTenantRoom;
        LinearLayout layoutActions;
        Button btnEdit;
        Button btnDelete;

        public TenantViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTenantName = itemView.findViewById(R.id.tvTenantName);
            tvTenantPhone = itemView.findViewById(R.id.tvTenantPhone);
            tvTenantIdCard = itemView.findViewById(R.id.tvTenantIdCard);
            tvTenantRoom = itemView.findViewById(R.id.tvTenantRoom);
            layoutActions = itemView.findViewById(R.id.layoutTenantActions);
            btnEdit = itemView.findViewById(R.id.btnEditTenant);
            btnDelete = itemView.findViewById(R.id.btnDeleteTenant);
        }
    }

    private String labelOrUnset(Map<Long, String> labels, Long id) {
        if (id == null) return "Chưa gán phòng";
        String label = labels == null ? null : labels.get(id);
        return label == null || label.trim().isEmpty() ? "Chưa có tên phòng" : label;
    }
}

