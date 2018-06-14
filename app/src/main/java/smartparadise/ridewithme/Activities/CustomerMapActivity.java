package smartparadise.ridewithme.Activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Camera;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;

import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import smartparadise.ridewithme.R;

public class CustomerMapActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener,RoutingListener {

    private GoogleMap mMap;

    GoogleApiClient mGoogleApiClient;

    Location mLastLocaion;

    LocationRequest mLocationRequest;

    private Button pickupRequest;

    private LatLng pickLatLng,destinationLatLng;

    private boolean requestBol=false;
    private boolean anyDriverOn=false;

    private Marker pickupMarker,destinationMarker;

    String destination="";
    SupportMapFragment mapFragment;

    String name;
    RatingBar driverRating;
    ProgressDialog progressDialog;
    LinearLayout driverInfoLayout;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    PlaceAutocompleteFragment autocompleteFragment;

    CircleImageView driverProfileImage;
    TextView driverName,driverPhone,driverCar,driverCarNo;
    TextView navUserName;
    View view;
    ImageView navUserImage;
    Toolbar toolbar;

    boolean linesEnabled=false;

    private List<Polyline> polylines;
    private static final int[] COLORS = new int[]{R.color.colorPrimaryDark};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_map);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(CustomerMapActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},LOCATION_REQUEST_CODE);
        }else{
            mapFragment.getMapAsync(this);

        }
        polylines=new ArrayList<>();

        driverInfoLayout=(LinearLayout)findViewById(R.id.driverInfoLayout);

        driverName=(TextView)findViewById(R.id.driverNameInfo);
        driverPhone=(TextView)findViewById(R.id.driverPhoneInfo);
        driverCar=(TextView)findViewById(R.id.driverCar);
        driverCarNo=(TextView)findViewById(R.id.driverCarNo);

        driverProfileImage=(CircleImageView)findViewById(R.id.driverProfileImage);


        pickupRequest=(Button)findViewById(R.id.pickRequest);

        destinationLatLng=new LatLng(0.0,0.0);

        driverRating=(RatingBar)findViewById(R.id.driverTotalRating);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        view=navigationView.getHeaderView(0);
        navUserImage=(ImageView)view.findViewById(R.id.userNavImage);
        navUserName=(TextView)view.findViewById(R.id.userNavName);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId()){
                    case R.id.nav_profile:
                        startActivity(new Intent(CustomerMapActivity.this,CustomerProfileActivity.class));
                        break;

                    case R.id.nav_history:
                        Intent intent=new Intent(CustomerMapActivity.this,HistoryActivity.class);
                        intent.putExtra("Character","Riders");
                        startActivity(intent);
                        break;

                    case R.id.nav_logout:
                        FirebaseAuth.getInstance().signOut();
                        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE);
                        sharedPreferences.edit().clear().apply();
                        startActivity(new Intent(CustomerMapActivity.this,MainActivity.class));
                        finish();
                        break;
                }
                drawerLayout.closeDrawers();
                return false;
                }


        });


         autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                destination=place.getName().toString();
                destinationLatLng=place.getLatLng();
               getRoutToMarker(destinationLatLng);

            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                 }
        });

        pickupRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(destination!=""){

                    if(requestBol){
                        pickupRequest.setText("Pick Me Up");
                        endRide();
                    }else{
                        progressDialog=new ProgressDialog(CustomerMapActivity.this);
                        progressDialog.setMessage("Looking for the driver...");
                        progressDialog.setTitle("RideWithMe");
                        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                        progressDialog.show();
                        try{
                            DatabaseReference driverLocation=FirebaseDatabase.getInstance().getReference().child("DriverAvailable");
                            driverLocation.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.exists()){
                                        anyDriverOn=true;
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                        }catch (Exception e){
                            Log.e("AvailablityError",e.toString());
                            progressDialog.dismiss();
                        }
                        if(anyDriverOn){
                            pickupRequest.setText("Cancel Request");

                            requestBol=true;
                            String userId=FirebaseAuth.getInstance().getCurrentUser().getUid();
                            DatabaseReference database=FirebaseDatabase.getInstance().getReference("CustomerRequest");
                            GeoFire geoFire=new GeoFire(database);
                            geoFire.setLocation(userId, new GeoLocation(mLastLocaion.getLatitude(), mLastLocaion.getLongitude()),new GeoFire.CompletionListener() {
                                @Override
                                public void onComplete(String key, DatabaseError error) {
                                    Log.e("error",""+error);
                                }
                            });
                            pickLatLng=new LatLng(mLastLocaion.getLatitude(),mLastLocaion.getLongitude());
                            pickupMarker=mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.pickupmark)).position(pickLatLng).title("PickUp Here!"));
                            getClosestDriver();
                        }else{
                            progressDialog.dismiss();
                            Toast.makeText(CustomerMapActivity.this, "No Driver Available at this moment.", Toast.LENGTH_SHORT).show();
                        }


                    }

                }else{
                    Toast.makeText(CustomerMapActivity.this, "Choose Destination First!", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        DatabaseReference customerDatabase=FirebaseDatabase.getInstance().getReference().child("Users").child("Riders").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        customerDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    Map<String,Object> map=(Map<String,Object>)dataSnapshot.getValue();
                    if(map.get("first_name")!=null&&map.get("last_name")!=null){
                        name=map.get("first_name").toString()+" "+map.get("last_name").toString();
                        navUserName.setText(name);
                    }
                    if(map.get("ProfileImage")!=null){
                        Glide.with(getApplicationContext()).load(map.get("ProfileImage").toString()).into(navUserImage);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }



    private int radius=1;
    private boolean driverFound=false;
    String driverFoundId;
    GeoQuery geoQuery;
    private void getClosestDriver(){
        DatabaseReference driverLocation=FirebaseDatabase.getInstance().getReference().child("DriverAvailable");
        GeoFire geoFire=new GeoFire(driverLocation);
        geoQuery=geoFire.queryAtLocation(new GeoLocation(pickLatLng.latitude,pickLatLng.longitude),radius);
        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {

            @Override
            public void onKeyEntered(String key, GeoLocation location) {

                if(!driverFound&&requestBol){
                    driverFound=true;
                    driverFoundId=key;
                    DatabaseReference driverRef=FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverFoundId).child("CustomerRequest");
                    String customerId=FirebaseAuth.getInstance().getCurrentUser().getUid();
                    HashMap map=new HashMap();
                    map.put("CustomerRideId",customerId);
                    map.put("destinationLat",destinationLatLng.latitude);
                    map.put("destinationLng",destinationLatLng.longitude);
                    map.put("destination",destination);
                    driverRef.updateChildren(map);
                    progressDialog.setMessage("Getting Driver's Location..");

                    getDriverLocation();
                    getDriverInfo();
                    hasRideEnded();
                }
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                if(!driverFound){
                    radius++;
                    getClosestDriver();
                }

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }



    private Marker mDriverMarker;
    private DatabaseReference driverLocationRef;
    ValueEventListener driverLocationRefListener;
    private void getDriverLocation(){

        driverLocationRef=FirebaseDatabase.getInstance().getReference().child("DriverWorking").child(driverFoundId).child("l");


        driverLocationRefListener=driverLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()&&requestBol){
                    List<Object> map=(List<Object>) dataSnapshot.getValue();
                    double locationLat=0;
                    double locationLong=0;

                    if(map.get(0)!=null){
                        locationLat=Double.parseDouble(map.get(0).toString());

                    }

                    if(map.get(1)!=null){
                        locationLong=Double.parseDouble(map.get(1).toString());
                    }

                    LatLng driverLatLng=new LatLng(locationLat,locationLong);
                    if(mDriverMarker!=null){
                        mDriverMarker.remove();
                    }
                    Location loc1=new Location("");
                    loc1.setLatitude(pickLatLng.latitude);
                    loc1.setLongitude(pickLatLng.longitude);

                    Location loc2=new Location("");
                    loc1.setLatitude(driverLatLng.latitude);
                    loc1.setLongitude(driverLatLng.longitude);

                    float distance=loc1.distanceTo(loc2);

                    if(distance<100){
                        progressDialog.setMessage("Driver is Here!");


                    }else{
                        progressDialog.setMessage("Driver Found");
                      //  pickupRequest.setText("Driver Found "+String.valueOf(distance));

                    }

                    mDriverMarker=mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.cabicon)).position(driverLatLng).title("Your Driver"));

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    String driverAssignedName;
    private void getDriverInfo() {
        progressDialog.dismiss();
        driverInfoLayout.setVisibility(View.VISIBLE);
        DatabaseReference customerInfoRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverFoundId);

        customerInfoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();

                    if (map.get("first_name") != null&&map.get("last_name")!=null) {
                        driverAssignedName=map.get("first_name").toString()+" "+map.get("last_name").toString();
                        driverName.setText(driverAssignedName);
                    }
                    if (map.get("phone") != null) {
                        driverPhone.setText(map.get("phone").toString());
                    }

                    if (map.get("vehical_model") != null) {
                        driverCar.setText(map.get("vehical_model").toString());
                    }
                    if(map.get("vehical_reg_num")!=null){
                        driverCarNo.setText(map.get("vehical_reg_num").toString());
                    }
                    if (map.get("ProfileImage") != null) {
                        Glide.with(getApplication()).load(map.get("ProfileImage").toString()).into(driverProfileImage);
                    }
                    int ratingSum=0;
                    float ratingTotal=0;
                    float ratingAvg=0;
                    for(DataSnapshot child:dataSnapshot.child("rating").getChildren()){
                        ratingSum=ratingSum+Integer.valueOf(child.getValue().toString());
                        ratingTotal++;
                    }
                    if(ratingTotal!=0){
                        ratingAvg=ratingSum/ratingTotal;
                        driverRating.setRating(ratingAvg);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    DatabaseReference driveHasEndedRef;
    ValueEventListener driveHasEndedRefListener;
    private void hasRideEnded() {
        driveHasEndedRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverFoundId).child("CustomerRequest").child("CustomerRideId");

        driveHasEndedRefListener=driveHasEndedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                } else {
                        endRide();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void endRide(){
        requestBol=false;

        erasePolyLines();
        geoQuery.removeAllListeners();
        try{
            driverLocationRef.removeEventListener(driverLocationRefListener);
            driveHasEndedRef.removeEventListener(driverLocationRefListener);
        }catch (Exception e){
            Log.e("Exception",e.toString());
        }
        if(driverFoundId!=null){
            DatabaseReference driverRef=FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverFoundId).child("CustomerRequest");
            driverRef.removeValue();
            driverFoundId=null;

        }

        driverFound=false;
        radius=1;

        String userId=FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference database=FirebaseDatabase.getInstance().getReference("CustomerRequest");
        GeoFire geoFire=new GeoFire(database);
        geoFire.removeLocation(userId);
        if(pickupMarker!=null){
            pickupMarker.remove();
        }
        if(mDriverMarker!=null){
            mDriverMarker.remove();
        }
        if(destinationMarker!=null){
            destinationMarker.remove();
        }
        pickupRequest.setText("Pick Me Up!!");
        driverInfoLayout.setVisibility(View.GONE);
        if(driverName!=null&&driverPhone!=null&&driverCar!=null&&driverProfileImage!=null){
            driverName.setText("");
            driverPhone.setText("");
            driverCar.setText("");
            driverProfileImage.setImageResource(R.drawable.userimage);

        }


    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(CustomerMapActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},LOCATION_REQUEST_CODE);
            }
        }
        buildGoogleApi();
        mMap.setMyLocationEnabled(true);
    }

    protected synchronized void buildGoogleApi() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();

    }

    @Override
    public void onLocationChanged(Location location) {
        if(getApplicationContext()!=null){
            mLastLocaion = location;
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(16));

        }
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        mLocationRequest.setSmallestDisplacement(50);
        mLocationRequest.setFastestInterval(10000);

        mLocationRequest.setInterval(60000);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
           ActivityCompat.requestPermissions(CustomerMapActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},LOCATION_REQUEST_CODE);
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    final int LOCATION_REQUEST_CODE=1;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case LOCATION_REQUEST_CODE:
                if(grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    mapFragment.getMapAsync(this);
                }else{
                    Toast.makeText(getApplicationContext(), "Please Provide Location Permissions!", Toast.LENGTH_SHORT).show();
                }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

    }



    @Override
    public void onRoutingFailure(RouteException e) {
        if(e != null) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }else {
            Toast.makeText(this, "Something went wrong, Try again", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRoutingStart() {

    }


    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {
        linesEnabled=true;
        LatLngBounds.Builder builder=new LatLngBounds.Builder()
                .include(new LatLng(mLastLocaion.getLatitude(),mLastLocaion.getLongitude()))
                .include(destinationLatLng);
        LatLngBounds bounds=builder.build();

        int width=getResources().getDisplayMetrics().widthPixels;
        int padding=(int)(width*0.2);
        CameraUpdate cameraUpdate= CameraUpdateFactory.newLatLngBounds(bounds,padding);
        mMap.animateCamera(cameraUpdate);
        destinationMarker=mMap.addMarker(new MarkerOptions().position(destinationLatLng).title(destination));
        if(polylines.size()>0) {
            for (Polyline poly : polylines) {
                poly.remove();
            }
        }

        polylines = new ArrayList<>();
        //add route(s) to the map.
        for (int i = 0; i <route.size(); i++) {

            //In case of more than 5 alternative routes
            int colorIndex = i % COLORS.length;

            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(getResources().getColor(COLORS[colorIndex]));
            polyOptions.width(10 + i * 3);
            polyOptions.addAll(route.get(i).getPoints());
            Polyline polyline = mMap.addPolyline(polyOptions);
            polylines.add(polyline);

            Toast.makeText(getApplicationContext(),"Route "+ (i+1) +": distance - "+ route.get(i).getDistanceValue()+": duration - "+ route.get(i).getDurationValue(),Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRoutingCancelled() {

    }

    private void getRoutToMarker(LatLng latLng) {
        Routing routing=new Routing.Builder()
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .alternativeRoutes(false)
                .waypoints(new LatLng(mLastLocaion.getLatitude(),mLastLocaion.getLongitude()),latLng)
                .build();
        routing.execute();

    }
    private void erasePolyLines(){
        for(Polyline line:polylines){
            line.remove();
        }
        polylines.clear();
    }

    @Override
    public void onBackPressed() {
        if(linesEnabled){
            linesEnabled=false;
            erasePolyLines();
            autocompleteFragment.setText("");
            if(destinationMarker!=null){
                destinationMarker.remove();
            }

        }else{
            super.onBackPressed();

        }

    }
}


