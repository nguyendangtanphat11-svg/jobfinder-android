package com.example.jobfinderapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jobfinderapp.R;
import com.google.android.material.button.MaterialButton;

import java.util.List;
import java.util.Map;

public class ManageApplicantsAdapter extends RecyclerView.Adapter<ManageApplicantsAdapter.ApplicantViewHolder> {

    private Context context;
    private List<Map<String, Object>> applicantList;
    private OnApplicantActionListener listener;

    public interface OnApplicantActionListener {
        void onViewCV(int appId);
        void onAccept(int appId);
        void onReject(int appId);
    }

    public ManageApplicantsAdapter(Context context, List<Map<String, Object>> applicantList, OnApplicantActionListener listener) {
        this.context = context;
        this.applicantList = applicantList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ApplicantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_applicant, parent, false);
        return new ApplicantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ApplicantViewHolder holder, int position) {
        Map<String, Object> applicant = applicantList.get(position);
        int appId = (int) applicant.get("id");
        
        holder.tvName.setText((String) applicant.get("fullname"));
        holder.tvEmail.setText((String) applicant.get("email"));
        holder.tvJobTitle.setText("Ứng tuyển: " + applicant.get("job_title"));
        holder.tvApplyDate.setText("Ngày: " + applicant.get("apply_date"));
        
        String status = (String) applicant.get("status");
        holder.tvStatus.setText(status);

        if ("Chấp nhận".equals(status)) {
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_accepted);
            holder.tvStatus.setTextColor(context.getResources().getColor(R.color.primary_green));
        } else if ("Từ chối".equals(status)) {
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_rejected);
            holder.tvStatus.setTextColor(context.getResources().getColor(R.color.error_color));
        } else {
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_pending);
            holder.tvStatus.setTextColor(context.getResources().getColor(R.color.primary_green));
        }

        holder.btnViewCV.setOnClickListener(v -> listener.onViewCV(appId));
        holder.btnAccept.setOnClickListener(v -> listener.onAccept(appId));
        holder.btnReject.setOnClickListener(v -> listener.onReject(appId));
    }

    @Override
    public int getItemCount() {
        return applicantList.size();
    }

    public static class ApplicantViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvEmail, tvJobTitle, tvApplyDate, tvStatus;
        MaterialButton btnViewCV, btnAccept, btnReject;

        public ApplicantViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvApplicantName);
            tvEmail = itemView.findViewById(R.id.tvApplicantEmail);
            tvJobTitle = itemView.findViewById(R.id.tvAppliedJob);
            tvApplyDate = itemView.findViewById(R.id.tvApplyDate);
            tvStatus = itemView.findViewById(R.id.tvAppStatus);
            btnViewCV = itemView.findViewById(R.id.btnViewCV);
            btnAccept = itemView.findViewById(R.id.btnAccept);
            btnReject = itemView.findViewById(R.id.btnReject);
        }
    }
}
