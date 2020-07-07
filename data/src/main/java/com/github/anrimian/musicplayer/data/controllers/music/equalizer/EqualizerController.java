package com.github.anrimian.musicplayer.data.controllers.music.equalizer;

import android.app.Activity;
import android.content.Context;

import com.github.anrimian.musicplayer.domain.repositories.SettingsRepository;

public class EqualizerController {

    private final SettingsRepository settingsRepository;

    private int audioSessionId;

    public EqualizerController(SettingsRepository settingsRepository) {
        this.settingsRepository = settingsRepository;
    }

    public void attachEqualizer(Context context, int audioSessionId) {
        this.audioSessionId = audioSessionId;
        switch (settingsRepository.getSelectedEqualizerType()) {
            case EqualizerTypes.EXTERNAL: {
                ExternalEqualizer.attachExternalEqualizer(context, audioSessionId);
                break;
            }
        }
    }

    public void detachEqualizer(Context context) {
        ExternalEqualizer.detachExternalEqualizer(context, this.audioSessionId);
    }

    public void disableExternalEqualizer(Context context) {
        int currentEqualizer = settingsRepository.getSelectedEqualizerType();
        if (currentEqualizer == EqualizerTypes.EXTERNAL) {
            ExternalEqualizer.detachExternalEqualizer(context, this.audioSessionId);
        }
        settingsRepository.setSelectedEqualizerType(EqualizerTypes.NONE);
    }

    public void enableExternalEqualizer(Context context) {
        settingsRepository.setSelectedEqualizerType(EqualizerTypes.EXTERNAL);
        ExternalEqualizer.attachExternalEqualizer(context, audioSessionId);

    }

    public void launchExternalEqualizerSetup(Activity activity) {
        settingsRepository.setSelectedEqualizerType(EqualizerTypes.EXTERNAL);
        ExternalEqualizer.launchExternalEqualizerSetup(activity, this.audioSessionId);

    }

    public int getSelectedEqualizerType() {
        return settingsRepository.getSelectedEqualizerType();
    }

}
