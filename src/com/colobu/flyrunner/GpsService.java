package com.colobu.flyrunner;

import java.util.ArrayList;
import java.util.List;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.GeoPoint;
import com.colobu.flyrunner.utils.AlarmUtil;
import com.colobu.flyrunner.utils.LocationUtil;

public class GpsService extends Service
{
	private NotificationManager mNM;
	private int NOTIFICATION = R.string.local_gps_service;
	private final IBinder mBinder = new LocalBinder();

	LocationClient mLocationClient = null;
	SensorManager mSensorManager;
    Sensor mSensor;
	StepDetector mStepDetector;
	
	public List<GeoPoint> geoPoints = new ArrayList<GeoPoint>();
	public int run_type = 0;
	public int selected_run_length = 0;
	public int selected_run_time = 0;
	public boolean hasWarned = false; 
	public int run_length; //已跑距离
	public long used_time; //用时
	public long lastPointTime;
	
	public int step_rate; //步伐速率
	public int steps; //总步数

	GeoPoint lastPoint;
	
	public int offsetX = 0;
	public int offsetY = 0;
	public int monitorInterval = 1000; //秒
	public int max_accuracy = 20;
	public int max_speed = 20;
	boolean gps_filter = true;
	
	public class LocalBinder extends Binder {
		GpsService getService() {
            return GpsService.this;
        }
    }
	

	
	@Override
	public void onCreate()
	{
		  
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
				GeoPoint point = new GeoPoint((int) (location.getLatitude() * 1e6) +  GpsService.this.offsetY,
						(int) (location.getLongitude() * 1e6) + GpsService.this.offsetX);
				
				
				if (geoPoints.size()>0)
				{
					
					if (lastPoint.getLatitudeE6() != point.getLatitudeE6() || lastPoint.getLongitudeE6() != point.getLongitudeE6())
					{
						double distance = LocationUtil.gps2m(lastPoint.getLatitudeE6()*1.0d/1e6,lastPoint.getLongitudeE6()*1.0d/1e6,
								point.getLatitudeE6()*1.0d/1e6,point.getLongitudeE6()*1.0d/1e6);
						if (!gps_filter || !location.hasSpeed() || (location.getSpeed() < max_speed)) //速度不会超过百米赛跑，否则可能GPS精度不够
						{
							if (!gps_filter || !location.hasRadius() || (location.hasRadius() && location.getRadius()<= max_accuracy))
							{
								lastPoint = point;
								geoPoints.add(point);
							}
						}
						
						run_length += distance;
					}
					
					
					used_time += (System.currentTimeMillis() - lastPointTime);
					lastPointTime = System.currentTimeMillis();
					
					if (run_type == 2 && !hasWarned)
					{
						if (used_time > selected_run_time*60*1000L) //响铃警告
						{
							AlarmUtil.showAlarmSound(GpsService.this, R.string.local_alarm_time, getText(R.string.local_alarm_time));
						}
						
					}
					else if (run_type == 1 && !hasWarned)
					{
						if (run_length > selected_run_length) //响铃警告
						{
							AlarmUtil.showAlarmSound(GpsService.this, R.string.local_alarm_length, getText(R.string.local_alarm_length));
						}
						
					}
				}
				else
				{
					lastPoint = point;
					lastPointTime = System.currentTimeMillis();
					geoPoints.add(point);
				}
			}
		}

		public void onReceivePoi(BDLocation arg0)
		{

		}

	};

	
	private void showNotification()
	{
		CharSequence text = getText(R.string.app_name);
		Notification notification = new Notification(R.drawable.ic_launcher, text, System.currentTimeMillis());

		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this,BaiduMapViewActivity.class), 0);

		// Set the info for the views that show in the notification panel.
		notification.setLatestEventInfo(this, getText(R.string.app_name), text, contentIntent);

		mNM.notify(NOTIFICATION, notification);
	}


	@Override
	public IBinder onBind(Intent arg0)
	{
		return mBinder;
	}



	
	public void startGpsMonitor()
	{
		mLocationClient = new LocationClient(this);
		track();
		mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        showNotification(); 
        
        mStepDetector = new StepDetector();
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		mSensorManager.registerListener(mStepDetector, mSensor, SensorManager.SENSOR_DELAY_FASTEST);
		mStepDetector.addStepListener(mStepListener);
	}
		
	StepListener mStepListener = new StepListener(){
		long lastStepTime = 0;
		int stepCount = 0;
		
		public synchronized void onStep()
		{
			if (stepCount == 0)
			{
				lastStepTime = System.currentTimeMillis();
			}
			stepCount++;
			steps++;
			long i = (System.currentTimeMillis()-lastStepTime)/1000;
			if (i>= 60) //每分钟统计一次
			{
				step_rate = (int)(stepCount*60/i);
				stepCount = 0;
				lastStepTime = 0;
			}
		}
	};
	
	public void stopGpsMonitor()
	{
		mNM.cancel(NOTIFICATION);
		
		mLocationClient.unRegisterLocationListener(myListener);
		mLocationClient.stop();
		
		mSensorManager.unregisterListener(mStepDetector);
		mStepDetector.removeStepListener(mStepListener);
		
		geoPoints.clear();
	}

	private void track()
	{
		mLocationClient.registerLocationListener(myListener);
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


}
