
package com.supraphonline.RNSound;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.media.MediaMetadataCompat;
import static android.support.v4.media.MediaMetadataCompat.*;
import android.util.Log;
import android.view.KeyEvent;

import android.content.Context;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;

import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v4.media.session.MediaSessionCompat;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReadableMap;

import java.util.Map;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.media.app.NotificationCompat.MediaStyle;

import static com.supraphonline.RNSound.RNSoundModule.NOTIFICATION_ID;

public class RNMusicNotification {

	protected static final String REMOVE_NOTIFICATION = "rn_sound_remove_notification";
	protected static final String MEDIA_BUTTON = "rn_sound_media_button";
	protected static final String PACKAGE_NAME = "rn_sound_package_name";

	private final ReactApplicationContext context;
	protected final MediaSessionCompat session;
	protected final PlaybackStateCompat.Builder playStateBuilder;
	protected final MediaMetadataCompat.Builder metadataBuilder;
	protected final NotificationCompat.Builder notificationBuilder;

	private final NotificationCompat.Action actionPlay;
	private final NotificationCompat.Action actionPause;
	private final NotificationCompat.Action actionStop;
	private final NotificationCompat.Action actionNext;
	private final NotificationCompat.Action actionPrev;
	private final NotificationCompat.Action actionSkipForward;
	private final NotificationCompat.Action actionSkipBackward;
	private boolean allowPrev = false;
	private boolean allowNext = false;
	private int playState = 0;

	public RNMusicNotification(ReactApplicationContext context) {
		this.context = context;

		IntentFilter filter = new IntentFilter();
		filter.addAction(REMOVE_NOTIFICATION);
		filter.addAction(MEDIA_BUTTON);
		filter.addAction(Intent.ACTION_MEDIA_BUTTON);
		filter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
		context.registerReceiver(new RNControlReceiver(this, context), filter);

		ComponentName receiver = new ComponentName(context.getPackageName(), RNControlReceiver.class.getName());

		playStateBuilder = new PlaybackStateCompat.Builder();
		metadataBuilder = new MediaMetadataCompat.Builder();

		notificationBuilder = new NotificationCompat.Builder(context, "RNSound");
		notificationBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
		notificationBuilder.setPriority(NotificationCompat.PRIORITY_HIGH);

		session = new MediaSessionCompat(context, "RNSound", receiver, null);
		session.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

		MediaStyle style = new MediaStyle();
		style.setMediaSession(session.getSessionToken());
		style.setShowActionsInCompactView(new int[0]);
		// nb.setContentTitle(getMetadataKey(metadata, "title"));
		// nb.setContentText(getMetadataKey(metadata, "artist"));
		// nb.setContentInfo(getMetadataKey(metadata, "album"));
		// nb.setColor(NotificationCompat.COLOR_DEFAULT);

		notificationBuilder.setStyle(style);
		notificationBuilder.setSmallIcon(R.drawable.play);

		session.setCallback(new MediaSessionCompat.Callback() {
			@Override
			public void onPlay() {
				Log.i("play", "test");
			}

			@Override
			public void onPause() {
				Log.i("pause", "test");
			}

			@Override
			public boolean onMediaButtonEvent(Intent mediaButtonEvent) {
				Log.i("onMediaButtonEvent", mediaButtonEvent.getAction());
				// final String intentAction = mediaButtonEvent.getAction();
				// if (Intent.ACTION_MEDIA_BUTTON.equals(intentAction)) {
				//     final KeyEvent event = mediaButtonEvent.getParcelableExtra(
				//             Intent.EXTRA_KEY_EVENT);
				//     if (event == null) {
				//         return super.onMediaButtonEvent(mediaButtonEvent);
				//     }
				//     final int keycode = event.getKeyCode();
				//     final int action = event.getAction();
				//     if (event.getRepeatCount() == 0 && action == KeyEvent.ACTION_DOWN) {
				//         switch (keycode) {
				//             // Do what you want in here
				//             case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
				//                 Log.d("MPS", "KEYCODE_MEDIA_PLAY_PAUSE");
				//                 break;
				//             case KeyEvent.KEYCODE_MEDIA_PAUSE:
				//                 Log.d("MPS", "KEYCODE_MEDIA_PAUSE");
				//                 break;
				//             case KeyEvent.KEYCODE_MEDIA_PLAY:
				//                 Log.d("MPS", "KEYCODE_MEDIA_PLAY");
				//                 break;
				//         }
				//     }
				// }
				return super.onMediaButtonEvent(mediaButtonEvent);
			}
		});

		actionPlay = createAction("play", "Play", PlaybackStateCompat.ACTION_PLAY);
		actionPause = createAction("pause", "Pause", PlaybackStateCompat.ACTION_PAUSE);
		actionStop = createAction("stop", "Stop", PlaybackStateCompat.ACTION_STOP);
		actionNext = createAction("next", "Next", PlaybackStateCompat.ACTION_SKIP_TO_NEXT);
		actionPrev = createAction("previous", "Previous", PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS);
		actionSkipForward = createAction("skip_forward", "Skip Forward", PlaybackStateCompat.ACTION_FAST_FORWARD);
		actionSkipBackward = createAction("skip_backward", "Skip Backward", PlaybackStateCompat.ACTION_REWIND);
	}

	public static String getMetadataKey(final ReadableMap metadata, String key) {
		try {
			if (metadata != null) {
				return metadata.getString(key);
			}

		} catch (Exception e) {}

		return new String("");
	}


