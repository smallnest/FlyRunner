package com.colobu.flyrunner;

import android.app.Application;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.MKEvent;
import com.baidu.mapapi.MKGeneralListener;

public class FlyRunnerApplication extends Application
{
	static FlyRunnerApplication flyRunnerApp;
	private UEHandler ueHandler; 
	public static final String PATH_ERROR_LOG = Environment.getExternalStorageDirectory().getAbsolutePath() + "/colobu/flyrunner/error.log";  
	
	// 百度MapAPI的管理类
	BMapManager mBMapMan = null;

	// 授权Key
	// 申请地址：http://dev.baidu.com/wiki/static/imap/key/
	String mStrKey = "9444E14BE642275DD5166E75D8B04654BB16C8A9";
	boolean m_bKeyRight = true; // 授权Key正确，验证通过

	// 常用事件监听，用来处理通常的网络错误，授权验证错误等
	static class MyGeneralListener implements MKGeneralListener
	{

		public void onGetNetworkState(int iError)
		{
			Log.d("MyGeneralListener", "onGetNetworkState error is " + iError);
			//Toast.makeText(FlyRunnerApplication.flyRunnerApp.getApplicationContext(), "您的网络出错啦！", Toast.LENGTH_LONG).show();
		}

		public void onGetPermissionState(int iError)
		{
			Log.d("MyGeneralListener", "onGetPermissionState error is " + iError);
			if (iError == MKEvent.ERROR_PERMISSION_DENIED)
			{
				// 授权Key错误：
				Toast.makeText(FlyRunnerApplication.flyRunnerApp.getApplicationContext(),
						"请在BMapApiDemoApp.java文件输入正确的授权Key！", Toast.LENGTH_LONG).show();
				FlyRunnerApplication.flyRunnerApp.m_bKeyRight = false;
			}
		}
	}

	@Override
	public void onCreate()
	{
		ueHandler = new UEHandler(this);  
        // 设置异常处理实例  
        Thread.setDefaultUncaughtExceptionHandler(ueHandler);  
        
		flyRunnerApp = this;
		mBMapMan = new BMapManager(this);
		mBMapMan.init(this.mStrKey, new MyGeneralListener());
		mBMapMan.getLocationManager().setNotifyInternal(10, 5);

		super.onCreate();
	}

	@Override
	// 建议在您app的退出之前调用mapadpi的destroy()函数，避免重复初始化带来的时间消耗
	public void onTerminate()
	{
		if (mBMapMan != null)
		{
			mBMapMan.destroy();
			mBMapMan = null;
		}
		super.onTerminate();
	}
}
