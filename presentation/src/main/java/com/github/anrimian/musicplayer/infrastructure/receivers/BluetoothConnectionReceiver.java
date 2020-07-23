package com.github.anrimian.musicplayer.infrastructure.receivers;

import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import com.github.anrimian.musicplayer.di.Components;

import java.util.List;
import java.util.Objects;

import static android.bluetooth.BluetoothClass.Device.AUDIO_VIDEO_HANDSFREE;
import static android.bluetooth.BluetoothClass.Device.AUDIO_VIDEO_HEADPHONES;
import static android.bluetooth.BluetoothClass.Device.AUDIO_VIDEO_PORTABLE_AUDIO;
import static android.bluetooth.BluetoothClass.Device.AUDIO_VIDEO_UNCATEGORIZED;
import static android.bluetooth.BluetoothClass.Device.AUDIO_VIDEO_WEARABLE_HEADSET;
import static android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
import static android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
import static android.content.pm.PackageManager.DONT_KILL_APP;
import static com.github.anrimian.musicplayer.domain.utils.ListUtils.asList;

public class BluetoothConnectionReceiver extends BroadcastReceiver {

    private static List<Integer> ALLOWED_DEVICES_TO_START = asList(
            AUDIO_VIDEO_UNCATEGORIZED,
            AUDIO_VIDEO_WEARABLE_HEADSET,
            AUDIO_VIDEO_HANDSFREE,
            AUDIO_VIDEO_HEADPHONES,
            AUDIO_VIDEO_PORTABLE_AUDIO
    );

    public static void setEnabled(Context context, boolean enabled) {
        context.getPackageManager().setComponentEnabledSetting(
                new ComponentName(context, BluetoothConnectionReceiver.class),
                enabled? COMPONENT_ENABLED_STATE_ENABLED : COMPONENT_ENABLED_STATE_DISABLED,
                DONT_KILL_APP
        );
    }

    public static boolean isEnabled(Context context) {
        return context.getPackageManager()
                .getComponentEnabledSetting(
                        new ComponentName(context, BluetoothConnectionReceiver.class)
                ) == COMPONENT_ENABLED_STATE_ENABLED;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Objects.equals(intent.getAction(), BluetoothDevice.ACTION_ACL_CONNECTED)) {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            if (device != null) {
                BluetoothClass bluetoothClass = device.getBluetoothClass();
                int deviceClass = bluetoothClass.getDeviceClass();
                if (!ALLOWED_DEVICES_TO_START.contains(deviceClass)) {
                    return;
                }
            }
            Components.getAppComponent().musicPlayerInteractor().play();
        }
    }
}
