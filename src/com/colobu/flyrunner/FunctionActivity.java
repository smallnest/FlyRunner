package com.colobu.flyrunner;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import com.colobu.flyrunner.utils.AppUtil;
import com.colobu.flyrunner.utils.LocationUtil;
import com.colobu.flyrunner.utils.SharedPreferencesUtils;
import com.colobu.flyrunner.view.FunctionAdapter;
import com.colobu.flyrunner.view.FunctionInfo;

public class FunctionActivity extends Activity implements GridView.OnItemClickListener
{
	private GridView gridview;
	private List<FunctionInfo> list;
	private FunctionAdapter adapter;
	int selected_length = 0;
	int selected_time=0;
	
	private Handler mHandler;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_function_view);
		gridview = (GridView) findViewById(R.id.gridview);
		
		list = new ArrayList<FunctionInfo>();
		list.add(new FunctionInfo("随意跑",R.drawable.ic_tab_free));
		list.add(new FunctionInfo("定长跑",R.drawable.ic_tab_distance));
		list.add(new FunctionInfo("定时跑",R.drawable.ic_tab_time));
				
		list.add(new FunctionInfo("校正位置",R.drawable.ic_tab_adjust));
		list.add(new FunctionInfo("历史记录",R.drawable.ic_tab_history));
		list.add(new FunctionInfo("离线地图",R.drawable.ic_tab_offline));
		
		list.add(new FunctionInfo("设置",R.drawable.ic_tab_setting));
		list.add(new FunctionInfo("帮助",R.drawable.ic_tab_help));
		list.add(new FunctionInfo("关于",R.drawable.ic_tab_about));
		
		adapter = new FunctionAdapter(this);
		adapter.setList(list);
		gridview.setAdapter(adapter);
		
		gridview.setOnItemClickListener(this);  
		
		mHandler = new Handler();

		checkUpdate();
	}

	@Override
	protected void onResume()
	{
		super.onResume();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
	}
	
	public void onItemClick(AdapterView<?> parent, View view,int position, long id)
	{
		Intent intent;
		String[] mList;
		
		AlertDialog.Builder listDia;
		
		switch(position)
		{
		case 0:
			intent = new Intent(FunctionActivity.this, BaiduMapViewActivity.class);
			intent.putExtra("run_type", 0); //随意跑
			startActivity(intent);
			break;
		case 1:
			mList=new String[]{"500米","1公里","3公里","5公里","10公里","42.195公里"};
	        listDia=new AlertDialog.Builder(FunctionActivity.this);
	        
	        listDia.setTitle("选择跑步长度");
	        listDia.setItems(mList, new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int which) {
	            	switch(which)
	            	{
	            	case 0:
	            		FunctionActivity.this.selected_length = 500;
	            		break;
	            	case 1:
	            		FunctionActivity.this.selected_length = 1000;
	            		break;
	            	case 2:
	            		FunctionActivity.this.selected_length = 3000;
	            		break;
	            	case 3:
	            		FunctionActivity.this.selected_length = 5000;
	            		break;
	            	case 4:
	            		FunctionActivity.this.selected_length = 10000;
	            		break;
	            	case 5:
	            		FunctionActivity.this.selected_length = 42195;
	            		break;
	            	}
	            	
	            	if (FunctionActivity.this.selected_length == 0)
	    	        	return;
	    	        
	    	        
	            	Intent intent = new Intent(FunctionActivity.this, BaiduMapViewActivity.class);
	    			intent.putExtra("run_type", 1); //定长跑
	    			intent.putExtra("run_length", FunctionActivity.this.selected_length);
	    			FunctionActivity.this.selected_length = 0;
	    			startActivity(intent);
	            }
	        });
	        
	        
	        listDia.create().show();
	        
			break;
		case 2:
			mList=new String[]{"10分钟","30分钟","1小时","2小时","3小时","5小时"};
	        listDia=new AlertDialog.Builder(FunctionActivity.this);
	        listDia.setTitle("选择跑步时间");
	        listDia.setItems(mList, new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int which) {
	            	switch(which)
	            	{
	            	case 0:
	            		FunctionActivity.this.selected_time = 10;
	            		break;
	            	case 1:
	            		FunctionActivity.this.selected_time = 30;
	            		break;
	            	case 2:
	            		FunctionActivity.this.selected_time = 60;
	            		break;
	            	case 3:
	            		FunctionActivity.this.selected_time = 120;
	            		break;
	            	case 4:
	            		FunctionActivity.this.selected_time = 180;
	            		break;
	            	case 5:
	            		FunctionActivity.this.selected_time = 300;
	            		break;
	            	}
	            	
	            	if (FunctionActivity.this.selected_time == 0)
	    	        	return;
	    	        
	            	Intent intent = new Intent(FunctionActivity.this, BaiduMapViewActivity.class);
	    			intent.putExtra("run_type", 2); //定时跑
	    			intent.putExtra("run_time", FunctionActivity.this.selected_time);
	    			FunctionActivity.this.selected_time=0;
	    			startActivity(intent);
	            }
	        });
	        listDia.create().show();
	        
	        
			break;
		case 3:
			
			startActivity(new Intent(FunctionActivity.this, AdjustBaiduMapViewActivity.class));
			break;
		case 4:
			startActivity(new Intent(FunctionActivity.this, HistoryRecordActivity.class));
			break;
		case 5:
			startActivity(new Intent(FunctionActivity.this, OfflineMapActivity.class));
			break;
		case 6:
			startActivity(new Intent(FunctionActivity.this, SettingActivity.class));
			break;
		case 7:
			startActivity(new Intent(FunctionActivity.this, HelpHtmlActivity.class));
			break;
		case 8:
			startActivity(new Intent(FunctionActivity.this, AboutActivity.class));
			break;
		}
		
	}


	public void checkUpdate()
	{
		SharedPreferences settings = getSharedPreferences(SharedPreferencesUtils.PREFS_NAME,
				Context.MODE_PRIVATE);
		boolean needUpdate = settings.getBoolean(SharedPreferencesUtils.CHECK_UPDATE, true);
		if (needUpdate && LocationUtil.isNetworkEnabled(this))
		{
			long lastUpdateTime = settings.getLong(SharedPreferencesUtils.LAST_UPDATE_TIME, System.currentTimeMillis());
			if ((lastUpdateTime + (24 * 60 * 60 * 1000)) < System.currentTimeMillis())
			{

				lastUpdateTime = System.currentTimeMillis();
				SharedPreferences.Editor editor = settings.edit();
				editor.putLong(SharedPreferencesUtils.LAST_UPDATE_TIME, lastUpdateTime);
				editor.commit();

				Thread checkUpdateThread = new Thread() {
					public void run()
					{
						try
						{
							/* Get current Version Number */
							int curVersion = AppUtil.getVerCode(FunctionActivity.this);
							
							URL updateURL = new URL("http://m.colobu.com/flyrunner/update?version=" + curVersion);
							URLConnection conn = updateURL.openConnection();
							conn.setConnectTimeout(3000);
							conn.setReadTimeout(60000);
							
							InputStream is = conn.getInputStream();
							BufferedInputStream bis = new BufferedInputStream(is);

							byte[] bytes = new byte[is.available()];
							int count = bis.read(bytes);
							
							String s = new String(bytes,0,count);
							JSONObject updateInfo = new JSONObject(s);
							int newVersion = Integer.valueOf(updateInfo.getString("current_version"));
							
							/* Is a higher version than the current already out? */
							if (newVersion > curVersion)
							{
								mHandler.post(showUpdate);
							}
						}
						catch (Exception e)
						{
							Log.e("FunctionActivity", e.getMessage());
						}
					}
				};
				
				checkUpdateThread.start();
			}
		}
	}

	private Runnable showUpdate = new Runnable(){
        public void run(){
         new AlertDialog.Builder(FunctionActivity.this)
         .setTitle("更新程序")
         .setMessage("发现新版本!\n\n是否要下载新版本?")
         .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int whichButton) {
                         Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://m.colobu.com/flyrunner/FlyRunner.apk"));
                         startActivity(intent);
                 }
         })
         .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int whichButton) {
                         
                 }
         })
         .show();
        }
 };

}
