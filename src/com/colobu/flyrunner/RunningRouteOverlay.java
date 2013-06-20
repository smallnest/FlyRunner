package com.colobu.flyrunner;

import java.util.List;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.view.MotionEvent;

import com.baidu.mapapi.GeoPoint;
import com.baidu.mapapi.MapView;
import com.baidu.mapapi.Overlay;
import com.baidu.mapapi.Projection;

class RunningRouteOverlay extends Overlay
{
	private BaiduMapViewActivity mContext;
	private List<GeoPoint> mGeoList;

	public RunningRouteOverlay(BaiduMapViewActivity context, List<GeoPoint> geoList)
	{
		super();
		this.mContext = context;
		this.mGeoList = geoList;

	}

	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow)
	{

		// Projection接口用于屏幕像素坐标和经纬度坐标之间的变换
		Projection projection = mapView.getProjection();
		int size = mGeoList.size();
		
		GeoPoint geoPoint = null;
		Point point = null;
		
		//画起点
		if (size>0)
		{
			geoPoint = mGeoList.get(0);
			point = projection.toPixels(geoPoint, null);
			canvas.drawBitmap(mContext.bmpStart, point.x-mContext.bmpStart.getWidth()/2,point.y-mContext.bmpStart.getHeight(), new Paint());
		}
		
		//画跑步路线
		if (size>1)
		{
			Paint paintLine = new Paint();
			paintLine.setAntiAlias(true);
			paintLine.setDither(true);
			paintLine.setStrokeJoin(Paint.Join.ROUND);
			paintLine.setStrokeCap(Paint.Cap.ROUND);
			paintLine.setColor(Color.BLUE);
			paintLine.setStrokeWidth(8);
			paintLine.setStyle(Paint.Style.STROKE);
			
			geoPoint = mGeoList.get(0);
			point = projection.toPixels(geoPoint, null);
			Path path = new Path();
			path.moveTo(point.x,point.y);
			
			for (int index = 1; index < size; index++)
			{ // 遍历mGeoList
				geoPoint = mGeoList.get(index); // 得到给定索引的item
				// 把经纬度变换到相对于MapView左上角的屏幕像素坐标
				point = projection.toPixels(geoPoint, null);
				path.lineTo(point.x, point.y);
				
			}
			
			
			canvas.drawPath(path, paintLine);
		}
		
		//画终点
		if (size>1)
		{
			geoPoint = mGeoList.get(mGeoList.size()-1);
			point = projection.toPixels(geoPoint, null);		
			canvas.drawBitmap(mContext.bmpEnd, point.x-mContext.bmpStart.getWidth()/2,point.y-mContext.bmpStart.getHeight(), new Paint());
		}
		
		super.draw(canvas, mapView, shadow);
	}

	@Override
	public boolean onTouchEvent(MotionEvent arg0, MapView arg1)
	{
		mContext.isNeedAnimated = false;
		return super.onTouchEvent(arg0, arg1);
	}

}