package com.colobu.flyrunner.weibo;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.text.TextUtils;

import com.baidu.mapapi.GeoPoint;
import com.colobu.flyrunner.utils.LocationUtil;
import com.colobu.flyrunner.utils.SharedPreferencesUtils;
import com.weibo.net.Utility;
import com.weibo.net.Weibo;
import com.weibo.net.WeiboException;
import com.weibo.net.WeiboParameters;

public class WeiboUtil
{
	//TODO url太长了，不能生成短网址，暂时屏蔽次功能
	public static String createShortUrlFromGeoList(String route)
	{
		String shortUrl = null;
		if (1==1) 
			return shortUrl;
		
		List<GeoPoint> mGeoList = LocationUtil.convertToGeoPointList(route);
		if (mGeoList != null && mGeoList.size()>0)
		{
			GeoPoint startPoint = mGeoList.get(0);
			GeoPoint endPoint = mGeoList.get(mGeoList.size()-1);
			
			String centerPointStr = (startPoint.getLongitudeE6() + endPoint.getLongitudeE6())*1.0f/2E6 +"," ;
			centerPointStr += (startPoint.getLatitudeE6() + endPoint.getLatitudeE6())*1.0f/2E6 +"," ;
			
			int groupNum = 1;
			
			if (mGeoList.size()>500)
			{
				groupNum = mGeoList.size()/500 + 1;
			}
			StringBuilder sb = new StringBuilder();
			for (int i=0;i<mGeoList.size();i=i+groupNum) {
				sb.append(";").append(mGeoList.get(i).getLongitudeE6()*1.0f/1E6).append(",").append(mGeoList.get(i).getLatitudeE6()*1.0f/1E6);
			}
			
			sb.append(";").append(mGeoList.get(mGeoList.size()-1).getLongitudeE6()*1.0f/1E6).append(",").append(mGeoList.get(mGeoList.size()-1).getLatitudeE6()*1.0f/1E6);
			String locationStr =sb.toString().substring(1);
			
			String mapUrl = "http://api.map.baidu.com/staticimage?center=" + centerPointStr 
					+"&width=500&height=500&zoom=15&paths="
					+ locationStr+
					"&pathStyles=0xff0000,5,1";
			
			shortUrl = WeiboUtil.getShortUrl(mapUrl, SharedPreferencesUtils.CONSUMER_KEY);
			if (shortUrl == null)
			{
				shortUrl = WeiboUtil.getShortUrl(mapUrl, SharedPreferencesUtils.TEMP_APP_KEY);
			}
			
		}
		return shortUrl;
	}


	
	public static String getShortUrl(String longUrl,String key)
	{
		
		String serverUrl = "http://api.t.sina.com.cn/short_url/shorten.json?source=" + key +  "&url_long=" + URLEncoder.encode(longUrl);
		HttpParams  httpParameters = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParameters, 2000);// Set the default socket timeout (SO_TIMEOUT) // in milliseconds which is the timeout for waiting for data.
        HttpConnectionParams.setSoTimeout(httpParameters, 3000);
		
		DefaultHttpClient client = new DefaultHttpClient(httpParameters); 
		HttpGet get = new HttpGet(serverUrl);  
        HttpResponse response;
		try
		{
			response = client.execute(get);
			if(response.getStatusLine().getStatusCode()==200){  
	            InputStream in = response.getEntity().getContent();
	            byte[] bytes = new byte[(int)response.getEntity().getContentLength()];
	            in.read(bytes);
	            JSONArray result = new JSONArray(new String(bytes));
	            if (result.length()>0)
	            {
	            	JSONObject urlInfo = result.getJSONObject(0);
	            	return urlInfo.getString("url_short");
	            }
	        }  
		}
		catch (ClientProtocolException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		} 
		
		return null;
	}
	
	public static void share2weibo(Activity context,String content, String picPath) throws WeiboException
	{
		Weibo weibo = Weibo.getInstance();
		weibo.share2weibo(context, weibo.getAccessToken().getToken(), weibo.getAccessToken().getSecret(), content, picPath);
	}

	public static String getPublicTimeline(Activity context,Weibo weibo) throws MalformedURLException, IOException, WeiboException
	{
		String url = Weibo.SERVER + "statuses/public_timeline.json";
		WeiboParameters bundle = new WeiboParameters();
		bundle.add("source", Weibo.getAppKey());
		String rlt = weibo.request(context, url, bundle, "GET", weibo.getAccessToken());
		return rlt;
	}

	public static String upload(Activity context,Weibo weibo, String source, String file, String status, String lon, String lat)
			throws WeiboException
	{
		WeiboParameters bundle = new WeiboParameters();
		bundle.add("source", source);
		bundle.add("pic", file);
		bundle.add("status", status);
		if (!TextUtils.isEmpty(lon))
		{
			bundle.add("lon", lon);
		}
		if (!TextUtils.isEmpty(lat))
		{
			bundle.add("lat", lat);
		}
		String rlt = "";
		String url = Weibo.SERVER + "statuses/upload.json";
		try
		{
			rlt = weibo.request(context, url, bundle, Utility.HTTPMETHOD_POST, weibo.getAccessToken());
		}
		catch (WeiboException e)
		{
			throw new WeiboException(e);
		}
		return rlt;
	}

	private String update(Activity context,Weibo weibo, String source, String status, String lon, String lat) throws WeiboException
	{
		WeiboParameters bundle = new WeiboParameters();
		bundle.add("source", source);
		bundle.add("status", status);
		if (!TextUtils.isEmpty(lon))
		{
			bundle.add("lon", lon);
		}
		if (!TextUtils.isEmpty(lat))
		{
			bundle.add("lat", lat);
		}
		String rlt = "";
		String url = Weibo.SERVER + "statuses/update.json";
		rlt = weibo.request(context, url, bundle, Utility.HTTPMETHOD_POST, weibo.getAccessToken());
		return rlt;
	}
}
