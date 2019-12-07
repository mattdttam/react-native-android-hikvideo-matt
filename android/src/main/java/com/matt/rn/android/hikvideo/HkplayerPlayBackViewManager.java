package com.matt.rn.android.hikvideo;

import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.annotations.ReactProp;

import java.util.Map;

import androidx.annotation.NonNull;

public class HkplayerPlayBackViewManager extends SimpleViewManager<HkplayerPlayBackView> {
    private static final int COMMAND_ID = 1;
    private static final String COMMAND_NAME = "executeCommand";

    @NonNull
    @Override
    public String getName() {
        return "HkplayerPlayBackView";
    }

    @NonNull
    @Override
    protected HkplayerPlayBackView createViewInstance(@NonNull ThemedReactContext reactContext) {
        HkplayerPlayBackView textView = new HkplayerPlayBackView(reactContext);
        return textView;
    }

    @ReactProp(name = "uri")
    public void setUri(HkplayerPlayBackView view, String uri) {
        view.setUri(uri);
    }

    @ReactProp(name = "segments")
    public void setSegments(HkplayerPlayBackView view, ReadableArray segments) {
        view.setSegments(segments);
    }

    @Override
    public Map<String, Integer> getCommandsMap() {
        return MapBuilder.of(
                COMMAND_NAME, COMMAND_ID
        );
    }

    @Override
    public void receiveCommand(HkplayerPlayBackView view, int commandId, ReadableArray args) {
        switch (commandId){
            case COMMAND_ID:
                if(args != null) {
                    view.executeCommand(args.getString(0));
                }
            default:
                break;
        }
    }
}
