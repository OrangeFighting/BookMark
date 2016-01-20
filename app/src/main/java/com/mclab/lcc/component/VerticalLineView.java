package com.mclab.lcc.component;



import com.mclab.lcc.bookmark.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
/**
 * 占用一些位置
 * @author Administrator
 *
 */
public class VerticalLineView extends LinearLayout{

	public View mView=null;
	public VerticalLineView(Context context) {
		super(context);
		init(context);
	}
	public VerticalLineView(Context context,AttributeSet attrs)
	{
		super(context,attrs);
		init(context);
	}
	private void init(Context context)
	{
		LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.view_vertical_line_item_layout,this);	
		mView = (View)findViewById(R.id.view_vertical_line_view_vertical_line_item_layout);
	
	}

	public void setMyWidth(int width)
	{
		LayoutParams lp = new LayoutParams(width,LayoutParams.MATCH_PARENT);
		mView.setLayoutParams(lp);
	}
	public void setMyHeight(int height)
	{
		LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT,height);
		mView.setLayoutParams(lp);
	}
	public void setMySize(int width,int height)
	{
		LayoutParams lp = new LayoutParams(width,height);
		mView.setLayoutParams(lp);
	}
	public void setLineColor(int resid)
	{
		mView.setBackgroundResource(resid);
	}
}
