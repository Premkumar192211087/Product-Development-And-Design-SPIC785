package com.example.stockpilot;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stockpilot.R;
import com.example.stockpilot.AdjustmentItem;

import java.util.List;

public class AdjustmentAdapter extends RecyclerView.Adapter<AdjustmentAdapter.AdjustmentViewHolder> {

    private final Context context;
    private final List<AdjustmentItem> adjustmentList;

    public AdjustmentAdapter(Context context, List<AdjustmentItem> adjustmentList) {
        this.context = context;
        this.adjustmentList = adjustmentList;
    }

    @NonNull
    @Override
    public AdjustmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.activity_inventory_adjustment_reycler, parent, false);
        return new AdjustmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdjustmentViewHolder holder, int position) {
        AdjustmentItem currentItem = adjustmentList.get(position);

        holder.tvProductName.setText(currentItem.product_name);
        holder.tvMovementType.setText(currentItem.movement_type.toUpperCase());
        holder.tvQuantity.setText(currentItem.formatted_quantity);
        holder.tvUnitPrice.setText("₹" + String.format("%.2f", currentItem.unit_price));
        holder.tvReferenceType.setText("Reference Type: " + currentItem.reference_type);

        // Only show date portion if date exists
        if (currentItem.movement_date != null && currentItem.movement_date.contains(" ")) {
            holder.tvMovementDate.setText("Date: " + currentItem.movement_date.split(" ")[0]);
        } else {
            holder.tvMovementDate.setText("Date: " + currentItem.movement_date);
        }

        holder.tvPerformedBy.setText("Performed By: " + currentItem.performed_by);
    }

    @Override
    public int getItemCount() {
        return adjustmentList.size();
    }

    public static class AdjustmentViewHolder extends RecyclerView.ViewHolder {
        TextView tvProductName;
        TextView tvMovementType;
        TextView tvQuantity;
        TextView tvUnitPrice;
        TextView tvReferenceType;
        TextView tvMovementDate;
        TextView tvPerformedBy;

        public AdjustmentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvMovementType = itemView.findViewById(R.id.tvMovementType);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvUnitPrice = itemView.findViewById(R.id.tvUnitPrice);
            tvReferenceType = itemView.findViewById(R.id.tvReferenceType);
            tvMovementDate = itemView.findViewById(R.id.tvMovementDate);
            tvPerformedBy = itemView.findViewById(R.id.tvPerformedBy);
        }
    }
}
