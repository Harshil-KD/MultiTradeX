package com.example.multitradex.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.multitradex.R;
import com.example.multitradex.models.Order;
import com.example.multitradex.utils.DateUtils;

import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private final Context context;
    private final List<Order> orderList;
    private final OnOrderClickListener listener;
    private final String userRole;

    public interface OnOrderClickListener {
        void onOrderClick(Order order);
    }

    public OrderAdapter(Context context, List<Order> orderList, String userRole, OnOrderClickListener listener) {
        this.context = context;
        this.orderList = orderList;
        this.userRole = userRole;
        this.listener = listener;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);

        holder.textOrderId.setText("Order ID: #" + order.getOrderId());
        holder.textOrderDate.setText("Date: " + DateUtils.formatTimestamp(order.getTimestamp()));
        holder.textTotalAmount.setText("Total: $" + String.format("%.2f", order.getTotalAmount()));

        String status = order.getPaymentStatus();
        holder.textPaymentStatus.setText("Status: " + status);

        switch (status) {
            case "SUCCESS":
                holder.textPaymentStatus.setTextColor(Color.parseColor("#2E7D32")); // Green
                break;
            case "PENDING":
                holder.textPaymentStatus.setTextColor(Color.parseColor("#FF8F00")); // Orange
                break;
            case "FAILED":
                holder.textPaymentStatus.setTextColor(Color.parseColor("#C62828")); // Red
                break;
            default:
                holder.textPaymentStatus.setTextColor(Color.DKGRAY);
        }

        // Only show shipping name for individuals
        if ("individual".equalsIgnoreCase(userRole)) {
            holder.textShippingName.setText("To: " + order.getShippingName());
            holder.textShippingName.setVisibility(View.VISIBLE);
        } else {
            holder.textShippingName.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> listener.onOrderClick(order));
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView textOrderId, textOrderDate, textTotalAmount, textPaymentStatus, textShippingName;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            textOrderId = itemView.findViewById(R.id.textOrderId);
            textOrderDate = itemView.findViewById(R.id.textOrderDate);
            textTotalAmount = itemView.findViewById(R.id.textTotalAmount);
            textPaymentStatus = itemView.findViewById(R.id.textPaymentStatus);
            textShippingName = itemView.findViewById(R.id.textShippingName);
        }
    }
}
