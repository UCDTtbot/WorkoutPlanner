package com.shibedays.workoutplanner.ui.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.INotificationSideChannel;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.shibedays.workoutplanner.R;
import com.shibedays.workoutplanner.ui.adapters.ImageAdapter;

import java.util.List;

public class ChooseImageDialog extends DialogFragment {

    //region CONSTANTS
    // Package and Debug Constants
    private static final String DEBUG_TAG = ChooseImageDialog.class.getSimpleName();
    private static final String PACKAGE = "com.shibedays.workoutplanner.ui.dialogs.ChooseImageDialog.";
    //endregion

    //region INTENT_KEYS
    public static final String EXTRA_SET_NAME = PACKAGE + "SET_NAME";
    public static final String EXTRA_SET_DESCIP = PACKAGE + "SET_DESCRIP";
    public static final String EXTRA_SET_MIN = PACKAGE + "SET_MIN";
    public static final String EXTRA_SET_ID = PACKAGE + "SET_ID";
    public static final String EXTRA_SET_SEC = PACKAGE + "SET_SEC";
    //endregion

    //region PRIVATE_VARS
    // DATA
    private List<Integer> mImageIds;
    // UI
    private RecyclerView mImageRecyclerView;

    private ImageAdapter mAdapter;
    //endregion

    //region LIFECYCLE

    public static ChooseImageDialog newInstance(Bundle args, List<Integer> imageIds){
        ChooseImageDialog dialog = new ChooseImageDialog();
        dialog.setArguments(args);
        dialog.setData(imageIds);
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
        final View view = inflater.inflate(R.layout.dialog_choose_image, null);



        mAdapter = new ImageAdapter(getContext(), new ImageAdapter.ImageListener() {

        });

        mImageRecyclerView = view.findViewById(R.id.image_recycler);
        RecyclerView.LayoutManager manager = new GridLayoutManager(mParentActivity, 2);
        mImageRecyclerView.setLayoutManager(manager);
        mImageRecyclerView.setAdapter(mAdapter);

        mAdapter.setImageList(mImageIds);
        mAdapter.notifyDataSetChanged();
        //TODO: Setup recycler view

        Bundle args = getArguments();

        if(args!= null){
            builder.setView(view)
                    .setTitle("")
                    .setPositiveButton("Ok", null);
        } else {
            throw new RuntimeException(DisplaySetDialog.class.getSimpleName() + " Args never set");
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
    public static Bundle getDialogBundle(){
        Bundle args = new Bundle();

        return args;
    }

    public void setData(List<Integer> ids){
        mImageIds = ids;
    }
    //endregion

}
