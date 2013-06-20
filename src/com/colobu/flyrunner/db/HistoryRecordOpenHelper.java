package com.colobu.flyrunner.db;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class HistoryRecordOpenHelper extends SQLiteOpenHelper
{
	private static final int DATABASE_VERSION = 1;
	public static final String DATABASE_NAME = "xys.db";

	private static final String ROUTE_TABLE_NAME = "run_route";
	private static final String ROUTE_TABLE_CREATE = "CREATE TABLE " + ROUTE_TABLE_NAME + " ("
			+ "_id INTEGER PRIMARY KEY AUTOINCREMENT," + "startDate TEXT," 
			+ "startDateTime INTEGER DEFAULT 0," + "endDateTime INTEGER DEFAULT 0," 
			+ "routelength INTEGER DEFAULT 0," + " usedtime INTEGER DEFAULT 0,"
			+ "steps INTEGER DEFAULT 0,"	
			+ "route TEXT," 
			+ "offsetX INTEGER DEFAULT 0,"
			+ "offsetY INTEGER DEFAULT 0);";

	private static HistoryRecordOpenHelper mInstance = null;
	private static SQLiteDatabase db = null;
	
	private static final String TAG = "HistoryRecordOpenHelper";
	
	public HistoryRecordOpenHelper(Context context)
	{
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	public static synchronized HistoryRecordOpenHelper getInstance(Context context)
	{
		if (mInstance == null)
		{
			mInstance = new HistoryRecordOpenHelper(context);
			db = mInstance.getWritableDatabase();
		}

		return mInstance;
	}

	@Override
	public void onCreate(SQLiteDatabase db)
	{
		db.execSQL(ROUTE_TABLE_CREATE);  
	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2)
	{

	}
	
	public boolean deleteDatabase(Context context)
	{
		return context.deleteDatabase(DATABASE_NAME);
	}

	
	public void insertHistoryRecord(String startDate,long startDateTime,long endDateTime,
			int routelength,long usedtime,int steps,String route,int offsetX,int offsetY)
	{
		ContentValues initialValues = new ContentValues();
		initialValues.put("startDate", startDate);
		initialValues.put("startDateTime", startDateTime);
		initialValues.put("endDateTime", endDateTime);
		initialValues.put("routelength", routelength);
		initialValues.put("usedtime", usedtime);
		initialValues.put("steps", steps);
		initialValues.put("route", route);
		initialValues.put("offsetX", offsetX);
		initialValues.put("offsetY", offsetY);
		
		try
		{
			db.insert(ROUTE_TABLE_NAME, null, initialValues);
		}
		catch(Exception ex)
		{
			Log.e(TAG, "failed to insert " + startDate);
		}
	}
	
	public void deleteHistoryRecord(long _id)
	{
		try
		{
			db.execSQL("delete from " + ROUTE_TABLE_NAME + " where _id=" + _id);
		}
		catch(Exception ex)
		{
			Log.e(TAG, "failed to delete " + _id);
		}
	}
	
	public String getHistoryRecordRoute(long _id)
	{
		String route = "";
		try
		{
			String query = "Select route from " + ROUTE_TABLE_NAME + " where _id=" + _id;
			Cursor cursor = db.rawQuery(query, null);
			if (cursor != null)
			{
				if(cursor.moveToNext()){
					route =  cursor.getString(0);
				}
			}
			
			cursor.close();
		}
		catch(Exception ex)
		{
			Log.e(TAG, "failed to get route");
		}
		return route;
	}
	
	public HistoryRoute getHistoryRecord(long _id)
	{
		HistoryRoute route = null;
		
		try
		{
			String query = "Select * from " + ROUTE_TABLE_NAME + " where _id=" + _id;
			Cursor cursor = db.rawQuery(query, null);
			if (cursor != null)
			{
				while(cursor.moveToNext()){
					route = new HistoryRoute();
					route.id = cursor.getLong(0);
					route.startDate = cursor.getString(1);
					route.startDateTime = cursor.getLong(2);
					route.endDateTime = cursor.getLong(3);
					route.routelength = cursor.getInt(4);
					route.usedtime = cursor.getInt(5);
					route.steps = cursor.getInt(6);
					route.route = cursor.getString(7);
					route.offsetX = cursor.getInt(8);
					route.offsetY = cursor.getInt(9);
					break;
				}
			}
			
			cursor.close();
		}
		catch(Exception ex)
		{
			Log.e(TAG, "failed to get data");
		}
		return route;
	}
	
	public List<HistoryRoute> getHistoryRecords(int index,int count)
	{
		List<HistoryRoute> data =  new ArrayList<HistoryRoute>();
		
		try
		{
			String query = "Select * from " + ROUTE_TABLE_NAME + " order by _id desc LIMIT " + index + "," + count;
			Cursor cursor = db.rawQuery(query, null);
			if (cursor != null)
			{
				while(cursor.moveToNext()){
					HistoryRoute route = new HistoryRoute();
					route.id = cursor.getLong(0);
					route.startDate = cursor.getString(1);
					route.startDateTime = cursor.getLong(2);
					route.endDateTime = cursor.getLong(3);
					route.routelength = cursor.getInt(4);
					route.usedtime = cursor.getInt(5);
					route.steps = cursor.getInt(6);
					route.route = cursor.getString(7);
					route.offsetX = cursor.getInt(8);
					route.offsetY = cursor.getInt(9);
					data.add(route);
				}
			}
			
			cursor.close();
		}
		catch(Exception ex)
		{
			Log.e(TAG, "failed to get data");
		}
		return data;
	}
	
	public void updateHistoryRecords(long _id,String[] columns,Object... values)
	{
		if (columns == null || values == null || columns.length != values.length)
			return;
		
		try
		{
			String sql = "update " + ROUTE_TABLE_NAME;
			for(int i=0;i<columns.length;i++)
			{
				if (values[i] instanceof String)
					sql = sql + " set " +columns[i] + "='" + values[i] + "' ";
				else
					sql = sql + " set " +columns[i] + "=" + values[i] + " ";
			}
			
			db.execSQL(sql + " where _id=" + _id);
		}
		catch(Exception ex)
		{
			Log.e(TAG, "failed to update " + _id);
		}
	}
	
	public long countHistoryRecords()
	{
		long count = 0;
		try
		{	
			Cursor cursor = db.rawQuery("select count(*) from " + ROUTE_TABLE_NAME, null);
			if (cursor != null)
			{
				if(cursor.moveToNext()){
					count = cursor.getLong(0);
				}
			}
			cursor.close();
		}
		catch(Exception ex)
		{
			Log.e(TAG, "failed to count");
		}
		
		return count;
	}
}
