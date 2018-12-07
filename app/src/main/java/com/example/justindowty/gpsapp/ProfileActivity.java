package com.example.justindowty.gpsapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Displays profile information.
 */
public class ProfileActivity extends AppCompatActivity {
    private TextView profileName;
    private TextView meetUpsTextView;
    private TextView clicksTextView;
    private String user;
    private String userName;
    private FirebaseDatabase db;
    private DatabaseReference dbRef;
    private DatabaseReference userRef;
    private Button menuButton;
    private ImageView meetUpsMedal;
    private ImageView clicksMedal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        /* Getting Current User */
        user = FirebaseAuth.getInstance().getCurrentUser().getUid();
        userName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        /* Getting Database */
        db = FirebaseDatabase.getInstance();
        /* Getting Outer Database Reference */
        dbRef = db.getReference();
        /* Getting Database Reference to Current User*/
        userRef = dbRef.child("users").child(user);
        menuButton = (Button) findViewById(R.id.menuButton);
        profileName = (TextView) findViewById(R.id.profileName);
        meetUpsTextView = (TextView) findViewById(R.id.meetUpsTextView);
        clicksTextView = (TextView) findViewById(R.id.clicksTextView);
        clicksMedal= (ImageView) findViewById(R.id.clicksMedal);
        meetUpsMedal = (ImageView) findViewById(R.id.meetUpsMedal);
        profileName.setText(userName);
        /* Num Meet Ups loaded from user db */
        userRef.child("NUM_MEET_UPS").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    meetUpsTextView.setText(dataSnapshot.getValue().toString());
                    int n = Integer.parseInt(dataSnapshot.getValue().toString());
                    if(n > 30){
                        meetUpsMedal.setImageResource(R.drawable.gold);
                    } else if(n > 10){
                        meetUpsMedal.setImageResource(R.drawable.silver);
                    }
                } else {
                    meetUpsTextView.setText("0");
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("ERROR", databaseError.toString());
            }
        });
        /* Num Clicks loaded from stats db */
        dbRef.child("STATS").child(userName).child("MEET_UP_CLICKS").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    clicksTextView.setText(dataSnapshot.getValue().toString());
                    int n = Integer.parseInt(dataSnapshot.getValue().toString());
                    if(n > 50){
                        meetUpsMedal.setImageResource(R.drawable.gold);
                    } else if(n > 25){
                        meetUpsMedal.setImageResource(R.drawable.silver);
                    }
                } else {
                    clicksTextView.setText("0");
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("ERROR", databaseError.toString());
            }
        });
        menuButton.setOnClickListener((View v) -> {
            Intent i = new Intent(ProfileActivity.this, MainMenuActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
        });
    }
}
