package com.example.multitradex.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.multitradex.R;
import com.example.multitradex.models.CartItem;

import java.util.ArrayList;
import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private final Context context;
    private final List<CartItem> cartItems = new ArrayList<>();
    private final CartInteractionListener listener;


    public interface CartInteractionListener {
        void onQuantityChanged(CartItem item, int newQty);
        void onRemoveItem(CartItem item);
    }

    public CartAdapter(Context context, CartInteractionListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setCartItemList(List<CartItem> updatedList) {
        cartItems.clear();
        if (updatedList != null) {
            cartItems.addAll(updatedList);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = cartItems.get(position);
        holder.productName.setText(item.getProductName());
        holder.unitPrice.setText("Unit Price: $" + String.format("%.2f", item.getUnitPrice()));
        holder.subtotal.setText("Subtotal: $" + String.format("%.2f", item.getUnitPrice() * item.getQuantity()));

        holder.quantityText.setText(String.valueOf(item.getQuantity()));

        holder.btnIncrease.setOnClickListener(v -> {
            int newQty = item.getQuantity() + 1;
            if (newQty <= 100) {
                item.setQuantity(newQty);
                holder.quantityText.setText(String.valueOf(newQty));
                holder.subtotal.setText("Subtotal: $" + String.format("%.2f", item.getUnitPrice() * newQty));
                listener.onQuantityChanged(item, newQty);
            }
        });

        holder.btnDecrease.setOnClickListener(v -> {
            int newQty = item.getQuantity() - 1;
            if (newQty >= 1) {
                item.setQuantity(newQty);
                holder.quantityText.setText(String.valueOf(newQty));
                holder.subtotal.setText("Subtotal: $" + String.format("%.2f", item.getUnitPrice() * newQty));
                listener.onQuantityChanged(item, newQty);
            }
        });



        Glide.with(context)
                .load(item.getImageUrl())
                .placeholder(R.drawable.card_background)
                .into(holder.productImage);

        holder.removeButton.setOnClickListener(v -> listener.onRemoveItem(item));
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {

        ImageView productImage;
        TextView productName, unitPrice, subtotal;
        ImageButton btnDecrease, btnIncrease;
        TextView quantityText;

        ImageButton removeButton;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.cartProductImage);
            productName = itemView.findViewById(R.id.cartProductName);
            unitPrice = itemView.findViewById(R.id.cartUnitPrice);
            subtotal = itemView.findViewById(R.id.cartSubtotal);
            btnDecrease = itemView.findViewById(R.id.btnDecrease);
            btnIncrease = itemView.findViewById(R.id.btnIncrease);
            quantityText = itemView.findViewById(R.id.quantityText);

            removeButton = itemView.findViewById(R.id.removeCartItemButton);
        }
    }
}
