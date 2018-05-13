package com.shibedays.workoutplanner.ui.dialogs;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.shibedays.workoutplanner.R;

import java.util.ArrayList;

public class BottomSheetDialog extends BottomSheetDialogFragment {
    //region CONSTANTS
    // Package and Debug Constants
    private static final String DEBUG_TAG = BottomSheetDialog.class.getSimpleName();
    private static final String PACKAGE = "com.shibedays.workoutplanner.ui.dialogs.BottomSheetDialog.";

    //endregion

    //region INTENT_KEYS
    public static final String EXTRA_ITEM_NAME = PACKAGE + "ITEM_NAME";
    public static final String EXTRA_NUM_ROWS = PACKAGE + "NUM_ROWS";
    public static final String EXTRA_ROW_TITLES = PACKAGE + "ROW_TITLES";
    public static final String EXTRA_ROW_IC_IDS = PACKAGE + "ROW_IC_IDS";
    public static final String EXTRA_RESULT_TYPE = PACKAGE + "RESULT_TYPE";
    //endregion


    //region PRIVATE_VARS
    // Data
    private String mItemName;
    // UI Components
    private int mNumRows;
    private int mSetID;
    private TextView mTitleTextView;
    private ArrayList<String> mRowTitles;
    private int[] mRowICs;
    private int[] mResultTypes;

    //endregion

    //region INTERFACES
    public interface BottomSheetDialogListener{
        void bottomSheetResult(int resultCode);
    }
    private BottomSheetDialogListener mListener;
    //endregion

    //region LIFECYCLE

    public static BottomSheetDialog newInstance(Bundle args, BottomSheetDialogListener listener){
        BottomSheetDialog dialog = new BottomSheetDialog();
        dialog.setListener(listener);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if(args != null){
            mItemName = args.getString(EXTRA_ITEM_NAME);
            mNumRows = args.getInt(EXTRA_NUM_ROWS);
            mRowTitles = args.getStringArrayList(EXTRA_ROW_TITLES);
            mRowICs = args.getIntArray(EXTRA_ROW_IC_IDS);
            mResultTypes = args.getIntArray(EXTRA_RESULT_TYPE);
        } else {
            throw new RuntimeException(DEBUG_TAG + " no bundle was passed.");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet, container, false);

        mTitleTextView = view.findViewById(R.id.bottom_sheet_title);
        mTitleTextView.setText(mItemName);

        LinearLayout rowContainer = view.findViewById(R.id.bottom_sheet_row_container);

        for(int i = 0; i < mNumRows; i++){
            View row = inflater.inflate(R.layout.bottom_sheet_item, null);
            ((ImageView)row.findViewById(R.id.bottom_sheet_row_ic)).setImageResource(mRowICs[i]);
            ((TextView)row.findViewById(R.id.bottom_sheet_row_title)).setText(mRowTitles.get(i));
            final int resultType = mResultTypes[i];
            row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.bottomSheetResult(resultType);
                    dismiss();
                }
            });
            rowContainer.addView(row);
        }

        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
    //endregion

    //region UTILITY
    public static Bundle getBottomSheetBundle(String itemName, int rows, ArrayList<String> rowTitles, int[] ics, int[] resultTypes){
        Bundle bundle = new Bundle();

        bundle.putString(EXTRA_ITEM_NAME, itemName);
        bundle.putInt(EXTRA_NUM_ROWS, rows);
        bundle.putStringArrayList(EXTRA_ROW_TITLES, rowTitles);
        bundle.putIntArray(EXTRA_ROW_IC_IDS, ics);
        bundle.putIntArray(EXTRA_RESULT_TYPE, resultTypes);

        return bundle;
    }

    private void setListener(BottomSheetDialogListener listener){
        mListener = listener;
    }
    //endregion
}
