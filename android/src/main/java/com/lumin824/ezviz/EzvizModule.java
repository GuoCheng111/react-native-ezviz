package com.lumin824.ezviz;

import android.app.Application;
import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.videogo.exception.BaseException;
import com.videogo.openapi.EZOpenSDK;
import com.videogo.openapi.bean.EZCameraInfo;
import com.videogo.openapi.bean.EZDeviceInfo;
import com.videogo.openapi.EZConstants.EZPTZCommand;
import com.videogo.openapi.EZConstants.EZPTZAction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;


public class EzvizModule extends ReactContextBaseJavaModule {
  private static final String TAG = EzvizModule.class.getSimpleName();
  private static String APP_KEY = BuildConfig.APP_KEY;
  private static ReactApplicationContext mContext;
  private boolean mInit = false;

  public EzvizModule(ReactApplicationContext reactContext) {
    super(reactContext);
  }

  @Override
  public String getName(){
    return "EzvizModule";
  }

  @Override
  public void initialize() {
    super.initialize();
    mContext = getReactApplicationContext();
    Application application = (Application) mContext.getBaseContext();

    EZOpenSDK.initLib(application, APP_KEY,"");
    EZOpenSDK.showSDKLog(true);
    // 设置是否支持P2P取流,详见api
    EZOpenSDK.enableP2P(true);
    // APP_KEY请替换成自己申请的
    if (!EZOpenSDK.initLib(application, APP_KEY)) {
      Log.d(TAG, "EZOpenSDK.initLib error");
      return;
    }
    mInit = true;
  }

  @Override
  public void onCatalystInstanceDestroy() {
    super.onCatalystInstanceDestroy();
    EZOpenSDK.finiLib();
  }

  static void sendEvent(String eventName, @Nullable WritableMap params) {
    mContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
            .emit(eventName, params);
  }

  @ReactMethod
  public void setAccessToken(String accessToken, Promise promise){
    if(!mInit)
      promise.reject("INIT_ERROR", "init error");
    EZOpenSDK.getInstance().setAccessToken(accessToken);
    promise.resolve(null);
  }

  @ReactMethod
  public void getCameraList(Promise promise){

  }

  @ReactMethod
  public void getDeviceInfo(String cameraId, Promise promise){

  }

  @ReactMethod
  public void getDeviceList(Promise promise){

  }

  @ReactMethod
  public void controlPTZ(String cameraId, String command, String action, int speed, Promise promise){

  }

  @Nullable
  @Override
  public Map<String, Object> getConstants() {
    final Map<String, Object> constants = new HashMap<>();
    constants.put("EZPTZAction_EZPTZActionSTART", EZPTZAction.EZPTZActionSTART.name());
    constants.put("EZPTZAction_EZPTZActionSTOP", EZPTZAction.EZPTZActionSTOP.name());
    constants.put("EZPTZCommand_EZPTZCommandLeft", EZPTZCommand.EZPTZCommandLeft.name());
    constants.put("EZPTZCommand_EZPTZCommandRight", EZPTZCommand.EZPTZCommandRight.name());
    constants.put("EZPTZCommand_EZPTZCommandUp", EZPTZCommand.EZPTZCommandUp.name());
    constants.put("EZPTZCommand_EZPTZCommandDown", EZPTZCommand.EZPTZCommandDown.name());
    return constants;
  }
}
