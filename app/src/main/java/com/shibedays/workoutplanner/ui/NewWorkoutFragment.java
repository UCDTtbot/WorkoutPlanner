package com.shibedays.workoutplanner.ui;

import android.app.Activity;
import android.arch.persistence.room.util.StringUtil;
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
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.amitshekhar.utils.Utils;
import com.shibedays.workoutplanner.R;
import com.shibedays.workoutplanner.db.entities.Set;
import com.shibedays.workoutplanner.db.entities.Workout;
import com.shibedays.workoutplanner.ui.adapters.sectioned.SectionedSetAdapter;
import com.shibedays.workoutplanner.ui.dialogs.AddEditSetDialog;
import com.shibedays.workoutplanner.ui.dialogs.NumberPickerDialog;
import com.shibedays.workoutplanner.ui.dialogs.SetBottomSheetDialog;
import com.shibedays.workoutplanner.ui.helpers.ListItemTouchHelper;
import com.shibedays.workoutplanner.ui.helpers.SectionedListItemTouchHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.crypto.spec.DESedeKeySpec;

public class NewWorkoutFragment extends Fragment{
    //region CONSTANTS
    // Factory Constant
    private static final String ARG_WORKOUT = "WORKOUT";
    // Package and Debug Constants
    private static final String PACKAGE = "com.shibedays.workoutplanner.ui.NewWorkoutFragment.";
    private static final String DEBUG_TAG = NewWorkoutFragment.class.getSimpleName();
    public static int LEFT_SIDE = 0;
    public static int RIGHT_SIDE = 1;
    //endregion

    //region PRIVATE_VARS
    // Data
    private List<Set> mDefaultSets;
    private List<Set> mUserCreatedSets;
    private List<Set> mUsersSets;

    private int mRounds;
    private int mRestTime;
    private int mBreakTime;
    private boolean mRestFlag;
    private boolean mBreakFlag;
    // Adapters
    //private SetAdapter mLeftAdapter;
    //private SetAdapter mRightAdapter;
    private SectionedSetAdapter mLeftAdapter;
    private SectionedSetAdapter mRightAdapter;
    // UI Components
    private RecyclerView mLeftRecyclerView;
    private RecyclerView mRightRecyclerView;

    private CoordinatorLayout mCoordLayout; // Displaying Toasts / Undo bar

