package com.example.stockpilot;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stockpilot.R;
import com.example.stockpilot.Product;

import java.util.ArrayList;
import java.util.List;

public class ProductSelectionAdapter extends RecyclerView.Adapter<ProductSelectionAdapter.ProductViewHolder> {

    public interface OnProductClickListener {
        void onProductClick(Product product);
    }

    private List<Product> productList;
    private final Context context;
    private final OnProductClickListener listener;

    public ProductSelectionAdapter(List<Product> products, Context ctx, OnProductClickListener listener) {
        this.productList = new ArrayList<>(products);
        this.context = ctx;
        this.listener = listener;
    }

    public void updateList(List<Product> filteredList) {
        productList = filteredList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.activity_item_product_selection, parent, false);
        return new ProductViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product p = productList.get(position);
        holder.tvProductName.setText(p.getProductName());
        holder.tvProductDetails.setText(
                "SKU: " + p.getSku() + " | Stock: " + p.getStockQuantity());
        holder.itemView.setOnClickListener(v -> {
            if (listener != null)
                listener.onProductClick(p);
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView tvProductName, tvProductDetails;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProductName = itemView.findViewById(R.id.tvProductName);
        }
    }
}

