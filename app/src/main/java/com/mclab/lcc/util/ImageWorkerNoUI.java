package com.mclab.lcc.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.ImageView;

import com.mclab.lcc.model.ImageGetModel;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by Administrator on 2015/4/20.
 */
public class ImageWorkerNoUI {
    private static final String TAG="ImageWorkerNoUI";
    private static ImageWorkerNoUI s_instance=null;

    /**
     * 该管理其中最多能够同事存在的要下载的图片的数目。
     */
    private int MAX_DOWNLOAD_IMAGE_NUM=256;

    /**
     * 该管理器中的线程数目
     */
    private int RUNING_THREAD_NUM=1;

    private int MAX_IMAGE_THUMB_WIDTH_SIZE=96;
    private int MAX_IMAGE_THUMB_HEIGHT_SIZE=96;

    private int MAX_IMAGE_ORIGINAL_WIDTH_SIZE=512;
    private int MAX_IMAGE_ORIGINAL_HEIGHT_SIZE=512;

    private Context s_context=null;

    private DiskLruCache mOriginalImageDiskCache; //用来存放原图的缓存
    private ImageCache mThumbMemoryCache; //用来存放图片的thumb

    private boolean mExitTasksEarly = false;//是否提前退出下载的任务
    private boolean mExitThreadEarly = false;//是否提前结束下载的任务

    //是否在任务队列为空的时候，退出所有后台线程。该任务为空只是表示线程当前看到的队列的状态，即使后续队列的状态不考虑
    private boolean mExitWhenQueueEmpty=false;

    private Map<String,ArrayList<ImageViewToShow>> mImageIDs=null;
    private ArrayList<String> mListIDS=null;

    final ReentrantReadWriteLock mLockForExitFlag = new ReentrantReadWriteLock();
    final ReentrantReadWriteLock mLockForImageIDs = new ReentrantReadWriteLock();
    final ReentrantReadWriteLock mLockForDiskImageCache = new ReentrantReadWriteLock();
    final ReentrantReadWriteLock mLockForImageThumbCache=new ReentrantReadWriteLock();


    public ImageWorkerNoUI(ImageCache thumbCache,DiskLruCache originalImageCache)
    {
        mThumbMemoryCache=thumbCache;
        mOriginalImageDiskCache=originalImageCache;
        mImageIDs= Collections.synchronizedMap(new WeakHashMap<String, ArrayList<ImageViewToShow>>());
        mListIDS=new ArrayList<String>();

    }

    /**
     * 启动后台线程去运行
     */
    public void start()
    {
        setExitEarly(false,false);
        for(int i=0;i<RUNING_THREAD_NUM;++i)
        {
            new FetchImageThread(i).start();
        }
    }

//	public static ImageWorkerNoUI getInstance(Context ctx)
//	{
//		if(s_instance==null)
//		{
//			ImageCache thumbCache=ImageCacheUtil.getImageWorkerNoUIThumbCache(ctx);
//			DiskLruCache diskCache=ImageCacheUtil.getImageWorkerNoUIDiskCache(ctx);
//			s_instance=new ImageWorkerNoUI(thumbCache, diskCache);
//			s_instance.start();
//		}
//		return s_instance;
//	}

    /**
     * 设置当前ImageWorkerV2所需的两个缓存，thumbImage的缓存和originalImage的缓存
     * @param thumbCache
     * @param originalImageCache
     */
    private void setCache(ImageCache thumbCache,DiskLruCache originalImageCache)
    {
        mThumbMemoryCache=thumbCache;
        mOriginalImageDiskCache=originalImageCache;
    }

    /**
     * 清除掉thumbImage的缓存和originalImage的缓存，一般不会进行此操作
     */
    public void clearCache()
    {
        mThumbMemoryCache.clearCaches();
        mOriginalImageDiskCache.clearCache();
    }

