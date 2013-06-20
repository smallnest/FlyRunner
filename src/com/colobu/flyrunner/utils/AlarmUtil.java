package com.colobu.flyrunner.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;

import com.colobu.flyrunner.R;

public class AlarmUtil
{
	public static boolean sentAlarm = false;
	
	public static void showAlarmSound(final Context context,final int id,CharSequence tickerText)
	{
		if (sentAlarm) //已经发送了Alarm
			return;
		
		NotificationManager mgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification nt = new Notification(R.drawable.ic_launcher, tickerText, System.currentTimeMillis());
		nt.defaults = Notification.DEFAULT_ALL;
		//nt.audioStreamType= android.media.AudioManager.ADJUST_LOWER;
		
		//PendingIntent contentIntent = PendingIntent.getActivity(context, 0, new Intent(context,BaiduMapViewActivity.class), 0);
		
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, null, 0);
		// Set the info for the views that show in the notification panel.
		nt.setLatestEventInfo(context, context.getText(R.string.app_name), tickerText, contentIntent);

		
		mgr.notify(id, nt);
		sentAlarm = true;
	}
}
