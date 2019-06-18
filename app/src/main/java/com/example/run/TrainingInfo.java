package com.example.run;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.TextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class TrainingInfo extends AppCompatActivity {
    private static final String TAG = "TrainingInfo";
    private Context mContext = TrainingInfo.this;
    private String userID;
    private TextView textview_today_count, textview_today_distance, textview_today_time, textview_today_speed;
    private TextView textview_total_count, textview_total_distance, textview_total_time, textview_total_speed;
    private int loginDate, currentDate;
    private int sec = 0, min = 0;
    private DatabaseReference mDatabaseReference;
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.training_info);
        setupBottomNavigationView();

        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference();
        FirebaseUser user = mAuth.getCurrentUser();
        userID = user.getUid();

        textview_today_count = findViewById(R.id.textview_today_count);
        textview_today_distance = findViewById(R.id.textview_today_distance);
        textview_today_time = findViewById(R.id.textview_today_time);
        textview_today_speed = findViewById(R.id.textview_today_speed);

        textview_total_count = findViewById(R.id.textview_total_count);
        textview_total_distance = findViewById(R.id.textview_total_distance);
        textview_total_time = findViewById(R.id.textview_total_time);
        textview_total_speed = findViewById(R.id.textview_total_speed);

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };

        determineifitisToday();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.menu_navigation,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
        final FirebaseUser user = mAuth.getCurrentUser();

        mDatabaseReference.child(user.getUid()).child("RunToday").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                RunToday runToday = dataSnapshot.getValue(RunToday.class);
                if (textview_today_count != null)
                    textview_today_count.setText(Integer.toString(runToday.getCount()));
                if (textview_today_distance != null)
                    textview_today_distance.setText(String.format("%.2f",runToday.getDistance()));
                if (textview_today_speed != null)
                    textview_today_speed.setText(String.format("%.2f",runToday.getSpeed()));

                sec = runToday.getTime() % 60;
                min = runToday.getTime() / 60;
                String s = "";
                if (min < 10) s = "0" + min;
                else s = "" + min;

                if (sec < 10)  s = s + ":0" + sec;
                else s = s + ":" + sec;

                textview_today_time.setText(s);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

        mDatabaseReference.child(user.getUid()).child("RunTotal").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                RunTotal runTotal = dataSnapshot.getValue(RunTotal.class);
                if (textview_total_count != null)
                    textview_total_count.setText(Integer.toString(runTotal.getTotalcount()));
                if (textview_total_distance != null)
                    textview_total_distance.setText(String.format("%.2f", runTotal.getTotaldistance()));
                if (textview_total_speed != null)
                    textview_total_speed.setText(String.format("%.2f", runTotal.getTotalspeed()));

                sec = runTotal.getTotaltime() % 60;
                min = runTotal.getTotaltime() / 60;
                String s = "";
                if (min < 10) s = "0" + min;
                else s = "" + min;

                if (sec < 10) s = s + ":0" + sec;
                else s = s + ":" + sec;

                textview_total_time.setText(s);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    private void determineifitisToday(){
        Calendar mCalendar = new GregorianCalendar();
        loginDate = mCalendar.get(Calendar.DAY_OF_MONTH);

        final FirebaseUser user = mAuth.getCurrentUser();
        mDatabaseReference.child(user.getUid()).child("LoginTime").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                LoginTime mLoginTime = dataSnapshot.getValue(LoginTime.class);
                if(mLoginTime == null)
                {
                    mLoginTime = new LoginTime();
                    mLoginTime.setLoginDate(loginDate);
                    mDatabaseReference.child(user.getUid()).child("LoginTime").setValue(mLoginTime);
                }
                currentDate = mLoginTime.getLoginDate();
                if(loginDate != currentDate){
                    int time = 0;
                    float distance = 0;
                    int count = 0;
                    float speed = 0;
                    RunToday runToday = new RunToday(time, distance, count, speed);
                    mDatabaseReference.child(user.getUid()).child("RunToday").setValue(runToday);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });


    }

    public void setupBottomNavigationView(){
        Log.d(TAG,"setupBottomNavigationView:setting up BottomNavigationView");
        BottomNavigationViewEx bottomNavigationViewEx = findViewById(R.id.bottomNavigationViewBar);
        /*if(bottomNavigationViewEx == null) Log.d(TAG,"null");
        else Log.d(TAG,"not null");
        */
        BottomNavigationViewSetting.setupBottomNavigationView(bottomNavigationViewEx, 0);
        BottomNavigationViewSetting.enableNavigation(mContext,this,bottomNavigationViewEx);
    }

}