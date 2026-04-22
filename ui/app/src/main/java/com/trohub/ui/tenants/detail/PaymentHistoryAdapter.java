package com.trohub.ui.tenants.detail;

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

public class PaymentHistoryAdapter extends RecyclerView.Adapter<PaymentHistoryAdapter.PaymentViewHolder> {

    private List<PaymentHistoryItem> items = new ArrayList<>();

    public void setItems(List<PaymentHistoryItem> items) {
        this.items = items == null ? new ArrayList<>() : items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PaymentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_payment_history, parent, false);
        return new PaymentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PaymentViewHolder holder, int position) {
        PaymentHistoryItem item = items.get(position);
        holder.tvPaymentTitle.setText(item.invoiceLabel);
        holder.tvPaymentMeta.setText("Ngày: " + safe(item.paymentDate) + " | Phương thức: " + safe(item.paymentMethod));
        holder.tvPaymentAmount.setText("Số tiền: " + formatAmount(item.amount) + " VND");
        holder.tvPaymentTxn.setText("Mã giao dịch: " + safe(item.externalTxnId));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private String formatAmount(Double value) {
        long amount = value == null ? 0L : Math.round(value);
        return String.format(Locale.US, "%,d", amount);
    }

    private String safe(String value) {
        return value == null || value.trim().isEmpty() ? "N/A" : value;
    }

    static class PaymentViewHolder extends RecyclerView.ViewHolder {
        TextView tvPaymentTitle;
        TextView tvPaymentMeta;
        TextView tvPaymentAmount;
        TextView tvPaymentTxn;

        PaymentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPaymentTitle = itemView.findViewById(R.id.tvPaymentTitle);
            tvPaymentMeta = itemView.findViewById(R.id.tvPaymentMeta);
            tvPaymentAmount = itemView.findViewById(R.id.tvPaymentAmount);
            tvPaymentTxn = itemView.findViewById(R.id.tvPaymentTxn);
        }
    }

    public static class PaymentHistoryItem {
        public final String invoiceLabel;
        public final String paymentDate;
        public final String paymentMethod;
        public final Double amount;
        public final String externalTxnId;

        public PaymentHistoryItem(String invoiceLabel, String paymentDate, String paymentMethod, Double amount, String externalTxnId) {
            this.invoiceLabel = invoiceLabel;
            this.paymentDate = paymentDate;
            this.paymentMethod = paymentMethod;
            this.amount = amount;
            this.externalTxnId = externalTxnId;
        }
    }
}
