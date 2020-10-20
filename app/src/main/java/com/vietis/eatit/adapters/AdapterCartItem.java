package com.vietis.eatit.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.vietis.eatit.R;
import com.vietis.eatit.ShopDetailsActivity;
import com.vietis.eatit.models.CartItem;

import java.util.List;

import p32929.androideasysql_library.Column;
import p32929.androideasysql_library.EasyDB;

public class AdapterCartItem extends RecyclerView.Adapter<AdapterCartItem.MyHolder> {
    private Context context;
    private List<CartItem> cartItemList;

    public AdapterCartItem(Context context, List<CartItem> cartItemList) {
        this.context = context;
        this.cartItemList = cartItemList;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_cart_item, parent, false);
        return new MyHolder(view);
    }

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, final int position) {
        CartItem cartItem = cartItemList.get(position);
        String title = cartItem.getName();
        String priceEach = cartItem.getPrice();
        final String priceTotal = cartItem.getCost();
        String quantity = cartItem.getQuantity();
        final String id = cartItem.getId();

        holder.itemTitleTv.setText(title);
        holder.itemPriceEachTv.setText("$" + String.format("%.2f", Double.parseDouble(priceEach)));
        holder.itemPriceTv.setText("$" + String.format("%.2f", Double.parseDouble(priceTotal)));
        holder.itemQuantityTv.setText("[" + quantity + "]");

        holder.itemRemoveTv.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("DefaultLocale")
            @Override
            public void onClick(View view) {
                EasyDB easyDB = EasyDB.init(context, "ITEMS_DB")
                        .setTableName("ITEMS_TABLE")
                        .addColumn(new Column("Item_Id", "text", "unique"))
                        .addColumn(new Column("Item_PID", "text", "not null"))
                        .addColumn(new Column("Item_Name", "text", "not null"))
                        .addColumn(new Column("Item_Price_Each", "text", "not null"))
                        .addColumn(new Column("Item_Price", "text", "not null"))
                        .addColumn(new Column("Item_Quantity", "text", "not null"))
                        .doneTableColumn();
                
                easyDB.deleteRow(1, id);
                Toast.makeText(context, "Remove from your cart", Toast.LENGTH_SHORT).show();

                //refresh list
                cartItemList.remove(position);
                notifyItemChanged(position);
                notifyDataSetChanged();

                double subTotalWithoutDiscount = ((ShopDetailsActivity) context).totalPrice;
                double subTotalAfterProductRemove = subTotalWithoutDiscount - Double.parseDouble(priceTotal.replaceAll("$", ""));
                ((ShopDetailsActivity) context).totalPrice = subTotalAfterProductRemove;
                ((ShopDetailsActivity) context).sTotalTv.setText("$" + String.format("%.2f", ((ShopDetailsActivity) context).totalPrice));

                double promoPrice = Double.parseDouble(((ShopDetailsActivity) context).promoPrice);
                double deliveryFee = Double.parseDouble(((ShopDetailsActivity) context).deliveryFee.replaceAll("$", ""));
                String costPrice = "$" + String.format("%.2f", Double.parseDouble(String.format("%.2f", subTotalAfterProductRemove + deliveryFee)));

                if (((ShopDetailsActivity) context).isPromoCodeApplied) {
                    if (subTotalAfterProductRemove < Double.parseDouble(((ShopDetailsActivity) context).promoMinimumOrderPrice)) {
                        Toast.makeText(context, "This code is valid for order with minimum amount: $" + ((ShopDetailsActivity) context).promoMinimumOrderPrice, Toast.LENGTH_SHORT).show();
                        ((ShopDetailsActivity) context).applyBtn.setVisibility(View.GONE);
                        ((ShopDetailsActivity) context).promoDescriptionTv.setVisibility(View.GONE);
                        ((ShopDetailsActivity) context).promoDescriptionTv.setText("");
                        ((ShopDetailsActivity) context).discountTv.setText("0$");
                        ((ShopDetailsActivity) context).isPromoCodeApplied = false;
                        ((ShopDetailsActivity) context).totalPriceTv.setText(costPrice);
                    } else {
                        ((ShopDetailsActivity) context).applyBtn.setVisibility(View.VISIBLE);
                        ((ShopDetailsActivity) context).promoDescriptionTv.setVisibility(View.VISIBLE);
                        ((ShopDetailsActivity) context).promoDescriptionTv.setText(((ShopDetailsActivity) context).promoDescription);
                        ((ShopDetailsActivity) context).isPromoCodeApplied = true;
                        ((ShopDetailsActivity) context).totalPriceTv.setText("$" + String.format("%.2f", Double.parseDouble(String.format("%.2f", subTotalAfterProductRemove + deliveryFee - promoPrice))));
                    }
                } else {
                    ((ShopDetailsActivity) context).totalPriceTv.setText(costPrice);
                }

                ((ShopDetailsActivity) context).cartCount();
            }
        });

    }

    @Override
    public int getItemCount() {
        return cartItemList.size();
    }

    static class MyHolder extends RecyclerView.ViewHolder {
        private TextView itemTitleTv, itemPriceTv, itemPriceEachTv,
                itemQuantityTv, itemRemoveTv;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            itemTitleTv = itemView.findViewById(R.id.itemTitleTv);
            itemPriceTv = itemView.findViewById(R.id.itemPriceTv);
            itemPriceEachTv = itemView.findViewById(R.id.itemPriceEachTv);
            itemQuantityTv = itemView.findViewById(R.id.itemQuantityTv);
            itemRemoveTv = itemView.findViewById(R.id.itemRemoveTv);
        }
    }

}
