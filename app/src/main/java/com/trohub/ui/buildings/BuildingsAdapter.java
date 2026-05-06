package com.trohub.ui.buildings;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.trohub.ui.R;
import com.trohub.ui.api.models.ToaNha;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BuildingsAdapter extends RecyclerView.Adapter<BuildingsAdapter.BuildingViewHolder> {

    private List<ToaNha> buildingList = new ArrayList<>();
    private final BuildingActionListener actionListener;
    private final boolean canManage;
    private Map<Long, String> managerLabels = new HashMap<>();

    public interface BuildingActionListener {
        void onEdit(ToaNha building);
        void onDelete(ToaNha building);
    }

    public BuildingsAdapter(BuildingActionListener actionListener, boolean canManage) {
        this.actionListener = actionListener;
        this.canManage = canManage;
    }

    public void setBuildings(List<ToaNha> buildings) {
        this.buildingList = buildings == null ? new ArrayList<>() : buildings;
        notifyDataSetChanged();
    }

    public void setManagerLabels(Map<Long, String> labels) {
        this.managerLabels = labels == null ? new HashMap<>() : new HashMap<>(labels);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BuildingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_building, parent, false);
        return new BuildingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BuildingViewHolder holder, int position) {
        ToaNha building = buildingList.get(position);
        holder.tvBuildingName.setText(building.getTen() != null ? building.getTen() : "Không tên");
        holder.tvBuildingAddress.setText("Địa chỉ: " + (building.getDiaChi() != null ? building.getDiaChi() : ""));
        holder.tvBuildingRooms.setText("Số phòng: " + (building.getRoomCount() != null ? building.getRoomCount() : "0") + " phòng");
        holder.tvBuildingManager.setText("Chủ trọ: " + labelOrUnset(managerLabels, building.getChuTroId()));

        holder.layoutActions.setVisibility(canManage ? View.VISIBLE : View.GONE);
        if (canManage) {
            holder.btnEdit.setOnClickListener(v -> actionListener.onEdit(building));
            holder.btnDelete.setOnClickListener(v -> actionListener.onDelete(building));
        }

        // Navigate to BuildingDetailActivity when clicking a building
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), BuildingDetailActivity.class);
            intent.putExtra("BUILDING_ID", building.getId());
            intent.putExtra("BUILDING_NAME", building.getTen());
            intent.putExtra("BUILDING_ADDRESS", building.getDiaChi());
            intent.putExtra("BUILDING_ROOM_COUNT", building.getRoomCount());
            intent.putExtra("BUILDING_OCCUPIED_COUNT", building.getOccupiedCount());
            intent.putExtra("BUILDING_MANAGER_ID", building.getChuTroId());
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return buildingList.size();
    }

    static class BuildingViewHolder extends RecyclerView.ViewHolder {
        TextView tvBuildingName;
        TextView tvBuildingAddress;
        TextView tvBuildingRooms;
        TextView tvBuildingManager;
        LinearLayout layoutActions;
        Button btnEdit;
        Button btnDelete;

        public BuildingViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBuildingName = itemView.findViewById(R.id.tvBuildingName);
            tvBuildingAddress = itemView.findViewById(R.id.tvBuildingAddress);
            tvBuildingRooms = itemView.findViewById(R.id.tvBuildingRooms);
            tvBuildingManager = itemView.findViewById(R.id.tvBuildingManager);
            layoutActions = itemView.findViewById(R.id.layoutBuildingActions);
            btnEdit = itemView.findViewById(R.id.btnEditBuilding);
            btnDelete = itemView.findViewById(R.id.btnDeleteBuilding);
        }
    }

    private String labelOrUnset(Map<Long, String> labels, Long id) {
        if (id == null) return "Chưa gán";
        String label = labels == null ? null : labels.get(id);
        return label == null || label.trim().isEmpty() ? "Chưa có tên" : label;
    }
}

