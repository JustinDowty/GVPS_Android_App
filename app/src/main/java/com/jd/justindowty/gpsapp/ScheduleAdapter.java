package com.jd.justindowty.gpsapp;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.jd.justindowty.gpsapp.ScheduleFragment.OnListFragmentInteractionListener;
import com.jd.justindowty.gpsapp.dummy.ScheduleContent;
import com.jd.justindowty.gpsapp.dummy.ScheduleContent.ScheduleItem;
import com.truizlop.sectionedrecyclerview.SectionedRecyclerViewAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
/**
 * {@link RecyclerView.Adapter} that can display a {@link ScheduleItem} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class ScheduleAdapter extends SectionedRecyclerViewAdapter<ScheduleAdapter.HeaderViewHolder,
        ScheduleAdapter.ViewHolder,
        ScheduleAdapter.FooterViewHolder> {

    private final HashMap<String,List<ScheduleContent.ScheduleItem>> itemsMap;
    private final List<String> dayHeaders;
    private final OnListFragmentInteractionListener mListener;

    /**
     * Items are first sorted into 7 lists for each day of the week, if an item is in multiple days it gets added in each applicable list
     * to properly display each days schedule. The items then get sorted by time, ascending.
     * @param items Schedule Items
     * @param listener The listener for adapter
     */
    public ScheduleAdapter(List<ScheduleItem> items, OnListFragmentInteractionListener listener) {
        dayHeaders = new ArrayList<String>();
        itemsMap = new HashMap<String,List<ScheduleContent.ScheduleItem>>();
        List<ScheduleItem> monItems = new ArrayList<ScheduleItem>();
        List<ScheduleItem> tuesItems = new ArrayList<ScheduleItem>();
        List<ScheduleItem> wedItems = new ArrayList<ScheduleItem>();
        List<ScheduleItem> thursItems = new ArrayList<ScheduleItem>();
        List<ScheduleItem> friItems = new ArrayList<ScheduleItem>();
        List<ScheduleItem> satItems = new ArrayList<ScheduleItem>();
        List<ScheduleItem> sunItems = new ArrayList<ScheduleItem>();
        for(ScheduleItem i : items){
            List<String> daysList = Arrays.asList(i.days);
            if(daysList.contains("Monday")){
                monItems.add(i);
            }
            if(daysList.contains("Tuesday")){
                tuesItems.add(i);
            }
            if(daysList.contains("Wednesday")){
                wedItems.add(i);
            }
            if(daysList.contains("Thursday")){
                thursItems.add(i);
            }
            if(daysList.contains("Friday")){
                friItems.add(i);
            }
            if(daysList.contains("Saturday")){
                satItems.add(i);
            }
            if(daysList.contains("Sunday")){
                sunItems.add(i);
            }
        }
        Collections.sort(monItems, new ScheduleItemComparator());
        Collections.sort(tuesItems, new ScheduleItemComparator());
        Collections.sort(wedItems, new ScheduleItemComparator());
        Collections.sort(thursItems, new ScheduleItemComparator());
        Collections.sort(friItems, new ScheduleItemComparator());
        Collections.sort(satItems, new ScheduleItemComparator());
        Collections.sort(sunItems, new ScheduleItemComparator());
        this.dayHeaders.add("Monday");
        this.itemsMap.put("Monday", monItems);
        this.dayHeaders.add("Tuesday");
        this.itemsMap.put("Tuesday", tuesItems);
        this.dayHeaders.add("Wednesday");
        this.itemsMap.put("Wednesday", wedItems);
        this.dayHeaders.add("Thursday");
        this.itemsMap.put("Thursday", thursItems);
        this.dayHeaders.add("Friday");
        this.itemsMap.put("Friday", friItems);
        this.dayHeaders.add("Saturday");
        this.itemsMap.put("Saturday", satItems);
        this.dayHeaders.add("Sunday");
        this.itemsMap.put("Sunday", sunItems);
        mListener = listener;
    }

    /**
     * Compares Schedule Item's based on time (24 hour clock)
     */
    public class ScheduleItemComparator implements Comparator<ScheduleItem> {
        public int compare(ScheduleItem a, ScheduleItem b){
            int aTime = a.time;
            int bTime = b.time;
            if(aTime < bTime){
                return -1;
            } else if (aTime > bTime) {
                return 1;
            } else {
                return 0;
            }
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView topView;
        public final TextView bottomView;
        public ScheduleItem mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            topView = (TextView) view.findViewById(R.id.topView);
            bottomView = (TextView) view.findViewById(R.id.bottomView);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + topView.getText() + "'";
        }
    }

    /* HEADER STUFF */
    public class HeaderViewHolder extends RecyclerView.ViewHolder {
        public TextView header;
        public HeaderViewHolder(View view) {
            super(view);
            header = (TextView) view.findViewById(R.id.header);
        }
    }

    public class FooterViewHolder extends RecyclerView.ViewHolder {
        public FooterViewHolder(View view) {
            super(view);
        }
    }

    @Override
    protected int getSectionCount() {
        return this.dayHeaders.size();
    }

    @Override
    protected int getItemCountForSection(int section) {
        return this.itemsMap.get(this.dayHeaders.get(section)).size();
    }

    @Override
    protected boolean hasFooterInSection(int section) {
        return false;
    }

    @Override
    protected HeaderViewHolder onCreateSectionHeaderViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.schedule_section_header, parent, false);
        return new HeaderViewHolder(view);
    }

    @Override
    protected FooterViewHolder onCreateSectionFooterViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    protected ViewHolder onCreateItemViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_meet_up, parent, false);
        return new ViewHolder(view);
    }

    @Override
    protected void onBindSectionHeaderViewHolder(HeaderViewHolder holder, int section) {
        holder.header.setText(this.dayHeaders.get(section));
    }

    @Override
    protected void onBindSectionFooterViewHolder(FooterViewHolder holder, int section) {

    }

    @Override
    protected void onBindItemViewHolder(ViewHolder holder, int section, int position) {
        holder.mItem = this.itemsMap.get(this.dayHeaders.get(section)).get(position);
        /* Converting from 24hr time */
        int time;
        String timeString;
        if(holder.mItem.time >= 1300){
            time = holder.mItem.time - 1200;
        } else {
            time = holder.mItem.time;
        }
        timeString = "" + time;
        if(time < 1000){
            timeString = timeString.substring(0,1) + ":" + timeString.substring(1,3);
        } else {
            timeString = timeString.substring(0, 2) + ":" + timeString.substring(2, 4);
        }
        String topLine = holder.mItem.course + " " + timeString + " " + holder.mItem.ampm;
        holder.topView.setText(topLine);
        holder.bottomView.setText(holder.mItem.description);

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.mItem);
                }
            }
        });
    }
}
