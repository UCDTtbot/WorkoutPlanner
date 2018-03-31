package com.shibedays.androidswipelayout.adapters;


import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.shibedays.androidswipelayout.SwipeLayout;
import com.shibedays.androidswipelayout.implments.SwipeItemMangerImpl;
import com.shibedays.androidswipelayout.interfaces.SwipeAdapterInterface;
import com.shibedays.androidswipelayout.interfaces.SwipeItemMangerInterface;
import com.shibedays.androidswipelayout.util.Attributes;

import java.util.List;

public abstract class RecyclerSwipeAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> implements SwipeItemMangerInterface, SwipeAdapterInterface {

    public SwipeItemMangerImpl mItemManager = new SwipeItemMangerImpl(this);

    @Override
    public abstract VH onCreateViewHolder(ViewGroup parent, int viewType);

    @Override
    public abstract void onBindViewHolder(VH viewHolder, final int position);

    @Override
    public void notifyDatasetChanged() {
        super.notifyDataSetChanged();
    }

    @Override
    public void openItem(int position) {
        mItemManager.openItem(position);
    }

    @Override
    public void closeItem(int position) {
        mItemManager.closeItem(position);
    }

    @Override
    public void closeAllExcept(SwipeLayout layout) {
        mItemManager.closeAllExcept(layout);
    }

    @Override
    public void closeAllItems() {
        mItemManager.closeAllItems();
    }

    @Override
    public List<Integer> getOpenItems() {
        return mItemManager.getOpenItems();
    }

    @Override
    public List<SwipeLayout> getOpenLayouts() {
        return mItemManager.getOpenLayouts();
    }

    @Override
    public void removeShownLayouts(SwipeLayout layout) {
        mItemManager.removeShownLayouts(layout);
    }

    @Override
    public boolean isOpen(int position) {
        return mItemManager.isOpen(position);
    }

    @Override
    public Attributes.Mode getMode() {
        return mItemManager.getMode();
    }

    @Override
    public void setMode(Attributes.Mode mode) {
        mItemManager.setMode(mode);
    }
}
