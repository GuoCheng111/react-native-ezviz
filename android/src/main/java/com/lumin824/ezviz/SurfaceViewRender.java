package com.lumin824.ezviz;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.uimanager.ThemedReactContext;
import com.videogo.errorlayer.ErrorInfo;
import com.videogo.openapi.EZConstants;
import com.videogo.openapi.EZOpenSDK;
import com.videogo.openapi.EZPlayer;
import com.videogo.openapi.bean.EZCameraInfo;
import com.videogo.openapi.bean.EZDeviceInfo;
import com.videogo.realplay.RealPlayStatus;
import com.videogo.util.LogUtil;

import static com.videogo.openapi.EZConstants.MSG_VIDEO_SIZE_CHANGED;

public class SurfaceViewRender extends SurfaceView implements SurfaceHolder.Callback, Handler.Callback {
    private static final String TAG = SurfaceViewRender.class.getSimpleName();

    private SurfaceHolder mRealPlaySh = null;

    private EZPlayer mEzPlayer;
    private int mStatus = RealPlayStatus.STATUS_INIT;
    private Handler mHandler = null;

    private EZDeviceInfo mDeviceInfo = null;
    private EZCameraInfo mCameraInfo = null;
    private String mVerifyCode = null;

    public SurfaceViewRender(Context context) {
        super(context);
        mHandler = new Handler(this);

        mRealPlaySh = getHolder();
        mRealPlaySh.addCallback(this);

        this.setZOrderOnTop(true);
        this.setZOrderMediaOverlay(true);
    }

    public void setVerifyCode(String verifyCode) {
        mVerifyCode = verifyCode;
    }

    public void setDeviceInfo(EZDeviceInfo deviceInfo) {
        mDeviceInfo = deviceInfo;
        mCameraInfo = mDeviceInfo.getCameraInfoList().get(0);

        EZOpenSDK ezOpenSDK = EZOpenSDK.getInstance();
        if (mCameraInfo != null) {
            if (mEzPlayer != null) {
                ezOpenSDK.releasePlayer(mEzPlayer);
            }
            mEzPlayer = ezOpenSDK.createPlayer(mCameraInfo.getDeviceSerial(), mCameraInfo.getCameraNo());
        }
    }

    public void startRealPlay() {
        if (mEzPlayer == null)
            return;

        if (mStatus == RealPlayStatus.STATUS_PLAY)
            return;

        mEzPlayer.setHandler(mHandler);

        mEzPlayer.setSurfaceHold(getHolder());
        mEzPlayer.setPlayVerifyCode(mVerifyCode);
        mEzPlayer.startRealPlay();
    }

    public void stopRealPlay() {
        if (mEzPlayer == null)
            return;
        if (mStatus == RealPlayStatus.STATUS_STOP)
            return;

        mEzPlayer.stopRealPlay();
    }

    public boolean openSound() {
        if (mEzPlayer == null)
            return false;

        if (!mDeviceInfo.isSupportAudioOnOff())
            return false;

        return mEzPlayer.openSound();
    }

    public boolean closeSound() {
        if (mEzPlayer == null)
            return false;

        if (!mDeviceInfo.isSupportAudioOnOff())
            return false;

        return mEzPlayer.closeSound();
    }

    public boolean startVoiceTalk() {
        if (mEzPlayer == null)
            return false;

        if (mDeviceInfo.isSupportTalk() != EZConstants.EZTalkbackCapability.EZTalkbackNoSupport)
            return false;

        return mEzPlayer.startVoiceTalk();
    }

    public boolean stopVoiceTalk() {
        if (mEzPlayer == null)
            return false;

        if (mDeviceInfo.isSupportTalk() != EZConstants.EZTalkbackCapability.EZTalkbackNoSupport)
            return false;

        return mEzPlayer.stopVoiceTalk();
    }

    public void release() {
        stopRealPlay();
        if(mEzPlayer!=null){
            mEzPlayer.release();
            mEzPlayer = null;
        }

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TAG, "surfaceCreated ");
        if (mEzPlayer != null) {
            mEzPlayer.setSurfaceHold(holder);
        }

        if (mStatus == RealPlayStatus.STATUS_INIT) {
            // 开始播放
            startRealPlay();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d(TAG, "surfaceDestroyed ");
        if (mEzPlayer != null) {
            mEzPlayer.setSurfaceHold(null);
        }
        mRealPlaySh = null;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.d(TAG, "surfaceChanged ! width: " + width + " height : " + height);
        if (mEzPlayer != null) {
            mEzPlayer.setSurfaceHold(holder);
        }
        mRealPlaySh = holder;
    }

    @SuppressLint("NewApi")
    @Override
    public boolean handleMessage(Message msg) {
        LogUtil.i(TAG, "handleMessage:" + msg.toString());
        WritableMap params = new WritableNativeMap();
        switch (msg.what) {
            case EZConstants.EZRealPlayConstants.MSG_REALPLAY_CONNECTION_START:
                params.putString("type", "MSG_REALPLAY_CONNECTION_START");
                break;
            case EZConstants.EZRealPlayConstants.MSG_REALPLAY_PLAY_START:
                mStatus = RealPlayStatus.STATUS_START;
                params.putString("type", "MSG_REALPLAY_PLAY_START");
                break;
            case EZConstants.EZRealPlayConstants.MSG_REALPLAY_PLAY_SUCCESS:
                mStatus = RealPlayStatus.STATUS_PLAY;
                params.putString("type", "MSG_REALPLAY_PLAY_SUCCESS");
                break;
            case EZConstants.EZRealPlayConstants.MSG_REALPLAY_PLAY_FAIL:
                mStatus = RealPlayStatus.STATUS_INIT;
                ErrorInfo errorInfo = (ErrorInfo) msg.obj;
                params.putString("type", "MSG_REALPLAY_PLAY_FAIL");
                params.putInt("errorCode", errorInfo.errorCode);
                params.putString("description", errorInfo.description);
                break;
            case EZConstants.EZRealPlayConstants.MSG_REALPLAY_STOP_SUCCESS:
                mStatus = RealPlayStatus.STATUS_STOP;
                params.putString("type", "MSG_REALPLAY_STOP_SUCCESS");
                break;
            case EZConstants.EZRealPlayConstants.MSG_PTZ_SET_SUCCESS:
                params.putString("type", "MSG_PTZ_SET_SUCCESS");
                break;
            case EZConstants.EZRealPlayConstants.MSG_PTZ_SET_FAIL:
                params.putString("type", "MSG_PTZ_SET_FAIL");
                break;
            default:
                params.putString("type", "UN_KNOW");
                params.putString("message", msg.toString());
                break;
        }

        EzvizModule.sendEvent("EzvizPlayEvent", params);
        return false;
    }
}
