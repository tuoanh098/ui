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
import java.util.List;

public class IncidentsAdapter extends RecyclerView.Adapter<IncidentsAdapter.IncidentViewHolder> {

    private List<Incident> incidentList = new ArrayList<>();
    private final IncidentActionListener actionListener;
    private final boolean canManage;

    public interface IncidentActionListener {
        void onEditIncident(Incident incident);
        void onDeleteIncident(Incident incident);
    }

    public IncidentsAdapter(IncidentActionListener listener, boolean canManage) {
        this.actionListener = listener;
        this.canManage = canManage;
    }

    public IncidentsAdapter() {
        this(null, false);
    }

    public void setIncidents(List<Incident> incidents) {
        this.incidentList = incidents == null ? new ArrayList<>() : incidents;
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
        holder.tvIncidentRoom.setText("Phòng ID: " + (incident.getPhongId() != null ? incident.getPhongId() : "N/A"));
        holder.tvIncidentReporter.setText("Người báo ID: " + (incident.getReportedBy() != null ? incident.getReportedBy() : "N/A"));

        boolean showActions = canManage && actionListener != null;
        holder.layoutActions.setVisibility(showActions ? View.VISIBLE : View.GONE);
        if (showActions) {
            holder.btnEdit.setOnClickListener(v -> actionListener.onEditIncident(incident));
            holder.btnDelete.setOnClickListener(v -> actionListener.onDeleteIncident(incident));
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
        LinearLayout layoutActions;
        Button btnEdit;
        Button btnDelete;

        public IncidentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvIncidentTitle = itemView.findViewById(R.id.tvIncidentTitle);
            tvIncidentStatus = itemView.findViewById(R.id.tvIncidentStatus);
            tvIncidentRoom = itemView.findViewById(R.id.tvIncidentRoom);
            tvIncidentReporter = itemView.findViewById(R.id.tvIncidentReporter);
            layoutActions = itemView.findViewById(R.id.layoutIncidentActions);
            btnEdit = itemView.findViewById(R.id.btnEditIncident);
            btnDelete = itemView.findViewById(R.id.btnDeleteIncident);
        }
    }
}

