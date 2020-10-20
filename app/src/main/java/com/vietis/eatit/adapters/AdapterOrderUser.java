package com.vietis.eatit.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.vietis.eatit.OrdersDetailsUsersActivity;
import com.vietis.eatit.R;
import com.vietis.eatit.models.OrderUser;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AdapterOrderUser extends RecyclerView.Adapter<AdapterOrderUser.MyHolder> {
    private Context context;
    private List<OrderUser> orderUserList;

    public AdapterOrderUser(Context context, List<OrderUser> orderUserList) {
        this.context = context;
        this.orderUserList = orderUserList;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_order_user, parent, false);
        return new MyHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        OrderUser orderUser = orderUserList.get(position);
        final String orderId = orderUser.getOrderId();
        final String orderTo = orderUser.getOrderTo();
        String timeStamp = orderUser.getOrderTime();
        String status = orderUser.getOrderStatus();
        String cost = orderUser.getOrderCost();
        
        loadShopInfo(orderUser, holder);

        holder.amountTv.setText("Amount: $" + cost);
        holder.statusTv.setText(status);
        holder.orderIdTv.setText("OrderID: " + orderId);
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

        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(Long.parseLong(timeStamp));
        String dateTime = DateFormat.format("dd/MM/yyyy", calendar).toString();
        holder.dateTv.setText(dateTime);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, OrdersDetailsUsersActivity.class);
                intent.putExtra("orderTo", orderTo);
                intent.putExtra("orderId", orderId);
                context.startActivity(intent);
            }
        });
    }

    private void loadShopInfo(OrderUser orderUser, final MyHolder holder) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(orderUser.getOrderTo())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String shopName = (String) snapshot.child("shopName").getValue();
                        holder.shopNameTv.setText(shopName);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return orderUserList.size();
    }

    static class MyHolder extends RecyclerView.ViewHolder {
        private TextView orderIdTv, dateTv, shopNameTv, amountTv, statusTv;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            orderIdTv = itemView.findViewById(R.id.orderIdTv);
            dateTv = itemView.findViewById(R.id.dateTv);
            shopNameTv = itemView.findViewById(R.id.shopNameTv);
            amountTv = itemView.findViewById(R.id.amountTv);
            statusTv = itemView.findViewById(R.id.statusTv);
        }
    }

}
