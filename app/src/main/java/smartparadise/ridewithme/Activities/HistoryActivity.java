package smartparadise.ridewithme.Activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.Format;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


import smartparadise.ridewithme.Adapters.HistoryAdapter;
import smartparadise.ridewithme.DataModels.HistoryModels;
import smartparadise.ridewithme.R;

public class HistoryActivity extends AppCompatActivity {

    private RecyclerView historyRecyclerView;
    private LinearLayoutManager layoutManager;
    private RecyclerView.Adapter adapter;
    private ArrayList<HistoryModels> historyList;
    private String character,userId;
    private TextView noHistoryView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        noHistoryView=(TextView)findViewById(R.id.noHistoryView);
        historyRecyclerView=(RecyclerView)findViewById(R.id.historyRecycler);
        historyRecyclerView.setHasFixedSize(true);
        layoutManager=new LinearLayoutManager(getApplicationContext());
        historyRecyclerView.setLayoutManager(layoutManager);
        historyRecyclerView.setItemAnimator(new DefaultItemAnimator());


        historyList=new ArrayList<>();

        character=getIntent().getStringExtra("Character");
        userId= FirebaseAuth.getInstance().getCurrentUser().getUid();
        getUserHistoryFromDatabase();


    }


    private void getUserHistoryFromDatabase(){
        DatabaseReference userHistoryDatabase= FirebaseDatabase.getInstance().getReference().child("Users").child(character).child(userId).child("history");
        userHistoryDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for(DataSnapshot history:dataSnapshot.getChildren()){
                        fetchInformation(history.getKey());
                    }
                }else{
                    noHistoryView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void fetchInformation(String key) {
        DatabaseReference historyDatabase= FirebaseDatabase.getInstance().getReference().child("history").child(key);
        historyDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    String rideId=dataSnapshot.getKey();
                    Log.e("RideID",rideId);
                    Long timeStamp=0L;
                    for(DataSnapshot child:dataSnapshot.getChildren()){
                        if(child.getKey().equals("time")){
                            Log.e("time",child.getValue().toString());

                            timeStamp=Long.valueOf(child.getValue().toString());
                            Log.e("time",child.getValue().toString());
                        }

                    }
                    HistoryModels historyModel=new HistoryModels(rideId,getDate(timeStamp));
                    historyList.add(historyModel);
                    adapter=new HistoryAdapter(historyList,HistoryActivity.this);
                    historyRecyclerView.setAdapter(adapter);
                   adapter.notifyDataSetChanged();
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

}
