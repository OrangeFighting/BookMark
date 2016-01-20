package com.mclab.lcc.util;

import android.content.Context;

import java.io.File;

public class ImageCacheUtil {
    public static final String IMAGE_WORKER_CACHE_NAME="ImageWorkerCache";
    public static final String IMAGE_WORKER_NO_UI_THUMB_CACHE_NAME="ImageWorkerNoUIThumbCache";
    public static final String IMAGE_WORKER_NO_UI_DISK_CACHE_DIR="ImageWorkerNoUIDiskCacheDir";

    private static ImageCache mImageWorkerCache=null;
    private static ImageCache mImageWorkerNoUIThumbCache=null;
    private static DiskLruCache mImageWorkerNoUIDiskCache=null;


    public static ImageCache getImageWorkerCache(Context ctx)
    {
        if(mImageWorkerCache==null)
        {
            ImageCache.ImageCacheParams cacheParams=new ImageCache.ImageCacheParams(IMAGE_WORKER_CACHE_NAME);
            cacheParams.memCacheSize = 1024 * 1024 * Utils.getMemoryClass(ctx) / 4; //ϵͳ�ڴ�1/8
            cacheParams.memoryCacheEnabled=true;
            cacheParams.diskCacheEnabled=true;
            cacheParams.diskCacheSize= 1024 * 1024 * 64; //64M

            mImageWorkerCache=new ImageCache(ctx,cacheParams);
        }
        return mImageWorkerCache;
    }
    public static ImageCache getImageWorkerNoUIThumbCache(Context ctx)
    {
        if(mImageWorkerNoUIThumbCache==null)
        {
            ImageCache.ImageCacheParams cacheParams=new ImageCache.ImageCacheParams(IMAGE_WORKER_NO_UI_THUMB_CACHE_NAME);
            cacheParams.memCacheSize = 1024 * 1024 * Utils.getMemoryClass(ctx) / 4; //ϵͳ�ڴ�1/8
            cacheParams.memoryCacheEnabled=true;
            cacheParams.diskCacheEnabled=true;
            cacheParams.diskCacheSize= 1024 * 1024 * 64; //64M�Ĵ��̻���
            mImageWorkerNoUIThumbCache=new ImageCache(ctx,cacheParams);
        }
        return mImageWorkerNoUIThumbCache;
    }

    public static DiskLruCache getImageWorkerNoUIDiskCache(Context ctx)
    {
        if(mImageWorkerNoUIDiskCache==null)
        {
            File diskCacheDir = DiskLruCache.getDiskCacheDir(ctx, IMAGE_WORKER_NO_UI_DISK_CACHE_DIR);
            mImageWorkerNoUIDiskCache=DiskLruCache.openCache(ctx, diskCacheDir, 1024 * 1024*100); //100M�Ĵ��̻���
        }
        return mImageWorkerNoUIDiskCache;
    }
}
