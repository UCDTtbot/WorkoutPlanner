package com.shibedays.workoutplanner.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.shibedays.workoutplanner.R;
import com.shibedays.workoutplanner.db.entities.Set;
import com.shibedays.workoutplanner.ui.adapters.ChooseSetAdapter;
import com.shibedays.workoutplanner.ui.dialogs.AddEditSetDialog;

import java.util.List;

public class SetListFragment extends Fragment {

    //region CONSTANTS
    private static final String PACKAGE = "com.shibedays.workoutplanner.ui.fragments.SetListFragment.";
    private static final String DEBUG_TAG = SetListFragment.class.getSimpleName();
    private static int NUM_GRID_ROWS = 2;
    //endregion

    //region PRIVATE_VARS
    // Data
    private List<Set> mSetList;
    // UI
    private RecyclerView mListView;
    private ChooseSetAdapter mAdapter;
    // FLAGS
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

    public static SetListFragment newInstance(List<Set> setList, int type, SetListListener listener) {
        SetListFragment fragment = new SetListFragment();
        fragment.setData(setList);
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
                mListener.openSetDialog(AddEditSetDialog.NEW_SET, Set.USER_CREATED, -1);
            }

            @Override
            public void openBottomSheet(int setID, int type) {
                mListener.openBottomSheet(type, setID);
            }

            @Override
            public void openDisplayInfo(int setID, int type) {
                mListener.openSetDialog(AddEditSetDialog.DISPLAY_SET, type, setID);
            }
        });
        mListView.setAdapter(mAdapter);
        mAdapter.setData(mSetList);

        return view;
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

    public void setData(List<Set> list){
        mSetList = list;
        if(mAdapter != null) {
            mAdapter.setData(mSetList);
        }
    }

    public void addSet(Set set){
        mSetList.add(set);
        mAdapter.notifyDataSetChanged();
    }

    public void updateSet(Set set){
        mSetList.set(mSetList.indexOf(set), set);
    }

    public void removeSet(Set set){
        int i = mSetList.indexOf(set);
        mAdapter.removeMapping(set);
        mSetList.remove(set);
        mAdapter.notifyItemRemoved(i);

    }

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
