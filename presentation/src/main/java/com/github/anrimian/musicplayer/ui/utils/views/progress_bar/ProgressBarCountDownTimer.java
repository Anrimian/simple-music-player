package com.github.anrimian.musicplayer.ui.utils.views.progress_bar;

import android.os.Build;
import android.os.CountDownTimer;
import android.widget.ProgressBar;

public class ProgressBarCountDownTimer extends CountDownTimer {

    private final long totalTimeMs;
    private final ProgressBar progressBar;

    public ProgressBarCountDownTimer(long millisInFuture,
                                     long countDownInterval,
                                     ProgressBar progressBar) {
        super(millisInFuture, countDownInterval);
        this.totalTimeMs = millisInFuture;
        this.progressBar = progressBar;
    }

    @Override
    public void onTick(long millisUntilFinished) {
        int progress = (int) ((totalTimeMs - millisUntilFinished) * 100 / totalTimeMs);
        setProgress(progressBar, progress);
    }

    @Override
    public void onFinish() {
        setProgress(progressBar, 100);
    }

    private void setProgress(ProgressBar pb, int progress) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            pb.setProgress(progress, true);
        } else {
            pb.setProgress(progress);
        }
    }

}
