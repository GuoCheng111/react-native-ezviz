package com.lumin824.ezviz;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.uimanager.ThemedReactContext;
import com.videogo.errorlayer.ErrorInfo;
import com.videogo.exception.BaseException;
import com.videogo.openapi.EZConstants;
import com.videogo.openapi.EZOpenSDK;
import com.videogo.openapi.EZPlayer;
import com.videogo.openapi.bean.EZCameraInfo;
import com.videogo.openapi.bean.EZDeviceInfo;
import com.videogo.util.LogUtil;

import static com.videogo.openapi.EZConstants.MSG_VIDEO_SIZE_CHANGED;

/**
 * Created by lumin on 16/7/2.
 */
public class EzvizView extends SurfaceView implements Handler.Callback{
    private static final String TAG = EzvizView.class.getSimpleName();

    private EZPlayer mEzPlayer;
    private Handler mHandler = null;
    private EZDeviceInfo mDeviceInfo = null;
    private EZCameraInfo mCameraInfo = null;

    private DeviceEventManagerModule.RCTDeviceEventEmitter mJSModule = null;

    public EzvizView(ThemedReactContext context) {
        super(context);
        mHandler = new Handler(this);
        mJSModule = context.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class);
    }

    private class GetDeviceInfoistTask extends AsyncTask<String, Void, EZDeviceInfo> {
        private int mErrorCode = 0;
        private String deviceSerial;

        public GetDeviceInfoistTask(String serial) {
            deviceSerial = serial;
        }

        @Override
        protected EZDeviceInfo doInBackground(String... params) {
            try {
                EZDeviceInfo deviceInfo = EZOpenSDK.getInstance().getDeviceInfo(deviceSerial);

                return deviceInfo;
            } catch (BaseException e) {
                ErrorInfo errorInfo = (ErrorInfo) e.getObject();
                mErrorCode = errorInfo.errorCode;
                LogUtil.d(TAG, errorInfo.toString());

                return null;
            }
        }

        @Override
        protected void onPostExecute(EZDeviceInfo result) {
            if(result == null){
                Log.d(TAG, "获取设备信息失败，请检查设备序列号是否正确！");
            }else{
                mDeviceInfo = result;
                mCameraInfo = mDeviceInfo.getCameraInfoList().get(0);

                EZOpenSDK ezOpenSDK = EZOpenSDK.getInstance();
                mEzPlayer = ezOpenSDK.createPlayer(mCameraInfo.getDeviceSerial(), mCameraInfo.getCameraNo());

                mEzPlayer.setHandler(mHandler);
                mEzPlayer.setSurfaceHold(getHolder());
                mEzPlayer.startRealPlay();
            }
        }
    }

    public void setDeviceSerial(String deviceSerial){
        if(mEzPlayer != null){
            mEzPlayer.stopRealPlay();
            mEzPlayer = null;
        }

        if(deviceSerial != null) {
            new GetDeviceInfoistTask(deviceSerial).execute();
        }
    }

    public void stopRealPlay(){
        if(mEzPlayer != null) {
            mEzPlayer.stopRealPlay();
        }
    }

    @SuppressLint("NewApi")
    @Override
    public boolean handleMessage(Message msg) {
        LogUtil.i(TAG, "handleMessage:" + msg.what);
        WritableMap params = new WritableNativeMap();
        switch (msg.what) {
            case MSG_VIDEO_SIZE_CHANGED:
                break;
            case EZConstants.EZRealPlayConstants.MSG_REALPLAY_CONNECTION_START:
                break;
            case EZConstants.EZRealPlayConstants.MSG_REALPLAY_PLAY_START:
                params.putString("type", "MSG_REALPLAY_PLAY_START");
                mJSModule.emit("EzvizPlayEvent", params);
                break;
            case EZConstants.EZRealPlayConstants.MSG_REALPLAY_PLAY_SUCCESS:
                params.putString("type", "MSG_REALPLAY_PLAY_SUCCESS");
                mJSModule.emit("EzvizPlayEvent", params);
                break;
            case EZConstants.EZRealPlayConstants.MSG_REALPLAY_PLAY_FAIL:
                ErrorInfo errorInfo = (ErrorInfo)msg.obj;
                params.putString("type", "MSG_REALPLAY_PLAY_FAIL");
                params.putInt("errorCode", errorInfo.errorCode);
                params.putString("description", errorInfo.description);
                mJSModule.emit("EzvizPlayEvent", params);
                break;
            default:
                break;
        }
        return false;
    }
}
