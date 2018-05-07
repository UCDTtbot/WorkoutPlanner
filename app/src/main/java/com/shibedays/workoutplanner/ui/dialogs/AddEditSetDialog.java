package com.shibedays.workoutplanner.ui.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.shawnlin.numberpicker.NumberPicker;
import com.shibedays.workoutplanner.BaseApp;
import com.shibedays.workoutplanner.R;
import com.shibedays.workoutplanner.ui.MainActivity;

import java.util.Locale;


public class AddEditSetDialog extends DialogFragment {

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
    public static final String EXTRA_SET_NAME = PACKAGE + "SET_NAME";
    public static final String EXTRA_SET_DESCIP = PACKAGE + "SET_DESCRIP";
    public static final String EXTRA_SET_MIN = PACKAGE + "SET_MIN";
    public static final String EXTRA_SET_SEC = PACKAGE + "SET_SEC";
    public static final String EXTRA_SET_INDEX = PACKAGE + "SET_INDEX";
    public static final String EXTRA_SET_SECTION = PACKAGE + "SET_SECTION";
    //endregion

    //region PRIVATE_VARS
    // UI
    private EditText mEditTextName;
    //endregion

    //region INTERFACES
    public interface AddEditSetDialogListener {
        void dialogResult(int dialogType, String name, String descrip, int min, int sec, int section, int index);
    }
    AddEditSetDialogListener mListener;
    //endregion

    //region LIFECYCLE

