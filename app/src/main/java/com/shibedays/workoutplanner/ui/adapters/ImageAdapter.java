package com.shibedays.workoutplanner.ui.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.shibedays.workoutplanner.R;

import java.util.HashMap;
import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {

    //region CONSTANTS
    private static final String DEBUG_TAG = ImageAdapter.class.getSimpleName();
    private static final String PACKAGE = "com.shibedays.workoutplanner.ui.adapters.ImageAdapter.";
    //endregion

    class ImageViewHolder extends RecyclerView.ViewHolder {

        ImageView set_image;
        TextView set_title;

        public ImageViewHolder(View itemView) {
            super(itemView);
            set_image = itemView.findViewById(R.id.image_view);
            set_title = itemView.findViewById(R.id.image_title);
        }
    }

    //region PRIVATE_VARS
    // Data
    private List<Integer> mImageList;
    // UI
    private Context mContext;
    // FLAGS

    //endregion

    //region INTERFACES
    public interface ImageListener{

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
        int image_id = mImageList.get(position);
        holder.set_title.setText("Image Name");

        Glide.with(mContext).load(image_id).into(holder.set_image);
    }
    //endregion

    //region UTILITY
    @Override
    public int getItemCount() {
        return mImageList != null ? mImageList.size() : 0;
    }

    public void setImageList(List<Integer> image_ids){
        mImageList = image_ids;
    }
    //endregion

}
