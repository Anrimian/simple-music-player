package com.github.anrimian.musicplayer.data.repositories.settings;

import android.content.Context;
import android.content.SharedPreferences;

import com.github.anrimian.musicplayer.data.controllers.music.equalizer.EqualizerTypes;
import com.github.anrimian.musicplayer.data.utils.preferences.SharedPreferencesHelper;
import com.github.anrimian.musicplayer.domain.models.order.Order;
import com.github.anrimian.musicplayer.domain.models.order.OrderType;
import com.github.anrimian.musicplayer.domain.models.player.modes.RepeatMode;
import com.github.anrimian.musicplayer.domain.repositories.SettingsRepository;

import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;

import static com.github.anrimian.musicplayer.data.utils.rx.RxUtils.withDefaultValue;
import static com.github.anrimian.musicplayer.domain.models.order.OrderType.ADD_TIME;
import static com.github.anrimian.musicplayer.domain.models.order.OrderType.COMPOSITION_COUNT;

/**
 * Created on 16.04.2018.
 */
public class SettingsRepositoryImpl implements SettingsRepository {

    private static final String PREFERENCES_NAME = "settings_preferences";

    private static final String RANDOM_PLAYING_ENABLED = "random_playing_enabled";
    private static final String REPEAT_MODE = "repeat_mode";
    private static final String FOLDER_ORDER = "folder_order";
    private static final String COMPOSITIONS_ORDER = "compositions_order";
    private static final String ARTISTS_ORDER = "artists_order";
    private static final String ALBUMS_ORDER = "albums_order";
    private static final String GENRES_ORDER = "genres_order";

    private static final String SHOW_COVERS = "show_covers";
    private static final String SHOW_COVERS_IN_NOTIFICATION = "show_covers_in_notification";
    private static final String COLORED_NOTIFICATION = "colored_notification";
    private static final String SHOW_COVERS_ON_LOCK_SCREEN = "show_covers_on_lock_screen";

    private static final String DECREASE_VOLUME_ON_AUDIO_FOCUS_LOSS = "decrease_volume_on_audio_focus_loss";
    private static final String SELECTED_EQUALIZER_TYPE = "selected_equalizer_type";

    private static final String EXTERNAL_PLAYER_REPEAT_MODE = "external_player_repeat_mode";
    private static final String EXTERNAL_PLAYER_KEEP_IN_BACKGROUND = "external_player_keep_in_background";

    private final BehaviorSubject<Integer> repeatModeSubject = BehaviorSubject.create();
    private final BehaviorSubject<Boolean> randomModeSubject = BehaviorSubject.create();
    private final BehaviorSubject<Order> folderOrderSubject = BehaviorSubject.create();
    private final BehaviorSubject<Order> artistsOrderSubject = BehaviorSubject.create();
    private final BehaviorSubject<Order> albumsOrderSubject = BehaviorSubject.create();
    private final BehaviorSubject<Order> genresOrderSubject = BehaviorSubject.create();
    private final BehaviorSubject<Boolean> showCoversSubject = BehaviorSubject.create();
    private final BehaviorSubject<Boolean> showCoversNotificationSubject = BehaviorSubject.create();
    private final BehaviorSubject<Boolean> coloredNotificationSubject = BehaviorSubject.create();
    private final BehaviorSubject<Boolean> showCoversOnLockScreenSubject = BehaviorSubject.create();
    private final BehaviorSubject<Integer> selectedEqualizerSubject = BehaviorSubject.create();

    private final BehaviorSubject<Integer> externalPlayerRepeatModeSubject = BehaviorSubject.create();

    private SharedPreferencesHelper preferences;

    public SettingsRepositoryImpl(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        this.preferences = new SharedPreferencesHelper(sharedPreferences);
    }

    @Override
    public void setRandomPlayingEnabled(boolean enabled) {
        preferences.putBoolean(RANDOM_PLAYING_ENABLED, enabled);
        randomModeSubject.onNext(enabled);
    }

    @Override
    public boolean isRandomPlayingEnabled() {
        return preferences.getBoolean(RANDOM_PLAYING_ENABLED);
    }

