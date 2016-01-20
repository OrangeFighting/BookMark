package com.mclab.lcc.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.mclab.lcc.bookmark.R;
import com.mclab.lcc.component.CircleImageView;
import com.mclab.lcc.connection.MediaServerConfig;
import com.mclab.lcc.connection.SocketClient;
import com.mclab.lcc.entity.NewsEntity;
import com.mclab.lcc.model.BaseAsyncTaskModel;
import com.mclab.lcc.util.ImageResizer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*import com.mclab.lcc.util.ImageFetcher;
import com.mclab.lcc.util.ImageResizer;*/

public class OneDayNewsAdapter extends ArrayAdapter<NewsEntity>{

	private LayoutInflater mLayoutInflater;

    private static String OpGetImage="1a";
	private Context cxt;
    private SocketClient mClient ;
    private static final String TAG = OneDayNewsAdapter.class.getSimpleName();
    //重要的缓存操作，避免滑动list的时候重复下载图片
    private Map<String,Bitmap> m_ImageID2BitmapMap= new HashMap<>();

    private float IMAGE_HEIGHT = this.getContext().getResources().getDimension(R.dimen.view_head_photo_item_layout_head_photo_width);
    private float IMAGE_WIDTH = this.getContext().getResources().getDimension(R.dimen.view_head_photo_item_layout_head_photo_width);
    //test
    //int count=0;
	public OneDayNewsAdapter(Context context) {
		super(context,R.layout.view_news_item_layout);
		cxt = context;
		mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        //Log.e(TAG,"!!!!!!!!!!!!!!!!  "+count);
		// TODO Auto-generated constructor stub
	}

    /**
	 * public View inflate(int Resourece,ViewGroup root)
		作用：动态填充一个新的视图层次结构从指定的XML资源文件中
		reSource：View的layout的ID
		root： 生成的层次结构的根视图
		return 填充的层次结构的根视图。如果参数root提供了，那么root就是根视图；否则填充的XML文件的根就是根视图。
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		 ViewHolder vh;
         //count++;//以前遇到过的问题，getview要执行很多很多次，需要设定listview的高宽为fill_parent就可以了
         //Log.e(TAG,"!!!!!!!!!!!!!!!!  "+count);
		if(convertView==null)
		{
			convertView = mLayoutInflater.inflate(R.layout.view_news_item_layout, parent, false);
			vh = new ViewHolder();
			vh.oneDayNewsImage = (CircleImageView) convertView.findViewById(R.id.cv_head_photo_view_headphoto_and_introtext_item_layout);
			vh.oneDayNewsTitle = (TextView) convertView.findViewById(R.id.tv_head_photo_name_view_head_photo_intro_text_item_layout);
			vh.oneDayNewsTimeCount = (TextView) convertView.findViewById(R.id.tv_time_count_head_photo_and_intro_text_item_layout);
			vh.oneDayNewsVisitCount = (TextView) convertView.findViewById(R.id.tv_visit_count_head_photo_and_intro_text_item_layout);

		}
		else
		{
			vh = (ViewHolder) convertView.getTag();
		}
            NewsEntity ent = getItem(position);
            if(ent!=null)
            {
                String imageID = ent.getNewsImageURL();

            if(imageID.isEmpty())
            {
                Bitmap b = ImageResizer.decodeSampledBitmapFromResource
                    (this.getContext().getResources(),R.drawable.p1,R.dimen.view_head_photo_item_layout_head_photo_width,R.dimen.view_head_photo_item_layout_head_photo_width);
                vh.oneDayNewsImage.setImageBitmap(b);
            }
            else if(m_ImageID2BitmapMap.containsKey(imageID))
            {
                vh.oneDayNewsImage.setImageBitmap(m_ImageID2BitmapMap.get(imageID));
            }
            else {
                Map<String, String> param = new HashMap<>();
                param.put("imageID", ent.getNewsImageURL());
                new ImageDownloadAsyncTaskModel(vh).execute(param);
                //count++;
            }
			vh.oneDayNewsTitle.setText(ent.getNewsTitle());
			vh.oneDayNewsTimeCount.setText(ent.getNewsTime());
			vh.oneDayNewsVisitCount.setText(String.valueOf(ent.getVisitCount()));
		}
        convertView.setTag(vh);
		return convertView;
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
	public void setData(List<NewsEntity> data)
	{
		if(data!=null)
		{
			clear();
            m_ImageID2BitmapMap.clear();

			for(int i=0;i<data.size();i++) {
                add(data.get(i));
            }
		}
	}
	private class ViewHolder{
		private CircleImageView oneDayNewsImage;
		private TextView oneDayNewsTitle;
		private TextView oneDayNewsTimeCount;
		private TextView oneDayNewsVisitCount;
	}
    private  class ImageDownloadAsyncTaskModel extends BaseAsyncTaskModel<Bitmap> {
        private Map<String,String> param1 = new HashMap<>();
        protected  ViewHolder vh;

        public ImageDownloadAsyncTaskModel(ViewHolder viewHolder)
        {
            vh = viewHolder;
            //count++;
        }

        @Override
        protected void onPostExecute(Bitmap result) {

            if(result!=null)
            {
                vh.oneDayNewsImage.setImageBitmap(result);

                String imageId = param1.get("imageID");
                if(!m_ImageID2BitmapMap.containsKey(imageId))
                {
                    m_ImageID2BitmapMap.put(imageId,result);
                }
            }
        }

        @Override
        protected Bitmap doInBackground(Map<String,String>...param) {
            param1 = param[0];

            String imageID = param1.get("imageID");
            //Bitmap bitmap = null;
            byte[] bStream;
            Bitmap resizedBitmap = null;
            mClient = new SocketClient(MediaServerConfig.ServerIpAddress, MediaServerConfig.ServerPort);
            try {
                bStream = mClient.SendAndGetStream(OpGetImage, Int2Bytes(Integer.decode(imageID)));
                resizedBitmap = ImageResizer.decodeSampledBitmapFromByteArray(bStream,IMAGE_WIDTH,IMAGE_HEIGHT);

                mClient.Close();
            }catch (Exception e)
            {
                Log.e(TAG, " get image:" +imageID + "  failed!");
                //return false;
            }

            return resizedBitmap;
        }
    }

}
