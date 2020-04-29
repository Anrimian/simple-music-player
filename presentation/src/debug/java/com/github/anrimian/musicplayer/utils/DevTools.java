package com.github.anrimian.musicplayer.utils;

import android.app.Application;

import com.github.anrimian.acrareportdialog.AcraReportDialog;

public class DevTools {

    public static void run(Application application) {
        AcraReportDialog.setupCrashDialog(application);
    }
}
