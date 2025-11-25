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

import java.text.BreakIterator;

public class InvoiceAdapter extends RecyclerView.Adapter<InvoiceAdapter.InvoiceViewHolder> {

    private JSONArray invoiceArray;

    public InvoiceAdapter(JSONArray invoiceArray) {
        this.invoiceArray = invoiceArray;
    }

    @NonNull
    @Override
    public InvoiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_invoice, parent, false);
        return new InvoiceViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull InvoiceViewHolder holder, int position) {
        try {
            JSONObject obj = invoiceArray.getJSONObject(position);
            holder.tvInvoiceNumber.setText(obj.getString("invoice_number"));
            holder.tvInvoiceAmount.setText("₹ " + obj.getString("total_amount"));
            holder.tvInvoiceDate.setText(obj.getString("date"));
            
            // Add customer name if available
            if (obj.has("customer_name") && !obj.isNull("customer_name")) {
                holder.tvInvoiceCustomer.setText(obj.getString("customer_name"));
            } else {
                holder.tvInvoiceCustomer.setText("N/A");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return invoiceArray != null ? invoiceArray.length() : 0;
    }

    public static class InvoiceViewHolder extends RecyclerView.ViewHolder {
        public BreakIterator tvInvoiceCustomer;
        TextView tvInvoiceNumber, tvInvoiceAmount, tvInvoiceDate;

        public InvoiceViewHolder(@NonNull View itemView) {
            super(itemView);
            tvInvoiceNumber = itemView.findViewById(R.id.tvInvoiceNumber);
            tvInvoiceAmount = itemView.findViewById(R.id.tvInvoiceAmount);
            tvInvoiceDate = itemView.findViewById(R.id.tvInvoiceDate);
        }
    }
}
