
package com.supraphonline.RNSound;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.view.KeyEvent;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import android.util.Log;

public class RNControlReceiver extends BroadcastReceiver {

    private final RNMusicNotification module;
    private final String packageName;
    private final ReactApplicationContext context;

    public RNControlReceiver(RNMusicNotification module, ReactApplicationContext context) {
        this.module = module;
        this.packageName = context.getPackageName();
        this.context = context;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (module.session == null) {
            return;
        }

        String action = intent.getAction();

        if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(action)) {
            module.session.getController().getTransportControls().pause();
            return;
        }

        if (checkApp(intent) == false) {
            return;
        }

        if (RNMusicNotification.REMOVE_NOTIFICATION.equals(action)) {
        } else if (RNMusicNotification.MEDIA_BUTTON.equals(action) || Intent.ACTION_MEDIA_BUTTON.equals(action)) {
            if (intent.hasExtra(Intent.EXTRA_KEY_EVENT)) {
                KeyEvent ke = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
                module.session.getController().dispatchMediaButtonEvent(ke);
            }
        }
    }

    private boolean checkApp(Intent intent) {
        if (intent.hasExtra(RNMusicNotification.PACKAGE_NAME)) {
            String name = intent.getStringExtra(RNMusicNotification.PACKAGE_NAME);
            return packageName.equals(name);
        }

        return false;
    }

}
