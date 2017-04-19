package com.jacksen.uil.demo;

import android.app.Application;
import android.content.Context;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

/**
 * @author jacksen
 */

public class BaseApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        initImageLoader(getApplicationContext());
    }

    private void initImageLoader(Context context) {
        ImageLoaderConfiguration.Builder builder = new ImageLoaderConfiguration.Builder(context);
        builder.denyCacheImageMultipleSizesInMemory();
        builder.threadPriority(Thread.NORM_PRIORITY - 2);
        builder.tasksProcessingOrder(QueueProcessingType.LIFO);
        builder.diskCacheSize(50 * 1024 * 1024);
        builder.writeDebugLogs();

        ImageLoader.getInstance().init(builder.build());
    }
}
