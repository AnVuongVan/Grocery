package com.vietis.eatit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.vietis.eatit.adapters.PromotionShop;
import com.vietis.eatit.models.Promotion;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class PromotionCodesActivity extends AppCompatActivity {
    private TextView filteredTv;
    private RecyclerView promoRv;

    private FirebaseAuth firebaseAuth;
    private List<Promotion> promotionList;
    private PromotionShop adapterPromotionShop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_promotion_codes);

        ImageButton backBtn = findViewById(R.id.backBtn);
        ImageButton addPromotionBtn = findViewById(R.id.addPromotionBtn);
        ImageButton filterBtn = findViewById(R.id.filterBtn);
        filteredTv = findViewById(R.id.filteredTv);
        promoRv = findViewById(R.id.promoRv);

        firebaseAuth = FirebaseAuth.getInstance();
        loadAllPromotionCodes();

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        addPromotionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(PromotionCodesActivity.this, AddPromotionCodesActivity.class));
            }
        });

        filterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                filterDialog();
            }
        });

    }

    private void filterDialog() {
        String[] options = {"All", "Expired", "Not Expired"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Promotion Codes")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (i == 0) {
                            filteredTv.setText("All Promotion Codes");
                            loadAllPromotionCodes();
                        } else if (i == 1) {
                            filteredTv.setText("Expired Promotion Codes");
                            loadExpiredPromotionCodes();
                        } else if (i == 2) {
                            filteredTv.setText("Not Expired Promotion Codes");
                            loadNotExpiredPromotionCodes();
                        }
                    }
                })
                .show();
    }

    private void loadAllPromotionCodes() {
        promotionList = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(Objects.requireNonNull(firebaseAuth.getUid())).child("Promotions")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        promotionList.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Promotion model = dataSnapshot.getValue(Promotion.class);
                            promotionList.add(model);
                        }
                        adapterPromotionShop = new PromotionShop(PromotionCodesActivity.this, promotionList);
                        promoRv.setAdapter(adapterPromotionShop);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadExpiredPromotionCodes() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        final String todayDate = day + "/" + month + "/" + year;
        promotionList = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(Objects.requireNonNull(firebaseAuth.getUid())).child("Promotions")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        promotionList.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Promotion model = dataSnapshot.getValue(Promotion.class);
                            assert model != null;
                            String expDate = model.getExpireDate();
                            try {
                                @SuppressLint("SimpleDateFormat")
                                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
                                Date currentDate = simpleDateFormat.parse(todayDate);
                                Date expireDate = simpleDateFormat.parse(expDate);

                                assert expireDate != null;
                                if (expireDate.compareTo(currentDate) < 0) {
                                    promotionList.add(model);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        adapterPromotionShop = new PromotionShop(PromotionCodesActivity.this, promotionList);
                        promoRv.setAdapter(adapterPromotionShop);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadNotExpiredPromotionCodes() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        final String todayDate = day + "/" + month + "/" + year;
        promotionList = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(Objects.requireNonNull(firebaseAuth.getUid())).child("Promotions")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        promotionList.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Promotion model = dataSnapshot.getValue(Promotion.class);
                            assert model != null;
                            String expDate = model.getExpireDate();
                            try {
                                @SuppressLint("SimpleDateFormat")
                                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
                                Date currentDate = simpleDateFormat.parse(todayDate);
                                Date expireDate = simpleDateFormat.parse(expDate);

                                assert expireDate != null;
                                if (expireDate.compareTo(currentDate) >= 0) {
                                    promotionList.add(model);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        adapterPromotionShop = new PromotionShop(PromotionCodesActivity.this, promotionList);
                        promoRv.setAdapter(adapterPromotionShop);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

}