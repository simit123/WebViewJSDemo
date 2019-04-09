package com.example.webviewjsdemo;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.http.SslError;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toolbar;

/**
 * webView 的基本用法和一些基础配置
 */
public class MainActivity extends AppCompatActivity {

    private FrameLayout webContent;
    private WebView webView;
    private  WebSettings mWebSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        loadWebView();
        initWebSetting();
        clearCacheData();
        offLineLoad();
        initWebViewClient();
        initWebChromeClient();
        /**
         * 避免WebView内存泄漏
         * 1.不在xml中定义 Webview ，而是在需要的时候在Activity中创建，并且Context使用 getApplicationgContext()
         *
         * LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
         *         mWebView = new WebView(getApplicationContext());
         *         mWebView.setLayoutParams(params);
         *         mLayout.addView(mWebView);
         *
         * 2.在 Activity 销毁（ WebView ）的时候，先让 WebView 加载null内容，然后移除 WebView，再销毁 WebView，最后置空。
         * @Override
         *     protected void onDestroy() {
         *         if (mWebView != null) {
         *             mWebView.loadDataWithBaseURL(null, "", "text/html", "utf-8", null);
         *             mWebView.clearHistory();
         *             ((ViewGroup) mWebView.getParent()).removeView(mWebView);
         *             mWebView.destroy();
         *             mWebView = null;
         *         }
         *         super.onDestroy();
         *     }
         */
    }

    /**
     * WebChromeClient的作用
     * 1.网页加载进度条
     * 2.拦截js弹窗
     */
    private void initWebChromeClient() {

        webView.setWebChromeClient(new WebChromeClient(){

            //获取网页加载进度
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
            }

            //获取webView中的标题
            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
            }

            //支持js警告框
            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                return super.onJsAlert(view, url, message, result);
            }

            //支持js确认框
            @Override
            public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
                return super.onJsConfirm(view, url, message, result);
            }

            //支持js输入框
            @Override
            public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
                return super.onJsPrompt(view, url, message, defaultValue, result);
            }
        });



    }

    /**
     * WebViewClient类的作用
     * shouldOverrideUrlLoading
     * 页面加载loading处理
     * 绕过https证书等
     */
    private void initWebViewClient() {

        webView.setWebViewClient(new WebViewClient(){

            //复写shouldOverrideUrlLoading()方法，使得打开网页时不调用系统浏览器， 而是在本WebView中显示
            //注意7.0版本下适配问题
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    view.loadUrl(request.getUrl().toString());
                } else {
                    view.loadUrl(request.toString());
                }
                return true;
            }

            //页面开始加载
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }

            //页面加载结束
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
            }

            //加载页面资源时调用，比如加载一张图片
            @Override
            public void onLoadResource(WebView view, String url) {
                super.onLoadResource(view, url);
            }

            //加载页面的服务器出现错误 404等
            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
            }

            //处理https请求，webView不支持Https请求 页面空白显示
            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed();//此处理可绕过Https证书认证
            }
        });

    }

    /**
     * 问题6.设置webView离线加载
     *
     * 没网时加载本地数据
     */
    private void offLineLoad() {
        if (NetUtils.isNetworkConnected(getApplicationContext())) {
            mWebSettings.setCacheMode(WebSettings.LOAD_DEFAULT);//根据cache-control决定是否从网络上取数据。
        } else {
            mWebSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);//没网，则从本地获取，即离线加载
        }

        mWebSettings.setDomStorageEnabled(true); // 开启 DOM storage API 功能
        mWebSettings.setDatabaseEnabled(true);   //开启 database storage API 功能
        mWebSettings.setAppCacheEnabled(true);//开启 Application Caches 功能

        String cacheDirPath = getFilesDir().getAbsolutePath() + "APP_CACAHE_DIRNAME";
        mWebSettings.setAppCachePath(cacheDirPath); //设置  Application Caches 缓存目录

    }

    /**
     * 问题4.webView清除缓存数据
     */
    private void clearCacheData() {
        //清除网页访问留下的缓存，整个app访问网页留下的缓存都会被清除
        webView.clearCache(true);
        //清除当前webView访问的历史记录
        webView.clearHistory();
        //仅仅清除自动完成填充的表单数据，并不会清除WebView存储到本地的数据
        webView.clearFormData();
    }

    /**
     * 问题5.webViewSettings的配置
     */
    private void initWebSetting() {
        mWebSettings = webView.getSettings();

        //是否支持javaScript，需要webView与js交互则必须设置
        //注意：如果加载的 html 里有JS 在执行动画等操作，会造成资源浪费（CPU、电量）
        // 在onStop 和 onResume 里分别把 setJavaScriptEnabled() 给设置成 false 和 true 即可
        mWebSettings.setJavaScriptEnabled(true);

        //设置屏幕自适应
        mWebSettings.setUseWideViewPort(true);//将图片调整到适合webView的大小
        mWebSettings.setLoadWithOverviewMode(true);//缩放至屏幕大小

        //缩放操作
        mWebSettings.setSupportZoom(true);//支持缩放 默认为true
        mWebSettings.setBuiltInZoomControls(true);//设置内置的缩放控件，若为false则webView不可缩放
        mWebSettings.setDisplayZoomControls(false);//隐藏原生的缩放控件


        mWebSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);//设置缓存模式
//        LOAD_CACHE_ONLY： 不使用网络，只读取本地缓存数据，
//        LOAD_DEFAULT：根据cache-control决定是否从网络上取数据，
//        LOAD_NO_CACHE: 不使用缓存，只从网络获取数据，
//        LOAD_CACHE_ELSE_NETWORK，只要本地有，无论是否过期，或者no-cache，都使用缓存中的数据，本地没有则从网络获取。



        mWebSettings.setAllowFileAccess(true);//设置可以访问文件
        mWebSettings.setJavaScriptCanOpenWindowsAutomatically(true);//通过js打开新的窗口
        mWebSettings.setLoadsImagesAutomatically(true);//支持自动加载图片
        mWebSettings.setDefaultTextEncodingName("utf-8");//设置编码格式

        mWebSettings.setDomStorageEnabled(true);//使用DOM存储，客户端保存信息的一种方式
    }

    private void loadWebView() {
        /**
         * 问题1.webView的加载
         */
        //加载网页
        webView.loadUrl("http://www.baidu.com/");
        //加载本地H5
//        webView.loadUrl("file:///android_asset/js.html");
    }

    private void initView() {
        webContent = ((FrameLayout) findViewById(R.id.web_content));
        webView = new WebView(getApplicationContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        webView.setLayoutParams(params);
        webContent.addView(webView);
        /**
         * 问题2.webView的生命周期管理
         */
        //webView生命周期的管理，原则上要跟随Activity的生命周期
        /**
         *     webView.onResume();
         *     webView.onPause();
         *     //销毁Webview时，如果创建webView时传入的是Activity的context对象可能造成内存泄漏，最好先从父容器移除再进行销毁
         *     webContent.removeView(webView);
         *     webView.destroy();
         */
    }

    /**
     * 问题3.webViw的网页回退
     * 拦截Activity的onKeyDown事件并交由webView处理
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
            webView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
