package com.shibedays.workoutplanner.ui;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.shibedays.workoutplanner.R;
import com.shibedays.workoutplanner.db.entities.Set;
import com.shibedays.workoutplanner.db.entities.Workout;
import com.shibedays.workoutplanner.ui.adapters.sectioned.SectionedSetAdapter;
import com.shibedays.workoutplanner.ui.dialogs.AddEditSetDialog;
import com.shibedays.workoutplanner.ui.helpers.ListItemTouchHelper;
import com.shibedays.workoutplanner.ui.helpers.SectionedListItemTouchHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.crypto.spec.DESedeKeySpec;

public class NewWorkoutFragment extends Fragment {
    //region CONSTANTS
    // Factory Constant
    private static final String ARG_WORKOUT = "WORKOUT";
    // Package and Debug Constants
    private static final String PACKAGE = "com.shibedays.workoutplanner.ui.NewWorkoutFragment.";
    private static final String DEBUG_TAG = NewWorkoutFragment.class.getSimpleName();
    //endregion

    //region PRIVATE_VARS
    // Data
    private List<Set> mDefaultSets;
    private List<Set> mUserCreatedSets;
    private List<Set> mUsersSets;
    // Adapters
    //private SetAdapter mLeftAdapter;
    //private SetAdapter mRightAdapter;
    private SectionedSetAdapter mLeftAdapter;
    private SectionedSetAdapter mRightAdapter;
    // UI Components
    private RecyclerView mLeftRecyclerView;
    private RecyclerView mRightRecyclerView;
    private CoordinatorLayout mCoordLayout;
    private Button mSaveButton;
    // Parent
    private MainActivity mParentActivity;
    //endregion

    //region PUBLIC_VARS

    //endregion

    //region INTERFACES
    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface OnFragmentInteractionListener {
        void addNewWorkout();
    }
    private OnFragmentInteractionListener mListener;
    //endregion

    //region FACTORY_CONSTRUCTORS
    // Empty default constructor
    public NewWorkoutFragment() {
        // Required empty public constructor
    }


    public static NewWorkoutFragment newInstance() {
        NewWorkoutFragment newFragment = new NewWorkoutFragment();
        Bundle args = new Bundle();
        newFragment.setArguments(args);

        return newFragment;
    }
    //endregion

    //region LIFECYCLE

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if(context instanceof OnFragmentInteractionListener){
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
        Activity act = getActivity();
        if(act instanceof MainActivity){
            mParentActivity = (MainActivity) act;
        } else {
            throw new RuntimeException(DEBUG_TAG + " wasn't called from MainActivity");
        }
    }

    // onCreate for data
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDefaultSets = new ArrayList<>();
        mUserCreatedSets = new ArrayList<>();
        mUsersSets = new ArrayList<>();
        mDefaultSets.add(new Set("Set 1", "Descrip 1", 10000));
        mDefaultSets.add(new Set("Set 2", "Descrip 1", 10000));
        mUserCreatedSets.add(new Set("User Created", "Made by Tyler", 50000));
        mUsersSets.add(new Set("My First Set", "First Set", 60000));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        //region UI
        View view = inflater.inflate(R.layout.fragment_add_workout, container, false);
        mSaveButton = view.findViewById(R.id.button_save);
        mCoordLayout = mParentActivity.findViewById(R.id.main_coord_layout);
        //endregion

        //region RECYCLER_VIEWS
            //region LEFT_RV
        mLeftRecyclerView = view.findViewById(R.id.left_recyclerview);
        mLeftRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mLeftAdapter = new SectionedSetAdapter(getContext(), SectionedSetAdapter.LEFT_VIEW, new SectionedSetAdapter.SectionedSetListener() {
            @Override
            public void onClick(Set setToAdd) {
                mRightAdapter.addToUserSets(setToAdd);
                Log.d(DEBUG_TAG, Integer.toString(mUsersSets.size()));
            }

            @Override
            public void createUserSet() {
                // TODO: Create a new set
                Bundle bundle = new Bundle();
                bundle.putInt(AddEditSetDialog.EXTRA_DIALOG_TYPE, AddEditSetDialog.NEW_SET);
                AddEditSetDialog addEditSetDialog = new AddEditSetDialog();
                addEditSetDialog.setArguments(bundle);
                if (getFragmentManager() != null) {
                    addEditSetDialog.show(getFragmentManager(), DEBUG_TAG);
                }
            }

            @Override
            public void onLongClicked(Set set) {
                // TODO: Display Info
                Log.d(DEBUG_TAG, "Left Adapter Default Long Click: Display Info");
            }

            @Override
            public void onUserCreatedLongClicked(Set set) {
                // TODO: User - Bottom Sheet (Edit/Delete)
                Log.d(DEBUG_TAG, "Left Adapter User Created Long Click: Bottom Sheet");
            }
        });
        mLeftRecyclerView.setAdapter(mLeftAdapter);
        mLeftAdapter.setDefaultSets(mDefaultSets);
        mLeftAdapter.setUserCreated(mUserCreatedSets);
        mLeftAdapter.shouldShowHeadersForEmptySections(true);
        mLeftAdapter.shouldShowFooters(true);

