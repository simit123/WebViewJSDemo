package com.example.webviewjsdemo;

import android.webkit.JavascriptInterface;

public class AndroidToJS extends Object {

    // 定义JS需要调用的方法
    // 被JS调用的方法必须加入@JavascriptInterface注解
    @JavascriptInterface
    public void hello(String msg) {
        System.out.println(msg);
    }
}
