package com.example.jobfinderapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jobfinderapp.R;
import com.example.jobfinderapp.adapters.NotificationAdapter;
import com.example.jobfinderapp.database.DBHelper;
import com.example.jobfinderapp.models.Notification;
import com.example.jobfinderapp.models.User;
import com.example.jobfinderapp.utils.UserSession;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

public class NotificationActivity extends AppCompatActivity implements NotificationAdapter.Listener {

    private RecyclerView rvNotifications;
    private NotificationAdapter adapter;
    private DBHelper dbHelper;
    private UserSession session;
    private User currentUser;
    private View layoutEmpty, layoutLoading;
    private TextView tvEmptyTitle, tvEmptyDescription;
    private MaterialButton btnMarkAllRead;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHelper = DBHelper.getInstance(this);
        session = new UserSession(this);
        if (!restoreCandidateSession()) return;

        setContentView(R.layout.activity_notification);
        initViews();
        loadNotifications();
    }

    private boolean restoreCandidateSession() {
        currentUser = session.isLoggedIn() ? dbHelper.getUserById(session.getUserId()) : null;
        if (currentUser != null && DBHelper.ROLE_CANDIDATE.equals(currentUser.getRole())
                && DBHelper.STATUS_ACTIVE.equals(currentUser.getStatus())) return true;
        session.clear();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finishAffinity();
        return false;
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
        rvNotifications = findViewById(R.id.rvNotifications);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        layoutLoading = findViewById(R.id.layoutLoading);
        tvEmptyTitle = findViewById(R.id.tvEmptyTitle);
        tvEmptyDescription = findViewById(R.id.tvEmptyDescription);
        btnMarkAllRead = findViewById(R.id.btnMarkAllRead);

        rvNotifications.setLayoutManager(new LinearLayoutManager(this));
        rvNotifications.setHasFixedSize(true);
        adapter = new NotificationAdapter(new ArrayList<>(), this);
        rvNotifications.setAdapter(adapter);
        btnMarkAllRead.setOnClickListener(v -> markAllRead());
    }

    private void loadNotifications() {
        if (currentUser == null || adapter == null) return;
        showState(true, false, null, null);
        try {
            List<Notification> notifications = dbHelper.getNotifications(currentUser.getId());
            adapter.updateList(notifications);
            updateMarkAllButton();
            showState(false, notifications.isEmpty(), getString(R.string.notification_empty_title),
                    getString(R.string.notification_empty_description));
        } catch (Exception exception) {
            adapter.updateList(new ArrayList<>());
            btnMarkAllRead.setVisibility(View.GONE);
            showState(false, true, getString(R.string.notification_error_title),
                    getString(R.string.notification_error_description));
        }
    }

    @Override
    public void onNotificationClick(Notification notification) {
        if (currentUser == null || notification.getIsRead() != 0) return;
        if (dbHelper.markAsRead(notification.getId(), currentUser.getId())) {
            notification.setIsRead(1);
            adapter.notifyRead(notification);
            updateMarkAllButton();
        } else {
            Toast.makeText(this, R.string.notification_read_error, Toast.LENGTH_SHORT).show();
        }
    }

    private void markAllRead() {
        if (currentUser == null || adapter.getUnreadCount() == 0) return;
        int updated = dbHelper.markAllNotificationsAsRead(currentUser.getId());
        if (updated > 0) {
            adapter.markAllRead();
            updateMarkAllButton();
        }
    }

    @Override
    public void onDeleteNotification(Notification notification) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.notification_delete_title)
                .setMessage(R.string.notification_delete_message)
                .setNegativeButton(R.string.notification_cancel, null)
                .setPositiveButton(R.string.notification_delete_confirm, (dialog, which) -> deleteNotification(notification))
                .show();
    }

    private void deleteNotification(Notification notification) {
        if (currentUser == null) return;
        if (dbHelper.deleteNotification(notification.getId(), currentUser.getId())) {
            adapter.removeById(notification.getId());
            updateMarkAllButton();
            showState(false, adapter.getItemCount() == 0, getString(R.string.notification_empty_title),
                    getString(R.string.notification_empty_description));
        } else {
            Toast.makeText(this, R.string.notification_delete_error, Toast.LENGTH_SHORT).show();
        }
    }

    private void updateMarkAllButton() {
        boolean hasUnread = adapter.getUnreadCount() > 0;
        btnMarkAllRead.setVisibility(hasUnread ? View.VISIBLE : View.GONE);
        btnMarkAllRead.setEnabled(hasUnread);
    }

    private void showState(boolean loading, boolean empty, String title, String description) {
        layoutLoading.setVisibility(loading ? View.VISIBLE : View.GONE);
        layoutEmpty.setVisibility(!loading && empty ? View.VISIBLE : View.GONE);
        rvNotifications.setVisibility(!loading && !empty ? View.VISIBLE : View.GONE);
        if (title != null) tvEmptyTitle.setText(title);
        if (description != null) tvEmptyDescription.setText(description);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (currentUser != null && adapter != null) loadNotifications();
    }
}
