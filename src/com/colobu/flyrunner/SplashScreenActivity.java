package com.colobu.flyrunner;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.ImageView;

public class SplashScreenActivity extends Activity
{
	private Thread mSplashThread;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		// Splash screen view
		setContentView(R.layout.activity_splash_view);

		final ImageView splashImageView = (ImageView) findViewById(R.id.SplashImageView);
		splashImageView.setBackgroundResource(R.drawable.run);
		final AnimationDrawable frameAnimation = (AnimationDrawable) splashImageView.getBackground();
		splashImageView.post(new Runnable() {
			public void run()
			{
				frameAnimation.start();
			}
		});

		final SplashScreenActivity sPlashScreen = this;

		// The thread to wait for splash screen events
		mSplashThread = new Thread() {
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

				// Run next activity
				Intent intent = new Intent(sPlashScreen, HelpImageActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				finish();
			}
		};

		mSplashThread.start();

	}

	/**
	 * Processes splash screen touch events
	 */
	@Override
	public boolean onTouchEvent(MotionEvent evt)
	{
		if (evt.getAction() == MotionEvent.ACTION_DOWN)
		{
			synchronized (mSplashThread)
			{
				mSplashThread.notifyAll();
			}
		}
		return true;
	}
}
