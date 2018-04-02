package com.shibedays.workoutplanner.ui.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.afollestad.sectionedrecyclerview.SectionedViewHolder;
import com.shibedays.workoutplanner.R;
import com.shibedays.workoutplanner.db.entities.Workout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


public class WorkoutAdapter extends PendingRemovalAdapter<WorkoutAdapter.WorkoutViewHolder> implements ListItemTouchHelper.UnderlayButtonClickListener {

    //region CONSTANTS
    // Timeout Constant
    private static final int PENDING_REMOVAL_TIMEOUT = 4000; // LENGTH_LONG is defined as 3500, so lets put 4000 just in case
    // Package and Debug Constants
    private static final String DEBUG_TAG = WorkoutAdapter.class.getSimpleName();
    private static final String PACKAGE = "com.shibedays.workoutplanner.ui.adapters.WorkoutAdapter.";

    private static final int NUM_SECIONS = 2;
    private static final int FAVORITE = 0;
    private static final int NORMAL = 1;


    //endregion

    //region VIEW_HOLDER
    public class WorkoutViewHolder extends SectionedViewHolder implements View.OnClickListener, View.OnLongClickListener, CompoundButton.OnCheckedChangeListener {

        // Data
        private Workout curWorkout;
        private int absolutePos;
        // Header
        private ImageView caret;
        private TextView title;
        // Item
        private TextView itemName;
        private TextView sets;
        private ToggleButton favStar;

        //TEMP
        private TextView TEST_POS_ID;
        private TextView TEST_WRK_ID;
        // Adapter
        private WorkoutAdapter adapter;

        private boolean onBind;

        private WorkoutViewHolder(View itemView, WorkoutAdapter adapter) {
            super(itemView);
            //Initialize the views for the RecyclerView
            itemName = itemView.findViewById(R.id.item_name);
            sets = itemView.findViewById(R.id.item_sets);
            caret = itemView.findViewById(R.id.caret);
            title = itemView.findViewById(R.id.header_title);
            favStar = itemView.findViewById(R.id.workout_favorite);
            this.adapter = adapter;

            itemView.setOnClickListener(this);

            TEST_POS_ID = itemView.findViewById(R.id.TEST_POS_ID);
            TEST_WRK_ID = itemView.findViewById(R.id.TEST_WRK_ID);
        }

