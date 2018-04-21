package com.shibedays.workoutplanner.ui.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.shibedays.workoutplanner.R;
import com.shibedays.workoutplanner.db.entities.Set;

import java.util.List;

public class SetListFragment extends Fragment {

    //region CONSTANTS

    //endregion

    //region PRIVATE_VARS
    // Data
    private List<Set> mSetList;

    //endregion



    public interface SetListListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
    private SetListListener mListener;

    //region FACTORY CONSTRUCTORS
    public SetListFragment() {
        // Required empty public constructor
    }

    public static SetListFragment newInstance(List<Set> setList, SetListListener listener) {
        SetListFragment fragment = new SetListFragment();
        //fragment.setArguments(args);
        fragment.setListener(listener);
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

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        TextView textView = new TextView(getActivity());
        textView.setText(R.string.hello_blank_fragment);
        return textView;
    }



    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    //endregion

    private void setListener(SetListListener listener){
        mListener = listener;
    }

    public static Bundle getBundle(){
        Bundle args = new Bundle();

        return args;
    }

    public void setSetList(List<Set> list){
        mSetList = list;
    }

}
