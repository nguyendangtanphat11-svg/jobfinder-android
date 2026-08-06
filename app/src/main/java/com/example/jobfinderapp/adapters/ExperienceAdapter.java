package com.example.jobfinderapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.jobfinderapp.R;
import com.example.jobfinderapp.models.Experience;
import java.util.ArrayList;
import java.util.List;

public class ExperienceAdapter extends RecyclerView.Adapter<ExperienceAdapter.Holder> {
    public interface Listener { void onDelete(Experience experience); default void onEdit(Experience experience) { } }
    private final List<Experience> items = new ArrayList<>();
    private final Listener listener;
    public ExperienceAdapter(Listener listener) { this.listener = listener; }
    public void update(List<Experience> values) { items.clear(); items.addAll(values); notifyDataSetChanged(); }
    @NonNull @Override public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) { return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_experience, parent, false)); }
    @Override public void onBindViewHolder(@NonNull Holder holder, int position) { Experience experience = items.get(position); holder.title.setText(experience.getTitle()); holder.company.setText(experience.getCompany()); String endDate = experience.getEndDate(); holder.period.setText((experience.getStartDate() == null ? "" : experience.getStartDate()) + " – " + (endDate == null || endDate.trim().isEmpty() ? "Hiện tại" : endDate)); holder.edit.setOnClickListener(view -> listener.onEdit(experience)); holder.delete.setOnClickListener(view -> listener.onDelete(experience)); }
    @Override public int getItemCount() { return items.size(); }
    static class Holder extends RecyclerView.ViewHolder { final TextView title, company, period; final ImageButton edit, delete; Holder(View view) { super(view); title = view.findViewById(R.id.tvExperienceTitle); company = view.findViewById(R.id.tvExperienceCompany); period = view.findViewById(R.id.tvExperiencePeriod); edit = view.findViewById(R.id.btnEditExperience); delete = view.findViewById(R.id.btnDeleteExperience); } }
}
