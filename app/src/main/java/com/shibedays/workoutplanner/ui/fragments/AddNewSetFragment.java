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

import com.shibedays.workoutplanner.R;
import com.shibedays.workoutplanner.db.entities.Set;
import com.shibedays.workoutplanner.ui.MainActivity;
import com.shibedays.workoutplanner.ui.MyWorkoutActivity;
import com.shibedays.workoutplanner._deprecated.SectionedAddSetAdapter;
import com.shibedays.workoutplanner.ui.dialogs.AddEditSetDialog;
import com.shibedays.workoutplanner._deprecated.SetBottomSheetDialog;

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
    private SectionedAddSetAdapter mLeftAdapter;
    private SectionedAddSetAdapter mRightAdapter;
    // UI Components
    private RecyclerView mLeftRecyclerView;
    private RecyclerView mRightRecyclerView;

    private CoordinatorLayout mCoordLayout; // Displaying Toasts / Undo bar

    // Parent
    private MyWorkoutActivity mParentActivity;


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

        mLeftAdapter = new SectionedAddSetAdapter(getContext(), SectionedAddSetAdapter.LEFT_VIEW, new SectionedAddSetAdapter.SectionedAddSetListener() {
            @Override
            public void onClick(Set set, int relativePos) {
                Log.d(DEBUG_TAG, "Left Adapter Default Clicked Add Set");
                addSetConfirmation(set);
            }

            @Override
            public void onLongClick(Set set, int section, int relativePos) {
                Log.d(DEBUG_TAG, "Left Adapter Default LongClicked Open Info");
                Bundle bundle = new Bundle();
                bundle.putInt(AddEditSetDialog.EXTRA_DIALOG_TYPE, AddEditSetDialog.DISPLAY_SET);
                bundle.putString(AddEditSetDialog.EXTRA_SET_NAME, set.getName());
                bundle.putString(AddEditSetDialog.EXTRA_SET_DESCIP, set.getDescrip());
                int[] time = MainActivity.convertFromMillis(set.getTime());
                bundle.putInt(AddEditSetDialog.EXTRA_SET_MIN, time[0]);
                bundle.putInt(AddEditSetDialog.EXTRA_SET_SEC, time[1]);
                AddEditSetDialog addEditSetDialog = new AddEditSetDialog();
                addEditSetDialog.setArguments(bundle);
                if(getFragmentManager() != null){
                    addEditSetDialog.show(getFragmentManager(), DEBUG_TAG);
                }
            }

            @Override
            public void createUserSet() {
                // Nothing
            }
        });
        mLeftRecyclerView.setAdapter(mLeftAdapter);
        mLeftAdapter.setDefaultSets(mDefaultSets);
        mLeftAdapter.shouldShowHeadersForEmptySections(true);
        mLeftAdapter.shouldShowFooters(false);
                //endregion

                //region RIGHT_RV
        mRightRecyclerView = view.findViewById(R.id.right_recyclerview);
        mRightRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mRightAdapter = new SectionedAddSetAdapter(getContext(), SectionedAddSetAdapter.RIGHT_VIEW, new SectionedAddSetAdapter.SectionedAddSetListener() {
            @Override
            public void onClick(Set set, int relativePos) {
                Log.d(DEBUG_TAG, "Right Adapter User Created Clicked Add Set");
                addSetConfirmation(set);
            }

            @Override
            public void onLongClick(Set set, int section, int relativePos) {
                Log.d(DEBUG_TAG, "Right Adapter User Created Long Clicked Open Bottom Sheet");
                Bundle bundle = new Bundle();
                bundle.putString(SetBottomSheetDialog.EXTRA_SET_NAME, set.getName());
                bundle.putInt(SetBottomSheetDialog.EXTRA_SET_INDEX, relativePos);
                bundle.putInt(SetBottomSheetDialog.EXTRA_SET_SECTION, MyWorkoutActivity.NEW_SET_SCREEN);
                SetBottomSheetDialog setBottomSheetDialog = new SetBottomSheetDialog();
                setBottomSheetDialog.setArguments(bundle);
                if(getFragmentManager() != null){
                    setBottomSheetDialog.show(getFragmentManager(), DEBUG_TAG);
                }
            }

            @Override
            public void createUserSet() {
                // TODO: Create user set
                Log.d(DEBUG_TAG, "Right Adapter Create New User Set");
                Bundle bundle = new Bundle();
                bundle.putInt(AddEditSetDialog.EXTRA_DIALOG_TYPE, AddEditSetDialog.NEW_SET);
                AddEditSetDialog addEditSetDialog = new AddEditSetDialog();
                addEditSetDialog.setArguments(bundle);
                if (getFragmentManager() != null) {
                    addEditSetDialog.show(getFragmentManager(), DEBUG_TAG);
                }
            }
        });
        mRightRecyclerView.setAdapter(mRightAdapter);
        mRightAdapter.setUserCreated(mUserCreatedSets);
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
    public void setListener(NewSetListener listener){
        mListener = listener;
    }

    public void setUserCreatedSets(List<Set> sets) {
        mUserCreatedSets = sets;
    }

    public void setDefaultSets(List<Set> sets){
        mDefaultSets = sets;
    }
    //endregion

    //region UTILITY
    public void addSetConfirmation(final Set set){
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

    public void addUserSet(Set set){
        mRightAdapter.addToUserCreated(set);
    }

    public void editUserSet(int pos, int section){
        Set set = mUserCreatedSets.get(pos);
        Bundle bundle = new Bundle();
        bundle.putInt(AddEditSetDialog.EXTRA_DIALOG_TYPE, AddEditSetDialog.EDIT_SET);
        bundle.putString(AddEditSetDialog.EXTRA_SET_NAME, set.getName());
        bundle.putString(AddEditSetDialog.EXTRA_SET_DESCIP, set.getDescrip());
        bundle.putInt(AddEditSetDialog.EXTRA_SET_INDEX, pos);
        int[] time = MainActivity.convertFromMillis(set.getTime());
        bundle.putInt(AddEditSetDialog.EXTRA_SET_MIN, time[0]);
        bundle.putInt(AddEditSetDialog.EXTRA_SET_SEC, time[1]);
        bundle.putInt(AddEditSetDialog.EXTRA_SET_SECTION, 0);
        AddEditSetDialog addEditSetDialog = new AddEditSetDialog();
        addEditSetDialog.setArguments(bundle);
        if(getFragmentManager() != null){
            addEditSetDialog.show(getFragmentManager(), DEBUG_TAG);
        }
    }
    public void deleteUserSet(int index){
        mUserCreatedSets.remove(index);
        mRightAdapter.notifyDataSetChanged();
    }

    public void updateRightAdapter() {
        mLeftAdapter.notifyDataSetChanged();
        mRightAdapter.notifyDataSetChanged();
    }
    //endregion




}
