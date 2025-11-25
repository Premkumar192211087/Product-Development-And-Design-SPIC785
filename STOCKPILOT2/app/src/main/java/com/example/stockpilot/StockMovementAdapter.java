package com.example.stockpilot;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class StockMovementAdapter extends RecyclerView.Adapter<StockMovementAdapter.ViewHolder> {

    private List<StockMovement> stockMovements;

    public StockMovementAdapter(List<StockMovement> stockMovements) {
        this.stockMovements = stockMovements;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_stock_movement, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StockMovement movement = stockMovements.get(position);
        holder.tvProductName.setText(movement.getProductName());
        holder.tvMovementType.setText(movement.getMovementType());
        holder.tvQuantity.setText(String.valueOf(movement.getQuantity()));
        holder.tvDate.setText(movement.getTimestamp());
    }

    @Override
    public int getItemCount() {
        return stockMovements.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvProductName, tvMovementType, tvQuantity, tvDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvMovementType = itemView.findViewById(R.id.tvMovementType);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvDate = itemView.findViewById(R.id.tvTimestamp);
        }
    }
}