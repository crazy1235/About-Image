[TOC]

---

[**上篇文章**](http://blog.csdn.net/crazy1235/article/details/70312924)介绍 displayImage() 这个重要的方法时，重要涉及到 ProcessAndDisplayImageTask （处理和显示图片任务）和 LoadAndDisplayImageTask（加载显示图片任务） 这两个任务。

---

先来看下 ProcessAndDisplayImageTask

## ProcessAndDisplayImageTask 

displayImage() 中，当从内存缓存中读取到bitmap，然后需要后续处理的时候，创建了 ProcessAndDisplayImageTask 对象

```
ProcessAndDisplayImageTask displayTask = new ProcessAndDisplayImageTask(engine, bmp, imageLoadingInfo, defineHandler(options));
```

**defineHandler()**

```
private static Handler defineHandler(DisplayImageOptions options) {
		Handler handler = options.getHandler();
		if (options.isSyncLoading()) {
			handler = null;
		} else if (handler == null && Looper.myLooper() == Looper.getMainLooper()) {
			handler = new Handler();
		}
		return handler;
	}
```

从上面代码可以看出，当需要同步加载时，**handler = null** 。异步加载时，创建了一个  MainLooper 下的一个handler对象。

---

下面来看 ProcessAndDisplayImageTask 的类内部结构！

```
final class ProcessAndDisplayImageTask implements Runnable {

	private static final String LOG_POSTPROCESS_IMAGE = "PostProcess image before displaying [%s]";

	private final ImageLoaderEngine engine;
	private final Bitmap bitmap;
	private final ImageLoadingInfo imageLoadingInfo;
	private final Handler handler;

	public ProcessAndDisplayImageTask(ImageLoaderEngine engine, Bitmap bitmap, ImageLoadingInfo imageLoadingInfo,
			Handler handler) {
		this.engine = engine;
		this.bitmap = bitmap;
		this.imageLoadingInfo = imageLoadingInfo;
		this.handler = handler;
	}

	@Override
	public void run() {
		L.d(LOG_POSTPROCESS_IMAGE, imageLoadingInfo.memoryCacheKey);

		BitmapProcessor processor = imageLoadingInfo.options.getPostProcessor();
		Bitmap processedBitmap = processor.process(bitmap);
		
		// 创建了DisplayBitmapTask 对象
		DisplayBitmapTask displayBitmapTask = new DisplayBitmapTask(processedBitmap, imageLoadingInfo, engine,
				LoadedFrom.MEMORY_CACHE);
		LoadAndDisplayImageTask.runTask(displayBitmapTask, imageLoadingInfo.options.isSyncLoading(), handler, engine);
	}
}
```

重点看 **run()** 方法！

其实UIL库内部并没有 BitmapProecssor 的默认实现！所以displayImage() 方法内部直接走else分支，显示图片并回调成功函数！


![这里写图片描述](http://img.blog.csdn.net/20170423112000358?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvY3JhenkxMjM1/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

所以需要post处理的时候就要自己写实现方法来做！

我们分析的时候假定 options.shouldPostProcess() == true

在ProcessAndDisplayImageTask 的run方法内部，post处理完bitmap之后，创建了 <font  color="red"> **DisplayBitmapTask** </font> 对象！

```
final class DisplayBitmapTask implements Runnable {

	private static final String LOG_DISPLAY_IMAGE_IN_IMAGEAWARE = "Display image in ImageAware (loaded from %1$s) [%2$s]";
	private static final String LOG_TASK_CANCELLED_IMAGEAWARE_REUSED = "ImageAware is reused for another image. Task is cancelled. [%s]";
	private static final String LOG_TASK_CANCELLED_IMAGEAWARE_COLLECTED = "ImageAware was collected by GC. Task is cancelled. [%s]";

	private final Bitmap bitmap;
	private final String imageUri;
	private final ImageAware imageAware;
	private final String memoryCacheKey;
	private final BitmapDisplayer displayer;
	private final ImageLoadingListener listener;
	private final ImageLoaderEngine engine;
	private final LoadedFrom loadedFrom;

	public DisplayBitmapTask(Bitmap bitmap, ImageLoadingInfo imageLoadingInfo, ImageLoaderEngine engine,
			LoadedFrom loadedFrom) {
		this.bitmap = bitmap;
		imageUri = imageLoadingInfo.uri;
		imageAware = imageLoadingInfo.imageAware;
		memoryCacheKey = imageLoadingInfo.memoryCacheKey;
		displayer = imageLoadingInfo.options.getDisplayer();
		listener = imageLoadingInfo.listener;
		this.engine = engine;
		this.loadedFrom = loadedFrom;
	}

	@Override
	public void run() {
		if (imageAware.isCollected()) {
			L.d(LOG_TASK_CANCELLED_IMAGEAWARE_COLLECTED, memoryCacheKey);
			listener.onLoadingCancelled(imageUri, imageAware.getWrappedView());
		} else if (isViewWasReused()) {
			L.d(LOG_TASK_CANCELLED_IMAGEAWARE_REUSED, memoryCacheKey);
			listener.onLoadingCancelled(imageUri, imageAware.getWrappedView());
		} else {
			L.d(LOG_DISPLAY_IMAGE_IN_IMAGEAWARE, loadedFrom, memoryCacheKey);
			displayer.display(bitmap, imageAware, loadedFrom);
			engine.cancelDisplayTaskFor(imageAware);
			listener.onLoadingComplete(imageUri, imageAware.getWrappedView(), bitmap);
		}
	}

	/** Checks whether memory cache key (image URI) for current ImageAware is actual */
	private boolean isViewWasReused() {
		String currentCacheKey = engine.getLoadingUriForView(imageAware);
		return !memoryCacheKey.equals(currentCacheKey);
	}
}
```

该类也很简单！重点就在run()函数！就是经过判断回调加载成功、失败、去掉回调函数！

![这里写图片描述](http://img.blog.csdn.net/20170423114137275?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvY3JhenkxMjM1/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)


最后调用了LoadAndDisplayImageTask 函数的一个静态方法！  <font color="red"> **runTask()** </font>

```  
LoadAndDisplayImageTask.runTask(displayBitmapTask, imageLoadingInfo.options.isSyncLoading(), handler, engine);
```

```
static void runTask(Runnable r, boolean sync, Handler handler, ImageLoaderEngine engine) {
		if (sync) {
			r.run();
		} else if (handler == null) {
			engine.fireCallback(r);
		} else {
			handler.post(r);
		}
	}
```

当需要同步加载时直接运行 DisplayBitmapTask 的run方法，进而回调函数显示bitmap！

不需要同步加载时，调用了 **handler.post（handler）** 方法！由[**hanler机制**](http://blog.csdn.net/crazy1235/article/details/51707527)异步回调处理！

分析到这里，从缓存中读取bitmap进而显示的流程就分析完毕了！

接下来看 LoadAndDisplayImageTask

---


## LoadAndDisplayImageTask

该类的作用是加载并显示图片，实现了Runnable接口，用于从网络、文件系统或内存获取图片并解析，然后调用DisplayBitmapTask显示图片。

```
@Override
	public void run() {
		if (waitIfPaused()) return; // 
		if (delayIfNeed()) return; // 

		ReentrantLock loadFromUriLock = imageLoadingInfo.loadFromUriLock;
		L.d(LOG_START_DISPLAY_IMAGE_TASK, memoryCacheKey);
		if (loadFromUriLock.isLocked()) {
			L.d(LOG_WAITING_FOR_IMAGE_LOADED, memoryCacheKey);
		}

		loadFromUriLock.lock();
		Bitmap bmp;
		try {
			checkTaskNotActual(); // 

			// 1. 从内存缓存中读取bitmap
			bmp = configuration.memoryCache.get(memoryCacheKey);
			if (bmp == null || bmp.isRecycled()) {
				// 2. 内存缓存中读取失败时，调用tryLoadBitmap();
				bmp = tryLoadBitmap();
				if (bmp == null) return; // listener callback already was fired

				checkTaskNotActual(); // 
				checkTaskInterrupted(); // 

				// 3. 缓存之前的处理
				if (options.shouldPreProcess()) {
					L.d(LOG_PREPROCESS_IMAGE, memoryCacheKey);
					bmp = options.getPreProcessor().process(bmp);
					if (bmp == null) {
						L.e(ERROR_PRE_PROCESSOR_NULL, memoryCacheKey);
					}
				}
				
				// 4. 需要缓存到内存时保存
				if (bmp != null && options.isCacheInMemory()) {
					L.d(LOG_CACHE_IMAGE_IN_MEMORY, memoryCacheKey);
					configuration.memoryCache.put(memoryCacheKey, bmp);
				}
			} else {
				loadedFrom = LoadedFrom.MEMORY_CACHE;
				L.d(LOG_GET_IMAGE_FROM_MEMORY_CACHE_AFTER_WAITING, memoryCacheKey);
			}

			//  5. bitmap显示之前的处理
			if (bmp != null && options.shouldPostProcess()) {
				L.d(LOG_POSTPROCESS_IMAGE, memoryCacheKey);
				bmp = options.getPostProcessor().process(bmp);
				if (bmp == null) {
					L.e(ERROR_POST_PROCESSOR_NULL, memoryCacheKey);
				}
			}
			checkTaskNotActual(); //
			checkTaskInterrupted(); // 
		} catch (TaskCancelledException e) {
			fireCancelEvent();
			return;
		} finally {
			loadFromUriLock.unlock();
		}

		// 6. 同样是创建的DisplayBitmapTask 任务
		DisplayBitmapTask displayBitmapTask = new DisplayBitmapTask(bmp, imageLoadingInfo, engine, loadedFrom);
		
		//7. 调用该类内部的静态方法runTask 显示图片
		runTask(displayBitmapTask, syncLoading, handler, engine);
	}
```

先来说下下面几个函数

- waitIfPaused()

```
private boolean waitIfPaused() {
		AtomicBoolean pause = engine.getPause();
		if (pause.get()) {
			synchronized (engine.getPauseLock()) {
				if (pause.get()) {
					L.d(LOG_WAITING_FOR_RESUME, memoryCacheKey);
					try {
						engine.getPauseLock().wait();
					} catch (InterruptedException e) {
						L.e(LOG_TASK_INTERRUPTED, memoryCacheKey);
						return true;
					}
					L.d(LOG_RESUME_AFTER_PAUSE, memoryCacheKey);
				}
			}
		}
		return isTaskNotActual();
	}
```

真正运行任务之前先检查任务是否被暂停！

这里使用到了java的原子操作！如果被暂停则等待！


- delayIfNeed()

```
private boolean delayIfNeed() {
		if (options.shouldDelayBeforeLoading()) {
			try {
				Thread.sleep(options.getDelayBeforeLoading()); // !!!
			} catch (InterruptedException e) {
				return true;
			}
			return isTaskNotActual();
		}
		return false;
	}
```

如果需要延迟加载，则暂停线程等待！


- checkTaskNotActual()

用来检查view是否被回收或者重用！如果有则抛异常！退出

```
private void checkTaskNotActual() throws TaskCancelledException {
		checkViewCollected();
		checkViewReused();
	}
```

```
private void checkViewCollected() throws TaskCancelledException {
		if (isViewCollected()) {
			throw new TaskCancelledException(); // 抛出异常！！！
		}
	}
	private boolean isViewCollected() {
		if (imageAware.isCollected()) {
			L.d(LOG_TASK_CANCELLED_IMAGEAWARE_COLLECTED, memoryCacheKey);
			return true;
		}
		return false;
	}
```

```
private void checkViewReused() throws TaskCancelledException {
		if (isViewReused()) {
			throw new TaskCancelledException();
		}
	}

	// 当从imageAware获取到的缓存key与当前的key不一致时就判定为当前imageAware被重用了！
	private boolean isViewReused() {
		String currentCacheKey = engine.getLoadingUriForView(imageAware);
		boolean imageAwareWasReused = !memoryCacheKey.equals(currentCacheKey);
		if (imageAwareWasReused) {
			L.d(LOG_TASK_CANCELLED_IMAGEAWARE_REUSED, memoryCacheKey);
			return true;
		}
		return false;
	}
```


- checkTaskInterrupted()

```
private void checkTaskInterrupted() throws TaskCancelledException {
		if (isTaskInterrupted()) {
			throw new TaskCancelledException(); 
		}
	}
	private boolean isTaskInterrupted() {
		if (Thread.interrupted()) {
			L.d(LOG_TASK_INTERRUPTED, memoryCacheKey);
			return true;
		}
		return false;
	}
```

当线程被interrupted的时候也是抛出异常 -- **TaskCancelledException**！！

---

OK，此时在看LoadAndDisplayImageTask 的run 函数就比较明了了！

来看一张LoadAndDisplayImageTask的内部函数调用流程图！！

![这里写图片描述](http://img.blog.csdn.net/20170423121029124?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvY3JhenkxMjM1/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)


--- 

https://github.com/crazy1235/About-Image/tree/master/UniversalImageLoaderDemo

OVER！