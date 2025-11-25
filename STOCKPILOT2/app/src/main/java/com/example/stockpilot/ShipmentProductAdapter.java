package com.example.stockpilot;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stockpilot.R;
import com.example.stockpilot.ShipmentProduct;

import java.util.List;

public class ShipmentProductAdapter extends RecyclerView.Adapter<ShipmentProductAdapter.ViewHolder> {

    private List<ShipmentProduct> productList;

    public ShipmentProductAdapter(List<ShipmentProduct> productList) {
        this.productList = productList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_shipment_products, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ShipmentProduct product = productList.get(position);

        holder.tvProductName.setText(product.getProductName());
        holder.tvProductSku.setText("SKU: " + product.getSku());
        holder.tvBatchId.setText("Batch: " + (product.getBatchId() != null ? product.getBatchId() : "N/A"));
        holder.tvQuantityShipped.setText(String.valueOf(product.getQuantityShipped()));
        holder.tvUnitPrice.setText("₹" + String.format("%.2f", product.getUnitPrice()));
        holder.tvTotalValue.setText("₹" + String.format("%.2f", product.getTotalValue()));
    }

    @Override
    public int getItemCount() {
        return productList != null ? productList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvProductName, tvProductSku, tvBatchId, tvQuantityShipped, tvUnitPrice, tvTotalValue;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvProductSku = itemView.findViewById(R.id.tvProductSku);
            tvBatchId = itemView.findViewById(R.id.tvBatchId);
            tvQuantityShipped = itemView.findViewById(R.id.tvQuantityShipped);
            tvUnitPrice = itemView.findViewById(R.id.tvUnitPrice);
            tvTotalValue = itemView.findViewById(R.id.tvTotalValue);
        }
    }
}
