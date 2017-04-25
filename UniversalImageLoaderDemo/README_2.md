[TOC]

---

在上篇，介绍了UIL的基本使用方法 **[Universal Image Loader 的用法解析](README_1.md)**

---
这篇针对源码进行解析

先祭出一张图！！！

![这里写图片描述](http://img.blog.csdn.net/20170421112608334?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvY3JhenkxMjM1/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)


该图是UIL加载并显示图片的大致流程图！

---

 1. 调用显示图片函数

 2. UIL判断是否为uri地址是否为空

 3. 不为空，判断内存缓存中是否存在此缓存

 4. 如果存在则调用post process处理（如果需要），然后显示

 5. 如果不存在内存，去判断是否缓存到了磁盘上

 6. 如果磁盘上存在，则读取文件decode，然后做pre process处理（如果需要）, 接着缓存到内存中，最后按照步骤4进行

 7. 如果磁盘中不存在，则请求网络下载到磁盘中，然后走步骤6往下进行

---

# 源码包结构

![这里写图片描述](http://img.blog.csdn.net/20170421145008003?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvY3JhenkxMjM1/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)


一级目录包含三大块：

- cache  缓存相关

- core  核心代码相关

- utils  工具类相关


---

## cache

![这里写图片描述](http://img.blog.csdn.net/20170421150550151?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvY3JhenkxMjM1/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)


cache目录结构下，包含两大块！

**disc & memory**

分别是磁盘缓存和内存缓存


----------


### disc

![这里写图片描述](http://img.blog.csdn.net/20170421151111284?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvY3JhenkxMjM1/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)


DiskCache 是一个接口！

里面定义了磁盘缓存的相关函数！

```

	/**
	 * 获取磁盘缓存路径文件
	 */
	File getDirectory();

	/**
	 * 根据图片地址获取本地存储文件
	 */
	File get(String imageUri);

	/**
	 * 保存图片输入流到磁盘上
	 */
	boolean save(String imageUri, InputStream imageStream, IoUtils.CopyListener listener) throws IOException;

	/**
	 * 保存bitmap到磁盘上
	 */
	boolean save(String imageUri, Bitmap bitmap) throws IOException;

	/**
	 * 根据图片地址从本地磁盘中移除
	 */
	boolean remove(String imageUri);

	/** 关闭磁盘缓存，释放资源 */
	void close();

	/** 清空磁盘缓存 */
	void clear();
```


DiskCache及其子类会在后面的博文中详细介绍！

----------


### memory

![这里写图片描述](http://img.blog.csdn.net/20170421151119070?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvY3JhenkxMjM1/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)


MemoryCache 也是一个接口！定义了内存缓存的相关函数！


```
public interface MemoryCache {
	/**
	 * 将bitmap缓存到内存中，如果缓存成功返回true，反之返回false。
	 */
	boolean put(String key, Bitmap value);

	/** 根据key从内存缓存中获取bitmap. */
	Bitmap get(String key);

	/** 根据key从内存缓存中移除一个Bitmap */
	Bitmap remove(String key);

	/** 返回内存缓存中键的集合 */
	Collection<String> keys();

	/** 清空内存缓存*/
	void clear();
}
```

MemoryCache的不同实现子类会在后序博文中详细介绍！


---

## core

![这里写图片描述](http://img.blog.csdn.net/20170422131810916?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvY3JhenkxMjM1/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

core包下面是UIL的核心代码！

包括:

- ImageLoaderEngine -- 图片加载引擎（任务分发器）

- ImageLoaderConfiguration -- UIL的配置类

- ImageLoader  -- 图片加载的入口类

- DisplayImageOptions -- 图片显示的相关配置类

- DefaultConfigurationFactory -- 默认配置工厂类

- LoadAndDisplayImageTask -- 图片加载并显示任务

- ProcessAndDisplayImageTask -- 图片处理并显示任务

- DisplayBitmapTask -- 图片显示的任务

- assist包 下面主要是一些枚举类型的定义和图片下载处理显示任务的队列

	![这里写图片描述](http://img.blog.csdn.net/20170422132632461?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvY3JhenkxMjM1/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

- decode包 -- 图片解码器。将从网络or本地等途径获取输入流解码为Bitmap。主要实现类是 <font color="blue">**BaseImageDecoder** </font>

	![这里写图片描述](http://img.blog.csdn.net/20170422132942728?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvY3JhenkxMjM1/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)        

	![这里写图片描述](http://img.blog.csdn.net/20170422142216082?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvY3JhenkxMjM1/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

- display包 -- 将Bitmap对象显示在对应的 ImageAware（ImageView的包装接口类）上！

	![这里写图片描述](http://img.blog.csdn.net/20170422142629179?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvY3JhenkxMjM1/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)


	![这里写图片描述](http://img.blog.csdn.net/20170422143256291?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvY3JhenkxMjM1/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

	(若图片看不清楚，可在新标签页中打开查看)

通过实现类的名字可以看出，可以普通显示bitmap，渐变显示，圆角图片处理显示，圆形图片，以及圆角渐变显示图片！


- download包 -- 负责从网路或者本地读取图片输入流！

	![这里写图片描述](http://img.blog.csdn.net/20170422143716871?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvY3JhenkxMjM1/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)


	![这里写图片描述](http://img.blog.csdn.net/20170422143823542?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvY3JhenkxMjM1/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)


默认只有一个实现类 <font color="blue"> **BaseImageDownloader** </font> 。内部包含6个重要函数，分别从网络、文件、content、asset、drawable、其他途径获取图片流！


- imageaware包 -- 显示图片的对象。

	![这里写图片描述](http://img.blog.csdn.net/20170422144641361?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvY3JhenkxMjM1/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

	![这里写图片描述](http://img.blog.csdn.net/20170422144721585?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvY3JhenkxMjM1/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

在ImageView上显示图片时，比如

```
ImageLoader.getInstance().displayImage("xxx", imageView, options);
```

构造的是一个 <font color="blue"> **ImageViewAware** </font>对象。

调用 loadImage时

```
ImageLoader.getInstance().loadImage("xxxx", new SimpleImageLoadingListener(){
            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                super.onLoadingComplete(imageUri, view, loadedImage);
            }
        });
```

构造的是一个 <font color="blue"> **NonViewAware** </blue>.

- listener 包 -- 图片加载回调！

	![这里写图片描述](http://img.blog.csdn.net/20170422145558378?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvY3JhenkxMjM1/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)


	![这里写图片描述](http://img.blog.csdn.net/20170422145900930?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvY3JhenkxMjM1/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

```
public interface ImageLoadingListener {

	/**
	 * 开始加载回调
	 */
	void onLoadingStarted(String imageUri, View view);

	/**
	 * 加载失败回调
	 */
	void onLoadingFailed(String imageUri, View view, FailReason failReason);

	/**
	 * 加载完成回调
	 */
	void onLoadingComplete(String imageUri, View view, Bitmap loadedImage);

	/**
	 * 取消加载回调
	 */
	void onLoadingCancelled(String imageUri, View view);
}
```

```
public interface ImageLoadingProgressListener {

	/**
	 * 加载过程中进度回调
	 */
	void onProgressUpdate(String imageUri, View view, int current, int total);
}
```

<font color="blue"> **PauseOnScrollListener** </font> 是给ListView和GridView用的，设置滚动中暂停加载图片可有效避免界面卡顿！

而 <font color="blue"> **RVPauseOnScrollListener** </font> 是我为 RecyclerView 写的一个扩展类。作者并没有为RV写相关的滚动控制！

- process包下之有一个接口 --  <font color="blue"> **BitmapProcessor** </font> ，负责写入缓存或者从缓存读取后进行处理。


	![这里写图片描述](http://img.blog.csdn.net/20170422150725049?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvY3JhenkxMjM1/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)



---

## utils
![这里写图片描述](http://img.blog.csdn.net/20170421150526728?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvY3JhenkxMjM1/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)


utils下面主要是一些工具类，包括IO流处理工具类，磁盘缓存工具类，内存缓存工具类，日志输出类，存储工具类等！

---

上面介绍完了UIL源码的包结构和主要类的用途！下面就按照介绍UIL内部图片加载流程！

## 流程分析

上篇文章中就提到，ImageLoader类中有很多显示加载图片的重载方法！

共有12个displayImage()重载方法，5个loadImage()重载方法，4个loadImageSync()重载方法。

loadImage()的那几个重载方法，最终都是调用的下面的重载方法：

```
public void loadImage(String uri, ImageSize targetImageSize, DisplayImageOptions options,
			ImageLoadingListener listener, ImageLoadingProgressListener progressListener) {
		checkConfiguration();
		if (targetImageSize == null) {
			targetImageSize = configuration.getMaxImageSize();
		}
		if (options == null) {
			options = configuration.defaultDisplayImageOptions;
		}

		// NonViewAware
		NonViewAware imageAware = new NonViewAware(uri, targetImageSize, ViewScaleType.CROP);
		displayImage(uri, imageAware, options, listener, progressListener);
	}
```

上面提到过，load图片并不显示图片，NonViewAware 类是一个没有View对象的包装类，实现ViewAware接口时，getWrappedView() 是个空对象。

```
@Override
	public View getWrappedView() {
		return null;
	}
```

接着看loadImageSync()的重载函数们！

```
public Bitmap loadImageSync(String uri, ImageSize targetImageSize, DisplayImageOptions options) {
		if (options == null) {
			options = configuration.defaultDisplayImageOptions;
		}
		options = new DisplayImageOptions.Builder().cloneFrom(options).syncLoading(true).build();

		SyncImageLoadingListener listener = new SyncImageLoadingListener();
		
		// !!!
		loadImage(uri, targetImageSize, options, listener);
		return listener.getLoadedBitmap();
	}
```

可以看出，调用的是loadImage()。

SyncImageLoadingListener 是一个同步加载图片监听类！

```
private static class SyncImageLoadingListener extends SimpleImageLoadingListener {

		private Bitmap loadedImage;

		@Override
		public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
			this.loadedImage = loadedImage;
		}

		public Bitmap getLoadedBitmap() {
			return loadedImage;
		}
	}
```
由上面的分析，可以看出所有的矛头都指向 <font color="red"> **displayImage()** </font> 。


来看下源码：

```
public void displayImage(String uri, ImageAware imageAware, DisplayImageOptions options,
			ImageSize targetSize, ImageLoadingListener listener, ImageLoadingProgressListener progressListener) {
			
		// 1. 检查参数配置及回到函数
		checkConfiguration();
		if (imageAware == null) {
			throw new IllegalArgumentException(ERROR_WRONG_ARGUMENTS);
		}
		if (listener == null) {
			listener = defaultListener;
		}
		if (options == null) {
			options = configuration.defaultDisplayImageOptions;
		}
		
		// 2. 判断图片地址
		if (TextUtils.isEmpty(uri)) {
			engine.cancelDisplayTaskFor(imageAware);
			listener.onLoadingStarted(uri, imageAware.getWrappedView());
			if (options.shouldShowImageForEmptyUri()) {
				imageAware.setImageDrawable(options.getImageForEmptyUri(configuration.resources));
			} else {
				imageAware.setImageDrawable(null);
			}
			listener.onLoadingComplete(uri, imageAware.getWrappedView(), null);
			return;
		}

		// 3. 目标ImageAware的大小
		if (targetSize == null) {
			targetSize = ImageSizeUtils.defineTargetSizeForView(imageAware, configuration.getMaxImageSize());
		}
		// 4. 根据uri生成内存缓存的key
		String memoryCacheKey = MemoryCacheUtils.generateKey(uri, targetSize);
		// 5. 准备加载任务
		engine.prepareDisplayTaskFor(imageAware, memoryCacheKey);
		// 6. 开始加载回调函数
		listener.onLoadingStarted(uri, imageAware.getWrappedView());

		// 7. 尝试从内存中回去bitmap
		Bitmap bmp = configuration.memoryCache.get(memoryCacheKey);
		if (bmp != null && !bmp.isRecycled()) { // 8. 如果从内存中获取bitmap成功
			
			if (options.shouldPostProcess()) { // 9. 需要后续处理bitmap
				ImageLoadingInfo imageLoadingInfo = new ImageLoadingInfo(uri, imageAware, targetSize, memoryCacheKey,
						options, listener, progressListener, engine.getLockForUri(uri));
				// 10. 构造ProcessAndDisplayImageTask 处理并显示图片
				ProcessAndDisplayImageTask displayTask = new ProcessAndDisplayImageTask(engine, bmp, imageLoadingInfo,
						defineHandler(options));
				// 11. 是否需要同步加载，需要直接运行task，不需要提交到线程池中
				if (options.isSyncLoading()) {
					displayTask.run();
				} else {
					engine.submit(displayTask);
				}
			} else { // 12. 不需要后续处理，直接显示并回调加载成功方法
				options.getDisplayer().display(bmp, imageAware, LoadedFrom.MEMORY_CACHE);
				listener.onLoadingComplete(uri, imageAware.getWrappedView(), bmp);
			}
		} else { //13. 内粗中没有缓存， 然后判断是否需要显示loading图片
			if (options.shouldShowImageOnLoading()) {
				imageAware.setImageDrawable(options.getImageOnLoading(configuration.resources));
			} else if (options.isResetViewBeforeLoading()) {
				imageAware.setImageDrawable(null);
			}
			// 14. 构造ImageLoadingInfo对象
			ImageLoadingInfo imageLoadingInfo = new ImageLoadingInfo(uri, imageAware, targetSize, memoryCacheKey,
					options, listener, progressListener, engine.getLockForUri(uri));
			// 15. 构造LoadAndDisplayImageTask 对象
			LoadAndDisplayImageTask displayTask = new LoadAndDisplayImageTask(engine, imageLoadingInfo,
					defineHandler(options));
			// 16. 是否需要同步加载，如果是则直接运行任务，否则添加到线程池中。
			if (options.isSyncLoading()) {
				displayTask.run();
			} else {
				engine.submit(displayTask);
			}
		}
	}
```


上面代码注释已经将主体流程说明清楚了！

当我们没有初始化ImageLoader时，运行到 **checkConfiguration** 就会抛出异常！
```
private void checkConfiguration() {
		if (configuration == null) {
			throw new IllegalStateException(ERROR_NOT_INIT);
		}
	}
```
看一下 *注释3*  的代码块

```
targetSize = ImageSizeUtils.defineTargetSizeForView(imageAware, configuration.getMaxImageSize());
```

从ImageLoaderConfiguration对象中获取MaxImageSize。但是我们通常用的时候并没有配置这个参数。

其实在构造该对象时，框架会为没有赋值的参数赋上默认值

```
/** Builds configured {@link ImageLoaderConfiguration} object */
		public ImageLoaderConfiguration build() {
			initEmptyFieldsWithDefaultValues();
			return new ImageLoaderConfiguration(this);
		}
```

```
private void initEmptyFieldsWithDefaultValues() {
			if (taskExecutor == null) {
				// 1. 默认的executor实现类
				taskExecutor = DefaultConfigurationFactory
						.createExecutor(threadPoolSize, threadPriority, tasksProcessingType);
			} else {
				customExecutor = true;
			}
			if (taskExecutorForCachedImages == null) {
				// 2. 默认的缓存线程池
				taskExecutorForCachedImages = DefaultConfigurationFactory
						.createExecutor(threadPoolSize, threadPriority, tasksProcessingType);
			} else {
				customExecutorForCachedImages = true;
			}
			if (diskCache == null) {
				if (diskCacheFileNameGenerator == null) {
					// 3. 磁盘缓存默认的名称生成规则
					diskCacheFileNameGenerator = DefaultConfigurationFactory.createFileNameGenerator();
				}
				// 4. 默认的磁盘缓存策略
				diskCache = DefaultConfigurationFactory
						.createDiskCache(context, diskCacheFileNameGenerator, diskCacheSize, diskCacheFileCount);
			}
			if (memoryCache == null) {
				// 5. 创建默认的内存缓存策略
				memoryCache = DefaultConfigurationFactory.createMemoryCache(context, memoryCacheSize);
			}
			if (denyCacheImageMultipleSizesInMemory) {
				// 6. 如果不允许在内存中缓存同一图片的多种尺寸，则将内存缓存策略换成FuzzyKeyMemoryCache
				memoryCache = new FuzzyKeyMemoryCache(memoryCache, MemoryCacheUtils.createFuzzyKeyComparator());
			}
			if (downloader == null) {
				// 7. 创建默认的图片下载器
				downloader = DefaultConfigurationFactory.createImageDownloader(context);
			}
			if (decoder == null) {
				// 8. 创建默认的图片解码器
				decoder = DefaultConfigurationFactory.createImageDecoder(writeLogs);
			}
			if (defaultDisplayImageOptions == null) {
				// 9. 创建默认的图片显示option对象
				defaultDisplayImageOptions = DisplayImageOptions.createSimple();
			}
		}
```

而**DefaultConfigurationFactory**类正是一个提供默认配置的工厂类！！！

OK， 此时得到默认的getMaxImageSize() 值之后，就通过ImageSizeUtils工具类获取ImageSize对象！

---

### DefaultConfigurationFactory

现在来看下 <font color="red"> **DefaultConfigurationFactory** </font> 这个类

```
/** {@value} */
		public static final int DEFAULT_THREAD_POOL_SIZE = 3;
		/** {@value} */
		public static final int DEFAULT_THREAD_PRIORITY = Thread.NORM_PRIORITY - 2;
		/** {@value} */
		public static final QueueProcessingType DEFAULT_TASK_PROCESSING_TYPE = QueueProcessingType.FIFO;
```

```
/** Creates default implementation of task executor */
	public static Executor createExecutor(int threadPoolSize, int threadPriority,
			QueueProcessingType tasksProcessingType) {
		boolean lifo = tasksProcessingType == QueueProcessingType.LIFO;
		BlockingQueue<Runnable> taskQueue =
				lifo ? new LIFOLinkedBlockingDeque<Runnable>() : new LinkedBlockingQueue<Runnable>();
		return new ThreadPoolExecutor(threadPoolSize, threadPoolSize, 0L, TimeUnit.MILLISECONDS, taskQueue,
				createThreadFactory(threadPriority, "uil-pool-"));
	}
```

上面代码的用途是创建默认的线程池！

```
private static ThreadFactory createThreadFactory(int threadPriority, String threadNamePrefix) {
		return new DefaultThreadFactory(threadPriority, threadNamePrefix);
	}

	private static class DefaultThreadFactory implements ThreadFactory {

		private static final AtomicInteger poolNumber = new AtomicInteger(1);

		private final ThreadGroup group;
		private final AtomicInteger threadNumber = new AtomicInteger(1);
		private final String namePrefix;
		private final int threadPriority;

		DefaultThreadFactory(int threadPriority, String threadNamePrefix) {
			this.threadPriority = threadPriority;
			group = Thread.currentThread().getThreadGroup();
			
			// !!!
			namePrefix = threadNamePrefix + poolNumber.getAndIncrement() + "-thread-";
		}

		@Override
		public Thread newThread(Runnable r) {
			Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
			if (t.isDaemon()) t.setDaemon(false);
			t.setPriority(threadPriority); // 设置优先级
			return t;
		}
	}
```

在DefaultThreadFactory 这个静态内部类中可以看出，创建的线程的名称规则是【uil-pool-1-thread-】、【uil-pool-2-thread-】、【uil-pool-3-thread-】这种形式！！


```
	/** 默认的磁盘缓存图片名称规则 -- HashCodeFileNameGenerator */
	public static FileNameGenerator createFileNameGenerator() {
		return new HashCodeFileNameGenerator();
	}
```

```
/**
	 * 默认的磁盘缓存策略是 -- UnlimitedDiskCache
	 */
	public static DiskCache createDiskCache(Context context, FileNameGenerator diskCacheFileNameGenerator,
			long diskCacheSize, int diskCacheFileCount) {
		File reserveCacheDir = createReserveDiskCacheDir(context);
		if (diskCacheSize > 0 || diskCacheFileCount > 0) {
			File individualCacheDir = StorageUtils.getIndividualCacheDirectory(context);
			try {
				return new LruDiskCache(individualCacheDir, reserveCacheDir, diskCacheFileNameGenerator, diskCacheSize,
						diskCacheFileCount);
			} catch (IOException e) {
				L.e(e);
				// continue and create unlimited cache
			}
		}
		File cacheDir = StorageUtils.getCacheDirectory(context);
		return new UnlimitedDiskCache(cacheDir, reserveCacheDir, diskCacheFileNameGenerator);
	}
```


```
/**
	 * 默认的内存缓存策略 -- LruMemoryCache
	 */
	public static MemoryCache createMemoryCache(Context context, int memoryCacheSize) {
		if (memoryCacheSize == 0) {
			ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
			int memoryClass = am.getMemoryClass();
			if (hasHoneycomb() && isLargeHeap(context)) {
				memoryClass = getLargeMemoryClass(am);
			}
			memoryCacheSize = 1024 * 1024 * memoryClass / 8;
		}
		return new LruMemoryCache(memoryCacheSize);
	}
```

```
/** 默认的图片下载器 -- BaseImageDownloader */
	public static ImageDownloader createImageDownloader(Context context) {
		return new BaseImageDownloader(context);
	}
```

```
/** 默认的图片解码器 -- BaseImageDecoder */
	public static ImageDecoder createImageDecoder(boolean loggingEnabled) {
		return new BaseImageDecoder(loggingEnabled);
	}
```

```
/** 默认的图片显示方式 -- SimpleBitmapDisplayer */
	public static BitmapDisplayer createBitmapDisplayer() {
		return new SimpleBitmapDisplayer();
	}
```

---

现在还回过头来看上面的 **displayImage** 函数


```
// 4. 根据uri生成内存缓存的key
        String memoryCacheKey = MemoryCacheUtils.generateKey(uri, targetSize);
```

这里使用 **MemoryCacheUtils** 工具类来生成内存缓存的key

```
private static final String URI_AND_SIZE_SEPARATOR = "_";
	private static final String WIDTH_AND_HEIGHT_SEPARATOR = "x";

public static String generateKey(String imageUri, ImageSize targetSize) {
		return new StringBuilder(imageUri).append(URI_AND_SIZE_SEPARATOR).append(targetSize.getWidth()).append(WIDTH_AND_HEIGHT_SEPARATOR).append(targetSize.getHeight()).toString();
	}
```

所以，使用的过程中在logcat中就可以看到类似于下面的log输出

![这里写图片描述](http://img.blog.csdn.net/20170422170247630?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvY3JhenkxMjM1/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)


再接着往下看 ↓

```
engine.prepareDisplayTaskFor(imageAware, memoryCacheKey);
```
这里牵扯到另外一个重要的类 <font color="red"> **ImageLoaderEngine **</font>

它内部有一个Map对象，用来保存ImageAware的id和 cacheKey 的一个对应关系！

```
private final Map<Integer, String> cacheKeysForImageAwares = Collections
			.synchronizedMap(new HashMap<Integer, String>());
```

当uri为空时，调用的是下面的取消任务，也就是从map中移除！
```
void cancelDisplayTaskFor(ImageAware imageAware) {
		cacheKeysForImageAwares.remove(imageAware.getId());
	}
```

uri不为空时，往map中添加一条！！
```
void prepareDisplayTaskFor(ImageAware imageAware, String memoryCacheKey) {
		cacheKeysForImageAwares.put(imageAware.getId(), memoryCacheKey);
	}
```


在接着displayImage() 往下看！！

关键就在 <font color="red"> **ProcessAndDisplayImageTask** </font> 和 <font color="red"> **LoadAndDisplayImageTask** </font> 。

当图片不存在内存缓存中是，创建ProcessAndDisplayImageTask 对象，然后取决于是否同步加载，进而直接运行任务加载图片或者提交到线程池中！

当从内存缓存中找到了对应的bitmap，则创建 LoadAndDisplayImageTask 对象，然后也是取决于是否同步加载，进而直接运行任务显示图片或者提交到线程池中！

---

```
engine.submit(displayTask);
```

图片显示任务提交到线程池

ImageLoaderEngine.java
```
void submit(ProcessAndDisplayImageTask task) {
		initExecutorsIfNeed();
		taskExecutorForCachedImages.execute(task);
	}
```

---

图片加载任务提交到线程池

```
void submit(final LoadAndDisplayImageTask task) {
		taskDistributor.execute(new Runnable() {
			@Override
			public void run() {
				File image = configuration.diskCache.get(task.getLoadingUri());
				boolean isImageCachedOnDisk = image != null && image.exists();
				initExecutorsIfNeed();
				if (isImageCachedOnDisk) {
					taskExecutorForCachedImages.execute(task);
				} else {
					taskExecutor.execute(task);
				}
			}
		});
	}
```

可见最后都是运行的LoadAndDisplayImageTask 和 ProcessAndDisplayImageTask 这两个任务！！

这两个类的具体操作下篇blog再讲！！


---

### displayBitmap函数流程图

（图片看不清可打开新标签页查看）

![这里写图片描述](http://img.blog.csdn.net/20170422161702796?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvY3JhenkxMjM1/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

至此，图片加载并显示的整体流程就分析完毕了！

---
