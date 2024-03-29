package com.vietis.eatit.adapters;

import android.content.Context;
import android.text.format.DateFormat;
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
import com.vietis.eatit.models.Review;

import java.util.Calendar;
import java.util.List;

public class AdapterReview extends RecyclerView.Adapter<AdapterReview.HolderReview> {
    private Context context;
    private List<Review> reviewList;

    public AdapterReview(Context context, List<Review> reviewList) {
        this.context = context;
        this.reviewList = reviewList;
    }

    @NonNull
    @Override
    public HolderReview onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_review, parent, false);
        return new HolderReview(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderReview holder, int position) {
        Review model = reviewList.get(position);
        String uid = model.getUid();
        String review = model.getReview();
        String ratings = model.getRatings();
        String timestamp = model.getTimestamp();

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Long.parseLong(timestamp));
        String dateTime = DateFormat.format("dd/MM/yyyy", calendar).toString();

        holder.ratingBar.setRating(Float.parseFloat(ratings));
        holder.reviewTv.setText(review);
        holder.dateTv.setText(dateTime);
        loadUserDetails(uid, holder);
    }

    private void loadUserDetails(String uid, final HolderReview holder) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(uid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String name = (String) snapshot.child("name").getValue();
                        String profileImage = (String) snapshot.child("profileImage").getValue();

                        holder.nameTv.setText(name);
                        try {
                            Picasso.get().load(profileImage).placeholder(R.drawable.ic_person_gray)
                                    .into(holder.profileIv);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return reviewList.size();
    }

    static class HolderReview extends RecyclerView.ViewHolder {
        private ImageView profileIv;
        private TextView nameTv, dateTv, reviewTv;
        private RatingBar ratingBar;
        public HolderReview(@NonNull View itemView) {
            super(itemView);
            profileIv = itemView.findViewById(R.id.profileIv);
            nameTv = itemView.findViewById(R.id.nameTv);
            dateTv = itemView.findViewById(R.id.dateTv);
            reviewTv = itemView.findViewById(R.id.reviewTv);
            ratingBar = itemView.findViewById(R.id.ratingBar);
        }
    }

}
