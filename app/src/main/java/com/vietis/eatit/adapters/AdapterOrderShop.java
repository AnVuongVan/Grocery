package com.vietis.eatit.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.vietis.eatit.OrderDetailsSellerActivity;
import com.vietis.eatit.R;
import com.vietis.eatit.models.OrderUser;
import com.vietis.eatit.utils.FilterOrderShop;

import java.util.Calendar;
import java.util.List;

public class AdapterOrderShop extends RecyclerView.Adapter<AdapterOrderShop.HolderOrderShop> implements Filterable {
    private Context context;
    public List<OrderUser> orderSellerList, filterList;
    private FilterOrderShop filter;

    public AdapterOrderShop(Context context, List<OrderUser> orderSellerList) {
        this.context = context;
        this.orderSellerList = orderSellerList;
        this.filterList = orderSellerList;
    }

    @NonNull
    @Override
    public HolderOrderShop onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_order_seller, parent, false);
        return new HolderOrderShop(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull HolderOrderShop holder, int position) {
        OrderUser model = orderSellerList.get(position);
        final String orderId = model.getOrderId();
        String status = model.getOrderStatus();
        String timestamp = model.getOrderTime();
        String cost = model.getOrderCost();
        final String orderBy = model.getOrderBy();
        loadUserInfo(orderBy, holder);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Long.parseLong(timestamp));
        String dateTime = DateFormat.format("dd/MM/yyyy", calendar).toString();

        holder.amountTv.setText("$" + cost);
        holder.statusTv.setText(status);
        holder.orderIdTv.setText("OrderID: " + orderId);
        holder.orderDateTv.setText(dateTime);

        switch (status) {
            case "In Progress":
                holder.statusTv.setTextColor(context.getResources().getColor(R.color.colorPrimary));
                break;
            case "Completed":
                holder.statusTv.setTextColor(context.getResources().getColor(R.color.colorGreen));
                break;
            case "Cancelled":
                holder.statusTv.setTextColor(context.getResources().getColor(R.color.colorRed));
                break;
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, OrderDetailsSellerActivity.class);
                intent.putExtra("orderId", orderId);
                intent.putExtra("orderBy", orderBy);
                context.startActivity(intent);
            }
        });
    }

    private void loadUserInfo(String orderBy, final HolderOrderShop holder) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(orderBy)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String email = (String) snapshot.child("email").getValue();
                        holder.emailTv.setText(email);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return orderSellerList.size();
    }

    @Override
    public Filter getFilter() {
        if (filter == null) {
            filter = new FilterOrderShop(this, filterList);
        }
        return filter;
    }

    static class HolderOrderShop extends RecyclerView.ViewHolder {
        private TextView orderIdTv, orderDateTv, emailTv, amountTv, statusTv;

        public HolderOrderShop(@NonNull View itemView) {
            super(itemView);
            orderIdTv = itemView.findViewById(R.id.orderIdTv);
            orderDateTv = itemView.findViewById(R.id.orderDateTv);
            emailTv = itemView.findViewById(R.id.emailTv);
            amountTv = itemView.findViewById(R.id.amountTv);
            statusTv = itemView.findViewById(R.id.statusTv);
        }
    }

}
