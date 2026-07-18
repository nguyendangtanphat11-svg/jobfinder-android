package com.example.jobfinderapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide; // Import thư viện Glide để load ảnh động từ mạng
import com.example.jobfinderapp.models.Job;

import java.util.List;

public class JobAdapter extends RecyclerView.Adapter<JobAdapter.JobViewHolder> {

    private List<Job> jobList;

    public JobAdapter(List<Job> jobList) {
        this.jobList = jobList;
    }

    @NonNull
    @Override
    public JobViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_job, parent, false);
        return new JobViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull JobViewHolder holder, int position) {
        Job job = jobList.get(position);

        // Gán dữ liệu chữ
        holder.tvTitle.setText(job.getTitle());
        holder.tvCompany.setText(job.getCompany());
        holder.tvSalary.setText(job.getSalary());
        holder.tvLocation.setText(job.getLocation());

        // XỬ LÝ ĐỔ ẢNH ĐỘNG TỪ URL: Tự động thay đổi ảnh theo từng item công việc
        Glide.with(holder.itemView.getContext())
                .load(job.getImageUrl()) // Lấy link ảnh động từ Object Job hiện tại
                .placeholder(android.R.color.darker_gray) // Hiển thị màu xám khi đang tải
                .error(android.R.drawable.ic_menu_gallery) // Hiển thị ảnh mặc định nếu link lỗi
                .into(holder.imgCompany); // Đổ trực tiếp vào ImageView
    }

    @Override
    public int getItemCount() {
        return jobList != null ? jobList.size() : 0;
    }

    public static class JobViewHolder extends RecyclerView.ViewHolder {
        // 1. Thay đổi khai báo tên biến cho đồng bộ (hoặc giữ nguyên biến cũ nhưng ánh xạ đúng ID)
        TextView tvTitle, tvCompany, tvSalary, tvLocation;
        ImageView imgCompany;

        public JobViewHolder(@NonNull View itemView) {
            super(itemView);
            // 2. Ánh xạ chính xác theo ID trong file XML của bạn
            tvTitle = itemView.findViewById(R.id.tvJobTitle);       // XML là tvJobTitle
            tvCompany = itemView.findViewById(R.id.tvCompanyName);   // XML là tvCompanyName
            tvSalary = itemView.findViewById(R.id.tvJobSalary);     // XML là tvJobSalary
            tvLocation = itemView.findViewById(R.id.tvJobLocation); // XML là tvJobLocation
            imgCompany = itemView.findViewById(R.id.imgCompanyLogo); // XML là imgCompanyLogo
        }
    }
}