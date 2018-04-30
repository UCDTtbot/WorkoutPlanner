package com.shibedays.workoutplanner.ui.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.shibedays.workoutplanner.BaseApp;
import com.shibedays.workoutplanner.R;
import com.shibedays.workoutplanner.db.entities.Set;
import com.shibedays.workoutplanner.ui.MainActivity;
import com.shibedays.workoutplanner.ui.MyWorkoutActivity;
import com.shibedays.workoutplanner.ui.adapters.sectioned.SectionedSetAdapter;
import com.shibedays.workoutplanner.ui.dialogs.AddEditSetDialog;
import com.shibedays.workoutplanner.ui.dialogs.BottomSheetDialog;

import java.util.ArrayList;
import java.util.List;

public class AddNewSetFragment extends Fragment {


    //region CONSTANTS
    // Package and Debug Constants
    private static final String PACKAGE = "com.shibedays.workoutplanner.ui.fragments.AddNewSetFragment.";
    private static final String DEBUG_TAG = AddNewSetFragment.class.getSimpleName();
    //endregion

    //region PRIVATE_VARS
    // Data
    private List<Set> mDefaultSets;
    private List<Set> mUserCreatedSets;

    // Adapters
    private SectionedSetAdapter mLeftAdapter;
    private SectionedSetAdapter mRightAdapter;
    // UI Components
    private RecyclerView mLeftRecyclerView;
    private RecyclerView mRightRecyclerView;

    private CoordinatorLayout mCoordLayout; // Displaying Toasts / Undo bar

    // Parent
    private MyWorkoutActivity mParentActivity;
    private AddNewSetFragment mThis;


    //endregion

    //region INTERFACES

    public interface NewSetListener {
        // TODO: Update argument type and name
        void addSet(Set set);
    }
    private NewSetListener mListener;


    //endregion

    //region FACTORY_CONSTRUCTOR
    public AddNewSetFragment() {

    }

