package com.example.stockpilot;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stockpilot.R;
import com.example.stockpilot.Product;

import java.util.ArrayList;
import java.util.List;

public class ItemProductSelection extends RecyclerView.Adapter<ItemProductSelection.ViewHolder> {

    private Context context;
    private List<Product> productList;
    private List<Product> filteredList;
    private OnProductSelectedListener listener;

    public interface OnProductSelectedListener {
        void onProductSelected(Product product);
    }

    public ItemProductSelection(Context context, List<Product> productList, OnProductSelectedListener listener) {
        this.context = context;
        this.productList = productList;
        this.filteredList = new ArrayList<>(productList);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.activity_item_product_selection, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = filteredList.get(position);
        
        // Set product name
        holder.tvProductName.setText(product.getProductName());
        
        // Set product price
        holder.tvProductPrice.setText(String.format("₹%.2f", product.getSellingPrice()));
        
        // Set stock quantity
        holder.tvProductStockQty.setText("Stock: " + product.getStockQuantity());
        
        // Set stock status based on quantity
        if (product.getStockQuantity() > 0) {
            holder.tvProductStock.setText("In Stock");
            holder.tvProductStock.setTextColor(context.getResources().getColor(R.color.success));
        } else {
            holder.tvProductStock.setText("Out of Stock");
            holder.tvProductStock.setTextColor(context.getResources().getColor(R.color.error));
        }
        
        // Set click listener
        holder.cardProduct.setOnClickListener(v -> {
            if (listener != null) {
                listener.onProductSelected(product);
            }
        });
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }
    
    public void filter(String query) {
        filteredList.clear();
        
        if (query.isEmpty()) {
            filteredList.addAll(productList);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (Product product : productList) {
                if (product.getProductName().toLowerCase().contains(lowerCaseQuery) ||
                    product.getSku().toLowerCase().contains(lowerCaseQuery)) {
                    filteredList.add(product);
                }
            }
        }
        
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardProduct;
        ImageView ivProductImage;
        TextView tvProductName, tvProductPrice, tvProductStockQty, tvProductStock;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardProduct = itemView.findViewById(R.id.card_product);
            ivProductImage = itemView.findViewById(R.id.iv_product_image);
            tvProductName = itemView.findViewById(R.id.tv_product_name);
            tvProductPrice = itemView.findViewById(R.id.tv_product_price);
            tvProductStockQty = itemView.findViewById(R.id.tv_product_stock_qty);
            tvProductStock = itemView.findViewById(R.id.tv_product_stock);
        }
    }
}

