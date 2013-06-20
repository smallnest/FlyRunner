package com.colobu.flyrunner;

import java.io.InputStream;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.GeoPoint;
import com.baidu.mapapi.MapActivity;
import com.baidu.mapapi.MapView;
import com.baidu.mapapi.Overlay;
import com.baidu.mapapi.Projection;
import com.colobu.flyrunner.utils.LocationUtil;
import com.colobu.flyrunner.utils.SharedPreferencesUtils;
/**
 * 百度地图会有些偏移，此Activity提供了校正的功能.
 * 当前根据GPS获得的地图位置用红色点显示，用户点击工具栏的"校正"按钮后，
 * 会以蓝色的点显示。以地图显示时将加上偏移量。
 *
 */
public class AdjustBaiduMapViewActivity extends MapActivity
{
	LocationClient mLocationClient = null;
	MapView mapView = null;
	AdjustOverlay adjustOverlay = null;
	
	GeoPoint gpsCurrentLocation = null;
	GeoPoint userCurrentLocation = null;
	
	Bitmap bmpAdjust = null;
	Bitmap bmpGpsLocation = null;
	Bitmap bmpMyLocation = null;
	
	boolean adjusted = false;
	/**
	 * 初始化环境
	 */
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_adjust_baidu_map_view);

		InputStream is = getResources().openRawResource(R.drawable.icon_adjust_location);    
		bmpAdjust = BitmapFactory.decodeStream(is);  
		is = getResources().openRawResource(R.drawable.icon_location_red);    
		bmpGpsLocation = BitmapFactory.decodeStream(is);  
		is = getResources().openRawResource(R.drawable.icon_location_green);    
		bmpMyLocation = BitmapFactory.decodeStream(is);  
		
		mLocationClient = new LocationClient(this);

		FlyRunnerApplication app = (FlyRunnerApplication) this.getApplication();
		if (app.mBMapMan == null)
		{
			app.mBMapMan = new BMapManager(getApplication());
			app.mBMapMan.init(app.mStrKey, new FlyRunnerApplication.MyGeneralListener());
		}
		
		app.mBMapMan.start();
		long iTime = System.nanoTime();
		super.initMapActivity(app.mBMapMan);
		iTime = System.nanoTime() - iTime;
		Log.d("AdjustBaiduMapViewActivity", "the init time is  " + iTime);
		mapView = (MapView) findViewById(R.id.bmapView);
		mapView.setBuiltInZoomControls(true);
		mapView.setDrawOverlayWhenZooming(true);
		mapView.setKeepScreenOn(true);
		mapView.getController().setZoom(mapView.getMaxZoomLevel()-1);


		adjustOverlay = new AdjustOverlay(this);
		mapView.getOverlays().add(adjustOverlay);
		
		
		track();
		
	}

	@Override
	protected void onPause()
	{
		mLocationClient.stop();
		FlyRunnerApplication app = (FlyRunnerApplication) this.getApplication();
		if (app.mBMapMan != null)
			app.mBMapMan.stop();
		super.onPause();
	}

	@Override
	protected void onResume()
	{
		mLocationClient.start();
		FlyRunnerApplication app = (FlyRunnerApplication) this.getApplication();
		app.mBMapMan.start();
		super.onResume();
		
		if (!LocationUtil.isGPSEnabled(this))
		{
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);
			dialog.setTitle(R.string.enable_gps);
			dialog.setMessage(R.string.gps_is_not_enabled);
			dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
	
				public void onClick(DialogInterface dialog, int which)
				{
					LocationUtil.startGps(AdjustBaiduMapViewActivity.this);				
				}
			});
			dialog.setNegativeButton("返回", new DialogInterface.OnClickListener() {
				
				public void onClick(DialogInterface dialog, int which)
				{
					AdjustBaiduMapViewActivity.this.finish();		
				}
			});
	
			dialog.create().show();
		}
	}

	@Override
	protected void onDestroy()
	{
		mLocationClient.stop();
		mLocationClient.unRegisterLocationListener(myListener);
		super.onDestroy();
	}

	@Override
	protected boolean isRouteDisplayed()
	{
		return false;
	}

	//开始启动记录路线，  监听GPS地理位置的改变
	private void track()
	{
		mLocationClient.registerLocationListener(myListener);
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true);
		option.setAddrType("detail");
		option.setCoorType("gcj02");
		option.setScanSpan(5000);
		option.setPoiNumber(0); // 最多返回POI个数
		option.setPoiDistance(1000); // poi查询距离
		option.setPoiExtraInfo(false); // 是否需要POI的电话和地址等详细信息
		option.setProdName("FlyRunner");
		mLocationClient.setLocOption(option);
		mLocationClient.start();
		mLocationClient.requestLocation();
	}

	BDLocationListener myListener = new BDLocationListener() {

		public void onReceiveLocation(BDLocation location)
		{
			//61 ： GPS定位结果
		    //62 ： 扫描整合定位依据失败。此时定位结果无效。
		    //63 ： 网络异常，没有成功向服务器发起请求。此时定位结果无效。
		    //65 ： 定位缓存的结果。
		    //161： 表示网络定位结果
		    //162~167： 服务端定位失败。 
			if (location != null && location.getLocType() == BDLocation.TypeGpsLocation)
			{
				if (gpsCurrentLocation == null)
				{
					gpsCurrentLocation = new GeoPoint((int) (location.getLatitude() * 1e6),	(int) (location.getLongitude() * 1e6));	
					AdjustBaiduMapViewActivity.this.mapView.getController().animateTo(gpsCurrentLocation);
				}
				else
					gpsCurrentLocation = new GeoPoint((int) (location.getLatitude() * 1e6),	(int) (location.getLongitude() * 1e6));
				
				mapView.invalidate();
			}			
		}

		public void onReceivePoi(BDLocation arg0)
		{

		}

	};

}

