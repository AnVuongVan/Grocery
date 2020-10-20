package com.vietis.eatit.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.vietis.eatit.R;
import com.vietis.eatit.ShopDetailsActivity;
import com.vietis.eatit.models.Shop;

import java.util.List;
import java.util.Objects;

public class AdapterShop extends RecyclerView.Adapter<AdapterShop.HolderShop> {
    private Context context;
    private List<Shop> shopList;
    private float ratingSum;

    public AdapterShop(Context context, List<Shop> shopList) {
        this.context = context;
        this.shopList = shopList;
    }

    @NonNull
    @Override
    public HolderShop onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_shop, parent, false);
        return new HolderShop(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderShop holder, int position) {
        Shop shop = shopList.get(position);
        String address = shop.getAddress();
        String shopName = shop.getShopName();
        String phone = shop.getPhone();
        final String uid = shop.getUid();
        boolean online = shop.isOnline();
        String icon = shop.getProfileImage();
        boolean shopOpen = shop.isShopOpen();
        loadReviews(uid, holder);

        holder.shopNameTv.setText(shopName);
        holder.phoneTv.setText(phone);
        holder.addressTv.setText(address);
        if (online) {
            holder.onlineIv.setVisibility(View.VISIBLE);
        } else {
            holder.onlineIv.setVisibility(View.GONE);
        }

        if (shopOpen) {
            holder.shopClosedTv.setVisibility(View.GONE);
        } else {
            holder.shopClosedTv.setVisibility(View.VISIBLE);
        }

        try {
            Picasso.get().load(icon).placeholder(R.drawable.ic_store_gray)
                    .into(holder.shopIv);
        } catch (Exception e) {
            e.printStackTrace();
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ShopDetailsActivity.class);
                intent.putExtra("shopUid", uid);
                context.startActivity(intent);
            }
        });

    }

    private void loadReviews(String uid, final HolderShop holder) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(uid).child("Ratings")
                .addValueEventListener(new ValueEventListener() {
                    @SuppressLint({"DefaultLocale", "SetTextI18n"})
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        ratingSum = 0;
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            float rating = Float.parseFloat((String) Objects.requireNonNull(dataSnapshot.child("ratings").getValue()));
                            ratingSum += rating;
                        }

                        long numberOfReviews = snapshot.getChildrenCount();
                        float avgRatings = ratingSum/numberOfReviews;
                        holder.ratingBar.setRating(avgRatings);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return shopList.size();
    }

    static class HolderShop extends RecyclerView.ViewHolder {
        private ImageView shopIv, onlineIv;
        private TextView shopClosedTv, shopNameTv, phoneTv, addressTv;
        private RatingBar ratingBar;

        public HolderShop(@NonNull View itemView) {
            super(itemView);
            shopIv = itemView.findViewById(R.id.shopIv);
            onlineIv = itemView.findViewById(R.id.onlineIv);
            ImageView nextIv = itemView.findViewById(R.id.nextIv);
            shopClosedTv = itemView.findViewById(R.id.shopClosedTv);
            shopNameTv = itemView.findViewById(R.id.shopNameTv);
            phoneTv = itemView.findViewById(R.id.phoneTv);
            addressTv = itemView.findViewById(R.id.addressTv);
            ratingBar = itemView.findViewById(R.id.ratingBar);
        }

    }

}