    private EditText mNameEntry;
    private EditText mRoundEntry;
    private TextView mRestEntry;
    private TextView mBreakEntry;

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
        void addNewWorkout(Workout workout);
    }
    private OnFragmentInteractionListener mListener;
    //endregion

    //region FACTORY_CONSTRUCTORS
    // Empty default constructor
    public NewWorkoutFragment() {
        // Required empty public constructor
    }


    public static NewWorkoutFragment newInstance(List<Set> sets) {
        NewWorkoutFragment newFragment = new NewWorkoutFragment();
        Bundle args = new Bundle();
        newFragment.setArguments(args);
        newFragment.setUserCreatedSets(sets);

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
        mUsersSets = new ArrayList<>();

        // Hardcoded default sets
        mDefaultSets.add(new Set("Jogging", "Light Jog", 90000));
        mDefaultSets.add(new Set("Walk", "Brisk walk", 30000));
        mDefaultSets.add(new Set("Pushups", "As many pushups as possible in the time limit", 45000));
        mDefaultSets.add(new Set("Situps", "Arms across chest", 45000));

        mRounds = 1;
        mRestTime = 60000;
        mBreakTime = 60000;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        //region UI

        View view = inflater.inflate(R.layout.fragment_add_workout, container, false);
        mCoordLayout = mParentActivity.findViewById(R.id.main_coord_layout);

        mNameEntry = view.findViewById(R.id.name_entry);

        //region RECYCLER_VIEWS

        //region LEFT_RV
        mLeftRecyclerView = view.findViewById(R.id.left_recyclerview);
        mLeftRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mLeftAdapter = new SectionedSetAdapter(getContext(), SectionedSetAdapter.LEFT_VIEW, new SectionedSetAdapter.SectionedSetListener() {
            @Override
            public void onClick(Set setToAdd, int relPos) {
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
            public void onDefaultLongClick(Set set, int section, int relPos) {
                // TODO: Display Info
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
                Log.d(DEBUG_TAG, "Left Adapter Default Long Click: Display Info");
            }

            @Override
            public void onUserLongClicked(Set set, int section, int relPos) {
                // TODO: User - Bottom Sheet (Edit/Delete)
                Bundle bundle = new Bundle();
                bundle.putString(SetBottomSheetDialog.EXTRA_SET_NAME, set.getName());
                bundle.putInt(SetBottomSheetDialog.EXTRA_SET_INDEX, relPos);
                bundle.putInt(SetBottomSheetDialog.EXTRA_SET_SECTION, LEFT_SIDE);
                SetBottomSheetDialog setBottomSheetDialog = new SetBottomSheetDialog();
                setBottomSheetDialog.setArguments(bundle);
                if(getFragmentManager() != null){
                    setBottomSheetDialog.show(getFragmentManager(), DEBUG_TAG);
                }
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
            public void onClick(Set set, int relPos) {
                Bundle bundle = new Bundle();
                bundle.putString(SetBottomSheetDialog.EXTRA_SET_NAME, set.getName());
                bundle.putInt(SetBottomSheetDialog.EXTRA_SET_INDEX, relPos);
                bundle.putInt(SetBottomSheetDialog.EXTRA_SET_SECTION, RIGHT_SIDE);
                SetBottomSheetDialog setBottomSheetDialog = new SetBottomSheetDialog();
                setBottomSheetDialog.setArguments(bundle);
                if(getFragmentManager() != null){
                    setBottomSheetDialog.show(getFragmentManager(), DEBUG_TAG);
                }
                Log.d(DEBUG_TAG, "Right Adapter Clicked: Bottom Sheet");
            }

            @Override
            public void createUserSet() {
                throw new RuntimeException(DEBUG_TAG + " createUserSet called within mRightAdapter. Footer shouldn't exist");
            }

            @Override
            public void onDefaultLongClick(Set set, int section, int relPos) {

            }

            @Override
            public void onUserLongClicked(Set set, int section, int relPos) {
                Bundle bundle = new Bundle();
                bundle.putString(SetBottomSheetDialog.EXTRA_SET_NAME, set.getName());
                bundle.putInt(SetBottomSheetDialog.EXTRA_SET_INDEX, relPos);
                bundle.putInt(SetBottomSheetDialog.EXTRA_SET_SECTION, RIGHT_SIDE);
                SetBottomSheetDialog setBottomSheetDialog = new SetBottomSheetDialog();
                setBottomSheetDialog.setArguments(bundle);
                if(getFragmentManager() != null){
                    setBottomSheetDialog.show(getFragmentManager(), DEBUG_TAG);
                }
                Log.d(DEBUG_TAG, "Right Adapter Long Clicked: Bottom Sheet");
            }
        });
        mRightRecyclerView.setAdapter(mRightAdapter);
        mRightAdapter.setUserSets(mUsersSets);
        mRightAdapter.shouldShowHeadersForEmptySections(true);
        mRightAdapter.shouldShowFooters(false);

        //region ITEM_TOUCH_HELPER
        /*
        SectionedListItemTouchHelper rightItemHelper = new SectionedListItemTouchHelper(getContext(), false, true, 0, mRightRecyclerView, false){
            @Override
            public void instantiateUnderlayButton(RecyclerView.ViewHolder viewHolder, Context context, List<UnderlayButton> underlayButtons) {
                underlayButtons.add(new UnderlayButton(R.drawable.ic_delete_white_24dp,
                        ResourcesCompat.getColor(getResources(), R.color.material_red_500, null), getContext(), new UnderlayButtonClickListener() {
                    @Override
                    public void onDeleteButtonClick(int pos) {
                        mRightAdapter.removeFromUserSets(pos - 1); // pos in this case is absolute, so ( - 1) to account for the header
                        Log.d(DEBUG_TAG, Integer.toString(mUsersSets.size()));
                    }
                }));
            }
        };
        */
        //endregion
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

        mRoundEntry = view.findViewById(R.id.round_entry_num);
        mRoundEntry.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //TODO: Round input validation
                if(s.length() > 2){
                    String working = s.subSequence(0, 2).toString();
                    mRoundEntry.setText(working);
                    mRoundEntry.setSelection( (start + 1) <= working.length() ? start + 1 : working.length());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        mRestEntry = view.findViewById(R.id.rest_entry_time);
        mRestEntry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openNumberPicker(NumberPickerDialog.REST_TYPE, mRestTime);
            }
        });

        mBreakEntry = view.findViewById(R.id.break_entry_time);
        mBreakEntry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openNumberPicker(NumberPickerDialog.BREAK_TYPE, mBreakTime);
            }
        });

        mSaveButton = view.findViewById(R.id.button_save);
        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: New workout
                Log.d(DEBUG_TAG, "Adding working: " + mNameEntry.getText().toString());
                saveWorkout();
            }
        });

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

    //region USER_CREATED_SETS
    public void editUserCreatedSet(int pos, int section){
        Set set = mUserCreatedSets.get(pos);
        Bundle bundle = new Bundle();
        bundle.putInt(AddEditSetDialog.EXTRA_DIALOG_TYPE, AddEditSetDialog.EDIT_SET);
        bundle.putString(AddEditSetDialog.EXTRA_SET_NAME, set.getName());
        bundle.putString(AddEditSetDialog.EXTRA_SET_DESCIP, set.getDescrip());
        int[] time = MainActivity.convertFromMillis(set.getTime());
        bundle.putInt(AddEditSetDialog.EXTRA_SET_MIN, time[0]);
        bundle.putInt(AddEditSetDialog.EXTRA_SET_SEC, time[1]);
        bundle.putInt(AddEditSetDialog.EXTRA_SET_SECTION, section);
        AddEditSetDialog addEditSetDialog = new AddEditSetDialog();
        addEditSetDialog.setArguments(bundle);
        if(getFragmentManager() != null){
            addEditSetDialog.show(getFragmentManager(), DEBUG_TAG);
        }
    }
    public void updateUserCreatedSet(int index, String name, String descrip, int min, int sec){
        Set set = mUserCreatedSets.get(index);
        set.setName(name);
        set.setDescrip(descrip);
        set.setTime(MainActivity.convertToMillis(min, sec));
        mLeftAdapter.notifyDataSetChanged();
    }
    public void deleteUserCreatedSet(int pos){
        mUserCreatedSets.remove(pos);
        mLeftAdapter.notifyDataSetChanged();
    }

    public void addUserCreatedSet(Set set){
        mLeftAdapter.addToUserCreated(set);
    }
    public void setUserCreatedSets(List<Set> sets){
        mUserCreatedSets = sets;
    }
    //endregion

    //region USER_SETS
    public void editUserSet(int pos, int section) {
        Set set = mUsersSets.get(pos);
        Bundle bundle = new Bundle();
        bundle.putInt(AddEditSetDialog.EXTRA_DIALOG_TYPE, AddEditSetDialog.EDIT_SET);
        bundle.putString(AddEditSetDialog.EXTRA_SET_NAME, set.getName());
        bundle.putString(AddEditSetDialog.EXTRA_SET_DESCIP, set.getDescrip());
        int[] time = MainActivity.convertFromMillis(set.getTime());
        bundle.putInt(AddEditSetDialog.EXTRA_SET_MIN, time[0]);
        bundle.putInt(AddEditSetDialog.EXTRA_SET_SEC, time[1]);
        bundle.putInt(AddEditSetDialog.EXTRA_SET_SECTION, section);
        AddEditSetDialog addEditSetDialog = new AddEditSetDialog();
        addEditSetDialog.setArguments(bundle);
        if(getFragmentManager() != null){
            addEditSetDialog.show(getFragmentManager(), DEBUG_TAG);
        }
    }
    public void updateUserSet(int index, String name, String descrip, int min, int sec){
        Set set = mUsersSets.get(index);
        set.setName(name);
        set.setDescrip(descrip);
        set.setTime(MainActivity.convertToMillis(min, sec));
        mRightAdapter.notifyDataSetChanged();
    }
    public void deleteUserSet(int pos){
        mRightAdapter.removeFromUserSets(pos);
    }
    //endregion

    //region NUMBER_PICKER
    public void setRestTime(int min, int sec, boolean flag) {
        if(flag){
            mRestEntry.setText(R.string.none_text);
        }else if( sec == 0 ){
            mRestEntry.setText(String.format(Locale.US, "%d:%d%d", min, sec, 0));
        } else if ( (sec % 10) == 0 ) {
            mRestEntry.setText(String.format(Locale.US, "%d:%d", min, sec));
        } else if ( sec < 10 ) {
            mRestEntry.setText(String.format(Locale.US, "%d:%d%d", min, 0, sec));
        }else {
            mRestEntry.setText(String.format(Locale.US, "%d:%d", min, sec));
        }

        mRestTime = MainActivity.convertToMillis(min, sec);
    }

    public void setBreakTime(int min, int sec, boolean flag) {
        if(flag){
            mBreakEntry.setText(R.string.none_text);
        }else if((sec % 10) == 0){
            mBreakEntry.setText(String.format(Locale.US, "%d:%d", min, sec));
        } else if (min == 0 && sec == 0) {
            mBreakEntry.setText(String.format(Locale.US, "%d:%d%d", min, 0, sec));
        } else if ( sec < 10 ) {
            mBreakEntry.setText(String.format(Locale.US, "%d:%d%d", min, 0, sec));
        }else {
            mBreakEntry.setText(String.format(Locale.US, "%d:%d", min, sec));
        }

        mBreakTime = MainActivity.convertToMillis(min, sec);
    }

    private void openNumberPicker(int type, int time){
        NumberPickerDialog dialog = new NumberPickerDialog();
        Bundle bundle = new Bundle();
        bundle.putInt(NumberPickerDialog.EXTRA_DIALOG_TYPE, type);
        bundle.putInt(NumberPickerDialog.EXTRA_GIVEN_TIME, time); // default to 1 minute
        bundle.putBoolean(NumberPickerDialog.EXTRA_NO_FLAG, false);
        dialog.setArguments(bundle);
        if(getFragmentManager() != null) {
            dialog.show(getFragmentManager(), DEBUG_TAG);
        }
    }
    //endregion

    public void saveWorkout(){
        boolean isOk = true;

        String name = mNameEntry.getText().toString();
        int restTime[] = MainActivity.convertFromMillis(mRestTime);
        int breakTime[] = MainActivity.convertFromMillis(mBreakTime);
        int rounds = 0;
        List<Set> setList = mUsersSets;
        if(TextUtils.isEmpty(name)){
            mNameEntry.setError("Name can not be empty.");
            isOk = false;
        }

        if(!TextUtils.isEmpty(mRoundEntry.getText())){
            rounds = Integer.parseInt(mRoundEntry.getText().toString());
        } else {
            mRoundEntry.setError("Must choose number of rounds.");
            isOk = false;
        }

        if(restTime[0] == 0 && restTime[1] == 0){
            mRestEntry.setError("Time cannot be 0:00!");
            isOk = false;
        }

        if(breakTime[0] == 0 && breakTime[1] == 0){
            mBreakEntry.setError("Time cannot be 0:00!");
            isOk = false;
        }

        if(setList.isEmpty()){
            Toast.makeText(mParentActivity, "Choose at least 1 set.", Toast.LENGTH_SHORT).show();
            isOk = false;
        }

        if(isOk) {
            Workout workout = new Workout(MainActivity.NEXT_WORKOUT_ID, name);
            workout.setNumOfRounds(rounds);
            workout.setNoRestFlag(mRestFlag);
            workout.setNoBreakFlag(mBreakFlag);
            workout.setTimeBetweenSets(MainActivity.convertToMillis(restTime));
            workout.setTimeBetweenRounds(MainActivity.convertToMillis(breakTime));
            workout.addSets(setList);
            mListener.addNewWorkout(workout);
        }
    }
}
