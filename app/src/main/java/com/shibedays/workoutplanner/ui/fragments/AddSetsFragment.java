package com.shibedays.workoutplanner.ui.fragments;

import android.app.Activity;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.shibedays.workoutplanner.BaseApp;
import com.shibedays.workoutplanner.R;
import com.shibedays.workoutplanner.db.entities.Set;
import com.shibedays.workoutplanner.ui.MyWorkoutActivity;
import com.shibedays.workoutplanner.ui.adapters.ViewPagerAdapter;
import com.shibedays.workoutplanner.ui.adapters.sectioned.SectionedSetAdapter;
import com.shibedays.workoutplanner.ui.dialogs.DisplaySetDialog;
import com.shibedays.workoutplanner.ui.dialogs.BottomSheetDialog;
import com.shibedays.workoutplanner.viewmodel.SetViewModel;

import java.util.ArrayList;
import java.util.List;

public class AddSetsFragment extends Fragment {


    //region CONSTANTS
    // Package and Debug Constants
    private static final String PACKAGE = "com.shibedays.workoutplanner.ui.fragments.AddSetsFragment.";
    private static final String DEBUG_TAG = AddSetsFragment.class.getSimpleName();
    //endregion

    //region PRIVATE_VARS
    // Data
    private SetViewModel mSetViewModel;
    private List<SetListFragment> mSetListFrags;
    // Adapters
    private SectionedSetAdapter mLeftAdapter;
    private SectionedSetAdapter mRightAdapter;
    // UI Components
    private Button mSaveButton;

    private CoordinatorLayout mCoordLayout; // Displaying Toasts / Undo bar

    // Parent
    private MyWorkoutActivity mParentActivity;
    private AddSetsFragment mThis;
    private CreateEditSetFragment mCreateEditFragment;


    //endregion

    //region INTERFACES

    public interface NewSetListener {
        void addSetsToWorkout(List<Long> sets);
    }
    private NewSetListener mListener;


    //endregion

    //region FACTORY_CONSTRUCTOR
    public AddSetsFragment() {

    }

    public static AddSetsFragment newInstance(NewSetListener listener) {
        AddSetsFragment newFragment = new AddSetsFragment();
        newFragment.setListener(listener);

        return newFragment;
    }
    //endregion

    //region LIFECYCLE
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Activity act = getActivity();
        if(act instanceof MyWorkoutActivity){
            mParentActivity = (MyWorkoutActivity) act;
        } else {
            throw new RuntimeException(DEBUG_TAG + " wasn't called from MyWorkoutActivity");
        }
        mThis = this;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //region UI

        View view = inflater.inflate(R.layout.fragment_add_set, container, false);
        mCoordLayout = mParentActivity.findViewById(R.id.set_coord_layout);

