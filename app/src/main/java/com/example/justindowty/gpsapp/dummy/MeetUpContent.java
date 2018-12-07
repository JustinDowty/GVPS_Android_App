package com.example.justindowty.gpsapp.dummy;

import java.util.ArrayList;
import java.util.List;

/**
 * This class serves as a model for the Meet Up Activity's content.
 * This class contains the static list of all of the Meet Up Items.
 * MeetUpItem class is contained and models each MeetUpItem's content.
 */
public class MeetUpContent {
    /** List of Meet Up Items */
    public static final List<MeetUpItem> ITEMS = new ArrayList<MeetUpItem>();
    /** Adds a MeetUpItem to static list */
    public static void addItem(MeetUpItem item) {
        ITEMS.add(item);
    }
    /** Clears the static list */
    public static void clearItems(){
        ITEMS.clear();
    }

    /**
     * Meet Up Item's Content
     */
    public static class MeetUpItem {
        public final String description;
        public final String course;
        public final Double lat;
        public final Double lng;
        public final int time;
        public final String ampm;
        public final String username;

        public MeetUpItem(String description, String courseName, String courseNumber, Double lat, Double lng, int time, String ampm, String username) {
            this.description = description;
            this.course = courseName + " " + courseNumber;
            this.lat = lat;
            this.lng = lng;
            this.time = time;
            this.ampm = ampm;
            this.username = username;
        }

        @Override
        public String toString() {
            return description + " " + course + " " + time + " - " + username;
        }
    }
}
