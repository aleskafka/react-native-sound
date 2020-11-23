package com.supraphonline.RNSound;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.media.RatingCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.content.Intent;
import android.view.KeyEvent;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.modules.core.DeviceEventManagerModule.RCTDeviceEventEmitter;


public class RNSessionEvents extends MediaSessionCompat.Callback
{

    private final RNMusicNotification notification;
    private final RNSoundModule module;
    private final RCTDeviceEventEmitter emitter;


    public RNSessionEvents(RNMusicNotification notification, ReactApplicationContext context, RNSoundModule module)
    {
    	this.notification = notification;
        this.module = module;
        this.emitter = context.getJSModule(RCTDeviceEventEmitter.class);
    }


    @Override
	public void onPlay()
	{
		module.instancePlay(module.active);
	}


	@Override
	public void onPause()
	{
		module.instancePause(module.active);
	}


	@Override
	public void onStop()
	{
		notification.clean();
		module.instancePause(module.active);
	}


	@Override
    public void onSkipToPrevious()
    {
        notification.clean();
    	if (emitter!=null) emitter.emit("prev", Arguments.createMap());
    }


    @Override
    public void onSkipToNext()
    {
        notification.clean();
    	if (emitter!=null) emitter.emit("next", Arguments.createMap());
    }


	@Override
	public boolean onMediaButtonEvent(Intent mediaButtonEvent)
	{
	    final KeyEvent event = mediaButtonEvent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);

	    if (event!=null && event.getRepeatCount()==0 && event.getAction()==KeyEvent.ACTION_DOWN) {
	        switch (event.getKeyCode()) {
	            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
	            	if (module.active!=null && module.active.player.isPlaying()) {
	            		module.instancePause(module.active);

	            	} else {
                		module.instancePlay(module.active);
	            	}
	                break;

	            case KeyEvent.KEYCODE_MEDIA_STOP:
            		notification.clean();
	            case KeyEvent.KEYCODE_MEDIA_PAUSE:
            		module.instancePause(module.active);
	                break;

	            case KeyEvent.KEYCODE_MEDIA_PLAY:
            		module.instancePlay(module.active);
	                break;

	            case KeyEvent.KEYCODE_MEDIA_NEXT:
		            notification.clean();
	            	if (emitter!=null) emitter.emit("next", Arguments.createMap());
		            break;

	            case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                    notification.clean();
	            	if (emitter!=null) emitter.emit("prev", Arguments.createMap());
		            break;

				case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:
				case KeyEvent.KEYCODE_MEDIA_SKIP_BACKWARD:
				case KeyEvent.KEYCODE_MEDIA_SKIP_FORWARD:
				case KeyEvent.KEYCODE_MEDIA_STEP_BACKWARD:
				case KeyEvent.KEYCODE_MEDIA_STEP_FORWARD:
					break;
	        }
	    }

		return super.onMediaButtonEvent(mediaButtonEvent);
	}

}
