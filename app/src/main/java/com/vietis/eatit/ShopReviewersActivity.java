package com.vietis.eatit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.vietis.eatit.adapters.AdapterReview;
import com.vietis.eatit.models.Review;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ShopReviewersActivity extends AppCompatActivity {
    private ImageView profileIv;
    private TextView shopNameTv, ratingsTv;
    private RatingBar ratingBar;
    private RecyclerView reviewersRv;

    private List<Review> reviewList;
    private AdapterReview adapterReview;

    private float ratingSum;
    private String shopUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_reviewers);

        ImageButton backBtn = findViewById(R.id.backBtn);
        profileIv = findViewById(R.id.profileIv);
        shopNameTv = findViewById(R.id.shopNameTv);
        ratingsTv = findViewById(R.id.ratingsTv);
        ratingBar = findViewById(R.id.ratingBar);
        reviewersRv = findViewById(R.id.reviewersRv);

        shopUid = getIntent().getStringExtra("shopUid");
        loadShopDetails();
        loadReviews();

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

    }

    private void loadReviews() {
        reviewList = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(shopUid).child("Ratings")
                .addValueEventListener(new ValueEventListener() {
                    @SuppressLint({"DefaultLocale", "SetTextI18n"})
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        reviewList.clear();
                        ratingSum = 0;
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            float rating = Float.parseFloat((String) Objects.requireNonNull(dataSnapshot.child("ratings").getValue()));
                            ratingSum += rating;

                            Review model = dataSnapshot.getValue(Review.class);
                            reviewList.add(model);
                        }
                        adapterReview = new AdapterReview(ShopReviewersActivity.this, reviewList);
                        reviewersRv.setAdapter(adapterReview);

                        long numberOfReviews = snapshot.getChildrenCount();
                        float avgRatings = ratingSum/numberOfReviews;
                        ratingsTv.setText(String.format("%.2f", avgRatings) + " [" + numberOfReviews + "]");
                        ratingBar.setRating(avgRatings);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadShopDetails() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(shopUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String shopName = (String) snapshot.child("shopName").getValue();
                        String profileImage = (String) snapshot.child("profileImage").getValue();

                        shopNameTv.setText(shopName);
                        try {
                            Picasso.get().load(profileImage).placeholder(R.drawable.ic_store_gray)
                                    .into(profileIv);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}