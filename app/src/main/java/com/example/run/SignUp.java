package com.example.run;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class SignUp extends AppCompatActivity{
    private static final String TAG = "SignUp";
    private TextInputLayout mailLayout;
    private TextInputLayout passwordLayout;
    private EditText mailEdit;
    private EditText passwordEdit;
    private Button signUpButton;
    private int loginDate, loginMonth, loginYear;
    private DatabaseReference mDataBaseReference;
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);
        initView();
    }

    private void initView() {
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDataBaseReference = mFirebaseDatabase.getReference();

        // Read from the database
        mDataBaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange: Added information to database: \n" + dataSnapshot.getValue());
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });


        mailEdit = findViewById(R.id.mail_edit);
        passwordEdit = findViewById(R.id.password_edit);
        mailLayout = findViewById(R.id.mail_layout);
        mailLayout.setErrorEnabled(true);
        passwordLayout = findViewById(R.id.password_layout);
        passwordLayout.setErrorEnabled(true);

        signUpButton = findViewById(R.id.signup_button);
        signUpButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String email = mailEdit.getText().toString();
                String password = passwordEdit.getText().toString();

                if (TextUtils.isEmpty(email)) {
                    mailLayout.setError("Please enter your email(account):");
                    passwordLayout.setError("");
                    return;
                }
                if (TextUtils.isEmpty(password)) {
                    mailLayout.setError("");
                    passwordLayout.setError("Please enter your password:");
                    return;
                }

                mailLayout.setError("");
                passwordLayout.setError("");
                mAuth.createUserWithEmailAndPassword(email,password)
                        .addOnCompleteListener(SignUp.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful()){
                                    initializeTotalInfo();
                                    initializeTodayInfo();
                                    initializeLoginTime();
                                    Toast.makeText(SignUp.this,"Registration successful!",Toast.LENGTH_SHORT).show();

                                    Intent intent=new Intent();
                                    intent.setClass(SignUp.this, com.example.run.Login.class);
                                    startActivity(intent);
                                    finish();
                                }else {
                                    Toast.makeText(SignUp.this,task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
    }

    public void initializeTodayInfo(){
        int time = 0;
        float distance = 0;
        int count = 0;
        float speed = 0;
        RunTotal runTotal = new RunTotal(time,distance,count,speed);

        FirebaseUser user = mAuth.getCurrentUser();
        mDataBaseReference.child(user.getUid()).child("RunToday").setValue(runTotal);
    }

    public void initializeTotalInfo(){
        int totalTime = 0;
        float totalDistance = 0;
        int totalCount = 0;
        float totalSpeed = 0;
        RunTotal runTotal = new RunTotal(totalTime,totalDistance,totalCount,totalSpeed);

        FirebaseUser user = mAuth.getCurrentUser();
        mDataBaseReference.child(user.getUid()).child("RunTotal").setValue(runTotal);
    }

    private void initializeLoginTime(){
        loginDate = 0;
        loginMonth = 0;
        loginYear = 0;
        LoginTime mLoginTime = new LoginTime(loginDate,loginMonth,loginYear);

        FirebaseUser user = mAuth.getCurrentUser();
        mDataBaseReference.child(user.getUid()).child("LoginTime").setValue(mLoginTime);
    }

}
