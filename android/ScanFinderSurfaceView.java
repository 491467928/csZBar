package org.cloudsky.cordovaPlugins;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class ScanFinderSurfaceView extends SurfaceView implements SurfaceHolder.Callback, Runnable {
	private SurfaceHolder holder;
	private Canvas canvas;
	private boolean isDrawing;
	private Paint maskPaint;
	private Paint linePaint;
	private Paint traAnglePaint;

	private int finderWith;
	private int finderHeight;
	private final int maskColor = Color.parseColor("#60000000");                          //蒙在摄像头上面区域的半透明颜色
	private final int triAngleColor = Color.parseColor("#FFFFFF");                        //边角的颜色
	private final int lineColor = Color.parseColor("#FF0000");                              //中间线的颜色

	private final int triAngleLength = dp2px(20);                                         //每个角的点距离
	private final int triAngleWidth = dp2px(4);                                           //每个角的点宽度
	private final int textMarinTop = dp2px(30);                                           //文字距离识别框的距离
	private int lineOffsetCount = 0;

	private String package_name;
	private Resources resources;
	private Bitmap scanLine;

	public ScanFinderSurfaceView(Context context) {
		super(context, null);
		holder = getHolder();
		holder.addCallback(this);
		setZOrderOnTop(true);
		getHolder().setFormat(PixelFormat.TRANSLUCENT);
		this.getKeepScreenOn();
		maskPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		maskPaint.setColor(maskColor);

		traAnglePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		traAnglePaint.setColor(triAngleColor);
		traAnglePaint.setStrokeWidth(triAngleWidth);
		traAnglePaint.setStyle(Paint.Style.STROKE);

		linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		linePaint.setColor(lineColor);
		scanLine = ((BitmapDrawable) (getResources().getDrawable(getResourceId("drawable/scanline")))).getBitmap();
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		isDrawing = true;
		new Thread(this).start();

	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		isDrawing = false;
	}

	@Override
	public void run() {
		while (isDrawing) {
			this.drawding();
		}
	}

	private void drawding() {
		try {
			canvas = holder.lockCanvas();
			if (canvas != null){
				canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
				int width = canvas.getWidth();
				int height = canvas.getHeight();
				int finderTop = (height - finderHeight) / 2;
				int finderLeft = (width - finderWith) / 2;
				int finderBottom = finderTop + finderHeight;
				int finderRight = finderLeft + finderWith;
				canvas.drawRect(0, 0, width, finderTop, maskPaint);
				canvas.drawRect(0, finderTop, finderLeft, finderBottom + 1, maskPaint);
				canvas.drawRect(finderRight + 1, finderTop, width, finderBottom + 1, maskPaint);
				canvas.drawRect(0, finderBottom + 1, width, height, maskPaint);

				Path leftTopPath = new Path();
				leftTopPath.moveTo(finderLeft + triAngleLength, finderTop + triAngleWidth / 2);
				leftTopPath.lineTo(finderLeft + triAngleWidth / 2, finderTop + triAngleWidth / 2);
				leftTopPath.lineTo(finderLeft + triAngleWidth / 2, finderTop + triAngleLength);
				canvas.drawPath(leftTopPath, traAnglePaint);

				Path rightTopPath = new Path();
				rightTopPath.moveTo(finderRight - triAngleLength, finderTop + triAngleWidth / 2);
				rightTopPath.lineTo(finderRight - triAngleWidth / 2, finderTop + triAngleWidth / 2);
				rightTopPath.lineTo(finderRight - triAngleWidth / 2, finderTop + triAngleLength);
				canvas.drawPath(rightTopPath, traAnglePaint);

				Path leftBottomPath = new Path();
				leftBottomPath.moveTo(finderLeft + triAngleWidth / 2, finderBottom - triAngleLength);
				leftBottomPath.lineTo(finderLeft + triAngleWidth / 2, finderBottom - triAngleWidth / 2);
				leftBottomPath.lineTo(finderLeft + triAngleLength, finderBottom - triAngleWidth / 2);
				canvas.drawPath(leftBottomPath, traAnglePaint);

				Path rightBottomPath = new Path();
				rightBottomPath.moveTo(finderRight - triAngleLength, finderBottom - triAngleWidth / 2);
				rightBottomPath.lineTo(finderRight - triAngleWidth / 2, finderBottom - triAngleWidth / 2);
				rightBottomPath.lineTo(finderRight - triAngleWidth / 2, finderBottom - triAngleLength);
				canvas.drawPath(rightBottomPath, traAnglePaint);

				//循环划线，从上到下
				if (lineOffsetCount > finderBottom - finderTop - dp2px(10)) {
					lineOffsetCount = 0;
				} else {
					lineOffsetCount = lineOffsetCount + 6;
					Rect lineRect = new Rect();
					lineRect.left = finderLeft;
					lineRect.top = finderTop + lineOffsetCount;
					lineRect.right = finderRight;
					lineRect.bottom = finderTop + dp2px(10) + lineOffsetCount;
					canvas.drawBitmap(scanLine, null, lineRect, linePaint);
				}
			}
		} catch (Exception ex) {
			Log.d("wxl", ex.getMessage());
		} finally {
			if (canvas != null)
				holder.unlockCanvasAndPost(canvas);
		}
	}

	private int dp2px(int dp) {
		float density = getContext().getResources().getDisplayMetrics().density;
		return (int) (dp * density + 0.5f);
	}

	private int getResourceId(String typeAndName) {
		if (package_name == null)
			package_name = this.getContext().getApplicationContext().getPackageName();
		if (resources == null) resources = this.getContext().getApplicationContext().getResources();
		return resources.getIdentifier(typeAndName, null, package_name);
	}

	public void setFinderRect(int width, int height) {
		finderWith = width;
		finderHeight = height;
	}
}
