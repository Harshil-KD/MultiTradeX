package com.example.multitradex.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.multitradex.R;
import com.example.multitradex.models.OrderItem;

import java.util.List;

public class OrderItemAdapter extends RecyclerView.Adapter<OrderItemAdapter.OrderItemViewHolder> {

    private final Context context;
    private final List<OrderItem> itemList;

    public OrderItemAdapter(Context context, List<OrderItem> itemList) {
        this.context = context;
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public OrderItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order_item, parent, false);
        return new OrderItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderItemViewHolder holder, int position) {
        OrderItem item = itemList.get(position);

        holder.textProductName.setText(item.getProductName());
        holder.textQuantity.setText("Qty: " + item.getQuantity());
        holder.textUnitPrice.setText("Unit Price: $" + String.format("%.2f", item.getUnitPrice()));
        holder.textSubtotal.setText("Subtotal: $" + String.format("%.2f", item.getSubtotal()));
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    static class OrderItemViewHolder extends RecyclerView.ViewHolder {
        TextView textProductName, textQuantity, textUnitPrice, textSubtotal;

        public OrderItemViewHolder(@NonNull View itemView) {
            super(itemView);
            textProductName = itemView.findViewById(R.id.textProductName);
            textQuantity = itemView.findViewById(R.id.textQuantity);
            textUnitPrice = itemView.findViewById(R.id.textUnitPrice);
            textSubtotal = itemView.findViewById(R.id.textSubtotal);
        }
    }
}
