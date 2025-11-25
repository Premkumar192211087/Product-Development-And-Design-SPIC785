package com.example.stockpilot;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stockpilot.R;
import com.example.stockpilot.DeleteStaffActivity;
import com.example.stockpilot.staffmodel;

import java.util.ArrayList;

public class staffadapter extends RecyclerView.Adapter<staffadapter.StaffViewHolder> {

    private final ArrayList<staffmodel> staffList;
    private final OnDeleteClickListener deleteClickListener;

    public staffadapter(ArrayList<staffmodel> filteredStaffList, DeleteStaffActivity deleteClickListener, ArrayList<staffmodel> staffList, OnDeleteClickListener deleteClickListener1) {
        this.staffList = staffList;
        this.deleteClickListener = deleteClickListener1;
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(int staffId);
    }

    public staffadapter(ArrayList<staffmodel> staffList, OnDeleteClickListener deleteClickListener) {
        this.staffList = staffList;
        this.deleteClickListener = deleteClickListener;
    }

    @NonNull
    @Override
    public StaffViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_deleterecycler, parent, false);
        return new StaffViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StaffViewHolder holder, int position) {
        staffmodel staff = staffList.get(position);

        holder.tvName.setText("Name: " + staff.getFullName());
        holder.tvEmail.setText("Email: " + staff.getEmail());
        holder.tvPhone.setText("Phone: " + staff.getPhone());
        holder.tvRole.setText("Role: " + staff.getRole());
        holder.tvAddress.setText("Address: " + staff.getAddress());

        if (holder.btnDelete != null) {
            holder.btnDelete.setOnClickListener(v -> {
                if (deleteClickListener != null) {
                    deleteClickListener.onDeleteClick(staff.getStaffId());
                }
            });
        } else {
            Log.e("staffadapter", "btnDelete is null! Check activity_deleterecycler.xml for a Button with ID btn_delete_staff.");
        }
    }

    @Override
    public int getItemCount() {
        return staffList.size();
    }

    static class StaffViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvEmail, tvPhone, tvRole, tvAddress;
        Button btnDelete;

        public StaffViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_staff_name);
            tvEmail = itemView.findViewById(R.id.tv_staff_email);
            tvPhone = itemView.findViewById(R.id.tv_staff_phone);
            tvRole = itemView.findViewById(R.id.tv_staff_role);
            tvAddress = itemView.findViewById(R.id.tv_staff_address);
            btnDelete = itemView.findViewById(R.id.btn_delete_staff);
        }
    }
}

