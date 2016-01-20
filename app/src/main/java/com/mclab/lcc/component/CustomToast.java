package com.mclab.lcc.component;

import android.content.Context;
import android.widget.Toast;


public class CustomToast {

	private static Context context = null;
	private static Toast toast;
	 public static void showToast(Context context,String msg,int duration)
	 {
			if(toast!=null && CustomToast.context==context)
			{
				toast.setText(msg);
				toast.setDuration(duration);
			}
			else
			{
				CustomToast.context=context;
				toast=Toast.makeText(context, msg,duration);
			}
			toast.show();
		}
	 public static void showNoNetWorkToast(Context context)
	 {
		 CustomToast.showToast(context, "未检测到网络,请连接网络后再试", Toast.LENGTH_SHORT);
	 }
}
