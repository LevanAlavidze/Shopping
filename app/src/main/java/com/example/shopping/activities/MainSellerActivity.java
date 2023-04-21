package com.example.shopping.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.shopping.AdapterOrderShop;
import com.example.shopping.AdapterProductSeller;
import com.example.shopping.Constants;
import com.example.shopping.models.ModelOrderShop;
import com.example.shopping.models.ModelProduct;
import com.example.shopping.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

public class MainSellerActivity extends AppCompatActivity {

    TextView nameTv,shopNameTv,emailTv, tabProductsTv,tabOrdersTv, filteredProductsTv, filteredOrdersTv;
    EditText searchProductEt;
    RecyclerView productsRv, ordersRv;
    ImageButton logoutBtn,editProfileBtn, addProductBtn, filterProductBtn, filterOrderBtn, settingsBtn;
    ImageView profileIv;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    ArrayList<ModelProduct> productList;
    AdapterProductSeller adapterProductSeller;
    RelativeLayout productsRl,ordersRl;
    DatabaseReference databaseReference;
    FirebaseDatabase database;
    private String USER_KEY = "Users";

    ArrayList<ModelOrderShop> orderShopArrayList;
    AdapterOrderShop adapterOrderShop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_seller);
        nameTv = findViewById(R.id.nameTv);
        searchProductEt = findViewById(R.id.searchProductEt);
        filterProductBtn = findViewById(R.id.filterProductBtn);
        filteredProductsTv = findViewById(R.id.filteredProductsTv);
        emailTv = findViewById(R.id.emailTv);
        filterOrderBtn = findViewById(R.id.filterOrderBtn);
        filteredOrdersTv = findViewById(R.id.filteredOrdersTv);
        ordersRv = findViewById(R.id.ordersRv);
        productsRv = findViewById(R.id.productsRv);
        tabOrdersTv = findViewById(R.id.tabOrdersTv);
        settingsBtn = findViewById(R.id.settingsBtn);
        shopNameTv = findViewById(R.id.shopNameTv);
        logoutBtn = findViewById(R.id.logoutBtn);
        productsRl = findViewById(R.id.productsRl);
        ordersRl = findViewById(R.id.ordersRl);
        tabProductsTv = findViewById(R.id.tabProductsTv);
        profileIv = findViewById(R.id.profileIv);
        editProfileBtn = findViewById(R.id.editProfileBtn);
        addProductBtn = findViewById(R.id.addProductBtn);
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);
        firebaseAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance("https://shopping-29b18-default-rtdb.europe-west1.firebasedatabase.app/");
        databaseReference = database.getReference().child(USER_KEY);
        Toast.makeText(this, "Welcome Seller", Toast.LENGTH_SHORT).show();
        checkUser();
        loadAllProducts();
        loadALlOrders();
        showProductsUi();

        searchProductEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    adapterProductSeller.getFilter().filter(s);

                }
                catch (Exception e){
                    e.printStackTrace();

                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeMeOffline();

            }
        });
        addProductBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainSellerActivity.this, AddProductActivity.class));


            }
        });
        editProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //open profiled edit
                startActivity(new Intent(MainSellerActivity.this, ProfileEditSellerActivity.class));
            }
        });
        tabProductsTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProductsUi();

            }
        });
        tabOrdersTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showOrdersUi();

            }
        });
        filterProductBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainSellerActivity.this);
                builder.setTitle("Choose category:")
                        .setItems(Constants.productCategories1, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String selected = Constants.productCategories1[which];
                                filteredProductsTv.setText(selected);
                                if (selected.equals("All")){
                                    loadAllProducts();
                                }
                                else {
                                    loadFilteredProducts(selected);
                                }

                            }
                        }).show();
            }
        });

        filterOrderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] options = {"All","In Progress", "Completed", "Cancelled"};
                AlertDialog.Builder builder = new AlertDialog.Builder(MainSellerActivity.this);
                builder.setTitle("Filter Orders:")
                        .setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which==0){
                                    filteredOrdersTv.setText("Showing all orders");
                                    adapterOrderShop.getFilter().filter("");
                                }
                                else {
                                    String optionClicked = options[which];
                                    filteredOrdersTv.setText("Showing "+optionClicked+" Orders");
                                    adapterOrderShop.getFilter().filter(optionClicked);
                                }
                            }
                        }).show();

            }
        });

        settingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainSellerActivity.this, SettingsActivity.class));
            }
        });
    }

    private void loadALlOrders() {

        orderShopArrayList = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase
                .getInstance("https://shopping-29b18-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Orders")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        orderShopArrayList.clear();
                        for (DataSnapshot ds : dataSnapshot.getChildren()){
                            ModelOrderShop modelOrderShop = ds.getValue(ModelOrderShop.class);
                            orderShopArrayList.add(modelOrderShop);
                        }
                        adapterOrderShop = new AdapterOrderShop(MainSellerActivity.this,orderShopArrayList);
                        ordersRv.setAdapter(adapterOrderShop);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadFilteredProducts(String selected) {

        productList = new ArrayList<>();
        databaseReference.child(firebaseAuth.getUid()).child("Products")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        productList.clear();
                        for (DataSnapshot ds: dataSnapshot.getChildren()){
                            String productCategory = ""+ds.child("productCategory").getValue();
                            if (selected.equals(productCategory)){
                                ModelProduct modelProduct = ds.getValue(ModelProduct.class);
                                productList.add(modelProduct);
                            }

                        }
                        adapterProductSeller = new AdapterProductSeller(MainSellerActivity.this,productList);
                        productsRv.setAdapter(adapterProductSeller);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


    }

    private void loadAllProducts() {

        productList = new ArrayList<>();
        databaseReference.child(firebaseAuth.getUid()).child("Products")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        productList.clear();
                        for (DataSnapshot ds: dataSnapshot.getChildren()){
                            ModelProduct modelProduct = ds.getValue(ModelProduct.class);
                            productList.add(modelProduct);


                        }
                        adapterProductSeller = new AdapterProductSeller(MainSellerActivity.this,productList);
                        productsRv.setAdapter(adapterProductSeller);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


    }

    private void showProductsUi() {
        productsRl.setVisibility(View.VISIBLE);
        ordersRl.setVisibility(View.GONE);

        tabProductsTv.setTextColor(getResources().getColor(R.color.colorBlack));
        tabProductsTv.setBackgroundResource(R.drawable.shape_rect04);

        tabOrdersTv.setTextColor(getResources().getColor(R.color.colorWhite));
        tabOrdersTv.setBackgroundColor(getResources().getColor(android.R.color.transparent));

    }
    private void showOrdersUi() {
        productsRl.setVisibility(View.GONE);
        ordersRl.setVisibility(View.VISIBLE);
        tabProductsTv.setTextColor(getResources().getColor(R.color.colorWhite));
        tabProductsTv.setBackgroundColor(getResources().getColor(android.R.color.transparent));

        tabOrdersTv.setTextColor(getResources().getColor(R.color.colorBlack));
        tabOrdersTv.setBackgroundResource(R.drawable.shape_rect04);
    }



    private void makeMeOffline() {
        progressDialog.setMessage("Logging Out");
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("online","false");

        //updated value in DB
        DatabaseReference reference = FirebaseDatabase.getInstance("https://shopping-29b18-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Users");
        reference.child(firebaseAuth.getUid()).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //updated successfully
                        firebaseAuth.signOut();
                        checkUser();

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed to update

                        progressDialog.dismiss();
                        Toast.makeText(MainSellerActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkUser() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user==null){
            startActivity(new Intent(MainSellerActivity.this, LogInActivity.class));;
            finish();
        }
        else {
            loadMyInfo();
        }

    }

    private void loadMyInfo() {
        DatabaseReference reference = FirebaseDatabase.getInstance("https://shopping-29b18-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Users");
        reference.orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds: dataSnapshot.getChildren()){
                            String name = ""+ds.child("name").getValue();
                            String email = ""+ds.child("email").getValue();
                            String accountType = ""+ds.child("accountType").getValue();
                            String shopName = ""+ds.child("shopName").getValue();
                            String profileImage = ""+ds.child("profileImage").getValue();

                            nameTv.setText(name);
                            shopNameTv.setText(shopName);
                            emailTv.setText(email);

                            try {
                                Picasso.get().load(profileImage).placeholder(R.drawable.ic_store_gray).into(profileIv);
                            }
                            catch (Exception e ){
                                profileIv.setImageResource(R.drawable.ic_store_gray);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}