
package com.supraphonline.RNSound;

import android.media.MediaPlayer;
import android.support.v4.media.session.PlaybackStateCompat;

import com.facebook.react.modules.core.DeviceEventManagerModule.RCTDeviceEventEmitter;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;

import java.util.HashMap;
import java.util.Map;
import java.util.Date;


public class RCTSoundDelegate
{
	private final int index;
	private final MediaPlayer player;
	private final RCTDeviceEventEmitter emitter;
	private RNMusicNotification notification = null;

	public RCTSoundDelegate(MediaPlayer player, RCTDeviceEventEmitter emitter, int index) {
		this.player = player;
		this.emitter = emitter;
		this.index = index;
	}


	public void setNotification() {
		this.notification = null;
	}


	public void setNotification(RNMusicNotification notification) {
		this.notification = notification;
		this.updateMediaCenter();
	}


	protected void updateMediaCenter() {
		if (this.notification != null) {
			int playState = player.isPlaying() ? PlaybackStateCompat.STATE_PLAYING : PlaybackStateCompat.STATE_PAUSED;
			int currentTime = player.getCurrentPosition();
			int duration = player.getDuration();
			this.notification.updatePlayState(playState, currentTime, duration);
		}
	}


	public void SoundEventPause(double currentTime) {
		this.updateMediaCenter();
		WritableMap params = Arguments.createMap();
		params.putInt("index", this.index);
		params.putDouble("currentTime", currentTime);
		this.emitter.emit("pause", params);
	}


	public void SoundEventBuffering(double currentTime) {
		this.updateMediaCenter();
		WritableMap params = Arguments.createMap();
		params.putInt("index", this.index);
		params.putDouble("currentTime", currentTime);
		this.emitter.emit("buffering", params);
	}


	public void SoundEventPlaying(double currentTime) {
		this.updateMediaCenter();
		WritableMap params = Arguments.createMap();
		params.putInt("index", this.index);
		params.putDouble("currentTime", currentTime);
		params.putDouble("startTime", new Date().getTime());
		this.emitter.emit("playing", params);
	}


	public void SoundEventError() {
		WritableMap params = Arguments.createMap();
		params.putInt("index", this.index);
		this.emitter.emit("error", params);
	}


	public void SoundEventError(Object error) {
		WritableMap params = Arguments.createMap();
		params.putInt("index", this.index);
		this.emitter.emit("error", params);
	}


	public void SoundEventSeeking(double currentTime) {
		WritableMap params = Arguments.createMap();
		params.putInt("index", this.index);
		params.putDouble("currentTime", currentTime);
		this.emitter.emit("seeking", params);
	}


	public void SoundEventSeeked(double currentTime) {
		this.updateMediaCenter();
		WritableMap params = Arguments.createMap();
		params.putInt("index", this.index);
		params.putDouble("currentTime", currentTime);
		params.putDouble("startTime", new Date().getTime());
		this.emitter.emit("seeked", params);
    }


	public void SoundEventDuration() {
		this.updateMediaCenter();
		WritableMap params = Arguments.createMap();
		params.putInt("index", this.index);
		params.putDouble("duration", player.getDuration() * .001);
		this.emitter.emit("duration", params);
	}


	public void SoundEventCanPlay() {
		WritableMap params = Arguments.createMap();
		params.putInt("index", this.index);
		this.emitter.emit("canplay", params);
	}


	public void SoundEventCanPlayThrough() {
		WritableMap params = Arguments.createMap();
		params.putInt("index", this.index);
		this.emitter.emit("canplaythrough", params);
	}


	public void SoundEventEnded() {
		this.updateMediaCenter();
		WritableMap params = Arguments.createMap();
		params.putInt("index", this.index);
		this.emitter.emit("ended", params);
	}

}
