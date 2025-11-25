package com.example.stockpilot;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stockpilot.R;
import com.example.stockpilot.SaleItem;
import com.example.stockpilot.SaleItemModel;
import com.example.stockpilot.SalesOrder;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

public class SalesOrderAdapter extends RecyclerView.Adapter<SalesOrderAdapter.ViewHolder> {

    private List<SalesOrder> salesOrders;
    private final Context context;

    public SalesOrderAdapter(List<SalesOrder> salesOrders, Context context) {
        this.salesOrders = salesOrders;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.activity_sales_order_recyler, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SalesOrder order = salesOrders.get(position);
        holder.tvInvoiceNumber.setText(order.getInvoiceNumber());
        holder.tvCustomerName.setText(order.getCustomerName());
        holder.tvSaleDate.setText(order.getSaleDate());
        holder.tvFinalAmount.setText("₹" + String.format("%.2f", order.getFinalAmount()));
        holder.tvPaymentMethod.setText(order.getPaymentMethod());
        holder.tvPaymentStatus.setText(order.getPaymentStatus().toUpperCase());

        List<SaleItem> items = order.getItems();
        holder.tvItemsCount.setText((items != null ? items.size() : 0) + " items");
        holder.tvServedBy.setText(order.getServedBy());

        holder.btnPrintReceipt.setOnClickListener(v -> generateAndOpenPdf(order));
        
        // Add click listener for View Details button
        holder.btnViewDetails.setOnClickListener(v -> {
            try {
                Class<?> salesOrderDetailsClass = Class.forName("com.example.stockpilot.activities.SalesOrderDetailsActivity");
                Intent intent = new Intent(context, salesOrderDetailsClass);
                intent.putExtra("invoice_number", order.getInvoiceNumber());
                context.startActivity(intent);
            } catch (ClassNotFoundException e) {
                Toast.makeText(context, "Order details view not available", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return salesOrders.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvInvoiceNumber, tvCustomerName, tvSaleDate, tvFinalAmount,
                tvPaymentMethod, tvPaymentStatus, tvItemsCount, tvServedBy;
        Button btnViewDetails, btnPrintReceipt;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvInvoiceNumber = itemView.findViewById(R.id.tv_invoice_number);
            tvCustomerName = itemView.findViewById(R.id.tv_customer_name);
            tvSaleDate = itemView.findViewById(R.id.tv_sale_date);
            tvFinalAmount = itemView.findViewById(R.id.tv_final_amount);
            tvPaymentMethod = itemView.findViewById(R.id.tv_payment_method);
            tvPaymentStatus = itemView.findViewById(R.id.tv_payment_status);
            tvItemsCount = itemView.findViewById(R.id.tv_items_count);
            tvServedBy = itemView.findViewById(R.id.tv_served_by);
            btnViewDetails = itemView.findViewById(R.id.btnViewDetails);
            btnPrintReceipt = itemView.findViewById(R.id.btn_print_receipt);
        }
    }

    private void generateAndOpenPdf(SalesOrder order) {
        String fileName = "Invoice_" + order.getInvoiceNumber() + ".pdf";
        File pdfDir = new File(context.getExternalFilesDir(null), "invoices");
        if (!pdfDir.exists()) pdfDir.mkdirs();

        File pdfFile = new File(pdfDir, fileName);

        try {
            PdfDocument document = new PdfDocument();
            Paint paint = new Paint();
            Paint boldPaint = new Paint();
            boldPaint.setFakeBoldText(true);
            boldPaint.setTextSize(14);

            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
            PdfDocument.Page page = document.startPage(pageInfo);
            Canvas canvas = page.getCanvas();

            int y = 40;

            // Header
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setTextSize(20);
            canvas.drawText("STOCKPILOT", pageInfo.getPageWidth() / 2, y, paint);
            y += 25;
            paint.setTextSize(16);
            canvas.drawText("Sales Invoice Report", pageInfo.getPageWidth() / 2, y, paint);
            y += 30;

            paint.setTextAlign(Paint.Align.LEFT);
            paint.setTextSize(13);
            canvas.drawText("Invoice #: " + order.getInvoiceNumber(), 20, y, paint); y += 18;
            canvas.drawText("Date     : " + order.getSaleDate(), 20, y, paint); y += 18;
            canvas.drawText("Customer : " + order.getCustomerName(), 20, y, paint); y += 18;
            canvas.drawText("Payment  : " + order.getPaymentMethod(), 20, y, paint); y += 18;
            canvas.drawText("Status   : " + order.getPaymentStatus(), 20, y, paint); y += 18;
            canvas.drawText("Served By: " + order.getServedBy(), 20, y, paint); y += 25;

            // Items header
            canvas.drawLine(20, y, pageInfo.getPageWidth() - 20, y, paint); y += 15;
            canvas.drawText("Items:", 20, y, boldPaint); y += 20;

            // Table Header
            boldPaint.setTextSize(13);
            canvas.drawText("Product", 20, y, boldPaint);
            canvas.drawText("Qty", 220, y, boldPaint);
            canvas.drawText("Price", 300, y, boldPaint);
            canvas.drawText("Subtotal", 420, y, boldPaint);
            y += 15;
            canvas.drawLine(20, y, pageInfo.getPageWidth() - 20, y, paint); y += 15;

            paint.setTextSize(13);

            List<SaleItem> items = order.getItems();
            if (items != null) {
                for (SaleItem item : items) {
                    canvas.drawText(item.getProductName(), 20, y, paint);
                    canvas.drawText("x" + item.getQuantity(), 220, y, paint);
                    canvas.drawText("₹" + String.format("%.2f", item.getUnitPrice()), 300, y, paint);
                    canvas.drawText("₹" + String.format("%.2f", item.getTotalPrice()), 420, y, paint);
                    y += 18;
                }
            }

            y += 10;
            canvas.drawLine(20, y, pageInfo.getPageWidth() - 20, y, paint); y += 20;

            boldPaint.setTextSize(14);
            canvas.drawText("Total: ₹" + String.format("%.2f", order.getFinalAmount()), 20, y, boldPaint);
            y += 40;

            // Footer
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setTextSize(12);
            canvas.drawText("Thank you for your purchase!", pageInfo.getPageWidth() / 2, y, paint); y += 15;
            canvas.drawText("Powered by StockPilot", pageInfo.getPageWidth() / 2, y, paint); y += 15;
            canvas.drawText("www.stockpilot.in | support@stockpilot.in", pageInfo.getPageWidth() / 2, y, paint);

            document.finishPage(page);
            document.writeTo(new FileOutputStream(pdfFile));
            document.close();

            Uri uri = FileProvider.getUriForFile(context,
                    context.getPackageName() + ".provider", pdfFile);

            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("application/pdf");
            share.putExtra(Intent.EXTRA_STREAM, uri);
            share.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            context.startActivity(Intent.createChooser(share, "Share Invoice PDF"));

        } catch (Exception e) {
            Log.e("PDF", "PDF generation error", e);
            Toast.makeText(context, "Error creating PDF", Toast.LENGTH_SHORT).show();
        }
    }
}

