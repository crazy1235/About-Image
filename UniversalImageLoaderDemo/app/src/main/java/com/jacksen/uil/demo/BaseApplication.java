package com.jacksen.uil.demo;

import android.app.Application;
import android.content.Context;

import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiskCache;
import com.nostra13.universalimageloader.cache.disc.impl.ext.LruDiskCache;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.LimitedMemoryCache;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.cache.memory.impl.UsingFreqLimitedMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.nostra13.universalimageloader.utils.StorageUtils;

import java.io.File;

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

        File cacheDir = StorageUtils.getOwnCacheDirectory(getApplicationContext(), "imageloader/Cache");

        /*ImageLoaderConfiguration configuration = ImageLoaderConfiguration.createDefault(this);
        ImageLoader.getInstance().init(configuration);*/


        ImageLoaderConfiguration.Builder builder = new ImageLoaderConfiguration.Builder(context);
        builder.threadPoolSize(5); // 线程池大小
        builder.threadPriority(Thread.NORM_PRIORITY - 2); // 设置线程优先级
        builder.denyCacheImageMultipleSizesInMemory(); // 不允许在内存中缓存同一张图片的多个尺寸
        builder.tasksProcessingOrder(QueueProcessingType.LIFO); // 设置处理队列的类型，包括LIFO， FIFO
        builder.memoryCache(new LruMemoryCache(3 * 1024 * 1024)); // 内存缓存策略
        builder.memoryCacheSize(5 * 1024 * 1024);  // 内存缓存大小
        builder.memoryCacheExtraOptions(480, 800); // 内存缓存中每个图片的最大宽高
        builder.memoryCacheSizePercentage(50); // 内存缓存占总内存的百分比
        builder.diskCache(new UnlimitedDiskCache(cacheDir)); // 设置磁盘缓存策略
        builder.diskCacheSize(50 * 1024 * 1024); // 设置磁盘缓存的大小
        builder.diskCacheFileCount(50); // 磁盘缓存文件数量
        builder.diskCacheFileNameGenerator(new Md5FileNameGenerator()); // 磁盘缓存时图片名称加密方式
        builder.imageDownloader(new BaseImageDownloader(this)); // 图片下载器
        builder.defaultDisplayImageOptions(DisplayImageOptions.createSimple());
        builder.writeDebugLogs(); // 打印日志

        ImageLoader.getInstance().init(builder.build());
    }
}
