package com.example.stockpilot;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stockpilot.PurchaseOrderModel;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Currency;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PurchaseOrdersAdapter extends RecyclerView.Adapter<PurchaseOrdersAdapter.ViewHolder> {

    private final List<PurchaseOrderModel> purchaseOrders;
    private final OnPurchaseOrderClickListener listener;
    private final SimpleDateFormat inputDateFormat;
    private final SimpleDateFormat outputDateFormat;
    private final NumberFormat currencyFormat;

    /**
     * Interface for purchase order click actions
     */
    public interface OnPurchaseOrderClickListener {
        void onPurchaseOrderClick(PurchaseOrderModel order);
    }

    /**
     * Constructor
     *
     * @param purchaseOrders List of purchase orders
     * @param listener Listener for purchase order click actions
     */
    public PurchaseOrdersAdapter(List<PurchaseOrderModel> purchaseOrders, OnPurchaseOrderClickListener listener) {
        this.purchaseOrders = purchaseOrders;
        this.listener = listener;
        this.inputDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        this.outputDateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        this.currencyFormat = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
        this.currencyFormat.setCurrency(Currency.getInstance("INR"));
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_purchase_order, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PurchaseOrderModel order = purchaseOrders.get(position);
        
        // Set PO number and vendor name
        holder.tvPoNumber.setText(order.getPoNumber());
        holder.tvVendorName.setText("Supplier: " + order.getVendorName());
        
        // Format and set dates
        try {
            Date poDate = inputDateFormat.parse(order.getPoDate());
            
            if (poDate != null) {
                holder.tvPoDate.setText("Order Date: " + outputDateFormat.format(poDate));
            }
        } catch (ParseException e) {
            // Fallback to original format if parsing fails
            holder.tvPoDate.setText("Order Date: " + order.getPoDate());
        }
        
        // Format and set total
        holder.tvTotal.setText("Total: " + currencyFormat.format(order.getTotal()));
        
        // Set status with appropriate color
        holder.tvStatus.setText(order.getStatus());
        setStatusColor(holder.tvStatus, order.getStatus());
        
        // Set click listener
        holder.cardView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPurchaseOrderClick(order);
            }
        });
    }

    @Override
    public int getItemCount() {
        return purchaseOrders.size();
    }

    /**
     * Set the background color of the status badge based on the status
     *
     * @param tvStatus TextView to set color for
     * @param status Status text
     */
    private void setStatusColor(TextView tvStatus, String status) {
        int color = StatusColors.forPurchaseOrderStatus(tvStatus.getContext(), status);
        tvStatus.setBackgroundColor(color);
    }

    /**
     * ViewHolder class for purchase order items
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvPoNumber, tvVendorName, tvStatus, tvPoDate, tvTotal;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (CardView) itemView;
            tvPoNumber = itemView.findViewById(R.id.tv_po_number);
            tvVendorName = itemView.findViewById(R.id.tv_supplier);
            tvStatus = itemView.findViewById(R.id.tv_status);
            tvPoDate = itemView.findViewById(R.id.tv_order_date);
            tvTotal = itemView.findViewById(R.id.tv_total_amount);
        }
    }
}