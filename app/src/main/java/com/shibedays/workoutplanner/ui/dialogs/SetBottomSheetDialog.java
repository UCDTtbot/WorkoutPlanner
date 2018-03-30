package com.shibedays.workoutplanner.ui.dialogs;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.shibedays.workoutplanner.R;

/**
 * Created by ttbot on 3/26/2018.
 */

public class SetBottomSheetDialog extends BottomSheetDialogFragment {


    //region CONSTANTS
    // Package and Debug Constants
    private static final String DEBUG_TAG = SetBottomSheetDialog.class.getSimpleName();
    private static final String PACKAGE = "com.shibedays.workoutplanner.ui.dialogs.SetBottomSheetDialog.";
    //endregion

    //region INTENT_KEYS
    public static final String EXTRA_SET_NAME = PACKAGE + "SET_NAME";
    public static final String EXTRA_SET_INDEX = PACKAGE + "SET_INDEX";
    //endregion

    //region PRIVATE_KEYS
    // Data
    private int mSetIndex;
    private String mSetName;
    // UI Components
    private TextView mTitleTextView;
    private LinearLayout mEdit;
    private LinearLayout mDelete;
    //endregion

    //region INTERFACES
    public interface SetBottomSheetDialogListener {
        void editItem(int index);
        void deleteItem(int index);
    }
    SetBottomSheetDialogListener mListener;
    //endregion

    //region LIFECYCLE
    public SetBottomSheetDialog() {

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if(context instanceof SetBottomSheetDialogListener) {
            mListener = (SetBottomSheetDialogListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement SetBottomSheetDialogListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if(args != null){
            mSetName = args.getString(EXTRA_SET_NAME);
            mSetIndex = args.getInt(EXTRA_SET_INDEX);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.edit_delete_bottom_sheet, container, false);

        mTitleTextView = view.findViewById(R.id.bottom_sheet_title);
        mTitleTextView.setText(mSetName);

        mEdit = view.findViewById(R.id.bottom_sheet_edit);
        mEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.editItem(mSetIndex);
                dismiss();
            }
        });

        mDelete = view.findViewById(R.id.bottom_sheet_delete);
        mDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.deleteItem(mSetIndex);
                dismiss();
            }
        });

        return view;
    }
    //endregion

    //region UTILITY
    public void setName(String name){
        mTitleTextView.setText(name);
    }
    //endregion
}
