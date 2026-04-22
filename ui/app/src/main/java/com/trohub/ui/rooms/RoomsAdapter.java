package com.trohub.ui.rooms;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.trohub.ui.R;
import com.trohub.ui.api.models.Phong;

import java.util.ArrayList;
import java.util.List;

public class RoomsAdapter extends RecyclerView.Adapter<RoomsAdapter.RoomViewHolder> {

    private List<Phong> roomList = new ArrayList<>();
    private final RoomActionListener actionListener;
    private final boolean canEdit;
    private final boolean canDelete;
    private RoomItemClickListener roomItemClickListener;

    public interface RoomActionListener {
        void onEditRoom(Phong room);
        void onDeleteRoom(Phong room);
    }

    public interface RoomItemClickListener {
        void onRoomClick(Phong room);
    }

    public RoomsAdapter(RoomActionListener listener, boolean canEdit, boolean canDelete) {
        this.actionListener = listener;
        this.canEdit = canEdit;
        this.canDelete = canDelete;
    }

    public RoomsAdapter() {
        this(null, false, false);
    }

    public void setRooms(List<Phong> rooms) {
        this.roomList = rooms == null ? new ArrayList<>() : rooms;
        notifyDataSetChanged();
    }

    public void setRoomItemClickListener(RoomItemClickListener listener) {
        this.roomItemClickListener = listener;
    }

    @NonNull
    @Override
    public RoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_room, parent, false);
        return new RoomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RoomViewHolder holder, int position) {
        Phong phong = roomList.get(position);
        holder.tvRoomName.setText(phong.getMaPhong() != null ? phong.getMaPhong() : "Phòng #" + phong.getId());
        holder.tvRoomBeds.setText("Số giường: " + (phong.getSoGiuong() != null ? phong.getSoGiuong() : 0));
        holder.tvRoomStatus.setText("Trạng thái: " + (phong.getTrangThai() != null ? phong.getTrangThai() : "N/A"));
        holder.tvRoomBuilding.setText("Tòa nhà ID: " + (phong.getToaNhaId() != null ? phong.getToaNhaId() : "N/A"));

        boolean showActions = actionListener != null && (canEdit || canDelete);
        holder.layoutActions.setVisibility(showActions ? View.VISIBLE : View.GONE);
        if (showActions) {
            holder.btnEdit.setVisibility(canEdit ? View.VISIBLE : View.GONE);
            holder.btnDelete.setVisibility(canDelete ? View.VISIBLE : View.GONE);
            holder.btnEdit.setEnabled(canEdit);
            holder.btnDelete.setEnabled(canDelete);
            holder.btnEdit.setOnClickListener(v -> actionListener.onEditRoom(phong));
            holder.btnDelete.setOnClickListener(v -> actionListener.onDeleteRoom(phong));
        } else {
            holder.btnEdit.setOnClickListener(null);
            holder.btnDelete.setOnClickListener(null);
        }

        holder.itemView.setOnClickListener(v -> {
            if (roomItemClickListener != null) {
                roomItemClickListener.onRoomClick(phong);
            }
        });
    }

    @Override
    public int getItemCount() {
        return roomList.size();
    }

    static class RoomViewHolder extends RecyclerView.ViewHolder {
        TextView tvRoomName;
        TextView tvRoomBeds;
        TextView tvRoomStatus;
        TextView tvRoomBuilding;
        LinearLayout layoutActions;
        Button btnEdit;
        Button btnDelete;

        public RoomViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRoomName = itemView.findViewById(R.id.tvRoomName);
            tvRoomBeds = itemView.findViewById(R.id.tvRoomBeds);
            tvRoomStatus = itemView.findViewById(R.id.tvRoomStatus);
            tvRoomBuilding = itemView.findViewById(R.id.tvRoomBuilding);
            layoutActions = itemView.findViewById(R.id.layoutRoomActions);
            btnEdit = itemView.findViewById(R.id.btnEditRoom);
            btnDelete = itemView.findViewById(R.id.btnDeleteRoom);
        }
    }
}

