package com.colobu.flyrunner.utils;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.Date;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.util.Log;

public class AppUtil
{
	public static void sendEamil(Context context,String address,String title,String body)
	{
		Intent intent = new Intent(android.content.Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(android.content.Intent.EXTRA_EMAIL, body); //设置收件人
        intent.putExtra(android.content.Intent.EXTRA_TITLE, body); 
        intent.putExtra(android.content.Intent.EXTRA_TEXT, title); //设置内容
        context.startActivity(Intent.createChooser(intent,"请选择Email发送程序"));
	}
	
	public static String getErrorInfo(Throwable ex)
	{
		String info = "";
		ByteArrayOutputStream baos = null;
		PrintStream printStream = null;
		try
		{
			baos = new ByteArrayOutputStream();
			printStream = new PrintStream(baos);
			
			printStream.println("-----------ERROR Report----------------------");
			printStream.println(new Date().toLocaleString());
			//mobile info
			printStream.println(AppUtil.getMobileInfo());
			
			ex.printStackTrace(printStream);
			
			byte[] data = baos.toByteArray();
			info = new String(data);
			data = null;
		}
		catch (Exception e)
		{
			Log.e("getErrorInfo", e.getMessage());
		}
		finally
		{
			try
			{
				if (printStream != null)
				{
					printStream.close();
				}
				if (baos != null)
				{
					baos.close();
				}
			}
			catch (Exception e)
			{
				Log.e("getErrorInfo", e.getMessage());
			}
		}
		
		return info;
	}
	
	public static String getMobileInfo() {  
        StringBuffer sb = new StringBuffer();  
        //通过反射获取系统的硬件信息   
        try {  
  
            Field[] fields = Build.class.getDeclaredFields();  
            for(Field field: fields){  
                //暴力反射 ,获取私有的信息   
                field.setAccessible(true);  
                String name = field.getName();  
                String value = field.get(null).toString();  
                sb.append(name+"="+value);  
                sb.append("\n");  
            }  
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
        return sb.toString();  
    }  
	
	public static int getVerCode(Context context)
	{
		int verCode = -1;
		try
		{
			verCode = context.getPackageManager().getPackageInfo("com.myapp", 0).versionCode;
		}
		catch (NameNotFoundException e)
		{
			Log.e("AppUtil", e.getMessage());
		}
		return verCode;
	}

	public static String getVerName(Context context)
	{
		String verName = "";
		try
		{
			verName = context.getPackageManager().getPackageInfo("com.myapp", 0).versionName;
		}
		catch (NameNotFoundException e)
		{
			Log.e("AppUtil", e.getMessage());
		}
		return verName;
	}

}
