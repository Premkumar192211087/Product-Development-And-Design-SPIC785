package com.example.stockpilot;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.stockpilot.R;
import com.example.stockpilot.Item;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ItemsAdapter extends RecyclerView.Adapter<ItemsAdapter.ItemViewHolder> {

    private Context context;
    private List<Item> itemsList;
    private NumberFormat currencyFormat;

    public ItemsAdapter(Context context, List<Item> itemsList) {
        this.context = context;
        this.itemsList = itemsList;
        this.currencyFormat = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.activity_items_reycler, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        Item item = itemsList.get(position);

        // Set product name
        holder.productName.setText(item.getProductName());

        // Set SKU
        holder.productSku.setText(item.getSku());

        // Set sales price (assuming this is the main price)
        holder.salesPrice.setText("Rs." + String.format("%.2f", item.getPrice()));

        // Set purchase price (you might need to add this field to Item class)
        // For now, showing a calculated value or placeholder
        double purchasePrice = item.getPrice() * 0.8; // Assuming 20% margin
        holder.purchasePrice.setText("Rs." + String.format("%.2f", purchasePrice));

        // Set stock quantity as whole number (integer)
        int stockQuantity = (int) item.getQuantity();
        holder.stockQuantity.setText(String.valueOf(stockQuantity));

        // Load product image
        if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(item.getImageUrl())
                    .apply(new RequestOptions()
                            .placeholder(R.drawable.ic_product_placeholder)
                            .error(R.drawable.ic_product_placeholder)
                            .transform(new RoundedCorners(8)))
                    .into(holder.productImage);
        } else {
            holder.productImage.setImageResource(R.drawable.ic_product_placeholder);
        }

        // Set stock status indicator color based on quantity
        setStockStatusColor(holder.stockStatusIndicator, item.getQuantity(), item.getStatus());

        // Set click listener for the entire item
        holder.itemView.setOnClickListener(v -> {
            // Navigate to item details
            try {
                Class<?> itemDetailsClass = Class.forName("com.example.stockpilot.ItemDetailsActivity");
                android.content.Intent intent = new android.content.Intent(context, itemDetailsClass);
                intent.putExtra("item_id", item.getId());
                intent.putExtra("item_name", item.getProductName());
                context.startActivity(intent);
            } catch (ClassNotFoundException e) {
                android.widget.Toast.makeText(context, "Item details view not available", android.widget.Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return itemsList.size();
    }

    private void setStockStatusColor(View indicator, double quantity, String status) {
        String level;
        if (status != null) {
            switch (status.toLowerCase()) {
                case "out of stock":
                    level = "low";
                    break;
                case "low stock":
                    level = "medium";
                    break;
                case "in stock":
                default:
                    // derive from quantity
                    if (quantity <= 0) {
                        level = "low";
                    } else if (quantity <= 10) {
                        level = "medium";
                    } else {
                        level = "high";
                    }
                    break;
            }
        } else {
            if (quantity <= 0) {
                level = "low";
            } else if (quantity <= 10) {
                level = "medium";
            } else {
                level = "high";
            }
        }
        int color = StatusColors.forStockLevel(indicator.getContext(), level);
        indicator.setBackgroundColor(color);
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage;
        TextView productName;
        TextView productSku;
        TextView salesPrice;
        TextView purchasePrice;
        TextView stockQuantity;
        View stockStatusIndicator;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.product_image);
            productName = itemView.findViewById(R.id.product_name);
            productSku = itemView.findViewById(R.id.product_sku);
            salesPrice = itemView.findViewById(R.id.sales_price);
            purchasePrice = itemView.findViewById(R.id.purchase_price);
            stockQuantity = itemView.findViewById(R.id.stock_quantity);
            stockStatusIndicator = itemView.findViewById(R.id.stock_status_indicator);
        }
    }
}
