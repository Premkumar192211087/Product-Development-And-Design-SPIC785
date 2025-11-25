package com.example.stockpilot;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class BillsAdapter extends RecyclerView.Adapter<BillsAdapter.BillViewHolder> {

    private List<Bills> billsList;
    private OnBillClickListener onBillClickListener;
    private Context context;

    public interface OnBillClickListener {
        void onBillClick(Bills bill);
        void onPayBillClick(Bills bill);
        void onPaymentClick(Bills bill);
    }

    public BillsAdapter(List<Bills> billsList, Context context, OnBillClickListener onBillClickListener) {
        this.billsList = billsList;
        this.context = context;
        this.onBillClickListener = onBillClickListener;
    }

    @NonNull
    @Override
    public BillViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bill, parent, false);
        return new BillViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BillViewHolder holder, int position) {
        Bills bill = billsList.get(position);
        holder.tvBillNumber.setText(bill.getBillNumber());
        holder.tvVendorName.setText(bill.getVendorName());
        holder.tvBillDate.setText(bill.getBillDate());
        holder.tvDueDate.setText(bill.getDueDate());
        holder.tvAmount.setText(String.format("$%.2f", bill.getAmount()));
        holder.tvStatus.setText(bill.getStatus());

        holder.itemView.setOnClickListener(v -> onBillClickListener.onBillClick(bill));
        holder.btnPay.setOnClickListener(v -> onBillClickListener.onPayBillClick(bill));
    }

    @Override
    public int getItemCount() {
        return billsList.size();
    }

    static class BillViewHolder extends RecyclerView.ViewHolder {
        TextView tvBillNumber, tvVendorName, tvBillDate, tvDueDate, tvAmount, tvStatus;
        Button btnPay;

        public BillViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBillNumber = itemView.findViewById(R.id.tv_bill_number);
            tvVendorName = itemView.findViewById(R.id.tv_vendor_name);
            tvBillDate = itemView.findViewById(R.id.tv_bill_date);
            tvDueDate = itemView.findViewById(R.id.tv_due_date);
            tvAmount = itemView.findViewById(R.id.tv_amount);
            tvStatus = itemView.findViewById(R.id.tv_status);
            btnPay = itemView.findViewById(R.id.btn_payment);
        }
    }
}