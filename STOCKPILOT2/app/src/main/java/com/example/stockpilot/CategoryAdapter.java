package com.example.stockpilot;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stockpilot.R;
import com.example.stockpilot.Category;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private List<Category> categoryList;
    private OnCategoryActionListener listener;

    public interface OnCategoryActionListener {
        void onEditCategory(Category category);
        void onDeleteCategory(Category category);
    }

    public CategoryAdapter(List<Category> categoryList, OnCategoryActionListener listener) {
        this.categoryList = categoryList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_items_group_reycler, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categoryList.get(position);
        holder.bind(category, listener);
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    public static class CategoryViewHolder extends RecyclerView.ViewHolder {
        private CardView cardView;
        private TextView categoryName;
        private ImageView editIcon;
        private ImageView deleteIcon;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.category_card);
            categoryName = itemView.findViewById(R.id.category_name);
            editIcon = itemView.findViewById(R.id.edit_icon);
            deleteIcon = itemView.findViewById(R.id.delete_icon);
        }

        public void bind(final Category category, final OnCategoryActionListener listener) {
            categoryName.setText(category.getCategoryName());

            editIcon.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditCategory(category);
                }
            });

            deleteIcon.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteCategory(category);
                }
            });

            cardView.setOnClickListener(v -> {
                // Navigate to items in this category
                try {
                    Class<?> itemsActivityClass = Class.forName("com.example.stockpilot.ItemsActivity");
                    android.content.Intent intent = new android.content.Intent(v.getContext(), itemsActivityClass);
                    intent.putExtra("category_id", category.getCategoryId());
                    intent.putExtra("category_name", category.getCategoryName());
                    v.getContext().startActivity(intent);
                } catch (ClassNotFoundException e) {
                    android.widget.Toast.makeText(v.getContext(), "Items view not available", android.widget.Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