        void bindTo(final Workout curWorkout, final int absolute){
            onBind = true;

            //Populate data when they bind the workouts to the view holder
            this.curWorkout = curWorkout;
            absolutePos = absolute;

            itemName.setText(curWorkout.getName());
            sets.setText(String.format(mContext.getString(R.string.item_sets), curWorkout.getNumOfSets()));
            favStar.setTextOff("");
            favStar.setTextOn("");

            if(curWorkout.getIsFavorite()){
                favStar.setBackgroundDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_star_gold_24dp));
                favStar.setChecked(true);
            } else {
                favStar.setBackgroundDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_star_border_black_24dp));
                favStar.setChecked(false);
            }
            favStar.setOnCheckedChangeListener(this);

            TEST_POS_ID.setText(String.format(Locale.US, "AbsPos: %1$d", absolute));
            TEST_WRK_ID.setText(String.format(Locale.US, "WrkID: %1$d", curWorkout.getWorkoutID()));
            onBind = false;
        }

        @Override
        public void onClick(View v) {

            if(isHeader()){
                adapter.toggleSectionExpanded(getRelativePosition().section());
            } else {
                mListener.onWorkoutClicked(curWorkout.getWorkoutID());
            }
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if(!onBind) {
                if (isChecked) {
                    favStar.setBackgroundDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_star_gold_24dp));
                    curWorkout.setIsFavorite(true);
                    toggleFav(curWorkout, absolutePos);
                } else {
                    favStar.setBackgroundDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_star_border_black_24dp));
                    curWorkout.setIsFavorite(false);
                    toggleFav(curWorkout, absolutePos);
                }
            }
        }

        @Override
        public boolean onLongClick(View view) {
            mListener.onWorkoutLongClick(mAllWorkoutData.indexOf(curWorkout), curWorkout.getWorkoutID());
            // return true to indicate the click was handled
            return true;
        }

         public Workout getWorkout(){
            return curWorkout;
        }
    }
    //endregion

    //region PRIVATE_VARS
    // Data
    private List<Workout> mAllWorkoutData;
    private List<Workout> mFavorite;
    private List<Workout> mNonFavorite;
    private List<Workout> mWorkoutsPendingRemoval;
    // UI Components
    private CoordinatorLayout mCoordLayout;
    private Context mContext;
    // Threading Components
    private Handler handler = new Handler(); // Handler for running async delayed tasks
    private HashMap<Workout, Runnable> pendingRunnables = new HashMap<>(); // Map of the items to their async runnable rasks



    //endregion

    //region INTERFACES
    public interface WorkoutAdapterListener{
        void onWorkoutClicked(int workoutIndex);
        void onWorkoutLongClick(int workoutIndex, int workoutID);
        void deleteWorkout(Workout workout);
    }
    private WorkoutAdapterListener mListener;
    //endregion

    //region LIFECYCLE
    public WorkoutAdapter(Context context, View coordLayout){
        mWorkoutsPendingRemoval = new ArrayList<>();
        mContext = context;
        mAllWorkoutData = new ArrayList<>();
        mFavorite = new ArrayList<>();
        mNonFavorite = new ArrayList<>();
        if(coordLayout instanceof CoordinatorLayout){
            mCoordLayout = (CoordinatorLayout) coordLayout;
        }else{
            throw new RuntimeException(WorkoutAdapter.class.getSimpleName() + " was passed a non-coord layout view");
        }        // Make sure our context is an activity and set the Listener to it

        if(context instanceof WorkoutAdapterListener) {
            mListener = (WorkoutAdapterListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement WorkoutAdapterListener");
        }

    }

    @NonNull
    @Override
    public WorkoutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layout = 0;
        switch (viewType){
            case VIEW_TYPE_HEADER:
                layout = R.layout.list_item_favorite_header;
                break;
            case VIEW_TYPE_ITEM:
                layout = R.layout.list_workout_items;
                break;
            case VIEW_TYPE_FOOTER:
                break;
            default:
                layout = R.layout.list_workout_items;
                break;
        }
        View v = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        return new WorkoutViewHolder(v, this);
    }


    //endregion

    @Override
    public int getSectionCount() {
        return NUM_SECIONS;
    }

    @Override
    public int getItemCount(int section) {
        if(mAllWorkoutData == null){
            return 0;
        } else if(section == FAVORITE) {
            return mFavorite.size();
        } else if(section == NORMAL) {
            return mNonFavorite.size();
        } else {
            return 0;
        }
    }

    @Override
    public void onBindHeaderViewHolder(WorkoutViewHolder holder, int section, boolean expanded) {
        if(section == FAVORITE) {
            holder.title.setText(String.format(Locale.US, "Favorites %d", section));
            holder.caret.setImageResource(expanded ? R.drawable.ic_down_arrow_black_24dp : R.drawable.ic_right_arrow_24dp);
        } else if (section == NORMAL){
            holder.title.setText(String.format(Locale.US, "Workouts %d", section));
            holder.caret.setImageResource(expanded ? R.drawable.ic_down_arrow_black_24dp : R.drawable.ic_right_arrow_24dp);
        }
    }

    @Override
    public void onBindFooterViewHolder(WorkoutViewHolder holder, int section) {

    }

    @Override
    public void onBindViewHolder(WorkoutViewHolder holder, int section, int relativePosition, int absolutePosition) {
        // Get the current data
        Workout currentWorkout = null;
        switch(section){
            case FAVORITE:
                currentWorkout = mFavorite.get(relativePosition);
                break;
            case NORMAL:
                currentWorkout = mNonFavorite.get(relativePosition);
                break;
            default:
                throw new RuntimeException(DEBUG_TAG + "workout doesn't exist in mFavorite or mNonFavorite");
        }
        //Workout currentWorkout = mAllWorkoutData.get(absolutePosition);
        // Bind to the correct data
        holder.bindTo(currentWorkout, absolutePosition);
    }

    //region UTILITY

    public void addData(Workout workout){
        mAllWorkoutData.add(workout);
        if(workout.getIsFavorite()) {
            mFavorite.add(workout);
        } else {
            mNonFavorite.add(workout);
        }
        if(mWorkoutsPendingRemoval.size() > 0){
            for(int i = 0; i < mWorkoutsPendingRemoval.size(); i++){
                Workout w = mWorkoutsPendingRemoval.get(i);
                if (mAllWorkoutData.contains(w)){
                    mAllWorkoutData.remove(w);
                }
            }
        }
        notifyDataSetChanged();
    }

    private void toggleFav(Workout wrk, int absolute){
        boolean isFav = wrk.getIsFavorite();
        if(isFav){ // became fav
            if(mNonFavorite.contains(wrk)) {
                mNonFavorite.remove(wrk);
                mFavorite.add(wrk);
                notifyItemChanged(absolute);
            } else {
                throw new RuntimeException(DEBUG_TAG + wrk.getName() + " wasn't in the normal list");
            }
        } else {
            if(mFavorite.contains(wrk)){
                mNonFavorite.add(wrk);
                mFavorite.remove(wrk);
                notifyItemChanged(absolute);
            } else {
                throw new RuntimeException(DEBUG_TAG + wrk.getName() + " wasn't in the fav list");
            }
        }
    }
    //endregion

    //region PENDING_DELETE
    @Override
    public void onDeleteButtonClick(int absolutePos) {

    }

    @Override
    public void pendingRemoval(final int swipedPos){
        final Workout workout = mAllWorkoutData.get(swipedPos);
        if(!mWorkoutsPendingRemoval.contains(workout)){
            mWorkoutsPendingRemoval.add(workout);
            final int pendingPos = mWorkoutsPendingRemoval.indexOf(workout);
            mAllWorkoutData.remove(swipedPos);
            notifyItemRemoved(swipedPos);
            notifyItemRangeChanged(swipedPos, mAllWorkoutData.size());
            Runnable pendingRemovalRunnable = new Runnable(){
                @Override
                public void run() {
                    deletePending(mWorkoutsPendingRemoval.indexOf(workout), swipedPos);
                }
            };
            handler.postDelayed(pendingRemovalRunnable, PENDING_REMOVAL_TIMEOUT);
            pendingRunnables.put(workout, pendingRemovalRunnable);

            Snackbar undoBar = Snackbar.make(mCoordLayout, "Undo", Snackbar.LENGTH_LONG);
            undoBar.setAction("Undo", new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    undo(swipedPos, pendingPos);
                }
            });
            undoBar.show();

        }
    }

    @Override
    public void deletePending(int pendingIndex, int origWorkoutPos){
        mWorkoutsPendingRemoval.remove(pendingIndex);

        mListener.deleteWorkout(mAllWorkoutData.get(origWorkoutPos));
        // Update the file that we have removed a curWorkout
    }

    @Override
    public boolean isPendingRemoval(int pos){
        Workout workout = mAllWorkoutData.get(pos);
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
            mAllWorkoutData.add(itemPos, workout);
            notifyItemInserted(itemPos);
        }
    }

    //endregion

}
