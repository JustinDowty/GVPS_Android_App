package com.jd.justindowty.gpsapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.jd.justindowty.gpsapp.dummy.MeetUpContent;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static com.jd.justindowty.gpsapp.MeetUpsFragment.meetUpAdapter;
import static com.jd.justindowty.gpsapp.dummy.MeetUpContent.ITEMS;
import static com.jd.justindowty.gpsapp.dummy.MeetUpContent.addItem;
import static com.jd.justindowty.gpsapp.dummy.MeetUpContent.clearItems;

/**
 * This activity reads Meet Ups from Firebase and displays them within a MeetUpsFragment.
 * When a meet up is clicked the Maps activity is opened to show location and path to location.
 */
public class MeetUpsActivity extends Activity implements MeetUpsFragment.OnListFragmentInteractionListener  {
    private Spinner filterCourseName;
    private EditText filterCourseNumber;
    private FloatingActionButton backFAB;
    private String user;
    private FirebaseDatabase db;
    private DatabaseReference dbRef;
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meet_ups);
        /* Getting Current User */
        user = FirebaseAuth.getInstance().getCurrentUser().getUid();
        /* Getting Database */
        db = FirebaseDatabase.getInstance();
        /* Getting Outer Database Reference */
        dbRef = db.getReference();
        /* Getting Database Reference to Current User*/
        userRef = dbRef.child("users").child(user);
        filterCourseName = (Spinner) findViewById(R.id.filterCourseSpinner);
        filterCourseNumber = (EditText) findViewById(R.id.filterCourseNumber);
        backFAB = (FloatingActionButton) findViewById(R.id.backFAB);
        loadCourseCodesSpinner();
        filterCourseName.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                updateMeetUps();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView) { }
        });
        filterCourseNumber.addTextChangedListener(new TextWatcher(){
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override
            public void afterTextChanged(Editable editable) {
                updateMeetUps();
            }
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
        });
        backFAB.setOnClickListener((View v) -> {
            finish();
        });
    }

    @Override
    public void onListFragmentInteraction(MeetUpContent.MeetUpItem item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("View Meet Up On Map?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        viewOnMap(item);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    /**
     * Opens the map activity, passing the chosen Meet Up location as extras to be viewed.
     * @param item Meet Up Item to be passed to Google Map.
     */
    private void viewOnMap(MeetUpContent.MeetUpItem item) {
        dbRef.child("STATS").child(item.username).child("MEET_UP_CLICKS").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    int n = Integer.parseInt(dataSnapshot.getValue().toString());
                    n++;
                    dbRef.child("STATS").child(item.username).child("MEET_UP_CLICKS").setValue(n);
                } else {
                    dbRef.child("STATS").child(item.username).child("MEET_UP_CLICKS").setValue(1);
                }
                /* Converting from 24hr time */
                int time;
                String timeString;
                if(item.time >= 1300){
                    time = item.time - 1200;
                } else {
                    time = item.time;
                }
                timeString = "" + time;
                if(time < 1000){
                    timeString = timeString.substring(0,1) + ":" + timeString.substring(1,3);
                } else {
                    timeString = timeString.substring(0, 2) + ":" + timeString.substring(2, 4);
                }
                String timeAndDescription = timeString + " " + item.ampm + " - " + item.description;
                Intent i = new Intent(MeetUpsActivity.this, MapsActivity.class);
                i.putExtra("DESCRIPTION", timeAndDescription);
                i.putExtra("COURSE", item.course);
                i.putExtra("LAT", item.lat);
                i.putExtra("LONG", item.lng);
                startActivity(i);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("ERROR", databaseError.toString());
            }
        });
    }

    /**
     * Loads the Course Code Spinner with GVSU Course Codes.
     */
    private void loadCourseCodesSpinner(){
        List<String> courses = new ArrayList<String>(Arrays.asList("ALL", "AAA", "ACC", "ANT", "ARA", "ART", "ATH", "BIO", "BMS", "BUS", "CAP", "CBR", "CFV", "CHI", "CHM", "CJ", "CJR", "CLA", "CLS", "CMB", "COM", "CPH", "CIS", "CTH", "DAN", "EAS", "ECO", "ED", "EDC", "EDG", "EDR", "EDS", "EGR", "ENG", "ENT", "ESL", "FIN", "FRE", "GEO", "GER", "GPY", "GRK", "HNR", "HPR", "HSC", "HST", "HTM", "xIR", "ITA", "JPN", "LAS", "LAT", "LIB", "LS", "MES", "MGT", "MKT", "MOV", "MTH", "MUS", "NRM", "NUR", "OSH", "xOT", "PA", "PAS", "PED", "PHI", "PHY", "PLS", "POL", "PSM", "PSY", "xPT", "REC", "RST", "RUS", "SCI", "SOC", "SPA", "SS", "SST", "STA", "SW", "US", "WGS", "WRT"));
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, courses);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterCourseName.setAdapter(adapter);
    }

    /**
     * Updates meet ups based on the Course Code and Course Number search criteria.
     */
    private void updateMeetUps(){
        /* Database Instance */
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        /* Usable reference to DB */
        DatabaseReference dbRef = db.getReference();
        dbRef.child("MEET_UPS")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        clearItems();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            /* Removing old posts before posts are viewed */
                            long currentTime = new Date().getTime();
                            long cutOff = currentTime - 12 * 60 * 60 * 1000; // 12 Hours
                            if(snapshot.child("timestamp").getValue(Long.class) < cutOff){
                                snapshot.getRef().removeValue();
                                continue;
                            }
                            double meetUpNum = Double.parseDouble(snapshot.child("COURSE_NUMBER").getValue().toString());
                            /* Try to get valid input, if empty or invalid default to 0, or no input */
                            double searchNum;
                            try {
                                searchNum = Double.parseDouble(filterCourseNumber.getText().toString());
                            } catch(NumberFormatException e){
                                searchNum = 0;
                            }
                            String meetUpCode = snapshot.child("COURSE_CODE").getValue().toString();
                            String searchCode = filterCourseName.getSelectedItem().toString();
                            //  View all checked first, both searched second, just code third
                            if(searchCode.equals("ALL")) {
                                addItem(new MeetUpContent.MeetUpItem(snapshot.child("SHORT_DESCRIPTION").getValue().toString(),
                                        snapshot.child("COURSE_CODE").getValue().toString(),
                                        snapshot.child("COURSE_NUMBER").getValue().toString(),
                                        Double.parseDouble(snapshot.child("LAT").getValue().toString()),
                                        Double.parseDouble(snapshot.child("LONG").getValue().toString()),
                                        snapshot.child("TIME").getValue(Integer.class),
                                        snapshot.child("AM_PM").getValue().toString(),
                                        snapshot.child("USERNAME").getValue().toString()));
                                /* Checking if only code was searched because number was empty or wrong */
                            } else if(meetUpNum == searchNum && searchNum != 0 && meetUpCode.equals(searchCode)) {
                                addItem(new MeetUpContent.MeetUpItem(snapshot.child("SHORT_DESCRIPTION").getValue().toString(),
                                        snapshot.child("COURSE_CODE").getValue().toString(),
                                        snapshot.child("COURSE_NUMBER").getValue().toString(),
                                        Double.parseDouble(snapshot.child("LAT").getValue().toString()),
                                        Double.parseDouble(snapshot.child("LONG").getValue().toString()),
                                        snapshot.child("TIME").getValue(Integer.class),
                                        snapshot.child("AM_PM").getValue().toString(),
                                        snapshot.child("USERNAME").getValue().toString()));
                                /* Checking if only code was searched because number was empty or wrong */
                            } else if (searchNum == 0 && meetUpCode.equals(searchCode)){
                                addItem(new MeetUpContent.MeetUpItem(snapshot.child("SHORT_DESCRIPTION").getValue().toString(),
                                        snapshot.child("COURSE_CODE").getValue().toString(),
                                        snapshot.child("COURSE_NUMBER").getValue().toString(),
                                        Double.parseDouble(snapshot.child("LAT").getValue().toString()),
                                        Double.parseDouble(snapshot.child("LONG").getValue().toString()),
                                        snapshot.child("TIME").getValue(Integer.class),
                                        snapshot.child("AM_PM").getValue().toString(),
                                        snapshot.child("USERNAME").getValue().toString()));
                            }
                        }
                        Collections.reverse(ITEMS);
                        meetUpAdapter.notifyDataSetChanged();
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.d("ERROR", databaseError.toString());
                    }
                });
    }
}
