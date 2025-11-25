package com.example.stockpilot;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

public class VendorDistributionAdapter extends RecyclerView.Adapter<VendorDistributionAdapter.ViewHolder> {

    private List<PurchaseReportsActivity.VendorDistribution> vendorDistributions;
    private NumberFormat currencyFormat;

    public VendorDistributionAdapter(List<PurchaseReportsActivity.VendorDistribution> vendorDistributions) {
        this.vendorDistributions = vendorDistributions;
        this.currencyFormat = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
        this.currencyFormat.setCurrency(Currency.getInstance("INR"));
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_vendor_distribution, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PurchaseReportsActivity.VendorDistribution vendorDistribution = vendorDistributions.get(position);

        // Set vendor name with null check
        String vendorName = vendorDistribution.getVendorName();
        holder.tvVendorName.setText(vendorName != null ? vendorName : "Unknown Vendor");

        // Format and set amount
        holder.tvAmount.setText(currencyFormat.format(vendorDistribution.getAmount()));

        // Format percentage with proper rounding
        double percentage = vendorDistribution.getPercentage();
        holder.tvPercentage.setText(String.format(Locale.getDefault(), "%.1f%%", percentage));

        // Set progress bar with bounds checking
        int progress = Math.max(0, Math.min(100, (int) Math.round(percentage)));
        holder.progressBar.setProgress(progress);
    }

    @Override
    public int getItemCount() {
        return vendorDistributions != null ? vendorDistributions.size() : 0;
    }

    // Method to update data
    public void updateData(List<PurchaseReportsActivity.VendorDistribution> newVendorDistributions) {
        this.vendorDistributions = newVendorDistributions;
        notifyDataSetChanged();
    }

    // Method to add single item
    public void addItem(PurchaseReportsActivity.VendorDistribution vendorDistribution) {
        if (vendorDistributions != null) {
            vendorDistributions.add(vendorDistribution);
            notifyItemInserted(vendorDistributions.size() - 1);
        }
    }

    // Method to remove item
    public void removeItem(int position) {
        if (vendorDistributions != null && position >= 0 && position < vendorDistributions.size()) {
            vendorDistributions.remove(position);
            notifyItemRemoved(position);
        }
    }

    // Method to clear all data
    public void clearData() {
        if (vendorDistributions != null) {
            int size = vendorDistributions.size();
            vendorDistributions.clear();
            notifyItemRangeRemoved(0, size);
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvVendorName, tvAmount, tvPercentage;
        ProgressBar progressBar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvVendorName = itemView.findViewById(R.id.tvVendorName);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvPercentage = itemView.findViewById(R.id.tvPercentage);
            progressBar = itemView.findViewById(R.id.progressBar);
        }
    }
}