package com.lumin824.ezviz;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReadableMap;
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

public class EzvizView extends ViewGroup {
    private static final String TAG = EzvizView.class.getSimpleName();
    private EZDeviceInfo mDeviceInfo = null;
    private EZCameraInfo mCameraInfo = null;

    private SurfaceViewRender mSurfaceViewRenderer;

    private ThemedReactContext mContext;
    private DeviceEventManagerModule.RCTDeviceEventEmitter mJSModule = null;

    public EzvizView(ThemedReactContext context) {
        super(context);
        mContext = context;

        mSurfaceViewRenderer = new SurfaceViewRender(mContext);
        addView(mSurfaceViewRenderer);
    }

    @Override
    @SuppressLint("DrawAllocation")
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        mSurfaceViewRenderer.layout(0, 0, right - left, bottom - top);
    }

    private class GetDeviceInfoistTask extends AsyncTask<String, Void, EZDeviceInfo> {
        private int mErrorCode = 0;
        private String deviceSerial;

        public GetDeviceInfoistTask(String serial) {
            deviceSerial = serial;
        }

        @Override
        protected EZDeviceInfo doInBackground(String... params) {
            WritableMap msg = new WritableNativeMap();

            try {
                Log.d(TAG, "get device info : " + deviceSerial);
                EZDeviceInfo deviceInfo = EZOpenSDK.getInstance().getDeviceInfo(deviceSerial);
                return deviceInfo;
            } catch (BaseException e) {
                ErrorInfo errorInfo = (ErrorInfo) e.getObject();
                mErrorCode = errorInfo.errorCode;

                msg.putString("type", "MSG_GET_DEVICE_INFO_FAIL");
                msg.putInt("errorCode", errorInfo.errorCode);
                msg.putString("description", errorInfo.description);
                EzvizModule.sendEvent("EzvizPlayEvent", msg);

                LogUtil.d(TAG, errorInfo.toString());

                return null;
            }
        }

        @Override
        protected void onPostExecute(EZDeviceInfo result) {
            if (result == null) {
                Log.d(TAG, "获取设备信息失败，请检查设备序列号是否正确!");
            } else {
                mDeviceInfo = result;
                mCameraInfo = mDeviceInfo.getCameraInfoList().get(0);
                if(mSurfaceViewRenderer != null){
                    mSurfaceViewRenderer.setDeviceInfo(mDeviceInfo);

                    mSurfaceViewRenderer.startRealPlay();
                }
            }
        }
    }

    public void setDeviceSerial(String deviceSerial) {
        if (mSurfaceViewRenderer != null) {
            mSurfaceViewRenderer.stopRealPlay();
        }

        if (deviceSerial != null) {
            new GetDeviceInfoistTask(deviceSerial).execute();
        }
    }

    public void setVerifyCode(String verifyCode) {
        mSurfaceViewRenderer.setVerifyCode(verifyCode);
    }

    public void executeCommand(final ReadableMap cmdObject) {
        Log.d(TAG, "cmdObject : " + cmdObject.toString());

        if (cmdObject.hasKey("type")) {
            switch (cmdObject.getString("type")) {
                case "play":
                    mSurfaceViewRenderer.startRealPlay();
                    break;
                case "stop":
                    mSurfaceViewRenderer.stopRealPlay();
                    break;
                case "openSound":
                    mSurfaceViewRenderer.openSound();
                    break;
                case "closeSound":
                    mSurfaceViewRenderer.closeSound();
                    break;
                case "startVoiceTalk":
                    mSurfaceViewRenderer.startVoiceTalk();
                    break;
                case "stopVoiceTalk":
                    mSurfaceViewRenderer.stopVoiceTalk();
                    break;
                case "controlPTZ": {
                    EZConstants.EZPTZCommand command;
                    EZConstants.EZPTZAction action;
                    if (!cmdObject.hasKey("command") && !cmdObject.hasKey("action"))
                        break;

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                EZOpenSDK.getInstance().controlPTZ(mCameraInfo.getDeviceSerial(),
                                        mCameraInfo.getCameraNo(),
                                        EZConstants.EZPTZCommand.valueOf(cmdObject.getString("command")),
                                        EZConstants.EZPTZAction.valueOf(cmdObject.getString("action")),
                                        EZConstants.PTZ_SPEED_DEFAULT);
                            } catch (BaseException e) {
                                e.printStackTrace();
                                Log.d(TAG, "controlPTZ error ! " + e.getMessage());
                            }
                        }
                    }).start();

                }
                break;
                default:
                    break;
            }
        }
    }

    public void release() {
        if (mSurfaceViewRenderer != null) {
            mSurfaceViewRenderer.release();
            mSurfaceViewRenderer = null;
        }
    }
}