    @Override
    public Observable<Boolean> getRandomPlayingObservable() {
        return withDefaultValue(randomModeSubject, this::isRandomPlayingEnabled)
                .distinctUntilChanged();
    }

    @Override
    public void setRepeatMode(int mode) {
        preferences.putInt(REPEAT_MODE, mode);
        repeatModeSubject.onNext(mode);
    }

    @Override
    public int getRepeatMode() {
        return preferences.getInt(REPEAT_MODE, RepeatMode.NONE);
    }

    @Override
    public Observable<Integer> getRepeatModeObservable() {
        return withDefaultValue(repeatModeSubject, this::getRepeatMode);
    }

    @Override
    public Observable<Order> getFolderOrderObservable() {
        return withDefaultValue(folderOrderSubject, this::getFolderOrder);
    }

    @Override
    public Observable<Boolean> getCoversEnabledObservable() {
        return withDefaultValue(showCoversSubject, this::isCoversEnabled);
    }

    @Override
    public Observable<Boolean> getCoversInNotificationEnabledObservable() {
        return withDefaultValue(showCoversNotificationSubject, this::isCoversInNotificationEnabled);
    }

    @Override
    public Observable<Boolean> getColoredNotificationEnabledObservable() {
        return withDefaultValue(coloredNotificationSubject, this::isColoredNotificationEnabled);
    }

    @Override
    public Observable<Boolean> getCoversOnLockScreenEnabledObservable() {
        return withDefaultValue(showCoversOnLockScreenSubject, this::isCoversOnLockScreenEnabled);
    }

    @Override
    public void setCoversEnabled(boolean enabled) {
        if (enabled != isCoversEnabled()) {
            preferences.putBoolean(SHOW_COVERS, enabled);
            showCoversSubject.onNext(enabled);
        }
    }

    @Override
    public void setCoversInNotificationEnabled(boolean enabled) {
        if (enabled != isCoversInNotificationEnabled()) {
            preferences.putBoolean(SHOW_COVERS_IN_NOTIFICATION, enabled);
            showCoversNotificationSubject.onNext(enabled);
        }
    }

    @Override
    public void setColoredNotificationEnabled(boolean enabled) {
        if (enabled != isColoredNotificationEnabled()) {
            preferences.putBoolean(COLORED_NOTIFICATION, enabled);
            coloredNotificationSubject.onNext(enabled);
        }
    }

    @Override
    public void setCoversOnLockScreenEnabled(boolean enabled) {
        if (enabled != isCoversOnLockScreenEnabled()) {
            preferences.putBoolean(SHOW_COVERS_ON_LOCK_SCREEN, enabled);
            showCoversOnLockScreenSubject.onNext(enabled);
        }
    }

    @Override
    public Order getFolderOrder() {
        // + 1 means reversed order
        return orderFromInt(preferences.getInt(FOLDER_ORDER, ADD_TIME.getId() + 1));
    }

    @Override
    public void setFolderOrder(Order order) {
        preferences.putInt(FOLDER_ORDER, orderToInt(order));
        folderOrderSubject.onNext(order);
    }

    @Override
    public Order getCompositionsOrder() {
        // + 1 means reversed order
        return orderFromInt(preferences.getInt(COMPOSITIONS_ORDER, ADD_TIME.getId() + 1));
    }

    @Override
    public void setCompositionsOrder(Order order) {
        preferences.putInt(COMPOSITIONS_ORDER, orderToInt(order));
    }

    @Override
    public Order getArtistsOrder() {
        // + 1 means reversed order
        return orderFromInt(preferences.getInt(ARTISTS_ORDER, COMPOSITION_COUNT.getId() + 1));
    }

    @Override
    public void setArtistsOrder(Order order) {
        preferences.putInt(ARTISTS_ORDER, orderToInt(order));
        artistsOrderSubject.onNext(order);
    }

    @Override
    public Observable<Order> getArtistsOrderObservable() {
        return withDefaultValue(artistsOrderSubject, this::getArtistsOrder);
    }

    @Override
    public Order getAlbumsOrder() {
        // + 1 means reversed order
        return orderFromInt(preferences.getInt(ALBUMS_ORDER, COMPOSITION_COUNT.getId() + 1));
    }