class AdjustOverlay extends Overlay
{
	private AdjustBaiduMapViewActivity mContext;
	
	public AdjustOverlay(AdjustBaiduMapViewActivity context)
	{
		super();
		this.mContext = context;
	}

	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow)
	{
		canvas.drawBitmap(mContext.bmpAdjust, 5,5, new Paint());
		
		Projection projection = mapView.getProjection();
		
		if (mContext.gpsCurrentLocation != null)
		{
			Point point = projection.toPixels(mContext.gpsCurrentLocation, null);			
			canvas.drawBitmap(mContext.bmpGpsLocation, point.x-mContext.bmpGpsLocation.getWidth()/2,point.y-mContext.bmpGpsLocation.getHeight(), new Paint());
		}
		if (mContext.userCurrentLocation != null)
		{
			Point point = projection.toPixels(mContext.userCurrentLocation, null);
			canvas.drawBitmap(mContext.bmpMyLocation, point.x-mContext.bmpMyLocation.getWidth()/2,point.y-mContext.bmpMyLocation.getHeight(), new Paint());
		}
		
		if (mContext.userCurrentLocation != null && mContext.gpsCurrentLocation != null && mContext.adjusted)
		{
			int offsetX = mContext.userCurrentLocation.getLongitudeE6() - mContext.gpsCurrentLocation.getLongitudeE6();
			int offsetY = mContext.userCurrentLocation.getLatitudeE6() - mContext.gpsCurrentLocation.getLatitudeE6();
			
			SharedPreferences settings = mContext.getSharedPreferences(SharedPreferencesUtils.PREFS_NAME, Context.MODE_PRIVATE);
			SharedPreferences.Editor editor = settings.edit();  
			editor.putInt(SharedPreferencesUtils.OFFSET_LONGITUDE, offsetX);
			editor.putInt(SharedPreferencesUtils.OFFSET_LATITUDEE6, offsetY);
			editor.commit();
			
			Point pointGps = projection.toPixels(mContext.gpsCurrentLocation, null);	
			Point pointMy = projection.toPixels(mContext.userCurrentLocation, null);
			
			Paint paint = new Paint();
			paint.setColor(Color.RED);
			paint.setStrokeWidth(5);
			canvas.drawLine(pointGps.x, pointGps.y, pointMy.x, pointMy.y, paint);
		}
		
		super.draw(canvas, mapView, shadow);
	}

	@Override
	public boolean onTap(GeoPoint arg0, MapView arg1)
	{
		Projection projection = mContext.mapView.getProjection();
		Point point = projection.toPixels(arg0, null);
		if (point.x>5 && point.y>5 && point.x<5+mContext.bmpAdjust.getWidth() && point.y<mContext.bmpAdjust.getHeight())
		{
			if (mContext.userCurrentLocation == null)
				Toast.makeText(mContext, "先在地图上点击你的当前位置", Toast.LENGTH_SHORT).show();
			else
			{
				mContext.adjusted = true;
				mContext.mapView.invalidate();
			}
		}
		else
		{
			mContext.userCurrentLocation = arg0;
			mContext.mapView.invalidate();
		}
		return true;
	}

}

