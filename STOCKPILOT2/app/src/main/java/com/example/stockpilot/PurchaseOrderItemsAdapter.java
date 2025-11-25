package com.example.stockpilot;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for displaying purchase order items in a RecyclerView
 */
public class PurchaseOrderItemsAdapter extends RecyclerView.Adapter<PurchaseOrderItemsAdapter.ViewHolder> {

    private List<PurchaseOrderItemModel> itemList;
    private OnItemActionListener listener;
    private boolean isEditable;
    private NumberFormat currencyFormat;

    /**
     * Interface for item click and remove actions
     */
    public interface OnItemActionListener {
        void onItemClick(PurchaseOrderItemModel item, int position);
        void onRemoveItem(PurchaseOrderItemModel item, int position);
    }

    /**
     * Constructor
     *
     * @param itemList List of purchase order items
     * @param listener Listener for item actions
     * @param isEditable Whether items can be edited/removed
     */
    public PurchaseOrderItemsAdapter(List<PurchaseOrderItemModel> itemList, OnItemActionListener listener, boolean isEditable) {
        this.itemList = itemList;
        this.listener = listener;
        this.isEditable = isEditable;
        this.currencyFormat = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_purchase_order_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PurchaseOrderItemModel item = itemList.get(position);
        
        // Set item details
        holder.tvItemName.setText(item.getItemName());
        holder.tvItemDescription.setText(item.getItemDescription());
        holder.tvQuantity.setText(String.format(Locale.getDefault(), "%.2f", item.getQuantity()));
        holder.tvUnitPrice.setText(currencyFormat.format(item.getUnitPrice()));
        holder.tvTotal.setText(currencyFormat.format(item.getTotal()));
        
        // Set click listener for the item
        holder.itemView.setOnClickListener(v -> {
            if (listener != null && isEditable) {
                listener.onItemClick(item, holder.getAdapterPosition());
            }
        });
        
        // Show/hide remove button based on editability
        if (isEditable) {
            holder.btnRemove.setVisibility(View.VISIBLE);
            holder.btnRemove.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRemoveItem(item, holder.getAdapterPosition());
                }
            });
        } else {
            holder.btnRemove.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    /**
     * ViewHolder for purchase order items
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvItemName, tvItemDescription, tvQuantity, tvUnitPrice, tvTotal;
        ImageButton btnRemove;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvItemName = itemView.findViewById(R.id.tv_item_name);
            tvItemDescription = itemView.findViewById(R.id.tv_item_description);
            tvQuantity = itemView.findViewById(R.id.tv_quantity);
            tvUnitPrice = itemView.findViewById(R.id.tv_unit_price);
            tvTotal = itemView.findViewById(R.id.tv_total);
            btnRemove = itemView.findViewById(R.id.btn_remove);
        }
    }
}