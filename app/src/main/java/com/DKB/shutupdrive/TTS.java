package com.DKB.shutupdrive;

import android.content.Context;
import android.os.Build;
import android.speech.tts.TextToSpeech;

import java.util.Locale;

/**
 * Created by kyle on 2/28/16.
 */
public class TTS {

    private TextToSpeech textToSpeech;
    private Context context;

    public TTS(Context context) {
        this.context = context;
    }

    public void speak(final String text) {
        textToSpeech = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    textToSpeech.setLanguage(Locale.US);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
                    else
                        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null);
                }
            }
        });
    }

    public void stop() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }

}
