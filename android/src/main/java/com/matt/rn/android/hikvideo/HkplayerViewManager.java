package com.matt.android.hikvideo;

import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.annotations.ReactProp;

import java.util.Map;

import androidx.annotation.NonNull;

public class HkplayerViewManager extends SimpleViewManager<HkplayerView> {

    private static final int COMMAND_ID = 1;
    private static final String COMMAND_NAME = "executeCommand";

    @NonNull
    @Override
    public String getName() {
        return "HkplayerView";
    }

    @NonNull
    @Override
    protected HkplayerView createViewInstance(@NonNull ThemedReactContext reactContext) {
        HkplayerView textView = new HkplayerView(reactContext);
        return textView;
    }

    @ReactProp(name = "uri")
    public void setUri(HkplayerView view, String uri) {
        view.setUri(uri);
    }

    @Override
    public Map<String, Integer> getCommandsMap() {
        return MapBuilder.of(
                COMMAND_NAME, COMMAND_ID
        );
    }

    @Override
    public void receiveCommand(HkplayerView view, int commandId, ReadableArray args) {
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
