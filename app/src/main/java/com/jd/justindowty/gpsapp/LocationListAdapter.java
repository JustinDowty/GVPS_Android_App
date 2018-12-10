package com.jd.justindowty.gpsapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * This adapter formats the location display list pop up that is used in the Maps Activity.
 */
public class LocationListAdapter extends ArrayAdapter<SavedLocation> {
    private final Context context;

    public LocationListAdapter(ArrayList<SavedLocation> locations, Context context) {
        super(context, R.layout.custom_pop_up_list, locations);
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        SavedLocation singleLoc = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.custom_pop_up_list, parent, false);
        }
        TextView textView = (TextView) convertView.findViewById(R.id.locationTextView);
        textView.setTextColor(context.getResources().getColor(R.color.white));
        textView.setText(singleLoc.getDescription());
        textView.setTextSize(35);
        return convertView;
    }
}