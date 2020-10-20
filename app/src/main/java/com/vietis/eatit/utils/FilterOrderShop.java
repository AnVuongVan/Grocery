package com.vietis.eatit.utils;

import android.widget.Filter;

import com.vietis.eatit.adapters.AdapterOrderShop;
import com.vietis.eatit.models.OrderUser;

import java.util.ArrayList;
import java.util.List;

public class FilterOrderShop extends Filter {
    private AdapterOrderShop adapter;
    private List<OrderUser> filterList;

    public FilterOrderShop(AdapterOrderShop adapter, List<OrderUser> filterList) {
        this.adapter = adapter;
        this.filterList = filterList;
    }

    @Override
    protected FilterResults performFiltering(CharSequence charSequence) {
        FilterResults results = new FilterResults();
        if (charSequence != null && charSequence.length() > 0) {
            charSequence = charSequence.toString().toUpperCase();
            List<OrderUser> filteredModels = new ArrayList<>();
            for (int i = 0; i < filterList.size(); i++) {
                if (filterList.get(i).getOrderStatus().toUpperCase().contains(charSequence)) {
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
        adapter.orderSellerList = (List<OrderUser>) filterResults.values;
        adapter.notifyDataSetChanged();
    }
}
