package com.shibedays.workoutplanner.ui.fragments;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.shibedays.workoutplanner.BaseApp;
import com.shibedays.workoutplanner.R;
import com.shibedays.workoutplanner.db.entities.Set;
import com.shibedays.workoutplanner.viewmodel.fragments.SetInfoViewModel;
import com.shibedays.workoutplanner.viewmodel.SetViewModel;

public class SetInfoFragment extends Fragment {

    //region CONSTANTS
    private static final String PACKAGE = "com.shibedays.workoutplanner.ui.fragments.SetInfoFragment.";
    private static final String DEBUG_TAG = SetInfoFragment.class.getSimpleName();
    //endregion

    private static final String EXTRA_SET_ID = PACKAGE + "ID";

    //region PRIVATE_VARS
    // Data
    private SetInfoViewModel mMainVM;
    private SetViewModel mSetViewModel;
    // UI
    private TextView mSetNameView;
    private ImageView mSetImageView;
    private TextView mSetDescrip;
    private TextView mSetTime;
    // FLAGS
    //endregion



    public interface SetInfoListener {
        void updateSet(String setName, String descrip, int time, int imageId);
    }
    private SetInfoListener mListener;

    //region FACTORY CONSTRUCTORS
    public SetInfoFragment() {
        // Required empty public constructor
    }

    public static SetInfoFragment newInstance(Bundle args, SetInfoListener listener) {
        SetInfoFragment fragment = new SetInfoFragment();
        fragment.setListener(listener);
        fragment.setArguments(args);
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
        Bundle args = getArguments();
        mMainVM = ViewModelProviders.of(this).get(SetInfoViewModel.class);
        mSetViewModel = ViewModelProviders.of(this).get(SetViewModel.class);
        if(args != null) {
            int i = args.getInt(EXTRA_SET_ID);
            mMainVM.setId(i);
        } else {
            mMainVM.setId(-1);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_set_info, container, false);

        mSetNameView = view.findViewById(R.id.set_name);
        mSetImageView = view.findViewById(R.id.set_image);
        mSetDescrip = view.findViewById(R.id.set_descrip);
        mSetTime = view.findViewById(R.id.set_time);

        mSetImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSetImageView.setVisibility(View.GONE);
                mSetDescrip.setVisibility(View.GONE);
            }
        });
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        setupDataListener();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    //endregion

    public static Bundle getBundle(int id){
        Bundle args = new Bundle();
        args.putInt(EXTRA_SET_ID, id);
        return args;
    }

    private void updateUi(Set s){
        if(s != null) {
            mSetNameView.setText(s.getName());
            mSetImageView.setImageResource(s.getSetImageId());
            mSetDescrip.setText(s.getDescrip());
            mSetDescrip.setMovementMethod(new ScrollingMovementMethod());

            int[] time = BaseApp.convertFromMillis(s.getTime());
            final int min = time[0], sec = time[1];
            String text = BaseApp.formatTime(min, sec);
            mSetTime.setText(text);
        }
    }

    private void setupDataListener(){
        mSetViewModel.getSet(mMainVM.getId()).observe(this, new Observer<Set>() {
            @Override
            public void onChanged(@Nullable Set set) {
                mMainVM.setData(set);
                updateUi(set);
            }
        });
    }

    //region UTILITY
    private void setListener(SetInfoListener listener){
        mListener = listener;
    }
    //endregion
}
