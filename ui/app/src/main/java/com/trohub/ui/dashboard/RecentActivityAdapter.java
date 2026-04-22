package com.trohub.ui.dashboard;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.trohub.ui.R;

import java.util.ArrayList;
import java.util.List;

public class RecentActivityAdapter extends RecyclerView.Adapter<RecentActivityAdapter.ActivityViewHolder> {
    private List<RecentActivityItem> items = new ArrayList<>();

    public void setItems(List<RecentActivityItem> items) {
        this.items = items == null ? new ArrayList<>() : items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ActivityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recent_activity, parent, false);
        return new ActivityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ActivityViewHolder holder, int position) {
        RecentActivityItem item = items.get(position);
        holder.tvTitle.setText(item.getTitle());
        holder.tvSubtitle.setText(item.getSubtitle());
        holder.tvTime.setText(item.getTimeLabel());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ActivityViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        TextView tvSubtitle;
        TextView tvTime;

        ActivityViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvActivityTitle);
            tvSubtitle = itemView.findViewById(R.id.tvActivitySubtitle);
            tvTime = itemView.findViewById(R.id.tvActivityTime);
        }
    }
}
