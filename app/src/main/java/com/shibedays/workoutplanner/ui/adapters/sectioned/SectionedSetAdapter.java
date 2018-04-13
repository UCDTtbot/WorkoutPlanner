package com.shibedays.workoutplanner.ui.adapters.sectioned;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.sectionedrecyclerview.SectionedRecyclerViewAdapter;
import com.afollestad.sectionedrecyclerview.SectionedViewHolder;
import com.shibedays.workoutplanner.BaseApp;
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
        private int footerHeight;
        private int footerWidth;
        // Is Binding
        private boolean onBind;
        // Adapter
        private SectionedSetAdapter adapter;
        // Section and Pos
        private int section;
        private int relativePos;

        SectionedSetViewHolder(View itemView, SectionedSetAdapter adapter) {
            super(itemView);
            // Header Item Views
            title = itemView.findViewById(R.id.header_title);
            caret = itemView.findViewById(R.id.caret);
            // List Item Views
            setNameTextView = itemView.findViewById(R.id.set_name_narrow);
            timeTextView = itemView.findViewById(R.id.set_time_narrow);
            // Footer Item Views
            footerCard = itemView.findViewById(R.id.add_set_card_view);
            if(footerCard != null) {
                ViewTreeObserver observer = footerCard.getViewTreeObserver();
                observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        footerHeight = footerCard.getHeight();
                        footerWidth = footerCard.getWidth();
                    }
                });
            }
            this.adapter = adapter;

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        void bindToHeader(int section, boolean expanded){
            if(mSectionList != null){
                title.setText(mSectionList.get(section));
                caret.setImageResource(expanded ? R.drawable.ic_down_arrow_black_24dp : R.drawable.ic_right_arrow_24dp);
            } else {
                title.setText("");
            }
        }

        void bindToItem(final Set curSet, int section, int relPos){
            onBind = true;
            //Populate data when they bind the workouts to the view holder
            this.curSet = curSet;

            this.section = section;
            relativePos = relPos;

            setNameTextView.setText(curSet.getName());
            int[] time = BaseApp.convertFromMillis(curSet.getTime());
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
            if(section != mNewSetFooterSection){
                footerCard.setVisibility(View.GONE);
                //footerCard.setLayoutParams(new CardView.LayoutParams(footerWidth, 0));
            } else {
                footerCard.setVisibility(View.VISIBLE);
                //footerCard.setLayoutParams(new CardView.LayoutParams(footerWidth, footerHeight));

            }
        }

        @Override
        public void onClick(View v) {
            if(isHeader()){
                adapter.toggleSectionExpanded(getRelativePosition().section());
            } else if (isFooter()){
                mListener.createUserSet(section);
            } else {
                mListener.onClick(curSet, section,  relativePos);
            }
        }

        @Override
        public boolean onLongClick(View v) {
            if(isHeader()){
                // Nothing
            } else if (isFooter()){
                // Nothing
            } else {
                mListener.onLongClick(curSet, section, relativePos);

            }
            return true;
        }
    }
    //endregion

    //region PRIVATE_VARS
    // Data
    private List<List<Set>> mDataLists; // Section 0 should correspond to DataList 0

    // UI Components
    private Context mContext;
    // Adapter Type
    private int mViewType;
    // Sections
    private List<String> mSectionList; // Section List and Data List should use same indexing/mapping
    private int mNewSetFooterSection;
    // Flags
    private boolean mHeaderSetup;
    private boolean mItemSetup;
    //endregion

    //region INTERFACES
    public interface SectionedAddSetListener{
        void onClick(Set set, int section, int relativePos);
        void onLongClick(Set set, int section, int relativePos);
        void createUserSet(int section);
    }
    private SectionedAddSetListener mListener;
    //endregion

    //region LIFECYCLE

    public SectionedSetAdapter(Context context, List<String> sectionList, int footerSection, SectionedAddSetListener listener){
        mContext = context;

        mHeaderSetup = false;
        mItemSetup = false;

        mSectionList = sectionList;
        mNewSetFooterSection = footerSection;

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
                layout = R.layout.list_set_items_narrow;
                mItemSetup = true;
                break;
            case VIEW_TYPE_FOOTER:
                layout = R.layout.list_set_footer;
                break;
            default:
                layout = R.layout.list_set_items_narrow;
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
        curSet = mDataLists.get(section).get(relativePosition);
        holder.bindToItem(curSet, section, relativePosition);
    }

    @Override
    public void onBindFooterViewHolder(SectionedSetViewHolder holder, int section) {
        holder.bindFooter(section);
    }

    //endregion

    //region GETTERS
    @Override
    public int getSectionCount() {
        return mSectionList.size();
    }

    @Override
    public int getItemCount(int section) {
        if(mDataLists != null){
            return mDataLists.get(section) == null ? 0 : mDataLists.get(section).size();
        } else {
            return 0;
        }
    }
    //endregion

    //region UTILITY
    public void addToDataList(List<Set> sets){
        if(mDataLists == null){
            mDataLists = new ArrayList<>();
        }
        mDataLists.add(sets);
        notifyDataSetChanged();
    }

    public void setDataList(List<List<Set>> setList){
        mDataLists = setList;
    }

    public void updateSetList(int section, List<Set> sets){
        mDataLists.set(section, sets);
        notifyDataSetChanged();
    }

    public void addSet(int section, Set set){
        mDataLists.get(section).add(set);
        notifyDataSetChanged();
    }

    public void removeSet(int section, int pos){
        mDataLists.get(section).remove(pos);
        notifyDataSetChanged();
    }
    //endregion

}
