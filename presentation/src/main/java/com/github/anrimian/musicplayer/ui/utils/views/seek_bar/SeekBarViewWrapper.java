package com.github.anrimian.musicplayer.ui.utils.views.seek_bar;

import android.widget.SeekBar;

/**
 * Created on 03.06.2018.
 */
public class SeekBarViewWrapper {

    private SeekBar seekBar;

    private ProgressChangeListener progressChangeListener;
    private OnSeekStartListener onSeekStartListener;
    private OnSeekStopListener onSeekStopListener;

    private boolean isOnTouch;
    private int progress;

    public SeekBarViewWrapper(SeekBar seekBar) {
        this.seekBar = seekBar;

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && progressChangeListener != null) {
                    progressChangeListener.onProgressChanged(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isOnTouch = true;
                onSeekStartListener.onSeekStart();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isOnTouch = false;
                onSeekStopListener.onSeekStop(seekBar.getProgress());
            }
        });
    }

    public void setProgress(long progress) {
        if (!isOnTouch) {
            this.progress = (int) progress;
            seekBar.setProgress(this.progress);
        }
    }

    public void setMax(long max) {
        seekBar.setMax((int) max);
        seekBar.setProgress(progress);
    }

    public void setOnSeekStartListener(OnSeekStartListener onSeekStartListener) {
        this.onSeekStartListener = onSeekStartListener;
    }

    public void setOnSeekStopListener(OnSeekStopListener onSeekStopListener) {
        this.onSeekStopListener = onSeekStopListener;
    }

    public void setProgressChangeListener(ProgressChangeListener progressChangeListener) {
        this.progressChangeListener = progressChangeListener;
    }

    public interface ProgressChangeListener {
        void onProgressChanged(int progress);
    }

    public interface OnSeekStartListener {
        void onSeekStart();
    }

    public interface OnSeekStopListener {
        void onSeekStop(int progress);
    }

}
