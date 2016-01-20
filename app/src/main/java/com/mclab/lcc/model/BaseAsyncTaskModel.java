package com.mclab.lcc.model;

import java.util.List;
import java.util.Map;

import com.mclab.lcc.component.CustomToast;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class BaseAsyncTaskModel<T> extends AsyncTask<Map<String,String>,Integer,T>{

	protected static Context context = null;
	private static final String TAG = "BaseAsyncTaskModel";
	public static void setContext(Context cxt)
    {
        context = cxt;
    }
	@Override
	protected void onPreExecute() {
		//CustomToast.showToast(context, "onPreExecute is running", 1000);
		Log.e(TAG, "onPreExecute called");
		super.onPreExecute();
	}

	@Override
	protected void onPostExecute(T result) {
		//CustomToast.showToast(context, "onPostExecute is running", 1000);
		Log.e(TAG, "onPostExecute called");
		super.onPostExecute(result);
	}


	@Override
	protected void onProgressUpdate(Integer... values) {
		//CustomToast.showToast(context, "onProgressUpdate is running", 1000);
		Log.e(TAG, "onProgressUpdate called");
		super.onProgressUpdate(values);
	}

	@Override
	protected T doInBackground(Map<String, String>... arg0) {
		// TODO Auto-generated method stub
		return null;
	}


}
