package com.example.iteq;

import android.app.Application;

import com.baidu.mapapi.CoordType;
import com.baidu.mapapi.SDKInitializer;
import com.tencent.bugly.Bugly;

import org.litepal.LitePal;

import cn.bmob.v3.Bmob;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        LitePal.initialize(this);
        Bugly.init(this, "58e868ed1c", false);
        SDKInitializer.initialize(this);
        SDKInitializer.setCoordType(CoordType.BD09LL);
        Bmob.initialize(this, "27d2ab6ae2ee8589c8c40ee369cfd6cd");
    }
}
