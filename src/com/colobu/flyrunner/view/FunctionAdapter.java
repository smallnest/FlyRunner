package com.colobu.flyrunner.view;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.colobu.flyrunner.R;

public class FunctionAdapter extends BaseAdapter
{

	private class GridHolder
	{
		ImageView appImage;
		TextView appName;
	}

	private Context context;

	private List<FunctionInfo> list;
	private LayoutInflater mInflater;

	public FunctionAdapter(Context c)
	{
		super();
		this.context = c;
	}

	public void setList(List<FunctionInfo> list)
	{
		this.list = list;
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

	}

	public int getCount()
	{
		return list.size();
	}

	public Object getItem(int index)
	{

		return list.get(index);
	}


	public long getItemId(int index)
	{
		return index;
	}

	public View getView(int index, View convertView, ViewGroup parent)
	{
		GridHolder holder;
		if (convertView == null)
		{
			convertView = mInflater.inflate(R.layout.function_item_view, null);
			holder = new GridHolder();
			holder.appImage = (ImageView) convertView.findViewById(R.id.function_item_image);
			holder.appName = (TextView) convertView.findViewById(R.id.itemText);
			convertView.setTag(holder);

		}
		else
		{
			holder = (GridHolder) convertView.getTag();

		}
		FunctionInfo info = list.get(index);
		if (info != null)
		{
			holder.appName.setText(info.getName());
			holder.appImage.setImageResource(info.getResId());
		}
		
		return convertView;
	}

}
