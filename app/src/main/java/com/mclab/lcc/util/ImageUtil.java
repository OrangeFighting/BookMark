package com.mclab.lcc.util;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import com.mclab.lcc.component.CustomToast;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;

public class ImageUtil {
    public static int MAX_WIDTH = 640;
    public static int MAX_HEIGHT=640;


    /**
     * Decode and sample down a bitmap from resources to the requested width and height.
     *
     * @param res The resources object containing the image data
     * @param resId The resource id of the image data
     * @param reqWidth The requested width of the resulting bitmap
     * @param reqHeight The requested height of the resulting bitmap
     * @return A bitmap sampled down from the original with the same aspect ratio and dimensions
     *         that are equal to or greater than the requested width and height
     */
    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
                                                         int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    /**
     * Decode and sample down a bitmap from a file to the requested width and height.
     *
     * @param filename The full path of the file to decode
     * @param reqWidth The requested width of the resulting bitmap
     * @param reqHeight The requested height of the resulting bitmap
     * @return A bitmap sampled down from the original with the same aspect ratio and dimensions
     *         that are equal to or greater than the requested width and height
     */
    public static synchronized Bitmap decodeSampledBitmapFromFile(String filename,
                                                                  int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filename, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filename, options);
    }

    /**
     * Calculate an inSampleSize for use in a {@link BitmapFactory.Options} object when decoding
     * bitmaps using the decode* methods from {@link BitmapFactory}. This implementation calculates
     * the closest inSampleSize that will result in the final decoded bitmap having a width and
     * height equal to or larger than the requested width and height. This implementation does not
     * ensure a power of 2 is returned for inSampleSize which can be faster when decoding but
     * results in a larger bitmap which isn't as useful for caching purposes.
     *
     * @param options An options object with out* params already populated (run through a decode*
     *            method with inJustDecodeBounds==true
     * @param reqWidth The requested width of the resulting bitmap
     * @param reqHeight The requested height of the resulting bitmap
     * @return The value to be used for inSampleSize
     */
    public static int calculateInSampleSize(BitmapFactory.Options options,
                                            int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            if (width > height) {
                inSampleSize = Math.round((float) height / (float) reqHeight);
            } else {
                inSampleSize = Math.round((float) width / (float) reqWidth);
            }

            // This offers some additional logic in case the image has a strange
            // aspect ratio. For example, a panorama may have a much larger
            // width than height. In these cases the total pixels might still
            // end up being too large to fit comfortably in memory, so we should
            // be more aggressive with sample down the image (=larger
            // inSampleSize).

            final float totalPixels = width * height;

            // Anything more than 2x the requested pixels we'll sample down
            // further.
            final float totalReqPixelsCap = reqWidth * reqHeight * 2;

            while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
                inSampleSize++;
            }
        }
        return inSampleSize;
    }
    /**
     * ��ȡһ��Բ�ε�ͼƬ
     * @param bitmap
     * @return
     */
    public static Bitmap getCircleImage(Bitmap bitmap)
    {
        Paint paint=new Paint();
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Bitmap output = Bitmap.createBitmap(width,height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        final Rect rect = new Rect(0,0,width,height);
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(Color.WHITE);
        canvas.drawCircle(width / 2, width / 2, width / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }
    /**
     * ��ȡһ������Բ�ǵ�ͼƬ
     * @param bitmap
     * @param roundPx��Խ����ôԲ��Ҳ��Խ��
     * @return
     */
    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap,int roundPx)
    {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        //�õ�����
        Canvas canvas = new Canvas(output);
        //���������Ľ�Բ��
        final int color = Color.RED;
        final Paint paint = new Paint();
        //�õ���ͼ����ͬ��С������  �ɹ�����ĸ�ֵ���������λ���Լ���С
        //��53.3  ��55.1
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        //ֵԽ��Ƕ�Խ����

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        //drawRoundRect�ĵ�2,3������һ���򻭵�����Բ��һ�ǣ������ֵ��ͬ������Բ��һ��
        canvas.drawRoundRect(rectF, roundPx,roundPx, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }


    public static boolean validatePictureSuffix(String suffix)
    {
        String sufTemp=suffix.toLowerCase(Locale.getDefault());
        if (sufTemp.equals(".jpg") ||sufTemp.equals(".png")||sufTemp.equals(".jpeg")||sufTemp.equals(".bmp"))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * ʹ��ϵͳ��������ȡͼƬ
     * @param activity ����ϵͳ���յ�activity
     * @param storeImagePath ������ս��ͼƬ��ͼƬ·��
     * @param requestCode activity������Ϣ��request code
     */
    public static void startActivityForTakePhoto(Activity activity,String storeImagePath,int requestCode)
    {
        String state = Environment.getExternalStorageState();
        if(state.equals(Environment.MEDIA_MOUNTED))
        {
            try {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                Uri uri =Uri.fromFile(new File(storeImagePath));
                intent.putExtra(MediaStore.Images.Media.ORIENTATION,0);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                activity.startActivityForResult(intent, requestCode);
            } catch (Exception e) {

                CustomToast.showToast(activity, "û���ҵ��洢Ŀ¼", Toast.LENGTH_LONG);
            }
        }
        else {
            CustomToast.showToast(activity, "û���ҵ�SD��", Toast.LENGTH_LONG);
        }
    }


    public static void startActivityForSelectPicture(Activity activity,int requestCode)
    {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        activity.startActivityForResult(intent,requestCode);
    }

    public static String getPicturePathByURI(Context context,Uri uri)
    {
        if(uri==null)
        {
            CustomToast.showToast(context, "û�л��ͼƬ·��", Toast.LENGTH_SHORT);
            return null;
        }
        String picPath = null;
        String[] pojo = { MediaStore.Images.Media.DATA };
        ContentResolver cr = context.getContentResolver();
        Cursor cursor=cr.query(uri, pojo, null, null, null);
        if(cursor==null)
        {
            if(uri.getScheme().compareTo("file")==0)
            {
                String tmpUri=uri.toString();
                String tmpPath=tmpUri.replace("file://", "");
                picPath=tmpPath;
            }
        }
        else
        {
            int colunm_index=-1;
            try{
                colunm_index= cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                cursor.moveToFirst();
                picPath= cursor.getString(colunm_index);
                cursor.close();
            }
            catch(IllegalArgumentException e)
            {
                Log.e("select_photo", "get MediaStore.Images.Media.DATA column index error!");
                CustomToast.showToast(context, "��ȡ��Դʧ��,ϵͳ����", Toast.LENGTH_SHORT);
            }
        }
        if(picPath==null)
        {
            return null;
        }
        int suffixIndex=picPath.lastIndexOf(".");
        if(suffixIndex<0)
        {
            Log.e("ellegal_pic_path","path:"+picPath);
            CustomToast.showToast(context, "�Ƿ���ͼƬ·����", Toast.LENGTH_SHORT);
            return null;
        }
        String suffix=picPath.substring(suffixIndex);
        if (!validatePictureSuffix(suffix))
        {
            picPath=null;
            CustomToast.showToast(context, "ͼƬ��ʽ������Ҫ�󣬱���Ϊ(.jpg,.jpeg,.png,.bmp)", Toast.LENGTH_SHORT);
        }
        return picPath;
    }

    public static void saveBitmapToJPGFile(Bitmap bitmap,String dstPath) throws IOException
    {
        File file = new File(dstPath);
        BufferedOutputStream bos = new BufferedOutputStream( new FileOutputStream(file));
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, bos);
        bos.flush();
        bos.close();
}
        }
