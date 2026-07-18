package com.example.jobfinderapp.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jobfinderapp.R;
import com.example.jobfinderapp.database.DBHelper;
import com.example.jobfinderapp.models.Notification;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private Context context;
    private List<Notification> notificationList;
    private DBHelper dbHelper;

    public NotificationAdapter(Context context, List<Notification> notificationList) {
        this.context = context;
        this.notificationList = notificationList;
        this.dbHelper = new DBHelper(context);
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification notification = notificationList.get(position);

        holder.tvTitle.setText(notification.getTitle());
        holder.tvContent.setText(notification.getContent());
        holder.tvTime.setText(notification.getCreatedAt());

        if (notification.getIsRead() == 1) {
            holder.viewUnreadBadge.setVisibility(View.GONE);
            holder.itemView.setAlpha(0.7f);
            holder.cardNotification.setCardBackgroundColor(Color.parseColor("#F5F5F5"));
        } else {
            holder.viewUnreadBadge.setVisibility(View.VISIBLE);
            holder.itemView.setAlpha(1.0f);
            holder.cardNotification.setCardBackgroundColor(Color.WHITE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (notification.getIsRead() == 0) {
                dbHelper.markAsRead(notification.getId());
                notification.setIsRead(1);
                notifyItemChanged(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    public void updateList(List<Notification> newList) {
        this.notificationList = newList;
        notifyDataSetChanged();
    }

    public static class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvContent, tvTime;
        View viewUnreadBadge;
        com.google.android.material.card.MaterialCardView cardNotification;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvContent = itemView.findViewById(R.id.tvContent);
            tvTime = itemView.findViewById(R.id.tvTime);
            viewUnreadBadge = itemView.findViewById(R.id.viewUnreadBadge);
            cardNotification = itemView.findViewById(R.id.cardNotification);
        }
    }
}
