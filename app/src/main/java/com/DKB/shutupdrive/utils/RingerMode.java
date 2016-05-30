package com.DKB.shutupdrive.utils;

import android.content.Context;
import android.media.AudioManager;

/**
 * Created by kyle on 2/16/16.
 */
public class RingerMode {
    private AudioManager am;
    private int originalAudioProfile;

    public RingerMode(Context context) {
        am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        originalAudioProfile = am.getRingerMode();
    }

    public int getAudioProfileOnStart() {
        return originalAudioProfile;
    }

    public void toSilentMode() {
        am.setRingerMode(AudioManager.RINGER_MODE_SILENT);
    }

    public void toVibrateMode() {
        am.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
    }

    public void toNormalMode() {
        am.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
    }

    public int getCurrentMode() {
        return am.getRingerMode();
    }

    public void restoreAudioProfileOnStart() {
        am.setRingerMode(getAudioProfileOnStart());
    }
}
