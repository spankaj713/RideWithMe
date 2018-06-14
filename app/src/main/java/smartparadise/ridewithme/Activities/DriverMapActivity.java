package smartparadise.ridewithme.Activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.Image;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
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
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
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

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import smartparadise.ridewithme.R;
import smartparadise.ridewithme.Services.OnAppKilled;

import static smartparadise.ridewithme.R.id.map;

/**
 * Created by HP on 19-04-2018.
 */

public class DriverMapActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, RoutingListener
{
    @BindView(R.id.CustomerNameInfo)
    TextView customerName;
    @BindView(R.id.CustomerPhoneInfo)
    TextView customerPhone;
    private GoogleMap mMap;

    GoogleApiClient mGoogleApiClient;

    Location mLastLocaion;

    LocationRequest mLocationRequest;


    public Boolean isLoggingOut = false;

    private String customerId = "";
    String destination,name;

    private float rideDistance;

    private LatLng pickupLatLng,destinationLatLng;

    Button acceptButton;

    private int status=0;

    SupportMapFragment mapFragment;

    LinearLayout customerInfoLayout;
    DrawerLayout drawerLayout;
    NavigationView navigationView;

    CircleImageView customerProfileImage;
    TextView  customerDestination;
    TextView navUserName;
    View view;
    ImageView navUserImage;
    Switch workingSwitch;
    Toolbar toolbar;


