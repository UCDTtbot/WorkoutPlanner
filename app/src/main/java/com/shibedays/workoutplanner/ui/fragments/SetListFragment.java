package com.shibedays.workoutplanner.ui.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.TextView;
import android.widget.Toast;

import com.shibedays.workoutplanner.R;
import com.shibedays.workoutplanner.db.entities.Set;
import com.shibedays.workoutplanner.ui.adapters.ChooseSetAdapter;

import java.util.ArrayList;
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
    private List<Set> mSetMap;
    // UI
    private RecyclerView mListView;
    private ChooseSetAdapter mAdapter;
    // FLAGS
    private boolean mIncludeHeader;

    //endregion



    public interface SetListListener {
        void onFragmentInteraction(Uri uri);
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
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSetMap = new ArrayList<>();

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
            public void mapSet(Set set) {
                if(mSetMap.contains(set)){
                    Toast.makeText(getContext(), "Set is already mapped", Toast.LENGTH_SHORT).show();
                    //Log.e(DEBUG_TAG, "Set " + set.getName() + " is already mapped ");
                } else {
                    mSetMap.add(set);
                    for(Set sets : mSetMap){
                        //Log.d(DEBUG_TAG, set.getName());
                    }
                }
            }

            @Override
            public void unmapSet(Set set) {
                if(mSetMap.contains(set)){
                    mSetMap.remove(set);
                    for(Set sets : mSetMap){
                        Log.d(DEBUG_TAG, set.getName());
                    }
                } else {
                    Toast.makeText(getContext(), "Set isn't mapped", Toast.LENGTH_SHORT).show();
                    Log.e(DEBUG_TAG, "Set " + set.getName() + " is not mapped ");
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
    }
    private void setHeader(boolean header){
        mIncludeHeader = header;
    }
    //endregion

}
