package com.jd.justindowty.gpsapp;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.android.PolyUtil;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.TravelMode;

import android.location.LocationManager;
import android.location.LocationListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * The Maps Activity is used to display a Google Maps instance that the user can interact with
 * by adding their current location with lat/long to their locations list in Firebase. The user is also
 * able to select a location they have saved to view it on the map, as well as remove locations from
 * their Firebase user save.
 */
public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener{
    /** List of user's locations, loaded from Firebase */
    private ArrayList<SavedLocation> locations;
    /** Google Map Instance */
    private GoogleMap mMap;
    /** Runnable to update the path, time, and distance if en route to a location */
    private Runnable updatePath;
    private Handler handler;
    private Button addLocationButton;
    private Button chooseLocationButton;
    private Button clearMarkersButton;
    private Button removeLocationButton;

    // These four variables refer to the location of a meet up if there is one to be shown
    private Intent ex;
    private TextView targetLocation;

    private Button menuButton;
    private final int REQUEST_PERMISSION_FINE_LOCATION = 1;
    LocationManager locationManager;

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
        setContentView(R.layout.activity_maps);
        final Context context = getApplicationContext();
        locations = new ArrayList<SavedLocation>();
        locationManager = (LocationManager)
                getSystemService(getApplicationContext().LOCATION_SERVICE);
        addLocationButton = (Button) findViewById(R.id.addLocationButton);
        chooseLocationButton = (Button) findViewById(R.id.chooseLocationButton);
        clearMarkersButton = (Button) findViewById(R.id.clearMarkersButton);
        menuButton = (Button) findViewById(R.id.menuButton);
        removeLocationButton = (Button) findViewById(R.id.removeLocationButton);
        targetLocation = (TextView) findViewById(R.id.targetLocationDescription);
        handler = new Handler();

            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);

        // Reading in extras if coming to map to view meet up location
        ex = getIntent();
        if(ex.hasExtra("COURSE") && ex.hasExtra("DESCRIPTION")){
            targetLocation.setText(ex.getStringExtra("COURSE")+" "+ex.getStringExtra("DESCRIPTION"));
        }

        /* SETTING FIREBASE DB REFERENCES AND USER INFO */
        /* Getting Current User */
        userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        /* Getting Database */
        db = FirebaseDatabase.getInstance();
        /* Getting Outer Database Reference */
        dbRef = db.getReference();
        /* Getting Database Reference to Current User*/
        userRef = dbRef.child("users").child(userID);

        // Reading in users locations
        readLocations();

        /* Calls a new instance of LocationListAdapter for a pop up in which the user can choose a location,
           when a location is selected that location is focused on the map.
         */
        chooseLocationButton.setOnClickListener((View v) -> {
            chooseLocation();
        });
        addLocationButton.setOnClickListener((View v) -> {
            Location c = getLastBestLocation();
            addLocationWindow(c);
        });
        clearMarkersButton.setOnClickListener((View v) -> {
            handler.removeCallbacks(updatePath, null);
            mMap.clear();
            targetLocation.setText("");
        });
        menuButton.setOnClickListener((View v)->{
            Intent i = new Intent(MapsActivity.this, MainMenuActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
        });
        removeLocationButton.setOnClickListener((View v) -> {
            removeLocationWindow();
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(updatePath, null);
    }

    /**
     * This method opens a dialog displaying each of the user's locations. When a user selects a location,
     * a marker is added for location and a loop that updates the path to the location begins on the handler.
     */
    private void chooseLocation(){
        if (!locations.isEmpty()) {
            DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mMap.clear();
                    targetLocation.setText("");
                    SavedLocation loc = locations.get(which);
                    Location c = loc.getLocation();
                    LatLng cLatLng = new LatLng(c.getLatitude(), c.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.zoomTo(17));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(cLatLng));

                    startPathUpdateLoop(c.getLatitude(), c.getLongitude(), loc.getDescription());
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
            builder.setTitle("YOUR LOCATIONS");
            LocationListAdapter adapter = new LocationListAdapter(locations, (MapsActivity.this));
            builder.setAdapter(adapter, listener);
            builder.create().show();
        } else {
            Toast.makeText(MapsActivity.this, "You Have No Locations Yet!", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Adds the update loop for path to location to the handler, removes previous loops first.
     * @param targetLat Latitude of destination
     * @param targetLong Longitude of destination
     * @param description Description string to be displayed.
     */
    private void startPathUpdateLoop(double targetLat, double targetLong, String description){
        handler.removeCallbacks(updatePath, null);
        updatePath = new Runnable(){
            public void run(){
                try {
                    DirectionsResult results = getDestinationResult(targetLat, targetLong);
                    if(results != null) {
                        mMap.clear();
                        targetLocation.setText("");
                        LatLng cLatLng = new LatLng(targetLat, targetLong);
                        mMap.addMarker(new MarkerOptions().position(cLatLng)
                                .title(description));
                        String distanceString = results.routes[0].legs[0].distance.humanReadable;
                        /* Checking if user is within 30 feet of their destination, which cancels the
                        /* Directions and alerts the user they have arrived.
                         */
                        if(distanceString.length() == 5){
                            if(distanceString.substring(3, 5).equals("ft") && Integer.parseInt(distanceString.substring(0,2)) < 30){
                                Toast.makeText(MapsActivity.this, "You have arrived at your destination!", Toast.LENGTH_SHORT).show();
                                handler.removeCallbacks(updatePath, null);
                                return;
                            }
                        } else if(distanceString.length() == 4){
                            if(distanceString.substring(2, 4).equals("ft") && Integer.parseInt(distanceString.substring(0,1)) < 30){
                                Toast.makeText(MapsActivity.this, "You have arrived at your destination!", Toast.LENGTH_SHORT).show();
                                handler.removeCallbacks(updatePath, null);
                                return;
                            }
                        }
                        addPolyline(results, mMap);
                        String resultString = getDistanceString(results);
                        String targetText = description + resultString;
                        targetLocation.setText(targetText);
                        handler.postDelayed(updatePath, 20 * 1000);
                    } else { throw new Exception(); }
                } catch (Exception e){
                    Toast.makeText(MapsActivity.this, "Error drawing path to location, may be too far.", Toast.LENGTH_SHORT).show();
                }
            }
        };
        handler.post(updatePath);
    }

    /** Adds Location to Firebase through current Lat/Long and a description */
    private void addLocationWindow(final Location c){
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("ADDING LOCATION");
        alert.setMessage("Describe your location!");

        // Set an EditText view to get user input
        final EditText input = new EditText(this);
        alert.setView(input);
        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                String i = input.getText().toString();
                if(i.isEmpty()){
                    Toast.makeText(MapsActivity.this, "Failure! Enter a description to add!", Toast.LENGTH_SHORT).show();
                    return;
                }
                // Adding new location to stored locations array
                SavedLocation s = new SavedLocation(i, c);
                locations.add(s);
                // Adding new location to DB
                DatabaseReference newRef = userRef.child("LOCATIONS").push();
                newRef.child("DESCRIPTION").setValue(i);
                newRef.child("LAT").setValue(c.getLatitude());
                newRef.child("LONG").setValue(c.getLongitude());
                Toast.makeText(MapsActivity.this, "Success! Location Added!", Toast.LENGTH_SHORT).show();
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });
        alert.show();
    }

    /**
     * Launches a confirmation dialog when the option to delete a location is chosen.
     * Selecting Yes removes location from Firebase and locations array.
     * @param snapshot Firebase item that is to be removed.
     * @param which The index at which this item is in the locations array.
     */
    private void confirmRemove(DataSnapshot snapshot, int which) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Are You Sure?")
                .setPositiveButton("Yes!", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        locations.remove(which);
                        snapshot.getRef().removeValue();
                        Toast.makeText(MapsActivity.this, "Success! Location Removed!", Toast.LENGTH_SHORT).show();
                        return;
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
     * This method spawns a window for removing a location containing each of the user's locations, when a location is clicked
     * the location is passed to the confirm window dialog.
     */
    private void removeLocationWindow(){
        if (!locations.isEmpty()) {
            DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    final SavedLocation loc = locations.get(which);
                    userRef.child("LOCATIONS")
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                        if (snapshot.child("DESCRIPTION").getValue().toString().equals(loc.getDescription())) {
                                            confirmRemove(snapshot , which);
                                            return;
                                        }
                                    }
                                    Toast.makeText(MapsActivity.this, "Failure! No Location By That Name!", Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                }
                            });
                }
            };
            AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
            builder.setTitle("YOUR LOCATIONS");
            LocationListAdapter adapter = new LocationListAdapter(locations, (MapsActivity.this));
            builder.setAdapter(adapter, listener);
            builder.create().show();
        } else {
            Toast.makeText(MapsActivity.this, "You Have No Locations Yet!", Toast.LENGTH_SHORT).show();
        }
    }

    /** Reads the users locations from Firebase into locations array */
    private void readLocations(){
        locations.clear();
        userRef.child("LOCATIONS")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Location c = new Location("L");
                            c.setLatitude(Double.parseDouble(snapshot.child("LAT").getValue().toString()));
                            c.setLongitude(Double.parseDouble(snapshot.child("LONG").getValue().toString()));
                            locations.add(new SavedLocation(snapshot.child("DESCRIPTION").getValue().toString(), c));
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.d("ERROR", databaseError.toString());
                    }
                });
    }

    @Override
    protected void onStop(){
        super.onStop();
    }

    /**
     * @return the last known best location
     */
    private Location getLastBestLocation() {
        int permissionCheck = ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSION_FINE_LOCATION);
            }
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 500, this);
        Location locationGPS;
        Location locationNet;
        try {
            locationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            locationNet = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        } catch (Exception e) {
            return new Location("L");
        }

        long GPSLocationTime = 0;
        if (null != locationGPS) {
            GPSLocationTime = locationGPS.getTime();
        }

        long NetLocationTime = 0;

        if (null != locationNet) {
            NetLocationTime = locationNet.getTime();
        }

        if (0 < GPSLocationTime - NetLocationTime) {
            return locationGPS;
        } else {
            return locationNet;
        }
    }

    /** Initializes the Google Map */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        int permissionCheck = ContextCompat.checkSelfPermission(
                this, Manifest.permission.READ_PHONE_STATE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_PHONE_STATE)) {

            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSION_FINE_LOCATION);
            }
        }
        mMap.setMyLocationEnabled(true);
        Location c = getLastBestLocation();
        mMap.moveCamera(CameraUpdateFactory.zoomTo(17));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(c.getLatitude(), c.getLongitude())));
        /* Adding Meet Up if requested to map, info on location passed from intent */
        if(ex.hasExtra("LAT") && ex.hasExtra("LONG")){
            double targetLat = ex.getDoubleExtra("LAT", 0);
            double targetLong = ex.getDoubleExtra("LONG", 0);
            LatLng latLng = new LatLng(targetLat, targetLong);
            mMap.addMarker(new MarkerOptions().position(latLng).title(ex.getStringExtra("COURSE")));
            mMap.moveCamera(CameraUpdateFactory.zoomTo(17));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            startPathUpdateLoop(targetLat, targetLong, targetLocation.getText().toString());
        }
    }

    private String getDistanceString(DirectionsResult results){
        return  " - Time: "+ results.routes[0].legs[0].duration.humanReadable
                + " - Distance: " + results.routes[0].legs[0].distance.humanReadable;
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            String permissions[],
            int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_FINE_LOCATION:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    Toast.makeText(MapsActivity.this, "Permission Denied!", Toast.LENGTH_SHORT).show();
                }
        }
    }

    //LocationListener methods (UNNEEDED)
    @Override
    public void onLocationChanged(Location location) {    }
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {    }
    @Override
    public void onProviderEnabled(String provider) {   }
    @Override
    public void onProviderDisabled(String provider) {   }

    /* DIRECTION FUNCTIONS */

    /**
     * Returns the GeoApiContext necessary for building a DirectionsResult in the getDestinationResult() function, uses my unique API key.
     * @return GeoApiContext
     */
    private GeoApiContext getGeoContext() {
        String directionsApiKey = "AIzaSyAFOQGrMmclCoaG_JhqwTg1Lb235SC75gk";
        GeoApiContext geoApiContext = new GeoApiContext();
        return geoApiContext.setQueryRateLimit(3)
                .setApiKey(directionsApiKey).setConnectTimeout(2, TimeUnit.SECONDS)
                .setReadTimeout(2, TimeUnit.SECONDS)
                .setWriteTimeout(2, TimeUnit.SECONDS);
    }

    /**
     * Takes a destination location and returns the direction info from the user's current location
     * to that destination, this is used to draw the Polyline direction to location.
     * @param destLat Destination Latitude.
     * @param destLng Destination longitude.
     * @return DirectionsResult to destination.
     */
    private DirectionsResult getDestinationResult(double destLat, double destLng){
        try {
            Location c = getLastBestLocation();
            com.google.maps.model.LatLng origin = new com.google.maps.model.LatLng(c.getLatitude(), c.getLongitude());
            com.google.maps.model.LatLng destination = new com.google.maps.model.LatLng(destLat, destLng);
            DateTime now = new DateTime();
            return DirectionsApi.newRequest(getGeoContext())
                    .mode(TravelMode.WALKING)
                    .origin(origin)
                    .destination(destination)
                    .departureTime(now)
                    .await();
        } catch(Exception e) {
            Toast.makeText(MapsActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    /**
     * Draws the optimized direction path between locations based on a DirectionsResult calculation.
     * @param results DirectionsResult between two locations.
     * @param mMap Google map instance.
     */
    private void addPolyline(DirectionsResult results, GoogleMap mMap) {
        List<LatLng> decodedPath = PolyUtil.decode(results.routes[0].overviewPolyline.getEncodedPath());
        mMap.addPolyline(new PolylineOptions().addAll(decodedPath));
    }
}