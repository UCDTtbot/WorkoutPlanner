package com.shibedays.workoutplanner.ui.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.shibedays.workoutplanner.BaseApp;
import com.shibedays.workoutplanner.R;
import com.shibedays.workoutplanner.viewmodel.dialogs.DisplaySetViewModel;

import java.util.Locale;


public class DisplaySetDialog extends DialogFragment {

    //region CONSTANTS
    // Package and Debug Constants
    private static final String DEBUG_TAG = DisplaySetDialog.class.getSimpleName();
    private static final String PACKAGE = "com.shibedays.workoutplanner.ui.dialogs.DisplaySetDialog.";
    //endregion

    //region INTENT_KEYS
    public static final String EXTRA_SET_NAME = PACKAGE + "SET_NAME";
    public static final String EXTRA_SET_DESCIP = PACKAGE + "SET_DESCRIP";
    public static final String EXTRA_SET_MIN = PACKAGE + "SET_MIN";
    public static final String EXTRA_SET_SEC = PACKAGE + "SET_SEC";
    public static final String EXTRA_SET_IMAGE = PACKAGE + "SET_IMAGE";
    //endregion

    //region PRIVATE_VARS
    // Data
    private DisplaySetViewModel mViewModel;
    //endregion

    //region LIFECYCLE

    public static DisplaySetDialog newInstance(Bundle args){
        DisplaySetDialog dialog = new DisplaySetDialog();
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
        final View view = inflater.inflate(R.layout.dialog_set_info, null);

        // UI Comoponents
        final TextView textViewName = view.findViewById(R.id.display_set_name);
        final TextView textViewDescrip = view.findViewById(R.id.display_set_descrip);
        final ImageView imageView = view.findViewById(R.id.display_image);
        final TextView timeDisplay = view.findViewById(R.id.display_set_time);

        Bundle args = getArguments();
        mViewModel = ViewModelProviders.of(this).get(DisplaySetViewModel.class);

        if(args!= null){
            mViewModel.setSetName(args.getString(EXTRA_SET_NAME));
            mViewModel.setSetDescrip(args.getString(EXTRA_SET_DESCIP));
            mViewModel.setSetMin(args.getInt(EXTRA_SET_MIN));
            mViewModel.setSetSec(args.getInt(EXTRA_SET_SEC));
            mViewModel.setSetImageId(args.getInt(EXTRA_SET_IMAGE));
        } else {
            throw new RuntimeException(DisplaySetDialog.class.getSimpleName() + " Args never set");
        }

        textViewName.setText(mViewModel.getSetName());
        textViewDescrip.setText(mViewModel.getSetDescrip());
        textViewDescrip.setMovementMethod(new ScrollingMovementMethod());
        imageView.setImageResource(mViewModel.getSetImageId());
        timeDisplay.setText(BaseApp.formatTime(mViewModel.getSetMin(), mViewModel.getSetSec()));

        builder.setView(view)
                .setTitle("Set Info")
                .setPositiveButton("Ok", null);
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
    public static Bundle getDialogBundle(int id, String setName, String setDescrip, int timeInMil, int imageResource){
        Bundle bundle = new Bundle();

        int[] time = BaseApp.convertFromMillis(timeInMil);

        bundle.putString(EXTRA_SET_NAME, setName);
        bundle.putString(EXTRA_SET_DESCIP, setDescrip);
        bundle.putInt(EXTRA_SET_MIN, time[0]);
        bundle.putInt(EXTRA_SET_SEC, time[1]);
        bundle.putInt(EXTRA_SET_IMAGE, imageResource);

        return bundle;
    }
    //endregion
}
