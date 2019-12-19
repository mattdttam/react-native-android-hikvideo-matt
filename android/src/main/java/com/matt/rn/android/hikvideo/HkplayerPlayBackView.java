package com.matt.rn.android.hikvideo;

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
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.uimanager.ThemedReactContext;
import com.hikvision.open.hikvideoplayer.HikVideoPlayer;
import com.hikvision.open.hikvideoplayer.HikVideoPlayerCallback;
import com.hikvision.open.hikvideoplayer.HikVideoPlayerFactory;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import hik.common.isms.hpsclient.AbsTime;

import static com.facebook.react.bridge.UiThreadUtil.runOnUiThread;

public class HkplayerPlayBackView extends RelativeLayout implements HikVideoPlayerCallback, TextureView.SurfaceTextureListener {

    private final String TAG = "HkplayerPlayBackView";

    private Context context;
    private HkplayerStatusChangeHandler statusChangeHandler;

    private TextureView textureView;
    private ProgressBar progressBar;
    private TextView playHintText;
    private TimeBarView timeBar;
    private String mUri;
    private HikVideoPlayer mPlayer;
    /*回放开始时间*/
    private Calendar mStartCalendar;
    /*回放结束时间*/
    private Calendar mEndCalendar;
    /*回放定位时间*/
    private Calendar mSeekCalendar = Calendar.getInstance();
    private ReadableArray segments;
    private long startTime;
    private long endTime;

    public HkplayerPlayBackView(final ThemedReactContext themedReactContext) {
        super(themedReactContext);
        this.context = themedReactContext;
        this.statusChangeHandler = new HkplayerStatusChangeHandler(themedReactContext,
                "HKPLAYER_PLAY_BACK_STATUS");
        initView(themedReactContext);
        mPlayer = HikVideoPlayerFactory.provideHikVideoPlayer();
    }

    public void setUri(String uri) {
        this.mUri = uri;
        this.statusChangeHandler.setUri(uri);
    }

    public void setSegments(ReadableArray segments) {
        this.segments = segments;
        initTimeBarView();
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
            case "PAUSE":
                executePauseEvent();
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
        //flParams.bottomMargin = MyUtils.dip2px(context, 10);
        flParams.alignWithParent = true;
        fl.setLayoutParams(flParams);
        fl.setBackgroundColor(getResources().getColor(R.color.playerBackground));
        fl.setId(View.generateViewId());
        addView(fl);

        timeBar = new TimeBarView(themedReactContext, null);
        LayoutParams timeBarParams = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, MyUtils.dip2px(context, 50));
        timeBarParams.addRule(RelativeLayout.BELOW, fl.getId());
        timeBar.setLayoutParams(timeBarParams);
        timeBar.setBackgroundColor(getResources().getColor(R.color.playbackTimebarBackground));
        addView(timeBar);

        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        this.setLayoutParams(params);

