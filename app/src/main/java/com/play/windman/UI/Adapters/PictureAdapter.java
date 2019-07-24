package com.play.windman.UI.Adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.play.windman.Beans.Picture;
import com.play.windman.R;
import com.play.windman.UI.Activitys.PictureActivity;

import java.util.List;

public class PictureAdapter extends RecyclerView.Adapter<PictureAdapter.ViewHolder> {

    private static final String TAG = "PictureAdapter";

    private Context mContext;

    private List<Picture> mPictureList;

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView pictureImage;
        TextView pictureName;

        public ViewHolder(View view) {
            super(view);
            cardView = (CardView) view;
            pictureImage = view.findViewById(R.id.picture_image);
            pictureName = view.findViewById(R.id.picture_name);
        }
    }

    public PictureAdapter(List<Picture> pictureList) {
        mPictureList = pictureList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (mContext == null) {
            mContext = parent.getContext();
        }
        View view = LayoutInflater.from(mContext).inflate(R.layout.picture_item, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                Picture picture = mPictureList.get(position);
                Intent intent = new Intent(mContext, PictureActivity.class);
                intent.putExtra(PictureActivity.PICTURE_NAME, picture.getName());
                intent.putExtra(PictureActivity.PICTURE_ID, picture.getImageId());
                mContext.startActivity(intent);
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Picture picture = mPictureList.get(position);
        holder.pictureName.setText(picture.getName());
        Glide.with(mContext).load(picture.getImageId()).into(holder.pictureImage);
    }

    @Override
    public int getItemCount() {
        return mPictureList.size();
    }

}
