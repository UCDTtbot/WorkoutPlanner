package com.shibedays.workoutplanner.ui.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.shawnlin.numberpicker.NumberPicker;
import com.shibedays.workoutplanner.BaseApp;
import com.shibedays.workoutplanner.R;
import com.shibedays.workoutplanner.db.entities.Set;
import com.shibedays.workoutplanner.ui.dialogs.NumberPickerDialog;

import java.util.Locale;

public class SetInfoFragment extends Fragment {

    //region CONSTANTS
    private static final String PACKAGE = "com.shibedays.workoutplanner.ui.fragments.SetInfoFragment.";
    private static final String DEBUG_TAG = SetInfoFragment.class.getSimpleName();
    //endregion

    //region PRIVATE_VARS
    // Data
    private Set mSetData;
    // UI
    private TextView mSetNameView;
    private ImageView mSetImageView;
    private TextView mSetDescrip;
    private TextView mSetTime;
    // FLAGS

    private SetInfoFragment mThis;
    //endregion



    public interface SetInfoListener {
        void updateSet(String setName, String descrip, int time, int imageId);
    }
    private SetInfoListener mListener;

    //region FACTORY CONSTRUCTORS
    public SetInfoFragment() {
        // Required empty public constructor
    }

    public static SetInfoFragment newInstance(Set setData, SetInfoListener listener) {
        SetInfoFragment fragment = new SetInfoFragment();
        fragment.setData(setData);
        fragment.setListener(listener);
        return fragment;
    }
    //endregion

    //region LIFECYCLE
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mThis = this;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_set_info, container, false);

        mSetNameView = view.findViewById(R.id.set_name);
        mSetImageView = view.findViewById(R.id.set_image);
        mSetDescrip = view.findViewById(R.id.set_descrip);
        mSetTime = view.findViewById(R.id.set_time);

        if(mSetData != null) {
            mSetNameView.setText(mSetData.getName());
            mSetImageView.setImageResource(mSetData.getSetImageId());
            mSetDescrip.setText(mSetData.getDescrip());
            mSetDescrip.setMovementMethod(new ScrollingMovementMethod());

            int[] time = BaseApp.convertFromMillis(mSetData.getTime());
            final int min = time[0], sec = time[1];
            String text = BaseApp.formatTime(min, sec);
            mSetTime.setText(text);
        }


        mSetImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSetImageView.setVisibility(View.GONE);
                mSetDescrip.setVisibility(View.GONE);
            }
        });
        return view;
    }



    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    //endregion

    public static Bundle getBundle(){
        Bundle args = new Bundle();

        return args;
    }

    //region UTILITY
    private void setListener(SetInfoListener listener){
        mListener = listener;
    }

    public void setData(Set s){
        mSetData = s;
    }

    public void updateSet(Set set){
        mSetData = set;
    }

    public void updateTime(int min, int sec){
        mSetData.setTime(BaseApp.convertToMillis(min, sec));
        mSetTime.setText(BaseApp.formatTime(min, sec));
    }

    //endregion
}

/*
{
                @Override
                public void onClick(View v) {
                    LayoutInflater inflater = LayoutInflater.from(getContext());
                    final View dialogView = inflater.inflate(R.layout.dialog_number_picker, null);

                    dialogView.findViewById(R.id.check_layout).setVisibility(View.INVISIBLE);
                    View number_spinners = dialogView.findViewById(R.id.spinners);
                    final NumberPicker min_picker = number_spinners.findViewById(R.id.MinutePicker);
                    final NumberPicker sec_picker = number_spinners.findViewById(R.id.SecondsPicker);

                    min_picker.setMinValue(0);
                    min_picker.setMaxValue(30);
                    min_picker.setValue(min);
                    min_picker.setWrapSelectorWheel(true);

                    sec_picker.setMinValue(0);
                    sec_picker.setMaxValue(59);
                    sec_picker.setValue(sec);
                    sec_picker.setFormatter(new NumberPicker.Formatter() {
                        @Override
                        public String format(int value) {
                            return String.format(Locale.US, "%02d", value);
                        }
                    });
                    sec_picker.setWrapSelectorWheel(true);

                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setView(dialogView);
                    builder.setTitle("Change Rep Time");
                    builder.setPositiveButton("Set", null);
                    builder.setNegativeButton("Cancel", null);

                    final AlertDialog dialog = builder.create();
                    dialog.show();
                    Button button = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            int min = min_picker.getValue();
                            int sec = sec_picker.getValue();
                            if(min == 0 && sec == 0){
                                Toast.makeText(getContext(), "Time cannot be 0:00", Toast.LENGTH_SHORT).show();
                            } else {
                                updateTime(min, sec);
                                dialog.dismiss();
                            }
                        }
                    });
                }
            });
 */
