package com.example.justindowty.gpsapp;

import android.location.Location;

/**
 * Model for a Saved Location used by Maps Activity.
 */

public class SavedLocation {
    private String description;
    private Location location;

    public SavedLocation(String description, Location location){
        this.description = description;
        this.location = location;
    }

    public String getDescription(){
        return description;
    }

    public Location getLocation(){
        return location;
    }
}
