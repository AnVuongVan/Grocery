package com.vietis.eatit.adapters;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.vietis.eatit.AddPromotionCodesActivity;
import com.vietis.eatit.R;
import com.vietis.eatit.models.Promotion;

import java.util.List;
import java.util.Objects;

public class PromotionShop extends RecyclerView.Adapter<PromotionShop.HolderPromotionShop> {
    private Context context;
    private List<Promotion> promotionList;

    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    public PromotionShop(Context context, List<Promotion> promotionList) {
        this.context = context;
        this.promotionList = promotionList;

        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);
    }

    @NonNull
    @Override
    public HolderPromotionShop onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_promotion_shop, parent, false);
        return new HolderPromotionShop(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull final HolderPromotionShop holder, int position) {
        Promotion model = promotionList.get(position);
        final String id = model.getId();
        String description = model.getDescription();
        String promoCode = model.getPromoCode();
        String promoPrice = model.getPromoPrice();
        String expireDate = model.getExpireDate();
        String minimumOrderPrice = model.getMinimumOrderPrice();

        holder.descriptionTv.setText(description);
        holder.promoCodeTv.setText("Code: " + promoCode);
        holder.promoPriceTv.setText(promoPrice);
        holder.expireDateTv.setText("Expire Date: " + expireDate);
        holder.minimumOrderPriceTv.setText(minimumOrderPrice);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editDialog(id);
            }
        });
    }

    private void editDialog(final String id) {
        String[] options = {"Edit", "Remove"};
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Choose Option")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (i == 0) {
                            editPromotionCode(id);
                        } else if (i == 1) {
                            removePromotionCode(id);
                        }
                    }
                })
                .show();
    }

    private void removePromotionCode(String id) {
        progressDialog.setMessage("Removing promotion codes...");
        progressDialog.show();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(Objects.requireNonNull(firebaseAuth.getUid())).child("Promotions").child(id)
                .removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        progressDialog.dismiss();
                        Toast.makeText(context, "Removed successfully", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void editPromotionCode(String id) {
        Intent intent = new Intent(context, AddPromotionCodesActivity.class);
        intent.putExtra("promoId", id);
        context.startActivity(intent);
    }

    @Override
    public int getItemCount() {
        return promotionList.size();
    }

    static class HolderPromotionShop extends RecyclerView.ViewHolder {
        private TextView promoCodeTv, promoPriceTv, minimumOrderPriceTv,
                expireDateTv, descriptionTv;

        public HolderPromotionShop(@NonNull View itemView) {
            super(itemView);
            promoCodeTv = itemView.findViewById(R.id.promoCodeTv);
            promoPriceTv = itemView.findViewById(R.id.promoPriceTv);
            minimumOrderPriceTv = itemView.findViewById(R.id.minimumOrderPriceTv);
            expireDateTv = itemView.findViewById(R.id.expireDateTv);
            descriptionTv = itemView.findViewById(R.id.descriptionTv);
        }
    }

}
