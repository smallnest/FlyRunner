package com.colobu.flyrunner.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.colobu.flyrunner.HistoryMapViewActivity;
import com.colobu.flyrunner.HistoryRecordActivity;
import com.colobu.flyrunner.R;
import com.colobu.flyrunner.db.HistoryRecordOpenHelper;
import com.colobu.flyrunner.db.HistoryRoute;
import com.colobu.flyrunner.utils.DatetimeUtil;
import com.colobu.flyrunner.utils.LocationUtil;
import com.colobu.flyrunner.utils.SharedPreferencesUtils;
import com.colobu.flyrunner.weibo.WeiboUtil;
import com.renren.api.connect.android.Renren;
import com.renren.api.connect.android.status.StatusSetRequestParam;
import com.tencent.weibo.api.TAPI;
import com.tencent.weibo.constants.OAuthConstants;
import com.tencent.weibo.oauthv1.OAuthV1;
import com.tencent.weibo.oauthv1.OAuthV1Client;
import com.tencent.weibo.webview.OAuthV1AuthorizeWebView;
import com.weibo.net.AccessToken;
import com.weibo.net.DialogError;
import com.weibo.net.Oauth2AccessTokenHeader;
import com.weibo.net.Utility;
import com.weibo.net.Weibo;
import com.weibo.net.WeiboDialogListener;
import com.weibo.net.WeiboException;

public class HistoryRecordAdapter extends BaseAdapter
{

	private ArrayList<HashMap<String, Object>> data;
	private LayoutInflater layoutInflater;
	private HistoryRecordActivity context;
	private int selectItem = -1;

	private String weiboText = "";
	private OAuthV1 qqOAuth;
	
	ViewSwitcher footerView;
	boolean footerLoading = false;
	ListView historyRecordListView;

	public int countPerPage = 10;

