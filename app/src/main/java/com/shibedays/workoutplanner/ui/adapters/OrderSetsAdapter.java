package com.shibedays.workoutplanner.ui.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.shibedays.workoutplanner.R;
import com.shibedays.workoutplanner.db.entities.Set;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class OrderSetsAdapter extends RecyclerView.Adapter<OrderSetsAdapter.OrderViewHolder> {

    //region CONSTANTS
    private static final String DEBUG_TAG = OrderSetsAdapter.class.getSimpleName();
    private static final String PACKAGE = "com.shibedays.workoutplanner.ui.adapters.OrderSetsAdapter.";
    //endregion

    class OrderViewHolder extends RecyclerView.ViewHolder {

        TextView name;

        public OrderViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.set_name);
        }
    }

    //region PRIVATE_VARS
    // Data
    private List<Set> mSetList;
    // UI
    private Context mContext;
    // FLAGS

    //endregion

    //region LIFECYCLE
    public OrderSetsAdapter(Context context){
        mContext = context;
    }

    @NonNull
    @Override
    public OrderSetsAdapter.OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new OrderViewHolder(LayoutInflater.from(mContext)
                .inflate(R.layout.list_order_set_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull OrderSetsAdapter.OrderViewHolder holder, int position) {
        // Bind UI stuff for each item
        if(mSetList != null) {
            holder.name.setText(mSetList.get(position).getName());
        }
    }

    //endregion

    //region UTILITY
    @Override
    public int getItemCount() {
        return mSetList != null ? mSetList.size() : 0;
    }

    public void setData(List<Set> data){
        mSetList = data;
    }

    public void moveItems(int from, int to){
        if(from < to){
            for(int i = from; i < to; i++){
                Collections.swap(mSetList, i, i + 1);
            }
        } else {
            for(int i = from; i > to; i--){
                Collections.swap(mSetList, i, i - 1);
            }
        }
        notifyItemMoved(from, to);
    }
    //endregion

}
