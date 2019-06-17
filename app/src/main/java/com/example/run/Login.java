package com.example.run;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;


public class Login extends AppCompatActivity {
    private static final String TAG = "Login";
    private Context mContext= Login.this;
    private TextInputLayout accountLayout;
    private TextInputLayout passwordLayout;
    private EditText accountEdit;
    private EditText passwordEdit;
    private Button loginButton;
    private TextView signup;
    private FirebaseAuth mAuth;

   @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        initView();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Intent intent = new Intent();
        intent.setClass(Login.this, TrainingInfo.class);
        startActivity(intent);
        finish();
    }

    private void initView() {
        mAuth = FirebaseAuth.getInstance();
        accountEdit = findViewById(R.id.account_edit);
        passwordEdit = findViewById(R.id.password_edit);
        accountLayout = findViewById(R.id.account_layout);
        accountLayout.setErrorEnabled(true);
        passwordLayout = findViewById(R.id.password_layout);
        passwordLayout.setErrorEnabled(true);

        loginButton = findViewById(R.id.login_button);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String account  = accountEdit.getText().toString();
                String password = passwordEdit.getText().toString();
                if(TextUtils.isEmpty(account)){
                    accountLayout.setError("Enter your account here:");
                    return;
                }
                if(TextUtils.isEmpty(password)){
                    passwordLayout.setError("Enter your password here");
                    return;
                }

                accountLayout.setError("");
                passwordLayout.setError("");
                mAuth.signInWithEmailAndPassword(account, password)
                        .addOnCompleteListener(Login.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(Login.this, "Login success!", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent();
                                    intent.setClass(Login.this, TrainingInfo.class);
                                    startActivity(intent);
                                    overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
                                    finish();
                                } else {
                                    Toast.makeText(Login.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

            }
        });

        signup = findViewById(R.id.link_signup);
        signup.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(mContext, SignUp.class);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
            }
        });
    }
}
