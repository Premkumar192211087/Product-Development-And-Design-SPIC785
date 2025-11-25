package com.example.stockpilot;

import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stockpilot.R;

import java.util.List;

public class BatchListAdapter extends RecyclerView.Adapter<BatchListAdapter.BatchViewHolder> {

    private List<BatchListActivity.BatchItem> batchList;

    public BatchListAdapter(List<BatchListActivity.BatchItem> batchList) {
        this.batchList = batchList;
    }

    @NonNull
    @Override
    public BatchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_batch, parent, false);
        return new BatchViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BatchViewHolder holder, int position) {
        BatchListActivity.BatchItem batch = batchList.get(position);

        holder.productCodeText.setText(batch.getProductCode());
        holder.productNameText.setText(batch.getProductName());
        holder.manufacturingDateText.setText(batch.getManufacturingDate());
        holder.expiryDateText.setText(batch.getExpiryDate());
        holder.quantityText.setText(String.valueOf(batch.getQuantity()));
        holder.statusText.setText(batch.getStatus());

        // Set status background color based on status
        GradientDrawable statusBackground = (GradientDrawable) holder.statusText.getBackground();
        int statusColor;
        switch (batch.getStatus().toLowerCase()) {
            case "expired":
                statusColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.batch_status_expired);
                break;
            case "near_expiry":
                statusColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.batch_status_near_expiry);
                break;
            case "fresh":
                statusColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.batch_status_fresh);
                break;
            default:
                statusColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.medium_gray);
                break;
        }
        statusBackground.setColor(statusColor);
    }

    @Override
    public int getItemCount() {
        return batchList.size();
    }

    public void updateList(List<BatchListActivity.BatchItem> newList) {
        this.batchList = newList;
        notifyDataSetChanged();
    }

    public static class BatchViewHolder extends RecyclerView.ViewHolder {
        TextView productCodeText;
        TextView productNameText;
        TextView manufacturingDateText;
        TextView expiryDateText;
        TextView quantityText;
        TextView statusText;

        public BatchViewHolder(@NonNull View itemView) {
            super(itemView);
            productCodeText = itemView.findViewById(R.id.product_code_text);
            productNameText = itemView.findViewById(R.id.product_name_text);
            manufacturingDateText = itemView.findViewById(R.id.mfg_date_text);
            expiryDateText = itemView.findViewById(R.id.expiry_date_text);
            quantityText = itemView.findViewById(R.id.quantity_text);
            statusText = itemView.findViewById(R.id.status_text);
        }
    }
}