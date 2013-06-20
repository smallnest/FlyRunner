/*
 * Copyright 2011 Sina.
 *
 * Licensed under the Apache License and Weibo License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.open.weibo.com
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.colobu.flyrunner.weibo;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.colobu.flyrunner.R;
import com.weibo.net.ShareActivity;
import com.weibo.net.Utility;
import com.weibo.net.Weibo;
import com.weibo.net.WeiboException;
import com.weibo.net.WeiboParameters;

/**
 * Sample code for testing weibo APIs.
 * 
 * @author ZhangJie (zhangjie2@staff.sina.com.cn)
 */

public class TestActivity extends Activity {
    TextView mResult;
    Weibo mWeibo = Weibo.getInstance();

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.timeline);
        
        mResult = (TextView) this.findViewById(R.id.tvResult);
        Button getShare = (Button) this.findViewById(R.id.btnShare);
        getShare.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {

                File file = Environment.getExternalStorageDirectory();
                String sdPath = file.getAbsolutePath();
                // 请保证SD卡根目录下有这张图片文件
                String picPath = sdPath + "/" + "abc.jpg";
                File picFile = new File(picPath);
                if (!picFile.exists()) {
                    Toast.makeText(TestActivity.this, "图片" + picPath + "不存在！", Toast.LENGTH_SHORT)
                            .show();
                    picPath = null;
                }
                try {
                    share2weibo("abc", picPath);
                    Intent i = new Intent(TestActivity.this, ShareActivity.class);
                    TestActivity.this.startActivity(i);

                } catch (WeiboException e) {
                    e.printStackTrace();
                } finally {

                }

            }

        });
    }

    private void share2weibo(String content, String picPath) throws WeiboException {
        Weibo weibo = Weibo.getInstance();
        weibo.share2weibo(this, weibo.getAccessToken().getToken(), weibo.getAccessToken()
                .getSecret(), content, picPath);
    }

    private String getPublicTimeline(Weibo weibo) throws MalformedURLException, IOException,
            WeiboException {
        String url = Weibo.SERVER + "statuses/public_timeline.json";
        WeiboParameters bundle = new WeiboParameters();
        bundle.add("source", Weibo.getAppKey());
        String rlt = weibo.request(this, url, bundle, "GET", mWeibo.getAccessToken());
        return rlt;
    }

    private String upload(Weibo weibo, String source, String file, String status, String lon,
            String lat) throws WeiboException {
        WeiboParameters bundle = new WeiboParameters();
        bundle.add("source", source);
        bundle.add("pic", file);
        bundle.add("status", status);
        if (!TextUtils.isEmpty(lon)) {
            bundle.add("lon", lon);
        }
        if (!TextUtils.isEmpty(lat)) {
            bundle.add("lat", lat);
        }
        String rlt = "";
        String url = Weibo.SERVER + "statuses/upload.json";
        try {
            rlt = weibo
                    .request(this, url, bundle, Utility.HTTPMETHOD_POST, mWeibo.getAccessToken());
        } catch (WeiboException e) {
            throw new WeiboException(e);
        }
        return rlt;
    }

    private String update(Weibo weibo, String source, String status, String lon, String lat)
            throws WeiboException {
        WeiboParameters bundle = new WeiboParameters();
        bundle.add("source", source);
        bundle.add("status", status);
        if (!TextUtils.isEmpty(lon)) {
            bundle.add("lon", lon);
        }
        if (!TextUtils.isEmpty(lat)) {
            bundle.add("lat", lat);
        }
        String rlt = "";
        String url = Weibo.SERVER + "statuses/update.json";
        rlt = weibo.request(this, url, bundle, Utility.HTTPMETHOD_POST, mWeibo.getAccessToken());
        return rlt;
    }
}
