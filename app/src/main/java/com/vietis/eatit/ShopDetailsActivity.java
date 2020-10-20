package com.vietis.eatit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.vietis.eatit.adapters.AdapterCartItem;
import com.vietis.eatit.adapters.ProductsUser;
import com.vietis.eatit.models.CartItem;
import com.vietis.eatit.models.Products;
import com.vietis.eatit.utils.Constants;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import p32929.androideasysql_library.Column;
import p32929.androideasysql_library.EasyDB;

public class ShopDetailsActivity extends AppCompatActivity {
    private TextView shopNameTv, phoneTv, emailTv, openCloseTv, cartCountTv,
                     deliveryFeeTv, addressTv, filteredProductsTv;
    private ImageView shopIv;
    private EditText searchProductEt;
    private RatingBar ratingBar;
    private ImageButton backBtn, callBtn, mapBtn, cartBtn, filterProductBtn, reviewersBtn;

    private RecyclerView productsRv;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    private List<Products> productsList;
    private List<CartItem> cartItemList;
    private ProductsUser adapter;
    private EasyDB easyDB;

    private double myLatitude = 0.0, myLongitude = 0.0;
    private String myPhone;
    private double shopLatitude, shopLongitude;
    private String shopUid, shopName, shopEmail, shopPhone, shopAddress;

