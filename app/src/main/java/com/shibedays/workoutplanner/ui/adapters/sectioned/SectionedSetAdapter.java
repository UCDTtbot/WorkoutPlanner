package com.shibedays.workoutplanner.ui.adapters.sectioned;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.util.Log;
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
    private static final String PACKAGE = "com.shibedays.workoutplanner.ui.adapters.sectioned.SectionedSetAdapter.";

    // Sections
    public static final int DEFAULT_AND_USER_SETS = 0;
    public static final int USER_CREATED_SECTION = 1;
    // Recycler View Constant
    public static final int LEFT_VIEW = 0;
    public static final int RIGHT_VIEW = 1;
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
        // Footer
        private CardView footerCard;
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
            // Header Item Views
            footerCard = itemView.findViewById(R.id.add_set_card_view);
            this.adapter = adapter;

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        void bindToHeader(int section, boolean expanded){
            if(section == DEFAULT_AND_USER_SETS) {
                if (mAdapterType == LEFT_VIEW){
                    title.setText(R.string.header_title_default);
                    caret.setImageResource(expanded ? R.drawable.ic_down_arrow_black_24dp : R.drawable.ic_right_arrow_24dp);
                } else if(mAdapterType == RIGHT_VIEW){
                    title.setText(R.string.header_title_my_sets);
                    caret.setImageResource(expanded ? R.drawable.ic_down_arrow_black_24dp : R.drawable.ic_right_arrow_24dp);
                }
            } else if (section == USER_CREATED_SECTION){
                title.setText(R.string.header_title_user_created);
                caret.setImageResource(expanded ? R.drawable.ic_down_arrow_black_24dp : R.drawable.ic_right_arrow_24dp);
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

        void bindFooter(int section){
            if(section != USER_CREATED_SECTION){
                footerCard.setVisibility(View.GONE);
            } else {
                footerCard.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onClick(View v) {
            if(isHeader()){
                adapter.toggleSectionExpanded(getRelativePosition().section());
            } else if (isFooter()){
                Log.d(DEBUG_TAG, "clicked footer");
            } else {
                mListener.onClick(curSet);
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
    // Flags
    private boolean mHeaderSetup;
    private boolean mItemSetup;
    //endregion

    //region INTERFACES
    public interface SectionedSetListener{
        void onClick(Set set);
    }
    private SectionedSetListener mListener;
    //endregion

    //region LIFECYCLE
    public SectionedSetAdapter(Context context, int adapterType, SectionedSetListener listener){
        mContext = context;

        mUserSetData = new ArrayList<>();
        mDefaultSetData = new ArrayList<>();
        mUserCreatedSetData = new ArrayList<>();

        mHeaderSetup = false;
        mItemSetup = false;

        mAdapterType = adapterType;

        mListener = listener;
    }

    @NonNull
    @Override
    public SectionedSetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layout = 0;
        switch (viewType){
            case VIEW_TYPE_HEADER:
                layout = R.layout.list_item_header;
                mHeaderSetup = true;
                break;
            case VIEW_TYPE_ITEM:
                layout = R.layout.list_set_items;
                mItemSetup = true;
                break;
            case VIEW_TYPE_FOOTER:
                layout = R.layout.list_set_footer;
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
            case DEFAULT_AND_USER_SETS:
                if(mAdapterType == LEFT_VIEW)
                    curSet = mDefaultSetData.get(relativePosition);
                else if (mAdapterType == RIGHT_VIEW)
                    curSet = mUserSetData.get(relativePosition);
                break;
            case USER_CREATED_SECTION:
                curSet = mUserCreatedSetData.get(relativePosition);
                break;
            default:
                throw new RuntimeException(DEBUG_TAG + " set doesn't exist in any lists");
        }
        holder.bindToItem(curSet);
    }

    @Override
    public void onBindFooterViewHolder(SectionedSetViewHolder holder, int section) {
        holder.bindFooter(section);
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
        if(section == DEFAULT_AND_USER_SETS){
            if(mAdapterType == LEFT_VIEW)
                return mDefaultSetData == null ? 0 : mDefaultSetData.size();
            else if(mAdapterType == RIGHT_VIEW)
                return mUserSetData == null ? 0 : mUserSetData.size();
        } if (section == USER_CREATED_SECTION){
            return mUserCreatedSetData == null ? 0 : mUserCreatedSetData.size();
        } else
            return 0;
    }
    //endregion

    //region UTILITY
    public void setDefaultSets(List<Set> sets){
        mDefaultSetData = sets;
    }
    public void setUserCreated(List<Set> sets){
        mUserCreatedSetData = sets;
    }
    public void setUserSets(List<Set> sets){
        mUserSetData = sets;
    }

    public void addToUserSets(Set set){
        mUserSetData.add(set);
        //notifyItemInserted(mUserSetData.indexOf(set));
        notifyDataSetChanged();
    }
    //endregion
}