	public void clean() {
		NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			Intent myIntent = new Intent(context, RNMusicNotification.NotificationService.class);
			context.stopService(myIntent);
		}
	}


	public void updateMetadata(SoundInstance instance, final ReadableMap metadata, Bitmap artwork) {
		metadataBuilder.putString(METADATA_KEY_TITLE, getMetadataKey(metadata, "title"));
		metadataBuilder.putString(METADATA_KEY_ARTIST, getMetadataKey(metadata, "artist"));
		metadataBuilder.putString(METADATA_KEY_ALBUM, getMetadataKey(metadata, "album"));
		metadataBuilder.putBitmap(METADATA_KEY_ART, artwork);

        notificationBuilder.setContentTitle(getMetadataKey(metadata, "title"));
        notificationBuilder.setContentText(getMetadataKey(metadata, "artist"));
        notificationBuilder.setContentInfo(getMetadataKey(metadata, "album"));
        notificationBuilder.setColor(NotificationCompat.COLOR_DEFAULT);

		notificationBuilder.setLargeIcon(artwork);
	}


	public void setPrevNextControls(boolean prev, boolean next) {
		this.allowPrev = prev;
		this.allowNext = next;
		updateNotificationSession();
	}


	public void updatePlayState(int playState, int position, int duration) {
		this.playState = playState;
		metadataBuilder.putLong(METADATA_KEY_DURATION, duration);

		playStateBuilder.setState(playState, position, 1.0f, SystemClock.elapsedRealtime());
		playStateBuilder.setBufferedPosition(duration);

		updateNotificationSession();
	}


	@SuppressLint("RestrictedApi")
    private synchronized void updateNotificationSession() {
        boolean isPlaying = playState==PlaybackStateCompat.STATE_PLAYING || playState==PlaybackStateCompat.STATE_BUFFERING;

        playStateBuilder.setActions(0
            | PlaybackStateCompat.ACTION_REWIND
            | PlaybackStateCompat.ACTION_FAST_FORWARD
            | PlaybackStateCompat.ACTION_STOP
            | (allowPrev ? PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS : 0)
            | (allowNext ? PlaybackStateCompat.ACTION_SKIP_TO_NEXT : 0)
            | (isPlaying ? PlaybackStateCompat.ACTION_PLAY_PAUSE : PlaybackStateCompat.ACTION_PLAY)
        );

		notificationBuilder.mActions.clear();
		if (allowPrev) notificationBuilder.addAction(actionPrev);
		notificationBuilder.addAction(actionSkipBackward);
		if (!isPlaying) notificationBuilder.addAction(actionPlay);
		if (isPlaying) notificationBuilder.addAction(actionPause);
		notificationBuilder.addAction(actionStop);
		if (allowNext) notificationBuilder.addAction(actionNext);
		notificationBuilder.addAction(actionSkipForward);

		notificationBuilder.setOngoing(isPlaying);

		session.setPlaybackState(playStateBuilder.build());
		session.setMetadata(metadataBuilder.build());
		session.setActive(true);

        String packageName = context.getPackageName();
        Intent openApp = context.getPackageManager().getLaunchIntentForPackage(packageName);
        notificationBuilder.setContentIntent(PendingIntent.getActivity(context, 0, openApp, 0));

        Intent remove = new Intent(REMOVE_NOTIFICATION);
        remove.putExtra(PACKAGE_NAME, context.getApplicationInfo().packageName);
        notificationBuilder.setDeleteIntent(PendingIntent.getBroadcast(context, 0, remove, PendingIntent.FLAG_UPDATE_CURRENT));

		NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notificationBuilder.build());
	}


	private NotificationCompat.Action createAction(String iconName, String title, long action) {
		String packageName = context.getPackageName();
		int icon = context.getResources().getIdentifier(iconName, "drawable", packageName);

		int keyCode = PlaybackStateCompat.toKeyCode(action);
		Intent intent = new Intent(MEDIA_BUTTON);
		intent.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN, keyCode));
		intent.putExtra(PACKAGE_NAME, packageName);
		PendingIntent i = PendingIntent.getBroadcast(context, keyCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);

		return new NotificationCompat.Action(icon, title, i);
	}

	public static class NotificationService extends Service {

		@Override
		public IBinder onBind(Intent intent) {
			return null;
		}

		private Notification notification;

		@Override
		public void onCreate() {
			super.onCreate();
			// notification = RNSoundModule.INSTANCE.notification.prepareNotification(RNSoundModule.INSTANCE.nb, false);
			startForeground(NOTIFICATION_ID, notification);
		}

		@Override
		public int onStartCommand(Intent intent, int flags, int startId) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
				// notification = RNSoundModule.INSTANCE.notification.prepareNotification(RNSoundModule.INSTANCE.nb, false);
				startForeground(NOTIFICATION_ID, notification);
			}
			return START_NOT_STICKY;
		}

		@Override
		public void onTaskRemoved(Intent rootIntent) {
			// Destroy the notification and sessions when the task is removed (closed, killed, etc)
			// if (RNSoundModule.INSTANCE != null) {
				// RNSoundModule.INSTANCE.destroy();
			// }
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
				stopForeground(true);
			}
			stopSelf(); // Stop the service as we won't need it anymore
		}

		@Override
		public void onDestroy() {

			// if (RNSoundModule.INSTANCE != null) {
				// RNSoundModule.INSTANCE.destroy();
			// }

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
				stopForeground(true);
			}

			stopSelf();
		}
	}
}
