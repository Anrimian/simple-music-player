package com.github.anrimian.musicplayer.ui.notifications.builder;

import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationCompat.Builder;

import com.github.anrimian.musicplayer.R;

import static androidx.core.content.ContextCompat.getColor;
import static com.github.anrimian.musicplayer.ui.notifications.NotificationsDisplayer.FOREGROUND_CHANNEL_ID;

public class AppNotificationBuilder {

    public Builder buildMusicNotification(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return new Builder(context, FOREGROUND_CHANNEL_ID)
                    .setColor(getColor(context, R.color.default_notification_color));
        } else {
            return new Builder(context, FOREGROUND_CHANNEL_ID);
        }
    }
}
