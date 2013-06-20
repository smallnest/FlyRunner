package com.colobu.flyrunner;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.GeoPoint;
import com.baidu.mapapi.MKRoutePlan;
import com.baidu.mapapi.MapActivity;
import com.baidu.mapapi.MapView;
import com.baidu.mapapi.Projection;
import com.colobu.flyrunner.db.HistoryRecordOpenHelper;
import com.colobu.flyrunner.utils.AlarmUtil;
import com.colobu.flyrunner.utils.DatetimeUtil;
import com.colobu.flyrunner.utils.LocationUtil;
import com.colobu.flyrunner.utils.SharedPreferencesUtils;
import com.tencent.mobwin.AdListener;
import com.tencent.mobwin.AdView;

public class BaiduMapViewActivity extends MapActivity implements OnClickListener,AdListener
{
	FlyRunnerApplication app;
	//地图
	LocationClient mLocationClient = null;
	MapView mapView = null;
	RunningRouteOverlay routeOverlay = null;
	ControlOverlay controlOverlay = null;
	AdView adView = null;
	
	//位置点
	List<GeoPoint> mGeoList = null;
	
	boolean isNeedAnimated = true; //手指拖动后应该设置为false,也就是不再自动居中
	boolean networkCentered = false; //网络方式取得的地点是否居中
	
	//最后位置居中按钮
	Bitmap bmpRoute = null;
	Bitmap bmpStart = null; //起始点
	Bitmap bmpEnd = null; //结束点
	
	//GPS偏离纠正
	int offsetX = 0;
	int offsetY = 0;
	
	
	
	private GpsService gpsService;
	boolean mIsBound = false; //服务是否绑定
	
	//上一次位置点取得时的时间
	GeoPoint lastPoint; //上一次时间点
	
	int monitorInterval = 1000; //秒
	int max_accuracy = 100;
	int max_speed = 20;
	boolean gps_filter = true;
	
	//步伐监控
	SensorManager mSensorManager;
    Sensor mSensor;
	StepDetector mStepDetector;
	
	
	boolean isStarted = false;
	boolean isMonitoring = false;
	
	//开始/暂停按钮
	Button btnStart;
	Button btnStop; //结束
	
	TextView tv_run_length;
	TextView tv_used_time;
	TextView tv_step_rate;
	
	int run_length; //已跑距离
	long used_time; //用时
	int step_rate; //步伐速率
	int steps; //总步数
	
	long startTime; //跑步开始时间
	long lastPointTime;
	
	int run_type = 0;
	int selected_run_length = 0;
	int selected_run_time = 0;
	boolean hasWarned = false; 
	
