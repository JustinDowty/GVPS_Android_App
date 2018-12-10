package com.jd.justindowty.gpsapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Toast;

import com.jd.justindowty.gpsapp.dummy.ScheduleContent;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Displays User's Schedule Items via a ScheduleFragment.
 */
public class ScheduleActivity extends Activity implements ScheduleFragment.OnListFragmentInteractionListener  {
    private FloatingActionButton backFAB;
    /* FIREBASE USER VARIABLES */
    /* User ID String (DB Writes go here) */
    private String userID;
    /* Database Instance */
    private FirebaseDatabase db;
    /* Usable reference to DB */
    private DatabaseReference dbRef;
    /* User Reference (DB/users/userID */
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);
        /* SETTING FIREBASE DB REFERENCES AND USER INFO */
        /* Getting Current User */
        userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        /* Getting Database */
        db = FirebaseDatabase.getInstance();
        /* Getting Outer Database Reference */
        dbRef = db.getReference();
        /* Getting Database Reference to Current User*/
        userRef = dbRef.child("users").child(userID);
        backFAB = (FloatingActionButton) findViewById(R.id.backFAB);
        backFAB.setOnClickListener((View v) -> {
            finish();
        });
    }

    @Override
    public void onListFragmentInteraction(ScheduleContent.ScheduleItem item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("View location or remove schedule item?")
                .setPositiveButton("View Map", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
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
                        Intent i = new Intent(ScheduleActivity.this, MapsActivity.class);
                        i.putExtra("DESCRIPTION", timeAndDescription);
                        i.putExtra("COURSE", item.course);
                        i.putExtra("LAT", item.lat);
                        i.putExtra("LONG", item.lng);
                        startActivity(i);
                    }
                })
                .setNegativeButton("Remove", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                            confirmRemove(item);
                            dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    /**
     * If a user selects to remove a schedule item, this is used as a confirm check before removing permanently from Firebase.
     * @param item Schedule item to be removed
     */
    private void confirmRemove(ScheduleContent.ScheduleItem item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Are You Sure?")
                .setPositiveButton("Yes!", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        removeItem(item);
                    }
                })
                .setNegativeButton("NOO!", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    /**
     * Dialog to remove Schedule Item from Firebase.
     * @param item Item selected to be removed.
     */
    private void removeItem(ScheduleContent.ScheduleItem item){
        userRef.child("SCHEDULE")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            if (snapshot.child("SHORT_DESCRIPTION").getValue().toString().equals(item.description)) {
                                snapshot.getRef().removeValue();
                                Toast.makeText(ScheduleActivity.this, "Removed Schedule Item", Toast.LENGTH_LONG).show();
                                break;
                            }
                        }
                        finish();
                        Intent i = new Intent(ScheduleActivity.this, MainMenuActivity.class);
                        startActivity(i);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(ScheduleActivity.this, "Error Removing", Toast.LENGTH_LONG).show();
                    }
                });
    }
}
