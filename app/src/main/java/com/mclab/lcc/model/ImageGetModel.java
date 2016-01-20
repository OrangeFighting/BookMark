package com.mclab.lcc.model;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.mclab.lcc.connection.MediaServerConfig;
import com.mclab.lcc.connection.SocketClient;

import java.io.File;

public class ImageGetModel
{
    private SocketClient mClient;

    private static String OpGetImage="1a";

    public ImageGetModel()
    {
        mClient =new SocketClient(MediaServerConfig.ServerIpAddress,
                MediaServerConfig.ServerPort);
    }

    @SuppressLint("NewApi")
    public Bitmap getImage(String id)throws Exception
    {
        byte[]data = mClient.Send(OpGetImage, Int2Bytes(Integer.decode(id)));
        return BitmapFactory.decodeByteArray(data, 0, data.length);
    }

    public Bitmap getImage(String id,String type)
    {
        return null;
    }

    /**
     *  用完记得调用Dispose()---自己控制
     */

    public void Dispose()
    {
        mClient.Close();
    }
    @SuppressLint("NewApi")
    public void getAndStoreImage(String imageId,File cacheFile) throws Exception
    {
        mClient.SendAndStoreRespones(OpGetImage,Int2Bytes(Integer.decode(imageId)), cacheFile);
    }

    private byte[] Int2Bytes(int data)
    {
        byte[]rData =new byte[4];
        int it=0xff;
        for(int i=0;i<4;i++)
        {
            rData[3-i]=(byte)(data&it);
            data>>=8;
        }
        return rData;
    }
}
