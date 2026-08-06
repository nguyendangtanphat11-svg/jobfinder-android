package com.example.jobfinderapp.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jobfinderapp.R;
import com.example.jobfinderapp.models.Category;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    public interface OnCategorySelectedListener {
        void onCategorySelected(Category category);
    }

    private final List<Category> categoryList;
    private final OnCategorySelectedListener listener;
    private int selectedCategoryId = 0;

    public CategoryAdapter(List<Category> categoryList, OnCategorySelectedListener listener) {
        this.categoryList = categoryList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CategoryViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categoryList.get(position);
        boolean selected = category.getId() == selectedCategoryId;
        holder.tvCategoryName.setText(category.getName());
        holder.cardCategory.setCardBackgroundColor(Color.parseColor(selected ? "#622FB5" : "#F1EDFA"));
        holder.tvCategoryName.setTextColor(Color.parseColor(selected ? "#FFFFFF" : "#4A3D5E"));
        holder.itemView.setOnClickListener(v -> {
            int oldSelected = selectedCategoryId;
            selectedCategoryId = category.getId();
            notifyDataSetChanged();
            if (listener != null && oldSelected != selectedCategoryId) {
                listener.onCategorySelected(category);
            }
        });
    }

    @Override public int getItemCount() { return categoryList.size(); }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        final MaterialCardView cardCategory;
        final TextView tvCategoryName;
        CategoryViewHolder(@NonNull View view) {
            super(view);
            cardCategory = view.findViewById(R.id.cardCategory);
            tvCategoryName = view.findViewById(R.id.tvCategoryName);
        }
    }
}
