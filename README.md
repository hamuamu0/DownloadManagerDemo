# DownloadManagerDemo
一行代码处理更新那些事儿。用DwonLoadManage封装一个app的更新组件。（兼容android 6，7，8）



### 前言

android app的更新是我们在平时开发的时候常常需要遇到的问题。通常的情况是我们用第三方的网络加载库去进行地址的下载，然后进行更新。例如okHttp、volley等，都具备了下载的功能。

但是我们在用这些第三方库进行下载的时候可能需要做很多之外的处理，比如更新的时候处理进度。写一个notification去提示下载显示，这无疑让我们在编写代码的时候增加了很多不必要的麻烦。其实Android系统他已经自带了一个下载的库，DownloadManage，并且在里面已经帮我们处理了很多事情，我们只需知道他的用法，再做一些封装便可以处理我们日常中绝大多数下载的问题。

使用：

```
implementation 'com.qubin.download:download:1.0.0'
```

那么我们先来讲解一些常见的api用法。

### DownloadManager

首先，下载嘛，当然需要网络权限和文件读取写入权限啦，不然没网络如何下载？下载之后的apk放哪里？于是我们首先在清单文件中添加权限。

```JAVA
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>
    <uses-permission android:name="android.permission.INTERNET"/>
    
```
之后是实例化这个DownloadManager类，并且传入下载的地址。

```
 DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
```

在DownloadManager内部会判断手机所处在的环境是什么，也就是说，我们可以设置是在wifi情况下进行下载还是在移动网络情况下进行下载。

```
request.setAllowedNetworkTypes()
```

- **DownloadManager.Request.NETWORK_WIFI:** 代表在wifi情况下下载

- **DownloadManager.Request.NETWORK_MOBILE:** 代表在移动网络下进行下载

如果设置的是wifi情况下下载，但是切换到了4g网络，那么程序会自动停止，如果这时候再次切换回来，那么又会自动下载，并且还是会自动断点续传。

定制化notification：

在点击进行下载的时候，一般在手机下拉框中会出现一个notification来显示下载进行，DownloadManager在这方面做得很智能，几行代码就可以直接搞定这个复杂的功能。

```JAVA
        //下载时显示notification
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        //添加描述信息
        request.setDescription(description);
```

- VISIBILTY_HIDDEN: Notification:将不会显示，如果设置该属性的话，必须要添加权限。Android.permission.DOWNLOAD_WITHOUT_NOTIFICATION. 
VISIBILITY_VISIBLE： Notification显示，但是只是在下载任务执行的过程中显示，下载完成自动消失。（默认值） 

- VISIBILITY_VISIBLE_NOTIFY_COMPLETED : Notification显示，下载进行时，和完成之后都会显示。 

- VISIBILITY_VISIBLE_NOTIFY_ONLY_COMPLETION ：只有当任务完成时，Notification才会显示。 

之后便可以设置存储地址

```JAVA
        //file:///storage/emulated/0/Download/downloadName.apk
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, downloadName +".apk");
```

最后将请求加入队列，便可以开始进行下载了。

```JAVA
        request.setMimeType("application/vnd.android.package-archive");
        DownloadManager systemService = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        systemService.enqueue(request);
```

这时，就可以看到开始进行了下载：

