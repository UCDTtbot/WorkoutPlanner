package com.shibedays.workoutplanner.ui.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;


import com.shibedays.workoutplanner.R;
import com.shibedays.workoutplanner.db.entities.Set;

import java.util.List;

public class ChooseSetAdapter extends RecyclerView.Adapter<ChooseSetAdapter.ChooseSetHolder>{

    //region CONSTANTS
    private static final String DEBUG_TAG = ChooseSetAdapter.class.getSimpleName();
    private static final String PACKAGE = "com.shibedays.workoutplanner.ui.adapters.ChooseSetAdapter.";
    //endregion

    //region UI
    private CheckBox mCheckBox;
    private TextView mTextViewName;
    private TextView mTextViewTime;
    //endregion

    //region VIEW_HOLDER
    class ChooseSetHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        // Data
        private Set curSet;
        // UI

        ChooseSetHolder(View itemView) {
            super(itemView);
        }

        void bindTo(final Set curSet){
            this.curSet = curSet;
            choose_set_check
            choose_set_name
            choose_set_time
            /*
            if(seconds == 0){
                timeTextView.setText(String.format(Locale.US, "%d:%d%d", minutes, seconds, 0));
            } else if( seconds < 10){
                timeTextView.setText(String.format(Locale.US, "%d:%d%d", minutes, 0, seconds));
            }
            else {
                timeTextView.setText(String.format(Locale.US, "%d:%d", minutes, seconds));
            }
             */
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
    // UI
    private Context mContext;
    //endregion

    //region INTERFACES
    public interface ChooseSetListener{

    }
    private ChooseSetListener mListener;
    //endregion

    //region LIFECYCLE
    public ChooseSetAdapter(Context context, ChooseSetListener listener){
        mContext = context;
        mListener = listener;
    }

    @NonNull
    @Override
    public ChooseSetHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ChooseSetHolder(LayoutInflater.from(mContext)
                    .inflate(R.layout.list_choose_set_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ChooseSetHolder holder, int position) {
        holder.bindTo(mSetData.get(position));
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
