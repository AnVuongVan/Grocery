package com.vietis.eatit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.vietis.eatit.adapters.AdapterOrdersItem;
import com.vietis.eatit.models.CartItem;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class OrdersDetailsUsersActivity extends AppCompatActivity {
    private ImageButton backBtn, writeReviewBtn;
    private TextView orderIdTv, dateTv, orderStatusTv, shopNameTv,
                     totalItemsTv, amountTv, addressTv;

    private RecyclerView itemsRv;

    private List<CartItem> cartItemList;
    private AdapterOrdersItem adapterOrdersItem;
    private String orderTo, orderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders_details_users);

        mapper();

        Intent intent = getIntent();
        orderTo = intent.getStringExtra("orderTo");
        orderId = intent.getStringExtra("orderId");

        loadShopInfo();
        loadOrderDetails();
        loadOrderedItems();

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        writeReviewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(OrdersDetailsUsersActivity.this, WriteReviewActivity.class);
                intent.putExtra("shopUid", orderTo);
                startActivity(intent);
            }
        });
    }

    private void loadOrderedItems() {
        cartItemList = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(orderTo).child("Orders").child(orderId).child("Items")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        cartItemList.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            CartItem cartItem = dataSnapshot.getValue(CartItem.class);
                            cartItemList.add(cartItem);
                        }
                        adapterOrdersItem = new AdapterOrdersItem(OrdersDetailsUsersActivity.this, cartItemList);
                        itemsRv.setAdapter(adapterOrdersItem);
                        totalItemsTv.setText(String.valueOf(snapshot.getChildrenCount()));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadOrderDetails() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(orderTo).child("Orders").child(orderId)
                .addValueEventListener(new ValueEventListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String orderId = (String) snapshot.child("orderId").getValue();
                        String orderTime = (String) snapshot.child("orderTime").getValue();
                        String orderStatus = (String) snapshot.child("orderStatus").getValue();
                        String orderCost = (String) snapshot.child("orderCost").getValue();
                        String deliveryFee = (String) snapshot.child("deliveryFee").getValue();
                        double latitude = (double) snapshot.child("latitude").getValue();
                        double longitude = (double) snapshot.child("longitude").getValue();
                        String discount = (String) snapshot.child("discount").getValue();

                        assert discount != null;
                        if (discount.equals("0")) {
                            discount = " & discount $0";
                        } else {
                            discount = " & discount $" + discount;
                        }

                        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
                        assert orderTime != null;
                        calendar.setTimeInMillis(Long.parseLong(orderTime));
                        String dateTime = DateFormat.format("dd/MM/yyyy hh:mm a", calendar).toString();

                        assert orderStatus != null;
                        switch (orderStatus) {
                            case "In Progress":
                                orderStatusTv.setTextColor(getResources().getColor(R.color.colorPrimary));
                                break;
                            case "Completed":
                                orderStatusTv.setTextColor(getResources().getColor(R.color.colorGreen));
                                break;
                            case "Cancelled":
                                orderStatusTv.setTextColor(getResources().getColor(R.color.colorRed));
                                break;
                        }
                        orderIdTv.setText(orderId);
                        orderStatusTv.setText(orderStatus);
                        amountTv.setText("$" + orderCost + " [Including delivery fee $" + deliveryFee + discount +"]");
                        dateTv.setText(dateTime);
                        findAddress(latitude, longitude);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void findAddress(double latitude, double longitude) {
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());
        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1);
            String address = addresses.get(0).getAddressLine(0);
            addressTv.setText(address);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadShopInfo() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(orderTo)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String shopName = (String) snapshot.child("shopName").getValue();
                        shopNameTv.setText(shopName);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void mapper() {
        backBtn = findViewById(R.id.backBtn);
        orderIdTv = findViewById(R.id.orderIdTv);
        dateTv = findViewById(R.id.dateTv);
        orderStatusTv = findViewById(R.id.orderStatusTv);
        shopNameTv = findViewById(R.id.shopNameTv);
        totalItemsTv = findViewById(R.id.totalItemsTv);
        amountTv = findViewById(R.id.amountTv);
        addressTv = findViewById(R.id.addressTv);
        writeReviewBtn = findViewById(R.id.writeReviewBtn);
        itemsRv = findViewById(R.id.itemsRv);
    }

}