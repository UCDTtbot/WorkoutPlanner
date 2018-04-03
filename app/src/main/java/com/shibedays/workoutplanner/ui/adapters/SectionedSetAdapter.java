package com.shibedays.workoutplanner.ui.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.sectionedrecyclerview.SectionedRecyclerViewAdapter;
import com.afollestad.sectionedrecyclerview.SectionedViewHolder;
import com.shibedays.workoutplanner.R;
import com.shibedays.workoutplanner.db.entities.Set;
import com.shibedays.workoutplanner.ui.MainActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SectionedSetAdapter extends SectionedRecyclerViewAdapter<SectionedSetAdapter.SectionedSetViewHolder> {

    //region CONSTANTS
    private static final String DEBUG_TAG = SectionedSetAdapter.class.getSimpleName();
    private static final String PACKAGE = "com.shibedays.workoutplanner.ui.adapters.SectionedSetAdapter.";

    // Sections
    private static final int DEFAULT_SECTION = 0;
    private static final int USER_MADE_SECTION = 1;
    private static final int USER_SETS_SECTION = 2;
    // Recycler View Constant
    private static final int LEFT_VIEW = 0;
    private static final int RIGHT_VIEW = 1;
    //endregion


    //region VIEW_HOLDER
    public class SectionedSetViewHolder extends SectionedViewHolder implements View.OnClickListener, View.OnLongClickListener {

        // Data
        private Set curSet;
        // List Item
        private TextView setNameTextView;
        private TextView timeTextView;
        // Header
        private ImageView caret;
        private TextView title;
        // Is Binding
        private boolean onBind;
        // Adapter
        private SectionedSetAdapter adapter;

        public SectionedSetViewHolder(View itemView, SectionedSetAdapter adapter) {
            super(itemView);
            // Header Item Views
            title = itemView.findViewById(R.id.header_title);
            caret = itemView.findViewById(R.id.caret);
            // List Item Views
            setNameTextView = itemView.findViewById(R.id.set_name);
            timeTextView = itemView.findViewById(R.id.set_time);

            this.adapter = adapter;

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        void bindToHeader(int section, boolean expanded){
            if(section == DEFAULT_SECTION){
                title.setText(R.string.header_title_default);
                caret.setImageResource(expanded ? R.drawable.ic_down_arrow_black_24dp : R.id.right_arrow);
            } else if (section == USER_MADE_SECTION){
                title.setText(R.string.header_title_user_created);
                caret.setImageResource(expanded ? R.drawable.ic_down_arrow_black_24dp : R.id.right_arrow);
            } else if (section == USER_SETS_SECTION){
                title.setText(R.string.header_title_my_sets);
            } else {
                title.setText("");
            }
        }

        void bindToItem(final Set curSet){
            onBind = true;
            //Populate data when they bind the workouts to the view holder
            this.curSet = curSet;

            setNameTextView.setText(curSet.getName());
            int[] time = MainActivity.convertFromMillis(curSet.getTime());
            int minutes = time[0], seconds = time[1];

            if(seconds == 0){
                timeTextView.setText(String.format(Locale.US, "%d:%d%d", minutes, seconds, 0));
            } else if( seconds < 10){
                timeTextView.setText(String.format(Locale.US, "%d:%d%d", minutes, 0, seconds));
            }
            else {
                timeTextView.setText(String.format(Locale.US, "%d:%d", minutes, seconds));
            }
            onBind = false;
        }

        @Override
        public void onClick(View v) {
            if(isHeader()){
                adapter.toggleSectionExpanded(getRelativePosition().section());
            } else {
                //TODO: Send to the other RV
            }
        }

        @Override
        public boolean onLongClick(View v) {
            // TODO: Open Bottom Sheet
            return false;
        }
    }
    //endregion

    //region PRIVATE_VARS
    // Data
    private List<Set> mUserSetData;
    private List<Set> mDefaultSetData;
    private List<Set> mUserCreatedSetData;
    // UI Components
    private Context mContext;
    // Adapter Type
    private int mAdapterType;
    //endregion

    //region INTERFACES
    public interface SectionedSetListener{

    }
    private SectionedSetListener mListener;
    //endregion

    //region LIFECYCLE
    public SectionedSetAdapter(Context context){
        mContext = context;

        mUserSetData = new ArrayList<>();
        mDefaultSetData = new ArrayList<>();
        mUserCreatedSetData = new ArrayList<>();

        if(context instanceof SectionedSetListener){
            mListener = (SectionedSetListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement SectionedSetListener");
        }
    }

    @NonNull
    @Override
    public SectionedSetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layout = 0;
        switch (viewType){
            case VIEW_TYPE_HEADER:
                layout = R.layout.list_item_header;
                break;
            case VIEW_TYPE_ITEM:
                layout = R.layout.list_set_items;
                break;
            case VIEW_TYPE_FOOTER:
                break;
            default:
                layout = R.layout.list_set_items;
                break;
        }
        View v = LayoutInflater.from(mContext).inflate(layout, parent, false);
        return new SectionedSetViewHolder(v, this);
    }

    @Override
    public void onBindHeaderViewHolder(SectionedSetViewHolder holder, int section, boolean expanded) {
        holder.bindToHeader(section, expanded);
    }

    @Override
    public void onBindViewHolder(SectionedSetViewHolder holder, int section, int relativePosition, int absolutePosition) {
        Set curSet = null;
        switch (section){
            case DEFAULT_SECTION:
                curSet = mDefaultSetData.get(relativePosition);
                break;
            case USER_MADE_SECTION:
                curSet = mUserCreatedSetData.get(relativePosition);
                break;
            case USER_SETS_SECTION:
                curSet = mUserSetData.get(relativePosition);
                break;
            default:
                throw new RuntimeException(DEBUG_TAG + " set doesn't exist in any lists");
        }
        holder.bindToItem(curSet);
    }
    //endregion

    //region GETTERS
    @Override
    public int getSectionCount() {
        if(mAdapterType == LEFT_VIEW){
            return 2;
        } else if (mAdapterType == RIGHT_VIEW) {
            return 1;
        } else {
            return 1;
        }
    }

    @Override
    public int getItemCount(int section) {
        if(section == DEFAULT_SECTION){
            return mDefaultSetData == null ? 0 : mDefaultSetData.size();
        } if (section == USER_MADE_SECTION){
            return mUserCreatedSetData == null ? 0 : mUserCreatedSetData.size();
        } else if (section == USER_SETS_SECTION){
            return mUserSetData == null ? 0 : mUserSetData.size();
        } else
            return 0;
    }
    //endregion

    //region UTILITY
    public void setDefaultSets(List<Set> sets){
        mDefaultSetData = sets;
        notifySectionChanged(DEFAULT_SECTION);
    }
    public void setUserCreated(List<Set> sets){
        mUserCreatedSetData = sets;
        notifySectionChanged(USER_MADE_SECTION);
    }
    //endregion

    //region UNUSED
    @Override
    public void onBindFooterViewHolder(SectionedSetViewHolder holder, int section) {

    }
    //endregion
}
