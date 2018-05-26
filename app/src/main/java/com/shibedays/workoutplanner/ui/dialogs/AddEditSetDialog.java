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
import android.support.v7.widget.LinearLayoutCompat;
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

import java.util.Locale;


public class AddEditSetDialog extends DialogFragment {

    //region CONSTANTS
    // Package and Debug Constants
    private static final String DEBUG_TAG = AddEditSetDialog.class.getSimpleName();
    private static final String PACKAGE = "com.shibedays.workoutplanner.ui.dialogs.AddEditSetDialog.";
    //endregion

    //region INTENT_KEYS
    public static final String EXTRA_SET_NAME = PACKAGE + "SET_NAME";
    public static final String EXTRA_SET_DESCIP = PACKAGE + "SET_DESCRIP";
    public static final String EXTRA_SET_MIN = PACKAGE + "SET_MIN";
    public static final String EXTRA_SET_ID = PACKAGE + "SET_ID";
    public static final String EXTRA_SET_SEC = PACKAGE + "SET_SEC";
    //endregion

    //region PRIVATE_VARS
    // UI
    private EditText mEditTextName;

    private int mType;
    //endregion

    //region LIFECYCLE

    public static AddEditSetDialog newInstance(Bundle args){
        AddEditSetDialog dialog = new AddEditSetDialog();
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
        final TextView textViewName = view.findViewById(R.id.display_set_name);
        final EditText editTextDescrip = view.findViewById(R.id.new_set_descrip);
        final TextView textViewDescrip = view.findViewById(R.id.display_set_descrip);
        final View numSpinners = view.findViewById(R.id.spinners);
        final NumberPicker minPicker = numSpinners.findViewById(R.id.MinutePicker);
        final NumberPicker secPicker = numSpinners.findViewById(R.id.SecondsPicker);
        final LinearLayoutCompat timeDisplay = view.findViewById(R.id.new_set_time_display);

        minPicker.setMinValue(0);
        minPicker.setMaxValue(30);
        minPicker.setWrapSelectorWheel(true);
        minPicker.setFadingEdgeEnabled(true);

        secPicker.setMinValue(0);
        secPicker.setMaxValue(59);
        secPicker.setFormatter(new NumberPicker.Formatter() {
            @Override
            public String format(int value) {
                return String.format(Locale.US, "%02d", value);
            }
        });
        secPicker.setWrapSelectorWheel(true);
        secPicker.setFadingEdgeEnabled(true);


        Bundle args = getArguments();

        if(args!= null){
            final int setId = args.getInt(EXTRA_SET_ID);

            numSpinners.setVisibility(View.INVISIBLE);
            timeDisplay.setVisibility(View.VISIBLE);

            textViewName.setVisibility(View.VISIBLE);
            textViewName.setText(args.getString(EXTRA_SET_NAME));
            mEditTextName.setVisibility(View.INVISIBLE);
            textViewDescrip.setVisibility(View.VISIBLE);
            textViewDescrip.setText(args.getString(EXTRA_SET_DESCIP));
            editTextDescrip.setVisibility(View.INVISIBLE);

            minPicker.setValue(args.getInt(EXTRA_SET_MIN));
            minPicker.setScrollerEnabled(false);
            secPicker.setValue(args.getInt(EXTRA_SET_SEC));
            secPicker.setScrollerEnabled(false);

            builder.setView(view)
                    .setTitle("Set Info")
                    .setPositiveButton("Ok", null);

        } else {
            throw new RuntimeException(AddEditSetDialog.class.getSimpleName() + " Args never set");
        }

        return builder.create();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().setCanceledOnTouchOutside(true);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    //endregion

    //region UTILITY
    public static Bundle getDialogBundle(int id, String setName, String setDescrip, int timeInMil){
        Bundle bundle = new Bundle();

        int[] time = BaseApp.convertFromMillis(timeInMil);

        bundle.putInt(EXTRA_SET_ID, id);
        bundle.putString(EXTRA_SET_NAME, setName);
        bundle.putString(EXTRA_SET_DESCIP, setDescrip);
        bundle.putInt(EXTRA_SET_MIN, time[0]);
        bundle.putInt(EXTRA_SET_SEC, time[1]);

        return bundle;
    }
    //endregion
}
