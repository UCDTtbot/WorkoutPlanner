package com.shibedays.workoutplanner.ui.fragments;

import android.app.Activity;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.shibedays.workoutplanner.BaseApp;
import com.shibedays.workoutplanner.R;
import com.shibedays.workoutplanner.db.entities.Set;
import com.shibedays.workoutplanner.db.entities.Workout;
import com.shibedays.workoutplanner.ui.MyWorkoutActivity;
import com.shibedays.workoutplanner.viewmodel.WorkoutViewModel;
import com.shibedays.workoutplanner.viewmodel.fragments.SetInfoViewModel;
import com.shibedays.workoutplanner.viewmodel.SetViewModel;

import java.util.List;

public class SetInfoFragment extends Fragment {

    //region CONSTANTS
    private static final String PACKAGE = "com.shibedays.workoutplanner.ui.fragments.SetInfoFragment.";
    private static final String DEBUG_TAG = SetInfoFragment.class.getSimpleName();
    //endregion

    private static final String EXTRA_SET_JSON = PACKAGE + "JSON";
    private static final String EXTRA_PARENT_ID = PACKAGE + "WORKOUT_ID";
    private static final String EXTRA_SET_POS = PACKAGE + "POS";

    //region PRIVATE_VARS
    // Data
    private SetInfoViewModel mMainVM;
    private WorkoutViewModel mWorkoutViewModel;
    // UI
    private TextView mSetNameView;
    private ImageView mSetImageView;
    private TextView mSetDescrip;
    private TextView mSetTime;

    private ImageView mEditSetIC;
    private ImageView mDeleteSetIC;
    private ImageView mMoreInfoIC;
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
        mWorkoutViewModel = ViewModelProviders.of(this).get(WorkoutViewModel.class);
        mWorkoutViewModel.getAllWorkouts().observe(this, new Observer<List<Workout>>() {
            @Override
            public void onChanged(@Nullable List<Workout> workouts) {

            }
        });
        if(args != null) {
            mMainVM.setData(args.getString(EXTRA_SET_JSON));
            mMainVM.setParentWrkoutId(args.getInt(EXTRA_PARENT_ID));
            mMainVM.setPos(args.getInt(EXTRA_SET_POS));
        } else {
            mMainVM.setParentWrkoutId(-1);
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


        mEditSetIC = view.findViewById(R.id.edit_ic);
        mEditSetIC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openEditSet();
            }
        });

        mDeleteSetIC = view.findViewById(R.id.delete_ic);
        mDeleteSetIC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteSet();
            }
        });

        mMoreInfoIC = view.findViewById(R.id.more_info_ic);
        mMoreInfoIC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMoreInfo();
            }
        });

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

    public static Bundle getBundle(Set set, int setPos, int workoutId){
        Bundle args = new Bundle();
        Gson g = new Gson();
        String json = g.toJson(set);
        args.putString(EXTRA_SET_JSON, json);
        args.putInt(EXTRA_PARENT_ID, workoutId);
        args.putInt(EXTRA_SET_POS, setPos);
        return args;
    }

    private void updateUi(Set s){
        if(s != null) {
            mSetNameView.setText(s.getName());
            mSetImageView.setImageResource(s.getSetImageId());
            if(BaseApp.isDarkTheme()){
                mSetImageView.setColorFilter(ContextCompat.getColor(getContext(), R.color.colorDarkThemeIco));
            }
            mSetDescrip.setText(s.getDescrip());
            mSetDescrip.setMovementMethod(new ScrollingMovementMethod());

            int[] time = BaseApp.convertFromMillis(s.getTime());
            final int min = time[0], sec = time[1];
            String text = BaseApp.formatTime(min, sec);
            mSetTime.setText(text);
            if(s.getSetType() != Set.USER_CREATED){
                mDeleteSetIC.setVisibility(View.GONE);
            }
        }
    }

    private void setupData(){
        mMainVM.getData().observe(this, new Observer<Set>() {
            @Override
            public void onChanged(@Nullable Set set) {
                updateUi(set);
            }
        });
    }

    private void openEditSet(){
        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
        Set set = mMainVM.getData().getValue();
        if(set != null) {
            Bundle args = CreateEditSetFragment.getPosBundle(
                    mMainVM.getPos(),
                    set.getSetId(),
                    mMainVM.getParentWrkoutId(),
                    set.getName(),
                    set.getDescrip(),
                    set.getTime(),
                    set.getSetImageId());

            CreateEditSetFragment frag = CreateEditSetFragment.newInstance(getActivity().getTitle().toString(), CreateEditSetFragment.TYPE_EDIT_WORKOUT_SET, args);
            View v = getActivity().findViewById(R.id.fragment_container);
            v.setVisibility(View.VISIBLE);
            fragmentTransaction.replace(R.id.fragment_container, frag);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();

            Activity act = getActivity();
            if (act instanceof MyWorkoutActivity) {
                ((MyWorkoutActivity) act).renameTitle(R.string.edit_set);
            }
        } else {
            Log.e(DEBUG_TAG, "Set came up null");
        }
    }

    private void deleteSet(){
        final Workout parent = mWorkoutViewModel.getWorkoutByID(mMainVM.getParentWrkoutId());
        final Set s = mMainVM.getData().getValue();
        if(s != null && parent != null) {
            if (getContext() != null) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.Theme_AppCompat_Light_Dialog_Alert);
                builder.setTitle("Delete Set")
                        .setMessage("Are you sure you want to delete \n" + s.getName() + " ?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                parent.removeSet(s);
                                mWorkoutViewModel.update(parent);
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();
            }
        }
    }

    private void openMoreInfo(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        WebView wv = new WebView(getContext());
        wv.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        wv.loadUrl(mMainVM.getData().getValue().getURL());
        wv.getSettings().setJavaScriptEnabled(true);
        builder.setTitle("More Info")
                .setView(wv)
                .setNeutralButton("Ok", null)
                .show();
    }

    public void updateData(Set s){
        mMainVM.setData(s);
    }

    //region UTILITY
    private void setListener(SetInfoListener listener){
        mListener = listener;
    }
    //endregion
}
