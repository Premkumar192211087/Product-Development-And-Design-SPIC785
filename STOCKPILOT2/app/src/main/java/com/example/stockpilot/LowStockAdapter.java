package com.example.stockpilot;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stockpilot.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class LowStockAdapter extends RecyclerView.Adapter<LowStockAdapter.LowStockViewHolder> {

    private JSONArray lowStockArray;

    public LowStockAdapter(JSONArray lowStockArray) {
        this.lowStockArray = lowStockArray;
    }

    @NonNull
    @Override
    public LowStockViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_low_stock, parent, false);
        return new LowStockViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull LowStockViewHolder holder, int position) {
        try {
            JSONObject obj = lowStockArray.getJSONObject(position);
            holder.tvProductName.setText(obj.getString("product_name"));
            holder.tvCurrentStock.setText("Qty: " + obj.getString("current_stock"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return lowStockArray != null ? lowStockArray.length() : 0;
    }

    public static class LowStockViewHolder extends RecyclerView.ViewHolder {
        TextView tvProductName, tvCurrentStock;

        public LowStockViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvCurrentStock = itemView.findViewById(R.id.tvCurrentStock);
        }
    }
}

