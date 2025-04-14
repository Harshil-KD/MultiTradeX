package com.example.multitradex.ui.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.multitradex.R;
import com.example.multitradex.adapters.ProductAdapter;
import com.example.multitradex.enums.ProductDetailMode;
import com.example.multitradex.models.FavoriteProduct;
import com.example.multitradex.models.Product;
import com.example.multitradex.ui.dialogs.FilterBottomSheetFragment;
import com.example.multitradex.utils.AppConstants;
import com.example.multitradex.viewmodel.FavoriteProductViewModel;
import com.example.multitradex.viewmodel.ProductViewModel;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.*;

public class BrowseFragment extends Fragment implements FilterBottomSheetFragment.OnFilterAppliedListener {

    private ProductViewModel productViewModel;
    private FavoriteProductViewModel favoriteViewModel;

    private Set<String> favoriteProductIds = new HashSet<>();
    private String currentUserId;

    private ProductAdapter adapter;
    private RecyclerView recyclerView;
    private ShimmerFrameLayout shimmerLayout;
    private ProgressBar paginationProgressBar;
    private FloatingActionButton fabScrollToTop;
    private ImageButton toggleLayoutButton, filterButton;
    private TextView emptyStateText;
    private Spinner sortSpinner;

    private boolean isGridLayout = true;
    private boolean showFavoritesOnly = false; // ✅ new flag
    private final Handler handler = new Handler();

    private List<Product> allProducts = new ArrayList<>();
    private List<Product> currentDisplayList = new ArrayList<>();
    private int currentIndex = 0;
    private int pageSize = 10;

