package com.example.webviewjsdemo;

import android.content.Context;
import android.net.ConnectivityManager;

public class NetUtils {

    /**
     * 检查是否有可用网络
     */
    public static boolean isNetworkConnected(Context mContext) {
        ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        assert connectivityManager != null;
        return connectivityManager.getActiveNetworkInfo() != null;
    }
}
