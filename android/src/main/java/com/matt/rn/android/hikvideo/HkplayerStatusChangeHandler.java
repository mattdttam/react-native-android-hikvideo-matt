/*
 * @Description: 
 * @Author: 党辉
 * @Date: 2019-12-14 17:25:35
 * @LastEditors: 党辉
 * @LastEditTime: 2019-12-17 10:14:21
 */
package com.matt.rn.android.hikvideo;

import com.facebook.react.bridge.ReactContext;

public class HkplayerStatusChangeHandler {

    private String eventName;
    private String uri;
    private HkplayerStatus status;
    private boolean mPausing;
    private boolean mSoundOpen;
    private boolean mRecording;
    private boolean mTalking;

    private HkplayerEventTrigger trigger;
    private ReactContext context;

    public HkplayerStatusChangeHandler(ReactContext context, String eventName) {
        this.eventName = eventName;
        this.status = HkplayerStatus.IDLE;
        this.mSoundOpen = false;
        this.trigger = new HkplayerEventTrigger();
        this.context = context;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public HkplayerStatus getStatus() {
        return status;
    }
	
	public void reset() {
		this.status = HkplayerStatus.IDLE;
		this.mPausing = false;
		this.mSoundOpen = false;
		this.mRecording = false;
		this.mTalking = false;
		triggerStatusChangeEvent();
	}

    public void setStatus(HkplayerStatus status) {
        this.status = status;
        triggerStatusChangeEvent();
    }

    public boolean ismPausing() {
        return mPausing;
    }

    public void setmPausing(boolean mPausing) {
        this.mPausing = mPausing;
        triggerStatusChangeEvent();
    }

    public boolean ismSoundOpen() {
        return mSoundOpen;
    }

    public void setmSoundOpen(boolean mSoundOpen) {
        this.mSoundOpen = mSoundOpen;
        triggerStatusChangeEvent();
    }

    public boolean ismRecording() {
        return mRecording;
    }

    public void setmRecording(boolean mRecording) {
        this.mRecording = mRecording;
        triggerStatusChangeEvent();
    }

    public boolean ismTalking() {
        return mTalking;
    }

    public void setmTalking(boolean mTalking) {
        this.mTalking = mTalking;
        triggerStatusChangeEvent();
    }

    private void triggerStatusChangeEvent() {
        trigger.notifyStatusChangedMessage(context, eventName,this);
    }
}
