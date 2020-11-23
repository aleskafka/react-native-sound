
package com.supraphonline.RNSound;

import android.media.MediaPlayer;
import android.media.AudioManager;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap;

import android.content.Context;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule.RCTDeviceEventEmitter;

import java.net.HttpURLConnection;
import java.net.URL;
import java.io.InputStream;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Map;

import android.util.Log;


public class RNSoundModule extends ReactContextBaseJavaModule implements AudioManager.OnAudioFocusChangeListener {

	public static final int NOTIFICATION_ID = 100;

	protected RNMusicNotification notification = null;

	Map<String, Bitmap> artworkList = new HashMap<String, Bitmap>();
	ArrayList<SoundInstance> sounds = new ArrayList<SoundInstance>();
	ReactApplicationContext context;
	SoundInstance active = null;
	final static Object NULL = null;
	ReadableMap pendingMetadata = null;

	Boolean wasPlayingBeforeFocusChange = false;

	public RNSoundModule(ReactApplicationContext context) {
		super(context);
		this.context = context;
	}

	@Override
	public String getName() {
		return "RNSound";
	}

	@ReactMethod
	public void createSound(final String fileName, final Callback callback) {
		MediaPlayer player = new MediaPlayer();
		RCTDeviceEventEmitter emitter = this.context.getJSModule(RCTDeviceEventEmitter.class);
		RCTSoundDelegate delegate = new RCTSoundDelegate(player, emitter, this.sounds.size());
		Sound sound = new Sound(this.context, player, fileName, delegate);

		this.sounds.add(new SoundInstance(player, delegate, sound));
		sound.load();

		callback.invoke(NULL, this.sounds.size()-1);
	}

	@ReactMethod
	public void setPrevNextControls(boolean prev, boolean next) {
		if (this.notification != null) {
			this.notification.setPrevNextControls(prev, next);
		}
	}

	@ReactMethod
	synchronized public void setTrackMetadata(final ReadableMap metadata) {
		if (this.notification == null) {
			this.notification = new RNMusicNotification(getReactApplicationContext(), this);
		}

		pendingMetadata = metadata;
	  	Log.i("metadata", String.valueOf(metadata));

	  	if (this.active != null) {
	  		String artwork = RNMusicNotification.getMetadataKey(metadata, "artwork");

	  		if (artworkList.containsKey(artwork) == false) {
				if (artwork.startsWith("http://") || artwork.startsWith("https://")) {
					try {
						URL url = new URL(artwork);
						HttpURLConnection connection = (HttpURLConnection) url.openConnection();
						connection.setDoInput(true);
						connection.connect();
						InputStream input = connection.getInputStream();
						artworkList.put(artwork, BitmapFactory.decodeStream(input));

					} catch (Exception e) {
						e.printStackTrace();
						artworkList.put(artwork, BitmapFactory.decodeResource(context.getResources(), R.drawable.play));
					}

				} else if (artwork.startsWith("file://")) {
					Bitmap artworkBitmap = BitmapFactory.decodeFile(artwork.substring(6));
					Log.i("artwork", String.valueOf(artworkBitmap));
					artworkList.put(artwork, artworkBitmap);

				} else { // unknown Artwork type
					artworkList.put(artwork, BitmapFactory.decodeResource(context.getResources(), R.drawable.play));
				}
			}

	  		if (this.active != null) {
				if (artwork == RNMusicNotification.getMetadataKey(pendingMetadata, "artwork")) {
					notification.updateMetadata(this.active, pendingMetadata, artworkList.get(artwork));
					this.active.delegate.setNotification(notification);
				}
			}
		}
	}

	@ReactMethod
	public void play(final int index) {
		SoundInstance instance = this.sounds.get(index);

		if (this.active != null && this.active != instance) {
			this.active.delegate.setNotification();
		}

		instancePlay(instance);
	}


	public void instancePlay(SoundInstance instance)
	{
		this.active = instance;

		if (this.active != null) {
			this.active.sound.play();

            AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
		}
	}

	@ReactMethod
	public void fade(final int index, final double fromValue, final double toValue, final double seconds) {
		SoundInstance instance = this.sounds.get(index);
		if (instance != null) {
			instance.sound.fade(fromValue, toValue, seconds);
		}
	}

	@ReactMethod
	public void playWithFade(final int index, final double fromValue, final double seconds) {
		this.fade(index, fromValue, 1.0, seconds);
		this.play(index);
	}

	@ReactMethod
	public void pause(final int index) {
		SoundInstance instance = this.sounds.get(index);
		instancePause(instance);
	}


	public void instancePause(SoundInstance instance)
	{
		if (instance != null) {
		    if (instance.player.isPlaying()) {
                instance.sound.pause();

                if (this.active == instance) {
                    AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                    audioManager.abandonAudioFocus(this);
                }
            }
		}
	}


	@ReactMethod
	public void seek(final int index, final double position) {
		SoundInstance instance = this.sounds.get(index);
		if (instance != null) {
			instance.sound.seek(position);
		}
	}

	@ReactMethod
	public void destroy(final int index) {
	    this.pause(index);

		SoundInstance instance = this.sounds.get(index);

		if (instance != null) {
            if (this.active == instance) {
            	this.active.delegate.setNotification();
            	this.notification.clean();
                this.active = null;
            }

			this.sounds.set(index, null);
			instance.release();
		}
	}

	@Override
	public Map<String, Object> getConstants() {
		final Map<String, Object> constants = new HashMap<>();
		constants.put("IsAndroid", true);
		return constants;
	}

	@Override
	public void onAudioFocusChange(int focusChange) {
		if (this.active != null) {
			if (focusChange <= 0) {
				this.wasPlayingBeforeFocusChange = this.active.player.isPlaying();

				if (this.wasPlayingBeforeFocusChange) {
					this.active.sound.pause();
				}

			} else if (this.wasPlayingBeforeFocusChange) {
				this.wasPlayingBeforeFocusChange = false;
				this.active.sound.play();
			}
		}
	}


	@Override
	public void onCatalystInstanceDestroy() {
		Log.i("onCatalystInstance", "destroy");
	//   java.util.Iterator it = this.playerPool.entrySet().iterator();
	//   while (it.hasNext()) {
	//     Map.Entry entry = (Map.Entry)it.next();
	//     MediaPlayer player = (MediaPlayer)entry.getValue();
	//     if (player != null) {
	//       player.reset();
	//       player.release();
	//     }
	//     it.remove();
	//   }
	}

}
