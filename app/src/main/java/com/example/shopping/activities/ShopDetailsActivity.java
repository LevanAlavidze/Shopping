package com.example.shopping.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.shopping.AdapterCartItem;
import com.example.shopping.AdapterProductUser;
import com.example.shopping.Constants;
import com.example.shopping.R;
import com.example.shopping.models.ModelCartItem;
import com.example.shopping.models.ModelProduct;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import p32929.androideasysql_library.Column;
import p32929.androideasysql_library.EasyDB;

public class ShopDetailsActivity extends AppCompatActivity {
    ImageView shopIv;
    TextView shopNameTv, phoneTv, emailTv, openCloseTv, deliveryFeeTv, addressTv, filteredProductsTv,cartCountTv;
    ImageButton callBtn, mapBtn, cartBtn, backBtn, filterProductBtn;
    EditText searchProductEt;
    RecyclerView productsRv;
    FirebaseAuth firebaseAuth;
    String shopUid, shopPhone, shopName, shopEmail, myLatitude, myLongitude, shopAddress, shopLatitude, shopLongitude;
    public String deliveryFee, myPhone;
    ArrayList<ModelProduct> productsList;
    AdapterProductUser adapterProductUser;
    ProgressDialog progressDialog;

    ArrayList<ModelCartItem> cartItemsList;
    AdapterCartItem adapterCartItem;
    DatabaseReference databaseReference;
    FirebaseDatabase database;
    private String USER_KEY = "Users";
    private EasyDB easyDB;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_details);

        shopIv = findViewById(R.id.shopIv);
        shopNameTv = findViewById(R.id.shopNameTv);
        phoneTv = findViewById(R.id.phoneTv);
        emailTv = findViewById(R.id.emailTv);
        openCloseTv = findViewById(R.id.openCloseTv);
        deliveryFeeTv = findViewById(R.id.deliveryFeeTv);
        addressTv = findViewById(R.id.addressTv);
        filteredProductsTv = findViewById(R.id.filteredProductsTv);
        callBtn = findViewById(R.id.callBtn);
        cartCountTv = findViewById(R.id.cartCountTv);
        mapBtn = findViewById(R.id.mapBtn);
        cartBtn = findViewById(R.id.cartBtn);
        backBtn = findViewById(R.id.backBtn);
        filterProductBtn = findViewById(R.id.filterProductBtn);
        searchProductEt = findViewById(R.id.searchProductEt);
        productsRv = findViewById(R.id.productsRv);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        shopUid = getIntent().getStringExtra("shopUid");
        firebaseAuth = FirebaseAuth.getInstance();
        loadMyInfo();
        loadShopDetails();
        loadShopProducts();

        easyDB = EasyDB.init(this,"ITEMS_DB")
                .setTableName("ITEMS_TABLE")
                .addColumn(new Column("Item_id",new String[]{"text","unique"}))
                .addColumn(new Column("Item_Pid",new String[]{"text","not null"}))
                .addColumn(new Column("Item_Name",new String[]{"text","not null"}))
                .addColumn(new Column("Item_Price_Each",new String[]{"text","not null"}))
                .addColumn(new Column("Item_Price",new String[]{"text","not null"}))
                .addColumn(new Column("Item_Quantity",new String[]{"text","not null"}))
                .doneTableColumn();

        // all shops are different so each must have its own cartData
        deleteCartData();
        cartCount();

        searchProductEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    adapterProductUser.getFilter().filter(s);

                }
                catch (Exception e){
                    e.printStackTrace();

                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();

            }
        });

        cartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showCartDialog();

            }
        });

        callBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialPhone();
            }
        });

        mapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMap();
            }
        });
        filterProductBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ShopDetailsActivity.this);
                builder.setTitle("Choose category:")
                        .setItems(Constants.productCategories1, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String selected = Constants.productCategories1[which];
                                filteredProductsTv.setText(selected);
                                if (selected.equals("All")){
                                    loadShopProducts();
                                }
                                else {
                                    adapterProductUser.getFilter().filter(selected);
                                }

                            }
                        }).show();
            }
        });
    }

    private void deleteCartData() {
        easyDB.deleteAllDataFromTable();
    }

    public  void cartCount(){
        int count = easyDB.getAllData().getCount();
        if (count<=0){
            cartCountTv.setVisibility(View.GONE);
        }
        else {
            cartCountTv.setVisibility(View.VISIBLE);
            cartCountTv.setText(""+count);

        }
    }

    public double allTotalPrice = 0.00;
        public TextView sTotalTv, dFeeTv, allTotalPriceTv;
    private void showCartDialog() {
        cartItemsList = new ArrayList<>();
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_cart,null);
        TextView shopNameTv = view.findViewById(R.id.shopNameTv);
        RecyclerView cartItemRv = view.findViewById(R.id.cartItemsRv);
        sTotalTv = view.findViewById(R.id.sTotalLabelTv);
        dFeeTv = view.findViewById(R.id.dFeeTv);
        allTotalPriceTv = view.findViewById(R.id.totalTv);
        Button checkoutBtn = view.findViewById(R.id.checkoutBtn);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(view);

        shopNameTv.setText(shopName);
        EasyDB easyDB = EasyDB.init(this,"ITEMS_DB")
                .setTableName("ITEMS_TABLE")
                .addColumn(new Column("Item_id",new String[]{"text","unique"}))
                .addColumn(new Column("Item_Pid",new String[]{"text","not null"}))
                .addColumn(new Column("Item_Name",new String[]{"text","not null"}))
                .addColumn(new Column("Item_Price_Each",new String[]{"text","not null"}))
                .addColumn(new Column("Item_Price",new String[]{"text","not null"}))
                .addColumn(new Column("Item_Quantity",new String[]{"text","not null"}))
                .doneTableColumn();

        Cursor res = easyDB.getAllData();
        while (res.moveToNext()){
            String id = res.getString(1);
            String pId = res.getString(2);
            String name = res.getString(3);
            String price = res.getString(4);
            String cost = res.getString(5);
            String quantity = res.getString(6);

            allTotalPrice = allTotalPrice+Double.parseDouble(cost);

            ModelCartItem modelCartItem = new ModelCartItem(""+id,
                    ""+pId,
                    ""+name,
                    ""+price,
                    ""+cost,
                    ""+quantity);

            cartItemsList.add(modelCartItem);

        }

        adapterCartItem = new AdapterCartItem(this,cartItemsList);

        cartItemRv.setAdapter(adapterCartItem);
        dFeeTv.setText("$"+deliveryFee);
        sTotalTv.setText("$" + String.format("%.2f", allTotalPrice));
        String deliveryFeeValue = deliveryFee;
        if (deliveryFeeValue.contains("$")) {
            deliveryFeeValue = deliveryFeeValue.replace("$", "");
        }

        deliveryFee.replaceAll("[^\\d.]", "");
        double deliveryFeeDouble = Double.parseDouble(deliveryFeeValue);

        //double deliveryFeeDouble = Double.parseDouble(deliveryFeeValue);
        double totalPrice = allTotalPrice + deliveryFeeDouble;
        allTotalPriceTv.setText("$" + String.format("%.2f", totalPrice));

        allTotalPriceTv.setText("$"+(allTotalPrice + Double.parseDouble(deliveryFee.replace("$",""))));



        //reset total price
        AlertDialog dialog = builder.create();
        dialog.show();

        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                allTotalPrice = 0.00;
            }
        });

        checkoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Objects.equals(myLatitude, "") || Objects.equals(myLatitude, "null") || Objects.equals(myLongitude, "") || Objects.equals(myLongitude, "null")) {

                   Toast.makeText(ShopDetailsActivity.this,
                           "Please enter your address in your profile before placing order",
                           Toast.LENGTH_SHORT).show();
                   return;
                 }
                if (Objects.equals(myPhone, "") || Objects.equals(myPhone, "null")) {

                    Toast.makeText(ShopDetailsActivity.this,
                            "Please enter your phone number in your profile before placing order",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                if (cartItemsList.size()==0){
                    Toast.makeText(ShopDetailsActivity.this, "no item in cart", Toast.LENGTH_SHORT).show();

                }
                submitOrder();
            }
        });

    }
    private void submitOrder() {
        progressDialog.setMessage("Placing Order");
        progressDialog.show();

        String timestamp = ""+System.currentTimeMillis();

        String cost = allTotalPriceTv.getText().toString().trim().replace("$","");

        HashMap<String,String> hashMap = new HashMap<>();
        hashMap.put("orderId",""+timestamp);
        hashMap.put("orderTime",""+timestamp);
        hashMap.put("orderStatus","In Progress");
        hashMap.put("orderCost",""+cost);
        hashMap.put("orderBy",""+firebaseAuth.getUid());
        hashMap.put("orderTo",""+shopUid);
        hashMap.put("latitude",""+myLatitude);
        hashMap.put("longitude",""+myLongitude);


        database = FirebaseDatabase.getInstance("https://shopping-29b18-default-rtdb.europe-west1.firebasedatabase.app/");
        databaseReference = database.getReference().child(USER_KEY).child(shopUid).child("Orders");
        databaseReference.child(timestamp).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {

                        for (int i=0; i<cartItemsList.size(); i++){
                            String pId = cartItemsList.get(i).getpId();
                            String id = cartItemsList.get(i).getId();
                            String cost = cartItemsList.get(i).getCost();
                            String name = cartItemsList.get(i).getName();
                            String price = cartItemsList.get(i).getPrice();
                            String quantity = cartItemsList.get(i).getQuantity();

                            HashMap<String, String>hashMap1 = new HashMap<>();
                            hashMap1.put("pId",pId);
                            hashMap1.put("name",name);
                            hashMap1.put("cost",cost);
                            hashMap1.put("price",price);
                            hashMap1.put("quantity",quantity);

                            databaseReference.child(timestamp).child("Items")
                                    .child(pId).setValue(hashMap1);

                        }
                        progressDialog.dismiss();
                        Toast.makeText(ShopDetailsActivity.this, "Order Placed Successfully", Toast.LENGTH_SHORT).show();

                        prepareNotificationMessage(timestamp);



                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(ShopDetailsActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void openMap() {
        String address = "https://maps.google.com/maps?saddr="+myLatitude+","+myLongitude+"&daddr="+shopLatitude+","+shopLongitude;
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(address));
        startActivity(intent);
    }
    private void dialPhone() {
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + Uri.encode(shopPhone)));
        startActivity(intent);
        Toast.makeText(this, "" + shopPhone, Toast.LENGTH_SHORT).show();
    }
    private void loadMyInfo() {
        DatabaseReference reference = FirebaseDatabase
                .getInstance("https://shopping-29b18-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("Users");
        reference.orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds: dataSnapshot.getChildren()){
                            String name = ""+ds.child("name").getValue();
                            String email = ""+ds.child("email").getValue();
                            String myPhone = ""+ds.child("phone").getValue();
                            String profileImage = ""+ds.child("profileImage").getValue();
                            String accountType = ""+ds.child("accountType").getValue();
                            String city = ""+ds.child("city").getValue();
                            myLatitude = ""+ds.child("latitude").getValue();
                            myLongitude = ""+ds.child("longitude").getValue();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
    private void loadShopDetails() {
        DatabaseReference reference = FirebaseDatabase
                .getInstance("https://shopping-29b18-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("Users");
        reference.child(shopUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                String name = ""+datasnapshot.child("name").getValue();
                shopName = ""+datasnapshot.child("shopName").getValue();
                shopEmail = ""+datasnapshot.child("email").getValue();
                shopPhone = ""+datasnapshot.child("phone").getValue();
                shopPhone = ""+datasnapshot.child("phone").getValue();
                shopAddress = ""+datasnapshot.child("address").getValue();
                shopLatitude = ""+datasnapshot.child("latitude").getValue();
                shopLongitude = ""+datasnapshot.child("longitude").getValue();
                deliveryFee = ""+datasnapshot.child("deliveryFee").getValue();
                String profileImage = ""+datasnapshot.child("profileImage").getValue();
                String shopOpen = ""+datasnapshot.child("shopOpen").getValue();

                shopNameTv.setText(shopName);
                emailTv.setText(shopEmail);
                deliveryFeeTv.setText("Delivery Fee: $"+deliveryFee);
                addressTv.setText(shopAddress);
                phoneTv.setText(shopPhone);
                if (shopOpen.equals("true")){
                    openCloseTv.setText("Open");
                }
                else {
                    openCloseTv.setText("Closed");
                }
                try {
                    Picasso.get().load(profileImage).into(shopIv);
                }
                catch (Exception e){}
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
    private void loadShopProducts() {

        productsList= new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase
                .getInstance("https://shopping-29b18-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("Users");
        reference.child(shopUid).child("Products").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                productsList.clear();
                for (DataSnapshot ds : datasnapshot.getChildren()){
                    ModelProduct modelProduct = ds.getValue(ModelProduct.class);
                    productsList.add(modelProduct);
                }
                adapterProductUser = new AdapterProductUser(ShopDetailsActivity.this,productsList);
                productsRv.setAdapter(adapterProductUser);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    public void prepareNotificationMessage(String orderId){
        String NOTIFICATION_TOPIC = "/topics/"+Constants.FCM_TOPIC;
        String NOTIFICATION_TITLE = "New Order"+orderId;
        String NOTIFICATION_MESSAGE = "Congratulations, You have NEW order";
        String NOTIFICATION_TYPE = "NewOrder";

        Log.d("FCM", "orderId: " + orderId);
        Log.d("FCM", "NOTIFICATION_TOPIC: " + NOTIFICATION_TOPIC);
        Log.d("FCM", "NOTIFICATION_TITLE: " + NOTIFICATION_TITLE);
        Log.d("FCM", "NOTIFICATION_MESSAGE: " + NOTIFICATION_MESSAGE);
        Log.d("FCM", "NOTIFICATION_TYPE: " + NOTIFICATION_TYPE);

        JSONObject notificationJo = new JSONObject();
        JSONObject notificationBodyJo = new JSONObject();
        try {
            notificationBodyJo.put("notificationType",NOTIFICATION_TYPE);
            notificationBodyJo.put("buyerUid",firebaseAuth.getUid());
            notificationBodyJo.put("sellerUid",shopUid);
            notificationBodyJo.put("orderId",orderId);
            notificationBodyJo.put("notificationTitle",NOTIFICATION_TITLE);
            notificationBodyJo.put("notificationMessage",NOTIFICATION_MESSAGE);

            notificationJo.put("to",NOTIFICATION_TOPIC);
            notificationJo.put("data",notificationBodyJo);

        }
        catch (Exception e){
            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

        }
        sendFcmNotification(notificationJo, orderId);
    }

    private void sendFcmNotification(JSONObject notificationJo, String orderId) {

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest("https://fcm.googleapis.com/fcm/send"
                , notificationJo, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d("FCM", "Notification sent: " + response.toString());
                Intent intent = new Intent(ShopDetailsActivity.this, OrderDetailsUsersActivity.class);
                intent.putExtra("orderTo", shopUid);
                intent.putExtra("orderId", orderId);
                startActivity(intent);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("FCM", "Notification send failed: " + error.toString());
                Intent intent = new Intent(ShopDetailsActivity.this, OrderDetailsUsersActivity.class);
                intent.putExtra("orderTo", shopUid);
                intent.putExtra("orderId", orderId);
                startActivity(intent);

            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> headers = new HashMap<>();
                headers.put("Content-Type","application/json");
                headers.put("Authorization","key="+Constants.FCM_KEY);
                return headers;
            }
        };

        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                5000, // 5 seconds in case of a network error
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }

}