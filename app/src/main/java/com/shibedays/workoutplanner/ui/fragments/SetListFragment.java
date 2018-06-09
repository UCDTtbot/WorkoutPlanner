package com.shibedays.workoutplanner.ui.fragments;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.shibedays.workoutplanner.R;
import com.shibedays.workoutplanner.db.entities.Set;
import com.shibedays.workoutplanner.ui.adapters.ChooseSetAdapter;
import com.shibedays.workoutplanner.viewmodel.SetViewModel;

import java.util.List;

public class SetListFragment extends Fragment {

    //region CONSTANTS
    private static final String PACKAGE = "com.shibedays.workoutplanner.ui.fragments.SetListFragment.";
    private static final String DEBUG_TAG = SetListFragment.class.getSimpleName();
    private static int NUM_GRID_ROWS = 2;

    public static final int NEW_SET = 0;
    public static final int EDIT_SET = 1;
    public static final int DISPLAY_SET = 2;
    //endregion

    //region PRIVATE_VARS
    // Data
    private SetViewModel mSetViewModel;
    // UI
    private RecyclerView mListView;
    private ChooseSetAdapter mAdapter;

    private int mType;

    private SetListFragment mThis;
    //endregion



    public interface SetListListener {
        void openBottomSheet(int setType, int setID);
        void openSetDialog(int type, int setType, int setID);
    }
    private SetListListener mListener;

    //region FACTORY CONSTRUCTORS
    public SetListFragment() {
        // Required empty public constructor
    }

    public static SetListFragment newInstance(int type, SetListListener listener) {
        SetListFragment fragment = new SetListFragment();
        fragment.setListener(listener);
        fragment.setType(type);
        return fragment;
    }
    //endregion

    //region LIFECYCLE
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mThis = this;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_set_list, container, false);

        //region RECYCLER_VIEW
        mListView = view.findViewById(R.id.setlist_recycler);
        mListView.setLayoutManager(new LinearLayoutManager(getContext()));
        //mListView.setLayoutManager(new GridLayoutManager(getContext(), NUM_GRID_ROWS));
        mAdapter = new ChooseSetAdapter(getContext(), mType, new ChooseSetAdapter.ChooseSetListener() {
            @Override
            public void createSet() {
                mListener.openSetDialog(NEW_SET, Set.USER_CREATED, -1);
            }

            @Override
            public void openBottomSheet(int setID, int setType) {
                mListener.openBottomSheet(setType, setID);
            }

            @Override
            public void openDisplayInfo(int setID, int setType) {
                mListener.openSetDialog(DISPLAY_SET, setType, setID);
            }
        });
        mListView.setAdapter(mAdapter);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        setupData();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    //endregion

    public static Bundle getBundle(){
        Bundle args = new Bundle();

        return args;
    }

    //region UTILITY
    private void setListener(SetListListener listener){
        mListener = listener;
    }

    public void setupData(){
        mSetViewModel = ViewModelProviders.of(this).get(SetViewModel.class);
        mSetViewModel.getAllTypedSets(mType).observe(this, new Observer<List<Set>>() {
            @Override
            public void onChanged(@Nullable List<Set> sets) {
                mAdapter.updateData(sets);
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    /*
    public void updateSet(Set set){
        if(mAdapter != null){
            mAdapter.updateSet(set);
        }
    }

    public void removeSet(Set set){
        mAdapter.removeSet(set);

    }
    */

    public void notifyData(){
        mAdapter.notifyDataSetChanged();
    }

    public List<Set> getSelectedSets(){
        return mAdapter.getMappedSets();
    }

    private void setType(int type){
        mType = type;
    }

    //endregion

}
