package com.matt.rn.android.hikvideo;

import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.JavaScriptModule;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.ViewManager;
import com.hikvision.open.hikvideoplayer.HikVideoPlayerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class HkplayerViewPackage implements ReactPackage {

    static {
        HikVideoPlayerFactory.initLib(null, true); // 初始化海康威视SDK
    }

    @Override
    public List<NativeModule> createNativeModules(ReactApplicationContext reactContext) {
        return Collections.emptyList();
    }

    public List<Class<? extends JavaScriptModule>> createJSModules() {
        return Collections.emptyList();
    }

    @Override
    public List<ViewManager> createViewManagers(ReactApplicationContext reactContext) {
        return Arrays.<ViewManager>asList(
                new HkplayerViewManager(),
                new HkplayerPlayBackViewManager()
        );
    }
}
