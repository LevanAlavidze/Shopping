package com.example.shopping;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shopping.activities.OrderDetailsUsersActivity;
import com.example.shopping.models.ModelOrderUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;

public class AdapterOrderUser extends RecyclerView.Adapter<AdapterOrderUser.HolderOrderUser>{

    Context context;
    ArrayList<ModelOrderUser> orderUsersList;
    DatabaseReference databaseReference;
    FirebaseDatabase database;
    private String USER_KEY = "Users";

    public AdapterOrderUser(Context context, ArrayList<ModelOrderUser> orderUsersList) {
        this.context = context;
        this.orderUsersList = orderUsersList;
    }

    class HolderOrderUser extends RecyclerView.ViewHolder{

        TextView orderIdTv, dateTv, shopNameTv, amountTv, statusTv;

        public HolderOrderUser(@NonNull View itemView) {
            super(itemView);

            orderIdTv = itemView.findViewById(R.id.orderIdTv);
            dateTv = itemView.findViewById(R.id.dateTv);
            shopNameTv = itemView.findViewById(R.id.shopNameTv);
            amountTv = itemView.findViewById(R.id.amountTv);
            statusTv = itemView.findViewById(R.id.statusTv);
        }
    }

    @NonNull
    @Override
    public HolderOrderUser onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_order_user,parent,false);
        return new HolderOrderUser(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderOrderUser holder, int position) {
        ModelOrderUser modelOrderUser = orderUsersList.get(position);
        String orderId = modelOrderUser.getOrderId();
        String orderStatus = modelOrderUser.getOrderStatus();
        String orderCost = modelOrderUser.getOrderCost();
        String orderBy = modelOrderUser.getOrderBy();
        String orderTo = modelOrderUser.getOrderTo();
        String orderTime = modelOrderUser.getOrderTime();

        loadShopInfo(modelOrderUser,holder);


        holder.amountTv.setText("Amount $"+orderCost);
        holder.statusTv.setText(orderStatus);
        holder.orderIdTv.setText(orderId);
        if (orderStatus.equals("In Progress")){
            holder.statusTv.setTextColor(context.getResources().getColor(R.color.colorPrimary));
        } else if (orderStatus.equals("Completed")) {
            holder.statusTv.setTextColor(context.getResources().getColor(R.color.colorGreen));
        }
        else if (orderStatus.equals("Canceled")) {
        holder.statusTv.setTextColor(context.getResources().getColor(R.color.colorRed));
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Long.parseLong(orderTime));
        String formatedDate = DateFormat.format("dd/MM/yyyy",calendar).toString();

        holder.dateTv.setText(formatedDate);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, OrderDetailsUsersActivity.class);
                intent.putExtra("orderTo", orderTo);
                intent.putExtra("orderId", orderId);
                context.startActivity(intent);
            }
        });

    }

    private void loadShopInfo(ModelOrderUser modelOrderUser, HolderOrderUser holder) {
        database = FirebaseDatabase.getInstance("https://shopping-29b18-default-rtdb.europe-west1.firebasedatabase.app/");
        databaseReference = database.getReference().child(USER_KEY);
        databaseReference.child(modelOrderUser.getOrderTo()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String shopName = ""+dataSnapshot.child("shopName").getValue();
                holder.shopNameTv.setText(shopName);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    public int getItemCount() {
        return orderUsersList.size();
    }


}
