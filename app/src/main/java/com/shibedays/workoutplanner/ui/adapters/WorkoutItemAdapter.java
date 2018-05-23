package com.shibedays.workoutplanner.ui.adapters;

import android.arch.persistence.room.PrimaryKey;
import android.content.Context;
import android.media.Image;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.shibedays.workoutplanner.R;
import com.shibedays.workoutplanner.db.entities.Workout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


public class WorkoutItemAdapter extends PendingRemovalAdapter<RecyclerView.ViewHolder> {

    //region CONSTANTS
    // Timeout Constant
    private static final int PENDING_REMOVAL_TIMEOUT = 4000; // LENGTH_LONG is defined as 3500, so lets put 4000 just in case
    // Package and Debug Constants
    private static final String DEBUG_TAG = WorkoutItemAdapter.class.getSimpleName();
    private static final String PACKAGE = "com.shibedays.workoutplanner.ui.adapters.WorkoutItemAdapter.";

    private static int FOOTER = -1;
    //endregion

    //region VIEW_HOLDER
    class WorkoutViewHolder extends RecyclerView.ViewHolder {
        // Data
        Workout curWorkout;
        // Foreground
        TextView itemName;
        ImageView itemImage;
        TextView itemDescrip;

        private WorkoutViewHolder(View itemView) {
            super(itemView);
            //Initialize the views for the RecyclerView
            itemName = itemView.findViewById(R.id.workout_name);
            itemImage = itemView.findViewById(R.id.workout_image);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onWorkoutClicked(curWorkout.getWorkoutID(), curWorkout.getWorkoutType());

                }
            });
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    mListener.onWorkoutLongClick(mWorkoutData.indexOf(curWorkout), curWorkout.getWorkoutID(), curWorkout.getWorkoutType());
                    return true;
                }
            });
        }
    }

    class FooterViewHolder extends RecyclerView.ViewHolder {

        // UI
        TextView footerName;
        ImageView footerImage;

        private FooterViewHolder(View itemView) {
            super(itemView);

            footerName = itemView.findViewById(R.id.workout_name);
            footerImage = itemView.findViewById(R.id.workout_image);

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
    private List<Workout> mWorkoutsPendingRemoval;
    private int mType;
    // UI Components
    private CoordinatorLayout mCoordLayout;
    private Context mContext;
    // Threading Components
    private Handler handler = new Handler(); // Handler for running async delayed tasks
    private HashMap<Workout, Runnable> pendingRunnables = new HashMap<>(); // Map of the items to their async runnable rasks



    //endregion

    //region INTERFACES
    public interface WorkoutAdapterListener{
        void onWorkoutClicked(int workoutIndex, int type);
        void onWorkoutLongClick(int workoutIndex, int workoutID, int type);
        void deleteFromDB(Workout workout);
    }
    private WorkoutAdapterListener mListener;
    //endregion

    //region LIFECYCLE
    public WorkoutItemAdapter(Context context, View coordLayout, List<Workout> workouts, int type, WorkoutAdapterListener listener){
        mWorkoutsPendingRemoval = new ArrayList<>();
        mContext = context;
        mType = type;
        if(coordLayout instanceof CoordinatorLayout){
            mCoordLayout = (CoordinatorLayout) coordLayout;
        }else{
            throw new RuntimeException(WorkoutItemAdapter.class.getSimpleName() + " was passed a non-coord layout view");
        }        // Make sure our context is an activity and set the Listener to it

        setData(workouts);
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
            } else if (viewHolder instanceof FooterViewHolder) {
                FooterViewHolder vh = (FooterViewHolder) viewHolder;
                Glide.with(mContext).load(R.drawable.ic_add_black_24dp).into(vh.footerImage);
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

    public void setData(List<Workout> workouts){
        mWorkoutData = workouts;
        if(mWorkoutsPendingRemoval.size() > 0){
            for(int i = 0; i < mWorkoutsPendingRemoval.size(); i++){
                Workout workout = mWorkoutsPendingRemoval.get(i);
                if (mWorkoutData.contains(workout)){
                    mWorkoutData.remove(workout);
                }
            }
        }
        notifyDataSetChanged();
    }
    //endregion

    //region PENDING_DELETE
    @Override
    public void pendingRemoval(final int index){
        final Workout workout = mWorkoutData.get(index);
        if(!mWorkoutsPendingRemoval.contains(workout)){
            mWorkoutsPendingRemoval.add(workout);
            final int pendingPos = mWorkoutsPendingRemoval.indexOf(workout);
            mWorkoutData.remove(index);
            notifyItemRemoved(index);
            notifyItemRangeChanged(index, mWorkoutData.size());
            Runnable pendingRemovalRunnable = new Runnable(){
                @Override
                public void run() {
                    deletePending(mWorkoutsPendingRemoval.indexOf(workout), index);
                }
            };
            handler.postDelayed(pendingRemovalRunnable, PENDING_REMOVAL_TIMEOUT);
            pendingRunnables.put(workout, pendingRemovalRunnable);

            Snackbar undoBar = Snackbar.make(mCoordLayout, "Undo", Snackbar.LENGTH_LONG);
            undoBar.setAction("Undo", new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    undo(index, pendingPos);
                }
            });
            undoBar.show();

        }
    }

    @Override
    public void deletePending(int pendingIndex, int origWorkoutPos){
        mListener.deleteFromDB(mWorkoutsPendingRemoval.get(pendingIndex));
        mWorkoutsPendingRemoval.remove(pendingIndex);
        // Update the file that we have removed a curWorkout
    }

    @Override
    public boolean isPendingRemoval(int pos){
        Workout workout = mWorkoutData.get(pos);
        return mWorkoutsPendingRemoval.contains(workout);
    }

    // Function for re-adding a pending item using it's orig position number
    @Override
    public void undo(int itemPos, int pendingPos) {
        // Stop the relevant runnable
        // Remove item from mWorkoutsPendingRemoval
        // Re-add item to mWorkoutData at correct pos
        if(mWorkoutsPendingRemoval.get(pendingPos) != null) {
            Workout workout = mWorkoutsPendingRemoval.get(pendingPos);
            Runnable pendingRunnable = pendingRunnables.get(workout);
            pendingRunnables.remove(workout);
            if (pendingRunnable != null) {
                handler.removeCallbacks(pendingRunnable);
            }
            mWorkoutsPendingRemoval.remove(pendingPos);
            mWorkoutData.add(itemPos, workout);
            notifyItemInserted(itemPos);
        }
    }

    //endregion

}