    public static AddEditSetDialog newInstance(Bundle args, AddEditSetDialogListener listener){
        AddEditSetDialog dialog = new AddEditSetDialog();
        dialog.setListener(listener);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Activity mParentActivity = getActivity();
        final AlertDialog.Builder builder = new AlertDialog.Builder(mParentActivity);
        LayoutInflater inflater = null;
        if(mParentActivity != null) {
            inflater = mParentActivity.getLayoutInflater();
        } else {
            throw new RuntimeException(DEBUG_TAG + " Parent Activity doesn't exist");
        }
        final View view = inflater.inflate(R.layout.dialog_edit_set, null);

        // UI Comoponents
        mEditTextName = view.findViewById(R.id.new_set_name);
        final TextView mTextViewName = view.findViewById(R.id.display_set_name);
        final EditText mEditTextDescrip = view.findViewById(R.id.new_set_descrip);
        final TextView mTextViewDescrip = view.findViewById(R.id.display_set_descrip);
        final View number_spinners = view.findViewById(R.id.spinners);
        final NumberPicker mMinutePicker = number_spinners.findViewById(R.id.MinutePicker);
        final NumberPicker mSecondPicker = number_spinners.findViewById(R.id.SecondsPicker);

        mMinutePicker.setMinValue(0);
        mMinutePicker.setMaxValue(30);
        mMinutePicker.setWrapSelectorWheel(true);
        mMinutePicker.setFadingEdgeEnabled(true);

        mSecondPicker.setMinValue(0);
        mSecondPicker.setMaxValue(59);
        mSecondPicker.setFormatter(new NumberPicker.Formatter() {
            @Override
            public String format(int value) {
                return String.format(Locale.US, "%02d", value);
            }
        });
        mSecondPicker.setWrapSelectorWheel(true);
        mSecondPicker.setFadingEdgeEnabled(true);


        Bundle args = getArguments();
        AlertDialog dialog = null;

        if(args!= null){
            final int type = args.getInt(EXTRA_DIALOG_TYPE);
            final int section = args.getInt(EXTRA_SET_SECTION);
            final int index = args.getInt(EXTRA_SET_INDEX);
            if(type == NEW_SET){
                mMinutePicker.setValue(0);
                mSecondPicker.setValue(10);

                builder.setView(view)
                        .setTitle("New Set")
                        .setPositiveButton("Add", null)
                        .setNegativeButton("Cancel", null);
                dialog = builder.create();
                dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(final DialogInterface dialog) {
                        Button pos = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                        pos.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // TODO: Input validation
                                Boolean OK = true;

                                String name = mEditTextName.getText().toString();
                                if(TextUtils.isEmpty(name)){
                                    mEditTextName.setError(getString(R.string.name_error));
                                    OK = false;
                                }
                                String descrip = mEditTextDescrip.getText().toString();
                                int min = mMinutePicker.getValue();
                                int sec = mSecondPicker.getValue();
                                if(min == 0 && sec == 0){
                                    Toast.makeText(getContext(), R.string.time_error, Toast.LENGTH_SHORT).show();
                                    OK = false;
                                }
                                if(OK) {
                                    mListener.dialogResult(type, name, descrip, min, sec, section, index);
                                    dialog.dismiss();
                                } else {
                                    Log.d(DEBUG_TAG, "Input Validation Error");
                                }
                            }
                        });
                        Button neg = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEGATIVE);
                        neg.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        });

                    }
                });

            } else if(type == EDIT_SET){
                mEditTextName.setText(args.getString(EXTRA_SET_NAME));
                mEditTextName.setSelection(mEditTextName.getText().length());
                mEditTextDescrip.setText(args.getString(EXTRA_SET_DESCIP));
                mEditTextDescrip.setSelection(mEditTextDescrip.getText().length());
                mMinutePicker.setValue(args.getInt(EXTRA_SET_MIN));
                mSecondPicker.setValue(args.getInt(EXTRA_SET_SEC));
                builder.setView(view)
                        .setTitle("Edit Set")
                        .setPositiveButton("Edit", null)
                        .setNegativeButton("Cancel", null);
                dialog = builder.create();
                dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(final DialogInterface dialog) {
                        Button pos = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                        pos.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                boolean OK = true;

                                String name = mEditTextName.getText().toString();
                                if(TextUtils.isEmpty(name)){
                                    mEditTextName.setError(getString(R.string.name_error));
                                    OK = false;
                                }
                                String descrip = mEditTextDescrip.getText().toString();
                                int min = mMinutePicker.getValue();
                                int sec = mSecondPicker.getValue();
                                if(min == 0 && sec == 0){
                                    Toast.makeText(getContext(), R.string.time_error, Toast.LENGTH_SHORT).show();
                                    OK = false;
                                }
                                if(OK){
                                    mListener.dialogResult(type, name, descrip, min, sec, section, index);
                                    dialog.dismiss();
                                } else {
                                    Log.d(DEBUG_TAG, "Input Validation Error");
                                }
                            }
                        });
                        Button neg = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEGATIVE);
                        neg.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        });

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
                        .setPositiveButton("Ok", null);
            }
            else {
                throw new RuntimeException(AddEditSetDialog.class.getSimpleName() + " Set Bottom Dialog Type was never set");
            }
        } else {
            throw new RuntimeException(AddEditSetDialog.class.getSimpleName() + " Args never set");
        }

        if(dialog != null)
            return dialog;
        else
            return builder.create();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mEditTextName.requestFocus();
        if(getDialog().getWindow() != null)
            getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        getDialog().setCanceledOnTouchOutside(true);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    //endregion

    //region UTILITY
    public static Bundle getDialogBundle(int dialogType, String setName, String setDescrip, int timeInMil, int setIndex, int setSection){
        Bundle bundle = new Bundle();

        int[] time = BaseApp.convertFromMillis(timeInMil);

        bundle.putInt(EXTRA_DIALOG_TYPE, dialogType);
        bundle.putString(EXTRA_SET_NAME, setName);
        bundle.putString(EXTRA_SET_DESCIP, setDescrip);
        bundle.putInt(EXTRA_SET_MIN, time[0]);
        bundle.putInt(EXTRA_SET_SEC, time[1]);
        bundle.putInt(EXTRA_SET_INDEX, setIndex);
        bundle.putInt(EXTRA_SET_SECTION, setSection);

        return bundle;
    }

    private void setListener(AddEditSetDialogListener listener){
        mListener = listener;
    }
    //endregion
}
