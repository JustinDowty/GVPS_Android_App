package com.example.justindowty.gpsapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.justindowty.gpsapp.dummy.MeetUpContent;
import com.example.justindowty.gpsapp.dummy.ScheduleContent;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import static com.example.justindowty.gpsapp.dummy.MeetUpContent.addItem;
import static com.example.justindowty.gpsapp.dummy.MeetUpContent.clearItems;

/**
 * The Main Menu for all activities in the application.
 */
public class MainMenuActivity extends AppCompatActivity {
    private String user;
    private FirebaseDatabase db;
    private DatabaseReference dbRef;
    private DatabaseReference userRef;
    private Button mapButton;
    private Button addMeetUpButton;
    private Button viewMeetUpButton;
    private Button addScheduleButton;
    private Button viewScheduleButton;
    private Button viewProfileButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        mapButton = (Button) findViewById(R.id.mapButton);
        addMeetUpButton = (Button) findViewById(R.id.menuMeetUpButton);
        viewMeetUpButton = (Button) findViewById(R.id.menuViewMeetUps);
        addScheduleButton = (Button) findViewById(R.id.menuScheduleButton);
        viewScheduleButton = (Button) findViewById(R.id.menuViewScheduleButton);
        viewProfileButton = (Button) findViewById(R.id.menuViewProfileButton);
        /* Getting Current User */
        user = FirebaseAuth.getInstance().getCurrentUser().getUid();
        /* Getting Database */
        db = FirebaseDatabase.getInstance();
        /* Getting Outer Database Reference */
        dbRef = db.getReference();
        /* Getting Database Reference to Current User*/
        userRef = dbRef.child("users").child(user);
        mapButton.setOnClickListener((View v) -> {
            Intent i = new Intent(MainMenuActivity.this, MapsActivity.class);
            startActivity(i);
        });
        addMeetUpButton.setOnClickListener((View v) -> {
            Intent i = new Intent(MainMenuActivity.this, AddMeetUpActivity.class);
            startActivity(i);
        });
        addScheduleButton.setOnClickListener((View v) -> {
            Intent i = new Intent(MainMenuActivity.this, AddScheduleActivity.class);
            startActivity(i);
        });
        viewMeetUpButton.setOnClickListener((View v) -> {
            /* ADDING ITEMS TO MEET UP LIST */
            MeetUpContent.clearItems();
            dbRef.child("MEET_UPS")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                MeetUpContent.addItem(new MeetUpContent.MeetUpItem(snapshot.child("SHORT_DESCRIPTION").getValue().toString(),
                                        snapshot.child("COURSE_CODE").getValue().toString(),
                                        snapshot.child("COURSE_NUMBER").getValue().toString(),
                                        Double.parseDouble(snapshot.child("LAT").getValue().toString()),
                                        Double.parseDouble(snapshot.child("LONG").getValue().toString()),
                                        snapshot.child("TIME").getValue(Integer.class),
                                        snapshot.child("AM_PM").getValue().toString(),
                                        snapshot.child("USERNAME").getValue().toString()));
                            }
                            Intent i = new Intent(MainMenuActivity.this, MeetUpsActivity.class);
                            startActivity(i);
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Log.d("ERROR", databaseError.toString());
                        }
                    });
        });
        viewScheduleButton.setOnClickListener((View v) -> {
            /* ADDING ITEMS TO SCHEDULE LIST */
            ScheduleContent.clearItems();
            userRef.child("SCHEDULE")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                GenericTypeIndicator<List<String>> t = new GenericTypeIndicator<List<String>>() {};
                                List<String> list = snapshot.child("DAYS").getValue(t);
                                String[] days = new String[list.size()];
                                days = list.toArray(days);
                                Log.d("MAINMENU", days[0]);
                                ScheduleContent.addItem(new ScheduleContent.ScheduleItem(snapshot.child("SHORT_DESCRIPTION").getValue().toString(),
                                        snapshot.child("COURSE_CODE").getValue().toString(),
                                        snapshot.child("COURSE_NUMBER").getValue().toString(),
                                        Double.parseDouble(snapshot.child("LAT").getValue().toString()),
                                        Double.parseDouble(snapshot.child("LONG").getValue().toString()),
                                        snapshot.child("TIME").getValue(Integer.class),
                                        snapshot.child("AM_PM").getValue().toString(),
                                        days));
                            }
                            Intent i = new Intent(MainMenuActivity.this, ScheduleActivity.class);
                            startActivity(i);
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Log.d("ERROR", databaseError.toString());
                        }
                    });
        });
        viewProfileButton.setOnClickListener((View v) -> {
            Intent i = new Intent(MainMenuActivity.this, ProfileActivity.class);
            startActivity(i);
        });
    }
}
