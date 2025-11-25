package com.example.stockpilot;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

public class PurchaseStatusAdapter extends RecyclerView.Adapter<PurchaseStatusAdapter.ViewHolder> {

    private List<PurchaseReportsActivity.PurchaseStatus> purchaseStatuses;

    public PurchaseStatusAdapter(List<PurchaseReportsActivity.PurchaseStatus> purchaseStatuses) {
        this.purchaseStatuses = purchaseStatuses;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_purchase_status, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PurchaseReportsActivity.PurchaseStatus purchaseStatus = purchaseStatuses.get(position);
        holder.tvStatus.setText(purchaseStatus.getStatus());
        holder.tvCount.setText(String.valueOf(purchaseStatus.getCount()));
        holder.tvPercentage.setText(String.format(Locale.getDefault(), "%.1f%%", purchaseStatus.getPercentage()));
        holder.progressBar.setProgress((int) purchaseStatus.getPercentage());
    }

    @Override
    public int getItemCount() {
        return purchaseStatuses.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvStatus, tvCount, tvPercentage;
        ProgressBar progressBar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvCount = itemView.findViewById(R.id.tvCount);
            tvPercentage = itemView.findViewById(R.id.tvPercentage);
            progressBar = itemView.findViewById(R.id.progressBar);
        }
    }
}