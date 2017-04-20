package com.jacksen.uil.demo;

import android.support.v7.widget.RecyclerView;
import android.widget.AbsListView;

import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * @author jacksen
 *         <br/>
 * @since 2017/4/20
 */

public class RVPauseOnScrollListener extends RecyclerView.OnScrollListener {

    private ImageLoader imageLoader;
    private final boolean pauseOnScroll;
    private final boolean pauseOnFling;

    public RVPauseOnScrollListener(ImageLoader imageLoader, boolean pauseOnScroll, boolean pauseOnFling) {
        this.imageLoader = imageLoader;
        this.pauseOnScroll = pauseOnScroll;
        this.pauseOnFling = pauseOnFling;
    }

    @Override
    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        super.onScrollStateChanged(recyclerView, newState);
        switch (newState) {
            case RecyclerView.SCROLL_STATE_IDLE: // 滑动已经停止
                imageLoader.resume();
                break;
            case RecyclerView.SCROLL_STATE_SETTLING: //　
                if (pauseOnFling) {
                    imageLoader.pause();
                }
                break;
            case RecyclerView.SCROLL_STATE_DRAGGING: // 拖动滑动
                if (pauseOnScroll) {
                    imageLoader.pause();
                }
                break;
        }
    }
}
