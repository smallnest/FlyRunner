package com.colobu.flyrunner;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.webkit.WebView;

/**
 * 使用webview显示html帮助文档.
 */
public class HelpHtmlActivity extends Activity
{
	private WebView webView = null;
	
	@SuppressLint("SetJavaScriptEnabled")
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);  
	    setContentView(R.layout.activity_help_html_view);  
	    webView = (WebView) findViewById(R.id.helpHtmlWebview);
	    webView.setBackgroundColor(Color.TRANSPARENT);
	    webView.setHorizontalScrollBarEnabled(true);
	    webView.setVerticalScrollBarEnabled(true);
	    
	    webView.loadUrl("file:///android_asset/help/index.html");  
	}
	
}