    private List<Polyline> polylines;
    private static final int[] COLORS = new int[]{R.color.colorPrimaryDark};


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_map);
        ButterKnife.bind(this);
        startService(new Intent(DriverMapActivity.this, OnAppKilled.class));
        polylines=new ArrayList<>();
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(map);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(DriverMapActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        } else {
            mapFragment.getMapAsync(this);

        }

        customerInfoLayout = (LinearLayout) findViewById(R.id.customerInfoLayout);


        customerDestination = (TextView) findViewById(R.id.customerDesinationInfo);

        customerProfileImage = (CircleImageView) findViewById(R.id.customerProfileImage);

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        view=navigationView.getHeaderView(0);
        navUserImage=(ImageView)view.findViewById(R.id.userNavImage);
        navUserName=(TextView)view.findViewById(R.id.userNavName);
        workingSwitch=(Switch)view.findViewById(R.id.workingSwitch);
        workingSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    connectDriver();
                }else{
                    disconnectDriver();
                }
            }
        });

        acceptButton=(Button)findViewById(R.id.acceptRequestButton);
        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch(status){
                    case 1:
                        erasePolyLines();
                        status=2;
                        if(destinationLatLng.latitude!=0.0&&destinationLatLng.longitude!=0.0){
                            getRoutToMarker(destinationLatLng);
                            acceptButton.setText("Ride Completed");
                        }
                        break;
                    case 2:
                        recordRide();
                        endRide();
                        break;
                }
            }
        });

        getAssignedCustomer();

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId()) {
                    case R.id.nav_profile:
                        startActivity(new Intent(DriverMapActivity.this, DriverSettingsActivity.class));
                        break;

                    case R.id.nav_history:
                        Intent intent=new Intent(DriverMapActivity.this,HistoryActivity.class);
                        intent.putExtra("Character","Drivers");
                        startActivity(intent);
                        break;

                    case R.id.nav_logout:
                        logout();
                        break;
                }
                drawerLayout.closeDrawers();
                return false;
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        DatabaseReference driverInfoDb=FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        driverInfoDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    Map<String,Object> map=(Map<String,Object>)dataSnapshot.getValue();
                    if(map.get("first_name")!=null&&map.get("last_name")!=null){
                        name=map.get("first_name").toString()+" "+map.get("last_name").toString();
                        navUserName.setText(name);
                    }
                    if(map.get("ProfileImage")!=null){
                        Glide.with(getApplication()).load(map.get("ProfileImage").toString()).into(navUserImage);
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

    String driverId;

    private void logout() {
        isLoggingOut = true;
        disconnectDriver();

        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(DriverMapActivity.this, MainActivity.class));
        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE);
        sharedPreferences.edit().clear().apply();
        finish();

        return;
    }

    private void getAssignedCustomer() {
        driverId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference assignedCustomerRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverId).child("CustomerRequest").child("CustomerRideId");

        assignedCustomerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    status=1;
                    customerId = dataSnapshot.getValue().toString();
                    getAssignedCustomerPickupLocation();
                } else {
                    endRide();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    private DatabaseReference assignedCustomerPickupLocationRef;

    ValueEventListener assignedCustomerPickupLocationRefListener;

    Marker pickupMarker;

    private void getAssignedCustomerPickupLocation() {
        assignedCustomerPickupLocationRef = FirebaseDatabase.getInstance().getReference().child("CustomerRequest").child(customerId).child("l");
        assignedCustomerPickupLocationRefListener = assignedCustomerPickupLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && !customerId.equals("")) {
                    List<Object> map = (List<Object>) dataSnapshot.getValue();
                    double locationLat = 0;
                    double locationLong = 0;
                    if (map.get(0) != null) {
                        locationLat = Double.parseDouble(map.get(0).toString());

                    }
                    if (map.get(1) != null) {
                        locationLong = Double.parseDouble(map.get(1).toString());
                    }
                    pickupLatLng = new LatLng(locationLat, locationLong);

                    pickupMarker = mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.pickupmark)).position(pickupLatLng).title("pickup location"));
                    getRoutToMarker(pickupLatLng);
                    getAssignedCustomerInfo();

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    String assignedCustomerName;
    private void getAssignedCustomerInfo() {

        DatabaseReference customerInfoRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Riders").child(customerId);

        customerInfoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if (map.get("first_name") != null&&map.get("last_name")!=null) {
                        assignedCustomerName=map.get("first_name").toString()+" "+map.get("last_name").toString();
                        customerName.setText(assignedCustomerName);
                    } else {
                        customerName.setText("not available");
                    }
                    if (map.get("phone") != null) {
                        customerPhone.setText(map.get("phone").toString());
                    } else {
                        customerPhone.setText("not available");

                    }
                    if (map.get("ProfileImage") != null) {
                        Glide.with(getApplication()).load(map.get("ProfileImage").toString()).into(customerProfileImage);
                    } else {
                        customerProfileImage.setImageResource(R.drawable.userimage);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


                driverId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                DatabaseReference assignedCustomerRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverId).child("CustomerRequest");

                assignedCustomerRef.addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            Map<String,Object> map=(Map<String,Object>)dataSnapshot.getValue();
                            if(map.get("destination")!=null){
                                destination=map.get("destination").toString();
                                customerDestination.setText("Destination: "+destination);
                            }else{
                                customerDestination.setText("Destination: --");
                            }
                            double destinationLat=0.0;
                            double destinationLong=0.0;
                            if(map.get("destinationLat")!=null){
                                destinationLat= Double.valueOf(map.get("destinationLat").toString());
                            }
                            if(map.get("destinationLng")!=null){
                                destinationLat= Double.valueOf(map.get("destinationLng").toString());
                            }
                            destinationLatLng=new LatLng(destinationLat,destinationLong);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

        customerInfoLayout.setVisibility(View.VISIBLE);
    }

    private void endRide(){

        acceptButton.setText("Picked Customer");
        erasePolyLines();

        String userId=FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference driverRef=FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(userId).child("CustomerRequest");
        driverRef.removeValue();

        DatabaseReference database=FirebaseDatabase.getInstance().getReference("CustomerRequest");
        GeoFire geoFire=new GeoFire(database);
        geoFire.removeLocation(customerId);
        customerId="";
        rideDistance=0;
        if(pickupMarker!=null){
            pickupMarker.remove();
        }
        if (assignedCustomerPickupLocationRefListener != null) {
            assignedCustomerPickupLocationRef.removeEventListener(assignedCustomerPickupLocationRefListener);

        }
        customerInfoLayout.setVisibility(View.GONE);
        if (customerName != null && customerPhone != null && customerProfileImage != null) {
            customerName.setText("");
            customerPhone.setText("");
            customerProfileImage.setImageResource(R.drawable.userimage);

        }

    }


    private void recordRide(){
        String userId=FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(userId).child("history");
        DatabaseReference customerRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Riders").child(customerId).child("history");
        DatabaseReference historyRef=FirebaseDatabase.getInstance().getReference().child("history");
        String requestId=historyRef.push().getKey();
        driverRef.child(requestId).setValue(true);
        customerRef.child(requestId).setValue(true);

        HashMap map=new HashMap();
        map.put("driver",userId);
        map.put("rider",customerId);
        map.put("rating",0);
        map.put("time",getCurrentTime());
        map.put("destination",destination);
        map.put("location/from/lat",pickupLatLng.latitude);
        map.put("location/from/long",pickupLatLng.longitude);
        map.put("location/to/lat",destinationLatLng.latitude);
        map.put("location/to/long",destinationLatLng.longitude);
        map.put("distance",rideDistance);
        historyRef.child(requestId).updateChildren(map);

    }

    private Long getCurrentTime(){
        Long time=System.currentTimeMillis()/1000;
        return time;
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


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(DriverMapActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
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

        if (getApplicationContext() != null) {
            if(!customerId.equals("")){
                rideDistance+=mLastLocaion.distanceTo(location)/1000;
            }
            mLastLocaion = location;
            LatLng latLng = new LatLng(mLastLocaion.getLatitude(), mLastLocaion.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(16));
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference refAvailable = FirebaseDatabase.getInstance().getReference("DriverAvailable");
            DatabaseReference refWorking = FirebaseDatabase.getInstance().getReference("DriverWorking");
            GeoFire geoFireAvailable = new GeoFire(refAvailable);
            GeoFire geoFireWorking = new GeoFire(refWorking);
            switch (customerId) {
                case "":

                    geoFireWorking.removeLocation(userId);
                    geoFireAvailable.setLocation(userId, new GeoLocation(mLastLocaion.getLatitude(), mLastLocaion.getLongitude()));
                    break;
                default:
                    geoFireAvailable.removeLocation(userId);
                    geoFireWorking.setLocation(userId, new GeoLocation(mLastLocaion.getLatitude(), mLastLocaion.getLongitude()));
                    break;
            }


        }


    }



    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(10000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    public void connectDriver(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(DriverMapActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }


    public void disconnectDriver() {

        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("DriverAvailable");
        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(userId, new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {
                if (error != null) {
                    Toast.makeText(DriverMapActivity.this, "" + error, Toast.LENGTH_SHORT).show();
                } else {
                    //  Toast.makeText(getContext(), "Location Saved Successfully", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    final int LOCATION_REQUEST_CODE = 1;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case LOCATION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mapFragment.getMapAsync(this);
                } else {
                    Toast.makeText(getApplicationContext(), "Please Provide the permission", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }



    public void onBackPressed() {

        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
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
        LatLngBounds.Builder builder=LatLngBounds.builder();
        builder.include(new LatLng(mLastLocaion.getLatitude(),mLastLocaion.getLongitude()));
        builder.include(pickupLatLng);
        int width=getResources().getDisplayMetrics().widthPixels;
        int padding=(int)(width*0.2);
        LatLngBounds bounds=builder.build();
        CameraUpdate cameraUpdate=CameraUpdateFactory.newLatLngBounds(bounds,padding);
        mMap.animateCamera(cameraUpdate);
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

    private void getRoutToMarker(LatLng pickupLatLng) {
        Routing routing=new Routing.Builder()
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .alternativeRoutes(false)
                .waypoints(new LatLng(mLastLocaion.getLatitude(),mLastLocaion.getLongitude()),pickupLatLng)
                .build();
        routing.execute();

    }
    private void erasePolyLines(){
        for(Polyline line:polylines){
            line.remove();
        }
        polylines.clear();
    }
}


