package com.example.stockpilot;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.core.content.ContextCompat;

import com.example.stockpilot.R;
import com.example.stockpilot.Shipment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ShipmentAdapter extends RecyclerView.Adapter<ShipmentAdapter.ShipmentViewHolder> {

    private Context context;
    private List<Shipment> shipmentList;
    private OnShipmentClickListener listener;
    private SimpleDateFormat inputDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private SimpleDateFormat displayDateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    public interface OnShipmentClickListener {
        void onViewDetailsClick(Shipment shipment);
        void onTrackShipmentClick(Shipment shipment);
    }

    public ShipmentAdapter(Context context, List<Shipment> shipmentList, OnShipmentClickListener listener) {
        this.context = context;
        this.shipmentList = shipmentList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ShipmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.activity_shipments_recycler, parent, false);
        return new ShipmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ShipmentViewHolder holder, int position) {
        Shipment shipment = shipmentList.get(position);

        holder.tvShipmentNumber.setText(shipment.getShipmentNumber());
        holder.tvOrderNumber.setText("Order: " + shipment.getOrderNumber());

        holder.tvShipmentStatus.setText(formatStatus(shipment.getStatus()));
        holder.tvShipmentStatus.setBackgroundColor(getStatusColor(shipment.getStatus()));

        String carrierInfo = shipment.getCarrierName();
        if (!shipment.getTrackingNumber().isEmpty()) {
            carrierInfo += " - " + shipment.getTrackingNumber();
        }
        holder.tvCarrierInfo.setText(carrierInfo);

        holder.tvRecipientName.setText(shipment.getRecipientName());

        if (shipment.getShipDate() != null && !shipment.getShipDate().isEmpty()) {
            holder.tvShipDate.setText("Shipped: " + formatDate(shipment.getShipDate()));
            holder.tvShipDate.setVisibility(View.VISIBLE);
        } else {
            holder.tvShipDate.setVisibility(View.GONE);
        }

        if (shipment.getEstimatedDeliveryDate() != null && !shipment.getEstimatedDeliveryDate().isEmpty()) {
            String deliveryText = "Est. Delivery: " + formatDate(shipment.getEstimatedDeliveryDate());
            if (shipment.isOverdue()) {
                deliveryText += " (OVERDUE)";
                holder.tvEstimatedDelivery.setTextColor(ContextCompat.getColor(context, R.color.error_red));
            } else {
                holder.tvEstimatedDelivery.setTextColor(ContextCompat.getColor(context, R.color.text_secondary));
            }
            holder.tvEstimatedDelivery.setText(deliveryText);
            holder.tvEstimatedDelivery.setVisibility(View.VISIBLE);
        } else {
            holder.tvEstimatedDelivery.setVisibility(View.GONE);
        }

        holder.tvShippingCost.setText("Shipping Cost: $" + String.format(Locale.getDefault(), "%.2f", shipment.getShippingCost()));

        holder.btnTrackShipment.setEnabled(shipment.canTrack());
        holder.btnTrackShipment.setAlpha(shipment.canTrack() ? 1.0f : 0.5f);

        holder.btnViewDetails.setOnClickListener(v -> {
            if (listener != null) listener.onViewDetailsClick(shipment);
        });

        holder.btnTrackShipment.setOnClickListener(v -> {
            if (listener != null) listener.onTrackShipmentClick(shipment);
        });
    }

    @Override
    public int getItemCount() {
        return shipmentList.size();
    }

    private String formatStatus(String status) {
        return status.replace("_", " ").toUpperCase();
    }

    private int getStatusColor(String status) {
        return StatusColors.forShipmentStatus(context, status);
    }

    private String formatDate(String dateString) {
        try {
            Date date = inputDateFormat.parse(dateString);
            return displayDateFormat.format(date);
        } catch (ParseException e) {
            return dateString;
        }
    }

    public static class ShipmentViewHolder extends RecyclerView.ViewHolder {
        TextView tvShipmentNumber, tvOrderNumber, tvShipmentStatus, tvCarrierInfo;
        TextView tvRecipientName, tvShipDate, tvEstimatedDelivery, tvShippingCost;
        Button btnViewDetails, btnTrackShipment;

        public ShipmentViewHolder(@NonNull View itemView) {
            super(itemView);

            tvShipmentNumber = itemView.findViewById(R.id.tvShipmentNumber);
            tvOrderNumber = itemView.findViewById(R.id.tvOrderNumber);
            tvShipmentStatus = itemView.findViewById(R.id.tvShipmentStatus);
            tvCarrierInfo = itemView.findViewById(R.id.tvCarrierInfo);
            tvRecipientName = itemView.findViewById(R.id.tvRecipientName);
            tvShipDate = itemView.findViewById(R.id.tvShipDate);
            tvEstimatedDelivery = itemView.findViewById(R.id.tvEstimatedDelivery);
            tvShippingCost = itemView.findViewById(R.id.tvShippingCost);
            btnViewDetails = itemView.findViewById(R.id.btnViewDetails);
            btnTrackShipment = itemView.findViewById(R.id.btnTrackShipment);
        }
    }
}

