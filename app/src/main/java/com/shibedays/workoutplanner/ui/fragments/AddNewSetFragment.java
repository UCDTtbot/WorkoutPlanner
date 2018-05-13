package com.shibedays.workoutplanner.ui.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
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
    private List<List<Set>> mTypedSetList;

    private List<SetListFragment> mSetListFrags;
    // Adapters
    private SectionedSetAdapter mLeftAdapter;
    private SectionedSetAdapter mRightAdapter;
    // UI Components
    private Button mSaveButton;

    private CoordinatorLayout mCoordLayout; // Displaying Toasts / Undo bar

    // Parent
    private MyWorkoutActivity mParentActivity;
    private AddNewSetFragment mThis;


    //endregion

    //region INTERFACES

    public interface NewSetListener {
        // TODO: Update argument type and name
        void addSetsToWorkout(List<Set> sets);
        void applyUserSetToDB(Set set);
        void removeUserSetFromDB(Set set);
    }
    private NewSetListener mListener;


    //endregion

    //region FACTORY_CONSTRUCTOR
    public AddNewSetFragment() {

    }

    public static AddNewSetFragment newInstance(List<List<Set>> sets, NewSetListener listener) {
        AddNewSetFragment newFragment = new AddNewSetFragment();

        newFragment.setTypedSets(sets);
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
                List<Set> sets = new ArrayList<>();
                for(SetListFragment s : mSetListFrags){
                    sets.addAll(s.getSelectedSets());
                }
                mListener.addSetsToWorkout(sets);
            }
        });

        //region RECYCLER_VIEWS

        /*

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
                    public void addEditResult(int dialogType, String name, String descrip, int min, int sec, int section, int index) {
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
                                    public void addEditResult(int dialogType, String name, String descrip, int min, int sec, int section, int index) {
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
                    public void addEditResult(int dialogType, String name, String descrip, int min, int sec, int section, int index) {
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



        */
        //endregion

        //region PAGER

        ViewPager viewPager = view.findViewById(R.id.pager);
        viewPager.setOffscreenPageLimit(Set.TYPES.length);
        ViewPagerAdapter adapter = new ViewPagerAdapter(getChildFragmentManager());

        mSetListFrags = new ArrayList<>();
        for(int i = 0; i < Set.TYPES.length; i++){
            boolean header = i == 0;
            SetListFragment frag = SetListFragment.newInstance(mTypedSetList.get(i), header, new SetListFragment.SetListListener() {

                @Override
                public void openBottomSheet(int setID) {
                    final Set set = getSetByID(setID);
                    if(set == null) throw new RuntimeException(DEBUG_TAG + " set came up null");
                    AddNewSetFragment.this.openBottomSheet(set, new BottomSheetDialog.BottomSheetDialogListener() {
                        @Override
                        public void bottomSheetResult(int resultCode) {
                            if(resultCode == BaseApp.EDIT){
                                openDialog(AddEditSetDialog.EDIT_SET, set, new AddEditSetDialog.AddEditSetDialogListener() {
                                    @Override
                                    public void newSet(String name, String descrip, int min, int sec) {
                                        throw new RuntimeException("Should not have reached this point " + DEBUG_TAG);
                                    }

                                    @Override
                                    public void editSet(int id, String name, String descrip, int min, int sec) {
                                        if(id == set.getSetId()){
                                            updateUserSet(set, name, descrip, min, sec);
                                        }
                                    }
                                });
                            } else if (resultCode == BaseApp.DELETE) {
                                deleteSetConfirmation(set);
                            } else {
                                Log.e(DEBUG_TAG, "Invalid Result Code " + resultCode);
                            }
                        }
                    });
                }

                @Override
                public void openSetDialog(int type, int setID) {

                    final Set set = getSetByID(setID);

                    if(type == AddEditSetDialog.NEW_SET){
                        openDialog(type, set, new AddEditSetDialog.AddEditSetDialogListener() {
                            @Override
                            public void newSet(String name, String descrip, int min, int sec) {
                                Set newSet = new Set(BaseApp.getNextSetID(), name, descrip, Set.USER_CREATED, BaseApp.convertToMillis(min, sec));
                                BaseApp.incrementSetID(getContext());
                                mTypedSetList.get(Set.USER_CREATED).add(newSet);
                                mSetListFrags.get(Set.USER_CREATED).setData(mTypedSetList.get(Set.USER_CREATED));
                                mSetListFrags.get(Set.USER_CREATED).notifyData();
                                mListener.applyUserSetToDB(newSet);
                            }

                            @Override
                            public void editSet(int id, String name, String descrip, int min, int sec) {
                                throw new RuntimeException("Should not have reached this point " + DEBUG_TAG);
                            }
                        });
                    } else if (type == AddEditSetDialog.EDIT_SET) {
                        openDialog(type, set, new AddEditSetDialog.AddEditSetDialogListener() {
                            @Override
                            public void newSet(String name, String descrip, int min, int sec) {
                                throw new RuntimeException("Should not have reached this point " + DEBUG_TAG);
                            }

                            @Override
                            public void editSet(int id, String name, String descrip, int min, int sec) {
                                updateUserSet(set, name, descrip, min, sec);
                            }
                        });
                    } else if (type == AddEditSetDialog.DISPLAY_SET) {
                        openDialog(type, set, null);
                    } else {
                        throw new RuntimeException(DEBUG_TAG + " invalid AddEditSetDialog type");
                    }
                }
            });
            adapter.addFragment(frag, Set.TYPES[i]);
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

    private void openDialog(int type, Set set, AddEditSetDialog.AddEditSetDialogListener listener) {
        Bundle bundle = null;
        if( (type == AddEditSetDialog.EDIT_SET) && set != null){
            bundle = AddEditSetDialog.getDialogBundle(type, set.getSetId(), set.getName(), set.getDescrip(), set.getTime());
        } else if(type == AddEditSetDialog.NEW_SET){
            bundle = AddEditSetDialog.getDialogBundle(type, -1, "", "", -1);
        } else if (type == AddEditSetDialog.DISPLAY_SET){
            bundle = AddEditSetDialog.getDialogBundle(type, set.getSetId(), set.getName(), set.getDescrip(), set.getTime());
        } else {
            throw new RuntimeException(DEBUG_TAG + "dialog type was invalid" + type);
        }
        AddEditSetDialog dialog = AddEditSetDialog.newInstance(bundle, listener);
        dialog.setTargetFragment(mThis, 0);
        if (getFragmentManager() != null) {
            dialog.show(getFragmentManager(), DEBUG_TAG);
        }
    }

    //endregion

    //region DATA_FUNCTIONS
    private void updateUserSet(Set set, String name, String descrip, int min, int sec){
        if(set != null) {
            set.setName(name);
            set.setDescrip(descrip);
            set.setTime(BaseApp.convertToMillis(min, sec));
            mSetListFrags.get(Set.USER_CREATED).updateSet(set);
            mTypedSetList.get(Set.USER_CREATED).set(mTypedSetList.get(Set.USER_CREATED).indexOf(set), set);
            mListener.applyUserSetToDB(set);
        } else {
            throw new RuntimeException(DEBUG_TAG + " trying to update set, was null");
        }
    }

    private void deleteUserSet(Set set){
        mListener.removeUserSetFromDB(set);
        mSetListFrags.get(Set.USER_CREATED).removeSet(set);
        mTypedSetList.get(Set.USER_CREATED).remove(set);
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

    private Set getSetByID(int setID){
        for(Set s : mTypedSetList.get(Set.USER_CREATED)){
            if (s.getSetId() == setID) return s;
        }

        return null;
    }
    //endregion

    //region SETTERS
    private void setListener(NewSetListener listener){
        mListener = listener;
    }

    public void setTypedSets(List<List<Set>> sets){
        mTypedSetList = sets;
    }
    //endregion




}
