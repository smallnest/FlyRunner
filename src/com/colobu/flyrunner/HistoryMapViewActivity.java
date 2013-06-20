package com.colobu.flyrunner;

import java.io.InputStream;
import java.util.List;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;

import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.GeoPoint;
import com.baidu.mapapi.MapActivity;
import com.baidu.mapapi.MapView;
import com.baidu.mapapi.Overlay;
import com.baidu.mapapi.Projection;
import com.colobu.flyrunner.utils.LocationUtil;

public class HistoryMapViewActivity extends MapActivity
{
	FlyRunnerApplication app;
	MapView mapView = null;
	HistoryRouteOverlay routeOverlay = null;
	Bitmap bmpStart = null; //起始点
	Bitmap bmpEnd = null; //结束点
	
	// 位置点
	List<GeoPoint> mGeoList = null;

	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_history_map_view);
		InputStream is = getResources().openRawResource(R.drawable.icon_location_start);    
		bmpStart = BitmapFactory.decodeStream(is);  
		is = getResources().openRawResource(R.drawable.icon_location_end);    
		bmpEnd = BitmapFactory.decodeStream(is);  
		

		app = (FlyRunnerApplication) this.getApplication();
		if (app.mBMapMan == null)
		{
			app.mBMapMan = new BMapManager(getApplication());
			app.mBMapMan.init(app.mStrKey, new FlyRunnerApplication.MyGeneralListener());
		}
		app.mBMapMan.start();

		// 如果使用地图SDK，请初始化地图Activity
		long iTime = System.nanoTime();
		initMapActivity(app.mBMapMan);
		iTime = System.nanoTime() - iTime;
		Log.d("BaiduMapViewActivity", "the init time is  " + iTime);

		mapView = (MapView) findViewById(R.id.historyMapView);
		mapView.setBuiltInZoomControls(true);
		mapView.setDrawOverlayWhenZooming(true);
		mapView.getController().setZoom(mapView.getMaxZoomLevel() - 1);
		
		String route = (String)this.getIntent().getExtras().get("route");
		mGeoList = LocationUtil.convertToGeoPointList(route);
		mapView.getController().animateTo( mGeoList.get(mGeoList.size() - 1));
		routeOverlay = new HistoryRouteOverlay(this, mGeoList);
		mapView.getOverlays().add(routeOverlay);
		
		

	}

	@Override
	protected boolean isRouteDisplayed()
	{
		return false;
	}

	class HistoryRouteOverlay extends Overlay
	{
		private HistoryMapViewActivity mContext;
		private List<GeoPoint> mGeoList;

		public HistoryRouteOverlay(HistoryMapViewActivity context, List<GeoPoint> geoList)
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

			// 画起点
			if (size > 0)
			{
				geoPoint = mGeoList.get(0);
				point = projection.toPixels(geoPoint, null);
				canvas.drawBitmap(mContext.bmpStart, point.x - mContext.bmpStart.getWidth() / 2, point.y
						- mContext.bmpStart.getHeight(), new Paint());
			}

			// 画跑步路线
			if (size > 1)
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
				path.moveTo(point.x, point.y);

				for (int index = 1; index < size; index++)
				{ // 遍历mGeoList
					geoPoint = mGeoList.get(index); // 得到给定索引的item
					// 把经纬度变换到相对于MapView左上角的屏幕像素坐标
					point = projection.toPixels(geoPoint, null);
					path.lineTo(point.x, point.y);

				}

				canvas.drawPath(path, paintLine);
			}

			// 画终点
			if (size > 1)
			{
				geoPoint = mGeoList.get(mGeoList.size() - 1);
				point = projection.toPixels(geoPoint, null);
				canvas.drawBitmap(mContext.bmpEnd, point.x - mContext.bmpStart.getWidth() / 2, point.y
						- mContext.bmpStart.getHeight(), new Paint());
			}

			
			super.draw(canvas, mapView, shadow);
		}
	}

}
