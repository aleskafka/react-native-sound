
package com.supraphonline.RNSound;

import android.media.MediaPlayer;


public class SoundInstance
{

	public MediaPlayer player;
	public RCTSoundDelegate delegate;
	public Sound sound;


	public SoundInstance(MediaPlayer player, RCTSoundDelegate delegate, Sound sound)
	{
		this.player = player;
		this.delegate = delegate;
		this.sound = sound;
	}


	public void release() {
	    this.sound.release();
	    this.player.reset();
	    this.player.release();
    }

}