    @Override
    public void setAlbumsOrder(Order order) {
        preferences.putInt(ALBUMS_ORDER, orderToInt(order));
        albumsOrderSubject.onNext(order);
    }

    @Override
    public Observable<Order> getAlbumsOrderObservable() {
        return withDefaultValue(albumsOrderSubject, this::getAlbumsOrder);
    }

    @Override
    public Order getGenresOrder() {
        // + 1 means reversed order
        return orderFromInt(preferences.getInt(GENRES_ORDER, COMPOSITION_COUNT.getId() + 1));
    }

    @Override
    public void setGenresOrder(Order order) {
        preferences.putInt(GENRES_ORDER, orderToInt(order));
        genresOrderSubject.onNext(order);
    }

    @Override
    public Observable<Order> getGenresOrderObservable() {
        return withDefaultValue(genresOrderSubject, this::getGenresOrder);
    }

    @Override
    public int getSkipConstraintMillis() {
        return 15000;//just 15 seconds. Reserved setting for future
    }

    @Override
    public boolean isCoversEnabled() {
        return preferences.getBoolean(SHOW_COVERS, true);
    }

    @Override
    public boolean isCoversInNotificationEnabled() {
        return preferences.getBoolean(SHOW_COVERS_IN_NOTIFICATION, true);
    }

    @Override
    public boolean isColoredNotificationEnabled() {
        return preferences.getBoolean(COLORED_NOTIFICATION, true);
    }

    @Override
    public boolean isCoversOnLockScreenEnabled() {
        return preferences.getBoolean(SHOW_COVERS_ON_LOCK_SCREEN, false);
    }

    @Override
    public boolean isDecreaseVolumeOnAudioFocusLossEnabled() {
        return preferences.getBoolean(DECREASE_VOLUME_ON_AUDIO_FOCUS_LOSS, true);
    }

    @Override
    public void setDecreaseVolumeOnAudioFocusLossEnabled(boolean enabled) {
        preferences.putBoolean(DECREASE_VOLUME_ON_AUDIO_FOCUS_LOSS, enabled);
    }

    @Override
    public void setExternalPlayerRepeatMode(int mode) {
        preferences.putInt(EXTERNAL_PLAYER_REPEAT_MODE, mode);
        externalPlayerRepeatModeSubject.onNext(mode);
    }

    @Override
    public int getExternalPlayerRepeatMode() {
        return preferences.getInt(EXTERNAL_PLAYER_REPEAT_MODE, RepeatMode.NONE);
    }

    @Override
    public Observable<Integer> getExternalPlayerRepeatModeObservable() {
        return withDefaultValue(externalPlayerRepeatModeSubject, this::getExternalPlayerRepeatMode);
    }

    @Override
    public void setExternalPlayerKeepInBackground(boolean enabled) {
        preferences.putBoolean(EXTERNAL_PLAYER_KEEP_IN_BACKGROUND, enabled);
    }

    @Override
    public boolean isExternalPlayerKeepInBackground() {
        return preferences.getBoolean(EXTERNAL_PLAYER_KEEP_IN_BACKGROUND, true);
    }

    @Override
    public void setSelectedEqualizerType(int type) {
        preferences.putInt(SELECTED_EQUALIZER_TYPE, type);
        selectedEqualizerSubject.onNext(type);
    }

    @Override
    public int getSelectedEqualizerType() {
        return preferences.getInt(SELECTED_EQUALIZER_TYPE, EqualizerTypes.NONE);
    }

    @Override
    public Observable<Integer> getSelectedEqualizerTypeObservable() {
        return withDefaultValue(selectedEqualizerSubject, this::getSelectedEqualizerType);
    }

    private Order orderFromInt(int order) {
        boolean reversed = false;
        if (order % 2 == 0) {
            reversed = true;
            order--;
        }
        return new Order(OrderType.fromId(order), reversed);
    }

    private int orderToInt(Order order) {
        int id = order.getOrderType().getId();
        if (order.isReversed()) {
            id++;
        }
        return id;
    }
}
