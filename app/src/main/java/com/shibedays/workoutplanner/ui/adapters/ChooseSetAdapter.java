package com.shibedays.workoutplanner.ui.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;


import com.shibedays.workoutplanner.BaseApp;
import com.shibedays.workoutplanner.R;
import com.shibedays.workoutplanner.db.entities.Set;

import java.util.List;
import java.util.Locale;

public class ChooseSetAdapter extends RecyclerView.Adapter<ChooseSetAdapter.ChooseSetHolder>{

    //region CONSTANTS
    private static final String DEBUG_TAG = ChooseSetAdapter.class.getSimpleName();
    private static final String PACKAGE = "com.shibedays.workoutplanner.ui.adapters.ChooseSetAdapter.";
    //endregion

    //region VIEW_HOLDER
    class ChooseSetHolder extends RecyclerView.ViewHolder {

        // Data
        private Set currentSet;
        // Boolean
        // UI
        private CardView mCardView;
        private CheckBox mCheckBox;
        private TextView mTextViewName;
        private TextView mTextViewTime;

        ChooseSetHolder(View itemView) {
            super(itemView);

            mCheckBox = itemView.findViewById(R.id.choose_set_check);
            mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                }
            });
            mTextViewName = itemView.findViewById(R.id.choose_set_name);
            mTextViewTime = itemView.findViewById(R.id.choose_set_time);
            mTextViewTime.setVisibility(View.GONE);

            mCardView = itemView.findViewById(R.id.set_narrow_card);

        }

        void bindTo(final Set curSet){
            if(curSet == null){
                Log.d(DEBUG_TAG, "shit");
            }
            currentSet = curSet;

            //mCheckBox.setChecked(mCheckedMap.get(currentSet));
            mTextViewName.setText(currentSet.getName());

            int[] time = BaseApp.convertFromMillis(currentSet.getTime());
            int minutes = time[0], seconds = time[1];
            if(seconds == 0){
                mTextViewTime.setText(String.format(Locale.US, "%d:%d%d", minutes, seconds, 0));
            } else if( seconds < 10){
                mTextViewTime.setText(String.format(Locale.US, "%d:%d%d", minutes, 0, seconds));
            }
            else {
                mTextViewTime.setText(String.format(Locale.US, "%d:%d", minutes, seconds));
            }

            mCardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mCheckBox.toggle();
                }
            });

            mCardView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if(currentSet.getSetType() > 0){
                        mListener.openDisplayInfo(currentSet.getSetId());
                    } else {
                        mListener.openBottomSheet(currentSet.getSetId());
                    }
                    return false;
                }
            });
        }

        void bindHeader(){
            mCheckBox.setVisibility(View.GONE);
            mTextViewName.setText("Add Custom Set");

            mTextViewTime.setVisibility(View.GONE);

            mCardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.createSet();
                }
            });
        }
    }
    //endregion

    //region PRIVATE_VARS
    // Data
    private List<Set> mSetData;
    // UI
    private Context mContext;
    // FLAGS
    private boolean mHeader;
    //endregion

    //region INTERFACES
    public interface ChooseSetListener{
        void createSet();
        void openBottomSheet(int setID);
        void openDisplayInfo(int setID);
    }
    private ChooseSetListener mListener;
    //endregion

    //region LIFECYCLE
    public ChooseSetAdapter(Context context, boolean header, ChooseSetListener listener){
        mContext = context;
        mListener = listener;
        mHeader = header;
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
            holder.bindHeader();
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

    public List<Set> getMappedSets(){
        return null;
    }
    //endregion

}