    /**
     * 最大的线程数量为4个，因为太多会影响效率，所以当设置的n大于4个的时候，将设置线程数量为4;
     *该方法必须在start()方法之前调用
     * @param n 设置的最大线程数量
     */
    public void setThreadNum(int n)
    {
        if(n>2)
        {
            n=2;
        }
        else if(n<1)
        {
            n=1;
        }
        this.RUNING_THREAD_NUM=n;
    }

    /**
     * 设置下载队列的容量大小
     * @param num 队列中可以容纳的imageID的数量
     */
    public void setDownLoadingQueueCapacity(int num)
    {
        if(num<1)
        {
            return ;
        }
        else
        {
            this.MAX_DOWNLOAD_IMAGE_NUM=num;
        }
    }

    /**
     * 设置线程退出标志，即要求线程结束加载数据，只是设置exitTask为true（exitThread==false）的时候，线程是活动状态，但不进行任何下载操作，一旦遇到exitThread这个退出标志，所有与该ImageWorkerV2相关的threads都将退出（可能不是立即）;
     * 注意,如果某个线程已经结束了，那么再次设置这个exitThread为false的时候，并不会讲线程激活，切记！除非再次调用start()函数
     * @param exitTask 当为true的时候，表示要退出所有下载任务；当为false的时候，表示执行下载任务;
     * @param exitThread 当为true的时候，表示结束线程，为false的时候，表示不退出线程
     */
    public void setExitEarly(boolean exitTask,boolean exitThread)
    {
        mLockForExitFlag.writeLock().lock();
        this.mExitTasksEarly=exitTask;
        this.mExitThreadEarly=exitThread;
        mLockForExitFlag.writeLock().unlock();
    }

    /**
     * 只设置下载任务是否进行，是的话就停止下载，但是线程处于原始状态（原来为活动，则线程是活动的，反之亦然）
     * @param exitTask true的时候，表示停止下载操作
     */
    public void setExitTaskEarly(boolean exitTask)
    {
        mLockForExitFlag.writeLock().lock();
        this.mExitTasksEarly=exitTask;
        mLockForExitFlag.writeLock().unlock();
    }

    public void setExitWhenQueueEmpty(boolean exit)
    {
        this.mExitWhenQueueEmpty=exit;
    }


    /**
     * 设置是否强制结束所有下载的线程，当然所有下载任务也就停止了。（下载队列并没有清空，如果调用start，那么又会重新开始下载）
     * @param exitThread
     */
    public void setExitThreadEarly(boolean exitThread)
    {
        mLockForExitFlag.writeLock().lock();
        this.mExitThreadEarly=exitThread;
        mLockForExitFlag.writeLock().unlock();
    }


    /**
     * 将imageID添加到下载列表中，绑定上view(可以为null)，当view为null的时候，表示不需要显示到某个imageview上面。
     * @param imageID 将要下载的ImageID
     * @param view 将要显示在某个view上
     * @param showThumb 是否只是显示thumb image,当为false的时候，则显示原图（该原图是相对于thumbImage来的，有一定的缩放操作）
     */
    public void addImageIDForDownloading(String imageID,ImageView view,boolean showThumb)
    {
        Log.e(TAG, "start add image ID:" + imageID);
        int len=0;int len_1=0;
        mLockForImageIDs.writeLock().lock();

        if(mImageIDs.size()<MAX_DOWNLOAD_IMAGE_NUM)
        {
            if(mImageIDs.containsKey(imageID))
            {
                if(view!=null)
                {
                    mImageIDs.get(imageID).add(new ImageViewToShow(new WeakReference<ImageView>(view),showThumb));
                }
            }
            else
            {
                ArrayList<ImageViewToShow> list=new ArrayList<ImageViewToShow>();
                if(view!=null)
                {
                    list.add(new ImageViewToShow(new WeakReference<ImageView>(view),showThumb));
                }
                mImageIDs.put(imageID,list);
                mListIDS.add(imageID);
            }
        }
        len=mImageIDs.size();
        len_1=mListIDS.size();


        mLockForImageIDs.writeLock().unlock();
        Log.e(TAG,"end add image ID:"+imageID+"  total Len:"+len+"  len_1:"+len_1);
    }

