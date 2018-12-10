package com.jd.justindowty.gpsapp;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.jd.justindowty.gpsapp.MeetUpsFragment.OnListFragmentInteractionListener;
import com.jd.justindowty.gpsapp.dummy.MeetUpContent.MeetUpItem;
import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link MeetUpItem} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 */
public class MeetUpViewAdapter extends RecyclerView.Adapter<MeetUpViewAdapter.ViewHolder> {

    private final List<MeetUpItem> mValues;
    private final OnListFragmentInteractionListener mListener;

    public MeetUpViewAdapter(List<MeetUpItem> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_meet_up, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
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

        String topLine = holder.mItem.course + " " + timeString + " " + holder.mItem.ampm + " - " + holder.mItem.username;
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

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView topView;
        public final TextView bottomView;
        public MeetUpItem mItem;

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
}
