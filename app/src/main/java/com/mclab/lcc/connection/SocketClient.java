package com.mclab.lcc.connection;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.Charset;

import android.R.integer;
import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import com.mclab.lcc.model.CallInfoServerMethodException;
import com.mclab.lcc.util.Utils;


public class SocketClient {
	private final String TAG="SocketClient";
	
	private Socket mClientSocket;
	private String mServerIpAddress;
	private int mServerPort;
	private Boolean is_socket_alive=false;
	
	private InputStream mInputStream=null;
	private OutputStream mOutputStream=null;
	
	private void init()
	{
		try {
			mClientSocket =new Socket(mServerIpAddress,mServerPort);
			//mClientSocket.se
			mClientSocket.setSoTimeout(5000);
			mInputStream =mClientSocket.getInputStream();
			mOutputStream= mClientSocket.getOutputStream();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		is_socket_alive=true;
	}
	
	
	public SocketClient(String ipAddress,int port)
	{
		mServerIpAddress=ipAddress;
		mServerPort=port;
		init();
	}
	
	private void CopyTo(byte[]destArray,int dIndex,byte[]sourceArray,int sIndex,int length)
	{
		for(int i=0;i<length;i++)
			destArray[dIndex+i]=sourceArray[sIndex+i];
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
	
	private int byte2Int(byte[]data)
	{
		int rData=0;
		for(int i=0;i<4;i++)
		{
			rData<<=8;
			rData|=data[i]>=0?data[i]:data[i]+256;
		}
		return rData;
	}
	
	@SuppressLint("NewApi")
	private byte[]constructDatagram(String op,byte[]data)
	{
		byte[]rdata = new byte[data.length+6];
		
		byte[]rOp=op.getBytes(Charset.forName("UTF-8")); // 有可能出现sdk api过低 不支持
		byte[]rLen = Int2Bytes(data.length);
		
		CopyTo(rdata, 0, rOp, 0, 2);
		CopyTo(rdata, 2, rLen, 0, 4);
		CopyTo(rdata, 6, data, 0, data.length);
		
		return rdata;
	}
	
	public byte[] Send(String op,byte[]data) throws Exception
	{
		if(!is_socket_alive)throw new Exception("socket is not connected!");
		if(op.length()!=2)throw new Exception("op's lengtb must be 2");
		
		//if(mClientSocket.)
		Log.i(TAG,"in Send:"+data);
		byte[]rs=null;
		mOutputStream.write(constructDatagram(op, data));
		mOutputStream.flush();
		Log.i(TAG,"in write finish:");
		int rsLen = GetIntFromStream(mInputStream);
		rs =new byte[rsLen];
		
		int tLen=0;
		while(tLen<rsLen){
			int iLen=mInputStream.read(rs, tLen, rsLen-tLen);
			if(iLen<0)
			{
				Close();
				throw new IOException("network error!");
			}
			
			tLen+=iLen;
		}
		int errorCode = byte2Int(rs);
		if(errorCode!=0)
		{
			throw new IOException("couldn't get image!");
		}
		Log.i(TAG,"result:"+String.valueOf(rs));
		return rs;
	}
	
	private int GetIntFromStream(InputStream stream) throws IOException
	{
		int off =0;
		byte[]data =new byte[4];
		while (off <4) {
			int tLen=stream.read(data, off, 4-off);
			if(tLen<0)throw new IOException("newwork error!");
			off+=tLen;
		}
		return byte2Int(data);
	}
	
	public void SendAndStoreRespones(String op,byte[]data,File resultCacheFile) throws Exception
	{
//		Log.i(TAG,"SendAndStoreRespones data length:"+data.length);
		if(!is_socket_alive)throw new Exception("socket is not connected!");
		if(op.length()!=2)throw new Exception("op's lengtb must be 2");
		
		BufferedOutputStream out=null;
		try
		{
			mOutputStream.write(constructDatagram(op, data));
			mOutputStream.flush();
			Log.i(TAG,"SendAndStoreRespones file name:"+resultCacheFile);
			
			int rsLen = GetIntFromStream(mInputStream);
			
			int errorCode =GetIntFromStream(mInputStream);
			if(errorCode!=0)
			{
				throw new CallInfoServerMethodException(errorCode, "获取图片失败！");
			}
			
//			Log.i(TAG,"SendAndStoreRespones in reading data 11111111111");
			out = new BufferedOutputStream(new FileOutputStream(resultCacheFile), Utils.IO_BUFFER_SIZE);
			
			rsLen-=4;
			Log.i(TAG,"SendAndStoreRespones in reading data length:"+rsLen);
			int tLen=0;
			int b;
	        while ( tLen<rsLen && ((b = mInputStream.read()) != -1) ) 
	        {
	              out.write(b);
	              ++tLen;
	        }
	        out.flush();
//	        Log.i(TAG,"SendAndStoreRespones in reading data 333333333");
		}catch(Exception e)
		{
			Log.e(TAG,"exception:"+e.getLocalizedMessage());
		}finally
		{
			 if (out != null) {
             try {
                  out.close();
              } catch (final IOException e) {
                  Log.e(TAG, "Error in downloadBitmap - " + e);
              }
          }
		}
	}
    public byte[] SendAndGetStream(String op, byte[] data) throws Exception
    {
//		Log.i(TAG,"SendAndStoreRespones data length:"+data.length);
        if(!is_socket_alive)throw new Exception("socket is not connected!");
        if(op.length()!=2)throw new Exception("op's lengtb must be 2");

        Bitmap resultBitmap=null;
        byte[]tbuffer =null;
        //BufferedOutputStream out=null;
        try
        {
            mOutputStream.write(constructDatagram(op, data));
            mOutputStream.flush();
            //Log.i(TAG,"SendAndStoreRespones file name:"+resultCacheFile);

            int rsLen = GetIntFromStream(mInputStream);

            int errorCode =GetIntFromStream(mInputStream);
            if(errorCode!=0)
            {
                throw new CallInfoServerMethodException(errorCode, "获取图片失败！");
            }

//			Log.i(TAG,"SendAndStoreRespones in reading data 11111111111");


            rsLen-=4;
            //ByteArrayOutputStream out = new ByteArrayOutputStream();
            Log.i(TAG,"SendAndStoreRespones in reading data length:"+rsLen);
            int tLen=0;
            int offset=0;
            tbuffer =new byte[rsLen];
            while (offset<rsLen && (tLen=mInputStream.read(tbuffer,offset,rsLen-offset))>0)
            {
                //out.write(b);
                offset+=tLen;
            }
            //out.flush();
//	        Log.i(TAG,"SendAndStoreRespones in reading data 333333333");


           // resultBitmap=  BitmapFactory.decodeByteArray(tbuffer,0,tbuffer.length);
           // tbuffer=null;

        }catch(Exception e)
        {
            Log.e(TAG,"exception:"+e.getLocalizedMessage());
        }finally
        {
            /*if (out != null) {
                try {
                    out.close();
                } catch (final IOException e) {
                    Log.e(TAG, "Error in downloadBitmap - " + e);
                }
            }*/
            //return BitmapFactory.decodeStream(mInputStream);
        }
        return  tbuffer;
    }

    public void Close()
	{
		if(mClientSocket!=null)
		{
			try {
				is_socket_alive=false;
				mInputStream.close();
				mOutputStream.close();
				mClientSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
