package com.example.stockpilot;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stockpilot.R;
import com.example.stockpilot.SaleItemModel;

import java.util.List;

public class AddItemRecycler extends RecyclerView.Adapter<AddItemRecycler.ViewHolder> {

    private Context context;
    private List<SaleItemModel> itemList;
    private OnItemRemoveListener removeListener;
    
    public interface OnItemRemoveListener {
        void onRemove(int position);
    }

    public AddItemRecycler(Context context, List<SaleItemModel> itemList, OnItemRemoveListener removeListener) {
        this.context = context;
        this.itemList = itemList;
        this.removeListener = removeListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.activity_add_item_recyler, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SaleItemModel item = itemList.get(position);
        holder.tvProductName.setText(item.getProductName());
        holder.tvPrice.setText(String.format(java.util.Locale.getDefault(), "₹%.2f", item.getUnitPrice()));
        holder.etQuantity.setText(String.valueOf(item.getQuantity()));
        holder.tvDiscount.setText(String.format(java.util.Locale.getDefault(), "₹%.2f", item.getDiscountAmount()));
        holder.tvTotal.setText(String.format(java.util.Locale.getDefault(), "₹%.2f", item.getTotalPrice()));

        // Set up quantity change listener
        holder.etQuantity.removeTextChangedListener(holder.quantityWatcher);
        holder.quantityWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    int newQuantity = s.toString().isEmpty() ? 1 : Integer.parseInt(s.toString());
                    if (newQuantity <= 0) {
                        newQuantity = 1;
                        holder.etQuantity.setText(String.valueOf(newQuantity));
                    }
                    item.setQuantity(newQuantity);
                    updateTotalPrice(item, holder);
                } catch (NumberFormatException e) {
                    holder.etQuantity.setText("1");
                    item.setQuantity(1);
                    updateTotalPrice(item, holder);
                }
            }
        };
        holder.etQuantity.addTextChangedListener(holder.quantityWatcher);
        
        // Set up remove button
        holder.btnRemove.setOnClickListener(v -> {
            if (removeListener != null) {
                removeListener.onRemove(holder.getAdapterPosition());
            }
        });
    }

    private void updateTotalPrice(SaleItemModel item, ViewHolder holder) {
        double total = item.getQuantity() * item.getUnitPrice() - item.getDiscountAmount();
        item.setTotalPrice(total);
        holder.tvTotal.setText(String.format(java.util.Locale.getDefault(), "₹%.2f", total));
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvProductName, tvPrice, tvDiscount, tvTotal;
        EditText etQuantity;
        ImageButton btnRemove;
        TextWatcher quantityWatcher;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProductName = itemView.findViewById(R.id.tv_product_name);
            tvPrice = itemView.findViewById(R.id.tv_price);
            etQuantity = itemView.findViewById(R.id.et_quantity);
            tvDiscount = itemView.findViewById(R.id.tv_discount);
            tvTotal = itemView.findViewById(R.id.tv_total);
            btnRemove = itemView.findViewById(R.id.btn_remove);
        }
    }
}
