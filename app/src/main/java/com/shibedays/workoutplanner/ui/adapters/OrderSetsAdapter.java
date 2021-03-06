package com.shibedays.workoutplanner.ui.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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

    public interface OnStartDragListener{
        void onStartDrag(RecyclerView.ViewHolder viewHolder);
    }
    private OnStartDragListener mListener;

    class OrderViewHolder extends RecyclerView.ViewHolder {

        TextView name;
        ImageView dragHandle;

        public OrderViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.set_name);
            dragHandle = itemView.findViewById(R.id.drag_handle);
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
    public OrderSetsAdapter(Context context, OnStartDragListener listener) {
        mContext = context;
        mListener = listener;
    }

    @NonNull
    @Override
    public OrderSetsAdapter.OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new OrderViewHolder(LayoutInflater.from(mContext)
                .inflate(R.layout.list_order_set_item, parent, false));
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull final OrderSetsAdapter.OrderViewHolder holder, int position) {
        // Bind UI stuff for each item
        if(mSetList != null) {
            holder.name.setText(mSetList.get(position).getName());
            holder.dragHandle.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if(event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                        mListener.onStartDrag(holder);
                    }
                    return false;
                }
            });
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

    public List<Set> getData(){
        return mSetList;
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
