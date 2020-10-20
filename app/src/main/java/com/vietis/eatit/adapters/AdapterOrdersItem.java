package com.vietis.eatit.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.vietis.eatit.R;
import com.vietis.eatit.models.CartItem;

import java.util.List;

public class AdapterOrdersItem extends RecyclerView.Adapter<AdapterOrdersItem.MyHolder> {
    private Context context;
    private List<CartItem> cartItemList;

    public AdapterOrdersItem(Context context, List<CartItem> cartItemList) {
        this.context = context;
        this.cartItemList = cartItemList;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_orders_item, parent, false);
        return new MyHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        CartItem cartItem = cartItemList.get(position);
        String title = cartItem.getName();
        String cost = cartItem.getCost();
        String price = cartItem.getPrice();
        String quantity = cartItem.getQuantity();

        holder.itemTitleTv.setText(title);
        holder.itemPriceEachTv.setText("$" + price);
        holder.itemPriceTv.setText("$" + cost);
        holder.itemQuantityTv.setText("[" + quantity + "]");
    }

    @Override
    public int getItemCount() {
        return cartItemList.size();
    }

    static class MyHolder extends RecyclerView.ViewHolder {
        private TextView itemTitleTv, itemPriceEachTv, itemPriceTv, itemQuantityTv;
        public MyHolder(@NonNull View itemView) {
            super(itemView);
            itemTitleTv = itemView.findViewById(R.id.itemTitleTv);
            itemPriceEachTv = itemView.findViewById(R.id.itemPriceEachTv);
            itemPriceTv = itemView.findViewById(R.id.itemPriceTv);
            itemQuantityTv = itemView.findViewById(R.id.itemQuantityTv);
        }
    }
}
