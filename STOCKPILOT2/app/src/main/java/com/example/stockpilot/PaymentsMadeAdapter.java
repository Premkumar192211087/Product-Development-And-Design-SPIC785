package com.example.stockpilot;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class PaymentsMadeAdapter extends RecyclerView.Adapter<PaymentsMadeAdapter.PaymentViewHolder> {

    private List<PaymentMade> paymentsList;
    private List<PaymentMade> paymentsListFull;
    private Context context;
    private OnPaymentClickListener listener;

    public PaymentsMadeAdapter(Context context, List<PaymentMade> paymentsList) {
        this.context = context;
        this.paymentsList = paymentsList;
        this.paymentsListFull = new ArrayList<>(paymentsList);
    }

    public interface OnItemClickListener {

        void onViewDetailsClick(com.example.stockpilot.PaymentMade payment);
    }
    
    private OnItemClickListener itemClickListener;
    
    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.itemClickListener = onItemClickListener;
    }

    public interface OnPaymentClickListener {
        void onPaymentClick(PaymentMade payment);
        void onEditPayment(PaymentMade payment);
        void onDeletePayment(PaymentMade payment);
    }

    public void setOnPaymentClickListener(OnPaymentClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public PaymentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_payment, parent, false);
        return new PaymentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PaymentViewHolder holder, int position) {
        PaymentMade payment = paymentsList.get(position);
        
        holder.billNumberTextView.setText(payment.getBillNumber());
        holder.supplierNameTextView.setText(payment.getSupplierName());
        holder.paymentDateTextView.setText("Payment Date: " + payment.getPaymentDate());
        holder.paymentMethodTextView.setText("Method: " + payment.getPaymentMethod());
        holder.amountTextView.setText(String.format("$%.2f", payment.getAmount()));
        
        holder.statusTextView.setText(payment.getStatus());
        
        // Set status background color based on status
        int statusColor;
        switch (payment.getStatus().toLowerCase()) {
            case "completed":
                statusColor = ContextCompat.getColor(context, R.color.success);
                break;
            case "pending":
                statusColor = ContextCompat.getColor(context, R.color.zxing_custom_possible_result_points);
                break;
            case "cancelled":
                statusColor = ContextCompat.getColor(context, R.color.colorError);
                break;
            default:
                statusColor = ContextCompat.getColor(context, R.color.colorPrimary);
                break;
        }
        holder.statusTextView.getBackground().setTint(statusColor);
        
        // Set click listeners
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPaymentClick(payment);
            }
        });
        
        holder.editButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditPayment(payment);
            }
        });
        
        holder.deleteButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeletePayment(payment);
            }
        });
    }

    @Override
    public int getItemCount() {
        return paymentsList.size();
    }

    public void updateList(List<PaymentMade> newPayments) {
        paymentsList.clear();
        paymentsList.addAll(newPayments);
        paymentsListFull.clear();
        paymentsListFull.addAll(newPayments);
        notifyDataSetChanged();
    }
    
    // For backward compatibility
    public void updateData(List<PaymentMade> newPayments) {
        updateList(newPayments);
    }

    public void filter(String query, String status) {
        List<PaymentMade> filteredList = new ArrayList<>();
        
        for (PaymentMade payment : paymentsListFull) {
            boolean matchesQuery = query.isEmpty() || 
                    payment.getBillNumber().toLowerCase().contains(query.toLowerCase()) ||
                    payment.getSupplierName().toLowerCase().contains(query.toLowerCase()) ||
                    payment.getReferenceNumber().toLowerCase().contains(query.toLowerCase());
            
            boolean matchesStatus = status.equals("All") || 
                    payment.getStatus().equalsIgnoreCase(status);
            
            if (matchesQuery && matchesStatus) {
                filteredList.add(payment);
            }
        }
        
        paymentsList.clear();
        paymentsList.addAll(filteredList);
        notifyDataSetChanged();
    }

    public void sort(String sortBy) {
        switch (sortBy.toLowerCase()) {
            case "date (newest)":
                paymentsList.sort((p1, p2) -> p2.getPaymentDate().compareTo(p1.getPaymentDate()));
                break;
            case "date (oldest)":
                paymentsList.sort((p1, p2) -> p1.getPaymentDate().compareTo(p2.getPaymentDate()));
                break;
            case "amount (highest)":
                paymentsList.sort((p1, p2) -> Double.compare(p2.getAmount(), p1.getAmount()));
                break;
            case "amount (lowest)":
                paymentsList.sort((p1, p2) -> Double.compare(p1.getAmount(), p2.getAmount()));
                break;
            case "bill number":
                paymentsList.sort((p1, p2) -> p1.getBillNumber().compareTo(p2.getBillNumber()));
                break;
            default:
                // Default sort by date (newest)
                paymentsList.sort((p1, p2) -> p2.getPaymentDate().compareTo(p1.getPaymentDate()));
                break;
        }
        notifyDataSetChanged();
    }

    static class PaymentViewHolder extends RecyclerView.ViewHolder {
        TextView billNumberTextView;
        TextView supplierNameTextView;
        TextView paymentDateTextView;
        TextView paymentMethodTextView;
        TextView amountTextView;
        TextView statusTextView;
        ImageButton editButton;
        ImageButton deleteButton;

        PaymentViewHolder(@NonNull View itemView) {
            super(itemView);
            billNumberTextView = itemView.findViewById(R.id.tv_bill_number);
            supplierNameTextView = itemView.findViewById(R.id.tv_supplier_name);
            paymentDateTextView = itemView.findViewById(R.id.tv_payment_date);
            paymentMethodTextView = itemView.findViewById(R.id.tv_payment_method);
            amountTextView = itemView.findViewById(R.id.tv_amount);
            statusTextView = itemView.findViewById(R.id.tv_status);
            editButton = itemView.findViewById(R.id.btn_edit);
            deleteButton = itemView.findViewById(R.id.btn_delete);
        }
    }
}