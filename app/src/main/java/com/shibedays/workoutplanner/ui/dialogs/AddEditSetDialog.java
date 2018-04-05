package com.shibedays.workoutplanner.ui.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.shawnlin.numberpicker.NumberPicker;
import com.shibedays.workoutplanner.R;
import com.shibedays.workoutplanner.ui.MainActivity;
import com.shibedays.workoutplanner.ui.NewWorkoutFragment;
import com.shibedays.workoutplanner.ui.adapters.sectioned.SectionedSetAdapter;

import java.util.Locale;

import javax.crypto.spec.DESedeKeySpec;


public class AddEditSetDialog extends DialogFragment implements NumberPicker.OnValueChangeListener{

    //region CONSTANTS
    // Package and Debug Constants
    private static final String DEBUG_TAG = AddEditSetDialog.class.getSimpleName();
    private static final String PACKAGE = "com.shibedays.workoutplanner.ui.dialogs.AddEditSetDialog.";

    public static final int NEW_SET = 0;
    public static final int EDIT_SET = 1;
    public static final int DISPLAY_SET = 2;
    //endregion

    //region INTENT_KEYS
    public static final String EXTRA_DIALOG_TYPE = PACKAGE + "DIALOG_TYPE";
    public static final String EXTRA_SET_INDEX = PACKAGE + "SET_INDEX";
    public static final String EXTRA_SET_NAME = PACKAGE + "SET_NAME";
    public static final String EXTRA_SET_DESCIP = PACKAGE + "SET_DESCRIP";
    public static final String EXTRA_SET_MIN = PACKAGE + "SET_MIN";
    public static final String EXTRA_SET_SEC = PACKAGE + "SET_SEC";
    public static final String EXTRA_SET_SECTION = PACKAGE + "SET_SECTION";
    //endregion

    //region PRIVATE_VARS
    // UI Comoponents
    private EditText mEditTextName;
    private TextView mTextViewName;
    private EditText mEditTextDescrip;
    private TextView mTextViewDescrip;
    private NumberPicker mMinutePicker;
    private NumberPicker mSecondPicker;
    // Utility
    private MainActivity mParentActivity;
    //endregion

    //region INTERFACES
    public interface AddSetDialogListener {
        void addUserCreatedSet(String name, String descrip, int min, int sec);
        void editUserCreatedSet(int index, String name, String descrip, int min, int sec);

        void editUserSet(int index, String name, String descrip, int min, int sec);
    }
    AddSetDialogListener mListener;
    //endregion

    //region LIFECYCLE

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if(context instanceof AddSetDialogListener) {
            mListener = (AddSetDialogListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement AddSetDialogListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mParentActivity = (MainActivity) getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(mParentActivity);
        LayoutInflater inflater = mParentActivity.getLayoutInflater();

        final View view = inflater.inflate(R.layout.dialog_add_edit_set, null);

        mEditTextName = view.findViewById(R.id.new_set_name);
        mTextViewName = view.findViewById(R.id.display_set_name);
        mEditTextDescrip = view.findViewById(R.id.new_set_descrip);
        mTextViewDescrip = view.findViewById(R.id.display_set_descrip);

        View number_spinners = view.findViewById(R.id.spinners);
        mMinutePicker = number_spinners.findViewById(R.id.MinutePicker);
        mMinutePicker.setMinValue(0);
        mMinutePicker.setMaxValue(30);
        mMinutePicker.setWrapSelectorWheel(true);
        mMinutePicker.setOnValueChangedListener(this);
        mMinutePicker.setFadingEdgeEnabled(true);

        mSecondPicker = number_spinners.findViewById(R.id.SecondsPicker);
        mSecondPicker.setMinValue(0);
        mSecondPicker.setMaxValue(59);

        mSecondPicker.setFormatter(new NumberPicker.Formatter() {
            @Override
            public String format(int value) {
                return String.format(Locale.US, "%02d", value);
            }
        });
        mSecondPicker.setWrapSelectorWheel(true);
        mSecondPicker.setOnValueChangedListener(this);
        mSecondPicker.setFadingEdgeEnabled(true);

        Bundle args = getArguments();
        if(args!= null){
            int type = args.getInt(EXTRA_DIALOG_TYPE);
            if(type == NEW_SET){
                mMinutePicker.setValue(0);
                mSecondPicker.setValue(10);

                builder.setView(view)
                        .setTitle("New Set")
                        .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mListener.addUserCreatedSet(mEditTextName.getText().toString(),
                                                                        mEditTextDescrip.getText().toString(),
                                                                        mMinutePicker.getValue(), mSecondPicker.getValue());
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // NOTHING
                            }
                        });
            } else if(type == EDIT_SET){
                final int index = args.getInt(EXTRA_SET_INDEX);
                mEditTextName.setText(args.getString(EXTRA_SET_NAME));
                mEditTextDescrip.setText(args.getString(EXTRA_SET_DESCIP));
                mMinutePicker.setValue(args.getInt(EXTRA_SET_MIN));
                mSecondPicker.setValue(args.getInt(EXTRA_SET_SEC));
                final int section = args.getInt(EXTRA_SET_SECTION);
                builder.setView(view)
                        .setTitle("Edit Set")
                        .setPositiveButton("Edit", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(section == NewWorkoutFragment.RIGHT_SIDE) {
                                    mListener.editUserSet(index, mEditTextName.getText().toString(),
                                            mEditTextDescrip.getText().toString(),
                                            mMinutePicker.getValue(), mSecondPicker.getValue());
                                } else if (section == NewWorkoutFragment.LEFT_SIDE){
                                    mListener.editUserCreatedSet(index, mEditTextName.getText().toString(),
                                            mEditTextDescrip.getText().toString(),
                                            mMinutePicker.getValue(), mSecondPicker.getValue());
                                } else {
                                    throw new RuntimeException(DEBUG_TAG + " No section value was passed to AddEditSetDialog for " + mEditTextName.getText().toString() );
                                }
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // NOTHING
                            }
                        });
            } else if (type == DISPLAY_SET) {
                mTextViewName.setVisibility(View.VISIBLE);
                mTextViewName.setText(args.getString(EXTRA_SET_NAME));
                mEditTextName.setVisibility(View.INVISIBLE);
                mTextViewDescrip.setVisibility(View.VISIBLE);
                mTextViewDescrip.setText(args.getString(EXTRA_SET_DESCIP));
                mEditTextDescrip.setVisibility(View.INVISIBLE);

                mMinutePicker.setValue(args.getInt(EXTRA_SET_MIN));
                mMinutePicker.setScrollerEnabled(false);
                mSecondPicker.setValue(args.getInt(EXTRA_SET_SEC));
                mSecondPicker.setScrollerEnabled(false);

                builder.setView(view)
                        .setTitle("Set Info")
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Nothing
                            }
                        });
            }
            else {
                throw new RuntimeException(AddEditWorkoutDialog.class.getSimpleName() + " Set Bottom Dialog Type was never set");
            }
        }


        return builder.create();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //mEditTextName.requestFocus();
        //getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        getDialog().setCanceledOnTouchOutside(true);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    //endregion

    //region OVERRIDE_IMPLEMENTATIONS
    @Override
    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {

    }
    //endregion
}