        mSaveButton = view.findViewById(R.id.button_add);
        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Long> setIds = new ArrayList<>();
                for(SetListFragment s : mSetListFrags){
                    setIds.addAll(s.getSelectedSets());
                }
                mListener.addSetsToWorkout(setIds);
            }
        });

        //region PAGER
        ViewPager viewPager = view.findViewById(R.id.pager);
        viewPager.setOffscreenPageLimit(Set.TYPES.length);
        ViewPagerAdapter adapter = new ViewPagerAdapter(getChildFragmentManager());

        mSetListFrags = new ArrayList<>();
        for(int type = 0; type < Set.TYPES.length; type++){

            SetListFragment frag = SetListFragment.newInstance(type, new SetListFragment.SetListListener() {

                @Override
                public void openBottomSheet(int setType, long setID) {
                    final Set set = mSetViewModel.getSetById(setID);

                    if(set == null) throw new RuntimeException(DEBUG_TAG + " set came up null");
                    AddSetsFragment.this.openBottomSheet(set, new BottomSheetDialog.BottomSheetDialogListener() {
                        @Override
                        public void bottomSheetResult(int resultCode) {
                            if(resultCode == BaseApp.EDIT){
                                openEditSet(set);
                            } else if (resultCode == BaseApp.DELETE) {
                                deleteSetConfirmation(set);
                            } else {
                                Log.e(DEBUG_TAG, "Invalid Result Code " + resultCode);
                            }
                        }
                    });
                }

                @Override
                public void openSetDialog(int type, int setType, long setID) {

                    final Set set = mSetViewModel.getSetById(setID);

                    if(type == SetListFragment.NEW_SET){
                        openNewSet();
                    } else if (type == SetListFragment.EDIT_SET) {
                        openEditSet(set);
                    } else if (type == SetListFragment.DISPLAY_SET) {
                        displayDialog(set);
                    } else {
                        throw new RuntimeException(DEBUG_TAG + " invalid DisplaySetDialog type");
                    }
                }
            });
            adapter.addFragment(frag, Set.TYPES[type]);
            mSetListFrags.add(frag);
        }

        viewPager.setAdapter(adapter);

        TabLayout mTabLayout = view.findViewById(R.id.pager_header);
        mTabLayout.setupWithViewPager(viewPager);
        //endregion

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        setupViewModel();
        Log.d(DEBUG_TAG, "NEW_SET_FRAGMENT ON_START");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(DEBUG_TAG, "NEW_SET_FRAGMENT ON_RESUME");

    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(DEBUG_TAG, "NEW_SET_FRAGMENT ON_PAUSE");

    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(DEBUG_TAG, "NEW_SET_FRAGMENT ON_STOP");

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mParentActivity.findViewById(R.id.fragment_container).setVisibility(View.GONE);
        mParentActivity.showActionItems();
        mParentActivity.renameTitle(-1);
        Log.d(DEBUG_TAG, "NEW_SET_FRAGMENT ON_DESTROY");

    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @NonNull
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(DEBUG_TAG, "NEW_SET_FRAGMENT SAVING INSTANCE STATE");
    }
    //endregion



    //region OPEN_FUNCTIONS
    private void openBottomSheet(Set set, BottomSheetDialog.BottomSheetDialogListener listener){
        Bundle bundle = BottomSheetDialog.getBottomSheetBundle(set.getName(),
                BaseApp.getSetBtmSheetRows(), BaseApp.getSetBtmSheetNames(mParentActivity),
                BaseApp.getSetBtmSheetICs(), BaseApp.getSetBtmSheetResults());
        BottomSheetDialog dialog = BottomSheetDialog.newInstance(bundle, listener);
        dialog.setTargetFragment(mThis, 0);
        if(getFragmentManager() != null){
            dialog.show(getFragmentManager(), DEBUG_TAG);
        }
    }


    private void displayDialog(@NonNull Set set){
        Bundle bundle = DisplaySetDialog.getDialogBundle(set.getSetId(), set.getName(), set.getDescrip(), set.getTime(), set.getSetImageId());
        DisplaySetDialog dialog = DisplaySetDialog.newInstance(bundle);
        dialog.setTargetFragment(mThis, 0);
        if (getFragmentManager() != null) {
            dialog.show(getFragmentManager(), DEBUG_TAG);
        }
    }

    private void openNewSet(){
        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
        Bundle args = CreateEditSetFragment.getBundle(-1, "", "", 0, R.drawable.ic_fitness_black_24dp);
        mCreateEditFragment = CreateEditSetFragment.newInstance(R.string.add_new_set, args, new CreateEditSetFragment.CreateEditSetListener() {
            @Override
            public void returnData(String name, String descrip, int min, int sec, int imageId) {
                Set set = new Set(name, descrip, Set.USER_CREATED, BaseApp.convertToMillis(min, sec), imageId);
                mSetViewModel.insert(set);
            }
        });
        fragmentTransaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slight_out_left);
        fragmentTransaction.replace(R.id.new_workout_fragment_container, mCreateEditFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
        mParentActivity.renameTitle(R.string.new_set);
    }

    private void openEditSet(@NonNull final Set set){
        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
        Bundle args = CreateEditSetFragment.getBundle(set.getSetId(), set.getName(), set.getDescrip(), set.getTime(), set.getSetImageId());
        mCreateEditFragment = CreateEditSetFragment.newInstance(R.string.add_new_set, args, new CreateEditSetFragment.CreateEditSetListener() {
            @Override
            public void returnData(String name, String descrip, int min, int sec, int imageId) {
                updateUserSet(set, name, descrip, min, sec, imageId);
            }
        });
        fragmentTransaction.replace(R.id.new_workout_fragment_container, mCreateEditFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
        mParentActivity.renameTitle(R.string.edit_set);
    }

    //endregion

    //region DATA_FUNCTIONS
    private void updateUserSet(Set set, String name, String descrip, int min, int sec, int imageId){
        if(set != null) {
            set.setName(name);
            set.setDescrip(descrip);
            set.setTime(BaseApp.convertToMillis(min, sec));
            set.setSetImageId(imageId);
            mSetViewModel.insert(set);
        } else {
            throw new RuntimeException(DEBUG_TAG + " trying to update set, was null");
        }
    }

    private void deleteUserSet(Set set){
        mSetViewModel.remove(set);
    }

    //endregion

    //region UTILITY
    private void deleteSetConfirmation(final Set set){
        if(getContext() != null){
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.Theme_AppCompat_Light_Dialog_Alert);
            builder.setTitle("Delete Set")
                    .setMessage("Are you sure you want to delete " + set.getName() + " ?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            deleteUserSet(set);
                        }
                    })
                    .setNegativeButton("No", null)
                    .show();
        }
    }

    //endregion

    private void setupViewModel(){
        mSetViewModel = ViewModelProviders.of(this).get(SetViewModel.class);
    }

    //region SETTERS
    private void setListener(NewSetListener listener){
        mListener = listener;
    }
    //endregion




}