    /**
     * 取消imageID所绑定到的view上的下载任务，该处的view不应该为null.(如果想要取消imageID的下载，应该调用cancelImageIDForDownloading(String imageID)这个方法)
     * @param imageID 将要下载的imageID
     * @param view 所绑定的image view.
     */
    public void cancelImageIDForDownloading(String imageID,ImageView view)
    {
        mLockForImageIDs.writeLock().lock();

        if(mImageIDs.containsKey(imageID))
        {
            Log.e(TAG,"cancelImageIDForDownloading canel imageID:"+imageID);
            ArrayList<ImageViewToShow> list=mImageIDs.get(imageID);
            int len=list.size();
            int index=-1;
            for(int i=0;i<len;++i)
            {
                WeakReference<ImageView> curView=list.get(i).m_curImageView;
                if(curView==null)
                {
                    continue;
                }
                else if(curView.get()==view)
                {
                    index=i;
                    break;
                }
            }
            if(index>=0)
            {
                list.remove(index);
                if(len==1)
                {
                    removeImageIDFromList(imageID);
                }
            }
        }

        mLockForImageIDs.writeLock().unlock();
    }

    private void removeImageIDFromList(String imageID)
    {
        int tmpIndex=-1;
        for(int i=0;i<mListIDS.size();++i)
        {
            String s=mListIDS.get(i);
            if(s.equals(imageID))
            {
                tmpIndex=i;
                break;
            }
        }
        if(tmpIndex>=0)
        {
            mListIDS.remove(tmpIndex);
        }
    }
    /**
     * 取消某个imageID的下载任务
     * @param imageID 在下载列表中的imageID
     */
    public void cancelImageIDForDownloading(String imageID)
    {
        mLockForImageIDs.writeLock().lock();
        if(mImageIDs.containsKey(imageID))
        {
            ArrayList<ImageViewToShow>  list=mImageIDs.get(imageID);
            list.clear();
            mImageIDs.remove(imageID);
            removeImageIDFromList(imageID);

        }
        mLockForImageIDs.writeLock().unlock();
    }

    /**
     * 取消所有要下载的imageID
     */
    public void cancelAllImageIDs()
    {
        mLockForImageIDs.writeLock().lock();
        mImageIDs.clear();
        mListIDS.clear();
        mLockForImageIDs.writeLock().unlock();
    }

    /**
     * 该方法会停止掉后台线程，并且清除掉任务列表，缓存等。
     * 如果不想清除掉缓存，可以直接disposeWithOutClearCache()方法
     */
    public void dispose()
    {
        setExitEarly(true,true);
        cancelAllImageIDs();
        clearCache();
    }

    /**
     * 该方法只停止后台线程运行，清除任务列表，不清除缓存
     */
    public void disposeWithOutClearCache()
    {
        setExitEarly(true,true);
        cancelAllImageIDs();
    }

    /**
     *
     * @param imageID ,the image id which will be used to get the bitmap
     * @return bitmap if the cache has the image in thumb cache or original disk cache,
     * 		   null else;
     *
     */
    public Bitmap getImageThumb(String imageID)
    {
        Log.e(TAG,"start in getImageThumb :"+imageID);
        Bitmap bitmap=null;
        mLockForImageThumbCache.writeLock().lock();
        bitmap=mThumbMemoryCache.getBitmapFromMemCache(imageID);
        if(bitmap==null)
        {
            bitmap=mThumbMemoryCache.getBitmapFromDiskCache(imageID);
        }
        mLockForImageThumbCache.writeLock().unlock();
        if(bitmap!=null)
        {
            return bitmap;
        }
        mLockForDiskImageCache.writeLock().lock();
        if(mOriginalImageDiskCache.containsKey(imageID))
        {
            String cacheFilePath=mOriginalImageDiskCache.createFilePath(imageID);
            if(cacheFilePath!=null)
            {
                File cacheFile=new File(cacheFilePath);
                if(cacheFile.exists())
                {
                    bitmap=ImageResizer.decodeSampledBitmapFromFile(cacheFilePath, MAX_IMAGE_THUMB_WIDTH_SIZE, MAX_IMAGE_THUMB_HEIGHT_SIZE);
                }
            }
        }
        mLockForDiskImageCache.writeLock().unlock();
        if(bitmap!=null)
        {
            mLockForImageThumbCache.writeLock().lock();
            mThumbMemoryCache.addBitmapToCache(imageID, bitmap);
            mLockForImageThumbCache.writeLock().unlock();
        }
        Log.e(TAG,"end in getImageThumb:"+imageID);
        return bitmap;

    }


