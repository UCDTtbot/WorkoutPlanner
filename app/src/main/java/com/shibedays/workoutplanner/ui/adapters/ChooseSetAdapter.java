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


import com.bumptech.glide.Glide;
import com.shibedays.workoutplanner.BaseApp;
import com.shibedays.workoutplanner.R;
import com.shibedays.workoutplanner.db.entities.Set;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class ChooseSetAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    //region CONSTANTS
    private static final String DEBUG_TAG = ChooseSetAdapter.class.getSimpleName();
    private static final String PACKAGE = "com.shibedays.workoutplanner.ui.adapters.ChooseSetAdapter.";

    private static final int HEADER = -1;
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
                    mCheckedMap.put(currentSet, b);
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

            if(mCheckedMap.containsKey(currentSet)){
                mCheckBox.setChecked(mCheckedMap.get(currentSet));
            } else {
                mCheckBox.setChecked(false);
            }
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
                    if(currentSet.getSetType() != Set.USER_CREATED){
                        mListener.openDisplayInfo(currentSet.getSetId(), currentSet.getSetType());
                    } else {
                        mListener.openBottomSheet(currentSet.getSetId(), currentSet.getSetType());
                    }
                    return false;
                }
            });
        }
    }

    class HeaderViewHolder extends RecyclerView.ViewHolder {

        // UI
        private CardView mCardView;
        private CheckBox mCheckBox;
        private TextView mTextViewName;
        private TextView mTextViewTime;

        public HeaderViewHolder(View itemView) {
            super(itemView);

            mCheckBox = itemView.findViewById(R.id.choose_set_check);
            mTextViewName = itemView.findViewById(R.id.choose_set_name);
            mTextViewTime = itemView.findViewById(R.id.choose_set_time);

            mCardView = itemView.findViewById(R.id.set_narrow_card);
        }


        void bindTo(){
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
    private int mType;
    // Map
    private HashMap<Set, Boolean> mCheckedMap;
    //endregion

    //region INTERFACES
    public interface ChooseSetListener{
        void createSet();
        void openBottomSheet(long setID, int type);
        void openDisplayInfo(long setID, int type);
    }
    private ChooseSetListener mListener;
    //endregion

    //region LIFECYCLE
    public ChooseSetAdapter(Context context, int type, ChooseSetListener listener){
        mContext = context;
        mListener = listener;
        mType = type;
        mCheckedMap = new HashMap<>();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if(viewType == HEADER && mType == Set.USER_CREATED){
            return new HeaderViewHolder(LayoutInflater.from(mContext)
                    .inflate(R.layout.list_choose_set_item, parent, false));
        }
        return new ChooseSetHolder(LayoutInflater.from(mContext)
                .inflate(R.layout.list_choose_set_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        // Get the current data
        try{
            if(holder instanceof ChooseSetHolder){
                ChooseSetHolder vh = (ChooseSetHolder) holder;
                if(mSetData.size() > 0) {
                    if (mSetData.get(0).getSetType() == Set.USER_CREATED) {
                        vh.bindTo(mSetData.get(position - 1));
                    } else {
                        vh.bindTo(mSetData.get(position));
                    }
                }
            } else if (holder instanceof HeaderViewHolder) {
                HeaderViewHolder vh = (HeaderViewHolder) holder;
                vh.bindTo();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //endregion

    //region UTILITY
    @Override
    public int getItemCount() {
        if(mSetData == null){
            return 0;
        } else if (mSetData.size() == 0) {
            return 1;
        } else if (mType == Set.USER_CREATED) {
            return mSetData.size() + 1;
        } else {
            return mSetData.size();
        }
    }

    @Override
    public int getItemViewType(int position) {
        if(position == 0 && mType == Set.USER_CREATED) {
            return HEADER;
        }

        return super.getItemViewType(position);
    }

    public void updateData(List<Set> data){
        mSetData = data;
        for(Set s : mSetData){
            if(!mCheckedMap.containsKey(s)){
                mCheckedMap.put(s, false);
            }
        }
        notifyDataSetChanged();
    }

    public void updateSet(Set set){
        if(mSetData.contains(set)) {
            mSetData.set(mSetData.indexOf(set), set);
            notifyItemChanged(mSetData.indexOf(set));
        }
    }

    public void removeSet(Set set){
        mCheckedMap.remove(set);
        int i = mSetData.indexOf(set);
        mSetData.remove(set);
        notifyItemChanged(i);
    }

    public List<Long> getMappedSets(){
        List<Long> selectedSets = new ArrayList<>();
        for(Set s : mCheckedMap.keySet()){
            if(mCheckedMap.get(s))
                selectedSets.add(s.getSetId());
        }
        return selectedSets;
    }
    //endregion

}
