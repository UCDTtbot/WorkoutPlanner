package com.shibedays.workoutplanner.ui.adapters;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.shibedays.workoutplanner.BaseApp;
import com.shibedays.workoutplanner.R;
import com.shibedays.workoutplanner.db.entities.Workout;
import com.shibedays.workoutplanner.ui.helpers.PendingRemovalHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


public class WorkoutItemHelper extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    //region CONSTANTS

    // Package and Debug Constants
    private static final String DEBUG_TAG = WorkoutItemHelper.class.getSimpleName();
    private static final String PACKAGE = "com.shibedays.workoutplanner.ui.adapters.WorkoutItemHelper.";

    private static int FOOTER = -1;
    //endregion

    //region VIEW_HOLDER
    class WorkoutViewHolder extends RecyclerView.ViewHolder {
        // Data
        Workout curWorkout;
        // Foreground
        TextView itemName;
        ImageView itemImage;
        TextView numSets;
        TextView totalTime;
        ImageView equipment;

        private WorkoutViewHolder(View itemView) {
            super(itemView);
            //Initialize the views for the RecyclerView
            itemName = itemView.findViewById(R.id.workout_name);
            itemImage = itemView.findViewById(R.id.workout_image);
            numSets = itemView.findViewById(R.id.workout_num_sets);
            totalTime = itemView.findViewById(R.id.workout_time);
            equipment = itemView.findViewById(R.id.equip_required);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onWorkoutClicked(curWorkout.getWorkoutID(), curWorkout.getWorkoutType());

                }
            });
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    mListener.onWorkoutLongClick(curWorkout.getWorkoutID(), curWorkout.getWorkoutType());
                    return true;
                }
            });
        }
    }

    class FooterViewHolder extends RecyclerView.ViewHolder {

        // UI
        TextView footerName;
        ImageView footerImage;
        TextView footerSets;
        TextView footerTime;

        private FooterViewHolder(View itemView) {
            super(itemView);

            footerName = itemView.findViewById(R.id.workout_name);
            footerImage = itemView.findViewById(R.id.workout_image);
            itemView.findViewById(R.id.workout_num_sets_layout).setVisibility(View.INVISIBLE);
            itemView.findViewById(R.id.workout_time_layout).setVisibility(View.INVISIBLE);
            itemView.findViewById(R.id.equip_required).setVisibility(View.INVISIBLE);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onWorkoutClicked(-1, Workout.USER_CREATED);
                }
            });
        }
    }
    //endregion

    //region PRIVATE_VARS
    // Data
    private List<Workout> mWorkoutData;
    private int mType;
    // UI Components
    private Context mContext;

    //endregion

    //region INTERFACES
    public interface WorkoutAdapterListener{
        void onWorkoutClicked(int id, int type);
        void onWorkoutLongClick(int workoutID, int type);
    }
    private WorkoutAdapterListener mListener;
    //endregion

    //region LIFECYCLE
    public WorkoutItemHelper(Context context, View coordLayout, List<Workout> workouts, int type, WorkoutAdapterListener listener){
        mContext = context;
        mType = type;

        mWorkoutData = workouts;
        mListener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == FOOTER && mType == Workout.USER_CREATED){
            return new FooterViewHolder(LayoutInflater.from(mContext)
                .inflate(R.layout.list_workout_items, parent, false));
        }
            return new WorkoutViewHolder(LayoutInflater.from(mContext)
                .inflate(R.layout.list_workout_items, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        // Get the current data
        try{
            if(viewHolder instanceof WorkoutViewHolder){
                WorkoutViewHolder vh = (WorkoutViewHolder) viewHolder;
                vh.curWorkout = mWorkoutData.get(position);
                Glide.with(mContext).load(vh.curWorkout.getWorkoutImageId()).into(vh.itemImage);
                vh.itemName.setText(mWorkoutData.get(position).getName());
                vh.numSets.setText(String.format(Locale.US, "%d", mWorkoutData.get(position).getNumOfSets()));
                vh.totalTime.setText(BaseApp.formatTime(mWorkoutData.get(position).getTotalTime()));
                vh.itemImage.setVisibility(View.GONE);
                if(vh.curWorkout.isEquipmentRequired()){
                    vh.equipment.setVisibility(View.VISIBLE);
                } else {
                    vh.equipment.setVisibility(View.GONE);
                }
            } else if (viewHolder instanceof FooterViewHolder) {
                FooterViewHolder vh = (FooterViewHolder) viewHolder;
                Glide.with(mContext).load(R.drawable.ic_add_black_24dp).into(vh.footerImage);
                if(BaseApp.isDarkTheme()){
                    vh.footerImage.setColorFilter(ContextCompat.getColor(mContext, R.color.colorDarkThemeIco));
                }
                vh.footerName.setText("Add Workout");

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Bind to the correct data
    }

    //endregion

    //region UTILITY
    @Override
    public int getItemCount() {
        if(mWorkoutData == null){
            return 0;
        } else if (mWorkoutData.size() == 0) {
            return 1;
        } else if (mType == Workout.USER_CREATED) {
            return mWorkoutData.size() + 1;
        } else {
            return mWorkoutData.size();
        }
    }

    @Override
    public int getItemViewType(int position) {
        if(position == mWorkoutData.size()) {
            return FOOTER;
        }

        return super.getItemViewType(position);
    }

    public void updateData(List<Workout> workouts){
        mWorkoutData = workouts;
        notifyDataSetChanged();
    }

    public void removeWorkout(Workout w){
        int i = mWorkoutData.indexOf(w);
        mWorkoutData.remove(w);
        notifyItemRemoved(i);
        notifyItemRangeChanged(i, mWorkoutData.size());
    }

    public void addWorkout(Workout w){
        mWorkoutData.add(w);
        notifyItemInserted(mWorkoutData.indexOf(w));
        notifyItemRangeChanged(mWorkoutData.indexOf(w), mWorkoutData.size());
    }
    //endregion


}