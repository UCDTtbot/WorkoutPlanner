package com.shibedays.workoutplanner.ui.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.sectionedrecyclerview.SectionedRecyclerViewAdapter;
import com.afollestad.sectionedrecyclerview.SectionedViewHolder;

public abstract class PendingRemovalAdapter<VH extends SectionedViewHolder> extends SectionedRecyclerViewAdapter<VH>{

    public abstract void pendingRemoval(final int swipedPos);
    public abstract void deletePending(int pendingIndex, int origPos);
    public abstract boolean isPendingRemoval(int pos);
    public abstract void undo(int itemPos, int pendingPos);

}