    private String searchFilter = "", minPriceFilter = "", maxPriceFilter = "", categoryFilter = "All", availabilityFilter = "All";
    private String sortOption = "Name (A-Z)";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_browse, container, false);
        initViews(view);
        setupRecyclerView();

        productViewModel = new ViewModelProvider(this).get(ProductViewModel.class);
        favoriteViewModel = new ViewModelProvider(requireActivity()).get(FavoriteProductViewModel.class);

        SharedPreferences prefs = requireContext().getSharedPreferences(AppConstants.PREF_NAME, Context.MODE_PRIVATE);
        currentUserId = prefs.getString(AppConstants.KEY_USER_ID, "");

        favoriteViewModel.getFavoriteProductIds(currentUserId).observe(getViewLifecycleOwner(), ids -> {
            favoriteProductIds.clear();
            if (ids != null) favoriteProductIds.addAll(ids);
            adapter.notifyDataSetChanged();
        });

        productViewModel.startRealtimeSync();
        productViewModel.syncIfRoomIsEmpty();
        observeProducts();

        setupListeners();
        setupSortSpinner();
        return view;
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.productRecyclerView);
        shimmerLayout = view.findViewById(R.id.shimmerLayout);
        paginationProgressBar = view.findViewById(R.id.paginationProgressBar);
        fabScrollToTop = view.findViewById(R.id.fabScrollToTop);
        toggleLayoutButton = view.findViewById(R.id.toggleLayoutButton);
        filterButton = view.findViewById(R.id.filterButton);
        emptyStateText = view.findViewById(R.id.emptyStateText);
        sortSpinner = view.findViewById(R.id.sortSpinner);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        adapter = new ProductAdapter(
                getContext(),
                new ArrayList<>(),
                product -> {
                    requireActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragmentContainer, new ProductDetailFragment(product, ProductDetailMode.BUYER_VIEW))
                            .addToBackStack(null)
                            .commit();
                },
                (product, isNowFavorite) -> {
                    FavoriteProduct favorite = new FavoriteProduct(
                            currentUserId + "_" + product.getId(),
                            currentUserId,
                            product.getId()
                    );
                    if (isNowFavorite) favoriteViewModel.addFavorite(favorite);
                    else favoriteViewModel.removeFavorite(favorite);
                },
                favoriteProductIds
        );
        recyclerView.setAdapter(adapter);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView rv, int dx, int dy) {
                if (!rv.canScrollVertically(1)) loadMoreProducts();
                fabScrollToTop.setVisibility(rv.computeVerticalScrollOffset() > 300 ? View.VISIBLE : View.GONE);
            }
        });
    }

    private void setupListeners() {
        fabScrollToTop.setOnClickListener(v -> recyclerView.smoothScrollToPosition(0));

        toggleLayoutButton.setOnClickListener(v -> {
            isGridLayout = !isGridLayout;
            toggleLayoutButton.setImageResource(isGridLayout ? R.drawable.ic_grid : R.drawable.ic_list);
            recyclerView.setLayoutManager(isGridLayout ? new GridLayoutManager(getContext(), 2) : new GridLayoutManager(getContext(), 1));
        });

        filterButton.setOnClickListener(v -> {
            Set<String> categoryOptions = new HashSet<>();
            for (Product p : allProducts) categoryOptions.add(p.getCategory());
            FilterBottomSheetFragment sheet = new FilterBottomSheetFragment(categoryOptions);
            sheet.setListener(BrowseFragment.this);
            sheet.show(getParentFragmentManager(), "FilterBottomSheet");
        });

        // Long press filter button to toggle favorites only
        filterButton.setOnLongClickListener(v -> {
            showFavoritesOnly = !showFavoritesOnly;
            Toast.makeText(getContext(), showFavoritesOnly ? "Showing only favorites" : "Showing all products", Toast.LENGTH_SHORT).show();
            adapter.setProductList(applyActiveFilters(currentDisplayList));
            return true;
        });
    }

    private void setupSortSpinner() {
        List<String> sortOptions = Arrays.asList("Name (A-Z)", "Price (Low to High)", "Price (High to Low)", "Newest First");
        ArrayAdapter<String> sortAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, sortOptions);
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sortSpinner.setAdapter(sortAdapter);

        sortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sortOption = sortOptions.get(position);
                adapter.setProductList(applyActiveFilters(currentDisplayList));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void observeProducts() {
        shimmerLayout.setVisibility(View.VISIBLE);
        shimmerLayout.startShimmer();
        productViewModel.getAllProducts().observe(getViewLifecycleOwner(), products -> {
            shimmerLayout.stopShimmer();
            shimmerLayout.setVisibility(View.GONE);
            allProducts = products != null ? products : new ArrayList<>();
            currentIndex = 0;
            currentDisplayList.clear();
            loadMoreProducts();
        });
    }

    private void loadMoreProducts() {
        if (currentIndex >= allProducts.size()) return;
        paginationProgressBar.setVisibility(View.VISIBLE);

        recyclerView.postDelayed(() -> {
            int endIndex = Math.min(currentIndex + pageSize, allProducts.size());
            List<Product> nextChunk = allProducts.subList(currentIndex, endIndex);
            currentDisplayList.addAll(nextChunk);
            adapter.setProductList(applyActiveFilters(currentDisplayList));
            currentIndex = endIndex;
            paginationProgressBar.setVisibility(View.GONE);
        }, 500);
    }

    private List<Product> applyActiveFilters(List<Product> baseList) {
        double min = TextUtils.isEmpty(minPriceFilter) ? 0 : Double.parseDouble(minPriceFilter);
        double max = TextUtils.isEmpty(maxPriceFilter) ? Double.MAX_VALUE : Double.parseDouble(maxPriceFilter);

        List<Product> filtered = new ArrayList<>();
        for (Product p : baseList) {
            boolean matches = true;
            if (!TextUtils.isEmpty(searchFilter) && !p.getName().toLowerCase().contains(searchFilter.toLowerCase())) matches = false;
            if (p.getPriceRetail() < min || p.getPriceRetail() > max) matches = false;
            if (!categoryFilter.equals("All") && !p.getCategory().equalsIgnoreCase(categoryFilter)) matches = false;
            if (!availabilityFilter.equals("All") && !p.getAvailableFor().equalsIgnoreCase(availabilityFilter)) matches = false;
            if (showFavoritesOnly && !favoriteProductIds.contains(p.getId())) matches = false;
            if (matches) filtered.add(p);
        }

        switch (sortOption) {
            case "Price (Low to High)":
                filtered.sort(Comparator.comparingDouble(Product::getPriceRetail));
                break;
            case "Price (High to Low)":
                filtered.sort((a, b) -> Double.compare(b.getPriceRetail(), a.getPriceRetail()));
                break;
            case "Newest First":
                filtered.sort((a, b) -> b.getId().compareTo(a.getId()));
                break;
            default:
                filtered.sort(Comparator.comparing(Product::getName, String.CASE_INSENSITIVE_ORDER));
        }

        emptyStateText.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
        return filtered;
    }

    @Override
    public void onFilterApplied(String search, String minPrice, String maxPrice, String category, String availability) {
        this.searchFilter = search;
        this.minPriceFilter = minPrice;
        this.maxPriceFilter = maxPrice;
        this.categoryFilter = category;
        this.availabilityFilter = availability;
        adapter.setProductList(applyActiveFilters(currentDisplayList));
    }
}
