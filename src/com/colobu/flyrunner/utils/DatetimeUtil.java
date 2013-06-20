package com.colobu.flyrunner.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DatetimeUtil
{
	static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	static SimpleDateFormat sdf2 = new SimpleDateFormat("hh:mm:ss");
	static SimpleDateFormat sdf3 = new SimpleDateFormat("MM-dd hh:mm:ss");
	
	public static long getUTCTime()
	{
		final java.util.Calendar cal = java.util.Calendar.getInstance();   
	    //2、取得时间偏移量：    
	    final int zoneOffset = cal.get(java.util.Calendar.ZONE_OFFSET);   
	    //3、取得夏令时差：    
	    final int dstOffset = cal.get(java.util.Calendar.DST_OFFSET);    
	    System.out.println(dstOffset);  
	    //4、从本地时间里扣除这些差量，即可以取得UTC时间：    
	    cal.add(java.util.Calendar.MILLISECOND, -(zoneOffset + dstOffset));    
	    return cal.getTimeInMillis();  
	}
	
	public static String getDate(Date date)
	{
		return sdf.format(date);
	}
	
	public static String getDate(long date)
	{
		return sdf.format(new Date(date));
	}
	
	public static String getTime(Date date)
	{
		return sdf2.format(date);
	}
	
	public static String getTime(long date)
	{
		return sdf2.format(new Date(date));
	}
	
	public static String getTimeOrDateTime(long date1,long date2)
	{
		Date d1 = new Date(date1);
		Date d2 = new Date(date2);
		
		//同一天内
		if (d1.getYear()== d2.getYear() && d1.getMonth() == d2.getMonth() && d1.getDay() == d2.getDay())
			return getTime(d2);
		else
			return sdf3.format(d2);
	}
	
}
