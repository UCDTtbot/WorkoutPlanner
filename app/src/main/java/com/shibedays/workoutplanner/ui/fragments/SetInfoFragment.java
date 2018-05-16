package com.shibedays.workoutplanner.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.shibedays.workoutplanner.R;
import com.shibedays.workoutplanner.db.entities.Set;

import java.util.List;

public class SetInfoFragment extends Fragment {

    //region CONSTANTS
    private static final String PACKAGE = "com.shibedays.workoutplanner.ui.fragments.SetInfoFragment.";
    private static final String DEBUG_TAG = SetInfoFragment.class.getSimpleName();
    //endregion

    //region PRIVATE_VARS
    // Data
    private Set mSetData;
    // UI
    private TextView mSetNameView;
    private ImageView mSetImageView;
    private TextView mSetDescrip;
    // FLAGS

    private SetInfoFragment mThis;
    //endregion



    public interface SetInfoListener {
    }
    private SetInfoListener mListener;

    //region FACTORY CONSTRUCTORS
    public SetInfoFragment() {
        // Required empty public constructor
    }

    public static SetInfoFragment newInstance(Set setData, SetInfoListener listener) {
        SetInfoFragment fragment = new SetInfoFragment();
        fragment.setData(setData);
        fragment.setListener(listener);
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

        View view = inflater.inflate(R.layout.fragment_set_info, container, false);

        mSetNameView = view.findViewById(R.id.set_name);
        mSetImageView = view.findViewById(R.id.set_image);
        mSetDescrip = view.findViewById(R.id.set_descrip);

        if(mSetData != null) {
            mSetNameView.setText(mSetData.getName());
        }

        mSetImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSetImageView.setVisibility(View.GONE);
                mSetDescrip.setVisibility(View.VISIBLE);
            }
        });

        mSetDescrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSetDescrip.setVisibility(View.GONE);
                mSetImageView.setVisibility(View.VISIBLE);
            }
        });

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
    private void setListener(SetInfoListener listener){
        mListener = listener;
    }

    public void setData(Set s){
        mSetData = s;
    }

    public void updateSet(Set set){
        mSetData = set;
    }

    //endregion
}
