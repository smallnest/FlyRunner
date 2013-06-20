package com.colobu.flyrunner;

import java.io.File;
import java.io.FileOutputStream;

import android.util.Log;

import com.colobu.flyrunner.utils.AppUtil;

public class UEHandler implements Thread.UncaughtExceptionHandler
{
	private File fileErrorLog;
	FlyRunnerApplication flyrunnerApp;
	
	public UEHandler(FlyRunnerApplication app)
	{
		flyrunnerApp = app;
		fileErrorLog = new File(FlyRunnerApplication.PATH_ERROR_LOG);
	}

	public void uncaughtException(Thread thread, Throwable ex)
	{
		// fetch Excpetion Info
		final String info = AppUtil.getErrorInfo(ex);
				
		long threadId = thread.getId();
		Log.d("UEHandler", "Thread.getName()=" + thread.getName() + " id=" + threadId + " state=" + thread.getState());
		Log.d("UEHandler", "Error[" + info + "]");
	
		Log.e("UEHandler", ex.getMessage(), ex);
		
		write2ErrorLog(fileErrorLog, info);
		
		android.os.Process.killProcess(android.os.Process.myPid());
	}

	private void write2ErrorLog(File file, String content)
	{
		FileOutputStream fos = null;
		try
		{
			if (file.exists())
			{
				// 清空之前的记录
				file.delete();
			}
			else
			{
				file.getParentFile().mkdirs();
			}
			file.createNewFile();
			fos = new FileOutputStream(file);
			fos.write(content.getBytes());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if (fos != null)
				{
					fos.close();
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
}