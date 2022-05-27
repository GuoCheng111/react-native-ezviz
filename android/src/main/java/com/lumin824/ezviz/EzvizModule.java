package com.lumin824.ezviz;

import android.app.Application;
import android.os.AsyncTask;
import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.videogo.errorlayer.ErrorInfo;
import com.videogo.exception.BaseException;
import com.videogo.openapi.EZConstants;
import com.videogo.openapi.EZOpenSDK;
import com.videogo.openapi.bean.EZCameraInfo;
import com.videogo.openapi.bean.EZDeviceInfo;
import com.videogo.openapi.EZConstants.EZPTZCommand;
import com.videogo.openapi.EZConstants.EZPTZAction;
import com.videogo.util.LogUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;


public class EzvizModule extends ReactContextBaseJavaModule {
    private static final String TAG = EzvizModule.class.getSimpleName();
    private static ReactApplicationContext mContext;
    private boolean mInit = false;
    private EZDeviceInfo mDeviceInfo = null;
    private String mDeviceSerial = null;

    public EzvizModule(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    private class GetDeviceInfoistTask extends AsyncTask<String, Void, EZDeviceInfo> {
        private Promise mPromise = null;
        private String deviceSerial;

        public GetDeviceInfoistTask(String serial, Promise promise) {
            deviceSerial = serial;
            mPromise = promise;
        }

        @Override
        protected EZDeviceInfo doInBackground(String... params) {
            try {
                EZDeviceInfo deviceInfo = EZOpenSDK.getInstance().getDeviceInfo(deviceSerial);
                return deviceInfo;
            } catch (BaseException e) {
                ErrorInfo errorInfo = (ErrorInfo) e.getObject();

                mPromise.reject(String.valueOf(errorInfo.errorCode), errorInfo.description);

                LogUtil.d(TAG, errorInfo.toString());

                return null;
            }
        }

        @Override
        protected void onPostExecute(EZDeviceInfo result) {
            if (result == null) {
                Log.d(TAG, "获取设备信息失败，请检查设备序列号是否正确");
            } else {
                mDeviceSerial = deviceSerial;
                mDeviceInfo = result;

                mPromise.resolve(prepareDeviceInfo());
            }
        }
    }

    private WritableMap prepareDeviceInfo() {
        if (mDeviceInfo == null)
            return null;
        WritableMap info = new WritableNativeMap();
        info.putBoolean("isEncrypt", mDeviceInfo.getIsEncrypt() == 0);//设备是否加密 1加密 0 未加密
        info.putBoolean("isOnline", mDeviceInfo.getStatus() == 1);//设备是否在线 1-在线，2-不在线

        info.putBoolean("isSupportTalk", mDeviceInfo.isSupportTalk() != EZConstants.EZTalkbackCapability.EZTalkbackNoSupport);//是否支持对讲模式类型
        info.putBoolean("isSupportZoom", mDeviceInfo.isSupportZoom());//是否支持光学缩放(镜头拉近放远
        info.putBoolean("isSupportPTZ", mDeviceInfo.isSupportPTZ());//是否支持云台控制
        info.putBoolean("isSupportAudioOnOff", mDeviceInfo.isSupportAudioOnOff());//是否声音开关设置

        return info;
    }

    @Override
    public String getName() {
        return "EzvizModule";
    }

    @Override
    public void initialize() {
        super.initialize();
        mContext = getReactApplicationContext();
    }

    @Override
    public void onCatalystInstanceDestroy() {
        super.onCatalystInstanceDestroy();
//        if (mInit)
//            EZOpenSDK.finiLib();
    }

    static void sendEvent(String eventName, @Nullable WritableMap params) {
        mContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(eventName, params);
    }

    @ReactMethod
    public void init(String appKey, Promise promise) {
        Log.d(TAG, "init! appKey : " + appKey);
        Application application = (Application) mContext.getBaseContext();

        if( EZOpenSDK.getInstance() == null){
            EZOpenSDK.showSDKLog(true);
            // 设置是否支持P2P取流,详见api
            EZOpenSDK.enableP2P(true);
            // APP_KEY请替换成自己申请
            if (!EZOpenSDK.initLib(application, appKey)) {
                Log.d(TAG, "EZOpenSDK.initLib error");
                promise.reject("ERROR_INIT", "EZOpenSDK.initLib error");
                return;
            }
        }

        mInit = true;
        promise.resolve(null);
    }

    @ReactMethod
    public void releaseLib() {
        if(mInit){
            mInit = false;
            EZOpenSDK.finiLib();
        }
    }

    @ReactMethod
    public void setAccessToken(String accessToken, Promise promise) {
        if (!mInit)
            promise.reject("INIT_ERROR", "init error");
        EZOpenSDK.getInstance().setAccessToken(accessToken);
        promise.resolve(null);
    }

    @ReactMethod
    public void getDeviceInfo(String deviceSerial, Promise promise) {
        if (mDeviceSerial == deviceSerial) {
            promise.resolve(prepareDeviceInfo());
        } else {
            new GetDeviceInfoistTask(deviceSerial, promise).execute();
        }
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