        textureView.setSurfaceTextureListener(this);
    }

    private void initTimeBarView() {

        // 如果有视频还在播放中，则先关闭视频
        if(statusChangeHandler.getStatus() == HkplayerStatus.SUCCESS) {
            if (mPlayer.stopPlay()) {
                //statusChangeHandler.setStatus(HkplayerStatus.IDLE);
                statusChangeHandler.reset();
                progressBar.setVisibility(View.INVISIBLE);
                playHintText.setVisibility(View.INVISIBLE);
                playHintText.setText("");
                cancelUpdateTime();
            }
        }

        // 设置视频片信息
        List segList = new ArrayList();
        for(int i=0; i<segments.size(); i++) {
            ReadableMap segMap = segments.getMap(i);
            RecordSegment recordSegment = new RecordSegment();
            recordSegment.setBeginTime(segMap.getString("beginTime"));
            recordSegment.setEndTime(segMap.getString("endTime"));
            segList.add(recordSegment);
        }
        timeBar.addFileInfoList(segList);

        // 设置开始、结束时间
        startTime = CalendarUtil.getDefaultStartCalendar().getTimeInMillis();
        endTime = CalendarUtil.getCurDayEndTime(startTime);
        if(segments.size()>0){
            ReadableMap segMapBegin = segments.getMap(0);
            ReadableMap segMapEnd = segments.getMap(segments.size() - 1);
            String segBtime = segMapBegin.getString("beginTime");
            String segEtime = segMapEnd.getString("endTime");
            try{
                SimpleDateFormat sdf =   new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss.SSS" );
                startTime = sdf.parse(segBtime.substring(0,23).replace('T',' ')).getTime();
                endTime = sdf.parse( segEtime.substring(0,23).replace('T',' ')).getTime();
            } catch (Exception e){
                Log.e(TAG, "segments: beginTime&endTime error");
            }
        }
        timeBar.setCurrentTime(startTime);

        // 设置回调
        timeBar.setTimeBarCallback(new TimeBarView.TimePickedCallBack() {
            @Override
            public void onMoveTimeCallback(long currentTime) {

            }

            @Override
            public void onBarMoving(long currentTime) {

            }

            @Override
            public void onTimePickedCallback(long currentTime) {
                if(currentTime>=startTime && currentTime<=endTime) {
                    //定位操作的时间要在录像片段开始时间和结束时间之内，不再范围内不要执行以下操作
                    mSeekCalendar.setTimeInMillis(currentTime);
                    Log.e(TAG, "onTimePickedCallback: currentTime = " + CalendarUtil.calendarToyyyy_MM_dd_T_HH_mm_SSSZ(mSeekCalendar));
                    AbsTime start = CalendarUtil.calendarToABS(mSeekCalendar);
                    progressBar.setVisibility(View.VISIBLE);
                    new Thread(() -> {
                        cancelUpdateTime();//seek时停止刷新时间
                        if (!mPlayer.seekAbsPlayback(start, HkplayerPlayBackView.this)) {
                            onPlayerStatus(Status.FAILED, mPlayer.getLastError());
                        }
                    }).start();
                }
            }

            @Override
            public void onMaxScale() {

            }

            @Override
            public void onMinScale() {

            }
        });
    }

    /**
     * 每隔400ms获取一次当前回放的系统时间
     * 更新时间条上的OSD时间
     */
    private final Runnable mGetOSDTimeTask = new Runnable() {
        @Override
        public void run() {
            long osdTime = mPlayer.getOSDTime();
            if (osdTime > -1) {
                timeBar.setCurrentTime(osdTime);
            }

            startUpdateTime();
        }
    };

    private void executeStartEvent() {
        if (getPreviewUri()) {
            if (statusChangeHandler.getStatus() != HkplayerStatus.SUCCESS) {
                startPlayback(textureView.getSurfaceTexture());
            } else {
                ToastUtils.showShort("视频已经在播放中");
            }
        }
    }

    private void executeStopEvent() {
        if (statusChangeHandler.getStatus() == HkplayerStatus.SUCCESS) {
            if (mPlayer.stopPlay()) {
                //statusChangeHandler.setStatus(HkplayerStatus.IDLE);
				statusChangeHandler.reset();
                progressBar.setVisibility(View.INVISIBLE);
                playHintText.setVisibility(View.INVISIBLE);
                playHintText.setText("");
                cancelUpdateTime();
            }
        } else {
            ToastUtils.showShort("没有视频在播放");
        }
    }

    /**
     * 执行播放暂停和恢复播放事件
     */
    private void executePauseEvent() {
        if (statusChangeHandler.getStatus() != HkplayerStatus.SUCCESS) {
            ToastUtils.showShort("没有视频在播放");
        }

        if (!statusChangeHandler.ismPausing()) {
            //暂停播放
            if (mPlayer.pause()) {
                statusChangeHandler.setmPausing(true);
				playHintText.setVisibility(View.VISIBLE);
                playHintText.setText("暂停中...");
                ToastUtils.showShort("暂停播放");
            }
        } else {
            //恢复播放
            if (mPlayer.resume()) {
                statusChangeHandler.setmPausing(false);
				playHintText.setVisibility(View.INVISIBLE);
                playHintText.setText("");
                ToastUtils.showShort("恢复播放");
            }
        }
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
            ToastUtils.showShort("截屏成功");
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
                statusChangeHandler.setmRecording(true);
                ToastUtils.showShort("已开始录像");
				playHintText.setVisibility(View.VISIBLE);
                playHintText.setText("正在录像中...");
            }
        } else {
            //关闭录像
            if (mPlayer.stopRecord()) {
                statusChangeHandler.setmRecording(false);
                ToastUtils.showShort("已停止录像");
				playHintText.setVisibility(View.INVISIBLE);
                playHintText.setText("");
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
                statusChangeHandler.setmSoundOpen(true);
                ToastUtils.showShort("声音已开");
            }
        } else {
            //关闭声音
            if (mPlayer.enableSound(false)) {
                statusChangeHandler.setmSoundOpen(false);
                ToastUtils.showShort("声音已关");
            }
        }
    }

    protected void onResume() {
        //APP前后台切换时 SurfaceTextureListener可能在有某些 华为手机 上不会回调，例如：华为P20，所以我们在这里手动调用
        if (textureView.isAvailable()) {
            Log.e(TAG, "onResume: onSurfaceTextureAvailable");
            onSurfaceTextureAvailable(textureView.getSurfaceTexture(), textureView.getWidth(), textureView.getHeight());
        }
    }

    protected void onPause() {
        //APP前后台切换时 SurfaceTextureListener可能在有某些 华为手机 上不会回调，例如：华为P20，所以我们在这里手动调用
        if (textureView.isAvailable()) {
            Log.e(TAG, "onPause: onSurfaceTextureDestroyed");
            onSurfaceTextureDestroyed(textureView.getSurfaceTexture());
        }
    }

    /**
     * 开始回放
     */
    private void startPlayback(SurfaceTexture surface) {
        progressBar.setVisibility(View.VISIBLE);
        playHintText.setVisibility(View.INVISIBLE);
        mPlayer.setSurfaceTexture(surface);
        //开始时间为你从服务端获取的录像片段列表中第一个片段的开始时间，结束时间为录像片段列表的最后一个片段的结束时间
        mStartCalendar = Calendar.getInstance();
        mEndCalendar = Calendar.getInstance();
        mStartCalendar.setTimeInMillis(startTime);
        mEndCalendar.setTimeInMillis(endTime);
        AbsTime startTimeST = CalendarUtil.calendarToABS(mStartCalendar);
        AbsTime stopTimeST = CalendarUtil.calendarToABS(mEndCalendar);

        //startPlayback() 方法会阻塞当前线程，需要在子线程中执行,建议使用RxJava
        new Thread(() -> {
            //不要通过判断 startPlayback() 方法返回 true 来确定播放成功，播放成功会通过HikVideoPlayerCallback回调，startPlayback() 方法返回 false 即代表 播放失败;
            //seekTime 参数可以为NULL，表示无需定位到指定时间开始播放。
            if (!mPlayer.startPlayback(mUri, startTimeST, stopTimeST, HkplayerPlayBackView.this)) {
                onPlayerStatus(Status.FAILED, mPlayer.getLastError());
            }
        }).start();
    }


    /**
     * 播放结果回调
     *
     * @param status    共四种状态：SUCCESS（播放成功）、FAILED（播放失败）、EXCEPTION（取流异常）、FINISH（录像回放结束）
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
                        timeBar.setCurrentTime(mPlayer.getOSDTime());
                        startUpdateTime();//开始刷新回放时间
                        break;
                    case FAILED:
                        //播放失败
                        statusChangeHandler.setStatus(HkplayerStatus.FAILED);
                        playHintText.setVisibility(View.VISIBLE);
                        playHintText.setText(MessageFormat.format("回放失败，错误码：{0}", Integer.toHexString(errorCode)));
                        break;
                    case EXCEPTION:
                        //取流异常
                        statusChangeHandler.setStatus(HkplayerStatus.EXCEPTION);
                        mPlayer.stopPlay();//异常时关闭取流
                        playHintText.setVisibility(View.VISIBLE);
                        playHintText.setText(MessageFormat.format("取流发生异常，错误码：{0}", Integer.toHexString(errorCode)));
                        break;
                    case FINISH:
                        //录像回放结束
                        statusChangeHandler.setStatus(HkplayerStatus.FINISH);
                        ToastUtils.showShort("没有录像片段了");
                        break;
                }
            }
        });
    }


    /**
     * 开始刷新回放时间
     */
    private void startUpdateTime() {
        playHintText.getHandler().postDelayed(mGetOSDTimeTask, 400);
    }

    /**
     * 停止刷新回放时间
     */
    private void cancelUpdateTime() {
        playHintText.getHandler().removeCallbacks(mGetOSDTimeTask);
    }


    /*************************TextureView.SurfaceTextureListener 接口的回调方法********************/
    //APP前后台切换时 SurfaceTextureListener可能在有某些华为手机上不会回调，例如：华为P20，因此我们需要在Activity生命周期中手动调用回调方法
    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        if (statusChangeHandler.getStatus() == HkplayerStatus.STOPPING) {
            //恢复处于暂停播放状态的窗口
            startPlayback(textureView.getSurfaceTexture());
            Log.d(TAG, "onSurfaceTextureAvailable: startPlayback");
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        if (statusChangeHandler.getStatus() == HkplayerStatus.SUCCESS) {
            if (mPlayer.stopPlay()) {
                statusChangeHandler.setStatus(HkplayerStatus.STOPPING);//暂停播放，再次进入时恢复播放
                cancelUpdateTime();
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
