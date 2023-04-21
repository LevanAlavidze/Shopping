package com.example.shopping;

import android.widget.Filter;

import com.example.shopping.models.ModelOrderShop;
import com.example.shopping.models.ModelProduct;

import java.util.ArrayList;

public class FilterOrderShop extends Filter {
    AdapterOrderShop adapter;
    ArrayList<ModelOrderShop> filterList;

    public FilterOrderShop(AdapterOrderShop adapter, ArrayList<ModelOrderShop> filterList) {
        this.adapter = adapter;
        this.filterList = filterList;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults results = new FilterResults();

        //change to uppercase to get in any-case insensitive
        if (constraint != null && constraint.length()>0){
            constraint = constraint.toString().toUpperCase();
            ArrayList<ModelOrderShop> filteredModels = new ArrayList<>();
            for (int i=0; i<filterList.size(); i++){
                if (filterList.get(i).getOrderStatus().toUpperCase().contains(constraint)){
                    filteredModels.add(filterList.get(i));
                }
            }
            results.count = filteredModels.size();
            results.values = filteredModels;
        }
        else {
            results.count = filterList.size();
            results.values = filterList;
        }
        return results;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
            adapter.orderShopArrayList = (ArrayList<ModelOrderShop>)results.values;
            adapter.notifyDataSetChanged();
    }
}
