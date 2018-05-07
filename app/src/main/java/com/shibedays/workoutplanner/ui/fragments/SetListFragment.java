package com.shibedays.workoutplanner.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.shibedays.workoutplanner.BaseApp;
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
    private boolean mIncludeHeader;

    private SetListFragment mThis;
    //endregion



    public interface SetListListener {
        void mapSet(Set set);
        void unmapSet(Set set);
        void applyUserSet(Set set);
        void openBottomSheet(Set set, int pos);
    }
    private SetListListener mListener;

    //region FACTORY CONSTRUCTORS
    public SetListFragment() {
        // Required empty public constructor
    }

    public static SetListFragment newInstance(List<Set> setList, boolean header, SetListListener listener) {
        SetListFragment fragment = new SetListFragment();
        fragment.setSetList(setList);
        fragment.setListener(listener);
        fragment.setHeader(header);
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
        mAdapter = new ChooseSetAdapter(getContext(), mIncludeHeader, new ChooseSetAdapter.ChooseSetListener() {


            @Override
            public void createSet() {
                Bundle args = AddEditSetDialog.getDialogBundle(AddEditSetDialog.NEW_SET, "", "", 0, 0, 0);
                AddEditSetDialog dialog = AddEditSetDialog.newInstance(args, new AddEditSetDialog.AddEditSetDialogListener() {
                    @Override
                    public void dialogResult(int dialogType, String name, String descrip, int min, int sec, int section, int index) {
                        Log.d(DEBUG_TAG, dialogType + " " + name + " " + section + " " + index);
                        Set set = new Set(name, descrip, Set.USER_CREATED, BaseApp.convertToMillis(min, sec));
                        mListener.applyUserSet(set);
                        mSetList.add(set);
                        mAdapter.notifyDataSetChanged();
                    }
                });
                dialog.setTargetFragment(mThis, 0);
                if (getFragmentManager() != null) {
                    dialog.show(getFragmentManager(), DEBUG_TAG);
                }
            }

            @Override
            public void openBottomSheet(Set set) {
                mListener.openBottomSheet(set, mSetList.indexOf(set));
            }

            @Override
            public void openDisplayInfo(Set set) {
                Bundle args = AddEditSetDialog.getDialogBundle(AddEditSetDialog.DISPLAY_SET, set.getName(), set.getDescrip(), set.getTime(), 0, 0);
                AddEditSetDialog dialog = AddEditSetDialog.newInstance(args, new AddEditSetDialog.AddEditSetDialogListener() {
                    @Override
                    public void dialogResult(int dialogType, String name, String descrip, int min, int sec, int section, int index) {
                        Log.d(DEBUG_TAG, "Closing display");
                    }
                });
                dialog.setTargetFragment(mThis, 0);
                if (getFragmentManager() != null) {
                    dialog.show(getFragmentManager(), DEBUG_TAG);
                }
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

    public void setSetList(List<Set> list){
        mSetList = list;
        if(mAdapter != null) {
            mAdapter.setData(mSetList);
            mAdapter.notifyDataSetChanged();
        }
    }

    private void setHeader(boolean header){
        mIncludeHeader = header;
    }

    //endregion

}
