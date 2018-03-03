package com.shibedays.workoutplanner.ui;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.widget.EditText;
import android.widget.NumberPicker;

import com.shibedays.workoutplanner.db.entities.Set;

/**
 * Created by ttbot on 3/3/2018.
 */

public class AddSetDialog extends DialogFragment {

    private static String DEBUG_TAG = AddSetDialog.class.getSimpleName();

    private EditText mEditText;
    private NumberPicker mMinutePicker;
    private NumberPicker mSecondPicker;

    public interface AddSetDialogListener {
        void onAddSetDialogPositiveClick(Set set);
        void onAddSetDialogNegativeClick();
    }
    AddSetDialogListener mListener;

    private MyWorkoutActivity mParentActivity;
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return super.onCreateDialog(savedInstanceState);
    }
}
