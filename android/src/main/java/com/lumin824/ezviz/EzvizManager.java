package com.lumin824.ezviz;

import androidx.annotation.Nullable;
import android.util.Log;

import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.annotations.ReactProp;

import java.util.Map;

public class EzvizManager extends SimpleViewManager<EzvizView> {
    private static final String TAG = EzvizManager.class.getSimpleName();
    ;

    public static final int COMMAND_PLAT = 1;
    public static final int COMMAND_STOP = 2;
    public static final int COMMAND_OPEN_SOUND = 3;
    public static final int COMMAND_CLOSE_SOUND = 4;
    public static final int COMMAND_START_VOICE_TALK = 5;
    public static final int COMMAND_STOP_VOICE_TALK = 6;
    public static final int COMMAND_CONTROL_PIZ = 7;

    @Override
    public String getName() {
        return "Ezviz";
    }

    @Override
    protected EzvizView createViewInstance(ThemedReactContext reactContext) {
        EzvizView view = new EzvizView(reactContext);
        return view;
    }

    @Override
    public void onDropViewInstance(EzvizView view) {
        super.onDropViewInstance(view);
        view.release();
    }

    @Override
    public Map<String, Integer> getCommandsMap() {
        return MapBuilder.of(
                "play",
                COMMAND_PLAT,
                "stop",
                COMMAND_STOP,
                "openSound",
                COMMAND_OPEN_SOUND,
                "closeSound",
                COMMAND_CLOSE_SOUND,
                "startVoiceTalk",
                COMMAND_START_VOICE_TALK,
                "stopVoiceTalk",
                COMMAND_STOP_VOICE_TALK,
                "controlPIZ",
                COMMAND_CONTROL_PIZ);
    }

    @Override
    public void receiveCommand(EzvizView view, int commandType, @Nullable ReadableArray args) {
        if (view == null) {
            throw new AssertionError();
        }

        Log.d(TAG, "receiveCommand : " + commandType);
        ReadableMap command = args.getMap(0);
        view.executeCommand(command);

        switch (commandType) {
            case COMMAND_PLAT:
                break;
            case COMMAND_STOP:
                break;
            case COMMAND_OPEN_SOUND:
                break;
            case COMMAND_CLOSE_SOUND:
                break;
            case COMMAND_START_VOICE_TALK:
                break;
            case COMMAND_STOP_VOICE_TALK:
                break;
            case COMMAND_CONTROL_PIZ:
                break;
            default:
                break;
        }
    }

    @ReactProp(name = "deviceSerial")
    public void setDeviceSerial(EzvizView view, String deviceSerial) {
        view.setDeviceSerial(deviceSerial);
    }

    @ReactProp(name = "verifyCode")
    public void setVerifyCode(EzvizView view, String verifyCode) {
        view.setVerifyCode(verifyCode);
    }

    @ReactProp(name = "command")
    public void setCommand(EzvizView view, ReadableMap command) {
        view.executeCommand(command);
    }
}
