package com.mclab.lcc.component;

import com.mclab.lcc.bookmark.R;
import com.mclab.lcc.util.ImageUtil;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

public class CircleImageView extends ImageView{

	private Paint paint = new Paint();
	public CircleImageView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public CircleImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	public CircleImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onDraw(Canvas canvas) {
		Drawable drawable = getDrawable();
		if(drawable!=null)
		{
			Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
            if(bitmap !=null) {
                int imSize = (int) getResources().getDimension(R.dimen.view_head_photo_item_layout_head_photo_width);
                bitmap = Bitmap.createScaledBitmap(bitmap, imSize, imSize, true);
                bitmap = ImageUtil.getCircleImage(bitmap);
                final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
                paint.reset();
                canvas.drawBitmap(bitmap, rect, rect, paint);
            }
		}
		else
		{
			super.onDraw(canvas);
		}
		
	}

}
