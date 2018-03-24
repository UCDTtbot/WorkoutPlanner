package com.shibedays.workoutplanner.ui.adapters;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.shibedays.workoutplanner.R;
import com.shibedays.workoutplanner.db.entities.Set;
import com.shibedays.workoutplanner.ui.MainActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


public class SetAdapter extends RecyclerView.Adapter<SetAdapter.ViewHolder> {

    // Constants
    private static final int PENDING_REMOVAL_TIMEOUT = 4000; // LENGTH_LONG is defined as 3500, so lets put 4000 just in case
    private static final String DEBUG_TAG = SetAdapter.class.getSimpleName();
    private static final String PACKAGE = "com.shibedays.workoutplanner.ui.adapters.SetAdapter.";

    //region PRIVATE_VARS
    // Data
    private List<Set> mSetData;
    private List<Set> mSetsPendingRemoval;
    // UI Components
    private CoordinatorLayout mCoordLayout;
    private Context mContext;
    // Threading Components
    private Handler handler = new Handler();
    private HashMap<Set, Runnable> pendingRunnables = new HashMap<>();
    //endregion

    //region INTERFACES
    public interface SetAdapaterListener{
        public void deleteSet(Set set);
    }
    private SetAdapaterListener listener;
    //endregion

    //region VIEW_HOLDER
    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        private TextView setNameTextView;
        private TextView descripTextView;
        private TextView timeTextView;
        private Set curSet;

        public ViewHolder(View itemView) {
            super(itemView);
            //Initialize the views for the RecyclerView

            setNameTextView = itemView.findViewById(R.id.set_name);
            descripTextView = itemView.findViewById(R.id.set_descrip);
            timeTextView = itemView.findViewById(R.id.set_time);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        void bindTo(Set curSet){
            //Populate data when they bind the workouts to the view holder
            setNameTextView.setText(curSet.getName());
            descripTextView.setText(curSet.getDescrip());
            int[] time = MainActivity.convertFromMillis(curSet.getTime());
            int minutes = time[0], seconds = time[1];
            if(seconds == 0){
                timeTextView.setText(String.format(Locale.US, "%d:%d%d", minutes, seconds, 0));
            } else if( seconds < 10){
                timeTextView.setText(String.format(Locale.US, "%d:%d%d", minutes, 0, seconds));
            }
            else {
                timeTextView.setText(String.format(Locale.US, "%d:%d", minutes, seconds));
            }


            this.curSet = curSet;
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

    //region LIFECYCLE
    public SetAdapter(Context context, View coordLayout){
        mSetsPendingRemoval = new ArrayList<>();
        mContext = context;
        if(coordLayout instanceof CoordinatorLayout){
            mCoordLayout = (CoordinatorLayout)coordLayout;
        } else {
            Log.e(DEBUG_TAG, "THE VIEW PASSED WAS NOT A COORDINATOR LAYOUT");
        }
        Activity activity = null;
        if(context instanceof Activity){
            activity = (Activity) context;
            try{
                listener = (SetAdapter.SetAdapaterListener) activity;
            } catch (ClassCastException e){
                Log.e(DEBUG_TAG, "ERROR IN SET ADAPTER LISTENER: " + e.getMessage());
            }
        }
    }

    @Override
    public SetAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext)
                .inflate(R.layout.set_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(SetAdapter.ViewHolder holder, int position) {
        // Get the current data
        Set currentSet = mSetData.get(position);
        // Bind to the data for the views
        holder.bindTo(currentSet);
        // Populate anymore data
    }
    //endregion
    // TODO: setup the timeTextView to display correctly

    //region UTILITY

    @Override
    public int getItemCount() {
        if(mSetData != null) {
            return mSetData.size();
        } else {
            return 0;
        }
    }

    public void setData(List<Set> data){
        mSetData = data;
        if(mSetsPendingRemoval.size() > 0){
            for(int i = 0; i < mSetsPendingRemoval.size(); i++){
                Set set = mSetsPendingRemoval.get(i);
                if (mSetData.contains(set)){
                    mSetData.remove(set);
                }
            }
        }
        notifyDataSetChanged();
    }
    //endregion

    //region PENDING_DELETE
    public void pendingRemoval(final int swipedPos){
        final Set set = mSetData.get(swipedPos);
        if(!mSetsPendingRemoval.contains(set)){
            mSetsPendingRemoval.add(set);
            final int pendingPos = mSetsPendingRemoval.indexOf(set);
            mSetData.remove(swipedPos);
            notifyItemRemoved(swipedPos);
            notifyItemRangeChanged(swipedPos, mSetData.size());
            Runnable pendingRemovalRunnable = new Runnable() {
                @Override
                public void run() {
                    deletePending(mSetsPendingRemoval.indexOf(set), set);
                }
            };
            handler.postDelayed(pendingRemovalRunnable, PENDING_REMOVAL_TIMEOUT);
            pendingRunnables.put(set, pendingRemovalRunnable);

            Snackbar undoBar = Snackbar.make(mCoordLayout, "Undo", Snackbar.LENGTH_LONG);
            undoBar.setAction("Undo", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    undoItem(swipedPos, pendingPos);
                }
            });
            undoBar.show();
        }
    }

    private void deletePending(int pendingIndex, Set originalSet){
        mSetsPendingRemoval.remove(pendingIndex);

        listener.deleteSet(originalSet);
        Log.d(DEBUG_TAG, "Removed pending set");
    }

    public boolean isPendingRemoval(int pos){
        Set set = mSetData.get(pos);
        return mSetsPendingRemoval.contains(set);
    }
    private void undoItem(int itemPos, int pendingPos){
        if(mSetsPendingRemoval.get(pendingPos) != null){
            Set set = mSetsPendingRemoval.get(pendingPos);
            Runnable pendingRunnable = pendingRunnables.get(set);
            pendingRunnables.remove(set);
            if(pendingRunnable != null){
                handler.removeCallbacks(pendingRunnable);
            }
            mSetsPendingRemoval.remove(pendingPos);
            mSetData.add(itemPos, set);
            notifyItemInserted(itemPos);
        }
    }
    //endregion


}
