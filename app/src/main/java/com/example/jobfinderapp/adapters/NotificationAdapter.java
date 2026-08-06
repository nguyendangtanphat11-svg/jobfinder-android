package com.example.jobfinderapp.adapters;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jobfinderapp.R;
import com.example.jobfinderapp.models.Notification;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    public interface Listener {
        void onNotificationClick(Notification notification);
        void onDeleteNotification(Notification notification);
    }

    private final List<Notification> notificationList = new ArrayList<>();
    private final Listener listener;

    public NotificationAdapter(List<Notification> notifications, Listener listener) {
        this.listener = listener;
        updateList(notifications);
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new NotificationViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification notification = notificationList.get(position);
        holder.tvTitle.setText(valueOrFallback(notification.getTitle(), holder.itemView.getContext().getString(R.string.notification_fallback_title)));
        holder.tvContent.setText(valueOrFallback(notification.getContent(), holder.itemView.getContext().getString(R.string.notification_fallback_content)));
        holder.tvTime.setText(valueOrFallback(notification.getCreatedAt(), holder.itemView.getContext().getString(R.string.not_updated)));

        boolean unread = notification.getIsRead() == 0;
        holder.viewUnreadBadge.setVisibility(unread ? View.VISIBLE : View.GONE);
        holder.cardNotification.setCardBackgroundColor(holder.itemView.getContext().getColor(
                unread ? R.color.notification_unread_background : R.color.notification_read_background));
        holder.tvTitle.setTypeface(null, unread ? Typeface.BOLD : Typeface.NORMAL);
        holder.tvContent.setAlpha(unread ? 1f : 0.78f);
        holder.imgIcon.setImageResource(isApplicationNotification(notification) ? R.drawable.ic_work : R.drawable.ic_notifications);
        holder.imgIcon.setContentDescription(holder.itemView.getContext().getString(
                isApplicationNotification(notification) ? R.string.notification_application_icon : R.string.notification_general_icon));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onNotificationClick(notification);
        });
        holder.btnDeleteNotification.setOnClickListener(v -> {
            if (listener != null) listener.onDeleteNotification(notification);
        });
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    public void updateList(List<Notification> notifications) {
        notificationList.clear();
        if (notifications != null) notificationList.addAll(notifications);
        notifyDataSetChanged();
    }

    public void markAllRead() {
        for (Notification notification : notificationList) notification.setIsRead(1);
        notifyDataSetChanged();
    }

    public void notifyRead(Notification notification) {
        int index = notificationList.indexOf(notification);
        if (index >= 0) notifyItemChanged(index);
    }

    public void removeById(int notificationId) {
        for (int index = 0; index < notificationList.size(); index++) {
            if (notificationList.get(index).getId() == notificationId) {
                notificationList.remove(index);
                notifyItemRemoved(index);
                return;
            }
        }
    }

    public int getUnreadCount() {
        int count = 0;
        for (Notification notification : notificationList) if (notification.getIsRead() == 0) count++;
        return count;
    }

    private boolean isApplicationNotification(Notification notification) {
        String combined = (valueOrFallback(notification.getTitle(), "") + " "
                + valueOrFallback(notification.getContent(), "")).toLowerCase(Locale.ROOT);
        return combined.contains("ứng tuyển") || combined.contains("hồ sơ") || combined.contains("đơn ứng tuyển");
    }

    private String valueOrFallback(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value;
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        final TextView tvTitle, tvContent, tvTime;
        final View viewUnreadBadge;
        final MaterialCardView cardNotification;
        final ImageView imgIcon;
        final ImageButton btnDeleteNotification;

        NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvContent = itemView.findViewById(R.id.tvContent);
            tvTime = itemView.findViewById(R.id.tvTime);
            viewUnreadBadge = itemView.findViewById(R.id.viewUnreadBadge);
            cardNotification = itemView.findViewById(R.id.cardNotification);
            imgIcon = itemView.findViewById(R.id.imgIcon);
            btnDeleteNotification = itemView.findViewById(R.id.btnDeleteNotification);
        }
    }
}
