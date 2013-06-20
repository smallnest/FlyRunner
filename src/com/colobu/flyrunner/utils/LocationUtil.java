package com.colobu.flyrunner.utils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.util.Log;

import com.baidu.mapapi.GeoPoint;

public class LocationUtil
{
	//private final static double EARTH_RADIUS = 6378137.0;
	private final static double EARTH_RADIUS = 6370693.5;
	
	public static double gps2m(double lat_a, double lng_a, double lat_b, double lng_b)
	{
		double radLat1 = (lat_a * Math.PI / 180.0);
		double radLat2 = (lat_b * Math.PI / 180.0);
		double a = radLat1 - radLat2;
		double b = (lng_a - lng_b) * Math.PI / 180.0;
		double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2) + Math.cos(radLat1) * Math.cos(radLat2)
				* Math.pow(Math.sin(b / 2), 2)));

		s = s * EARTH_RADIUS;
		s = Math.round(s * 10000) / 10000;
		return s;

	}

	public static String convertFromGeoPointList(List<GeoPoint> geoList)
	{
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < geoList.size(); i++)
		{
			GeoPoint point = geoList.get(i);
			sb.append(point.getLatitudeE6());
			sb.append("_");
			sb.append(point.getLongitudeE6());
			sb.append(",");
		}

		String route = sb.toString();
		if (route.endsWith(","))
			route.substring(0, route.length() - 1);

		return route;
	}

	public static List<GeoPoint> convertToGeoPointList(String route)
	{
		List<GeoPoint> geoList = new ArrayList<GeoPoint>();
		String[] points = route.split(",");
		for (int i = 0; i < points.length; i++)
		{
			String[] l = points[i].split("_");

			GeoPoint point = new GeoPoint(Integer.parseInt(l[0]), Integer.parseInt(l[1]));
			geoList.add(point);
		}
		return geoList;
	}

	public static String convertMetre(long run_length)
	{
		if (run_length < 1000)
		{
			return run_length + " 米";
		}
		else
		{
			return String.format("%.1f", run_length * 1.0 / 1000) + " 公里";
		}
	}

	public static String convertUsedTime(long used_time)
	{
		if (used_time < 60000)
		{
			return used_time / 1000 + " 秒";
		}
		else if (used_time < 3600000)
		{
			int usedMinute = (int)(used_time / 60000);
			return usedMinute + " 分钟" + (used_time-usedMinute*60000) / 1000 + "秒";
		}
		else
		{
			long usedHour = used_time / 3600000;
			long usedMinute = (used_time - usedHour * 3600000) / 60000;
			return usedHour + " 小时" + usedMinute + "分钟";
		}
	}

	public static boolean isGPSEnabled(Context context)
	{
		LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
	}

	public static void startGps(Context context)
	{
		Intent intent = new Intent(Settings.ACTION_SECURITY_SETTINGS);
		context.startActivity(intent);
	}
	
	public static void checkAndStartGps(Context context)
	{
		LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
		{
			Intent intent = new Intent(Settings.ACTION_SECURITY_SETTINGS);
			context.startActivity(intent);
		}
	}

	public static boolean isWifiEnabled(Context context)
	{
		WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		return wifiManager.isWifiEnabled();
	}

	public static boolean isNetworkEnabled(Context context)
	{
		ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (manager == null)
		{
			return false;
		}
		else
		{
			NetworkInfo[] info = manager.getAllNetworkInfo();
			if (info != null)
			{
				for (int i = 0; i < info.length; i++)
				{
					if (info[i].getState() == NetworkInfo.State.CONNECTED)
					{
						return true;
					}
				}
			}
		}

		return false;

	}
	
	public static String getLocalIpAddress()
	{
		try
		{
			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();)
			{
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();)
				{
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress())
					{
						return inetAddress.getHostAddress().toString();
					}
				}
			}
		}
		catch (SocketException ex)
		{
			Log.e("LocationUtil", ex.getMessage());
		}
		
		return null;
	}
}
