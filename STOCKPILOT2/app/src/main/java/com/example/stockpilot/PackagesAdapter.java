package com.example.stockpilot;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stockpilot.R;
import com.example.stockpilot.PackagesActivity;
import com.example.stockpilot.Package;

import java.util.List;

public class PackagesAdapter extends RecyclerView.Adapter<PackagesAdapter.PackageViewHolder> {

    private Context context;
    private List<Package> packagesList;

    public PackagesAdapter(Context context, List<Package> packagesList) {
        this.context = context;
        this.packagesList = packagesList;
    }

    public PackagesAdapter(PackagesActivity context, List<Package> packagesList) {
    }

    @NonNull
    @Override
    public PackageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.activity_packages_recycler, parent, false);
        return new PackageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PackageViewHolder holder, int position) {
        Package pkg = packagesList.get(position);

        holder.tvProductName.setText(pkg.getProductName());
        holder.tvPackageStatus.setText(pkg.getType());
        holder.tvPackageQuantity.setText(String.valueOf(pkg.getQuantity()));
        holder.tvPackagePrice.setText("₹" + pkg.getPrice());
        holder.tvPackageTimestamp.setText(pkg.getTimestamp());

        // Set status background color based on type
        setStatusBackground(holder.tvPackageStatus, pkg.getType());
    }

    private void setStatusBackground(TextView statusView, String status) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setCornerRadius(12f);

        int color = StatusColors.forShipmentStatus(context, status);
        drawable.setColor(color);
        statusView.setBackground(drawable);
    }

    @Override
    public int getItemCount() {
        return packagesList.size();
    }

    public static class PackageViewHolder extends RecyclerView.ViewHolder {
        TextView tvProductName, tvPackageStatus, tvPackageQuantity, tvPackagePrice, tvPackageTimestamp;

        public PackageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProductName = itemView.findViewById(R.id.tv_product_name_package);
            tvPackageStatus = itemView.findViewById(R.id.tv_package_status);
            tvPackageQuantity = itemView.findViewById(R.id.tv_package_quantity);
            tvPackagePrice = itemView.findViewById(R.id.tv_package_price);
            tvPackageTimestamp = itemView.findViewById(R.id.tv_package_timestamp);
        }
    }
}