    public static AddNewSetFragment newInstance(List<Set> userCreatedSets, List<Set> defaultSets, NewSetListener listener) {
        AddNewSetFragment newFragment = new AddNewSetFragment();

        newFragment.setDefaultSets(defaultSets);
        newFragment.setUserCreatedSets(userCreatedSets);
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

            //region RECYCLER_VIEWS

                //region LEFT_RV
        mLeftRecyclerView = view.findViewById(R.id.left_recyclerview);
        mLeftRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        List<String> headerList = new ArrayList<>();
        List<List<Set>> leftDataList = new ArrayList<>();
        headerList.add(getString(R.string.header_title_default));

        //        mLeftAdapter = new SectionedSetAdapter(getContext(), headerList, 0, new SectionedSetAdapter.SectionedAddSetListener() {
        mLeftAdapter = new SectionedSetAdapter(getContext(), headerList, -1, new SectionedSetAdapter.SectionedAddSetListener() {
            @Override
            public void onClick(Set set, int section, int relativePos) {
                Log.d(DEBUG_TAG, "Left Adapter Default Clicked Add Set");
                addSetConfirmation(set);
            }

            @Override
            public void onLongClick(Set set, int section, int relativePos) {
                Log.d(DEBUG_TAG, "Left Adapter Default LongClicked Open Info");
                openDialog(AddEditSetDialog.DISPLAY_SET, set, relativePos, section, new AddEditSetDialog.AddEditSetDialogListener() {
                    @Override
                    public void dialogResult(int dialogType, String name, String descrip, int min, int sec, int section, int index) {
                        // Nothing
                    }
                });
            }

            @Override
            public void createUserSet(int section) {
                // Nothing
            }
        });
        mLeftRecyclerView.setAdapter(mLeftAdapter);
        mLeftAdapter.addToDataList(mDefaultSets);
        mLeftAdapter.shouldShowHeadersForEmptySections(true);
        mLeftAdapter.shouldShowFooters(false);
                //endregion

                //region RIGHT_RV
        mRightRecyclerView = view.findViewById(R.id.right_recyclerview);
        mRightRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        List<String> rightHeaderList = new ArrayList<>();
        rightHeaderList.add(getString(R.string.header_title_user_created));
        mRightAdapter = new SectionedSetAdapter(getContext(), headerList, 0, new SectionedSetAdapter.SectionedAddSetListener(){
            @Override
            public void onClick(Set set, int section, final int relativePos) {
                Log.d(DEBUG_TAG, "Right Adapter User Created Clicked Add Set");
                addSetConfirmation(set);
            }

            @Override
            public void onLongClick(Set set, int section, final int relativePos) {
                Log.d(DEBUG_TAG, "Right Adapter User Created Long Clicked Open Bottom Sheet");
                openBottomSheet(set, relativePos, section, new BottomSheetDialog.BottomSheetDialogListener() {
                    @Override
                    public void bottomSheetResult(int resultCode, int index, int section) {
                        Log.d(DEBUG_TAG, "Result Code: " + Integer.toString(resultCode) + " Section: " + section);
                        switch (resultCode){
                            case BaseApp.EDIT:
                                openDialog(AddEditSetDialog.EDIT_SET, mUserCreatedSets.get(index), relativePos, section, new AddEditSetDialog.AddEditSetDialogListener() {
                                    @Override
                                    public void dialogResult(int dialogType, String name, String descrip, int min, int sec, int section, int index) {
                                        updateUserCreatedSet(index, name, descrip, min, sec);
                                    }
                                });
                                break;
                            case BaseApp.DELETE:
                                deleteUserCreatedSet(section, index);
                                break;
                            case BaseApp.DUPLCIATE:
                                break;
                            default:
                                break;
                        }
                    }
                });
            }

            @Override
            public void createUserSet(int section) {
                Log.d(DEBUG_TAG, "Right Adapter Create New User Set");
                //TODO : Dialog must be updated
                openDialog(AddEditSetDialog.NEW_SET, null, -1, section, new AddEditSetDialog.AddEditSetDialogListener() {
                    @Override
                    public void dialogResult(int dialogType, String name, String descrip, int min, int sec, int section, int index) {
                        //Set set = new Set(name, descrip, BaseApp.convertToMillis(min, sec));
                        //addUserCreatedSet(section, set);
                    }
                });
            }
        });
        mRightRecyclerView.setAdapter(mRightAdapter);
        mRightAdapter.addToDataList(mUserCreatedSets);
        mRightAdapter.shouldShowHeadersForEmptySections(true);
        mRightAdapter.shouldShowFooters(true);
                //endregion

            //endregion



        //endregion

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
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

    //region SETTERS
    private void setListener(NewSetListener listener){
        mListener = listener;
    }

    public void setDefaultSets(List<Set> sets){
        mDefaultSets = sets;
    }

    public void setUserCreatedSets(List<Set> sets) {
        mUserCreatedSets = sets;
    }
    public void addUserCreatedSet(int section, Set set){
        mRightAdapter.addSet(section, set);
    }
    public void updateUserCreatedSet(int index, String name, String descrip, int min, int sec){
        Set set = mUserCreatedSets.get(index);
        set.setName(name);
        set.setDescrip(descrip);
        set.setTime(BaseApp.convertToMillis(min, sec));
        mRightAdapter.updateSetList(0, mUserCreatedSets);
    }
    public void deleteUserCreatedSet(int section, int pos){
        mRightAdapter.removeSet(section, pos);
    }

    //endregion

    //region UTILITY
    private void addSetConfirmation(final Set set){
        if(getContext() != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.Theme_AppCompat_Light_Dialog_Alert);
            builder.setTitle("Add Set")
                    .setMessage("Are you sure you want to add " + set.getName() + "?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mListener.addSet(set);
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .show();

        }
    }

    private void openDialog(int type, Set set, int relativePos, int section, AddEditSetDialog.AddEditSetDialogListener listener) {
        Bundle bundle = null;
        if(set != null){
            bundle = AddEditSetDialog.getDialogBundle(type, set.getName(), set.getDescrip(), set.getTime(), relativePos, section);
        } else {
            bundle = AddEditSetDialog.getDialogBundle(type, "", "", -1, relativePos, section);
        }
        AddEditSetDialog dialog = AddEditSetDialog.newInstance(listener, bundle);
        dialog.setTargetFragment(mThis, 0);
        if (getFragmentManager() != null) {
            dialog.show(getFragmentManager(), DEBUG_TAG);
        }
    }

    private void openBottomSheet(Set set, int relativePos, int section, BottomSheetDialog.BottomSheetDialogListener listener){
        Bundle bundle = BottomSheetDialog.getBottomSheetBundle(set.getName(), relativePos, section,
                BaseApp.getSetBtmSheetRows(), BaseApp.getSetBtmSheetNames(mParentActivity), BaseApp.getSetBtmSheetICs(), BaseApp.getSetBtmSheetResults());
        BottomSheetDialog dialog = BottomSheetDialog.newInstance(bundle, listener);
        dialog.setTargetFragment(mThis, 0);
        if(getFragmentManager() != null){
            dialog.show(getFragmentManager(), DEBUG_TAG);
        }
    }



    //endregion




}
