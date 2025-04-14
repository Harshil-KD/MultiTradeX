package com.example.multitradex.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.multitradex.R;
import com.example.multitradex.models.Product;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private static final String TAG = "ProductAdapter";

    private final List<Product> productList = new ArrayList<>();
    private final Context context;
    private final OnProductClickListener listener;
    private final OnFavoriteClickListener favoriteClickListener;
    private final Set<String> favoriteProductIds;

    public interface OnProductClickListener {
        void onProductClick(Product product);
    }

    public interface OnFavoriteClickListener {
        void onFavoriteClick(Product product, boolean isNowFavorite);
    }

    public ProductAdapter(Context context, List<Product> initialList,
                          OnProductClickListener listener,
                          OnFavoriteClickListener favoriteClickListener,
                          Set<String> favoriteProductIds) {
        this.context = context;
        this.listener = listener;
        this.favoriteClickListener = favoriteClickListener;
        this.favoriteProductIds = favoriteProductIds != null ? favoriteProductIds : new HashSet<>();
        if (initialList != null) {
            this.productList.addAll(initialList);
        }
    }

    public ProductAdapter(Context context, List<Product> initialList, OnProductClickListener listener) {
        this(context, initialList, listener, null, new HashSet<>());
    }

    public void setProductList(List<Product> updatedList) {
        Log.d(TAG, "Updating product list. Size: " + (updatedList != null ? updatedList.size() : 0));
        productList.clear();
        if (updatedList != null) {
            productList.addAll(updatedList);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "Creating view holder");
        View view = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);

        if (product == null) {
            Log.w(TAG, "Skipping null product at position: " + position);
            return;
        }

        Log.d(TAG, "Binding product at position " + position + ": " + product.getName());

        holder.nameText.setText(product.getName());
        holder.categoryText.setText(product.getCategory());
        holder.priceText.setText("$" + String.format("%.2f", product.getPriceRetail()));

        // ✅ Safe image list fallback
        List<String> imageUrls = product.getImageUrls() != null ? product.getImageUrls() : new ArrayList<>();
        if (!imageUrls.isEmpty()) {
            String imageUrl = imageUrls.get(0);
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.card_background)
                    .into(holder.productImage);

            if (imageUrls.size() > 1) {
                holder.imageCount.setText(imageUrls.size() + "+");
                holder.imageCount.setVisibility(View.VISIBLE);
            } else {
                holder.imageCount.setVisibility(View.GONE);
            }
        } else {
            holder.productImage.setImageResource(R.drawable.card_background);
            holder.imageCount.setVisibility(View.GONE);
        }

        // ❤️ Favorite logic
        boolean isFavorite = favoriteProductIds.contains(product.getId());
        holder.favoriteIcon.setImageResource(isFavorite ? R.drawable.ic_favorite_filled : R.drawable.ic_favorite_border);
        holder.favoriteIcon.setContentDescription(isFavorite ? "Remove from favorites" : "Add to favorites");

        holder.favoriteIcon.setOnClickListener(v -> {
            boolean newState = !favoriteProductIds.contains(product.getId());
            if (newState) {
                favoriteProductIds.add(product.getId());
            } else {
                favoriteProductIds.remove(product.getId());
            }

            int adapterPos = holder.getAdapterPosition();
            if (adapterPos != RecyclerView.NO_POSITION) {
                notifyItemChanged(adapterPos);
            }

            if (favoriteClickListener != null) {
                favoriteClickListener.onFavoriteClick(product, newState);
            } else {
                Log.w(TAG, "No favoriteClickListener provided. Icon toggled locally only.");
            }
        });

        holder.itemView.setOnClickListener(v -> {
            try {
                Log.d(TAG, "Clicked on product: " + product.getName() + " (ID: " + product.getId() + ")");
                if (listener != null) {
                    listener.onProductClick(product);
                } else {
                    Log.w(TAG, "Click listener is null for product: " + product.getName());
                }
            } catch (Exception ex) {
                Log.e(TAG, "Error handling click on product: " + product.getName(), ex);
            }
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage;
        ImageButton favoriteIcon;
        TextView nameText, categoryText, priceText, imageCount;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.productImageView);
            favoriteIcon = itemView.findViewById(R.id.favoriteIcon);
            nameText = itemView.findViewById(R.id.productNameText);
            categoryText = itemView.findViewById(R.id.productCategoryText);
            priceText = itemView.findViewById(R.id.productPriceText);
            imageCount = itemView.findViewById(R.id.imageCountIndicator);
        }
    }
}
