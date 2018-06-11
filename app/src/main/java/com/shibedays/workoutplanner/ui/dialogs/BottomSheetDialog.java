package com.shibedays.workoutplanner.ui.dialogs;

import android.arch.lifecycle.ViewModelProviders;
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
import com.shibedays.workoutplanner.viewmodel.dialogs.BottomSheetViewModel;

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
    private BottomSheetViewModel mViewModel;
    // UI Components
    private TextView mTitleTextView;


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

        mViewModel = ViewModelProviders.of(this).get(BottomSheetViewModel.class);

        Bundle args = getArguments();
        if(args != null){
            mViewModel.setItemName(args.getString(EXTRA_ITEM_NAME));
            mViewModel.setNumRows(args.getInt(EXTRA_NUM_ROWS));
            mViewModel.setRowTitles(args.getStringArrayList(EXTRA_ROW_TITLES));
            mViewModel.setRowICs(args.getIntArray(EXTRA_ROW_IC_IDS));
            mViewModel.setResultTypes(args.getIntArray(EXTRA_RESULT_TYPE));
        } else {
            throw new RuntimeException(DEBUG_TAG + " no bundle was passed.");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet, container, false);

        mTitleTextView = view.findViewById(R.id.bottom_sheet_title);
        mTitleTextView.setText(mViewModel.getItemName());

        LinearLayout rowContainer = view.findViewById(R.id.bottom_sheet_row_container);

        for(int i = 0; i < mViewModel.getNumRows(); i++){
            View row = inflater.inflate(R.layout.bottom_sheet_item, null);
            ((ImageView)row.findViewById(R.id.bottom_sheet_row_ic)).setImageResource(mViewModel.getRowIC(i));
            ((TextView)row.findViewById(R.id.bottom_sheet_row_title)).setText(mViewModel.getRowTitle(i));
            final int resultType = mViewModel.getResultType(i);
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