![](https://note.youdao.com/yws/public/resource/59bb093c694f261c2964721b8dbf7c8b/xmlnote/WEBRESOURCE74cc60920f9fcfe9310c84814e2dfe9f/14255)

那么如何才知道下载完成，来进行安装呢？在DownloadManager内部在下载完成之后会发送一个广播告诉下载完成。**DownloadManager.ACTION_DOWNLOAD_COMPLETE**

于是我们便可以写一个broadcast来进行接收广播，同时处理安装事件。

```JAVA
class DownloadCompleteBroadcast extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)){

                //TODO...
            }
        }
    }
```

### 兼容处理：

在安装的时候就开始体现了版本的差异了，需要开始做兼容。我们在6.0以下版本，可以直接使用以下代码进行安装即可。

```java
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.parse("file:///storage/emulated/0/Download/" + downloadName +".apk"), "application/vnd.android.package-archive");
            //为这个新apk开启一个新的activity栈
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //开始安装
            startActivity(intent);
```

### 6.0兼容：

在6.0时候引入的动态权限问题。也就是说，我们在清单文件设置了权限问题，但是在需要一些比较私密的权限的时候，必须由用户去进行选择，如果不在处理这些权限的时候让用户去选择，那么程序必定会奔溃。这里推荐一个好用的动态权限库，RxPersmissions。这个库运用了RxJava的链式思想，来处理动态权限问题。github地址：[RxPermissions](https://github.com/tbruyelle/RxPermissions)。使用起来也非常简单，直接在进行下载的时候给出权限授权提示即可。

```JAVA
RxPermissions rxPermissions = new RxPermissions(this);
rxPermissions.requestEach(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    .subscribe(new Consumer<Permission>() {
                        @Override
                        public void accept(Permission permission) throws Exception {
                            if (permission.granted){
                                //TODO...
                                
                            }else {
                                Toast.makeText(context, "权限未开启", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
```

### 7.0兼容：

从文档里知道,Android 7 开始增加安全性,文件私有化,而需要共享文件给其他程序,例如APK安装程序,需要通过FileProvider配置共享文件,配置表是基于XML文件实现,然后通过Content URI携带配置文件xml来共享文件.

实现配置FileProvider 需要两步:
第一步: 需要配置AndroidManifest.xml清单.

```
        <provider
            android:name="android.support.v4.content.FileProvider"
            android:authorities="com.qubin.downloadmanager"
            android:exported="false"
            android:grantUriPermissions="true">
            <meta-data
                android:name="android.support.FILE_PROVIDER_PATHS"
                android:resource="@xml/provider_paths" />
        </provider>
```

第二步:建立文件 res/xml/file_paths.xml.

```
<?xml version="1.0" encoding="utf-8"?>  
<resources>  
    <paths>  
        <!--  
        files-path：          该方式提供在应用的内部存储区的文件/子目录的文件。  
                              它对应Context.getFilesDir返回的路径：eg:”/data/data/com.***.***/files”。  

        cache-path：          该方式提供在应用的内部存储区的缓存子目录的文件。  
                              它对应Context.getCacheDir返回的路:eg:“/data/data/com.***.***/cache”；  

        external-path：       该方式提供在外部存储区域根目录下的文件。  
                              它对应Environment.getExternalStorageDirectory返回的路径

        external-files-path:  Context.getExternalFilesDir(null)

        external-cache-path： Context.getExternalCacheDir(String)
        -->  
        <external-path name="external" path="" />  
    </paths>  
</resources>

```

而其中的 path=""是代表根目录,也就是向共享的应用程序共享根目录以及其子目录的任何一个文件.理论上说假如共享程序是恶意程序,那它便可以获取你的应用的所有共享文件信息.

最后准备好上面两步便可以安装文件

```java

            File file= new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "/" + downloadName +".apk");
            //参数1 上下文, 参数2 Provider主机地址 和配置文件中保持一致   参数3  共享的文件
            Uri apkUri = FileProvider.getUriForFile(context, "com.qubin.downloadmanager", file);

            Intent intent = new Intent(Intent.ACTION_VIEW);
            // 由于没有在Activity环境下启动Activity,设置下面的标签
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //添加这一句表示对目标应用临时授权该Uri所代表的文件
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
            startActivity(intent);
```

### 8.0兼容：

Android 8到时有了什么改变以致安装apk的方法有很大改变呢?

在2017年8月29号的谷歌开发者博客中写道 <<在 Android O 中更安全地获取应用>>新的安装未知应用的,Android O 禁用了总是安装未知应用的选择,改为安装未知应用时提出设置的提示,减少恶意应用通过虚假的安装界面欺骗用户行为.
所以开发者需要调整AndroidManifest文件里的权限,增加 REQUEST_INSTALL_PACKAGES权限.

```
<uses-permission android:name="android.permission.REQUEST_INSTALL_PACKAGES" />
```


谷歌建议是通过PackageManager canRequestPackageInstalls() 的API，查询此权限的状态,然后使用使用 ACTION_MANAGE_UNKNOWN_APP_SOURCES Intent 操作。

```java
Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);

startActivityForResult(intent, RESULT_CODE);

```

但是我不建议这样使用,因为使用 ACTION_MANAGE_UNKNOWN_APP_SOURCES Intent 操作后会跳到所有应用列表,然后从众多的应用里选择对应的APP的选择进入再打开权限,这样的用户体验不好。可以直接等到安装的时候点击跳转开发这个权限即可。

### 封装

好了，有了以上的一些操作，之后我利用了builder模式直接进行了一层封装操作，便可以方便我们使用这个下载的方法了。具体的builder写法不难，这里不做过多的说明，直接看代码就能看懂。

另外，我们在使用更新的时候一般来说，会先进行网络请求接口，拿到更新提示文案，弹出一个dialog弹窗，点击下载之后便可以开始下载。这里我也写了一个通用的dialog，通过这个便可以进行操作了。也是利用了builder设计模式。如果对这块不懂，可以参考一下我写的另一篇文章。[动手造轮子——用Builder模式撸一个通用版本的Dialog](https://juejin.im/post/5be41dfaf265da6151143dbd)。


![](https://note.youdao.com/yws/public/resource/59bb093c694f261c2964721b8dbf7c8b/xmlnote/WEBRESOURCEfbc38eefe17e58097f8b5cc2102ec29e/14303)

这里只是写了一个大概的界面，具体的界面操作，可以自己去根据这个demo进行改造。

在我们使用这个dialog：

```java
commonDialog = new CommonDialog.Builder(MainActivity.this)
                        .view(R.layout.dialog) //布局文件
                        .style(R.style.Dialog) //样式透明
                        .setMessage(R.id.txt_sure,"开始更新") //更新按钮文字
                        .setMessage(R.id.txt_cancel,"取消更新") //取消按钮文字
                        .addViewOnClick(R.id.txt_sure, new View.OnClickListener() { //点击开始更新按钮点击事件
                            @Override
                            public void onClick(View v) {

                                Toast.makeText(MainActivity.this, "开始下载", Toast.LENGTH_SHORT).show();
                                commonDialog.dismiss();
                            }
                        })
                        .addViewOnClick(R.id.txt_cancel, new View.OnClickListener() { //取消按钮点击事件
                            @Override
                            public void onClick(View v) {
                                commonDialog.dismiss();
                            }
                        })
                        .build();

                commonDialog.show();
```

在进行更新时，写下一下一行代码便可以开始进行更新了

```java
                                        new DownLoadBuilder.Builder(MainActivity.this)
                                        .addUrl(url)
                                        .isWiFi(true)
                                        .addDownLoadName(apkName)
                                        .addDscription("开始下载")
                                        .builder();
```

是不是觉得很方便？不要忘记下完写一个广播来接收下载完成事件。

所有代码都放到了github上，如果需要使用以上两个方法，只需要将 **DownLoadBuilder** 和 **CommonDialog** 这两个类引入到自己项目中即可操作。

如果觉得可以，欢迎start。

[代码dmeo的github地址](https://github.com/hamuamu0/DownloadManagerDemo)

有兴趣可以关注我的小专栏，学习更多职场产品思考知识：[小专栏](https://xiaozhuanlan.com/goodjob)

![](https://user-gold-cdn.xitu.io/2018/10/31/166c8a201aa27a0d?imageView2/0/w/1280/h/960/format/webp/ignore-error/1)



