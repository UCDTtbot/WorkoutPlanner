package com.shibedays.workoutplanner.ui.fragments;


import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
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
import com.shibedays.workoutplanner.db.entities.Set;
import com.shibedays.workoutplanner.ui.MainActivity;
import com.shibedays.workoutplanner.ui.dialogs.ChooseImageDialog;
import com.shibedays.workoutplanner.viewmodel.fragments.CreateEditViewModel;
import com.shibedays.workoutplanner.viewmodel.SetViewModel;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
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
    public static final String EXTRA_IMAGE_ID = PACKAGE + "SET_IMAGE_ID";
    //endregion

    private static WeakReference<CreateEditSetFragment> mInstance;

    //region PRIVATE_VARS
    // Data
    private CreateEditViewModel mViewModel;
    private SetViewModel mSetViewModel;

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

    }
    CreateEditSetListener mListener;
    //endregion

    //region CONSTRUCTORS
    public CreateEditSetFragment() {
        // Required empty public constructor
    }


    public static CreateEditSetFragment newInstance(int parentName, Bundle args) {
        if(mInstance == null) {
            mInstance = new WeakReference<>(new CreateEditSetFragment());
            mInstance.get().setArguments(args);
            mInstance.get().setParentTitle(parentName);
            return mInstance.get();
        } else {
            return mInstance.get();
        }
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

        mViewModel = ViewModelProviders.of(this).get(CreateEditViewModel.class);
        mSetViewModel = ViewModelProviders.of(this).get(SetViewModel.class);

        if(args != null) {
            mViewModel.setName(args.getString(EXTRA_SET_NAME, ""));
            mViewModel.setDescrip(args.getString(EXTRA_SET_DESCIP, ""));
            mViewModel.setId(args.getInt(EXTRA_SET_ID, -1));
            mViewModel.setMins(args.getInt(EXTRA_SET_MIN, 0));
            mViewModel.setSecs(args.getInt(EXTRA_SET_SEC, 0));
            mViewModel.setImage(args.getInt(EXTRA_IMAGE_ID, R.drawable.ic_fitness_black_24dp));
            mViewModel.setupDefaultImages();
        }
        Activity act = getActivity();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_edit_set, container, false);

        mChooseImage = view.findViewById(R.id.choose_image);
        mChooseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle args = ChooseImageDialog.getDialogBundle(mViewModel.getDefaultImageIds(), mViewModel.getImage());
                ChooseImageDialog dialog = ChooseImageDialog.newInstance(args, new ChooseImageDialog.ChooseImageListener() {
                    @Override
                    public void dialogResult(int image_id) {
                        mViewModel.setImage(image_id);
                        mChooseImage.setImageResource(mViewModel.getImage());
                    }
                });
                dialog.setTargetFragment(mInstance.get(), 0);
                if (getFragmentManager() != null) {
                    dialog.show(getFragmentManager(), DEBUG_TAG);
                }
            }
        });
        mChooseImage.setImageResource(mViewModel.getImage());


        mEditName = view.findViewById(R.id.new_set_name);
        mEditName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mViewModel.setName(s.toString());
            }
        });
        mEditName.setText(mViewModel.getName());


        mEditDescip = view.findViewById(R.id.new_set_descrip);
        mEditDescip.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mViewModel.setDescrip(s.toString());
            }
        });
        mEditDescip.setText(mViewModel.getDescrip());


        View spinners = view.findViewById(R.id.spinners);
        mMinSpinner = spinners.findViewById(R.id.MinutePicker);
        mMinSpinner.setMinValue(0);
        mMinSpinner.setMaxValue(30);
        mMinSpinner.setWrapSelectorWheel(true);
        mMinSpinner.setFadingEdgeEnabled(true);
        mMinSpinner.setValue(mViewModel.getMins());
        mMinSpinner.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                if(oldVal != newVal){
                    mViewModel.setMins(newVal);
                }
            }
        });


        mSecSpinner = spinners.findViewById(R.id.SecondsPicker);
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
        mSecSpinner.setValue(mViewModel.getSecs());
        mSecSpinner.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                if(oldVal != newVal){
                    mViewModel.setSecs(newVal);
                }
            }
        });


        mSaveButton = view.findViewById(R.id.button_save);
        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateSet();
            }
        });


        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mInstance = null;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        getActivity().setTitle(mParentTitle);
        if(getFragmentManager().getBackStackEntryCount() <= 0){
            // TODO: NOT ONLY MAIN ACT
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
    public static Bundle getBundle(int id, String setName, String setDescrip, int timeInMil, int imageId){
        Bundle bundle = new Bundle();

        int[] time = BaseApp.convertFromMillis(timeInMil);

        bundle.putInt(EXTRA_SET_ID, id);
        bundle.putString(EXTRA_SET_NAME, setName);
        bundle.putString(EXTRA_SET_DESCIP, setDescrip);
        bundle.putInt(EXTRA_SET_MIN, time[0]);
        bundle.putInt(EXTRA_SET_SEC, time[1]);
        bundle.putInt(EXTRA_IMAGE_ID, imageId);

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

        if(TextUtils.isEmpty(mViewModel.getName())){
            mEditName.setError(getString(R.string.name_error));
            OK = false;
        }

        if(mViewModel.getMins() == 0 && mViewModel.getSecs() == 0){
            Toast.makeText(getContext(), R.string.time_error, Toast.LENGTH_SHORT).show();
            OK = false;
        }
        if(OK) {
            Set s = new Set(
                    mViewModel.getName(),
                    mViewModel.getDescrip(),
                    Set.USER_CREATED,
                    BaseApp.convertToMillis(mViewModel.getMins(), mViewModel.getSecs()),
                    mViewModel.getImage());
            mSetViewModel.insert(s);

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
