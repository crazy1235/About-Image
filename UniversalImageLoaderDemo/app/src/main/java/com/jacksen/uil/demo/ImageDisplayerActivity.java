package com.jacksen.uil.demo;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.CircleBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.RoundedVignetteBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

public class ImageDisplayerActivity extends AppCompatActivity {

    private ImageView imageView1, imageView2, imageView3, imageView4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_displayer);

        imageView1 = (ImageView) findViewById(R.id.image_view1);
        imageView2 = (ImageView) findViewById(R.id.image_view2);
        imageView3 = (ImageView) findViewById(R.id.image_view3);
        imageView4 = (ImageView) findViewById(R.id.image_view4);

        init();
    }

    private void init() {

//        DisplayImageOptions imageOptions = DisplayImageOptions.createSimple();

        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.default_img) // 加载过程中的显示图片
                .showImageForEmptyUri(R.drawable.default_img) // 路径为空时显示的图片
                .showImageOnFail(R.drawable.default_img) // 加载失败显示的图片
//                .resetViewBeforeLoading(false) // 将要开始加载时是否需要替换成onLoading图片
//                .delayBeforeLoading(1000) // 加载延迟时间
//                .preProcessor() // 图片加入缓存之前的处理
//                .postProcessor() // 图片
//                .decodingOptions(BitmapFactory.Options) // 解码参数
                .cacheInMemory(true) // 需要缓存在内存中
                .cacheOnDisk(true) // 需要缓存到磁盘中
//                .considerExifParams(true) //
//                .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2) // 缩放类型
//                .bitmapConfig(Bitmap.Config.RGB_565) // bitmap模式
//                .displayer(new RoundedBitmapDisplayer(20)) // 设置图片显示形式(圆角 or 渐变等)
                .build();

        ImageLoader.getInstance().loadImage("xxxx", new SimpleImageLoadingListener(){
            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                super.onLoadingComplete(imageUri, view, loadedImage);
            }
        });

        ImageLoader.getInstance().displayImage(Constants.IMG_LIST.get(5), imageView1, options);

        options = new DisplayImageOptions.Builder().cloneFrom(options)
                .displayer(new CircleBitmapDisplayer(Color.BLUE, 15))
                .build();

        ImageLoader.getInstance().displayImage(Constants.IMG_LIST.get(1), imageView2, options);


        options = new DisplayImageOptions.Builder().cloneFrom(options)
                .displayer(new FadeInBitmapDisplayer(2000))
                .build();

        ImageLoader.getInstance().displayImage(Constants.IMG_LIST.get(6), imageView3, options);

        options = new DisplayImageOptions.Builder().cloneFrom(options)
                .displayer(new RoundedVignetteBitmapDisplayer(20, 20))
                .build();

        ImageLoader.getInstance().displayImage(Constants.IMG_LIST.get(3), imageView4, options);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_refresh, menu);
        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getGroupId() == R.id.action_refresh) {
            ImageLoader.getInstance().clearMemoryCache();
            ImageLoader.getInstance().clearDiskCache();
            init();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
