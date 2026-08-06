package com.example.jobfinderapp.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.jobfinderapp.R;
import com.example.jobfinderapp.database.DBHelper;
import com.example.jobfinderapp.models.User;
import com.example.jobfinderapp.utils.UserSession;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.util.List;
import java.util.Map;

public class AdminDashboardActivity extends AppCompatActivity {
    private DBHelper db; private UserSession session; private User admin;
    @Override public void onCreate(Bundle state) {
        super.onCreate(state); getWindow().setStatusBarColor(Color.parseColor("#35145F"));
        getWindow().getDecorView().setSystemUiVisibility(0); db=DBHelper.getInstance(this); session=new UserSession(this);
        if (!validateAdmin()) return; setContentView(R.layout.activity_admin_dashboard); bindActions(); refreshDashboard();
    }
    @Override protected void onResume() { super.onResume(); if (admin != null && validateAdmin()) refreshDashboard(); }
    private boolean validateAdmin() {
        admin=session.isLoggedIn()?db.getUserById(session.getUserId()):null;
        if(admin==null || !DBHelper.ROLE_ADMIN.equals(admin.getRole()) || !DBHelper.STATUS_ACTIVE.equals(admin.getStatus())) { session.clear(); toLogin(); return false; } return true;
    }
    private void bindActions() {
        findViewById(R.id.actionPending).setOnClickListener(v->open(PendingEmployerActivity.class));
        findViewById(R.id.actionUsers).setOnClickListener(v->open(ManageUsersActivity.class));
        findViewById(R.id.actionJobs).setOnClickListener(v->open(AdminManageJobsActivity.class));
        findViewById(R.id.actionReports).setOnClickListener(v->open(AdminReportActivity.class));
        findViewById(R.id.btnReport).setOnClickListener(v->open(AdminReportActivity.class));
        findViewById(R.id.btnViewAllPending).setOnClickListener(v->open(PendingEmployerActivity.class));
        ((MaterialButton)findViewById(R.id.btnAdminLogout)).setOnClickListener(v->confirmLogout());
    }
    private void open(Class<?> activity) { startActivity(new Intent(this, activity)); }
    private void refreshDashboard() { if (findViewById(R.id.dashboardContent)==null) return; loadAdminProfile(); loadDashboardStats(); loadPendingEmployers(); loadRecentActivities(); }
    private void loadAdminProfile() { ((TextView)findViewById(R.id.tvAdminName)).setText(safe(admin.getFullname(), "Quản trị viên")); }
    private void loadDashboardStats() {
        showLoading(true); try { Map<String,Integer> s=db.getAdminStats(); set(R.id.tvUsers,s.get("users")); set(R.id.tvEmployers,s.get("employers")); set(R.id.tvPending,s.get("pending")); set(R.id.tvJobs,s.get("jobs")); set(R.id.tvApplications,s.get("applications")); set(R.id.tvBlocked,s.get("blocked"));
            set(R.id.tvRecruiting,db.getJobCountByStatus("Đang tuyển")); set(R.id.tvClosed,db.getJobCountByStatus("Đã đóng")); set(R.id.tvHidden,db.getJobCountByStatus("Bị ẩn"));
        } catch (Exception e) { setAllStatsZero(); findViewById(R.id.tvDashboardError).setVisibility(View.VISIBLE); } finally { showLoading(false); }
    }
    private void loadPendingEmployers() {
        LinearLayout host=findViewById(R.id.pendingPreviewContainer); host.removeAllViews(); List<Map<String,Object>> rows=db.getPendingEmployers();
        findViewById(R.id.tvPendingEmpty).setVisibility(rows.isEmpty()?View.VISIBLE:View.GONE); int count=Math.min(3, rows.size());
        for(int i=0;i<count;i++) { Map<String,Object> row=rows.get(i); View item=getLayoutInflater().inflate(R.layout.item_admin_pending_preview,host,false); ((TextView)item.findViewById(R.id.tvPreviewCompany)).setText(safe(value(row,"company_name"),"Công ty đang cập nhật")); ((TextView)item.findViewById(R.id.tvPreviewRepresentative)).setText(safe(value(row,"fullname"),"Đại diện đang cập nhật")); ((TextView)item.findViewById(R.id.tvPreviewEmail)).setText(safe(value(row,"email"),"Email đang cập nhật")); item.findViewById(R.id.btnPreviewPending).setOnClickListener(v->open(PendingEmployerActivity.class)); host.addView(item); }
    }
    private void loadRecentActivities() {
        LinearLayout host=findViewById(R.id.recentActivityContainer); host.removeAllViews(); List<Map<String,Object>> rows=db.getRecentApplicationsForAdmin(3); findViewById(R.id.tvRecentEmpty).setVisibility(rows.isEmpty()?View.VISIBLE:View.GONE);
        for(Map<String,Object> row:rows) { View item=getLayoutInflater().inflate(R.layout.item_admin_recent_activity,host,false); ((TextView)item.findViewById(R.id.tvActivityTitle)).setText(safe(value(row,"fullname"),"Ứng viên")+" đã ứng tuyển"); ((TextView)item.findViewById(R.id.tvActivityDetail)).setText(safe(value(row,"job_title"),"Công việc đang cập nhật")); ((TextView)item.findViewById(R.id.tvActivityTime)).setText(safe(value(row,"apply_date"),"")); host.addView(item); }
    }
    private String value(Map<String,Object> row,String key){Object v=row.get(key);return v==null?null:String.valueOf(v);}
    private String safe(String v,String fallback){return v==null||v.trim().isEmpty()?fallback:v;}
    private void showLoading(boolean loading){findViewById(R.id.progressDashboard).setVisibility(loading?View.VISIBLE:View.GONE); findViewById(R.id.tvDashboardError).setVisibility(View.GONE);}
    private void set(int id,Integer value){((TextView)findViewById(id)).setText(String.valueOf(value==null?0:value));}
    private void setAllStatsZero(){int[] ids={R.id.tvUsers,R.id.tvEmployers,R.id.tvPending,R.id.tvJobs,R.id.tvApplications,R.id.tvBlocked,R.id.tvRecruiting,R.id.tvClosed,R.id.tvHidden};for(int id:ids)set(id,0);}
    private void confirmLogout(){new MaterialAlertDialogBuilder(this).setTitle("Đăng xuất?").setMessage("Bạn sẽ cần đăng nhập lại để quản trị hệ thống.").setNegativeButton("Hủy",null).setPositiveButton("Đăng xuất",(d,w)->{session.clear();toLogin();}).show();}
    private void toLogin(){Intent i=new Intent(this,LoginActivity.class); i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK); startActivity(i); finish();}
}
