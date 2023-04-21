package com.example.shopping.activities;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.shopping.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SplashActivity extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    DatabaseReference databaseReference;
    FirebaseDatabase database;
    private String USER_KEY = "Users";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //full screen
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);
        database = FirebaseDatabase.getInstance("https://shopping-29b18-default-rtdb.europe-west1.firebasedatabase.app/");
        databaseReference = database.getReference().child(USER_KEY);
        firebaseAuth = FirebaseAuth.getInstance();
        //go to login - 2sec

     /*   new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(SplashActivity.this, LogInActivity.class));
                finish();
            }
        }, 2000);*/

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    //user not logged
                    startActivity(new Intent(SplashActivity.this, LogInActivity.class));
                    finish();
                }
                else {
                    //user is logged in
                    checkUserType();
                }
            }

        },1000);

    }
    private void checkUserType() {
        DatabaseReference userRef = databaseReference.child(firebaseAuth.getUid()); // Get the reference to the user's data
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String accountType = dataSnapshot.child("accountType").getValue(String.class);
                    if (accountType != null && accountType.equals("Seller")) {

                        Toast.makeText(SplashActivity.this, "Seller account", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(SplashActivity.this, MainSellerActivity.class));
                        finish();
                    } else if (accountType != null && accountType.equals("User")) {

                        Toast.makeText(SplashActivity.this, "User account", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(SplashActivity.this, MainUserActivity.class));
                        finish();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle error
            }
        });


    }

    }
