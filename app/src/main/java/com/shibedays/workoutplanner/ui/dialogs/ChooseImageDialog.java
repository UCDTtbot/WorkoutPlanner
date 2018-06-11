package com.shibedays.workoutplanner.ui.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.shibedays.workoutplanner.R;
import com.shibedays.workoutplanner.ui.adapters.ImageAdapter;
import com.shibedays.workoutplanner.viewmodel.dialogs.ChooseImageViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ChooseImageDialog extends DialogFragment {

    //region CONSTANTS
    // Package and Debug Constants
    private static final String DEBUG_TAG = ChooseImageDialog.class.getSimpleName();
    private static final String PACKAGE = "com.shibedays.workoutplanner.ui.dialogs.ChooseImageDialog.";
    //endregion

    //region INTENT_KEYS
    public static final String EXTRA_SELECTED_IMAGE = PACKAGE + "SET_NAME";
    public static final String EXTRA_IMAGE_LIST = PACKAGE + "SET_DESCRIP";
    //endregion

    //region PRIVATE_VARS
    // DATA
    private ChooseImageViewModel mViewModel;

    // UI
    private RecyclerView mImageRecyclerView;

    private ImageAdapter mAdapter;
    //endregion

    public interface ChooseImageListener {
        void dialogResult(int image_id);
    }
    ChooseImageListener mListener;

    //region LIFECYCLE
    public static ChooseImageDialog newInstance(Bundle args, ChooseImageListener listener){
        ChooseImageDialog dialog = new ChooseImageDialog();
        dialog.setArguments(args);
        dialog.setListener(listener);
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

        mViewModel = ViewModelProviders.of(this).get(ChooseImageViewModel.class);
        Bundle args = getArguments();
        if(args != null){
            mViewModel.setImageIds(args.getIntegerArrayList(EXTRA_IMAGE_LIST));
            mViewModel.setSelected(args.getInt(EXTRA_SELECTED_IMAGE));
            mViewModel.setupMap();
        }

        LayoutInflater inflater = null;
        if(mParentActivity != null) {
            inflater = mParentActivity.getLayoutInflater();
        } else {
            throw new RuntimeException(DEBUG_TAG + " Parent Activity doesn't exist");
        }
        final View view = inflater.inflate(R.layout.dialog_choose_image, null);



        mAdapter = new ImageAdapter(getContext(), new ImageAdapter.ImageListener() {
            @Override
            public void changeChecked(int id) {
                mViewModel.putMapping(mViewModel.getSelected(), false);
                mViewModel.putMapping(id, true);
                mViewModel.setSelected(id);
            }
        });

        mImageRecyclerView = view.findViewById(R.id.image_recycler);
        RecyclerView.LayoutManager manager = new GridLayoutManager(mParentActivity, 2);
        mImageRecyclerView.setLayoutManager(manager);
        mImageRecyclerView.setAdapter(mAdapter);

        mAdapter.setImageList(mViewModel.getImageIds(),
                mViewModel.getMappedImageIds(),
                mViewModel.getSelected());
        mAdapter.notifyDataSetChanged();
        //TODO: Setup recycler view

        if(args!= null){
            builder.setView(view)
                    .setTitle("")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            mListener.dialogResult(mViewModel.getSelected());
                        }
                    });
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
    public static Bundle getDialogBundle(ArrayList<Integer> imageIds, int selectedImage){
        Bundle args = new Bundle();

        args.putIntegerArrayList(EXTRA_IMAGE_LIST, imageIds);
        args.putInt(EXTRA_SELECTED_IMAGE, selectedImage);

        return args;
    }

    public void setListener(ChooseImageListener listener){
        mListener = listener;
    }
    //endregion

}
