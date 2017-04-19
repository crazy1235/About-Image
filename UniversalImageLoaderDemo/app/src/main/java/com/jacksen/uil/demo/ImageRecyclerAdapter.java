package com.jacksen.uil.demo;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

/**
 * @author jacksen
 */

public class ImageRecyclerAdapter extends RecyclerView.Adapter<ImageRecyclerAdapter.ViewHolder> {

    private Context context;
    private List<String> mList;

    private DisplayImageOptions options;

    public ImageRecyclerAdapter(Context context, List<String> mList) {
        this.context = context;
        this.mList = mList;

        this.options = new DisplayImageOptions.Builder()
                .showImageOnFail(R.drawable.default_img)
                .showImageOnLoading(R.drawable.default_img)
                .showImageForEmptyUri(R.drawable.default_img)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .build();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_recycler_view, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ImageLoader.getInstance().displayImage(mList.get(position), holder.imageView, options);
    }

    @Override
    public int getItemCount() {
        return mList != null ? mList.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;

        public ViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.image_view);
        }
    }

}
