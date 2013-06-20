package com.colobu.flyrunner;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;

import com.baidu.mapapi.GeoPoint;
import com.baidu.mapapi.MapView;
import com.baidu.mapapi.Overlay;
import com.baidu.mapapi.Projection;

class ControlOverlay extends Overlay
{
	private BaiduMapViewActivity mContext;

	public ControlOverlay(BaiduMapViewActivity context)
	{
		super();
		this.mContext = context;
	}

	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow)
	{

		// Projection接口用于屏幕像素坐标和经纬度坐标之间的变换
		canvas.drawBitmap(mContext.bmpRoute, 5,5, new Paint());

		super.draw(canvas, mapView, shadow);
	}

	@Override
	public boolean onTap(GeoPoint arg0, MapView arg1)
	{
		Projection projection = mContext.mapView.getProjection();
		Point point = projection.toPixels(arg0, null);
		if (point.x<5+mContext.bmpRoute.getWidth() && point.y<mContext.bmpRoute.getHeight() && point.x>5 && point.y>5)
		{
			mContext.isNeedAnimated = true;
			mContext.networkCentered= false; 
			if (mContext.mGeoList.size()>0)
			{
				mContext.mapView.getController().animateTo(mContext.mGeoList.get(mContext.mGeoList.size()-1));
			}
			
		}
		return true;
	}

}