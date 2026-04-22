package com.trohub.ui.contracts;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.trohub.ui.R;
import com.trohub.ui.api.models.Contract;

import java.util.ArrayList;
import java.util.List;

public class ContractsAdapter extends RecyclerView.Adapter<ContractsAdapter.ContractViewHolder> {

    private List<Contract> contractList = new ArrayList<>();
    private final ContractActionListener actionListener;
    private final boolean canManage;

    public interface ContractActionListener {
        void onEditContract(Contract contract);
    }

    public ContractsAdapter(ContractActionListener listener, boolean canManage) {
        this.actionListener = listener;
        this.canManage = canManage;
    }

    public ContractsAdapter() {
        this(null, false);
    }

    public void setContracts(List<Contract> contracts) {
        this.contractList = contracts == null ? new ArrayList<>() : contracts;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ContractViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contract, parent, false);
        return new ContractViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContractViewHolder holder, int position) {
        Contract contract = contractList.get(position);
        holder.tvContractId.setText(contract.getMaHopDong() != null ? contract.getMaHopDong() : "N/A");
        holder.tvContractStatus.setText("Trạng thái: " + (contract.getTrangThai() != null ? contract.getTrangThai() : "Mới"));
        holder.tvContractPrice.setText("Tiền thuê: " + formatMoney(contract.getTienThue()) + " VNĐ");
        holder.tvContractRoom.setText("Phòng ID: " + contract.getPhongId() + " | Người ID: " + contract.getNguoiId());
        holder.tvContractPeriod.setText("Thời hạn: " + safe(contract.getNgayBatDau()) + " -> " + safe(contract.getNgayKetThuc()));
        holder.tvUtilities.setText("Giá điện: " + formatMoney(contract.getTienDienPerUnit()) + "/kWh | Nước: " + formatMoney(contract.getTienNuocFixed()) + " VNĐ");

        boolean showAction = canManage && actionListener != null;
        holder.layoutActions.setVisibility(showAction ? View.VISIBLE : View.GONE);
        if (showAction) {
            holder.btnEdit.setOnClickListener(v -> actionListener.onEditContract(contract));
        }
    }

    @Override
    public int getItemCount() {
        return contractList.size();
    }

    static class ContractViewHolder extends RecyclerView.ViewHolder {
        TextView tvContractId;
        TextView tvContractStatus;
        TextView tvContractPrice;
        TextView tvContractRoom;
        TextView tvContractPeriod;
        TextView tvUtilities;
        LinearLayout layoutActions;
        Button btnEdit;

        public ContractViewHolder(@NonNull View itemView) {
            super(itemView);
            tvContractId = itemView.findViewById(R.id.tvContractId);
            tvContractStatus = itemView.findViewById(R.id.tvContractStatus);
            tvContractPrice = itemView.findViewById(R.id.tvContractPrice);
            tvContractRoom = itemView.findViewById(R.id.tvContractRoom);
            tvContractPeriod = itemView.findViewById(R.id.tvContractPeriod);
            tvUtilities = itemView.findViewById(R.id.tvUtilities);
            layoutActions = itemView.findViewById(R.id.layoutContractActions);
            btnEdit = itemView.findViewById(R.id.btnEditContract);
        }
    }

    private String safe(String value) {
        return value == null || value.trim().isEmpty() ? "N/A" : value;
    }

    private String formatMoney(Double value) {
        if (value == null) return "0";
        return String.format(java.util.Locale.US, "%,.0f", value);
    }
}