	public HistoryRecordAdapter(HistoryRecordActivity context, ArrayList<HashMap<String, Object>> data,
			ListView historyRecordListView, int countPerPage)
	{
		this.context = context;
		this.data = data;
		this.historyRecordListView = historyRecordListView;
		this.layoutInflater = LayoutInflater.from(context);
		this.countPerPage = countPerPage;

		footerView = (ViewSwitcher) layoutInflater.inflate(R.layout.history_listview_footer, null);
		TextView moreByFooter = (TextView) footerView.findViewById(R.id.textview_to_load_more);
		moreByFooter.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v)
			{
				if (!footerLoading)
				{
					new FooterTask().execute();
				}
			}
		});

		if (data.size() >= countPerPage)
			historyRecordListView.addFooterView(footerView);

	}

	public int getCount()
	{
		return data.size();
	}

	/**
	 * 获取某一位置的数据
	 */
	public Object getItem(int position)
	{
		return data.get(position);
	}

	/**
	 * 获取唯一标识
	 */
	public long getItemId(int position)
	{
		return position;
	}

	/**
	 * android绘制每一列的时候，都会调用这个方法
	 */
	public View getView(final int position, View convertView, ViewGroup parent)
	{
		HistoryRecord record = null;
		if (convertView == null)
		{
			record = new HistoryRecord();
			convertView = layoutInflater.inflate(R.layout.history_listitem_view, null);
			record.imageView = (ImageView) convertView.findViewById(R.id.record_item_image);
			record.titleView = (TextView) convertView.findViewById(R.id.title);
			record.titleDetailView = (TextView) convertView.findViewById(R.id.title_detail);
			record.infoView = (TextView) convertView.findViewById(R.id.info);
			record.menuImage = (ImageView) convertView.findViewById(R.id.item_menu_view);

			// 这里要注意，是使用的tag来存储数据的。
			convertView.setTag(record);
		}
		else
		{
			record = (HistoryRecord) convertView.getTag();
		}
		// 绑定数据、以及事件触发
		// record.imageView.setImageResource((Integer)
		// data.get(position).get("image"));
		record.titleView.setText((String) data.get(position).get("title"));
		record.titleDetailView.setText((String) data.get(position).get("title_detail"));
		record.infoView.setText((String) data.get(position).get("info"));
		
		
		
		record.menuImage.setOnClickListener(new OnClickListener() {
			public void onClick(View v)
			{			
				
				String[] mList = new String[]{"显示地图","删除记录","取消","分享到新浪微博","分享到腾讯微博","分享到人人网"};
				AlertDialog.Builder listDia=new AlertDialog.Builder(context);
				listDia.setTitle("");
		        listDia.setItems(mList, new DialogInterface.OnClickListener() {
		            public void onClick(DialogInterface dialog, int which) {
		            	selectItem = position;
		            	switch(which)
		            	{
		            	case 0:
		            		historyRecordListView.invalidateViews();
							Intent intent = new Intent(context, HistoryMapViewActivity.class);
							String route = HistoryRecordOpenHelper.getInstance(context).getHistoryRecordRoute((Long) data.get(position).get("id"));
							intent.putExtra("route", route); 
							context.startActivity(intent);
		            		break;
		            	case 1:
		            		Long recorder_id = (Long) data.remove(position).get("id");
							HistoryRecordOpenHelper.getInstance(context).deleteHistoryRecord(recorder_id);
							notifyDataSetChanged();
		            		break;
		            	case 2:
		            		historyRecordListView.invalidateViews();
		            		break;
		            	case 3:
		            		historyRecordListView.invalidateViews();
		            		sendSinaWeibo(position);
		            		break;
		            	case 4:
		            		historyRecordListView.invalidateViews();
		            		sendTencentWeibo(position);
		            		break;
		            	case 5:
		            		historyRecordListView.invalidateViews();
		            		sendRenren(position);
		            		break;
		            	}
		            }
		        });
		        
		        
		        listDia.create().show();
			}
		});

		if (position == selectItem)
		{
			convertView.setBackgroundResource(R.color.list_item_bg_focus);
		}
		else
		{
			convertView.setBackgroundResource(R.color.list_item_bg);
		}

		return convertView;
	}
	
	public void sendRenren(int position)
	{
		if (!LocationUtil.isNetworkEnabled(context))
		{
			Toast.makeText(context, "未联网，无法将跑步信息分享到人人网", Toast.LENGTH_LONG).show();
			return;
		}
		
		
		HistoryRoute historyRoute = HistoryRecordOpenHelper.getInstance(context).getHistoryRecord((Long) data.get(position).get("id"));
		if (historyRoute == null)
			return;
		
		
		weiboText = historyRoute.startDate + " 跑了" + LocationUtil.convertMetre(historyRoute.routelength)
				+ ",用时" + LocationUtil.convertUsedTime(historyRoute.usedtime);
		
		
		String route = historyRoute.route;
		String shortUrl = WeiboUtil.createShortUrlFromGeoList(route);
		
		if (shortUrl != null)
		{
			weiboText = weiboText + ". 点击查看跑步路线: " + shortUrl;
		}
		
		Renren renren = new Renren(SharedPreferencesUtils.RENREN_APP_KEY,SharedPreferencesUtils.RENREN_APP_SECRET,"209624",context);
		renren.publishStatus(context,new StatusSetRequestParam(weiboText));
		
	}

	
	public void sendTencentWeibo(int position)
	{
		if (!LocationUtil.isNetworkEnabled(context))
		{
			Toast.makeText(context, "未联网，无法将跑步信息分享到腾讯微博", Toast.LENGTH_LONG).show();
			return;
		}
		
		
		HistoryRoute historyRoute = HistoryRecordOpenHelper.getInstance(context).getHistoryRecord((Long) data.get(position).get("id"));
		if (historyRoute == null)
			return;
		
		
		weiboText = historyRoute.startDate + " 跑了" + LocationUtil.convertMetre(historyRoute.routelength)
				+ ",用时" + LocationUtil.convertUsedTime(historyRoute.usedtime);
		
		
		String route = historyRoute.route;
		String shortUrl = WeiboUtil.createShortUrlFromGeoList(route);
		
		if (shortUrl != null)
		{
			weiboText = weiboText + ". 点击查看跑步路线: " + shortUrl;
		}
		
	
		qqOAuth=new OAuthV1("null");
		qqOAuth.setOauthConsumerKey("801115505");
		qqOAuth.setOauthConsumerSecret("be1dd1410434a9f7d5a2586bab7a6829");
        
        try {
        	qqOAuth=OAuthV1Client.requestToken(qqOAuth);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SharedPreferences settings = context.getSharedPreferences(SharedPreferencesUtils.PREFS_NAME, Context.MODE_PRIVATE);
        String qq_access_token = settings.getString(SharedPreferencesUtils.QQ_ACCESS_TOKEN, null);
        if (qq_access_token != null)
        {
        	qqOAuth.setOauthToken(qq_access_token);
        	String qq_access_token_secret = settings.getString(SharedPreferencesUtils.QQ_ACCESS_TOKEN_SECRET, null);
        	qqOAuth.setOauthTokenSecret(qq_access_token_secret);
        	sendQQWeibo();
        }
        else
        {
        	Intent intent = new Intent(context, OAuthV1AuthorizeWebView.class);
        	intent.putExtra("oauth", qqOAuth);
        	context.startActivityForResult(intent,1);
        }
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data)   {
        if (requestCode==1) {
        	if (resultCode==OAuthV1AuthorizeWebView.RESULT_CODE)    {
        		qqOAuth=(OAuthV1) data.getExtras().getSerializable("oauth");
                
                try {
                	qqOAuth=OAuthV1Client.accessToken(qqOAuth);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
                
                SharedPreferences settings = context.getSharedPreferences(SharedPreferencesUtils.PREFS_NAME, Context.MODE_PRIVATE);
                Editor editor = settings.edit();
                editor.putString(SharedPreferencesUtils.QQ_ACCESS_TOKEN, qqOAuth.getOauthToken());
                editor.putString(SharedPreferencesUtils.QQ_ACCESS_TOKEN_SECRET, qqOAuth.getOauthTokenSecret());
                editor.commit();
                
                
                sendQQWeibo();
            }
        }
    }

	private void sendQQWeibo()
	{
		TAPI qqAPI = new TAPI(OAuthConstants.OAUTH_VERSION_1);
		
		try
		{
			String response = qqAPI.add(qqOAuth, "json", weiboText, LocationUtil.getLocalIpAddress());
			JSONObject res = new JSONObject(response);
			
			String ret = res.getString("ret");
			
			if (ret != null && ret.equals("0"))
			{
				Toast.makeText(context, "分享成功", Toast.LENGTH_LONG).show();
			}
			else
			{
				String errcode = res.getString("errcode");
				String msg = res.getString("msg");
				Toast.makeText(context, "分享失败。\n错误码:" + errcode + ",错误信息:" + msg, Toast.LENGTH_LONG).show();
			}
			
		}
		catch (Exception e1)
		{
			SharedPreferences settings = context.getSharedPreferences(SharedPreferencesUtils.PREFS_NAME, Context.MODE_PRIVATE);
            Editor editor = settings.edit();
            editor.remove(SharedPreferencesUtils.QQ_ACCESS_TOKEN);
            editor.remove(SharedPreferencesUtils.QQ_ACCESS_TOKEN_SECRET);
            editor.commit();
			e1.printStackTrace();
		}
		qqAPI.shutdownConnection();
	}
	
	public void sendSinaWeibo(int position)
	{
		if (!LocationUtil.isNetworkEnabled(context))
		{
			Toast.makeText(context, "未联网，无法将跑步信息分享到新浪微博", Toast.LENGTH_LONG).show();
			return;
		}
		
		
		HistoryRoute historyRoute = HistoryRecordOpenHelper.getInstance(context).getHistoryRecord((Long) data.get(position).get("id"));
		if (historyRoute == null)
			return;
		
		
		weiboText = historyRoute.startDate + " 跑了" + LocationUtil.convertMetre(historyRoute.routelength)
				+ ",用时" + LocationUtil.convertUsedTime(historyRoute.usedtime);
		
		
		String route = historyRoute.route;
		String shortUrl = WeiboUtil.createShortUrlFromGeoList(route);
		
		if (shortUrl != null)
		{
			weiboText = weiboText + "\n点击查看跑步路线 " + shortUrl;
		}
		
		SharedPreferences settings = context.getSharedPreferences(SharedPreferencesUtils.PREFS_NAME, Context.MODE_PRIVATE);
		String token = settings.getString(SharedPreferencesUtils.ACCESS_TOKEN, null);
		String expire_in = settings.getString(SharedPreferencesUtils.TOKEN_EXPIRES_IN, "86400");
		long expire_time = settings.getLong(SharedPreferencesUtils.ACCESS_TOKEN_EXPIRES_TIME, -1);
	
		Weibo weibo = Weibo.getInstance();
		weibo.setupConsumerConfig(SharedPreferencesUtils.CONSUMER_KEY, SharedPreferencesUtils.CONSUMER_SECRET);
		// Oauth2.0 隐式授权认证方式
		weibo.setRedirectUrl("http://m.colobu.com/flyrunner");// 此处回调页内容应该替换为与opaque对应的应用回调页
		Utility.setAuthorization(new Oauth2AccessTokenHeader());
		
		if (token != null && (System.currentTimeMillis() < expire_time)) //使用已有的access-token
		{
			sendWeiboTextAndImage(token, expire_in);
		}
		else
		{
			// 对应的应用回调页可在开发者登陆新浪微博开发平台之后，
			// 进入我的应用--应用详情--应用信息--高级信息--授权设置--应用回调页进行设置和查看，
			// 应用回调页不可为空
			weibo.authorize(context,new AuthDialogListener());
		}
	}

	
	private void sendWeiboTextAndImage(String token, String expires_in)
	{
		AccessToken accessToken = new AccessToken(token, SharedPreferencesUtils.CONSUMER_SECRET);
		accessToken.setExpiresIn(expires_in);
		Weibo.getInstance().setAccessToken(accessToken);
		
		try {
			WeiboUtil.share2weibo(context,weiboText, null);
        } catch (WeiboException e) {
            Log.e("HistoryRecordAdapter", e.getMessage(),e);
        } finally {

        }
	}
	
	class AuthDialogListener implements WeiboDialogListener {
		public void onComplete(Bundle values) {
			String token = values.getString("access_token");
			String expires_in = values.getString("expires_in");
			
			SharedPreferences settings = context.getSharedPreferences(SharedPreferencesUtils.PREFS_NAME, Context.MODE_PRIVATE);
			Editor edit  = settings.edit();
			edit.putString(SharedPreferencesUtils.ACCESS_TOKEN, token);
			edit.putString(SharedPreferencesUtils.TOKEN_EXPIRES_IN, expires_in);
			edit.putLong(SharedPreferencesUtils.ACCESS_TOKEN_EXPIRES_TIME, Long.valueOf(expires_in).longValue()*1000 +System.currentTimeMillis() - 60000); 
			edit.commit();
			
			sendWeiboTextAndImage(token, expires_in);
		}
		public void onError(DialogError e) {
			Toast.makeText(context,	"授权失败 : " + e.getMessage(), Toast.LENGTH_LONG).show();
		}
		public void onCancel() {
			//Toast.makeText(context, "Auth cancel",Toast.LENGTH_LONG).show();
		}
		public void onWeiboException(WeiboException e) {
			Toast.makeText(context,	"授权失败 : " + e.getMessage(), Toast.LENGTH_LONG).show();
		}

	}
	public void setSelectItem(int selectItem)
	{
		this.selectItem = selectItem;
	}
	
	class FooterTask extends AsyncTask<String, Integer, ArrayList<HashMap<String, Object>>>
	{

		@Override
		protected void onPreExecute()
		{
			footerLoading(true);
		}

		@Override
		protected ArrayList<HashMap<String, Object>> doInBackground(String... params)
		{
			ArrayList<HashMap<String, Object>> arrayList = new ArrayList<HashMap<String, Object>>();

			List<HistoryRoute> routes = HistoryRecordOpenHelper.getInstance(context).getHistoryRecords(data.size(),
					countPerPage);

			for (int i = 0; i < routes.size(); i++)
			{
				String detail = LocationUtil.convertMetre(routes.get(i).routelength);
				
				detail = detail + "   " +  LocationUtil.convertUsedTime(routes.get(i).usedtime);
				detail = detail + "   " + routes.get(i).steps + "步";
				HashMap<String, Object> tempHashMap = new HashMap<String, Object>();
				tempHashMap.put("id", routes.get(i).id);
				tempHashMap.put("info", detail);
				tempHashMap.put("image", R.drawable.item_red);
				tempHashMap.put("title", routes.get(i).startDate);
				tempHashMap.put("title_detail", DatetimeUtil.getTime(routes.get(i).startDateTime) + " - "
						+ DatetimeUtil.getTimeOrDateTime(routes.get(i).startDateTime, routes.get(i).endDateTime));
				tempHashMap.put("startTime", DatetimeUtil.getTime(routes.get(i).startDateTime));
				tempHashMap.put("stopTime",
						DatetimeUtil.getTimeOrDateTime(routes.get(i).startDateTime, routes.get(i).endDateTime));

				arrayList.add(tempHashMap);
			}

			return arrayList;
		}

		@Override
		protected void onPostExecute(ArrayList<HashMap<String, Object>> result)
		{
			data.addAll(result);
			notifyDataSetChanged();
			
			long count = HistoryRecordOpenHelper.getInstance(context).countHistoryRecords();
			if (count == data.size())
				historyRecordListView.removeFooterView(footerView);

			footerLoading(false);
		}

	}

	private void footerLoading(boolean isLoading)
	{
		if (isLoading)
		{
			footerView.setDisplayedChild(1);
		}
		else
		{
			footerView.setDisplayedChild(0);
		}
		footerLoading = isLoading;
	}

}
