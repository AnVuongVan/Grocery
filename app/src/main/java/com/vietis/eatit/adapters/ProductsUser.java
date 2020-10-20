package com.vietis.eatit.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.vietis.eatit.R;
import com.vietis.eatit.ShopDetailsActivity;
import com.vietis.eatit.models.Products;
import com.vietis.eatit.utils.FilterProductUser;

import java.util.List;

import p32929.androideasysql_library.Column;
import p32929.androideasysql_library.EasyDB;

public class ProductsUser extends RecyclerView.Adapter<ProductsUser.HolderProductsUser> implements Filterable {
    private Context context;
    public List<Products> productsList;
    private List<Products> filterList;
    private FilterProductUser filter;

    private double cost, finalCost;
    private int quantity, itemId = 1;

    public ProductsUser(Context context, List<Products> productsList) {
        this.context = context;
        this.productsList = productsList;
        this.filterList = productsList;
    }

    @NonNull
    @Override
    public HolderProductsUser onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_product_user, parent, false);
        return new HolderProductsUser(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull HolderProductsUser holder, int position) {
        final Products model = productsList.get(position);
        String price = model.getOriginalPrice();
        String title = model.getProductTitle();
        String description = model.getProductDescription();
        String icon = model.getProductIcon();
        boolean discountAvailable = model.isDiscountAvailable();
        String discountNote = model.getDiscountNote();
        String discountPrice = model.getDiscountPrice();

        holder.titleTv.setText(title);
        holder.descriptionTv.setText(description);
        holder.discountedNoteTv.setText(discountNote);
        holder.discountedPriceTv.setText("$" + discountPrice);
        holder.originalPriceTv.setText("$" + price);
        if (discountAvailable) {
            holder.discountedPriceTv.setVisibility(View.VISIBLE);
            holder.discountedNoteTv.setVisibility(View.VISIBLE);
            holder.originalPriceTv.setPaintFlags(holder.originalPriceTv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            holder.discountedPriceTv.setVisibility(View.GONE);
            holder.discountedNoteTv.setVisibility(View.GONE);
            holder.originalPriceTv.setPaintFlags(0);
        }
        try {
            Picasso.get().load(icon).placeholder(R.drawable.ic_add_shopping_primary)
                    .into(holder.productIconIv);
        } catch (Exception e) {
            e.printStackTrace();
        }

        holder.addToCartTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showQuantityDialog(model);
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

    }

    @SuppressLint("SetTextI18n")
    private void showQuantityDialog(Products model) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_quantity, null);

        ImageView productIv = view.findViewById(R.id.productIv);
        final TextView titleTv = view.findViewById(R.id.titleTv);
        TextView descriptionTv = view.findViewById(R.id.descriptionTv);
        TextView pQuantityTv = view.findViewById(R.id.pQuantityTv);
        TextView discountedNoteTv = view.findViewById(R.id.discountedNoteTv);
        final TextView originalPriceTv = view.findViewById(R.id.originalPriceTv);
        TextView discountedPriceTv = view.findViewById(R.id.discountedPriceTv);
        final TextView finalPriceTv = view.findViewById(R.id.finalPriceTv);
        final TextView quantityTv = view.findViewById(R.id.quantityTv);
        ImageButton decrementBtn = view.findViewById(R.id.decrementBtn);
        ImageButton incrementBtn = view.findViewById(R.id.incrementBtn);
        Button continueBtn = view.findViewById(R.id.continueBtn);

        String title = model.getProductTitle();
        String description = model.getProductDescription();
        String icon = model.getProductIcon();
        final String productId = model.getProductId();
        String productQuantity = model.getProductQuantity();
        boolean discountAvailable = model.isDiscountAvailable();
        String discountNote = model.getDiscountNote();
        String discountPrice = model.getDiscountPrice();
        final String price;

        if (discountAvailable) {
            price = model.getDiscountPrice();
            discountedNoteTv.setVisibility(View.VISIBLE);
            originalPriceTv.setPaintFlags(originalPriceTv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            discountedPriceTv.setVisibility(View.GONE);
            discountedNoteTv.setVisibility(View.GONE);
            price = model.getOriginalPrice();
        }
        cost = Double.parseDouble(price.replaceAll("$", ""));
        finalCost = Double.parseDouble(price.replaceAll("$", ""));
        quantity = 1;

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(view);

        titleTv.setText(title);
        descriptionTv.setText(description);
        pQuantityTv.setText(productQuantity);
        quantityTv.setText(String.valueOf(quantity));
        discountedNoteTv.setText(discountNote);
        discountedPriceTv.setText("$" + discountPrice);
        originalPriceTv.setText("$" + model.getOriginalPrice());
        finalPriceTv.setText("$" + finalCost);
        try {
            Picasso.get().load(icon).placeholder(R.drawable.ic_cart_gray)
                    .into(productIv);
        } catch (Exception e) {
            e.printStackTrace();
        }

        final AlertDialog dialog = builder.create();
        dialog.show();

        incrementBtn.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View view) {
                finalCost += cost;
                quantity++;

                finalPriceTv.setText("$" + finalCost);
                quantityTv.setText(String.valueOf(quantity));
            }
        });

        decrementBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (quantity > 1) {
                    finalCost -= cost;
                    quantity--;

                    finalPriceTv.setText("$" + finalCost);
                    quantityTv.setText(String.valueOf(quantity));
                }
            }
        });

        continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String titleEvent = titleTv.getText().toString().trim();
                String priceFinal = finalPriceTv.getText().toString().trim().replace("$", "");
                String quantityEvent = quantityTv.getText().toString().trim();

                addToCart(productId, titleEvent, price, priceFinal, quantityEvent);
                dialog.dismiss();
            }
        });

    }

    private void addToCart(String productId, String title, String priceOriginal, String priceFinal, String quantity) {
        itemId++;
        EasyDB easyDB = EasyDB.init(context, "ITEMS_DB")
                .setTableName("ITEMS_TABLE")
                .addColumn(new Column("Item_Id", "text", "unique"))
                .addColumn(new Column("Item_PID", "text", "not null"))
                .addColumn(new Column("Item_Name", "text", "not null"))
                .addColumn(new Column("Item_Price_Each", "text", "not null"))
                .addColumn(new Column("Item_Price", "text", "not null"))
                .addColumn(new Column("Item_Quantity", "text", "not null"))
                .doneTableColumn();

        easyDB.addData("Item_Id", itemId)
                .addData("Item_PID", productId)
                .addData("Item_Name", title)
                .addData("Item_Price_Each", priceOriginal)
                .addData("Item_Price", priceFinal)
                .addData("Item_Quantity", quantity)
                .doneDataAdding();

        Toast.makeText(context, "Added to cart", Toast.LENGTH_SHORT).show();
        ((ShopDetailsActivity) context).cartCount();
    }

    @Override
    public int getItemCount() {
        return productsList.size();
    }

    @Override
    public Filter getFilter() {
        if (filter == null) {
            filter = new FilterProductUser(this, filterList);
        }
        return filter;
    }

    static class HolderProductsUser extends RecyclerView.ViewHolder {
        private ImageView productIconIv;
        private TextView discountedNoteTv, titleTv, descriptionTv,
                addToCartTv, discountedPriceTv, originalPriceTv;

        public HolderProductsUser(@NonNull View itemView) {
            super(itemView);
            productIconIv = itemView.findViewById(R.id.productIconIv);
            discountedNoteTv = itemView.findViewById(R.id.discountedNoteTv);
            titleTv = itemView.findViewById(R.id.titleTv);
            descriptionTv = itemView.findViewById(R.id.descriptionTv);
            addToCartTv = itemView.findViewById(R.id.addToCartTv);
            discountedPriceTv = itemView.findViewById(R.id.discountedPriceTv);
            originalPriceTv = itemView.findViewById(R.id.originalPriceTv);
        }
    }

}
