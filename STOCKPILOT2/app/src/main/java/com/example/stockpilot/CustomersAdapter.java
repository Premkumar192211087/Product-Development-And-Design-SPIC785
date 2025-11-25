package com.example.stockpilot;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CustomersAdapter extends RecyclerView.Adapter<CustomersAdapter.CustomerViewHolder> {

    private List<Customer> customers;
    private OnCustomerActionListener listener;

    public interface OnCustomerActionListener {
        void onEditCustomer(Customer customer);
        void onDeleteCustomer(Customer customer);
        void onToggleCustomerStatus(Customer customer); // Method for status toggle
    }

    public CustomersAdapter(CustomersActivity customersActivity, List<Customer> customers, OnCustomerActionListener listener) {
        this.customers = customers;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CustomerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_customerss_recycler, parent, false);
        return new CustomerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomerViewHolder holder, int position) {
        Customer customer = customers.get(position);
        holder.bind(customer);
    }

    @Override
    public int getItemCount() {
        return customers.size();
    }

    // Method to update customer status in the list
    public void updateCustomerStatus(int customerId, String newStatus) {
        for (int i = 0; i < customers.size(); i++) {
            Customer customer = customers.get(i);
            if (customer.getCustomerId() == customerId) {
                customer.setStatus(newStatus);
                notifyItemChanged(i);
                break;
            }
        }
    }

    // Method to remove customer from the list
    public void removeCustomer(int customerId) {
        for (int i = 0; i < customers.size(); i++) {
            Customer customer = customers.get(i);
            if (customer.getCustomerId() == customerId) {
                customers.remove(i);
                notifyItemRemoved(i);
                break;
            }
        }
    }

    class CustomerViewHolder extends RecyclerView.ViewHolder {
        private TextView tvAvatar, tvCustomerName, tvStatus, tvEmail, tvPhone, tvLastPurchase;
        private ImageButton btnEdit, btnDelete;

        public CustomerViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAvatar = itemView.findViewById(R.id.tv_avatar);
            tvCustomerName = itemView.findViewById(R.id.tv_customer_name);
            tvStatus = itemView.findViewById(R.id.tv_status);
            tvEmail = itemView.findViewById(R.id.tv_email);
            tvPhone = itemView.findViewById(R.id.tv_phone);
            tvLastPurchase = itemView.findViewById(R.id.tv_last_purchase);
            btnEdit = itemView.findViewById(R.id.btn_edit);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }

        public void bind(Customer customer) {
            tvAvatar.setText(customer.getAvatarLetter());
            tvCustomerName.setText(customer.getName());
            tvStatus.setText(customer.getStatus());
            tvEmail.setText(customer.getEmail());
            tvPhone.setText(customer.getPhone());
            tvLastPurchase.setText("Last: " + customer.getLastPurchaseDisplay());

            // Set status badge background and text color
            if ("Active".equals(customer.getStatus())) {
                // Use the status_active drawable as background
                tvStatus.setBackground(ContextCompat.getDrawable(itemView.getContext(), R.drawable.status_active));
                // Set white text color for better contrast
                tvStatus.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.white));
            } else {
                // Use the status_inactive drawable as background
                tvStatus.setBackground(ContextCompat.getDrawable(itemView.getContext(), R.drawable.status_inactive));
                // Set white text color for better contrast
                tvStatus.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.white));
            }

            // Add some padding to make the badge look better
            int padding = (int) (8 * itemView.getContext().getResources().getDisplayMetrics().density); // 8dp
            tvStatus.setPadding(padding, padding/2, padding, padding/2);

            // Make status clickable
            tvStatus.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onToggleCustomerStatus(customer);
                }
            });

            // Add click effect to status badge
            tvStatus.setClickable(true);
            tvStatus.setFocusable(true);
            tvStatus.setBackground(ContextCompat.getDrawable(itemView.getContext(),
                    "Active".equals(customer.getStatus()) ? R.drawable.status_active : R.drawable.status_inactive));

            btnEdit.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditCustomer(customer);
                }
            });

            btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteCustomer(customer);
                }
            });
        }
    }
}
