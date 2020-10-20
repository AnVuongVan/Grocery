package com.vietis.eatit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class AddPromotionCodesActivity extends AppCompatActivity {
    private EditText promoCodeEt, promoDescriptionEt, promoPriceEt, minimumOrderPriceEt;
    private TextView expireDateTv;

    private FirebaseAuth firebaseAuth;
    ProgressDialog progressDialog;

    private String promoId;
    private boolean isUpdating;
    private String description, promoCode, promoPrice, minimumOrderPrice, expireDate;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_promotion_codes);

        ImageButton backBtn = findViewById(R.id.backBtn);
        promoCodeEt = findViewById(R.id.promoCodeEt);
        promoDescriptionEt = findViewById(R.id.promoDescriptionEt);
        promoPriceEt = findViewById(R.id.promoPriceEt);
        minimumOrderPriceEt = findViewById(R.id.minimumOrderPriceEt);
        expireDateTv = findViewById(R.id.expireDateTv);
        TextView titleTv = findViewById(R.id.titleTv);
        Button addBtn = findViewById(R.id.addBtn);

        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        Intent intent = getIntent();
        if (intent.getStringExtra("promoId") != null) {
            promoId = intent.getStringExtra("promoId");
            titleTv.setText("Update Promotion Codes");
            addBtn.setText("Update");
            isUpdating = true;
            loadPromotionInfo();
        } else {
            titleTv.setText("Add Promotion Codes");
            addBtn.setText("Confirm");
            isUpdating = false;
        }

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        expireDateTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                datePickDialog();
            }
        });

        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                insertData();
            }
        });

    }

    private void loadPromotionInfo() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(Objects.requireNonNull(firebaseAuth.getUid())).child("Promotions").child(promoId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String desc = (String) snapshot.child("description").getValue();
                        String code = (String) snapshot.child("promoCode").getValue();
                        String price = (String) snapshot.child("promoPrice").getValue();
                        String minimumOrder = (String) snapshot.child("minimumOrderPrice").getValue();
                        String expired = (String) snapshot.child("expireDate").getValue();

                        promoCodeEt.setText(code);
                        promoDescriptionEt.setText(desc);
                        promoPriceEt.setText(price);
                        minimumOrderPriceEt.setText(minimumOrder);
                        expireDateTv.setText(expired);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void datePickDialog() {
        Calendar calendar = Calendar.getInstance();
        int mYear = calendar.get(Calendar.YEAR);
        int mMonth = calendar.get(Calendar.MONTH);
        int mDay = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {
                DecimalFormat mFormat = new DecimalFormat("00");
                String pDay = mFormat.format(dayOfMonth);
                String pMonth = mFormat.format(monthOfYear + 1);
                String pYear = String.valueOf(year);
                String pDate = pDay + "/" + pMonth + "/" + pYear;
                expireDateTv.setText(pDate);
            }
        }, mYear, mMonth, mDay);

        datePickerDialog.show();
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
    }

    private void insertData() {
        description = promoDescriptionEt.getText().toString().trim();
        promoCode = promoCodeEt.getText().toString().trim();
        promoPrice = promoPriceEt.getText().toString().trim();
        minimumOrderPrice = minimumOrderPriceEt.getText().toString().trim();
        expireDate = expireDateTv.getText().toString().trim();

        if (TextUtils.isEmpty(promoCode)) {
            Toast.makeText(this, "Enter discount code", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(description)) {
            Toast.makeText(this, "Enter description", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(promoPrice)) {
            Toast.makeText(this, "Enter promotion price", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(minimumOrderPrice)) {
            Toast.makeText(this, "Enter minimum order price", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(expireDate)) {
            Toast.makeText(this, "Choose expired date", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isUpdating) {
            updateData();
        } else {
            addData();
        }
    }

    private void updateData() {
        progressDialog.setMessage("Updating promotion code...");
        progressDialog.show();

        Map<String, Object> hashMap = new HashMap<>();
        hashMap.put("description", description);
        hashMap.put("promoCode", promoCode);
        hashMap.put("promoPrice", promoPrice);
        hashMap.put("minimumOrderPrice", minimumOrderPrice);
        hashMap.put("expireDate", expireDate);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(Objects.requireNonNull(firebaseAuth.getUid())).child("Promotions")
                .child(promoId).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        progressDialog.dismiss();
                        onBackPressed();
                        Toast.makeText(AddPromotionCodesActivity.this, "Promotion code updated", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        onBackPressed();
                        Toast.makeText(AddPromotionCodesActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void addData() {
        progressDialog.setMessage("Adding promotion code...");
        progressDialog.show();

        String timestamp = String.valueOf(System.currentTimeMillis());
        Map<String, Object> hashMap = new HashMap<>();
        hashMap.put("id", timestamp);
        hashMap.put("timestamp", timestamp);
        hashMap.put("description", description);
        hashMap.put("promoCode", promoCode);
        hashMap.put("promoPrice", promoPrice);
        hashMap.put("minimumOrderPrice", minimumOrderPrice);
        hashMap.put("expireDate", expireDate);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(Objects.requireNonNull(firebaseAuth.getUid())).child("Promotions")
                .child(timestamp).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        progressDialog.dismiss();
                        onBackPressed();
                        Toast.makeText(AddPromotionCodesActivity.this, "Promotion code added", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        onBackPressed();
                        Toast.makeText(AddPromotionCodesActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

}