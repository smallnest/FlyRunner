package com.colobu.flyrunner;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;

/**
 * 使用webview显示功能按钮.
 * 使用jquery mobile框架实现，性能超慢，弃用。
 * @deprecated
 */
public class WebMainActivity extends Activity
{
	private WebView webView = null;
	
	@SuppressLint("SetJavaScriptEnabled")
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);  
	    setContentView(R.layout.activity_web_main_view);  
	    webView = (WebView) findViewById(R.id.webview);
	    webView.setBackgroundColor(Color.TRANSPARENT);
	    webView.setHorizontalScrollBarEnabled(false);
	    webView.setVerticalScrollBarEnabled(false);
	    
	    webView.setBackgroundResource(R.drawable.backgroup1);
	    WebSettings ws = webView.getSettings();  
	    ws.setJavaScriptEnabled(true);  
	    //设定JavaScript脚本代码的界面名称是”frWebMain”
	    webView.addJavascriptInterface(this, "frWebMain");
	    
	    webView.loadUrl("file:///android_asset/html/main.html");  
	}
	
	public void startBaiduMap()
	{
		startActivity(new Intent(WebMainActivity.this, BaiduMapViewActivity.class));
	}
	
	public void adjustBaiduMap()
	{
		startActivity(new Intent(WebMainActivity.this, AdjustBaiduMapViewActivity.class));
	}
}
