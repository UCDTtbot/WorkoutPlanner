package com.shibedays.workoutplanner.ui.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;


import com.shibedays.workoutplanner.BaseApp;
import com.shibedays.workoutplanner.R;
import com.shibedays.workoutplanner.db.entities.Set;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class ChooseSetAdapter extends RecyclerView.Adapter<ChooseSetAdapter.ChooseSetHolder>{

    //region CONSTANTS
    private static final String DEBUG_TAG = ChooseSetAdapter.class.getSimpleName();
    private static final String PACKAGE = "com.shibedays.workoutplanner.ui.adapters.ChooseSetAdapter.";
    //endregion

    //region VIEW_HOLDER
    class ChooseSetHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        // Data
        private Set curSet;
        // UI
        private CheckBox mCheckBox;
        private TextView mTextViewName;
        private TextView mTextViewTime;

        ChooseSetHolder(View itemView) {
            super(itemView);

            mCheckBox = itemView.findViewById(R.id.choose_set_check);
            mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if(b) {
                        mListener.mapSet(curSet);
                        Log.d(DEBUG_TAG, "Adding " + curSet.getName() + " to set map");
                    } else {
                        mListener.unmapSet(curSet);
                        Log.d(DEBUG_TAG, "Unmapping " + curSet.getName());
                    }
                }
            });
            mTextViewName = itemView.findViewById(R.id.choose_set_name);
            mTextViewTime = itemView.findViewById(R.id.choose_set_time);
        }

        void bindTo(final Set curSet){
            this.curSet = curSet;

            mCheckBox.setChecked(false);
            mTextViewName.setText(curSet.getName());

            int[] time = BaseApp.convertFromMillis(curSet.getTime());
            int minutes = time[0], seconds = time[1];
            if(seconds == 0){
                mTextViewTime.setText(String.format(Locale.US, "%d:%d%d", minutes, seconds, 0));
            } else if( seconds < 10){
                mTextViewTime.setText(String.format(Locale.US, "%d:%d%d", minutes, 0, seconds));
            }
            else {
                mTextViewTime.setText(String.format(Locale.US, "%d:%d", minutes, seconds));
            }

        }

        @Override
        public void onClick(View v) {

        }

        @Override
        public boolean onLongClick(View v) {
            return false;
        }
    }
    //endregion

    //region PRIVATE_VARS
    // Data
    private List<Set> mSetData;
    private List<Set> mSetMap;
    // UI
    private Context mContext;
    // FLAGS
    private boolean mHeader;
    //endregion

    //region INTERFACES
    public interface ChooseSetListener{
        void mapSet(Set set);
        void unmapSet(Set set);
    }
    private ChooseSetListener mListener;
    //endregion

    //region LIFECYCLE
    public ChooseSetAdapter(Context context, boolean header, ChooseSetListener listener){
        mContext = context;
        mListener = listener;
        mSetMap = new ArrayList<>();
        mHeader = header;
        if(mHeader){
            //TODO: Create the special "Add User Set" header
        }
    }

    @NonNull
    @Override
    public ChooseSetHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ChooseSetHolder(LayoutInflater.from(mContext)
                    .inflate(R.layout.list_choose_set_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ChooseSetHolder holder, int position) {
        if(mHeader && position == 0){
            //TODO: Setup the special header and assign on click listener
        } else {
            holder.bindTo(mSetData.get(position));
        }
    }
    //endregion

    //region UTILITY
    @Override
    public int getItemCount() {
        return mSetData != null ? mSetData.size() : 0;
    }

    public void setData(List<Set> data){
        mSetData = data;
        notifyDataSetChanged();
    }

    //endregion

}