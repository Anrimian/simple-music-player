package com.github.anrimian.musicplayer.data.utils.rx.content_observer;

import android.content.ContentResolver;
import android.database.ContentObserver;
import android.net.Uri;

import androidx.annotation.NonNull;

import java.util.Timer;
import java.util.TimerTask;

import io.reactivex.Emitter;
import io.reactivex.Observable;

import static com.github.anrimian.musicplayer.domain.Constants.STORAGE_EVENTS_MIN_EMIT_WINDOW_MILLIS;
import static com.github.anrimian.musicplayer.domain.Constants.TRIGGER;

public class RxContentObserver {

    public static Observable<Object> getObservable(ContentResolver contentResolver,
                                                   @NonNull Uri uri) {
        return getObservable(contentResolver, uri, false, STORAGE_EVENTS_MIN_EMIT_WINDOW_MILLIS);
    }

    public static Observable<Object> getObservable(ContentResolver contentResolver,
                                                   @NonNull Uri uri,
                                                   long minEmitWindowMillis) {
        return getObservable(contentResolver, uri, false, minEmitWindowMillis);
    }

    public static Observable<Object> getObservable(ContentResolver contentResolver,
                                                   @NonNull Uri uri,
                                                   boolean notifyForDescendants,
                                                   long minEmitWindowMillis) {
        return Observable.create(emitter -> {
            ContentObserver contentObserver = new EmitterContentObserver(minEmitWindowMillis, emitter);
            contentResolver.registerContentObserver(uri, notifyForDescendants, contentObserver);
            emitter.setDisposable(new ContentObserverDisposable(contentObserver, contentResolver));
        });
    }

    private static class EmitterContentObserver extends ContentObserver {

        private final long minEmitWindowMillis;
        private final Emitter<Object> changeEmitter;

        private final Timer timer = new Timer();
        private long lastEmitTime;
        private boolean waitForDelayedEmit = false;

        EmitterContentObserver(long minEmitWindowMillis, Emitter<Object> changeEmitter) {
            super(null);
            this.minEmitWindowMillis = minEmitWindowMillis;
            this.changeEmitter = changeEmitter;
        }

        @Override
        public boolean deliverSelfNotifications() {
            return false;
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            //optimization to not fire change events so often
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastEmitTime < minEmitWindowMillis) {
                if (!waitForDelayedEmit) {
                    waitForDelayedEmit = true;
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            waitForDelayedEmit = false;
                            changeEmitter.onNext(TRIGGER);
                        }
                    }, minEmitWindowMillis);
                }
                return;
            }
            lastEmitTime = currentTime;

            changeEmitter.onNext(TRIGGER);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
        }

    }
}
