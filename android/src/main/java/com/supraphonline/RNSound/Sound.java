
package com.supraphonline.RNSound;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.media.VolumeShaper;
import android.media.VolumeShaper.Configuration;
import android.net.Uri;
import android.media.AudioManager;
import java.net.CookieHandler;
import com.facebook.react.modules.network.ForwardingCookieHandler;

import android.util.Log;

import com.facebook.react.bridge.ReactApplicationContext;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;


public class Sound
{

	private final ReactApplicationContext context;
	private final MediaPlayer player;
	private final String fileName;
	private final RCTSoundDelegate delegate;
	Timer timer = null;

	public Sound(ReactApplicationContext context, MediaPlayer player, String fileName, RCTSoundDelegate delegate)
	{
		this.context = context;
		this.player = player;
		this.fileName = fileName;
		this.delegate = delegate;
	}


	public void release() {
		if (this.timer != null) {
			this.timer.cancel();
			this.timer.purge();
			this.timer = null;
		}
	}


	public void load()
	{
		player.setOnPreparedListener(new OnPreparedListener() {
			boolean callbackWasCalled = false;

			@Override
			public synchronized void onPrepared(MediaPlayer mp)
			{
				if (callbackWasCalled == false) {
					callbackWasCalled = true;

					delegate.SoundEventDuration();
					delegate.SoundEventCanPlay();
					delegate.SoundEventCanPlayThrough();
				}
			}
		});

		player.setOnErrorListener(new OnErrorListener() {
			@Override
			public synchronized boolean onError(MediaPlayer mp, int what, int extra)
			{
				delegate.SoundEventError();
				return true;
			}
		});

		player.setOnInfoListener(new OnInfoListener() {
			@Override
			public boolean onInfo(MediaPlayer mp, int what, int extra) {
				Log.i("onInfo", String.valueOf(what));
				Log.i("onInfo", String.valueOf(extra));

				switch (what) {
					case MediaPlayer.MEDIA_INFO_BUFFERING_START:
						Log.i("buffering", String.valueOf(what));
						break;
					case MediaPlayer.MEDIA_INFO_BUFFERING_END:
						Log.i("buffering-end", String.valueOf(what));
						break;
				}
				return false;
			}
		});

		player.setOnSeekCompleteListener(new OnSeekCompleteListener() {
			@Override
			public synchronized void onSeekComplete(MediaPlayer mp) {
				delegate.SoundEventSeeked(player.getCurrentPosition()*.001);
			}
		});

		player.setOnCompletionListener(new OnCompletionListener() {
			@Override
			public synchronized void onCompletion(MediaPlayer mp)
			{
				delegate.SoundEventEnded();
			}
		});

		try {
			this.loadDataSource();
			player.prepareAsync();

		} catch (Exception ignored) {
			// When loading files from a file, we useMediaPlayer.create, which actually
			// prepares the audio for us already. So we catch and ignore this error
			Log.e("RNSoundModule", "Exception", ignored);

			delegate.SoundEventError();
		}
	}

	public void fade(double fromValue, final double toValue, double seconds) {
		final double delta = (toValue - fromValue) / 100;

		if (seconds <= 0 || Math.abs(delta) < 0.001) {
			this.player.setVolume((float) toValue, (float) toValue);

		} else {
			final double[] volume = {fromValue};
			this.player.setVolume((float) volume[0], (float) volume[0]);

			release();
			this.timer = new Timer(true);
			TimerTask task = new TimerTask() {
				@Override
				public void run() {
					volume[0] += delta;
					boolean isEnd = delta>0 ? volume[0]>=toValue : volume[0]<=toValue;

					if (isEnd) {
						volume[0] = toValue;
					}

					player.setVolume((float) volume[0], (float) volume[0]);

					if (isEnd) {
						release();
					}
				}
			};

			this.timer.schedule(task, Math.round(seconds*10), Math.round(seconds*10));
		}
	}


	public void play() {
		if (this.player.isPlaying()) {
			return;
		}

		this.player.start();
		delegate.SoundEventPlaying(this.player.getCurrentPosition()*.001);
	}

	public void pause() {
		if (this.player.isPlaying()) {
			this.player.pause();
			delegate.SoundEventPause(this.player.getCurrentPosition()*.001);
		}
	}

	public void stop() {
		this.pause();
	}

	public void togglePlayPause() {
		if (this.player.isPlaying()) {
			this.player.pause();

		} else {
			this.player.start();
		}
	}

	public void seek(double position) {
		delegate.SoundEventSeeking(this.player.getCurrentPosition()*.001);
		this.player.seekTo((int)Math.round(position * 1000));
	}


	private void loadDataSource() throws IOException
	{
		if (this.fileName.startsWith("http://") || this.fileName.startsWith("https://")) {
			if (CookieHandler.getDefault() == null) {
				CookieHandler.setDefault(new ForwardingCookieHandler(this.context));
			}

			this.player.setAudioStreamType(AudioManager.STREAM_MUSIC);
			this.player.setDataSource(this.fileName);

		} else if (this.fileName.startsWith("asset:/")){
			AssetFileDescriptor descriptor = this.context.getAssets().openFd(this.fileName.replace("asset:/", ""));
			this.player.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());
			descriptor.close();

		} else {
			File file = new File(this.fileName);

			if (file.exists()) {
				this.player.setAudioStreamType(AudioManager.STREAM_MUSIC);
				this.player.setDataSource(this.fileName);
			}
		}
	}

}