	/**
	 * Activity创建时
	 */
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_baidu_map_view);
		

		adView = (AdView) findViewById(R.id.adview);
		adView.setAdListener(this);
		

		InputStream is = getResources().openRawResource(R.drawable.icon_btn_route);    
		bmpRoute = BitmapFactory.decodeStream(is);  
		is = getResources().openRawResource(R.drawable.icon_location_start);    
		bmpStart = BitmapFactory.decodeStream(is);  
		is = getResources().openRawResource(R.drawable.icon_location_end);    
		bmpEnd = BitmapFactory.decodeStream(is);  
		
		btnStart = (Button) findViewById(R.id.btn_start);
		btnStart.setOnClickListener(this);
		btnStop = (Button) findViewById(R.id.btn_stop);
		btnStop.setOnClickListener(this);
		btnStop.setEnabled(false);
		
		tv_run_length = (TextView) findViewById(R.id.run_length);
		tv_used_time = (TextView) findViewById(R.id.used_time);
		tv_step_rate = (TextView) findViewById(R.id.step_rate);
		
		ImageView btnCenter = (ImageView) findViewById(R.id.btn_center);
		btnCenter.setOnClickListener(this);
		
		
		SharedPreferences settings = getSharedPreferences(SharedPreferencesUtils.PREFS_NAME, MODE_PRIVATE);
		offsetX = settings.getInt(SharedPreferencesUtils.OFFSET_LONGITUDE, 0);
		offsetY = settings.getInt(SharedPreferencesUtils.OFFSET_LATITUDEE6, 0);
	
		monitorInterval = Integer.valueOf(settings.getString(SharedPreferencesUtils.RECORD_INTERVAL, "1000"));
		max_accuracy = Integer.valueOf(settings.getString(SharedPreferencesUtils.MAX_ACCURACY, "100"));
		max_speed = Integer.valueOf(settings.getString(SharedPreferencesUtils.MAX_SPEED, "13"));
		gps_filter = settings.getBoolean(SharedPreferencesUtils.GPS_FILTER, true);
		
		mGeoList = new ArrayList<GeoPoint>();
		
		
		app = (FlyRunnerApplication) this.getApplication();
		
		if (app.mBMapMan == null)
		{
			app.mBMapMan = new BMapManager(getApplication());
			app.mBMapMan.init(app.mStrKey, new FlyRunnerApplication.MyGeneralListener());
		}
		
		mLocationClient = new LocationClient(this);
		
				
		// 如果使用地图SDK，请初始化地图Activity
		long iTime = System.nanoTime();
		initMapActivity(app.mBMapMan);
		iTime = System.nanoTime() - iTime;
		Log.d("BaiduMapViewActivity", "the init time is  " + iTime);
		
		mapView = (MapView) findViewById(R.id.bmapView);
		mapView.setBuiltInZoomControls(true);
		mapView.setDrawOverlayWhenZooming(true);
		mapView.getController().setZoom(mapView.getMaxZoomLevel()-1);
		//mapView.setKeepScreenOn(true);
				
		routeOverlay = new RunningRouteOverlay(this, mGeoList);
		mapView.getOverlays().add(routeOverlay);

		//controlOverlay = new ControlOverlay(this);
		//mapView.getOverlays().add(controlOverlay);
		
		mStepDetector = new StepDetector();
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
	    
	    
	    //if (savedInstanceState != null)
        //    mGeoList = savedInstanceState.getString("geoList");  
	    
	    Intent intent = getIntent();
        Bundle bundle=intent.getExtras();
        run_type = bundle.getInt("run_type", 0);
        selected_run_length = bundle.getInt("run_length", 0);
        selected_run_time = bundle.getInt("run_time", 0);
        
        AlarmUtil.sentAlarm = false;
	}

	public void onClick(View arg0)
	{
		switch (arg0.getId())
		{
		case R.id.btn_start:
			btnStop.setEnabled(true);
			if (btnStart.getText().equals("重新开始"))
			{
				isStarted = true;
				isMonitoring = true;
				btnStart.setText("暂停");
				restart();
			}
			else if (btnStart.getText().equals("暂停"))
			{
				btnStart.setText("开始");
				isMonitoring = false;
				pause();
			}
			else if (btnStart.getText().equals("开始"))
			{
				isStarted = true;
				isMonitoring = true;
				btnStart.setText("暂停");
				start();
			}
			break;
		case R.id.btn_stop:
			isStarted = false;
			isMonitoring = false;
			stop();
			btnStart.setText("重新开始");
			btnStop.setEnabled(false);
			btnStart.setEnabled(true);
			break;
		case R.id.btn_center:
			BaiduMapViewActivity.this.isNeedAnimated = true;
			BaiduMapViewActivity.this.networkCentered= false; 
			if (BaiduMapViewActivity.this.mGeoList.size()>0)
			{
				BaiduMapViewActivity.this.mapView.getController().animateTo(BaiduMapViewActivity.this.mGeoList.get(BaiduMapViewActivity.this.mGeoList.size()-1));
			}
			break;
		}
		
	}

	
	private void start()
	{		
		tv_run_length.setText("GPS搜星中...");
		
		track();
		
		mSensorManager.registerListener(mStepDetector, mSensor, SensorManager.SENSOR_DELAY_FASTEST);
		mStepDetector.addStepListener(mStepListener);
		if (startTime == 0)
			startTime = System.currentTimeMillis();
		
		lastPointTime = System.currentTimeMillis();
	}
	
	private void restart()
	{
		reset();
		track();
		mapView.invalidate();
		mSensorManager.registerListener(mStepDetector, mSensor, SensorManager.SENSOR_DELAY_FASTEST);
		mStepDetector.addStepListener(mStepListener);
		if (startTime == 0)
			startTime = System.currentTimeMillis();
		
		lastPointTime = System.currentTimeMillis();
	}
	
	private void pause()
	{
		used_time = used_time + System.currentTimeMillis() - lastPointTime;
		
		mLocationClient.unRegisterLocationListener(bdLocationListener);
		mLocationClient.stop();
		mSensorManager.unregisterListener(mStepDetector);
		mStepDetector.removeStepListener(mStepListener);
		
	}
	
	private void stop()
	{
		mLocationClient.unRegisterLocationListener(bdLocationListener);
		mLocationClient.stop();
		mSensorManager.unregisterListener(mStepDetector);
		mStepDetector.removeStepListener(mStepListener);
		showStopDialog();
	}
	
	private void showStopDialog()
	{
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		dialog.setTitle("停止跑步");
		dialog.setCancelable(false);
		if (mGeoList.size() > 1)
		{
			
			dialog.setMessage("停止记录\n距离:" + LocationUtil.convertMetre(run_length) + ",用时:" + LocationUtil.convertUsedTime(used_time));
			dialog.setPositiveButton("保存并返回", new DialogInterface.OnClickListener() {
	
				public void onClick(DialogInterface dialog, int which)
				{
					HistoryRecordOpenHelper.getInstance(BaiduMapViewActivity.this).insertHistoryRecord(DatetimeUtil.getDate(startTime), 
							startTime, System.currentTimeMillis(), run_length,	used_time, 
							BaiduMapViewActivity.this.steps, LocationUtil.convertFromGeoPointList(BaiduMapViewActivity.this.mGeoList),
							BaiduMapViewActivity.this.offsetX,BaiduMapViewActivity.this.offsetY);
					dialog.dismiss();
					BaiduMapViewActivity.this.finish();				
				}
			});
		}
		else
		{
			dialog.setMessage("停止记录\n您还未开始记录");
		}
		dialog.setNegativeButton("停止记录", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which)
			{
				dialog.dismiss();
			}
		});
		dialog.create().show();
	}
    
	private void reset()
	{
		AlarmUtil.sentAlarm = false;
		mGeoList.clear();
		isNeedAnimated = true; 
		networkCentered = false; 
		run_length = 0; //已跑距离
		used_time = 0; //用时
		step_rate = 0; //步伐速率
		steps = 0; //总步数
		
		startTime = 0; //跑步开始时间
		lastPointTime = 0;
		
		tv_run_length.setText("");
		tv_used_time.setText("");
		tv_step_rate.setText("");
		
		hasWarned = false;
	}
	
		
	//统计跑步速率
	StepListener mStepListener = new StepListener(){
		long lastStepTime = 0;
		int stepCount = 0;
		
		public synchronized void onStep()
		{
			if (mGeoList.size() == 0) //还未开始记录
				return;
			
			if (stepCount == 0)
			{
				lastStepTime = System.currentTimeMillis();
			}
			stepCount++;
			steps++;
			long i = (System.currentTimeMillis()-lastStepTime)/1000;
			if (i>= 10) //每10秒钟统计一次
			{
				step_rate = (int)(stepCount*60/i);
				stepCount = 0;
				lastStepTime = 0;
				
				tv_step_rate.setText( step_rate + " 步/分钟");
			}
		}
	};
	
	@Override
	protected void onPause()
	{
		mLocationClient.stop();
		mLocationClient.unRegisterLocationListener(bdLocationListener);
		
		mStepDetector.removeStepListener(mStepListener);
		
		//开始使用GpsService监听位置
		if (isMonitoring)
		{
			if (bindService(new Intent(this, GpsService.class), mConnection, Context.BIND_AUTO_CREATE))
				mIsBound = true;
		}
		
		super.onPause();
	}

	@Override
	protected void onResume()
	{
		if (!LocationUtil.isGPSEnabled(this))
		{
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);
			dialog.setTitle("开启GPS");
			dialog.setMessage("GPS还未开启，请开始GPS定位");
			dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
	
				public void onClick(DialogInterface dialog, int which)
				{
					LocationUtil.startGps(BaiduMapViewActivity.this);				
				}
			});
			dialog.setNegativeButton("返回", new DialogInterface.OnClickListener() {
				
				public void onClick(DialogInterface dialog, int which)
				{
					BaiduMapViewActivity.this.finish();
				}
			});
	
			dialog.create().show();
		}
		
		
		//重新获取偏移值
		SharedPreferences settings = getSharedPreferences(SharedPreferencesUtils.PREFS_NAME, MODE_PRIVATE);
		offsetX = settings.getInt(SharedPreferencesUtils.OFFSET_LONGITUDE, 0);
		offsetY = settings.getInt(SharedPreferencesUtils.OFFSET_LATITUDEE6, 0);
		monitorInterval = Integer.valueOf(settings.getString(SharedPreferencesUtils.RECORD_INTERVAL, "1000"));
		max_accuracy = Integer.valueOf(settings.getString(SharedPreferencesUtils.MAX_ACCURACY, "100"));
		max_speed = Integer.valueOf(settings.getString(SharedPreferencesUtils.MAX_SPEED, "13"));
		gps_filter = settings.getBoolean(SharedPreferencesUtils.GPS_FILTER, true);
		
		
		if (mIsBound) { //第一次启动不会进入此分支
			unbindGpsService();
	    } 
		

		app.mBMapMan.start();
		
		if (isStarted) //从待机恢复，而不是第一次进入
		{
			track();
			mStepDetector.addStepListener(mStepListener);
		}
		
		
		super.onResume();
	}

	private void unbindGpsService()
	{
		if (gpsService != null)
		{
			if (gpsService.geoPoints != null && gpsService.geoPoints.size()>0)
			{
				mGeoList.addAll(gpsService.geoPoints);
				gpsService.geoPoints.clear(); 
				BaiduMapViewActivity.this.run_length = gpsService.run_length;
				BaiduMapViewActivity.this.tv_run_length.setText(LocationUtil.convertMetre(BaiduMapViewActivity.this.run_length));
				BaiduMapViewActivity.this.used_time = gpsService.used_time;
				BaiduMapViewActivity.this.lastPointTime = gpsService.lastPointTime;
				BaiduMapViewActivity.this.lastPoint = gpsService.lastPoint;
				BaiduMapViewActivity.this.tv_used_time.setText(LocationUtil.convertUsedTime(BaiduMapViewActivity.this.used_time));
				
				BaiduMapViewActivity.this.steps = gpsService.steps;
				BaiduMapViewActivity.this.step_rate = gpsService.step_rate;
				BaiduMapViewActivity.this.tv_step_rate.setText(BaiduMapViewActivity.this.step_rate+ "步/分钟");
			}
			gpsService.stopGpsMonitor();
		}
		unbindService(mConnection);
		if (mGeoList.size()>0)
		{
			mapView.getController().animateTo(mGeoList.get(mGeoList.size()-1));
			mapView.invalidate();
		}
		mIsBound = false;
	}
	
	@Override  
    protected void onSaveInstanceState(Bundle outState) {  
        super.onSaveInstanceState(outState);  
        //outState.putStringArray("geoList", mGeoList);  
    }  
	
	@Override
	protected void onDestroy()
	{
		if (mSensorManager != null && mStepDetector != null)
		{
			mSensorManager.unregisterListener(mStepDetector);
			mStepDetector.removeStepListener(mStepListener);
		}
		
		if (mLocationClient != null)
		{
			if (bdLocationListener != null)
				mLocationClient.unRegisterLocationListener(bdLocationListener);
			mLocationClient.stop();
			mLocationClient = null;
		}
		
		if (mIsBound) {
			if (gpsService != null)
			{
				gpsService.stopGpsMonitor();
			}
	        unbindService(mConnection);
	        mIsBound = false;
	    } 

		if (app.mBMapMan != null)
			app.mBMapMan.stop();

		super.onDestroy();
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
		
		
		if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
		{ 
			// 竖屏
			RelativeLayout info_bar = (RelativeLayout) findViewById(R.id.info_bar);
			info_bar.setVisibility(View.VISIBLE);
			LinearLayout btn_control = (LinearLayout) findViewById(R.id.btn_control);
			btn_control.setVisibility(View.VISIBLE);
			adView.setVisibility(View.VISIBLE);
		}
		else
		{
			// 横屏
			RelativeLayout info_bar = (RelativeLayout) findViewById(R.id.info_bar);
			info_bar.setVisibility(View.GONE);
			LinearLayout btn_control = (LinearLayout) findViewById(R.id.btn_control);
			btn_control.setVisibility(View.GONE);
			adView.setVisibility(View.GONE);
		}
		
		mapView.invalidate();
		
		super.onConfigurationChanged(newConfig);
	}
	
	public void onAdClick()
	{
				
	}

	public void onReceiveAd()
	{
//		Toast.makeText(getApplicationContext(), "广告请求成功！",Toast.LENGTH_SHORT).show();
	}

	public void onReceiveFailed(int errorId)
	{
//		switch(errorId)
//		{
//		case AdListener.ERROR_CONNECTION_FAILED:
//			Toast.makeText(getApplicationContext(), "网络原因,广告请求失败！" + errorId,Toast.LENGTH_LONG).show();
//			break;
//		case AdListener.ERROR_GET_IMAGE_FAILED:
//			Toast.makeText(getApplicationContext(), "图片拉取错误,广告请求失败！" + errorId,Toast.LENGTH_LONG).show();
//			break;
//		case AdListener.ERROR_NO_AVAILABLE_ADS:
//			Toast.makeText(getApplicationContext(), "广告服务不可用,广告请求失败！" + errorId,Toast.LENGTH_LONG).show();
//			break;
//		case AdListener.ERROR_GIF_DECODE_FAILED:
//			Toast.makeText(getApplicationContext(), "GIF动画解码失败,广告请求失败！" + errorId,Toast.LENGTH_LONG).show();
//			break;
//		case AdListener.ERROR_SERVER_DATA_EXCEPTION:
//			Toast.makeText(getApplicationContext(), "服务器数据异常,广告请求失败！" + errorId,Toast.LENGTH_LONG).show();
//			break;
//		default:
//			Toast.makeText(getApplicationContext(), "未知原因,广告请求失败！" + errorId,Toast.LENGTH_LONG).show();
//			break;
//		}
	}
	
	private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service)
		{
	
			gpsService = ((GpsService.LocalBinder) service).getService();
			gpsService.offsetX = BaiduMapViewActivity.this.offsetX;
			gpsService.offsetY = BaiduMapViewActivity.this.offsetY;
						
			
			
			gpsService.run_type = BaiduMapViewActivity.this.run_type;
			gpsService.selected_run_length = BaiduMapViewActivity.this.selected_run_length;
			gpsService.selected_run_time = BaiduMapViewActivity.this.selected_run_time;
			gpsService.hasWarned = BaiduMapViewActivity.this.hasWarned;
			gpsService.run_length = BaiduMapViewActivity.this.run_length;
			gpsService.used_time = BaiduMapViewActivity.this.used_time;
			gpsService.lastPointTime = BaiduMapViewActivity.this.lastPointTime;
			
			gpsService.monitorInterval = BaiduMapViewActivity.this.monitorInterval;
			gpsService.max_speed = BaiduMapViewActivity.this.max_speed;
			gpsService.max_accuracy = BaiduMapViewActivity.this.max_accuracy;
			
			gpsService.steps = BaiduMapViewActivity.this.steps;
			gpsService.step_rate = BaiduMapViewActivity.this.step_rate;
			gpsService.gps_filter = BaiduMapViewActivity.this.gps_filter;
			gpsService.startGpsMonitor();
		}

		public void onServiceDisconnected(ComponentName className)
		{
			// This is called when the connection with the service has been
			// unexpectedly disconnected -- that is, its process crashed.
			// Because it is running in our same process, we should never
			// see this happen.
			gpsService = null;
			
		}
	};


	@Override
	protected boolean isRouteDisplayed()
	{
		return false;
	}

	//开始启动记录路线，  监听GPS地理位置的改变
	private void track()
	{	
		mLocationClient.registerLocationListener(bdLocationListener);
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true);
		//option.disableCache(true);
		option.setPriority(LocationClientOption.GpsFirst);
		option.setAddrType("detail");
		option.setCoorType("gcj02");
		option.setPoiNumber(0); // 最多返回POI个数
		option.setPoiDistance(1000); // poi查询距离
		option.setPoiExtraInfo(false); // 是否需要POI的电话和地址等详细信息
		option.setProdName("FlyRunner");
		option.setScanSpan(monitorInterval);
		mLocationClient.setLocOption(option);
		mLocationClient.start();
		mLocationClient.requestLocation();
	}

	BDLocationListener bdLocationListener = new BDLocationListener() {		
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
				GeoPoint point = new GeoPoint((int) (location.getLatitude() * 1e6) +  offsetY,
						(int) (location.getLongitude() * 1e6) + offsetX);
			
				if (mGeoList.size()>0)
				{
					boolean sameToLastPoint = lastPoint.getLatitudeE6() == point.getLatitudeE6() && lastPoint.getLongitudeE6() == point.getLongitudeE6();
					if (!sameToLastPoint)
					{
						
						double distance = LocationUtil.gps2m(lastPoint.getLatitudeE6()*1.0d/1e6,lastPoint.getLongitudeE6()*1.0d/1e6,
								point.getLatitudeE6()*1.0d/1e6,point.getLongitudeE6()*1.0d/1e6);
						
						if (!gps_filter || !location.hasSpeed() || (location.getSpeed() < max_speed)) //速度不会超过百米赛跑，否则可能GPS精度不够
						{
							if (!gps_filter || !location.hasRadius() || (location.getRadius()<= max_accuracy))
							{
								addNewPoint(point);
								run_length += distance;
								tv_run_length.setText(LocationUtil.convertMetre(run_length));
							}
						}	
					}
					
					
					used_time += (System.currentTimeMillis() - lastPointTime);
					lastPointTime = System.currentTimeMillis();
					
					tv_used_time.setText(LocationUtil.convertUsedTime(used_time));
					if (run_type == 2 && !hasWarned)
					{
						if (used_time > selected_run_time*60*1000L) //响铃警告
						{
							AlarmUtil.showAlarmSound(BaiduMapViewActivity.this, R.string.local_alarm_time, getText(R.string.local_alarm_time));
						}
						
					}
					else if (run_type == 1 && !hasWarned)
					{
						if (run_length > selected_run_length) //响铃警告
						{
							AlarmUtil.showAlarmSound(BaiduMapViewActivity.this, R.string.local_alarm_length, getText(R.string.local_alarm_length));
						}
						
					}
				}
				else //第一个点
				{
					addNewPoint(point);

					if (startTime == 0)
						startTime = System.currentTimeMillis();
					
					lastPointTime = System.currentTimeMillis();
					
					used_time = 0;
					tv_used_time.setText(LocationUtil.convertUsedTime(used_time));
					tv_run_length.setText(LocationUtil.convertMetre(run_length));
					
				}
				
				
				
			}
			else if (location != null && mGeoList.size() == 0 && isNeedAnimated && (!BaiduMapViewActivity.this.networkCentered)  && location.getLocType() == BDLocation.TypeNetWorkLocation)
			{
				GeoPoint point = new GeoPoint((int) (location.getLatitude() * 1e6),
						(int) (location.getLongitude() * 1e6));
				mapView.getController().animateTo(point);
				networkCentered = true;
				mapView.invalidate();
			}
		}

		private void addNewPoint(GeoPoint point) {
			if (isNeedAnimated)
				mapView.getController().animateTo(point);
			
			mGeoList.add(point);
			lastPoint = point;
			mapView.invalidate();
		}

		public void onReceivePoi(BDLocation arg0)
		{

		}

	};
}

