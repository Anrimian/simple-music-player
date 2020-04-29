package com.github.anrimian.musicplayer.domain.repositories;

import com.github.anrimian.musicplayer.domain.models.order.Order;

import io.reactivex.Observable;

/**
 * Created on 14.11.2017.
 */

public interface SettingsRepository {

    void setRandomPlayingEnabled(boolean enabled);

    boolean isRandomPlayingEnabled();

    Observable<Boolean> getRandomPlayingObservable();

    void setRepeatMode(int mode);

    int getRepeatMode();

    Observable<Integer> getRepeatModeObservable();

    void setFolderOrder(Order order);

    void setCompositionsOrder(Order order);

    Observable<Order> getFolderOrderObservable();

    Observable<Boolean> getCoversEnabledObservable();

    Observable<Boolean> getCoversInNotificationEnabledObservable();

    Observable<Boolean> getColoredNotificationEnabledObservable();

    Observable<Boolean> getCoversOnLockScreenEnabledObservable();

    void setCoversEnabled(boolean enabled);

    void setCoversInNotificationEnabled(boolean enabled);

    void setColoredNotificationEnabled(boolean enabled);

    void setCoversOnLockScreenEnabled(boolean enabled);

    Order getFolderOrder();

    Order getCompositionsOrder();

    Order getArtistsOrder();

    void setArtistsOrder(Order order);

    Observable<Order> getArtistsOrderObservable();

    Order getAlbumsOrder();

    void setAlbumsOrder(Order order);

    Observable<Order> getAlbumsOrderObservable();

    Order getGenresOrder();

    void setGenresOrder(Order order);

    Observable<Order> getGenresOrderObservable();

    int getSkipConstraintMillis();

    boolean isCoversEnabled();

    boolean isCoversInNotificationEnabled();

    boolean isColoredNotificationEnabled();

    boolean isCoversOnLockScreenEnabled();

    boolean isDecreaseVolumeOnAudioFocusLossEnabled();

    void setDecreaseVolumeOnAudioFocusLossEnabled(boolean enabled);
}
