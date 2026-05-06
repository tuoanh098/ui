package com.trohub.ui.incidents;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.trohub.ui.R;
import com.trohub.ui.api.models.Incident;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;

public class IncidentsAdapter extends RecyclerView.Adapter<IncidentsAdapter.IncidentViewHolder> {

    private List<Incident> incidentList = new ArrayList<>();
    private final IncidentActionListener actionListener;
    private final boolean canManage;
    private final boolean canResolve;
    private Map<Long, String> roomLabels = new HashMap<>();
    private Map<Long, String> reporterLabels = new HashMap<>();

    public interface IncidentActionListener {
        void onViewIncident(Incident incident);
        void onEditIncident(Incident incident);
        void onDeleteIncident(Incident incident);
        void onResolveIncident(Incident incident);
    }

    public IncidentsAdapter(IncidentActionListener listener, boolean canManage) {
        this(listener, canManage, canManage);
    }

    public IncidentsAdapter(IncidentActionListener listener, boolean canManage, boolean canResolve) {
        this.actionListener = listener;
        this.canManage = canManage;
        this.canResolve = canResolve;
    }

    public IncidentsAdapter() {
        this(null, false);
    }

    public void setIncidents(List<Incident> incidents) {
        this.incidentList = incidents == null ? new ArrayList<>() : incidents;
        notifyDataSetChanged();
    }

    public void setLookupLabels(Map<Long, String> roomLabels, Map<Long, String> reporterLabels) {
        this.roomLabels = roomLabels == null ? new HashMap<>() : new HashMap<>(roomLabels);
        this.reporterLabels = reporterLabels == null ? new HashMap<>() : new HashMap<>(reporterLabels);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public IncidentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_incident, parent, false);
        return new IncidentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IncidentViewHolder holder, int position) {
        Incident incident = incidentList.get(position);
        holder.tvIncidentTitle.setText(incident.getLoai() != null ? incident.getLoai() : "Sự cố");
        holder.tvIncidentStatus.setText("Trạng thái: " + (incident.getStatus() != null ? incident.getStatus() : "OPEN"));
        holder.tvIncidentRoom.setText("Phòng: " + labelOrUnset(roomLabels, incident.getPhongId()));
        holder.tvIncidentReporter.setText("Người báo: " + labelOrUnset(reporterLabels, incident.getReportedBy()));
        holder.tvIncidentDescription.setText(incident.getMoTa() != null && !incident.getMoTa().trim().isEmpty() ? incident.getMoTa() : "Chưa có mô tả");
        holder.itemView.setOnClickListener(v -> {
            if (actionListener != null) actionListener.onViewIncident(incident);
        });

        boolean showActions = canManage && actionListener != null && !isResolved(incident);
        holder.layoutActions.setVisibility(showActions ? View.VISIBLE : View.GONE);
        if (showActions) {
            holder.btnEdit.setOnClickListener(v -> actionListener.onEditIncident(incident));
            holder.btnDelete.setOnClickListener(v -> actionListener.onDeleteIncident(incident));
            holder.btnResolve.setVisibility(canResolve ? View.VISIBLE : View.GONE);
            holder.btnResolve.setOnClickListener(v -> actionListener.onResolveIncident(incident));
        } else {
            holder.btnEdit.setOnClickListener(null);
            holder.btnDelete.setOnClickListener(null);
            holder.btnResolve.setOnClickListener(null);
        }
    }

    @Override
    public int getItemCount() {
        return incidentList.size();
    }

    static class IncidentViewHolder extends RecyclerView.ViewHolder {
        TextView tvIncidentTitle;
        TextView tvIncidentStatus;
        TextView tvIncidentRoom;
        TextView tvIncidentReporter;
        TextView tvIncidentDescription;
        LinearLayout layoutActions;
        Button btnEdit;
        Button btnDelete;
        Button btnResolve;

        public IncidentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvIncidentTitle = itemView.findViewById(R.id.tvIncidentTitle);
            tvIncidentStatus = itemView.findViewById(R.id.tvIncidentStatus);
            tvIncidentRoom = itemView.findViewById(R.id.tvIncidentRoom);
            tvIncidentReporter = itemView.findViewById(R.id.tvIncidentReporter);
            tvIncidentDescription = itemView.findViewById(R.id.tvIncidentDescription);
            layoutActions = itemView.findViewById(R.id.layoutIncidentActions);
            btnEdit = itemView.findViewById(R.id.btnEditIncident);
            btnDelete = itemView.findViewById(R.id.btnDeleteIncident);
            btnResolve = itemView.findViewById(R.id.btnResolveIncident);
        }
    }

    private String labelOrUnset(Map<Long, String> labels, Long id) {
        if (id == null) return "Chưa gán";
        String label = labels == null ? null : labels.get(id);
        return label == null || label.trim().isEmpty() ? "Chưa có tên" : label;
    }

    private boolean isResolved(Incident incident) {
        if (incident == null) return false;
        if (incident.getResolvedAt() != null && !incident.getResolvedAt().trim().isEmpty()) return true;
        String status = incident.getStatus() == null ? "" : incident.getStatus().trim().toUpperCase(Locale.US);
        return "RESOLVED".equals(status)
                || "CLOSED".equals(status)
                || "DONE".equals(status)
                || "DA_XU_LY".equals(status)
                || "ĐÃ XỬ LÝ".equals(status);
    }
}

