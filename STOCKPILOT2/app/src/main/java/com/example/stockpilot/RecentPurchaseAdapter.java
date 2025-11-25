package com.example.stockpilot;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RecentPurchaseAdapter extends RecyclerView.Adapter<RecentPurchaseAdapter.ViewHolder> {
    private List<PurchaseReportsActivity.PurchaseOrder> recentPurchases;

    public RecentPurchaseAdapter(List<PurchaseReportsActivity.PurchaseOrder> recentPurchases) {
        this.recentPurchases = recentPurchases;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvPoId, tvVendorName, tvOrderDate, tvTotalAmount;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPoId = itemView.findViewById(R.id.tvPoId);
            tvVendorName = itemView.findViewById(R.id.tvVendorName);
            tvOrderDate = itemView.findViewById(R.id.tvOrderDate);
            tvTotalAmount = itemView.findViewById(R.id.tvTotalAmount);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recent_purchase, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PurchaseReportsActivity.PurchaseOrder order = recentPurchases.get(position);
        holder.tvPoId.setText(order.getPoId());
        holder.tvVendorName.setText(order.getVendorName());
        holder.tvOrderDate.setText(order.getOrderDate());
        holder.tvTotalAmount.setText(String.format("₹%.2f", order.getTotalAmount()));
    }

    @Override
    public int getItemCount() {
        return recentPurchases != null ? recentPurchases.size() : 0;
    }
}