    private float ratingSum;
    public double totalPrice;
    public String deliveryFee, promoId, promoTimestamp, promoCode, promoDescription,
            promoExpDate, promoMinimumOrderPrice, promoPrice;
    public EditText promoCodeEt;
    public Button applyBtn;
    public boolean isPromoCodeApplied;
    public TextView sTotalTv, dFeeTv, totalPriceTv, promoDescriptionTv, discountTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_details);

        mapper();
        loadMyInfo();

        loadShopDetails();
        loadShopProducts();
        loadReviews();

        easyDB = EasyDB.init(this, "ITEMS_DB")
                .setTableName("ITEMS_TABLE")
                .addColumn(new Column("Item_Id", "text", "unique"))
                .addColumn(new Column("Item_PID", "text", "not null"))
                .addColumn(new Column("Item_Name", "text", "not null"))
                .addColumn(new Column("Item_Price_Each", "text", "not null"))
                .addColumn(new Column("Item_Price", "text", "not null"))
                .addColumn(new Column("Item_Quantity", "text", "not null"))
                .doneTableColumn();

        removeCartData();
        cartCount();

        searchProductEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                try {
                    adapter.getFilter().filter(charSequence);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        cartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCartDialog();
            }
        });

        callBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialPhone();
            }
        });

        mapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openMap();
            }
        });

        filterProductBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ShopDetailsActivity.this);
                builder.setTitle("Choose Category")
                        .setItems(Constants.productAllCategories, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String selected = Constants.productAllCategories[i];
                                filteredProductsTv.setText(selected);
                                if (selected.equals("All")) {
                                    loadShopProducts();
                                } else {
                                    adapter.getFilter().filter(selected);
                                }
                            }
                        }).show();
            }
        });

        reviewersBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ShopDetailsActivity.this, ShopReviewersActivity.class);
                intent.putExtra("shopUid", shopUid);
                startActivity(intent);
            }
        });

    }

    private void loadReviews() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(shopUid).child("Ratings")
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
                        ratingBar.setRating(avgRatings);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    public void cartCount() {
        int count = easyDB.getAllData().getCount();
        if (count <= 0) {
            cartCountTv.setVisibility(View.GONE);
        } else {
            cartCountTv.setVisibility(View.VISIBLE);
            cartCountTv.setText(String.valueOf(count));
        }
    }

    private void removeCartData() {
        easyDB.deleteAllDataFromTable();
    }

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    private void showCartDialog() {
        cartItemList = new ArrayList<>();
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_cart, null);

        TextView shopNameTv = view.findViewById(R.id.shopNameTv);
        sTotalTv = view.findViewById(R.id.sTotalTv);
        dFeeTv = view.findViewById(R.id.dFeeTv);
        totalPriceTv = view.findViewById(R.id.totalTv);
        promoCodeEt = view.findViewById(R.id.promoCodeEt);
        promoDescriptionTv = view.findViewById(R.id.promoDescriptionTv);
        discountTv = view.findViewById(R.id.discountTv);
        FloatingActionButton validateBtn = view.findViewById(R.id.validateBtn);
        applyBtn = view.findViewById(R.id.applyBtn);
        RecyclerView cartItemRv = view.findViewById(R.id.cartItemsRv);
        Button checkoutBtn = view.findViewById(R.id.checkoutBtn);

        if (isPromoCodeApplied) {
            promoDescriptionTv.setVisibility(View.VISIBLE);
            applyBtn.setVisibility(View.VISIBLE);
            applyBtn.setText("Applied");
            promoCodeEt.setText(promoCode);
            promoDescriptionTv.setText(promoDescription);
        } else {
            promoDescriptionTv.setVisibility(View.GONE);
            applyBtn.setVisibility(View.GONE);
            applyBtn.setText("Apply Now");
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(view);

        shopNameTv.setText(shopName);
        EasyDB easyDB = EasyDB.init(this, "ITEMS_DB")
                .setTableName("ITEMS_TABLE")
                .addColumn(new Column("Item_Id", "text", "unique"))
                .addColumn(new Column("Item_PID", "text", "not null"))
                .addColumn(new Column("Item_Name", "text", "not null"))
                .addColumn(new Column("Item_Price_Each", "text", "not null"))
                .addColumn(new Column("Item_Price", "text", "not null"))
                .addColumn(new Column("Item_Quantity", "text", "not null"))
                .doneTableColumn();

        Cursor cursor = easyDB.getAllData();
        while (cursor.moveToNext()) {
            String id = cursor.getString(1);
            String pId = cursor.getString(2);
            String name = cursor.getString(3);
            String price = cursor.getString(4);
            String cost = cursor.getString(5);
            String quantity = cursor.getString(6);

            totalPrice += Double.parseDouble(cost);
            CartItem cartItem = new CartItem(id, pId, name, price, cost, quantity);
            cartItemList.add(cartItem);
        }

        AdapterCartItem adapterCartItem = new AdapterCartItem(ShopDetailsActivity.this, cartItemList);
        cartItemRv.setAdapter(adapterCartItem);

        if (isPromoCodeApplied) {
            priceWithDiscount();
        } else {
            priceWithoutDiscount();
        }

        AlertDialog dialog = builder.create();
        dialog.show();

        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                totalPrice = 0.00;
            }
        });

        checkoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (myLatitude == 0.0 || myLongitude == 0.0) {
                    Toast.makeText(ShopDetailsActivity.this, "Please enter your address in your profile", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (myPhone == null || myPhone.equals("")) {
                    Toast.makeText(ShopDetailsActivity.this, "Please enter your phone in your profile", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (cartItemList.size() == 0) {
                    Toast.makeText(ShopDetailsActivity.this, "No product in your cart", Toast.LENGTH_SHORT).show();
                    return;
                }
                submitOrder();
            }
        });

        validateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String promotionCode = promoCodeEt.getText().toString().trim();
                if (TextUtils.isEmpty(promotionCode)) {
                    Toast.makeText(ShopDetailsActivity.this, "Please enter promotion code", Toast.LENGTH_SHORT).show();
                } else {
                    checkCodeAvailability(promotionCode);
                }
            }
        });

        applyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isPromoCodeApplied = true;
                applyBtn.setText("Applied");
                priceWithDiscount();
            }
        });

    }

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    private void priceWithDiscount() {
        discountTv.setText("$" + promoPrice);
        dFeeTv.setText("$" + deliveryFee);
        sTotalTv.setText("$" + String.format("%.2f", totalPrice));
        totalPriceTv.setText("$" + (totalPrice + Double.parseDouble(deliveryFee.replaceAll("$", "")) - Double.parseDouble(promoPrice)));
    }

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    private void priceWithoutDiscount() {
        discountTv.setText("$0");
        dFeeTv.setText("$" + deliveryFee);
        sTotalTv.setText("$" + String.format("%.2f", totalPrice));
        totalPriceTv.setText("$" + (totalPrice + Double.parseDouble(deliveryFee.replaceAll("$", ""))));
    }

    @SuppressLint("SetTextI18n")
    private void checkCodeAvailability(String promotionCode) {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setMessage("Checking promotion code");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        isPromoCodeApplied = false;
        applyBtn.setText("Apply Now");
        priceWithoutDiscount();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(shopUid).child("Promotions").orderByChild("promoCode").equalTo(promotionCode)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            progressDialog.dismiss();
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                promoId = (String) dataSnapshot.child("id").getValue();
                                promoTimestamp = (String) dataSnapshot.child("timestamp").getValue();
                                promoCode = (String) dataSnapshot.child("promoCode").getValue();
                                promoDescription = (String) dataSnapshot.child("description").getValue();
                                promoExpDate = (String) dataSnapshot.child("expireDate").getValue();
                                promoMinimumOrderPrice = (String) dataSnapshot.child("minimumOrderPrice").getValue();
                                promoPrice = (String) dataSnapshot.child("promoPrice").getValue();

                                checkCodeExpireDate();
                            }
                        } else {
                            progressDialog.dismiss();
                            Toast.makeText(ShopDetailsActivity.this, "Invalid promotion code", Toast.LENGTH_SHORT).show();
                            applyBtn.setVisibility(View.GONE);
                            promoDescriptionTv.setVisibility(View.GONE);
                            promoDescriptionTv.setText("");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void checkCodeExpireDate() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        String todayDate = day + "/" + month + "/" + year;
        try {
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
            Date currentDate = simpleDateFormat.parse(todayDate);
            Date expireDate = simpleDateFormat.parse(promoExpDate);

            assert expireDate != null;
            if (expireDate.compareTo(currentDate) >= 0) {
                checkMinimumOrderPrice();
            } else if (expireDate.compareTo(currentDate) < 0) {
                Toast.makeText(this, "The promotion code is expired on", Toast.LENGTH_SHORT).show();
                applyBtn.setVisibility(View.GONE);
                promoDescriptionTv.setVisibility(View.GONE);
                promoDescriptionTv.setText("");
            }
        } catch (Exception e) {
            e.printStackTrace();
            applyBtn.setVisibility(View.GONE);
            promoDescriptionTv.setVisibility(View.GONE);
            promoDescriptionTv.setText("");
        }
    }

    @SuppressLint("DefaultLocale")
    private void checkMinimumOrderPrice() {
        if (Double.parseDouble(String.format("%.2f", totalPrice)) < Double.parseDouble(promoMinimumOrderPrice)) {
            Toast.makeText(this, "This code is valid for order with minimum amount: $" + promoMinimumOrderPrice, Toast.LENGTH_SHORT).show();
            applyBtn.setVisibility(View.GONE);
            promoDescriptionTv.setVisibility(View.GONE);
            promoDescriptionTv.setText("");
        } else {
            applyBtn.setVisibility(View.VISIBLE);
            promoDescriptionTv.setVisibility(View.VISIBLE);
            promoDescriptionTv.setText(promoDescription);
        }
    }

    private void submitOrder() {
        progressDialog.setMessage("Placing your order...");
        progressDialog.show();

        final String timestamp = String.valueOf(System.currentTimeMillis());
        String cost = totalPriceTv.getText().toString().trim().replace("$", "");
        Map<String, Object> map = new HashMap<>();
        map.put("orderId", timestamp);
        map.put("orderTime", timestamp);
        map.put("orderStatus", "In Progress");
        map.put("orderCost", cost);
        map.put("orderBy", firebaseAuth.getUid());
        map.put("orderTo", shopUid);
        map.put("latitude", myLatitude);
        map.put("longitude", myLongitude);
        map.put("deliveryFee", deliveryFee);
        if (isPromoCodeApplied) {
            map.put("discount", promoPrice);
        } else {
            map.put("discount", "0");
        }

        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(shopUid).child("Orders");
        reference.child(timestamp).setValue(map)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        for (int i = 0; i < cartItemList.size(); i++) {
                            String pId = cartItemList.get(i).getpId();
                            String name = cartItemList.get(i).getName();
                            String cost = cartItemList.get(i).getCost();
                            String price = cartItemList.get(i).getPrice();
                            String quantity = cartItemList.get(i).getQuantity();

                            Map<String, Object> hashMap = new HashMap<>();
                            hashMap.put("pId", pId);
                            hashMap.put("name", name);
                            hashMap.put("cost", cost);
                            hashMap.put("price", price);
                            hashMap.put("quantity", quantity);
                            
                            reference.child(timestamp).child("Items").child(pId).setValue(hashMap);
                        }
                        progressDialog.dismiss();
                        Toast.makeText(ShopDetailsActivity.this, "Your order placed successfully", Toast.LENGTH_SHORT).show();
                        prepareNotificationMessage(timestamp);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(ShopDetailsActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void dialPhone() {
        startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + Uri.encode(shopPhone))));
        Toast.makeText(this, "Tel: " + shopPhone, Toast.LENGTH_SHORT).show();
    }

    private void openMap() {
        String address = "https://maps.google.com/maps?saddr=" + myLatitude + "," + myLongitude + "&daddr=" + shopLatitude + "," + shopLongitude;
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(address));
        startActivity(intent);
    }

    private void loadMyInfo() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            myPhone = (String) dataSnapshot.child("phone").getValue();
                            myLatitude = (double) dataSnapshot.child("latitude").getValue();
                            myLongitude = (double) dataSnapshot.child("longitude").getValue();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadShopDetails() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(shopUid).addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                shopName = (String) snapshot.child("shopName").getValue();
                shopEmail = (String) snapshot.child("email").getValue();
                shopPhone = (String) snapshot.child("phone").getValue();
                shopAddress = (String) snapshot.child("address").getValue();
                shopLatitude = (double) snapshot.child("latitude").getValue();
                shopLongitude = (double) snapshot.child("longitude").getValue();
                deliveryFee = (String) snapshot.child("deliveryFee").getValue();
                String profileImage = (String) snapshot.child("profileImage").getValue();
                boolean shopOpen = (boolean) snapshot.child("shopOpen").getValue();

                shopNameTv.setText(shopName);
                emailTv.setText(shopEmail);
                phoneTv.setText(shopPhone);
                addressTv.setText(shopAddress);
                deliveryFeeTv.setText("Delivery Fee: $" + deliveryFee);
                if (shopOpen) {
                    openCloseTv.setText("Open");
                } else {
                    openCloseTv.setText("Closed");
                }
                try {
                    Picasso.get().load(profileImage).placeholder(R.drawable.ic_store_gray)
                            .into(shopIv);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadShopProducts() {
        productsList = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(shopUid).child("Products")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        productsList.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Products model = dataSnapshot.getValue(Products.class);
                            productsList.add(model);
                        }
                        adapter = new ProductsUser(ShopDetailsActivity.this, productsList);
                        productsRv.setAdapter(adapter);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void prepareNotificationMessage(String orderId) {
        String NOTIFICATION_TOPIC = "/topics/" + Constants.FCM_TOPIC;
        String NOTIFICATION_TITLE = "New Order " + orderId;
        String NOTIFICATION_MESSAGE = "Congratulations...! You have a new order";
        String NOTIFICATION_TYPE = "NewOrder";

        JSONObject notificationJo = new JSONObject();
        JSONObject notificationBodyJo = new JSONObject();
        try {
            notificationBodyJo.put("notificationType", NOTIFICATION_TYPE);
            notificationBodyJo.put("buyerUid", firebaseAuth.getUid());
            notificationBodyJo.put("sellerUid", shopUid);
            notificationBodyJo.put("orderId", orderId);
            notificationBodyJo.put("notificationTitle", NOTIFICATION_TITLE);
            notificationBodyJo.put("notificationMessage", NOTIFICATION_MESSAGE);

            notificationJo.put("to", NOTIFICATION_TOPIC);
            notificationJo.put("data", notificationBodyJo);
        } catch (Exception e) {
            e.printStackTrace();
        }
        sendFcmNotification(notificationJo, orderId);
    }

    private void sendFcmNotification(JSONObject notificationJo, final String orderId) {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest("https://fcm.googleapis.com/fcm/send", notificationJo, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Intent intent = new Intent(ShopDetailsActivity.this, OrdersDetailsUsersActivity.class);
                intent.putExtra("orderTo", shopUid);
                intent.putExtra("orderId", orderId);
                startActivity(intent);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Intent intent = new Intent(ShopDetailsActivity.this, OrdersDetailsUsersActivity.class);
                intent.putExtra("orderTo", shopUid);
                intent.putExtra("orderId", orderId);
                startActivity(intent);
            }
        }){
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", "key=" + Constants.FCM_KEY);
                return headers;
            }
        };
        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }

    private void mapper() {
        shopNameTv = findViewById(R.id.shopNameTv);
        phoneTv = findViewById(R.id.phoneTv);
        emailTv = findViewById(R.id.emailTv);
        openCloseTv = findViewById(R.id.openCloseTv);
        deliveryFeeTv = findViewById(R.id.deliveryFeeTv);
        addressTv = findViewById(R.id.addressTv);
        filteredProductsTv = findViewById(R.id.filteredProductsTv);
        cartCountTv = findViewById(R.id.cartCountTv);
        shopIv = findViewById(R.id.shopIv);
        searchProductEt = findViewById(R.id.searchProductEt);
        ratingBar = findViewById(R.id.ratingBar);
        backBtn = findViewById(R.id.backBtn);
        callBtn = findViewById(R.id.callBtn);
        mapBtn = findViewById(R.id.mapBtn);
        cartBtn = findViewById(R.id.cartBtn);
        reviewersBtn = findViewById(R.id.reviewersBtn);
        filterProductBtn = findViewById(R.id.filterProductBtn);
        productsRv = findViewById(R.id.productsRv);

        firebaseAuth = FirebaseAuth.getInstance();
        shopUid = getIntent().getStringExtra("shopUid");
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);
    }

}