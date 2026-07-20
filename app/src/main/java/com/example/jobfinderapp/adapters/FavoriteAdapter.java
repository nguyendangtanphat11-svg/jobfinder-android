package com.example.jobfinderapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.jobfinderapp.R;
import com.example.jobfinderapp.activities.JobDetailActivity;
import com.example.jobfinderapp.database.DBHelper;
import com.example.jobfinderapp.models.Job;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.List;

public class FavoriteAdapter extends RecyclerView.Adapter<FavoriteAdapter.ViewHolder> {

    private Context context;
    private List<Job> jobList;
    private int userId;
    private DBHelper dbHelper;

    public FavoriteAdapter(Context context, List<Job> jobList, int userId) {
        this.context = context;
        this.jobList = jobList;
        this.userId = userId;
        this.dbHelper = new DBHelper(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_favorite_job, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Job job = jobList.get(position);

        holder.tvJobTitle.setText(job.getTitle());
        holder.tvCompanyName.setText(job.getCompanyName());
        holder.tvSalary.setText(job.getSalary());
        holder.tvLocation.setText(job.getLocation());
        holder.tvDeadline.setText("Hạn nộp: " + job.getDeadline());

        Glide.with(context)
                .load(job.getCompanyLogo())
                .placeholder(R.drawable.ic_launcher_background)
                .into(holder.imgCompanyLogo);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, JobDetailActivity.class);
            intent.putExtra("JOB_ID", job.getId());
            context.startActivity(intent);
        });

        holder.btnRemoveFavorite.setOnClickListener(v -> {
            boolean result = dbHelper.removeFavorite(userId, job.getId());
            if (result) {
                jobList.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, jobList.size());
                Toast.makeText(context, "Đã bỏ khỏi danh sách yêu thích.", Toast.LENGTH_SHORT).show();
                
                if (jobList.isEmpty() && context instanceof OnListEmptyListener) {
                    ((OnListEmptyListener) context).onListEmpty();
                }
            } else {
                Toast.makeText(context, "Lỗi khi xóa.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return jobList.size();
    }

    public void updateList(List<Job> newList) {
        this.jobList = newList;
        notifyDataSetChanged();
    }

    public interface OnListEmptyListener {
        void onListEmpty();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView imgCompanyLogo;
        TextView tvJobTitle, tvCompanyName, tvSalary, tvLocation, tvDeadline;
        ImageButton btnRemoveFavorite;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgCompanyLogo = itemView.findViewById(R.id.imgCompanyLogo);
            tvJobTitle = itemView.findViewById(R.id.tvJobTitle);
            tvCompanyName = itemView.findViewById(R.id.tvCompanyName);
            tvSalary = itemView.findViewById(R.id.tvSalary);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvDeadline = itemView.findViewById(R.id.tvDeadline);
            btnRemoveFavorite = itemView.findViewById(R.id.btnRemoveFavorite);
        }
    }
}
