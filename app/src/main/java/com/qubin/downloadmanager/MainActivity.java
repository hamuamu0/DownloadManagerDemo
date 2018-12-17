package com.qubin.downloadmanager;

import android.Manifest;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;



public class MainActivity extends AppCompatActivity {

    Button btnDownload;
    String url = "https://downpack.baidu.com/appsearch_AndroidPhone_v8.0.3(1.0.65.172)_1012271b.apk";
    private DownloadCompleteBroadcast downloadCompleteBroadcast;
    private CommonDialog commonDialog;
    private String apkName = "测试";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnDownload = (Button)findViewById(R.id.btn_down);
        downloadCompleteBroadcast = new DownloadCompleteBroadcast();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        registerReceiver(downloadCompleteBroadcast,intentFilter);
        btnDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                commonDialog = new CommonDialog.Builder(MainActivity.this)
                        .view(R.layout.dialog)
                        .style(R.style.Dialog)
                        .setMessage(R.id.txt_sure,"开始更新")
                        .setMessage(R.id.txt_cancel,"取消更新")
                        .addViewOnClick(R.id.txt_sure, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                new DownLoadBuilder.Builder(MainActivity.this)
                                        .addUrl(url)
                                        .isWiFi(true)
                                        .addDownLoadName(apkName)
                                        .addDscription("开始下载")
                                        .builder();
                                Toast.makeText(MainActivity.this, "开始下载", Toast.LENGTH_SHORT).show();
                                commonDialog.dismiss();
                            }
                        })
                        .addViewOnClick(R.id.txt_cancel, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                commonDialog.dismiss();
                            }
                        })
                        .build();

                commonDialog.show();




            }
        });
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(downloadCompleteBroadcast);
    }

    class DownloadCompleteBroadcast extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)){

                DownLoadBuilder.intallApk(MainActivity.this,apkName);
            }
        }
    }
}