    /**
     *
     * @param imageID  the image which will be used to get the original image(with some resize)
     * @return the original image , or null;
     */
    public Bitmap GetImageOriginal(String imageID)
    {
        Bitmap bitmap=null;
        mLockForDiskImageCache.writeLock().lock();
        if(mOriginalImageDiskCache.containsKey(imageID))
        {
//			String cacheFile=mOriginalImageDiskCache.createFilePath(imageID);
//			bitmap=ImageResizer.decodeSampledBitmapFromFile(cacheFile, MAX_IMAGE_ORIGINAL_WIDTH_SIZE, MAX_IMAGE_ORIGINAL_HEIGHT_SIZE);
            Log.e(TAG,"end in GetImageOriginal:"+imageID);
            String cacheFilePath=mOriginalImageDiskCache.createFilePath(imageID);
            if(cacheFilePath!=null)
            {
                File cacheFile=new File(cacheFilePath);
                if(cacheFile.exists())
                {
                    bitmap=ImageResizer.decodeSampledBitmapFromFile(cacheFilePath, MAX_IMAGE_ORIGINAL_WIDTH_SIZE, MAX_IMAGE_ORIGINAL_HEIGHT_SIZE);
                }
            }

        }
        mLockForDiskImageCache.writeLock().unlock();
        return bitmap;
    }




    /**
     *
     * @return one imageID from the imageID queue,or null;
     */
    private ImageToLoad getOneImageID()
    {
        int id=Thread.currentThread().hashCode();
//		Log.i(TAG,"thread "+ id +"start get one image ID:");
        int len=-1;
        ImageToLoad toLoad=null;
        mLockForImageIDs.writeLock().lock();


        len=mListIDS.size();
        if(len>0)
        {
            String imageID=mListIDS.remove(0);
            ArrayList<ImageViewToShow> toShows=mImageIDs.remove(imageID);
            toLoad=new ImageToLoad(imageID,toShows);

//			Log.i(TAG,"thread "+ id +"  not has:"+imageID+"  alen:"+len);
        }

        mLockForImageIDs.writeLock().unlock();

        return toLoad;
    }


