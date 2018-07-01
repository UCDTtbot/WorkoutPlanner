package com.shibedays.workoutplanner.ui.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.shibedays.workoutplanner.R;
import com.shibedays.workoutplanner.db.entities.Set;
import com.shibedays.workoutplanner.db.entities.Workout;
import com.shibedays.workoutplanner.ui.adapters.OrderSetsAdapter;
import com.shibedays.workoutplanner.viewmodel.WorkoutViewModel;
import com.shibedays.workoutplanner.viewmodel.dialogs.ReorderViewModel;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ReorderSetsDialog extends DialogFragment {

    //region CONSTANTS
    // Package and Debug Constants
    private static final String DEBUG_TAG = ReorderSetsDialog.class.getSimpleName();
    private static final String PACKAGE = "com.shibedays.workoutplanner.ui.dialogs.ReorderSetsDialog.";
    //endregion

    //region INTENT_KEYS
    public static final String EXTRA_SET_LIST = PACKAGE + "SET_LIST";
    public static final String EXTRA_WRKOUT_ID = PACKAGE + "WRK_ID";
    //endregion

    //region PRIVATE_VARS
    // DATA
    private ReorderViewModel mViewModel;
    private WorkoutViewModel mWorkoutViewModel;
    private Activity mParentActivity;

    // UI
    private RecyclerView mSetsRecyclerView;
    private OrderSetsAdapter mAdapter;
    private ItemTouchHelper mItemTouchHelper;
    //endregion

    //region LIFECYCLE
    public static ReorderSetsDialog newInstance(Bundle args){
        ReorderSetsDialog dialog = new ReorderSetsDialog();
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mParentActivity = getActivity();
        if(mViewModel == null)
            mViewModel = ViewModelProviders.of(this).get(ReorderViewModel.class);
        if(mWorkoutViewModel == null)
            mWorkoutViewModel = ViewModelProviders.of(this).get(WorkoutViewModel.class);
        Gson g = new Gson();
        Bundle args = getArguments();
        if(args != null){
            String json = args.getString(EXTRA_SET_LIST);
            Type token = new TypeToken<ArrayList<Set>>(){}.getType();
            List<Set> list = g.fromJson(json, token);
            setupData(list, args.getInt(EXTRA_WRKOUT_ID));
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(mParentActivity);

        LayoutInflater inflater = null;
        if(mParentActivity != null) {
            inflater = mParentActivity.getLayoutInflater();
        } else {
            throw new RuntimeException(DEBUG_TAG + " Parent Activity doesn't exist");
        }
        final View view = inflater.inflate(R.layout.dialog_reorder_sets, null);



        mSetsRecyclerView = view.findViewById(R.id.order_recycler);

        mItemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.Callback() {
            @Override
            public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
                int swipeFlags = 0;
                return makeMovementFlags(dragFlags, swipeFlags);
            }

            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                float topY = viewHolder.itemView.getTop() + dY;
                float bottomY = topY + viewHolder.itemView.getHeight();
                if(topY < 0){
                    dY = 0;
                } else if (bottomY > recyclerView.getHeight()){
                    dY = recyclerView.getHeight() - viewHolder.itemView.getHeight() - viewHolder.itemView.getTop();
                }
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }

            @Override
            public boolean isLongPressDragEnabled() {
                return false;
            }

            @Override
            public boolean isItemViewSwipeEnabled() {
                return false;
            }

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                mAdapter.moveItems(viewHolder.getAdapterPosition(), target.getAdapterPosition());
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

            }
        });

        mAdapter = new OrderSetsAdapter(getContext(), new OrderSetsAdapter.OnStartDragListener() {
            @Override
            public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
                mItemTouchHelper.startDrag(viewHolder);
            }
        });

        RecyclerView.LayoutManager manager = new LinearLayoutManager(getContext());
        mSetsRecyclerView.setLayoutManager(manager);
        mSetsRecyclerView.setAdapter(mAdapter);

        mItemTouchHelper.attachToRecyclerView(mSetsRecyclerView);

        mAdapter.setData(mViewModel.getSetList());
        mAdapter.notifyDataSetChanged();
        //TODO: Setup recycler view

        builder.setView(view)
                .setTitle("")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //TODO handle the reorder
                        List<Set> s = mAdapter.getData();
                        Workout w = mWorkoutViewModel.getWorkoutByID(mViewModel.getID());
                        if(w != null && s != null){
                            w.setSetList(s);
                            mWorkoutViewModel.update(w);
                        }
                    }
                });

        return builder.create();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().setCanceledOnTouchOutside(true);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    //endregion

    //region UTILITY
    public static Bundle getBundle(List<Set> list, int workoutId){
        Bundle args = new Bundle();
        Gson g = new Gson();
        String json = g.toJson(list);
        args.putString(EXTRA_SET_LIST, json);
        args.putInt(EXTRA_WRKOUT_ID, workoutId);
        return args;
    }


    private void setupData(List<Set> data, int id) {
        if(mWorkoutViewModel == null){
            mWorkoutViewModel = ViewModelProviders.of(this).get(WorkoutViewModel.class);
        }
        mWorkoutViewModel.getAllWorkouts().observe(this, new Observer<List<Workout>>() {
            @Override
            public void onChanged(@Nullable List<Workout> workouts) {

            }
        });
        if(mViewModel == null){
            mViewModel = ViewModelProviders.of(this).get(ReorderViewModel.class);
        }
        mViewModel.setSetList(data);
        mViewModel.setID(id);
    }
    //endregion

}