            //endregion

            //region RIGHT_RV
        mRightRecyclerView = view.findViewById(R.id.right_recyclerview);
        mRightRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mRightAdapter = new SectionedSetAdapter(getContext(), SectionedSetAdapter.RIGHT_VIEW, new SectionedSetAdapter.SectionedSetListener() {
            @Override
            public void onClick(Set set) {
                // Nothing
            }

            @Override
            public void createUserSet() {
                throw new RuntimeException(DEBUG_TAG + " createUserSet called within mRightAdapter. Footer shouldn't exist");
            }

            @Override
            public void onLongClicked(Set set) {
                // TODO: Bottom Sheet (Edit/Delete)
                Log.d(DEBUG_TAG, "Right Adapter Long Clicked: Bottom Sheet");
            }

            @Override
            public void onUserCreatedLongClicked(Set set) {
                // Nothing
            }
        });
        mRightRecyclerView.setAdapter(mRightAdapter);
        mRightAdapter.setUserSets(mUsersSets);
        mRightAdapter.shouldShowHeadersForEmptySections(true);
        mRightAdapter.shouldShowFooters(false);

        SectionedListItemTouchHelper rightItemHelper = new SectionedListItemTouchHelper(getContext(), false, true, 0, mRightRecyclerView, false){
            @Override
            public void instantiateUnderlayButton(RecyclerView.ViewHolder viewHolder, Context context, List<UnderlayButton> underlayButtons) {
                underlayButtons.add(new UnderlayButton(R.drawable.ic_delete_white_24dp,
                        ResourcesCompat.getColor(getResources(), R.color.material_red_500, null), getContext(), new UnderlayButtonClickListener() {
                    @Override
                    public void onDeleteButtonClick(int pos) {
                        mRightAdapter.removeFromUserSets(pos);
                        Log.d(DEBUG_TAG, Integer.toString(mUsersSets.size()));
                    }
                }));
            }
        };

        //region old_code
        /*
        mLeftAdapter = new SetAdapter(getContext(), mCoordLayout, false);
        mLeftRecyclerView.setAdapter(mLeftAdapter);
        mLeftAdapter.setData(mDefaultSets);
        mLeftAdapter.notifyDataSetChanged();

        mRightAdapter = new SetAdapter(getContext(), mCoordLayout, true);
        mRightRecyclerView.setAdapter(mRightAdapter);
        mRightAdapter.setData(mUserCreatedSets);
        mRightAdapter.notifyDataSetChanged();
        */
        //endregion
            //endregion
        //endregion
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(DEBUG_TAG, "NEW_WORKOUT_FRAGMENT ON_START");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(DEBUG_TAG, "NEW_WORKOUT_FRAGMENT ON_RESUME");

    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(DEBUG_TAG, "NEW_WORKOUT_FRAGMENT ON_PAUSE");

    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(DEBUG_TAG, "NEW_WORKOUT_FRAGMENT ON_STOP");

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mParentActivity.findViewById(R.id.new_workout_fragment_container).setVisibility(View.GONE);
        mParentActivity.findViewById(R.id.fab).setVisibility(View.VISIBLE);
        mParentActivity.toggleUpArrow(false);
        Log.d(DEBUG_TAG, "NEW_WORKOUT_FRAGMENT ON_DESTROY");

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
        Log.d(DEBUG_TAG, "NEW_WORKOUT_FRAGMENT SAVING INSTANCE STATE");
    }

    //endregion

    //region TOOLBAR_MENU

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    //endregion

    public void deleteSet(Set set){
        Log.d(DEBUG_TAG, "Should be deleting?");
    }


    public void addUserCreatedSet(Set set){
        mLeftAdapter.addToUserCreated(set);
    }

}
