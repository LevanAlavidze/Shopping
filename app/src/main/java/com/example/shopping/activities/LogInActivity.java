package com.example.shopping.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.shopping.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class LogInActivity extends AppCompatActivity {

    EditText emailEt, passwordEt;
    TextView forgotTv, noAccountTv;
    Button loginBtn;
    private FirebaseAuth firebaseAuth;
    ProgressDialog progressDialog;
    DatabaseReference databaseReference;
    FirebaseDatabase database;
    private String email, password;
    private String USER_KEY = "Users";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);
        emailEt = findViewById(R.id.emailEt);
        passwordEt = findViewById(R.id.passwordEt);
        forgotTv = findViewById(R.id.forgotTv);
        noAccountTv = findViewById(R.id.noAccountTv);
        loginBtn = findViewById(R.id.loginBtn);
        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setCanceledOnTouchOutside(false);
        database = FirebaseDatabase.getInstance("https://shopping-29b18-default-rtdb.europe-west1.firebasedatabase.app/");
        databaseReference = database.getReference().child(USER_KEY);
        noAccountTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LogInActivity.this, RegisterUserActivity.class));
            }
        });
        forgotTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LogInActivity.this, ForgotPasswordActivity.class));
            }
        });
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });
    }

    private void loginUser() {
        email = emailEt.getText().toString().trim();
        password = passwordEt.getText().toString().trim();
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid Email Pattern", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Enter Password", Toast.LENGTH_SHORT).show();
            return;
        }
        progressDialog.setMessage("Logging In");
        progressDialog.show();
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        //logged succesffully
                        makeMeOnline();

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed to login
                        progressDialog.dismiss();
                        Toast.makeText(LogInActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void makeMeOnline() {
        //after login make user online
        progressDialog.setMessage("Checking user");
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("online", "true");

        //updated value in DB

        //DatabaseReference reference = FirebaseDatabase.getInstance("https://shopping-29b18-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Users");
        databaseReference.child(firebaseAuth.getUid()).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //update successfully
                        checkUserType();


                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed to update
                        progressDialog.dismiss();
                        Toast.makeText(LogInActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

   /* private void checkUserType1() {
        //seller or ordinary user
        //DatabaseReference reference = FirebaseDatabase.getInstance("https://shopping-29b18-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Users");
        databaseReference.orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            String accountType = "" + ds.child("accountType").getValue(String.class);

                            if (accountType.equals("Seller")) {
                                progressDialog.dismiss();
                                startActivity(new Intent(LogInActivity.this, MainSellerActivity.class));
                                finish();
                            } else {

                                progressDialog.dismiss();
                                startActivity(new Intent(LogInActivity.this, MainUserActivity.class));
                                finish();

                                 *//*   String accountType = snapshot.child("accountType").getValue(String.class);
                                    if (accountType != null && accountType.equals("True")) {
                                        progressDialog.dismiss();
                                        startActivity(new Intent(LogInActivity.this, MainUserActivity.class));

                                        finish();
                                    } else {

                                        progressDialog.dismiss();
                                        startActivity(new Intent(LogInActivity.this, MainSellerActivity.class));
                                        finish();
                                    }*//*
                            }
                        }
                    }


                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(LogInActivity.this, "Error DB", Toast.LENGTH_SHORT).show();

                    }
                });


    }*/

   /* private void checkUserType() {
        //seller or ordinary user
        //DatabaseReference reference = FirebaseDatabase.getInstance("https://shopping-29b18-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Users");
        databaseReference.orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (Objects.equals(snapshot.child("accountType").getValue(String.class), "Seller")) {
                                progressDialog.dismiss();
                                startActivity(new Intent(LogInActivity.this, MainSellerActivity.class));
                                finish();
                            } else if(Objects.equals(snapshot.child("accountType").getValue(String.class), "true")){

                                progressDialog.dismiss();
                                startActivity(new Intent(LogInActivity.this, MainUserActivity.class));
                                finish();
                            }
                        }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(LogInActivity.this, "Error DB", Toast.LENGTH_SHORT).show();
                    }
                });


    }*/

    private void checkUserType() {
        DatabaseReference userRef = databaseReference.child(firebaseAuth.getUid()); // Get the reference to the user's data
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String accountType = dataSnapshot.child("accountType").getValue(String.class);
                    if (accountType != null && accountType.equals("Seller")) {
                        progressDialog.dismiss();
                        Toast.makeText(LogInActivity.this, "Seller account", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(LogInActivity.this, MainSellerActivity.class));
                        finish();
                    } else if (accountType != null && accountType.equals("User")) {
                        progressDialog.dismiss();
                        Toast.makeText(LogInActivity.this, "User account", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(LogInActivity.this, MainUserActivity.class));
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