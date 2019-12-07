package com.matt.android.hikvideo;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blankj.utilcode.util.ToastUtils;
import com.facebook.react.uimanager.ThemedReactContext;
import com.hikvision.open.hikvideoplayer.HikVideoPlayer;
import com.hikvision.open.hikvideoplayer.HikVideoPlayerCallback;
import com.hikvision.open.hikvideoplayer.HikVideoPlayerFactory;

import java.text.MessageFormat;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import static com.facebook.react.bridge.UiThreadUtil.runOnUiThread;

public class HkplayerView extends RelativeLayout
        implements HikVideoPlayerCallback, HikVideoPlayerCallback.VoiceTalkCallback,
        TextureView.SurfaceTextureListener {

    private static final String TAG = "HkplayerView";

    private Context context;
    private HkplayerStatusChangeHandler statusChangeHandler;

    private TextureView textureView;
    private ProgressBar progressBar;
    private TextView playHintText;
    private String mUri;
    private HikVideoPlayer mPlayer;

    public HkplayerView(@NonNull final ThemedReactContext themedReactContext) {
        super(themedReactContext);
        this.context = themedReactContext;
        this.statusChangeHandler = new HkplayerStatusChangeHandler(themedReactContext,
                "HKPLAYER_PREVIEW_STATUS");
        initView(themedReactContext);
        mPlayer = HikVideoPlayerFactory.provideHikVideoPlayer();

    }

    public void setUri(String uri) {
        this.mUri = uri;
        this.statusChangeHandler.setUri(uri);
    }

    /**
     * 执行播放器命令
     *
     * @param command
     */
    public void executeCommand(String command) {
        switch (command) {
            case "START":
                executeStartEvent();
                break;
            case "STOP":
                executeStopEvent();
                break;
            case "CAPTURE":
                executeCaptureEvent();
                break;
            case "RECORD":
                executeRecordEvent();
                break;
            case "SOUND":
                executeSoundEvent();
                break;
            case "TALK":
                executeTalkEvent();
            case "ONPAUSE":
                onPause();
                break;
            case "ONRESUME":
                onResume();
                break;
            default:
                break;
        }
    }

    /**
     * 初始化视图布局
     *
     * @param themedReactContext
     */
    private void initView(ThemedReactContext themedReactContext) {

        FrameLayout fl = new FrameLayout(themedReactContext);

        textureView = new TextureView(themedReactContext);
        FrameLayout.LayoutParams textureViewParams = new FrameLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT);
        textureView.setLayoutParams(textureViewParams);
        fl.addView(textureView);

        progressBar = new ProgressBar(themedReactContext);
        FrameLayout.LayoutParams progressBarParams = new FrameLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
        progressBarParams.gravity = Gravity.CENTER;
        progressBar.setLayoutParams(progressBarParams);
        progressBar.setVisibility(View.INVISIBLE);
        fl.addView(progressBar);

        playHintText = new TextView(themedReactContext);
        FrameLayout.LayoutParams textViewParams = new FrameLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT);
        playHintText.setLayoutParams(textViewParams);
        playHintText.setTextColor(getResources().getColor(R.color.playerHintText));
        playHintText.setWidth(0);
        playHintText.setVisibility(View.INVISIBLE);
        playHintText.setGravity(Gravity.CENTER);
        fl.addView(playHintText);

        LayoutParams flParams = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, MyUtils.dip2px(context, 250));
        flParams.alignWithParent = true;
        fl.setLayoutParams(flParams);
        fl.setBackgroundColor(getResources().getColor(R.color.playerBackground));
        fl.setId(View.generateViewId());
        addView(fl);

        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        this.setLayoutParams(params);

        textureView.setSurfaceTextureListener(this);
    }

    private  void executeStartEvent() {
        if(getPreviewUri()) {
            if (statusChangeHandler.getStatus() != HkplayerStatus.SUCCESS) {
                startRealPlay(textureView.getSurfaceTexture());
            } else {
                ToastUtils.showShort("视频已经在播放中");
            }
        }
    }

    private void executeStopEvent() {
        if (statusChangeHandler.getStatus() == HkplayerStatus.SUCCESS) {
            if(mPlayer.stopPlay()) {
                statusChangeHandler.setStatus(HkplayerStatus.IDLE);
                progressBar.setVisibility(View.INVISIBLE);
                playHintText.setVisibility(View.VISIBLE);
                playHintText.setText("");
            }
        }
    }

    /**
     * 开始播放
     *
     * @param surface 渲染画面
     */
    private void startRealPlay(SurfaceTexture surface) {
        statusChangeHandler.setStatus(HkplayerStatus.LOADING);
        progressBar.setVisibility(View.VISIBLE);
        playHintText.setVisibility(View.INVISIBLE);
        mPlayer.setSurfaceTexture(surface);
        //startRealPlay() 方法会阻塞当前线程，需要在子线程中执行,建议使用RxJava
        new Thread(() -> {
            //不要通过判断 startRealPlay() 方法返回 true 来确定播放成功，播放成功会通过HikVideoPlayerCallback回调，startRealPlay() 方法返回 false 即代表 播放失败;
            if (!mPlayer.startRealPlay(mUri, HkplayerView.this)) {
                onPlayerStatus(Status.FAILED, mPlayer.getLastError());
            }
        }).start();
    }


    /**
     * 播放结果回调
     *
     * @param status    共四种状态：SUCCESS（播放成功）、FAILED（播放失败）、EXCEPTION（取流异常）、FINISH（回放结束）
     * @param errorCode 错误码，只有 FAILED 和 EXCEPTION 才有值
     */
    @Override
    @WorkerThread
    public void onPlayerStatus(@NonNull Status status, int errorCode) {
        //由于 HikVideoPlayerCallback 是在子线程中进行回调的，所以一定要切换到主线程处理UI
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(View.INVISIBLE);
                switch (status) {
                    case SUCCESS:
                        //播放成功
                        statusChangeHandler.setStatus(HkplayerStatus.SUCCESS);
                        playHintText.setVisibility(View.INVISIBLE);
                        textureView.setKeepScreenOn(true);//保持亮屏
                        break;
                    case FAILED:
                        //播放失败
                        statusChangeHandler.setStatus(HkplayerStatus.FAILED);
                        playHintText.setVisibility(View.VISIBLE);
                        playHintText.setText(MessageFormat.format("预览失败，错误码：{0}", Integer.toHexString(errorCode)));
                        break;
                    case EXCEPTION:
                        //取流异常
                        statusChangeHandler.setStatus(HkplayerStatus.EXCEPTION);
                        mPlayer.stopPlay();//异常时关闭取流
                        playHintText.setVisibility(View.VISIBLE);
                        playHintText.setText(MessageFormat.format("取流发生异常，错误码：{0}", Integer.toHexString(errorCode)));
                        break;
                }
            }
        });
    }

    /**
     * 执行抓图事件
     */
    private void executeCaptureEvent() {
        if (statusChangeHandler.getStatus() != HkplayerStatus.SUCCESS) {
            ToastUtils.showShort("没有视频在播放");
        }

        //抓图
        if (mPlayer.capturePicture(MyUtils.getCaptureImagePath(this.context))) {
            ToastUtils.showShort("抓图成功");
        }
    }

    /**
     * 执行录像事件
     */
    private void executeRecordEvent() {
        if (statusChangeHandler.getStatus() != HkplayerStatus.SUCCESS) {
            ToastUtils.showShort("没有视频在播放");
        }

        if (!statusChangeHandler.ismRecording()) {
            //开始录像
            String path = MyUtils.getLocalRecordPath(this.context);
            if (mPlayer.startRecord(path)) {
                ToastUtils.showShort("开始录像");
                statusChangeHandler.setmRecording(true);
            }
        } else {
            //关闭录像
            if(mPlayer.stopRecord()) {
                ToastUtils.showShort("关闭录像");
                statusChangeHandler.setmRecording(false);
            }

        }
    }

    /**
     * 执行声音开关事件
     */
    private void executeSoundEvent() {
        if (statusChangeHandler.getStatus() != HkplayerStatus.SUCCESS) {
            ToastUtils.showShort("没有视频在播放");
        }

        if (!statusChangeHandler.ismSoundOpen()) {
            //打开声音
            if (mPlayer.enableSound(true)) {
                ToastUtils.showShort("声音开");
                statusChangeHandler.setmSoundOpen(true);
            }
        } else {
            //关闭声音
            if (mPlayer.enableSound(false)) {
                ToastUtils.showShort("声音关");
                statusChangeHandler.setmSoundOpen(false);
            }
        }
    }

    private void executeTalkEvent() {
        if(!statusChangeHandler.ismTalking() && getPreviewUri()) {
            startVoiceTalk();
        } else {
            if(mPlayer.stopVoiceTalk()) {
                statusChangeHandler.setmTalking(false);
                ToastUtils.showShort("对讲已关闭");
            }
        }
    }

    /**
     * 开始播放
     */
    private void startVoiceTalk() {
        playHintText.setVisibility(View.INVISIBLE);
        //startVoiceTalk() 方法会阻塞当前线程，需要在子线程中执行,建议使用RxJava
        new Thread(() -> {
            if (mPlayer.startVoiceTalk(mUri, HkplayerView.this)) {
                onTalkStatus(HikVideoPlayerCallback.Status.SUCCESS, -1);
            } else {
                onTalkStatus(HikVideoPlayerCallback.Status.FAILED, mPlayer.getLastError());
            }
        }).start();
    }

    /**
     * 播放结果回调
     *
     * @param status    共四种状态：SUCCESS（开启成功）、FAILED（开启失败）、EXCEPTION（取流异常）
     * @param errorCode 错误码，只有 FAILED 和 EXCEPTION 才有值
     */
    @Override
    public void onTalkStatus(@NonNull HikVideoPlayerCallback.Status status, int errorCode) {
        //由于 VoiceTalkCallback 是在子线程中进行回调的，所以一定要切换到主线程处理UI
        runOnUiThread(() -> {
            progressBar.setVisibility(View.GONE);
            switch (status) {
                case SUCCESS:
                    //播放成功
                    statusChangeHandler.setmTalking(true);
                    ToastUtils.showShort("正在对讲中...");
                    break;
                case FAILED:
                    //播放失败
                    statusChangeHandler.setmTalking(false);
                    ToastUtils.showShort(MessageFormat.format("开启对讲失败，错误码：{0}", Integer.toHexString(errorCode)));
                    break;
                case EXCEPTION:
                    //取流异常
                    if(mPlayer.stopVoiceTalk()) {
                        statusChangeHandler.setmTalking(false);
                    }
                    ToastUtils.showShort(MessageFormat.format("对讲发生异常，错误码：{0}", Integer.toHexString(errorCode)));
                    break;
            }
        });
    }

    //APP前后台切换时 SurfaceTextureListener可能在有某些 华为手机 上不会回调，例如：华为P20，所以我们在这里手动调用
    private void onPause() {
        if (textureView.isAvailable()) {
            Log.e(TAG, "onPause: onSurfaceTextureDestroyed");
            onSurfaceTextureDestroyed(textureView.getSurfaceTexture());
        }
    }

    //APP前后台切换时 SurfaceTextureListener可能在有某些 华为手机 上不会回调，例如：华为P20，所以我们在这里手动调用
    private void onResume() {
        if (textureView.isAvailable()) {
            Log.e(TAG, "onResume: onSurfaceTextureAvailable");
            onSurfaceTextureAvailable(textureView.getSurfaceTexture(), textureView.getWidth(), textureView.getHeight());
        }
    }

    //APP前后台切换时 SurfaceTextureListener可能在有某些华为手机上不会回调，例如：华为P20，因此我们需要在Activity生命周期中手动调用回调方法
    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        if (statusChangeHandler.getStatus() == HkplayerStatus.STOPPING) {
            //恢复处于暂停播放状态的窗口
            startRealPlay(textureView.getSurfaceTexture());
            Log.d(TAG, "onSurfaceTextureAvailable: startRealPlay");
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        if (statusChangeHandler.getStatus() == HkplayerStatus.SUCCESS) {
            if(mPlayer.stopPlay()) {
                statusChangeHandler.setStatus(HkplayerStatus.STOPPING); //暂停播放，再次进入时恢复播放
                Log.d(TAG, "onSurfaceTextureDestroyed: stopPlay");
            }
        }
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    private boolean getPreviewUri() {
        if (TextUtils.isEmpty(mUri)) {
            ToastUtils.showShort("URI不能为空");
            return false;
        }

        if (!mUri.contains("rtsp")) {
            ToastUtils.showShort("非法URI");
            return false;
        }

        return true;
    }
}