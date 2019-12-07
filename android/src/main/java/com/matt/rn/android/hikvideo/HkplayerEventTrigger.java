package com.matt.rn.android.hikvideo;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import androidx.annotation.Nullable;

public class HkplayerEventTrigger {

    public void sendEvent(ReactContext reactContext, String eventName, @Nullable WritableMap params) {
        reactContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(eventName, params);
    }

    // 发送成功消息
    public void notifyStatusChangedMessage(ReactContext reactContext,
                                           String eventName, HkplayerStatusChangeHandler status) {
        WritableMap event = Arguments.createMap();
        event.putString("uri", status.getUri());
        event.putInt("status", status.getStatus().ordinal());
        event.putBoolean("mPausing", status.ismPausing());
        event.putBoolean("mSoundOpen", status.ismSoundOpen());
        event.putBoolean("mRecording", status.ismRecording());
        event.putBoolean("mTalking", status.ismTalking());
        sendEvent(reactContext, eventName, event);
    }

}
