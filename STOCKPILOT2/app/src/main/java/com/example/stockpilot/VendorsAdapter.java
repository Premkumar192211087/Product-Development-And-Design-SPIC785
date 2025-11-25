package com.example.stockpilot;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stockpilot.R;
import com.example.stockpilot.Vendor;

import java.util.List;

public class VendorsAdapter extends RecyclerView.Adapter<VendorsAdapter.VendorViewHolder> {

    private List<Vendor> vendors;
    private OnVendorActionListener listener;

    public interface OnVendorActionListener {
        void onEditVendor(Vendor vendor);
        void onDeleteVendor(Vendor vendor);
    }

    public VendorsAdapter(List<Vendor> vendors, OnVendorActionListener listener) {
        this.vendors = vendors;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VendorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_vendor, parent, false);
        return new VendorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VendorViewHolder holder, int position) {
        Vendor vendor = vendors.get(position);
        holder.bind(vendor);
    }

    @Override
    public int getItemCount() {
        return vendors.size();
    }

    class VendorViewHolder extends RecyclerView.ViewHolder {
        private TextView tvAvatar, tvVendorName, tvStatus, tvContactPerson, tvEmail, tvPhone, tvOutstandingBalance;
        private ImageButton btnEdit, btnDelete;

        public VendorViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAvatar = itemView.findViewById(R.id.tv_avatar);
            tvVendorName = itemView.findViewById(R.id.tv_vendor_name);
            tvStatus = itemView.findViewById(R.id.tv_status);
            tvContactPerson = itemView.findViewById(R.id.tv_contact_person);
            tvEmail = itemView.findViewById(R.id.tv_email);
            tvPhone = itemView.findViewById(R.id.tv_phone);
            tvOutstandingBalance = itemView.findViewById(R.id.tv_outstanding_balance);
            btnEdit = itemView.findViewById(R.id.btn_edit);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }

        public void bind(Vendor vendor) {
            // Set avatar letter (first letter of vendor name)
            String avatarLetter = vendor.getName().isEmpty() ? "?" : vendor.getName().substring(0, 1).toUpperCase();
            tvAvatar.setText(avatarLetter);
            
            tvVendorName.setText(vendor.getName());
            
            // Set status with appropriate styling
            tvStatus.setText(vendor.getStatus());
            if (vendor.getStatus().equalsIgnoreCase("active")) {
                tvStatus.setBackgroundResource(R.drawable.status_active);
                tvStatus.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.white));
            } else {
                tvStatus.setBackgroundResource(R.drawable.status_inactive);
                tvStatus.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.white));
            }
            
            tvContactPerson.setText(vendor.getContactPerson());
            tvEmail.setText(vendor.getEmail());
            tvPhone.setText(vendor.getPhone());
            
            // Format outstanding balance with currency symbol
            String formattedBalance = "$" + vendor.getOutstandingBalance();
            tvOutstandingBalance.setText("Outstanding: " + formattedBalance);

            // Set click listeners for actions
            btnEdit.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditVendor(vendor);
                }
            });

            btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteVendor(vendor);
                }
            });
            
            // Set click listener for the entire item
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditVendor(vendor);
                }
            });
        }
    }
}