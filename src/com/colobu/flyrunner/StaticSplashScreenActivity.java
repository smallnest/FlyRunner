package com.colobu.flyrunner;

import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.ImageView;

import com.colobu.flyrunner.utils.SharedPreferencesUtils;

public class StaticSplashScreenActivity extends Activity
{
	Thread waitingThread;
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		// Splash screen view
		setContentView(R.layout.activity_static_splash_view);
		ImageView imageView = (ImageView)findViewById(R.id.staticSplashImageView);
		int  i =  new Date().getDay() %5;
		if (i == 0)
			imageView.setBackgroundResource(R.drawable.splash_0);
		else if (i == 1)
			imageView.setBackgroundResource(R.drawable.splash_1);
		else if (i == 2)
			imageView.setBackgroundResource(R.drawable.splash_2);
		else if (i == 3)
			imageView.setBackgroundResource(R.drawable.splash_3);
		else if (i == 4)
			imageView.setBackgroundResource(R.drawable.splash_4);
		
		
		waitingThread = new Thread() {
			@Override
			public void run()
			{
				try
				{
					synchronized (this)
					{
						// Wait given period of time or exit on touch
						wait(5000);
					}
				}
				catch (InterruptedException ex)
				{
				}
				
				finish();
				
				SharedPreferences settings = getSharedPreferences(SharedPreferencesUtils.PREFS_NAME,
						Context.MODE_PRIVATE);
				boolean firstStart = settings.getBoolean(SharedPreferencesUtils.FIRST_START, true);
				
				//TODO
				firstStart = false;
				
				if (firstStart)
				{
					SharedPreferences.Editor editor = settings.edit();
					editor.putBoolean(SharedPreferencesUtils.FIRST_START, false);
					editor.commit();
					
					Intent intent = new Intent(StaticSplashScreenActivity.this, HelpImageActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(intent);
				}
				else
				{
					
					Intent intent = new Intent(StaticSplashScreenActivity.this, FunctionActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(intent);
				}
			}
		};

		waitingThread.start();
	}

	/**
	 * Processes splash screen touch events
	 */
	@Override
	public boolean onTouchEvent(MotionEvent evt)
	{
		if (evt.getAction() == MotionEvent.ACTION_DOWN)
		{
			synchronized (waitingThread)
			{
				waitingThread.notifyAll();
			}
		}
		return true;
	}
}
