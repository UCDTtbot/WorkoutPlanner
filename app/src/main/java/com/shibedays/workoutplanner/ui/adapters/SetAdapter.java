package com.shibedays.workoutplanner.ui.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.shibedays.workoutplanner.R;
import com.shibedays.workoutplanner.db.entities.Set;

import java.util.List;

/**
 * Created by ttbot on 2/11/2018.
 */

public class SetAdapter extends RecyclerView.Adapter<SetAdapter.ViewHolder> {

    private List<Set> mSetData;
    private Context mContext;

    /**
     *
     * @param context
     * @param setData
     */
    public SetAdapter(Context context, List<Set> setData){
            mSetData = setData;
            mContext = context;
            }

    /**
     *
     * @param parent
     * @param viewType
     * @return
     */
    @Override
    public SetAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext)
                .inflate(R.layout.set_list_item, parent, false));
    }

    /**
     *
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(SetAdapter.ViewHolder holder, int position) {
        // Get the current data
        Set currentSet = mSetData.get(position);
        // Bind to the data for the views
        holder.bindTo(currentSet);
        // Populate anymore data
    }

    /**
     *
     * @return
     */
    @Override
    public int getItemCount() {
        return mSetData.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        //private TextView rounds;

        public ViewHolder(View itemView) {
            super(itemView);
            //Initialize the views for the RecyclerView
        }

        void bindTo(Set curSet){
            //Populate data when they bind the workouts to the view holder

        }

        @Override
        public void onClick(View v) {

        }
    }
}
