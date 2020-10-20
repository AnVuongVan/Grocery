package com.vietis.eatit.utils;

import android.widget.Filter;

import com.vietis.eatit.adapters.ProductsSeller;
import com.vietis.eatit.models.Products;

import java.util.ArrayList;
import java.util.List;

public class FilterProductSeller extends Filter {
    private ProductsSeller adapter;
    private List<Products> filterList;

    public FilterProductSeller(ProductsSeller adapter, List<Products> filterList) {
        this.adapter = adapter;
        this.filterList = filterList;
    }

    @Override
    protected FilterResults performFiltering(CharSequence charSequence) {
        FilterResults results = new FilterResults();
        if (charSequence != null && charSequence.length() > 0) {
            charSequence = charSequence.toString().toUpperCase();
            List<Products> filteredModels = new ArrayList<>();
            for (int i = 0; i < filterList.size(); i++) {
                if (filterList.get(i).getProductTitle().toUpperCase().contains(charSequence) ||
                        filterList.get(i).getProductCategory().toUpperCase().contains(charSequence)) {
                    filteredModels.add(filterList.get(i));
                }
            }
            results.count = filteredModels.size();
            results.values = filteredModels;
        } else {
            results.count = filterList.size();
            results.values = filterList;
        }
        return results;
    }

    @Override
    protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
        adapter.productsList = (List<Products>) filterResults.values;
        adapter.notifyDataSetChanged();
    }
}
