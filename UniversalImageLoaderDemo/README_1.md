

[TOC]

作为图片加载类库的"老大哥"，相信大部分人都用过它！

不管是自己再项目里面用，还是自己造轮子，了解UIL的用法和框架原理都是有帮助的！

下面就来总结下UIL的基本使用！

---

### UIL的功能及优点

- 支持从网络，本地，项目内读取图片

- 多线程下载图片

- 支持图片下载进度监听

- 支持图片加载起始，加载中，加载结束等毁掉函数

- 包含内存缓存和磁盘缓存， 并可进行缓存大小等参数的配置

- 支持占位图，及图片加载过程中图片，以及图片加载失败图片的显示

- 支持图片加载成功后再处理

- 支持对bitmap裁剪等处理

- 可在ListView等滚动控件发生滚动时，暂定图片加载

- 提供在网路较慢时图片的加载

- 扩展性强，内存缓存和磁盘缓存策略可自行实现，图片加载各种参数也可自由配置！

---

### UIL的使用

> 
Github地址：
https://github.com/nostra13/Android-Universal-Image-Loader



方式一： 

下载jar包，导入项目

[universal-image-loader-1.9.5.jar](https://github.com/nostra13/Android-Universal-Image-Loader/raw/master/downloads/universal-image-loader-1.9.5.jar)

方式二：

直接在build.gradle中添加依赖项

> **compile 'com.nostra13.universalimageloader:universal-image-loader:1.9.3'**  


接下来在项目中添加【访问网络】和【写入外部存储】两个权限

```
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />  
<uses-permission android:name="android.permission.INTERNET" />  
```

接着， 配置UIL的初始化：

```
ImageLoaderConfiguration configuration = ImageLoaderConfiguration.createDefault(this);
ImageLoader.getInstance().init(configuration);
```

通过上面两行代码就可以完成UIL的初始配置！不错这是最简单的配置， 后面会详细说具体的配置！

然后需要配置显示图片的相关参数，比如图片是否需要缓存，是否需要占位图，是否需要圆角处理等等

```
DisplayImageOptions imageOptions = DisplayImageOptions.createSimple();
```
老规矩，还是先来个最简单的配置。

最后就显示图片了

```
ImageLoader.getInstance().displayImage("http://img.hb.aicdn.com/278475ced55b8361e0d2beb9b568dd225f1c8c9d1593b-1F63BW_fw658", imageView1, imageOptions);
```

OK，运行一下图片就可以出来了！（在手机联网的情况下啊！O(∩_∩)O）

---

### 具体配置

上面提到了3个重要的类

- ImageLoaderConfiguration

- ImageLoader

- DisplayImageOptions 

- ImageLoadingListener

先来一一介绍。

---

**ImageLoaderConfiguration** 是针对ImageLoader这个框架的全局配置。包括线程池线程数量，磁盘缓存大小，内存缓存大小，缓存文件数量，缓存文件路径等配置。

它使用了  <font color="blue"> **建造者模式** </font>， 还提供了一个静态函数 `createDefault(Context context)` 来创建一个最基本的配置。

详细配置：

```
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
```

---


**ImageLoader**

是UIL初始化和加载显示图片的关键类！

通过  <font color="blue"> **单例模式** </font> 进行初始化！

```
private volatile static ImageLoader instance;

	/** Returns singleton class instance */
	public static ImageLoader getInstance() {
		if (instance == null) {
			synchronized (ImageLoader.class) {
				if (instance == null) {
					instance = new ImageLoader();
				}
			}
		}
		return instance;
	}

	protected ImageLoader() {
	}
```

```
public synchronized void init(ImageLoaderConfiguration configuration) {
		if (configuration == null) {
			throw new IllegalArgumentException(ERROR_INIT_CONFIG_WITH_NULL); // 异常
		}
		if (this.configuration == null) {
			L.d(LOG_INIT_CONFIG);
			engine = new ImageLoaderEngine(configuration);
			this.configuration = configuration;
		} else {
			L.w(WARNING_RE_INIT_CONFIG);
		}
	}
```

所以当没有设置ImageLoaderConfirguation的时候，会抛出异常！

内部包含很多个重载的displayImage() 方法， loadImage()方法, loadImageSync()方法！

![这里写图片描述](http://img.blog.csdn.net/20170420215832801?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvY3JhenkxMjM1/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)


![这里写图片描述](http://img.blog.csdn.net/20170420215843485?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvY3JhenkxMjM1/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)


![这里写图片描述](http://img.blog.csdn.net/20170420215851614?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvY3JhenkxMjM1/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

displayImage() 异步加载图片并显示

loadImage() 异步加载图片

loadImageSync() 同步加载图片

---

**DisplayImageOptions** 

在配置 ImageLoaderConfiguration 的时候就可以统一配置图片的参数！但是我们也可以针对不同的图片做不同的配置！

```
DisplayImageOptions options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.default_img) // 加载过程中的显示图片
                .showImageForEmptyUri(R.drawable.default_img) // 路径为空时显示的图片
                .showImageOnFail(R.drawable.default_img) // 加载失败显示的图片
                .resetViewBeforeLoading(false) // 将要开始加载时是否需要替换成onLoading图片
                .delayBeforeLoading(1000) // 加载延迟时间
                .preProcessor() // 图片加入缓存之前的处理
                .postProcessor() // 图片
                .decodingOptions(BitmapFactory.Options) // 解码参数
                .cacheInMemory(true) // 需要缓存在内存中
                .cacheOnDisk(true) // 需要缓存到磁盘中
                .considerExifParams(true) //
                .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2) // 缩放类型
                .bitmapConfig(Bitmap.Config.RGB_565) // bitmap模式
                .displayer(new RoundedBitmapDisplayer(20)) // 设置图片显示形式(圆角 or 渐变等)
                .build();
```
**ImageLoadingListener**

在加载图片的时候设置监听器可以监听到开始，失败，成功，取消等回调！

```
public interface ImageLoadingListener {

	void onLoadingStarted(String imageUri, View view);

	void onLoadingFailed(String imageUri, View view, FailReason failReason);

	void onLoadingComplete(String imageUri, View view, Bitmap loadedImage);

	void onLoadingCancelled(String imageUri, View view);
}
```

也可以使用它的实现类 **SimpleImageLoadingListener** ， 这样可以在代码中少看到一些不需要的回调！


```
public class SimpleImageLoadingListener implements ImageLoadingListener {
	@Override
	public void onLoadingStarted(String imageUri, View view) {
		// Empty implementation
	}

	@Override
	public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
		// Empty implementation
	}

	@Override
	public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
		// Empty implementation
	}

	@Override
	public void onLoadingCancelled(String imageUri, View view) {
		// Empty implementation
	}
}
```



----



按照项目所需进行配置即可！！！

---

### 滚动时暂定加载

当使用ListView，RecyclerView， GridView的时候，滚动屏幕时暂停图片加载可减少界面卡顿！

```
listView.setOnScrollListener(new PauseOnScrollListener(ImageLoader.getInstance(), true, true));
```

```
gridView.setOnScrollListener(new PauseOnScrollListener(imageLoader, pauseOnScroll, pauseOnFling));  

```

```
recyclerView.addOnScrollListener(new RVPauseOnScrollListener(ImageLoader.getInstance(), true, true));
```

UIL项目中并没有关于RecyclerView类滚动事件的监听判断！ **RVPauseOnScrollListener** 是我自己写的一个扩展类！


```
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
            case RecyclerView.SCROLL_STATE_IDLE: // 
                imageLoader.resume();
                break;
            case RecyclerView.SCROLL_STATE_SETTLING: //　
                if (pauseOnFling) {
                    imageLoader.pause();
                }
                break;
            case RecyclerView.SCROLL_STATE_DRAGGING: // 
                if (pauseOnScroll) {
                    imageLoader.pause();
                }
                break;
        }
    }
}
```


---

源码请移步：

https://github.com/crazy1235/About-Image/tree/master/UniversalImageLoaderDemo

OVER !!!~