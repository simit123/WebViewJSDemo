package com.example.webviewjsdemo;

import android.content.DialogInterface;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.sql.BatchUpdateException;
import java.util.HashMap;
import java.util.Set;

/**
 * webView和js交互
 * android 调js
 * 1.通过WebView的loadUrl（）
 * 2.通过WebView的evaluateJavascript（）
 *
 * js 调 Android
 * 1.通过WebView的addJavascriptInterface（）进行对象映射
 * 2.通过 WebViewClient 的shouldOverrideUrlLoading ()方法回调拦截 url
 * 3.通过 WebChromeClient 的onJsAlert()、onJsConfirm()、onJsPrompt（）方法回调拦截JS对话框alert()、confirm()、prompt（） 消息
 */
public class WebViewJSActivity extends AppCompatActivity {

    private FrameLayout webContent;
    private WebView mWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view_js);
        initView();
    }

    @Override
    protected void onResume() {
        mWebView.onResume();
        super.onResume();
    }

    @Override
    protected void onPause() {
        mWebView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (mWebView != null) {
            mWebView.loadDataWithBaseURL(null, "", "text/html", "utf-8", null);
            mWebView.clearHistory();
            ((ViewGroup) mWebView.getParent()).removeView(mWebView);
            mWebView.destroy();
            mWebView = null;
        }
        super.onDestroy();
    }

    private void initView() {
        webContent = ((FrameLayout) findViewById(R.id.web_content));
        mWebView = new WebView(getApplicationContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mWebView.setLayoutParams(params);
        webContent.addView(mWebView);
        WebSettings mWebViewSettings = mWebView.getSettings();
        mWebViewSettings.setJavaScriptEnabled(true);
        mWebViewSettings.setJavaScriptCanOpenWindowsAutomatically(true);
//        mWebView.loadUrl("file:///android_asset/js.html");
        mWebView.loadUrl("file:///android_asset/callAndroid");

        /**
         * js 调用 Android 方法1 ：通过WebView的addJavascriptInterface（）进行对象映射,android 方法只要添加@JavascriptInterface注解即可被调用 存在漏洞
         */
        //添加对象映射
        mWebView.addJavascriptInterface(new AndroidToJS(),"demo");


        /**
         * js 调用 Android 方法2：
         * Android通过 WebViewClient 的回调方法shouldOverrideUrlLoading ()拦截 url
         * 解析该 url 的协议
         * 如果检测到是预先约定好的协议，就调用相应方法
         */

        mWebView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                //根据协议的参数，判断是否是所需要的url
                // 一般根据scheme（协议格式） & authority（协议名）判断（前两个参数）
                //假定传入进来的 url = "js://webview?arg1=111&arg2=222"（同时也是约定好的需要拦截的）
                Uri uri = request.getUrl();
                //如果协议格式相等则判断协议名
                if (uri.getScheme().equals("js")) {
                    //协议名相等，则此协议为约定好的协议
                    if (uri.getAuthority().equals("webview")) {
                        // 这里可以调用android的方法
                        System.out.println("js调用了Android的方法");
                        // 可以在协议上带有参数并传递到Android上
                        HashMap<String, String> params = new HashMap<>();
                        Set<String> collection = uri.getQueryParameterNames();

                    }
                    return true;
                }
                return super.shouldOverrideUrlLoading(view, request);
            }
        });


        /**
         * js 调用 Android 方法3：
         * 通过 WebChromeClient 的onJsAlert()、onJsConfirm()、onJsPrompt（）方法回调拦截JS对话框alert()、confirm()、prompt（） 消息，消息中带有js中定义的协议，同方式2一样
         */
        //拦截js中的alert确认js中的callJS()方法是否调用成功
        mWebView.setWebChromeClient(new WebChromeClient(){
            @Override
            public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
                AlertDialog.Builder builder = new AlertDialog.Builder(WebViewJSActivity.this);
                builder.setTitle("JS");
                builder.setMessage(message);
                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        result.confirm();
                    }
                });
                builder.setCancelable(false);
                builder.show();
                return true;
            }
        });
    }

    //点击调用js方法
    public void onClick(View view) {
        /**
         * 1.Android 调用js 的第一种方式 通过webView的loadUrl()并且传入js方法名 格式：javascript:***（方法名）获取返回值麻烦
         *
         *
         *   mWebView.post(new Runnable() {
         *             @Override
         *             public void run() {
         *                 //方法名要和js中的方法名保持一致
         *                 mWebView.loadUrl("javascript:callJS()");
         *             }
         *         });
         *
         */

        /**
         * 2.Android 调用 js 的第二种方式 mWebView.evaluateJavascript方法 Android 4.4 以下不可用 可获取返回值
         */
        mWebView.evaluateJavascript("javascript:callJS()", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                //js的返回结果
            }
        });
    }
}
