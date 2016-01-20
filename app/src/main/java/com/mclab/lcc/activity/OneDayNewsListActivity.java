package com.mclab.lcc.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.fourmob.datetimepicker.date.DatePickerDialog;
import com.fourmob.datetimepicker.date.DatePickerDialog.OnDateSetListener;
import com.mclab.lcc.adapter.OneDayNewsAdapter;
import com.mclab.lcc.bookmark.BuildConfig;
import com.mclab.lcc.bookmark.R;
import com.mclab.lcc.component.CustomToast;
import com.mclab.lcc.component.Gallery;
import com.mclab.lcc.connection.ConnConfig;
import com.mclab.lcc.connection.MyHTTPConnection;
import com.mclab.lcc.entity.NewsEntity;
import com.mclab.lcc.model.BaseAsyncTaskModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class OneDayNewsListActivity extends FragmentActivity implements OnDateSetListener {
	private static final String TAG = "OneDayNewsListAcivity";
    public static final String DATEPICKER_TAG = "datepicker";
	private final Context m_ctx = this;
	private OneDayNewsAdapter m_oneDayNewsAdapter = null;
	private ListView m_listView = null;
	private List<NewsEntity> m_resultList =null;
    private int m_iCurrentPosition = 0;
    static final int MAXGALLERYVIEWLENGTH = 11;//必须是奇数
    private int m_iDayofyear;
    private DatePickerDialog datePickerDialog;
    private Gallery g;
    private Map<String,String> params;
    private Bundle m_tagsBundle;


    @Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
        //会根据在日历上选择的不同而发生变化的
        Calendar calendar=Calendar.getInstance();
        int month = calendar.get(Calendar.MONTH)+1;
        int date = calendar.get(Calendar.DAY_OF_MONTH);
        setDayofyear(convert2dayofyear(month,date));//只能在日历里使用setDayofyear

		setContentView(R.layout.view_news_list_layout);

        datePickerDialog = DatePickerDialog.newInstance(this, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), false);
        if (savedInstanceState != null) {
            DatePickerDialog dpd = (DatePickerDialog) getSupportFragmentManager().findFragmentByTag(DATEPICKER_TAG);
            if (dpd != null) {
                dpd.setOnDateSetListener(this);
            }

        }

        //Gallery g = (Gallery) findViewById(R.id.lv_view_news_list_gallery);

        m_oneDayNewsAdapter = new OneDayNewsAdapter(m_ctx);
        m_listView = (ListView) findViewById(R.id.lv_view_news_list);

        m_listView.setAdapter(m_oneDayNewsAdapter);
        m_listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                m_iCurrentPosition = position;
                if(m_resultList ==null|| m_resultList.isEmpty())
                {
                    return;
                }
                String url = m_resultList.get(position).getNewsURL();
                Map<String,String> param2= new HashMap<String,String>();
                param2.put("AddVisitCountByUrl",url);
                new NewsAsyncTaskModel().execute(param2);

                Intent webpageIntent = new Intent(Intent.ACTION_VIEW);//intent to webpage
                webpageIntent.setData(Uri.parse(url));
                startActivity(webpageIntent);

            }
        });

        params= new HashMap<String,String>();
        params.put("GetSimNewsAndTagsByDayofyear", String.valueOf(getDayofyear()));
        //接收参数param
        processOneDayNewsListView(params);

        g = (Gallery) findViewById(R.id.lv_view_news_list_gallery);
        g.setAdapter(new TipAdapter(m_ctx,month,date));
        g.setOnItemClickListener(new com.mclab.lcc.component.AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(com.mclab.lcc.component.AdapterView<?> parent, View view, int position, long id) {

                int correctedDayofyear = getDayofyear()+position-MAXGALLERYVIEWLENGTH/2;
                if(correctedDayofyear<=0)correctedDayofyear = correctedDayofyear+366;
                else if(correctedDayofyear>366)correctedDayofyear = correctedDayofyear-366;
                params.clear();
                params.put("GetSimNewsAndTagsByDayofyear", correctedDayofyear+"");
                processOneDayNewsListView(params);
            }
        });
	}

    @Override
    public void onDateSet(DatePickerDialog datePickerDialog, int year, int month, int day) {
        //重刷界面UI
        month +=1;
        setDayofyear(convert2dayofyear(month,day));
        g.setAdapter(new TipAdapter(m_ctx,month,day));
        params.clear();
        params.put("GetSimNewsAndTagsByDayofyear", getDayofyear()+"");
        processOneDayNewsListView(params);
       // Toast.makeText(OneDayNewsListActivity.this, "new date:" + year + "-" + month + "-" + day, Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        super.onCreateOptionsMenu(menu);

        MenuItem menu_tag = menu.findItem(R.id.item_tags);
        MenuItem menu_cal = menu.findItem(R.id.item_calendar);

        menu_cal.setIcon(R.drawable.calendar_fix);
        menu_tag.setIcon(R.drawable.tags_fix);
        //menu_cal.setIcon(R.drawable.calendar_fix);
        menu_tag.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        menu_cal.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        /*int id = item.getItemId();
        Log.e(TAG, "item " + item.getTitle().toString() + " id " + id);*/

        String itemTitle = item.getTitle().toString();
        switch(itemTitle)
        {
            case "tags":
                if(m_tagsBundle==null)break;
                Intent intent = new Intent(this,OneDayTagsActivity.class);
                intent.putExtra("tagsBundle",m_tagsBundle);
                startActivity(intent);
                break;
            case "calendar":
                datePickerDialog.setVibrate(isVibrate());
                datePickerDialog.setYearRange(2015, 2015);
                datePickerDialog.setCloseOnSingleTapDay(isCloseOnSingleTapDay());
                datePickerDialog.show(getSupportFragmentManager(), DATEPICKER_TAG);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean isVibrate() {
        return false;
    }
    private boolean isCloseOnSingleTapDay() {
        return false;
    }

    private int getDayofyear(){return m_iDayofyear;}
    private void setDayofyear(int dayofyear){this.m_iDayofyear = dayofyear;}
    public int convert2dayofyear(int month,int day)
    {
        int[] daysInMonth = { 0,31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30};
        int dayInYear = 0;
        int sumDaysInPreMonth=0;
        for (int i = 0; i < month; i++)
        {
            sumDaysInPreMonth += daysInMonth[i];
        }
        dayInYear = sumDaysInPreMonth + day;
        return dayInYear;
    }

    /**
     * Gets the set of tip values.
     *初始化view的内容，每次在日历上重新选择日期会重新加载一次
     * g.setAdapter(new TipAdapter(context,Integer.parseInt(String.valueOf(id))));
     *  context Application context. Cannot be null.
     * @return Array of tips.
     */
    private static int[] getTips(int curMon, int date) {
        int[] MonsOfYear = {31,29,31,30,31,30,31,31,30,31,30,31};
        int[] a_id = new int[MAXGALLERYVIEWLENGTH];
        a_id[MAXGALLERYVIEWLENGTH/2] = date;//向下取整
        int i ;//循环角标
        //此处需要有判断，根据月份不同来获取每月的天数，现在假设每月都有30天
        //可以用一个数组来维护一年的每个月的天数，然后在本函数传参数的时候将月份数curMon也传进来，方便查找该月的天数
        int monDay = MonsOfYear[curMon-1];//其实是个初始值，之后会变化的
        int nextMonDay = MonsOfYear[(curMon+12)%12];
        int lastMonDay = MonsOfYear[(curMon-2+12)%12];

        for(i=1;i<MAXGALLERYVIEWLENGTH/2+1;i++)
        {
            a_id[MAXGALLERYVIEWLENGTH/2+i] = (date+i)%monDay;

            if(0==a_id[MAXGALLERYVIEWLENGTH/2+i])
            {
                a_id[MAXGALLERYVIEWLENGTH/2+i]=monDay;//应该是nextMonDay
                monDay = nextMonDay;
            }
        }
        for(i=1;i<MAXGALLERYVIEWLENGTH/2+1;i++)
        {
            a_id[MAXGALLERYVIEWLENGTH/2-i] = (date-i+monDay)%monDay;
            if(0==a_id[MAXGALLERYVIEWLENGTH/2-i])
            {
                a_id[MAXGALLERYVIEWLENGTH/2-i]=lastMonDay;//应该是lastMonDay
                monDay = lastMonDay;
            }
        }
        return a_id;
    }

    /**
     * Adapter to use with the Gallery which provides the underlying dataset.
     */
    private static class TipAdapter extends BaseAdapter {

        private final Context mContext;

        /**
         * Set of tip percentages.
         */
        private int[] mTips;

        /**
         * Constructor. Creates the TipAdapter.
         *
         * @param context the context to use when inflating
         */
        public TipAdapter(Context context,int curMon,int id) {
            mContext = context;
            mTips = getTips(curMon,id);
        }

        @Override
        public int getCount() {
            return mTips.length;
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final TextView view;

            if (convertView == null) {
                LayoutInflater inflater =
                        (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = (TextView) inflater.inflate(R.layout.view_news_gallery_item, parent, false);
            } else {
                view = (TextView) convertView;
            }
            //放入当前日期以及前后5天共11天的日期数
            view.setText(Integer.toString(mTips[position]));


            return view;
        }
    }


    private void processOneDayNewsListView(final Map<String,String> param)
	{
		m_resultList = new ArrayList<NewsEntity>();
        NewsAsyncTaskModel.setContext(m_ctx);
        new NewsAsyncTaskModel().execute(param);

	}

	private class NewsAsyncTaskModel extends BaseAsyncTaskModel<List<NewsEntity>>{

		private static final String TAG = "NewsAsyncTaskModel";
		//private OneDayNewsAdapter odnAdapter = null;

		@Override
		protected List<NewsEntity> doInBackground(Map... param)   {

			if(BuildConfig.DEBUG)
			{
				Log.e(TAG, "doInBackground called");
			}
			ConnConfig.initConfig(context);
			MyHTTPConnection conn = (MyHTTPConnection) ConnConfig.getInstance();
			//设置服务器地址，默认使用127.0.0.1
			conn.setHostIP("115.159.91.105");
            //conn.setHostIP("192.168.2.104");
			//设置servlet文件名，默认使用index.jsp
			conn.setServletPage("CCNServer/CCNServlet");
            List<NewsEntity> entList = null;

			try
			{
                String jsonString = conn.sendMessage(param);
                if(jsonString==null)
                {
                    Log.e(TAG,"jsonString null!");
                }
                //Log.e(TAG,jsonString);
                JSONObject jsonObject = new JSONObject(jsonString);
                Iterator iter_key = jsonObject.keys();
                String key = null;
                while(iter_key.hasNext())
                {
                    key = iter_key.next().toString();
                }

                switch (key)
                {
                    case "1":
                        entList = ParseJsonStringForNewsData(jsonObject);
                        break;
                    case "2":
                        entList = ParseJsonStringForAddVisitCount(jsonObject);
                        break;
                }


            } catch(JSONException e)
			{
				if(BuildConfig.DEBUG)
				{
					Log.e(TAG, "获取json数据失败！ ");
				}
			} catch (Exception e) {
				if(BuildConfig.DEBUG)
				{
					Log.e(TAG, "向服务器发送列表请求失败！");
					e.printStackTrace();
				}
			}
			return entList;
		}

        private List<NewsEntity> ParseJsonStringForNewsData(JSONObject jsonObject)
        {
            //Log.e(TAG,"ParseJsonStringForNewsData is working!");
            List<NewsEntity> entList = new ArrayList<NewsEntity>();
            m_tagsBundle = new Bundle();
            try {
                JSONObject jsonObject1 = jsonObject.getJSONObject("1");//先获取一天的数据，UI全部能走通之后的可以一次获取多天数据
                JSONArray ja_SimNews = jsonObject1.getJSONArray("OneDaySimNews");
                JSONArray ja_Tags = jsonObject1.getJSONArray("OneDayWordsCount");

                for (int i = 0; i < ja_SimNews.length(); i++) {
                    JSONObject tmp = ja_SimNews.getJSONObject(i);
                    NewsEntity ent = new NewsEntity();
                    ent.setNewsURL(tmp.getString("1"));
                    ent.setNewsTitle(tmp.getString("2"));
                    ent.setNewsTime(tmp.getString("3"));
                    ent.setNewsImageURL(tmp.getString("4"));
                    ent.setVisitCount(tmp.getInt("5"));
                    entList.add(ent);
                }
                for(int i=0;i<ja_Tags.length();i++)
                {
                    JSONObject obj = ja_Tags.getJSONObject(i);
                    //将json数据解析成Bundle数据,方便intent传输
                    m_tagsBundle.putInt(obj.getString("Word"),Integer.parseInt(obj.getString("Count")));
                }
            }catch (JSONException e)
            {
                if(BuildConfig.DEBUG)
                {
                    Log.e(TAG, "获取json 1 数据失败！ ");
                }
            }
            return entList;
        }
        private List<NewsEntity> ParseJsonStringForAddVisitCount(JSONObject jsonObject)
        {

            try {
                String s_vc = jsonObject.getString("2");
                int i_vc = Integer.parseInt(s_vc);

                m_resultList.get(m_iCurrentPosition).setVisitCount(i_vc);

            }catch (JSONException e)
            {
                if(BuildConfig.DEBUG)
                {
                    Log.e(TAG, "获取json 2 数据失败！ ");
                }
            }
                return m_resultList;
        }
		@Override
		protected void onPostExecute(List<NewsEntity> result) {
			super.onPostExecute(result);
			if(result!=null&&(!result.isEmpty()))
			{
				if(!result.isEmpty())
				{
					m_resultList = result;
					m_oneDayNewsAdapter.setData(result);
				}
			}
			else
			{
				CustomToast.showToast(m_ctx, "这一天没有新闻(づ￣ 3￣づ)", 5000);
				//show no single news today
			}
		}
		
	}


}
