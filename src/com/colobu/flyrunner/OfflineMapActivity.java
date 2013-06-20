package com.colobu.flyrunner;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.MKOLSearchRecord;
import com.baidu.mapapi.MKOLUpdateElement;
import com.baidu.mapapi.MKOfflineMap;
import com.baidu.mapapi.MKOfflineMapListener;
import com.colobu.flyrunner.view.OfflineMapAdapter;

public class OfflineMapActivity extends Activity implements
		MKOfflineMapListener, OnItemClickListener {

	private MKOfflineMap mOffline = null;
	private ProgressDialog processDia = null;
	EditText mCityName;
	String mName;
	
	FlyRunnerApplication app;
	
	OfflineMapAdapter adapter;
	ListView offlineMapListView;
	
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_offline_map_view);
		offlineMapListView = (ListView)findViewById(R.id.offlineMapListView);   
		
		
		app = (FlyRunnerApplication) this.getApplication();
		if (app.mBMapMan == null) {
			app.mBMapMan = new BMapManager(getApplication());
			app.mBMapMan.init(app.mStrKey,
					new FlyRunnerApplication.MyGeneralListener());
		}
		app.mBMapMan.start();
		
		mOffline = new MKOfflineMap();
		mOffline.init(app.mBMapMan, this);
		mOffline.scan();
		
		mCityName = (EditText) findViewById(R.id.city_name);
		
		Button btn = (Button) findViewById(R.id.btn_download);
		btn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				mName = mCityName.getText().toString();
				if (mName == null || mName.length() == 0)
					return;
				
				ArrayList<MKOLSearchRecord> records = mOffline.searchCity(mName);
				if (records == null || records.size() != 1)
				{
					showWarnDialog("离线地图不存在");
					return;
				}
				
				int cityid = -1;
				
				for(MKOLSearchRecord r : records)
				{
					if (r.cityName.indexOf(mName) > -1)
					{
						mName = r.cityName;
						cityid = r.cityID;
						break;
					}
				}
				
				if (cityid == -1)
				{
					showWarnDialog("离线地图不存在");
					return;
				}
				
				if (mOffline.start(cityid)) {
					Log.d("OfflineMapActivity",String.format("start cityid:%d", cityid));
				} else {
					Log.d("OfflineMapActivity",String.format("not start cityid:%d", cityid));
				}
				
				adapter.notifyDataSetChanged();
				showProcessDia(mName);
			}
		});
		
		
		ArrayList<HashMap<String, Object>> data = getData();
		adapter = new OfflineMapAdapter(this, data,offlineMapListView,mOffline);
		offlineMapListView.setAdapter(adapter);  
		offlineMapListView.setOnItemClickListener(this);
	}
	
	@Override
	protected void onPause() {
		app.mBMapMan.stop();
		super.onPause();
	}

	@Override
	protected void onResume() {
		app.mBMapMan.start();
		super.onResume();
	}

	private void showWarnDialog(String msg)
    {
        //AlertDialog.Builder normalDialog=new AlertDialog.Builder(getApplicationContext());
        AlertDialog.Builder normalDia=new AlertDialog.Builder(OfflineMapActivity.this);
        normalDia.setIcon(android.R.drawable.ic_dialog_alert);
        normalDia.setTitle("警告");
        normalDia.setMessage(msg);
        
        normalDia.setPositiveButton("确定",null);
        
        normalDia.create().show();
    }
	
	public void onGetOfflineMapState(int type, int state) {
		switch (type) {
		case MKOfflineMap.TYPE_DOWNLOAD_UPDATE: {
			Log.d("OfflineDemo", String.format("cityid:%d update", state));
			MKOLUpdateElement update = mOffline.getUpdateInfo(state);
			if (update.cityName.equals(mName))
			{
				processDia.setProgress(update.ratio);
				processDia.setMessage((String.format("大小:%.2fMB 已下载%d%%",
						((double) update.size) / 1000000,
						update.ratio)));
				if (update.status == MKOLUpdateElement.FINISHED)
				{
					processDia.dismiss();
					mOffline.scan();
				}
			}
		}
			break;
		case MKOfflineMap.TYPE_NEW_OFFLINE:
			Log.d("OfflineMapActivity", String.format("add offlinemap num:%d", state));
			//MKOLUpdateElement update = mOffline.getUpdateInfo(state);
			break;
		case MKOfflineMap.TYPE_VER_UPDATE:
			Log.d("OfflineMapActivity", String.format("new offlinemap ver"));
			break;
		}

	}

	private void showProcessDia(String cityName) {
		processDia = new ProgressDialog(OfflineMapActivity.this);
		processDia.setTitle(cityName);
		processDia.setMessage("开始下载... 0%");
		processDia.setIndeterminate(true);
		processDia.setCancelable(true);
		processDia.show();
	}

	private ArrayList<HashMap<String, Object>> getData()
	{
		ArrayList<HashMap<String, Object>> arrayList = new ArrayList<HashMap<String, Object>>();
		
		ArrayList<MKOLUpdateElement> info = mOffline.getAllUpdateInfo();
		if (info != null) {
			Log.d("OfflineMapActivity", String.format("has %d city info", info.size()));
			
			for (MKOLUpdateElement element : info)
			{
				HashMap<String, Object> tempHashMap = new HashMap<String, Object>();
				
				tempHashMap.put("cityId", element.cityID);
				tempHashMap.put("name", element.cityName);
				if (element.status == MKOLUpdateElement.FINISHED)
					tempHashMap.put("status", "下载完成");
				else if (element.status == MKOLUpdateElement.DOWNLOADING)
					tempHashMap.put("status", "下载中...");
				else if (element.status == MKOLUpdateElement.SUSPENDED)
					tempHashMap.put("status", "下载暂停");
				else if (element.status == MKOLUpdateElement.WAITING)
					tempHashMap.put("status", "等待下载...");
				else
					tempHashMap.put("status", "状态未知");
				arrayList.add(tempHashMap);
			}
		}

		// 根据需求添加一些数据,
		
		return arrayList;
	}
	
	public void onItemClick(AdapterView<?> arg0,  View v, int position, long id)
	{
		adapter.setSelectItem(position);  
        adapter.notifyDataSetInvalidated(); 		
	}
}
