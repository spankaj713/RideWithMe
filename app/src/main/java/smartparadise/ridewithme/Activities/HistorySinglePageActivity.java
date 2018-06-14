package smartparadise.ridewithme.Activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.BinderThread;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import smartparadise.ridewithme.Adapters.PayPalConfig;
import smartparadise.ridewithme.R;

public class HistorySinglePageActivity extends AppCompatActivity implements OnMapReadyCallback,RoutingListener {


    @BindView(R.id.rideLocation)
    TextView rideLocation;
    @BindView(R.id.rideDistance)
    TextView rideDistance;
    @BindView(R.id.rideDate)
    TextView rideDate;
    @BindView(R.id.userName)
    TextView userName;
    @BindView(R.id.userPhone)
    TextView userPhone;
    @BindView(R.id.userImageHistory)
    CircleImageView userImageHistory;
    private String rideId,currentUserId,customerId,driverId,character;
    private GoogleMap mMap;
    private SupportMapFragment mapFragment;
    private DatabaseReference historyDatabaseRef;
    private LatLng destinationLatLng,pickupLatLng;
    @BindView(R.id.driverRating)
    RatingBar driverRating;
    boolean customerPaid=false;

    Button payButton;
    String distance;
    Double rideCost;
    private List<Polyline> polylines;
    private static final int[] COLORS = new int[]{R.color.colorPrimaryDark};



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_single_page);
        ButterKnife.bind(this);
        Intent intent=new Intent(this,PayPalService.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION,config);
        startService(intent);
        payButton=(Button)findViewById(R.id.payButton);
        rideId = getIntent().getExtras().getString("rideId");
        polylines=new ArrayList<>();
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapHistory);
        mapFragment.getMapAsync(this);
        currentUserId= FirebaseAuth.getInstance().getCurrentUser().getUid();
        historyDatabaseRef= FirebaseDatabase.getInstance().getReference().child("history").child(rideId);
        getRideInformation();

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap=googleMap;
    }

    private void getRideInformation(){
        historyDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for(DataSnapshot child:dataSnapshot.getChildren()){
                        if(child.getKey().equals("rider")){
                            customerId=child.getValue().toString();
                            if(!customerId.equals(currentUserId)){
                                character="Drivers";
                                getUserInformation("Riders",customerId);
                            }
                        }
                        if(child.getKey().equals("driver")){
                            driverId=child.getValue().toString();
                            if(!driverId.equals(currentUserId)){
                                character="Riders";
                                getUserInformation("Drivers",driverId);
                                displayRiderObjects();
                            }
                        }
                        if(child.getKey().equals("time")){
                            driverId=child.getValue().toString();
                            rideDate.setText(getDate(Long.valueOf(child.getValue().toString())));
                        }
                        if(child.getKey().equals("rating")){
                            driverRating.setRating(Integer.valueOf(child.getValue().toString()));
                        }
                        if(child.getKey().equals("customerPaid")){
                            customerPaid=true;
                        }
                        if(child.getKey().equals("distance")){
                            distance=child.getValue().toString();
                            rideDistance.setText(distance.substring(0,Math.min(distance.length(),5))+" km");
                            rideCost=Double.valueOf(distance)*1.5;
                        }
                        if(child.getKey().equals("destination")){
                            rideLocation.setText(child.getValue().toString());
                        }
                        if(child.getKey().equals("location")){
                            pickupLatLng=new LatLng(Double.valueOf(child.child("from").child("lat").getValue().toString()),Double.valueOf(child.child("from").child("long").getValue().toString()));
                            destinationLatLng=new LatLng(Double.valueOf(child.child("to").child("lat").getValue().toString()),Double.valueOf(child.child("to").child("long").getValue().toString()));
                             if(destinationLatLng!=new LatLng(0.0,0.0)){
                                getRoutToMarker();
                            }

                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void displayRiderObjects() {
        driverRating.setVisibility(View.VISIBLE);
        payButton.setVisibility(View.VISIBLE);
        driverRating.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {
                historyDatabaseRef.child("rating").setValue(v);
                DatabaseReference driverRatingRef=FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverId).child("ratings");
                driverRatingRef.child(rideId).setValue(v);
            }
        });
        if(customerPaid){
            payButton.setEnabled(false);
        }else{
            payButton.setEnabled(true);
        }
        payButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                paypalPayment();
            }
        });
    }
    private int PAYPAL_REQUEST_CODE=1;
    private static PayPalConfiguration config=new PayPalConfiguration()
            .environment(PayPalConfiguration.ENVIRONMENT_SANDBOX).clientId(PayPalConfig.PAYPAL_CLIENT_ID);

    private void paypalPayment(){

        PayPalPayment payment=new PayPalPayment(new BigDecimal(rideCost),"USD","RideWithMe",PayPalPayment.PAYMENT_INTENT_SALE);
        Intent intent=new Intent(this, PaymentActivity.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION,config);
        intent.putExtra(PaymentActivity.EXTRA_PAYMENT,payment);
        startActivityForResult(intent,PAYPAL_REQUEST_CODE);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==PAYPAL_REQUEST_CODE){
            if(resultCode== Activity.RESULT_OK){
                PaymentConfirmation confirm=data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
                if(confirm!=null){
                    try{
                        JSONObject jsonObject=new JSONObject(confirm.toJSONObject().toString());

                        String paymentResponse=jsonObject.getJSONObject("response").getString("state");
                        if(paymentResponse.equals("approved")){
                            Toast.makeText(this, "Payment Successfull", Toast.LENGTH_SHORT).show();
                            historyDatabaseRef.child("customerPaid").setValue(true);
                            payButton.setEnabled(false);
                        }


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }else{
                Toast.makeText(this, "Payment Unsuccessful", Toast.LENGTH_SHORT).show();
            }
        }
    }

    String currentUserName;
    private void getUserInformation(String otherUserDriverOrCustomer, String otherUserId) {
        DatabaseReference OtherUserDB=FirebaseDatabase.getInstance().getReference().child("Users").child(otherUserDriverOrCustomer).child(otherUserId);
        OtherUserDB.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    Map<String,Object> map=(Map<String,Object>)dataSnapshot.getValue();
                    if(map.get("first_name")!=null&&map.get("last_name")!=null){
                        currentUserName= map.get("first_name")+" "+map.get("last_name").toString();
                        userName.setText(currentUserName);
                    }
                    if(map.get("phone")!=null){
                        userPhone.setText(map.get("phone").toString());
                    }
                    if(map.get("ProfileImage")!=null){
                        Glide.with(getApplication()).load(map.get("ProfileImage").toString()).into(userImageHistory);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private String getDate(Long timeStamp){
        Calendar cal=Calendar.getInstance(Locale.getDefault());
        cal.setTimeInMillis(timeStamp*1000);
        String date= android.text.format.DateFormat.format("dd-MM-yyyy hh:mm",cal).toString();

        return date;
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
        LatLngBounds.Builder builder=new LatLngBounds.Builder();
        builder.include(pickupLatLng);
        builder.include(destinationLatLng);
        LatLngBounds bounds=builder.build();

        int width=getResources().getDisplayMetrics().widthPixels;
        int padding=(int)(width*0.2);

        CameraUpdate cameraUpdate= CameraUpdateFactory.newLatLngBounds(bounds,padding);
        mMap.animateCamera(cameraUpdate);
        mMap.addMarker(new MarkerOptions().position(pickupLatLng).title("Pick Location").icon(BitmapDescriptorFactory.fromResource(R.drawable.pickupmark)));
        mMap.addMarker(new MarkerOptions().position(destinationLatLng).title("Destination"));


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

    private void getRoutToMarker() {
        Routing routing=new Routing.Builder()
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .alternativeRoutes(false)
                .waypoints(pickupLatLng,destinationLatLng)
                .build();
        routing.execute();

    }

    @Override
    protected void onDestroy() {
        stopService(new Intent(this,PayPalService.class));
        super.onDestroy();

    }
}
