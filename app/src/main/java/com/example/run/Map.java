package com.example.run;

import android.Manifest;
import android.animation.ValueAnimator;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Timer;
import java.util.TimerTask;
import static com.example.run.R.id.map;


public class Map extends AppCompatActivity implements OnMapReadyCallback,
        GoogleMap.OnInfoWindowClickListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener  {
    private static final String TAG = "";
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;
    private static final int REQUEST_LOCATION = 0;
    private static final long INTERVAL = 1000 * 5;
    private static final long FASTEST_INTERVAL = 1000 * 5;
    private static final float SMALLEST_DISPLACEMENT = 0.25F;
    private Location mLastLocation;
    private LocationRequest mLocationRequest;
    private boolean mRequestingLocationUpdates = false;
    private Context mContext = Map.this;
    private int markerCount;
    private ArrayList<LatLng> points;
    private Button startButton, pauseButton,resumeButton,finishButton;
    boolean isClick;
    private TextView distance, timer;
    private float TodayDistance, TotalDistance, Totaldistance, TodaySpeed, TotalSpeed;
    private int TodayCount,TotalCount,Totalcount;
    private int loginDate,loginMonth,loginYear;
    private int TodayTime,TotalTime;
    private int times;
    private int time = 0, sec = 0, min = 0;
    private boolean startflag = false;
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference myRef;
    private GoogleApiClient mGoogleApiClient;
    private GoogleMap mMap;
    Polyline line;
    private FragmentManager fm;
    private FusedLocationProviderClient mfusedLocationProviderClient;
    private LocationCallback mlocationCallback;

    public Map() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.current_run);

        //firebase
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        FirebaseUser user = mAuth.getCurrentUser();

        setupBottomNavigationView();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(map);
        if (mapFragment != null) mapFragment.getMapAsync(this);
        else{
            mapFragment = SupportMapFragment.newInstance();
            fm = getSupportFragmentManager();
            fm.beginTransaction().replace(R.id.map, mapFragment).commit();
        }

        distance = findViewById(R.id.distance);
        timer = findViewById(R.id.timer);
        markerCount = 0;
        points = new ArrayList<LatLng>();

        if (getServicesAvailable()) {
            buildGoogleApiClient();
            createLocationRequest();
        }

        startButton  = findViewById(R.id.startButton);
        pauseButton  = findViewById(R.id.pauseButton);
        resumeButton = findViewById(R.id.resumeButton);
        finishButton = findViewById(R.id.finishButton);

        Timer timer1 = new Timer();
        timer1.schedule(task, 0, 1000);

        startButton.setOnClickListener(listener);
        pauseButton.setOnClickListener(listener);
        resumeButton.setOnClickListener(listener);
        finishButton.setOnClickListener(listener);

        Calendar mCalendar = new GregorianCalendar();
        loginDate = mCalendar.get(Calendar.DAY_OF_MONTH);
        loginMonth = mCalendar.get(Calendar.MONTH);
        loginYear = mCalendar.get(Calendar.YEAR);

        LoginTime mLoginTime = new LoginTime(loginDate,loginMonth,loginYear);
        myRef.child(user.getUid()).child("LoginTime").setValue(mLoginTime);
    }

    public void setupBottomNavigationView() {
        Log.d(TAG, "setupBottomNavigationView:setting up BottomNavigationView");
        BottomNavigationViewEx bottomNavigationViewEx = findViewById(R.id.bottomNavigationViewBar);
        BottomNavigationViewSetting.setupBottomNavigationView(bottomNavigationViewEx, 1);
        BottomNavigationViewSetting.enableNavigation(mContext, this, bottomNavigationViewEx);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                (this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
    }


    // Add A Map Pointer To The MAp
    public void addMarker(GoogleMap googleMap, double lat, double lon) {
        Marker mk = null;
        if (markerCount == 1) animateMarker(mLastLocation, mk);
        else if (markerCount == 0) {
            mMap = googleMap;
            LatLng latlong = new LatLng(lat, lon);

            if (mMap != null) {
                mk = mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lon)));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlong, 16));
            }

            markerCount = 1;
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                return;
            }
            startLocationUpdates();
        }
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        Toast.makeText(this, marker.getTitle(), Toast.LENGTH_LONG).show();
    }

    public boolean getServicesAvailable() {
        GoogleApiAvailability api = GoogleApiAvailability.getInstance();
        int isAvailable = api.isGooglePlayServicesAvailable(this);

        if (isAvailable == ConnectionResult.SUCCESS) return true;
        else if (api.isUserResolvableError(isAvailable)) {
            Dialog dialog = api.getErrorDialog(this, isAvailable, 0);
            dialog.show();
        }
        else Toast.makeText(this, "Cannot Connect To Play Services", Toast.LENGTH_SHORT).show();
        return false;
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) mGoogleApiClient.connect();

        final FirebaseUser user = mAuth.getCurrentUser();
        myRef.child(user.getUid()).child("RunToday").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                RunToday runToday = dataSnapshot.getValue(RunToday.class);
                TodayCount = runToday.getCount();
                TodayDistance = runToday.getDistance();
                TodayTime = runToday.getTime();
                TodaySpeed = runToday.getSpeed();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

        myRef.child(user.getUid()).child("RunTotal").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                RunTotal runTotal =dataSnapshot.getValue(RunTotal.class);
                TotalCount= runTotal.getTotalcount();
                TotalDistance = runTotal.getTotaldistance();
                TotalTime= runTotal.getTotaltime();
                TotalSpeed= runTotal.getTotalspeed();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        getServicesAvailable();

        if (mGoogleApiClient.isConnected() && mRequestingLocationUpdates)
            startLocationUpdates();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected())
            mGoogleApiClient.disconnect();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mGoogleApiClient.disconnect();
        super.onDestroy();
    }


    private void displayLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        else {
            mfusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if(location != null) mLastLocation = location;
                    else{
                        //Toast.makeText(Map.this, "displayLocation(): location == null.", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            if (mLastLocation != null) {
                double latitude = mLastLocation.getLatitude();
                double longitude = mLastLocation.getLongitude();
                //String loca = "" + latitude + " ," + longitude + " ";
                //Toast.makeText(this,loca, Toast.LENGTH_SHORT).show();
                //Toast.makeText(Map.this, "displayLocation(): mLastLocation != null.", Toast.LENGTH_SHORT).show();
                addMarker(mMap, latitude, longitude);
            }
            else {
                //Toast.makeText(this, "Couldn't get the location.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mfusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
        mlocationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                mLastLocation = locationResult.getLastLocation();
                onLocationChanged(mLastLocation);
            }
        };
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setSmallestDisplacement(SMALLEST_DISPLACEMENT);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    protected void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                (this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        else {
            mfusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mlocationCallback, Looper.myLooper());
        }
    }

    protected void stopLocationUpdates() {
        mfusedLocationProviderClient.removeLocationUpdates(mlocationCallback);
    }


    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }

    @Override
    public void onConnected(Bundle arg0) {
        displayLocation();
        if (mRequestingLocationUpdates) startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        mGoogleApiClient.connect();
    }

    public void onLocationChanged(final Location location) {
        mLastLocation = location;
        final LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        //Toast.makeText(getApplicationContext(), "Location changed!", Toast.LENGTH_SHORT).show();

        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));
        displayLocation();
        if (isClick == true) {
            points.add(latLng);
            drawLine();
            distance.setText(String.format("%.2f", calculateMiles()));
        }
    }


    private void drawLine() {
        PolylineOptions options = new PolylineOptions().width(10).color(Color.RED).geodesic(true);
        for (int i = 0; i < points.size(); i++) {
            LatLng point = points.get(i);
            options.add(point);
        }
        line = mMap.addPolyline(options); //add Polyline
    }

    protected float calculateMiles() {
        float totalDistance = 0;
        for (int i = 1; i < line.getPoints().size(); i++) {
            Location curLocation = new Location("this");
            curLocation.setLatitude(line.getPoints().get(i).latitude);
            curLocation.setLongitude(line.getPoints().get(i).longitude);

            Location lastLocation = new Location("this");
            lastLocation.setLatitude(line.getPoints().get(i - 1).latitude);
            lastLocation.setLongitude(line.getPoints().get(i - 1).longitude);

            totalDistance += lastLocation.distanceTo(curLocation);
        }
        return totalDistance / 1000;
    }

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    sec = time % 60;
                    min = time / 60;
                    String s = "";

                    if (min < 10) s = "0" + min;
                    else s = "" + min;

                    if (sec < 10) s = s + ":0" + sec;
                    else s = s + ":" + sec;

                    timer.setText(s);
                    break;
            }
        }
    };

    private TimerTask task = new TimerTask() {
        @Override
        public void run() {
            if (startflag) {
                time++;
                Message message = new Message();
                message.what = 1;
                handler.sendMessage(message);
            }
        }
    };

    public static void animateMarker(final Location destination, final Marker marker) {
        if (marker != null) {
            final LatLng startPosition = marker.getPosition();
            final LatLng endPosition = new LatLng(destination.getLatitude(), destination.getLongitude());
            final float startRotation = marker.getRotation();
            final LatLngInterpolator latLngInterpolator = new LatLngInterpolator.LinearFixed();

            ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
            valueAnimator.setDuration(1000); // 1 second
            valueAnimator.setInterpolator(new LinearInterpolator());
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    try {
                        float v = animation.getAnimatedFraction();
                        LatLng newPosition = latLngInterpolator.interpolate(v, startPosition, endPosition);
                        marker.setPosition(newPosition);
                        marker.setRotation(computeRotation(v, startRotation, destination.getBearing()));
                    } catch (Exception ex) {}
                }
            });
            valueAnimator.start();
        }
    }

    private static float computeRotation(float fraction, float start, float end) {
        float normalizeEnd = end - start; // rotate start to 0
        float normalizedEndAbs = (normalizeEnd + 360) % 360;
        float direction = (normalizedEndAbs > 180) ? -1 : 1; // -1 = anticlockwise, 1 = clockwise
        float rotation;

        if (direction > 0) rotation = normalizedEndAbs;
        else rotation = normalizedEndAbs - 360;

        float result = fraction * rotation + start;
        return (result + 360) % 360;
    }

    private interface LatLngInterpolator {
        LatLng interpolate(float fraction, LatLng a, LatLng b);

        class LinearFixed implements LatLngInterpolator {
            @Override
            public LatLng interpolate(float fraction, LatLng a, LatLng b) {
                double lat = (b.latitude - a.latitude) * fraction + a.latitude;
                double lngDelta = b.longitude - a.longitude;

                if (Math.abs(lngDelta) > 180) {
                    lngDelta -= Math.signum(lngDelta) * 360;
                }
                double lng = lngDelta * fraction + a.longitude;
                return new LatLng(lat, lng);
            }
        }
    }

    private View.OnClickListener listener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.startButton:
                    pauseButton.setVisibility(View.VISIBLE);
                    startButton.setVisibility(View.INVISIBLE);
                    displayLocation();
                    startflag = true;
                    isClick = true;
                    Toast.makeText(mContext,"Let's get started!",Toast.LENGTH_SHORT).show();
                    break;

                case R.id.pauseButton:
                    resumeButton.setVisibility(View.VISIBLE);
                    finishButton.setVisibility(View.VISIBLE);
                    pauseButton.setVisibility(View.INVISIBLE);
                    startflag = false;
                    isClick = false;
                    break;

                case R.id.resumeButton:
                    resumeButton.setVisibility(View.INVISIBLE);
                    finishButton.setVisibility(View.INVISIBLE);
                    pauseButton.setVisibility(View.VISIBLE);
                    startflag = true;
                    isClick = true;
                    break;

                case R.id.finishButton:
                    resumeButton.setVisibility(View.INVISIBLE);
                    finishButton.setVisibility(View.INVISIBLE);
                    startButton.setVisibility(View.VISIBLE);
                    isClick=false;
                    startflag=false;
                    times++;
                    saveTodayInfo();
                    saveTotalInfo();
                    Toast.makeText(mContext,"Running info saved.",Toast.LENGTH_SHORT).show();
                    time=0;
                    points.clear();
                    distance.setText("0.00");
                    timer.setText("00:00");
                    mMap.clear();
            }
        }
    };

    private void saveTodayInfo(){
        float distance = calculateMiles();
        distance += TodayDistance;
        int curtime = TodayTime + time;
        TodayCount += 1;
        TodaySpeed = TodayDistance / curtime;
        Toast.makeText(this, "" + distance + " " + curtime + " " + TodaySpeed + " " + TodayTime + " " + time  ,Toast.LENGTH_SHORT).show();
        Log.d("hahahahahhahahaha", "" + distance + " " + curtime + " " + TodaySpeed + " " + TodayTime + " " + time);


        RunToday runToday = new RunToday(curtime, distance, TodayCount, TodaySpeed);

        FirebaseUser user = mAuth.getCurrentUser();
        myRef.child(user.getUid()).child("RunToday").setValue(runToday);
    }

    private void saveTotalInfo(){
        float distance = calculateMiles();
        Totaldistance = TotalDistance + distance;
        int totaltime = TotalTime + time;
        Totalcount = TotalCount + 1;
        TotalSpeed = Totaldistance / totaltime;

        RunTotal runTotal = new RunTotal(totaltime, Totaldistance, Totalcount, TotalSpeed);

        FirebaseUser user = mAuth.getCurrentUser();
        myRef.child(user.getUid()).child("RunTotal").setValue(runTotal);
    }

}

