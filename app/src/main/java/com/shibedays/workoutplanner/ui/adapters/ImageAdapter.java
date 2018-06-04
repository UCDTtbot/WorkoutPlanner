package com.shibedays.workoutplanner.ui.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.shibedays.workoutplanner.R;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {

    //region CONSTANTS
    private static final String DEBUG_TAG = ImageAdapter.class.getSimpleName();
    private static final String PACKAGE = "com.shibedays.workoutplanner.ui.adapters.ImageAdapter.";
    //endregion

    class ImageViewHolder extends RecyclerView.ViewHolder {

        ImageView set_image;
        CheckBox mCheckBox;
        int image_id;
        boolean setup;


        public ImageViewHolder(View itemView) {
            super(itemView);

            setup = false;
            mCheckBox = itemView.findViewById(R.id.choose_image_check);
            mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if(mImageMap.get(image_id) != b) {
                        mListener.changeChecked(image_id);
                        mImageMap.put(mSelectedImage, false);
                        mImageMap.put(image_id, true);
                        notifyItemChanged(mImageIds.indexOf(mSelectedImage));
                        notifyItemChanged(mImageIds.indexOf(image_id));
                        mSelectedImage = image_id;
                    }
                }
            });
            set_image = itemView.findViewById(R.id.image_view);
            set_image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mCheckBox.setChecked(!mCheckBox.isChecked());
                }
            });
            setup = true;
        }
    }

    //region PRIVATE_VARS
    // Data
    private List<Integer> mImageIds;
    private HashMap<Integer, Boolean> mImageMap;
    private int mSelectedImage;
    // UI
    private Context mContext;
    // FLAGS

    //endregion

    //region INTERFACES
    public interface ImageListener{
        void changeChecked(int id);
    }
    private ImageListener mListener;
    //endregion

    //region LIFECYCLE
    public ImageAdapter(Context context, ImageListener listener){
        mContext = context;
        mListener = listener;
    }

    @NonNull
    @Override
    public ImageAdapter.ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ImageViewHolder(LayoutInflater.from(mContext)
                .inflate(R.layout.list_image_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ImageAdapter.ImageViewHolder holder, int position) {
        // Bind UI stuff for each item
        holder.image_id = mImageIds.get(position);
        boolean checked = mImageMap.get(holder.image_id);
        holder.mCheckBox.setChecked(checked);

        Glide.with(mContext).load(holder.image_id).into(holder.set_image);
    }

    //endregion

    //region UTILITY
    @Override
    public int getItemCount() {
        return mImageIds != null ? mImageIds.size() : 0;
    }

    public void setImageList(List<Integer> image_ids, HashMap<Integer, Boolean> image_map, int selected){
        mImageIds = image_ids;
        mImageMap = image_map;
        mSelectedImage = selected;
    }
    //endregion

}
