package com.shibedays.workoutplanner.ui.adapters;

import android.support.v7.widget.RecyclerView;

public abstract class PendingRemovalAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {
    public abstract void pendingRemoval(final int swipedPos);
    public abstract void deletePending(int pendingIndex, int origPos);
    public abstract boolean isPendingRemoval(int pos);
    public abstract void undo(int itemPos, int pendingPos);
}
