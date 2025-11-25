package com.example.stockpilot;

import android.content.Context;
// removed: import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.stockpilot.R;
import com.google.android.material.card.MaterialCardView;
import java.util.List;

public class invoices_recycle extends RecyclerView.Adapter<invoices_recycle.InvoiceViewHolder> {
    private List<InvoiceModel> invoiceList;
    private Context context;
    private OnInvoiceClickListener listener;

    public interface OnInvoiceClickListener {
        void onInvoiceClick(InvoiceModel invoice);
        void onInvoiceLongClick(InvoiceModel invoice);
    }

    public invoices_recycle(Context context, List<InvoiceModel> invoiceList) {
        this.context = context;
        this.invoiceList = invoiceList;
    }

    public void setOnInvoiceClickListener(OnInvoiceClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public InvoiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.activity_invoices_recycle, parent, false);
        return new InvoiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InvoiceViewHolder holder, int position) {
        InvoiceModel invoice = invoiceList.get(position);

        holder.tvInvoiceNumber.setText(invoice.getInvoiceNumber());
        holder.tvCustomerName.setText(invoice.getCustomerName());
        holder.tvIssueDate.setText(invoice.getIssueDate());
        holder.tvDueDate.setText(invoice.getDueDate());
        holder.tvTotal.setText(invoice.getFormattedTotal());

        // Set status with appropriate color
        holder.tvStatus.setText(invoice.getDisplayStatus().toUpperCase());
        setStatusColor(holder.tvStatus, invoice.getDisplayStatus());

        // Set click listeners
        holder.cardView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onInvoiceClick(invoice);
            }
        });

        holder.cardView.setOnLongClickListener(v -> {
            if (listener != null) {
                listener.onInvoiceLongClick(invoice);
            }
            return true;
        });
    }

    private void setStatusColor(TextView statusView, String status) {
        int color = StatusColors.forInvoiceStatus(statusView.getContext(), status);
        statusView.setBackgroundColor(color);
    }

    @Override
    public int getItemCount() {
        return invoiceList.size();
    }

    public void updateInvoices(List<InvoiceModel> newInvoices) {
        this.invoiceList.clear();
        this.invoiceList.addAll(newInvoices);
        notifyDataSetChanged();
    }

    public static class InvoiceViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardView;
        TextView tvInvoiceNumber;
        TextView tvCustomerName;
        TextView tvStatus;
        TextView tvIssueDate;
        TextView tvDueDate;
        TextView tvTotal;

        public InvoiceViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.main);
            tvInvoiceNumber = itemView.findViewById(R.id.tv_invoice_number_detail);
            tvCustomerName = itemView.findViewById(R.id.tv_customer_name_invoice);
            tvStatus = itemView.findViewById(R.id.tv_invoice_status);
            tvIssueDate = itemView.findViewById(R.id.tv_invoice_issue_date);
            tvDueDate = itemView.findViewById(R.id.tv_invoice_due_date);
            tvTotal = itemView.findViewById(R.id.tv_invoice_total);
        }
    }
}
