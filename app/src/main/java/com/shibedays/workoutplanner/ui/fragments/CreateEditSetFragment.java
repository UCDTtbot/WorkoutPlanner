package com.shibedays.workoutplanner.ui.fragments;


import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.shawnlin.numberpicker.NumberPicker;
import com.shibedays.workoutplanner.BaseApp;
import com.shibedays.workoutplanner.R;
import com.shibedays.workoutplanner.ui.MainActivity;

import java.util.Locale;

public class CreateEditSetFragment extends Fragment {

    //region CONSTANTS
    // Package and Debug Constants
    private static final String DEBUG_TAG = CreateEditSetFragment.class.getSimpleName();
    private static final String PACKAGE = "com.shibedays.workoutplanner.ui.fragments.CreateEditSetFragment.";

    //endregion

    //region INTENT_KEYS
    public static final String EXTRA_SET_NAME = PACKAGE + "SET_NAME";
    public static final String EXTRA_SET_DESCIP = PACKAGE + "SET_DESCRIP";
    public static final String EXTRA_SET_MIN = PACKAGE + "SET_MIN";
    public static final String EXTRA_SET_ID = PACKAGE + "SET_ID";
    public static final String EXTRA_SET_SEC = PACKAGE + "SET_SEC";
    //endregion

    //region PRIVATE_VARS
    // Data
    private String mName;
    private String mDescrip;
    private int mMins;
    private int mSecs;
    private int mId;

    private int mParentTitle;

    // UI
    private ImageView mChooseImage;
    private EditText mEditName;
    private EditText mEditDescip;
    private NumberPicker mMinSpinner;
    private NumberPicker mSecSpinner;
    private Button mSaveButton;

    //endregion

    //region INTERFACES
    public interface CreateEditSetListener {
        void returnData(String name, String descrip, int min, int sec, int imageId);
    }
    CreateEditSetListener mListener;
    //endregion

    //region CONSTRUCTORS
    public CreateEditSetFragment() {
        // Required empty public constructor
    }


    public static CreateEditSetFragment newInstance(int parentName, Bundle args, CreateEditSetListener listener) {
        CreateEditSetFragment fragment = new CreateEditSetFragment();
        fragment.setListener(listener);
        fragment.setArguments(args);
        fragment.setParentTitle(parentName);
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

        if(args != null) {
            mName = args.getString(EXTRA_SET_NAME, "");
            mDescrip = args.getString(EXTRA_SET_DESCIP, "");
            mId = args.getInt(EXTRA_SET_ID, -1);
            mMins = args.getInt(EXTRA_SET_MIN, 0);
            mSecs = args.getInt(EXTRA_SET_SEC, 0);
        }
        Activity act = getActivity();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_edit_set, container, false);

        mChooseImage = view.findViewById(R.id.choose_image);

        mEditName = view.findViewById(R.id.new_set_name);
        mEditDescip = view.findViewById(R.id.new_set_descrip);
        View spinners = view.findViewById(R.id.spinners);
        mMinSpinner = spinners.findViewById(R.id.MinutePicker);
        mSecSpinner = spinners.findViewById(R.id.SecondsPicker);
        mSaveButton = view.findViewById(R.id.button_save);

        mEditName.setText(mName);
        mEditDescip.setText(mDescrip);

        // TODO: Set Image
        mChooseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "New Image", Toast.LENGTH_SHORT).show();
            }
        });

        mMinSpinner.setMinValue(0);
        mMinSpinner.setMaxValue(30);
        mMinSpinner.setWrapSelectorWheel(true);
        mMinSpinner.setFadingEdgeEnabled(true);
        mMinSpinner.setValue(mMins);

        mSecSpinner.setMinValue(0);
        mSecSpinner.setMaxValue(59);
        mSecSpinner.setFormatter(new NumberPicker.Formatter() {
            @Override
            public String format(int value) {
                return String.format(Locale.US, "%02d", value);
            }
        });
        mSecSpinner.setWrapSelectorWheel(true);
        mSecSpinner.setFadingEdgeEnabled(true);
        mSecSpinner.setValue(mSecs);


        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateSet();
            }
        });
        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        getActivity().setTitle(mParentTitle);
        if(getFragmentManager().getBackStackEntryCount() <= 0){
            MainActivity m = (MainActivity) getActivity();
            m.findViewById(R.id.new_workout_fragment_container).setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    //endregion

    //region UTILITY
    public static Bundle getBundle(int id, String setName, String setDescrip, int timeInMil){
        Bundle bundle = new Bundle();

        int[] time = BaseApp.convertFromMillis(timeInMil);

        bundle.putInt(EXTRA_SET_ID, id);
        bundle.putString(EXTRA_SET_NAME, setName);
        bundle.putString(EXTRA_SET_DESCIP, setDescrip);
        bundle.putInt(EXTRA_SET_MIN, time[0]);
        bundle.putInt(EXTRA_SET_SEC, time[1]);

        return bundle;
    }

    private void setListener(CreateEditSetListener listener ){
        mListener = listener;
    }

    private void setParentTitle(int name){
        mParentTitle = name;
    }

    private void validateSet(){
        // TODO: Input validation
        Boolean OK = true;

        String name = mEditName.getText().toString();
        if(TextUtils.isEmpty(name)){
            mEditName.setError(getString(R.string.name_error));
            OK = false;
        }
        String descrip = mEditDescip.getText().toString();
        int min = mMinSpinner.getValue();
        int sec = mSecSpinner.getValue();
        if(min == 0 && sec == 0){
            Toast.makeText(getContext(), R.string.time_error, Toast.LENGTH_SHORT).show();
            OK = false;
        }
        if(OK) {
            mListener.returnData(name, descrip, min, sec, R.drawable.android);

            View view = getActivity().getCurrentFocus();
            if(view != null) {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                if(imm != null) {
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
            }
            getFragmentManager().popBackStack();
        } else {
            Log.d(DEBUG_TAG, "Input Validation Error");
        }
    }
    //endregion

}
