package com.qubin.downloadmanager;

import android.Manifest;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.widget.Toast;

import com.tbruyelle.rxpermissions2.Permission;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.io.File;

import io.reactivex.functions.Consumer;

/**
 * 类或接口的描述信息
 *
 * @Author:qubin
 * @Theme: 更新
 * @Data:2018/12/17
 * @Describe:
 */
public class DownLoadBuilder {

    private Context context;
    private String url;
    private Boolean isWiFi;
    private String description;
    private String downloadName;

    public DownLoadBuilder(Builder builder) {
        super();
        context = builder.context;
        url = builder.url;
        isWiFi = builder.isWiFi;
        description = builder.description;
        downloadName = builder.downloadName;
        if (isWiFi == null){
            isWiFi = true;
        }
        download(context,url,isWiFi,description,downloadName);
    }



     static final class Builder{

        private Context context;
        private String url;
        private Boolean isWiFi;
        private String description;
        private String downloadName;


        public Builder(Context context){
            this.context = context;

        }

        public Builder addUrl(String url){
            this.url = url;
            return this;
        }

        public Builder isWiFi(Boolean isWiFi){
            this.isWiFi = isWiFi;
            return this;
        }

        public Builder addDscription(String description){
            this.description =description;
            return this;
        }

        public Builder addDownLoadName(String downloadName){
            this.downloadName = downloadName;
            return this;
        }

        public DownLoadBuilder builder(){
            return new DownLoadBuilder(this);
        }

    }

    public static void download(final Context context, final String url, final boolean isWIFI, final String description, final String downloadName){
        RxPermissions rxPermissions = new RxPermissions((FragmentActivity) context);
        if (TextUtils.isEmpty(url)){
            Toast.makeText(context, "下载地址不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M){
            downloadApk(context,url,isWIFI,description,downloadName);
        }else {
            rxPermissions.requestEach(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    .subscribe(new Consumer<Permission>() {
                        @Override
                        public void accept(Permission permission) throws Exception {
                            if (permission.granted){
                                downloadApk(context,url,isWIFI,description,downloadName);
                            }else {
                                Toast.makeText(context, "权限未开启", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });


        }


    }

    private static void downloadApk(Context context, String url, boolean isWIFI, String description, String downloadName){
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));

        //判断是在wifi还是在移动网络下进行下载
        if (isWIFI){
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
        }else {
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE);
        }
        //下载时显示notification
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        //添加描述信息
        request.setDescription(description);
        //file:///storage/emulated/0/Download/downloadName.apk
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, downloadName +".apk");

        request.setMimeType("application/vnd.android.package-archive");
        DownloadManager systemService = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        systemService.enqueue(request);    }


    public static void intallApk(Context context,String downloadName){

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N){
            //6.0及以下安装
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.parse("file:///storage/emulated/0/Download/" + downloadName +".apk"), "application/vnd.android.package-archive");
            //为这个新apk开启一个新的activity栈
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //开始安装
            context.startActivity(intent);

        }else {
            //7.0及以上
            File file= new File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    , "/" + downloadName +".apk");
            //参数1 上下文, 参数2 Provider主机地址 和配置文件中保持一致   参数3  共享的文件
            Uri apkUri =
                    FileProvider.getUriForFile(context, "com.qubin.downloadmanager", file);

            Intent intent = new Intent(Intent.ACTION_VIEW);
            // 由于没有在Activity环境下启动Activity,设置下面的标签
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //添加这一句表示对目标应用临时授权该Uri所代表的文件
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
            context.startActivity(intent);


        }
    }

}
