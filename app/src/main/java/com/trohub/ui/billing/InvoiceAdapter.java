package com.trohub.ui.billing;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.trohub.ui.R;
import com.trohub.ui.api.models.Invoice;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class InvoiceAdapter extends RecyclerView.Adapter<InvoiceAdapter.InvoiceViewHolder> {

    private List<Invoice> invoiceList = new ArrayList<>();
    private final InvoiceActionListener actionListener;
    private final boolean adminOrStaff;

    public interface InvoiceActionListener {
        void onPayQr(Invoice invoice);
        void onApplyLateFee(Invoice invoice);
        void onSimulatePaid(Invoice invoice);
        void onViewPayments(Invoice invoice);
    }

    public InvoiceAdapter(InvoiceActionListener listener, boolean adminOrStaff) {
        this.actionListener = listener;
        this.adminOrStaff = adminOrStaff;
    }

    public void setInvoices(List<Invoice> list) {
        this.invoiceList = list == null ? new ArrayList<>() : list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public InvoiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_invoice, parent, false);
        return new InvoiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InvoiceViewHolder holder, int position) {
        Invoice invoice = invoiceList.get(position);
        String invoiceNo = safe(invoice.getInvoiceNumber());
        String period = String.format(Locale.US, "%02d/%04d", safeInt(invoice.getPeriodMonth()), safeInt(invoice.getPeriodYear()));
        holder.tvInvoiceNumber.setText("Mã hóa đơn: " + invoiceNo + " (ID " + invoice.getId() + ")");
        holder.tvInvoicePeriod.setText("Kỳ hóa đơn: " + period);
        holder.tvInvoiceTenant.setText("Người trả: " + safe(invoice.getTenantName())
                + " | SĐT: " + safe(invoice.getTenantPhone())
                + " | Tenant ID: " + (invoice.getTenantId() == null ? "N/A" : invoice.getTenantId()));
        holder.tvInvoiceRoomBuilding.setText("Phòng: " + safe(invoice.getRoomCode())
                + " (ID " + (invoice.getRoomId() == null ? "N/A" : invoice.getRoomId()) + ")"
                + " | Tòa: " + safe(invoice.getBuildingName())
                + " | Chủ: " + safe(invoice.getLandlordName()));
        holder.tvInvoiceDates.setText("Ngày phát hành: " + safe(invoice.getIssueDate()) + " | Hạn: " + safe(invoice.getDueDate()));
        holder.tvInvoiceTotal.setText("Tổng tiền: " + formatAmount(invoice.getTotalAmount()) + " VND");
        holder.tvInvoicePenalty.setText("Phí trễ hạn: " + formatAmount(invoice.getPenaltyAmount()) + " VND");
        int shareCount = invoice.getRoomShareCount() == null || invoice.getRoomShareCount() < 1 ? 1 : invoice.getRoomShareCount();
        holder.tvInvoiceShare.setText("Tổng phòng: " + formatAmount(invoice.getRoomTotalAmount())
                + " VND | Chia " + shareCount + " người | Phần phải trả: " + formatAmount(invoice.getTotalAmount()) + " VND");

        String status = normalizeStatus(invoice.getStatus());
        holder.tvInvoiceStatus.setText("Trạng thái: " + statusLabel(status));

        boolean isPaid = "PAID".equalsIgnoreCase(status);
        boolean isClosed = isPaid || "CANCELLED".equalsIgnoreCase(status);

        if (isClosed) {
            holder.btnPayQr.setVisibility(View.GONE);
        } else {
            holder.btnPayQr.setVisibility(View.VISIBLE);
            holder.btnPayQr.setOnClickListener(v -> actionListener.onPayQr(invoice));
        }

        holder.btnViewPayments.setOnClickListener(v -> actionListener.onViewPayments(invoice));

        boolean showAdminActions = adminOrStaff && !isClosed;
        holder.btnApplyLateFee.setVisibility(showAdminActions ? View.VISIBLE : View.GONE);
        holder.btnSimulatePaid.setVisibility(showAdminActions ? View.VISIBLE : View.GONE);

        if (showAdminActions) {
            holder.btnApplyLateFee.setOnClickListener(v -> actionListener.onApplyLateFee(invoice));
            holder.btnSimulatePaid.setOnClickListener(v -> actionListener.onSimulatePaid(invoice));
        }
    }

    @Override
    public int getItemCount() {
        return invoiceList.size();
    }

    private String safe(String value) {
        return value == null || value.trim().isEmpty() ? "N/A" : value;
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }

    private String formatAmount(Double value) {
        long amount = value == null ? 0L : Math.round(value);
        return String.format(Locale.US, "%,d", amount);
    }

    private String normalizeStatus(String raw) {
        if (raw == null) return "UNKNOWN";
        if ("PENDING".equalsIgnoreCase(raw)) return "UNPAID";
        return raw.toUpperCase(Locale.US);
    }

    private String statusLabel(String status) {
        if ("DRAFT".equalsIgnoreCase(status)) return "DRAFT (nháp)";
        if ("UNPAID".equalsIgnoreCase(status)) return "UNPAID (chưa thanh toán)";
        if ("PARTIALLY_PAID".equalsIgnoreCase(status)) return "PARTIALLY_PAID (thanh toán một phần)";
        if ("PAID".equalsIgnoreCase(status)) return "PAID (đã thanh toán)";
        if ("OVERDUE".equalsIgnoreCase(status)) return "OVERDUE (trễ hạn)";
        if ("CANCELLED".equalsIgnoreCase(status)) return "CANCELLED (đã hủy)";
        return status;
    }

    static class InvoiceViewHolder extends RecyclerView.ViewHolder {
        TextView tvInvoiceNumber;
        TextView tvInvoicePeriod;
        TextView tvInvoiceTenant;
        TextView tvInvoiceRoomBuilding;
        TextView tvInvoiceDates;
        TextView tvInvoiceTotal;
        TextView tvInvoicePenalty;
        TextView tvInvoiceShare;
        TextView tvInvoiceStatus;
        Button btnPayQr;
        Button btnViewPayments;
        Button btnApplyLateFee;
        Button btnSimulatePaid;

        public InvoiceViewHolder(@NonNull View itemView) {
            super(itemView);
            tvInvoiceNumber = itemView.findViewById(R.id.tvInvoiceNumber);
            tvInvoicePeriod = itemView.findViewById(R.id.tvInvoicePeriod);
            tvInvoiceTenant = itemView.findViewById(R.id.tvInvoiceTenant);
            tvInvoiceRoomBuilding = itemView.findViewById(R.id.tvInvoiceRoomBuilding);
            tvInvoiceDates = itemView.findViewById(R.id.tvInvoiceDates);
            tvInvoiceTotal = itemView.findViewById(R.id.tvInvoiceTotal);
            tvInvoicePenalty = itemView.findViewById(R.id.tvInvoicePenalty);
            tvInvoiceShare = itemView.findViewById(R.id.tvInvoiceShare);
            tvInvoiceStatus = itemView.findViewById(R.id.tvInvoiceStatus);
            btnPayQr = itemView.findViewById(R.id.btnPayQr);
            btnViewPayments = itemView.findViewById(R.id.btnViewPayments);
            btnApplyLateFee = itemView.findViewById(R.id.btnApplyLateFee);
            btnSimulatePaid = itemView.findViewById(R.id.btnSimulatePaid);
        }
    }
}

