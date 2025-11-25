package com.example.stockpilot;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stockpilot.R;
import com.example.stockpilot.ShipmentItem;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ShipmentItemAdapter extends RecyclerView.Adapter<ShipmentItemAdapter.ViewHolder> {

    private List<ShipmentItem> shipmentItems;
    private Context context;
    private OnItemClickListener listener;
    private NumberFormat currencyFormatter;

    // Interface for click events
    public interface OnItemClickListener {
        void onRemoveClick(int position);
    }

    public ShipmentItemAdapter(Context context, OnItemClickListener listener) {
        this.context = context;
        this.listener = listener;
        this.shipmentItems = new ArrayList<>();
        this.currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_shipment_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ShipmentItem item = shipmentItems.get(position);
        
        // Set product details
        holder.tvProductName.setText(item.getProductName());
        holder.tvProductSku.setText("SKU: " + item.getProductSku());
        
        // Set batch ID if available
        if (item.getBatchId() != null && !item.getBatchId().isEmpty()) {
            holder.tvBatchId.setVisibility(View.VISIBLE);
            holder.tvBatchId.setText("Batch: " + item.getBatchId());
        } else {
            holder.tvBatchId.setVisibility(View.GONE);
        }
        
        // Set quantity and price details
        holder.tvQuantity.setText(String.valueOf(item.getQuantityShipped()));
        holder.tvUnitPrice.setText(currencyFormatter.format(item.getUnitPrice()));
        holder.tvTotalValue.setText(currencyFormatter.format(item.getTotalValue()));
        
        // Set click listener for remove button
        holder.btnRemoveItem.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRemoveClick(holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return shipmentItems.size();
    }

    // Add a new item to the list
    public void addItem(ShipmentItem item) {
        shipmentItems.add(item);
        notifyItemInserted(shipmentItems.size() - 1);
    }

    // Remove an item from the list
    public void removeItem(int position) {
        if (position >= 0 && position < shipmentItems.size()) {
            shipmentItems.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, shipmentItems.size());
        }
    }

    // Get all items in the list
    public List<ShipmentItem> getAllItems() {
        return new ArrayList<>(shipmentItems);
    }

    // Clear all items from the list
    public void clearItems() {
        shipmentItems.clear();
        notifyDataSetChanged();
    }

    // Check if the list is empty
    public boolean isEmpty() {
        return shipmentItems.isEmpty();
    }

    // Calculate total value of all items
    public double getTotalValue() {
        double total = 0;
        for (ShipmentItem item : shipmentItems) {
            total += item.getTotalValue();
        }
        return total;
    }

    // ViewHolder class
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvProductName, tvProductSku, tvBatchId;
        TextView tvQuantity, tvUnitPrice, tvTotalValue;
        ImageButton btnRemoveItem;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProductName = itemView.findViewById(R.id.tv_product_name);
            tvProductSku = itemView.findViewById(R.id.tv_product_sku);
            tvBatchId = itemView.findViewById(R.id.tv_batch_id);
            tvQuantity = itemView.findViewById(R.id.tv_quantity);
            tvUnitPrice = itemView.findViewById(R.id.tv_unit_price);
            tvTotalValue = itemView.findViewById(R.id.tv_total_value);
            btnRemoveItem = itemView.findViewById(R.id.btn_remove_item);
        }
    }
}
