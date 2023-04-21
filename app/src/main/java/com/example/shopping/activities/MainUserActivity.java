package com.example.shopping.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.shopping.AdapterOrderUser;
import com.example.shopping.AdapterShop;
import com.example.shopping.R;
import com.example.shopping.models.ModelOrderUser;
import com.example.shopping.models.ModelShop;
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

public class MainUserActivity extends AppCompatActivity {

    TextView nameTv, emailTv, phoneTv, tabShopsTv, tabOrdersTv;
    ImageButton logoutBtn,editProfileBtn,settingsBtn;
    RelativeLayout shopsRl, ordersRl;
    ImageView profileIv;
    RecyclerView shopsRv,ordersRv;
    ArrayList<ModelShop> shopsList;
    ArrayList<ModelOrderUser> ordersList;
    AdapterOrderUser adapterOrderUser;
    AdapterShop adapterShop;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_user);

        nameTv = findViewById(R.id.nameTv);
        logoutBtn = findViewById(R.id.logoutBtn);
        profileIv = findViewById(R.id.profileIv);
        tabShopsTv = findViewById(R.id.tabShopsTv);
        tabOrdersTv = findViewById(R.id.tabOrdersTv);
        emailTv = findViewById(R.id.emailTv);
        settingsBtn = findViewById(R.id.settingsBtn);
        phoneTv = findViewById(R.id.phoneTv);
        shopsRl = findViewById(R.id.shopsRl);
        ordersRl = findViewById(R.id.ordersRl);
        shopsRv = findViewById(R.id.shopsRv);
        ordersRv = findViewById(R.id.ordersRv);
        editProfileBtn = findViewById(R.id.editProfileBtn);
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);
        firebaseAuth = FirebaseAuth.getInstance();
        checkUser();
        showShopsUi();
        Toast.makeText(this, "Welcome User", Toast.LENGTH_SHORT).show();


        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            makeMeOffline();
            }
        });
        editProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            //open profiled edit
                startActivity(new Intent(MainUserActivity.this, ProfileEditUserActivity.class));
            }
        });
        tabShopsTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showShopsUi();
            }
        });
        tabOrdersTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showOrdersUi();
            }
        });
        settingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainUserActivity.this, SettingsActivity.class));
            }
        });
    }

    private void showShopsUi() {
        shopsRl.setVisibility(View.VISIBLE);
        ordersRl.setVisibility(View.GONE);

        tabShopsTv.setTextColor(getResources().getColor(R.color.colorBlack));
        tabShopsTv.setBackgroundColor(R.drawable.shape_rect04);

        tabOrdersTv.setTextColor(getResources().getColor(R.color.colorWhite));
        tabOrdersTv.setBackgroundColor(getResources().getColor(android.R.color.transparent));

    }

    private void showOrdersUi() {
        shopsRl.setVisibility(View.GONE);
        ordersRl.setVisibility(View.VISIBLE);

        tabShopsTv.setTextColor(getResources().getColor(R.color.colorWhite));
        tabShopsTv.setBackgroundColor(getResources().getColor(android.R.color.transparent));

        tabOrdersTv.setTextColor(getResources().getColor(R.color.colorBlack));
        tabOrdersTv.setBackgroundColor(R.drawable.shape_rect04);



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
                        //update successfully
                        firebaseAuth.signOut();
                        checkUser();

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed to update

                        progressDialog.dismiss();
                        Toast.makeText(MainUserActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void checkUser() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user==null){
            startActivity(new Intent(MainUserActivity.this, LogInActivity.class));;
            finish();
        }
        else {
            loadMyInfo();
        }

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
                            String phone = ""+ds.child("phone").getValue();
                            String profileImage = ""+ds.child("profileImage").getValue();
                            String accountType = ""+ds.child("accountType").getValue();
                            String city = ""+ds.child("city").getValue();

                            nameTv.setText(name);
                            emailTv.setText(email);
                            phoneTv.setText(phone);
                            try {
                                Picasso.get().load(profileImage).placeholder(R.drawable.ic_person_gray);
                            }
                            catch (Exception e){
                                profileIv.setImageResource(R.drawable.ic_person_gray);
                            }

                            loadShops(city);
                            loadOrders();

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadOrders() {
        ordersList = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase
                .getInstance("https://shopping-29b18-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ordersList.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    String uid = ""+ds.getRef().getKey();

                    DatabaseReference reference = FirebaseDatabase
                            .getInstance("https://shopping-29b18-default-rtdb.europe-west1.firebasedatabase.app/")
                            .getReference("Users").child(uid).child("Orders");
                    reference.orderByChild("orderBy").equalTo(firebaseAuth.getUid())
                            .addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()){
                                        for (DataSnapshot ds: dataSnapshot.getChildren()){
                                            ModelOrderUser modelOrderUser = ds.getValue(ModelOrderUser.class);
                                            ordersList.add(modelOrderUser);
                                        }
                                        adapterOrderUser = new AdapterOrderUser(MainUserActivity.this,ordersList);

                                        ordersRv.setAdapter(adapterOrderUser);
                                    }

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadShops(String myCity) {
        shopsList= new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase
                .getInstance("https://shopping-29b18-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("Users");

        reference.orderByChild("accountType").equalTo("Seller")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        shopsList.clear();
                        for (DataSnapshot ds : dataSnapshot.getChildren()){
                            ModelShop modelShop = ds.getValue(ModelShop.class);
                            String shopCity = ""+ds.child("city").getValue();
                            if (shopCity.equals(myCity)){
                                shopsList.add(modelShop);
                            }
                        }
                        adapterShop = new AdapterShop(MainUserActivity.this,shopsList);
                        shopsRv.setAdapter(adapterShop);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}