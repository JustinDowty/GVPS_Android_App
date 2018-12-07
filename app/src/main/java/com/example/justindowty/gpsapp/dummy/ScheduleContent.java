package com.example.justindowty.gpsapp.dummy;

import java.util.ArrayList;
import java.util.List;

/**
 * This class serves as a model for the Schedule Activity's content.
 * This class contains the static list of all of the Schedule Items.
 * ScheduleItem class is contained and models each ScheduleItem's content.
 */
public class ScheduleContent {
    /** Static list of Schedule Items */
    public static final List<ScheduleItem> ITEMS = new ArrayList<ScheduleItem>();
    /** Adds a ScheduleItem to the static list */
    public static void addItem(ScheduleItem item) {
        ITEMS.add(item);
    }
    /** Clears the static list */
    public static void clearItems(){
        ITEMS.clear();
    }

    /**
     * Schedule Item's Content
     */
    public static class ScheduleItem {
        public final String description;
        public final String course;
        public final Double lat;
        public final Double lng;
        public final int time;
        public final String ampm;
        public final String[] days;

        public ScheduleItem(String description, String courseName, String courseNumber, Double lat, Double lng, int time, String ampm, String[] days) {
            this.description = description;
            this.course = courseName + " " + courseNumber;
            this.lat = lat;
            this.lng = lng;
            this.time = time;
            this.ampm = ampm;
            this.days = days;
        }

        @Override
        public String toString() {
            return description + " " + course + " " + time + " " + days[0];
        }
    }
}
