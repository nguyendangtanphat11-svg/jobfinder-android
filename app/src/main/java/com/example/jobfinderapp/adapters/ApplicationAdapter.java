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

import java.util.List;

public class ApplicationAdapter extends RecyclerView.Adapter<ApplicationAdapter.ViewHolder> {

    private Context context;
    private List<Job> applicationList;

    public ApplicationAdapter(Context context, List<Job> applicationList) {
        this.context = context;
        this.applicationList = applicationList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_application, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Job job = applicationList.get(position);
        
        holder.tvJobTitle.setText(job.getTitle());
        holder.tvCompanyName.setText(job.getCompanyName());
        holder.tvSalary.setText(job.getSalary());
        holder.tvLocation.setText(job.getLocation());
        holder.tvApplyDate.setText("Ngày ứng tuyển: " + job.getApplyDate());
        holder.tvStatus.setText(job.getStatus());

        // Set status background based on status text
        String status = job.getStatus();
        if (status != null) {
            switch (status) {
                case "Đã gửi":
                    holder.tvStatus.setBackgroundResource(R.drawable.bg_status_sent);
                    break;
                case "Đang xét duyệt":
                    holder.tvStatus.setBackgroundResource(R.drawable.bg_status_pending);
                    break;
                case "Đã nhận":
                    holder.tvStatus.setBackgroundResource(R.drawable.bg_status_accepted);
                    break;
                case "Đã từ chối":
                    holder.tvStatus.setBackgroundResource(R.drawable.bg_status_rejected);
                    break;
                default:
                    holder.tvStatus.setBackgroundResource(R.drawable.bg_status_sent);
                    break;
            }
        }

        Glide.with(context)
                .load(job.getCompanyLogo())
                .placeholder(R.drawable.ic_launcher_background)
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

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCompanyLogo;
        TextView tvJobTitle, tvCompanyName, tvSalary, tvLocation, tvApplyDate, tvStatus;

        public ViewHolder(@NonNull View itemView) {
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
