package com.jd.justindowty.gpsapp;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.app.Activity;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * This activity acts as a form for submitting a Meet Up Item.
 */
public class AddMeetUpActivity extends Activity {
    private Spinner courseCodeSpinner;
    private Spinner locationsSpinner;
    private Spinner ampmSpinner;
    private Spinner hoursSpinner;
    private Spinner minutesSpinner;
    private EditText courseNumber;
    private EditText description;
    private Button submitButton;
    private Button menuButton;
    /** List of Saved Locations, populated from Firebase user instance */
    private ArrayList<SavedLocation> locations;

    /* FIREBASE USER VARIABLES */
    /* User ID String (DB Writes go here) */
    private String userID;
    /* Database Instance */
    private FirebaseDatabase db;
    /* Usable reference to DB */
    private DatabaseReference dbRef;
    /* User Reference (DB/users/userID */
    private DatabaseReference userRef;
    /** Username is user's First and Last Name */
    private String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_meet_up);
        courseCodeSpinner = (Spinner) findViewById(R.id.courseCodeSpinner);
        locationsSpinner = (Spinner) findViewById(R.id.locationSpinner);
        ampmSpinner = (Spinner) findViewById(R.id.ampmSpinner);
        hoursSpinner = (Spinner) findViewById(R.id.hoursSpinner);
        minutesSpinner = (Spinner) findViewById(R.id.minutesSpinner);
        courseNumber = (EditText) findViewById(R.id.courseNumberEditText);
        description = (EditText) findViewById(R.id.descriptionEditText);
        submitButton = (Button) findViewById(R.id.submitMeetUpButton);
        menuButton = (Button) findViewById(R.id.mainMenuButton);
        locations = new ArrayList<SavedLocation>();
        /* SETTING FIREBASE DB REFERENCES AND USER INFO */
        /* Getting Current User */
        userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        /* Getting Database */
        db = FirebaseDatabase.getInstance();
        /* Getting Outer Database Reference */
        dbRef = db.getReference();
        /* Getting Database Reference to Current User*/
        userRef = dbRef.child("users").child(userID);
        userName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        loadCourseCodesSpinner();
        loadAMPMSpinner();
        loadLocationSpinner();
        loadTimeSpinners();
        submitButton.setOnClickListener((View v) -> {
            submitMeetUp();
        });
        menuButton.setOnClickListener((View v) -> {
            Intent i = new Intent(AddMeetUpActivity.this, MainMenuActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
        });
    }

    /**
     * Inputs are pulled from their views and error checked before Meet Up is submitted
     * to Firebase.
     */
    private void submitMeetUp(){
        int hoursInt;
        int minutesInt;
        int timeInt;
        if(courseCodeSpinner.getSelectedItem().toString().equals("")){
            Toast.makeText(AddMeetUpActivity.this, "Cannot Leave Course Code Blank!",
                    Toast.LENGTH_LONG).show();
            return;
        }
        if(courseNumber.getText().toString().equals("")){
            Toast.makeText(AddMeetUpActivity.this, "Cannot Leave Course Number Blank!",
                    Toast.LENGTH_LONG).show();
            return;
        }
        if(locationsSpinner.getSelectedItem().toString().equals("")){
            Toast.makeText(AddMeetUpActivity.this, "Cannot Leave Location Blank!",
                    Toast.LENGTH_LONG).show();
            return;
        }
        if(hoursSpinner.getSelectedItem().toString().equals("") || minutesSpinner.getSelectedItem().toString().equals("")){
            Toast.makeText(AddMeetUpActivity.this, "Cannot Leave Time Blank!",
                    Toast.LENGTH_LONG).show();
            return;
        } else {
            /* Hours and minutes are added together to make the time, the time is based on a 24 hour clock so PM hours are incremented by 12 */
            hoursInt = Integer.parseInt(hoursSpinner.getSelectedItem().toString());
            minutesInt = Integer.parseInt(minutesSpinner.getSelectedItem().toString());
            if(ampmSpinner.getSelectedItem().toString().equals("PM")){
                hoursInt = hoursInt * 100 + 1200;
            } else {
                hoursInt = hoursInt * 100;
            }
            timeInt = hoursInt + minutesInt;
            /* 12 AM and 12 PM times need to be flipped, who ever came up with this 12 hour clock? */
            if(timeInt >= 1200 && timeInt < 1300){
                timeInt += 1200;
            } else if (timeInt >= 2400 && timeInt < 2500){
                timeInt -= 1200;
            }
        }
        if(ampmSpinner.getSelectedItem().toString().equals("")){
            Toast.makeText(AddMeetUpActivity.this, "Cannot Leave AM/PM Blank!",
                    Toast.LENGTH_LONG).show();
            return;
        }
        if(description.getText().toString().equals("")){
            Toast.makeText(AddMeetUpActivity.this, "Cannot Leave Description Blank!",
                    Toast.LENGTH_LONG).show();
            return;
        }

        userRef.child("NUM_MEET_UPS").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            int n = Integer.parseInt(dataSnapshot.getValue().toString());
                            n++;
                            userRef.child("NUM_MEET_UPS").setValue(n);
                        } else {
                            userRef.child("NUM_MEET_UPS").setValue("1");
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.d("ERROR", databaseError.toString());
                    }
                });
        DatabaseReference newRef = dbRef.child("MEET_UPS").push();
        newRef.child("COURSE_CODE").setValue(courseCodeSpinner.getSelectedItem().toString());
        newRef.child("COURSE_NUMBER").setValue(courseNumber.getText().toString());
        newRef.child("SHORT_DESCRIPTION").setValue(description.getText().toString());
        newRef.child("TIME").setValue(timeInt);
        newRef.child("AM_PM").setValue(ampmSpinner.getSelectedItem().toString());
        newRef.child("USERNAME").setValue(userName);
        newRef.child("timestamp").setValue(new Date().getTime());
        /* Getting Lat and Long of selected location */
        for(SavedLocation s:locations) {
            if (locationsSpinner.getSelectedItem().toString().equals(s.getDescription())) {
                newRef.child("LAT").setValue(s.getLocation().getLatitude());
                newRef.child("LONG").setValue(s.getLocation().getLongitude());
                break;
            }
        }
        Toast.makeText(AddMeetUpActivity.this, "Meet up submitted!",
                Toast.LENGTH_LONG).show();
    }

    /**
     * The activity's course code spinner is loaded from array of prefixes.
     */
    private void loadCourseCodesSpinner(){
        List<String> courses = new ArrayList<String>(Arrays.asList("AAA", "ACC", "ANT", "ARA", "ART", "ATH", "BIO", "BMS", "BUS", "CAP", "CBR", "CFV", "CHI", "CHM", "CJ", "CJR", "CLA", "CLS", "CMB", "COM", "CPH", "CIS", "CTH", "DAN", "EAS", "ECO", "ED", "EDC", "EDG", "EDR", "EDS", "EGR", "ENG", "ENT", "ESL", "FIN", "FRE", "GEO", "GER", "GPY", "GRK", "HNR", "HPR", "HSC", "HST", "HTM", "IR", "ITA", "JPN", "LAS", "LAT", "LIB", "LS", "MES", "MGT", "MKT", "MOV", "MTH", "MUS", "NRM", "NUR", "OSH", "OT", "PA", "PAS", "PED", "PHI", "PHY", "PLS", "POL", "PSM", "PSY", "PT", "REC", "RST", "RUS", "SCI", "SOC", "SPA", "SS", "SST", "STA", "SW", "US", "WGS", "WRT"));
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, courses);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        courseCodeSpinner.setAdapter(adapter);
    }

    /**
     * The activity's AM PM spinner is loaded with AM and PM.
     */
    private void loadAMPMSpinner(){
        List<String> ampm =  new ArrayList<String>();
        ampm.add("AM");
        ampm.add("PM");
        ArrayAdapter<String> a = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, ampm);
        a.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ampmSpinner.setAdapter(a);
    }

    /**
     * The activity's location spinner is loaded with user's saved locations read from Firebase.
     */
    private void loadLocationSpinner(){
        userRef.child("LOCATIONS")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        List<String> spinnerArray =  new ArrayList<String>();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            spinnerArray.add(snapshot.child("DESCRIPTION").getValue().toString());
                            Location c = new Location("S");
                            c.setLatitude(Double.parseDouble(snapshot.child("LAT").getValue().toString()));
                            c.setLongitude(Double.parseDouble(snapshot.child("LONG").getValue().toString()));
                            locations.add(new SavedLocation(snapshot.child("DESCRIPTION").getValue().toString(), c));
                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                                AddMeetUpActivity.this, android.R.layout.simple_spinner_item, spinnerArray);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        locationsSpinner.setAdapter(adapter);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.d("ERROR", databaseError.toString());
                    }
                });
    }

    /**
     * The activity's Hours and Minutes spinners are loaded with 1 - 12 for hours
     * and 0 - 55 (increments of 5) for minutes.
     */
    private void loadTimeSpinners(){
        List<String> hoursArray =  new ArrayList<String>();
        for(int i = 1; i <= 12; i++){
            if(i < 10){
                hoursArray.add("0" + i);
            } else {
                hoursArray.add("" + i);
            }
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, hoursArray);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        hoursSpinner.setAdapter(adapter);

        List<String> minutesArray =  new ArrayList<String>();
        for(int i = 0; i < 60; i+=5){
            if(i < 10){
                minutesArray.add("0" + i);
            } else {
                minutesArray.add("" + i);
            }
        }
        ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, minutesArray);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        minutesSpinner.setAdapter(adapter2);
    }
}