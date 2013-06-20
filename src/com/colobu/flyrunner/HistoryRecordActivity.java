package com.colobu.flyrunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.colobu.flyrunner.db.HistoryRecordOpenHelper;
import com.colobu.flyrunner.db.HistoryRoute;
import com.colobu.flyrunner.utils.DatetimeUtil;
import com.colobu.flyrunner.utils.LocationUtil;
import com.colobu.flyrunner.view.HistoryRecordAdapter;

public class HistoryRecordActivity extends Activity implements OnItemClickListener
{
	HistoryRecordAdapter adapter;
	ListView historyRecordListView;
	
	public static final int countPerPage = 10;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		// 数据格式必须严格
		setContentView(R.layout.activity_history_view);
		
		historyRecordListView = (ListView)findViewById(R.id.historyRecordListView);   
		
		ArrayList<HashMap<String, Object>> data = getData();
		adapter = new HistoryRecordAdapter(this, data,historyRecordListView,countPerPage);
		historyRecordListView.setAdapter(adapter);  
		historyRecordListView.setOnItemClickListener(this);  
				
	}

	@Override
	protected void onResume()
	{
		super.onResume();
	}

	private ArrayList<HashMap<String, Object>> getData()
	{
		ArrayList<HashMap<String, Object>> arrayList = new ArrayList<HashMap<String, Object>>();
		List<HistoryRoute> routes = HistoryRecordOpenHelper.getInstance(this).getHistoryRecords(0, countPerPage);
		
		// 根据需求添加一些数据,
		for (int i = 0; i < routes.size(); i++)
		{
			HashMap<String, Object> tempHashMap = new HashMap<String, Object>();
			String detail = LocationUtil.convertMetre(routes.get(i).routelength);
			
			detail = detail + "   " +  LocationUtil.convertUsedTime(routes.get(i).usedtime);
			detail = detail + "   " + routes.get(i).steps + "步";
			
			tempHashMap.put("id", routes.get(i).id);
			tempHashMap.put("info", detail);
			tempHashMap.put("image", R.drawable.item_red);
			tempHashMap.put("title", routes.get(i).startDate);
			tempHashMap.put("title_detail", DatetimeUtil.getTime(routes.get(i).startDateTime) + " - " 
						+ DatetimeUtil.getTimeOrDateTime(routes.get(i).startDateTime, routes.get(i).endDateTime));
			tempHashMap.put("startTime", DatetimeUtil.getTime(routes.get(i).startDateTime));
			tempHashMap.put("stopTime", DatetimeUtil.getTimeOrDateTime(routes.get(i).startDateTime, routes.get(i).endDateTime));
			
			arrayList.add(tempHashMap);
		}
		return arrayList;
	}

	public void onItemClick(AdapterView<?> arg0,  View v, int position, long id)
	{
		adapter.setSelectItem(position);  
        adapter.notifyDataSetInvalidated(); 
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data)   {
        if (requestCode==1) {
        	adapter.onActivityResult(requestCode, resultCode, data);
        }
    }
}
