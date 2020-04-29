package com.github.anrimian.musicplayer.data.utils.rx.audio_focus;

import android.content.Context;
import android.media.AudioManager;

import com.github.anrimian.musicplayer.domain.models.player.AudioFocusEvent;

import javax.annotation.Nullable;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

import static com.github.anrimian.musicplayer.domain.models.player.AudioFocusEvent.GAIN;
import static com.github.anrimian.musicplayer.domain.models.player.AudioFocusEvent.LOSS;
import static com.github.anrimian.musicplayer.domain.models.player.AudioFocusEvent.LOSS_SHORTLY;

/**
 * Created on 21.04.2018.
 */
public class AudioFocusRxWrapper {

    private final AudioManager audioManager;

    public AudioFocusRxWrapper(Context context) {
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    }

    @Nullable
    public Observable<AudioFocusEvent> requestAudioFocus(int streamType, int durationHint) {
        AudioFocusObservable audioFocusObservable = new AudioFocusObservable();
        int audioFocusResult = audioManager.requestAudioFocus(audioFocusObservable, streamType,
                durationHint);
        if (audioFocusResult == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            return audioFocusObservable.getObservable()
                    .doOnDispose(() -> audioManager.abandonAudioFocus(audioFocusObservable));
        }
        return null;
    }

    private static class AudioFocusObservable implements AudioManager.OnAudioFocusChangeListener {

        private final PublishSubject<AudioFocusEvent> subject = PublishSubject.create();

        @Override
        public void onAudioFocusChange(int focusChange) {
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_GAIN: {
                    subject.onNext(GAIN);
                    break;
                }
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK: {
                    subject.onNext(LOSS_SHORTLY);
                    break;
                }
                default: {
                    subject.onNext(LOSS);
                    break;
                }
            }
        }

        private Observable<AudioFocusEvent> getObservable() {
            return subject;
        }
    }
}
