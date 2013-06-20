package com.colobu.flyrunner.view;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.baidu.mapapi.MKOfflineMap;
import com.colobu.flyrunner.OfflineMapActivity;
import com.colobu.flyrunner.R;

public class OfflineMapAdapter extends BaseAdapter
{

	private ArrayList<HashMap<String, Object>> data;
	private LayoutInflater layoutInflater;
	private OfflineMapActivity context;
	private MKOfflineMap mOffline = null;
	
	ListView offlineMapListView;
	
	private int selectItem = -1;

	public OfflineMapAdapter(OfflineMapActivity context, ArrayList<HashMap<String, Object>> data,
			ListView offlineMapListView,MKOfflineMap mOffline)
	{
		this.context = context;
		this.data = data;
		this.offlineMapListView = offlineMapListView;
		this.layoutInflater = LayoutInflater.from(context);
		this.mOffline = mOffline;
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
		OfflineMapRecord record = null;
		if (convertView == null)
		{
			record = new OfflineMapRecord();
			convertView = layoutInflater.inflate(R.layout.offline_map_listitem_view, null);
			record.deleteButton = (Button) convertView.findViewById(R.id.btn_delete_offline_map);
			record.cityNameView = (TextView) convertView.findViewById(R.id.city_name);
			record.cityStatusView = (TextView) convertView.findViewById(R.id.city_rate);

			// 这里要注意，是使用的tag来存储数据的。
			convertView.setTag(record);
		}
		else
		{
			record = (OfflineMapRecord) convertView.getTag();
		}
		// 绑定数据、以及事件触发
		// record.imageView.setImageResource((Integer)
		// data.get(position).get("image"));
		record.cityNameView.setText((String) data.get(position).get("name"));
		record.cityStatusView.setText((String) data.get(position).get("status"));
		final int cityId = (Integer) data.get(position).get("cityId");
		record.cityId = cityId;
		
		record.deleteButton.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0)
			{
				AlertDialog.Builder normalDia = new AlertDialog.Builder(context);
				normalDia.setIcon(android.R.drawable.ic_dialog_alert);
				normalDia.setTitle("删除");
				normalDia.setMessage("你确定要删除" + (String) data.get(position).get("name") + "的地图吗?");

				normalDia.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					
					public void onClick(DialogInterface dialog, int which) {
						mOffline.remove(cityId);
						notifyDataSetChanged();
					}
				});

				normalDia.setNegativeButton("取消", null);
				normalDia.create().show();
				
				selectItem = position;
				notifyDataSetInvalidated();
				
			}});

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
	
	public void setSelectItem(int selectItem)
	{
		this.selectItem = selectItem;
	}
}
