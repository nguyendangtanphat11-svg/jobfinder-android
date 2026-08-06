package com.example.jobfinderapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.jobfinderapp.R;
import com.example.jobfinderapp.activities.JobDetailActivity;
import com.example.jobfinderapp.models.Job;

import java.util.ArrayList;
import java.util.List;

public class ApplicationAdapter extends RecyclerView.Adapter<ApplicationAdapter.ViewHolder> {

    private final Context context;
    private final List<Job> applicationList;

    public ApplicationAdapter(Context context, List<Job> applicationList) {
        this.context = context;
        this.applicationList = new ArrayList<>(applicationList);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_application, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Job job = applicationList.get(position);
        holder.tvJobTitle.setText(valueOrFallback(job.getTitle(), context.getString(R.string.application_job_unavailable)));
        holder.tvCompanyName.setText(valueOrFallback(job.getCompanyName(), context.getString(R.string.application_company_unavailable)));
        holder.tvSalary.setText(valueOrFallback(job.getSalary(), context.getString(R.string.not_updated)));
        holder.tvLocation.setText(valueOrFallback(job.getLocation(), context.getString(R.string.not_updated)));
        holder.tvApplyDate.setText(context.getString(R.string.application_date_format,
                valueOrFallback(job.getApplyDate(), context.getString(R.string.not_updated))));

        String status = normalizeStatus(job.getStatus());
        holder.tvStatus.setText(status);
        switch (status) {
            case "Đang xem xét":
                holder.tvStatus.setBackgroundResource(R.drawable.bg_application_status_reviewing);
                holder.tvStatus.setTextColor(context.getColor(R.color.status_pending_text));
                break;
            case "Đã chấp nhận":
                holder.tvStatus.setBackgroundResource(R.drawable.bg_application_status_accepted);
                holder.tvStatus.setTextColor(context.getColor(R.color.status_accepted_text));
                break;
            case "Đã từ chối":
                holder.tvStatus.setBackgroundResource(R.drawable.bg_application_status_rejected);
                holder.tvStatus.setTextColor(context.getColor(R.color.status_rejected_text));
                break;
            default:
                holder.tvStatus.setBackgroundResource(R.drawable.bg_application_status_waiting);
                holder.tvStatus.setTextColor(context.getColor(R.color.status_sent_text));
                break;
        }

        Glide.with(context)
                .load(job.getCompanyLogo())
.placeholder(R.drawable.ic_default_company)
.error(R.drawable.ic_default_company)
                .into(holder.ivCompanyLogo);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, JobDetailActivity.class);
            intent.putExtra("JOB_ID", job.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return applicationList.size();
    }

    public void updateList(List<Job> jobs) {
        applicationList.clear();
        if (jobs != null) applicationList.addAll(jobs);
        notifyDataSetChanged();
    }

    private String normalizeStatus(String status) {
        if ("Đã gửi".equals(status)) return "Đang chờ";
        if ("Đang xét duyệt".equals(status)) return "Đang xem xét";
        if ("Đã nhận".equals(status)) return "Đã chấp nhận";
        return status == null || status.trim().isEmpty() ? "Đang chờ" : status;
    }

    private String valueOrFallback(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView ivCompanyLogo;
        final TextView tvJobTitle, tvCompanyName, tvSalary, tvLocation, tvApplyDate, tvStatus;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCompanyLogo = itemView.findViewById(R.id.ivCompanyLogo);
            tvJobTitle = itemView.findViewById(R.id.tvJobTitle);
            tvCompanyName = itemView.findViewById(R.id.tvCompanyName);
            tvSalary = itemView.findViewById(R.id.tvSalary);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvApplyDate = itemView.findViewById(R.id.tvApplyDate);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }
}