    /**
     * 该函数仅由thread调用
     * @param toLoad 将要下载的对象
     * @return true，表示下载过程中遇到了mExitTasksEarly为true,即需要线程退出的标志。
     */
    private boolean downloadImage(ImageToLoad toLoad)
    {
        Log.i(TAG,"in downloadImage"+toLoad.m_curImageID+"");
        /**
         * 检查是否需要从网络获取数据，不需要则直接返回
         * 只要originalImageDiskCache没有包含这个imageID,则从网络获取数据。
         */
        mLockForExitFlag.readLock().lock();
        boolean exitTask=ImageWorkerNoUI.this.mExitTasksEarly;
        boolean exitThread=ImageWorkerNoUI.this.mExitThreadEarly;
        mLockForExitFlag.readLock().unlock();
        if(exitTask)
        {
            return false;
        }
        else if(exitThread)
        {
            return true;
        }

        //先查看缓存中是否有，有就直接设置图片,然后返回
        mLockForDiskImageCache.readLock().lock();
        Bitmap originalBitmap=null;
        String originalCacheFilePath=null;
        if (mOriginalImageDiskCache != null)
        {
            originalBitmap = mOriginalImageDiskCache.get(toLoad.m_curImageID);
            originalCacheFilePath=mOriginalImageDiskCache.createFilePath(toLoad.m_curImageID);
        }
        mLockForDiskImageCache.readLock().unlock();

        if(originalBitmap!=null)
        {
            Bitmap thumb=null;
            //检查缓存中是否存在thumb image
            mLockForImageThumbCache.writeLock().lock();
            thumb=mThumbMemoryCache.getBitmapFromDiskCache(toLoad.m_curImageID);
            if(thumb==null)
            {
                thumb=mThumbMemoryCache.getBitmapFromDiskCache(toLoad.m_curImageID);
            }
            mLockForImageThumbCache.writeLock().unlock();

            if(thumb==null) //缓存中没有thumb就生成thumbImage
            {
                thumb=ImageResizer.decodeSampledBitmapFromFile(originalCacheFilePath, MAX_IMAGE_THUMB_WIDTH_SIZE, MAX_IMAGE_THUMB_HEIGHT_SIZE);
                mThumbMemoryCache.addBitmapToCache(toLoad.m_curImageID, thumb);
            }
            setImageView(toLoad,thumb,originalBitmap);
            return false;
        }

        Log.i(TAG,"in downloadImage11111111111111"+toLoad.m_curImageID+"");


        mLockForExitFlag.readLock().lock();
        exitTask=ImageWorkerNoUI.this.mExitTasksEarly;
        exitThread=ImageWorkerNoUI.this.mExitThreadEarly;
        mLockForExitFlag.readLock().unlock();
        if(exitTask)
        {
            return false;
        }
        else if(exitThread)
        {
            return true;
        }

        mLockForDiskImageCache.readLock().lock();
        Log.i(TAG,"in downloadImage2222222222222"+toLoad.m_curImageID+"");


        //如果执行到这里，就表示应该从网络获取数据
        final File cacheFile = new File(mOriginalImageDiskCache.createFilePath(toLoad.m_curImageID));
        mLockForDiskImageCache.readLock().unlock();
        try {
            Log.i(TAG,"download the image "+toLoad.m_curImageID+"");
            ImageGetModel imageGetModel = new ImageGetModel();

            imageGetModel.getAndStoreImage(toLoad.m_curImageID,cacheFile);

            imageGetModel.Dispose();

        }
        catch (Exception e)
        {
            Log.e(TAG,"ImageGetModel get image:"+toLoad.m_curImageID+"  failed!");
            return false;
        }


        if(!cacheFile.exists())
        {
            return false;
        }

        //将orignal image加入到磁盘缓存
        Log.i(TAG,"in downloadImage3333333333333333333: "+toLoad.m_curImageID+"");
        final Bitmap originalImage=ImageResizer.decodeSampledBitmapFromFile(cacheFile.toString(), MAX_IMAGE_ORIGINAL_WIDTH_SIZE, MAX_IMAGE_ORIGINAL_HEIGHT_SIZE);
        if(originalImage!=null)
        {
            mLockForDiskImageCache.writeLock().lock();
            mOriginalImageDiskCache.put(toLoad.m_curImageID, originalImage);
            mLockForDiskImageCache.writeLock().unlock();
        }


        //将 thumb image加入到ImageCache中
        Log.i(TAG,"in downloadImage444444444444444444"+toLoad.m_curImageID+"");
        final Bitmap thumb=ImageResizer.decodeSampledBitmapFromFile(cacheFile.toString(), MAX_IMAGE_THUMB_WIDTH_SIZE, MAX_IMAGE_THUMB_HEIGHT_SIZE);
        if(thumb!=null)
        {
            mLockForImageThumbCache.writeLock().lock();
            mThumbMemoryCache.addBitmapToCache(toLoad.m_curImageID, thumb);
            mLockForImageThumbCache.writeLock().unlock();
        }

        mLockForExitFlag.readLock().lock();
        exitTask=ImageWorkerNoUI.this.mExitTasksEarly;
        exitThread=ImageWorkerNoUI.this.mExitThreadEarly;
        mLockForExitFlag.readLock().unlock();
        if(exitTask)
        {
            return false;
        }
        else if(exitThread)
        {
            return true;
        }

        setImageView(toLoad,thumb,originalImage);

        return false;

    }
    private void setImageView(ImageToLoad toLoad,final Bitmap thumb,final Bitmap originalImage)
    {
        //得到imageID对应的image views
        ArrayList<ImageViewToShow> imageViewsToShow = toLoad.m_relatedImageViews;
        int len = imageViewsToShow.size();
        if (len < 1) {
            return;
        }

        // 开始设置imageview的图片
        for (int i = 0; i < len; ++i) {
            ImageViewToShow toShow = imageViewsToShow.get(i);
            if (toShow.m_curImageView != null) {
                final ImageView curView = toShow.m_curImageView.get();
                if (curView == null) {
                    continue;
                } else {
                    Activity activity = (Activity) (curView.getContext());
                    boolean showThumb = toShow.m_showThumb;
                    if (showThumb) {
                        if (thumb != null) {

                            activity.runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    curView.setImageBitmap(thumb);
                                }
                            });

                        }
                    } else {
                        if (originalImage != null) {
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    curView.setImageBitmap(originalImage);
                                }
                            });

                        }
                    }
                }
            }
        }
    }
    private class ImageViewToShow
    {
        public boolean m_showThumb=true; //if true,show thumb ,else show original image
        public WeakReference<ImageView> m_curImageView=null;
        public ImageViewToShow(WeakReference<ImageView> imageView,boolean showThumb)
        {
            m_showThumb=showThumb;
            m_curImageView=imageView;
        }
    }
    private class ImageToLoad
    {
        public String m_curImageID=null;
        public ArrayList<ImageViewToShow> m_relatedImageViews=null;
        public ImageToLoad(String imageID,ArrayList<ImageViewToShow> imageViews)
        {
            m_curImageID=imageID;
            m_relatedImageViews=imageViews;
        }
    }
    class FetchImageThread extends Thread
    {
        private int mSleepMilsecond=2000;
        private int mThreadID=0;
        public FetchImageThread(int id)
        {
            mSleepMilsecond=2000;
            mThreadID=id;
        }
        public FetchImageThread(int milsecond,int id)
        {
            mSleepMilsecond=milsecond;
            mThreadID=id;
        }
        @Override
        public void run()
        {
            while(true)
            {
                Log.i(TAG,"in thread "+mThreadID+" to run:");


                mLockForExitFlag.readLock().lock();
                boolean exitTask=ImageWorkerNoUI.this.mExitTasksEarly;
                boolean exitThread=ImageWorkerNoUI.this.mExitThreadEarly;
                mLockForExitFlag.readLock().unlock();
                if(exitThread)
                {
                    break;
                }
                else if(exitTask)
                {
                    try
                    {
                        Thread.sleep(mSleepMilsecond);
                    } catch (InterruptedException e) {

                        e.printStackTrace();
                    }
                    continue;
                }
                else
                {
                    ImageToLoad toLoad=getOneImageID();
                    if(toLoad!=null)
                    {

                        boolean needExit=downloadImage(toLoad);
                        if(needExit)
                        {
                            break;
                        }
                    }
                    else
                    {
                        Log.e(TAG,"mExitWhenQueueEmpty :"+mExitWhenQueueEmpty);
                        if(mExitWhenQueueEmpty)
                        {
                            break;//当任务队列为空的时候，就退出。只管当前看到的任务列表是否为空。
                        }
                        try {
                            Thread.sleep(mSleepMilsecond);
                        } catch (InterruptedException e) {
                            Log.e(TAG, "thread " + mThreadID + " interrupted");
                            break;
                        }

                    }

                }
            }
        }

    }

}
