package com.github.anrimian.musicplayer.ui.settings.display;

import com.github.anrimian.musicplayer.domain.interactors.settings.DisplaySettingsInteractor;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import moxy.InjectViewState;
import moxy.MvpPresenter;

@InjectViewState
public class DisplaySettingsPresenter extends MvpPresenter<DisplaySettingsView> {

    private final DisplaySettingsInteractor interactor;

    private final CompositeDisposable presenterDisposable = new CompositeDisposable();

    public DisplaySettingsPresenter(DisplaySettingsInteractor interactor) {
        this.interactor = interactor;
    }

    @Override
    protected void onFirstViewAttach() {
        super.onFirstViewAttach();
        getViewState().showCoversChecked(interactor.isCoversEnabled());
        getViewState().showCoversInNotificationChecked(interactor.isCoversInNotificationEnabled());
        getViewState().showColoredNotificationChecked(interactor.isColoredNotificationEnabled());
        getViewState().showCoversOnLockScreenChecked(interactor.isCoversOnLockScreenEnabled());

        subscribeOnCoversEnabledState();
        subscribeOnColoredNotificationEnabledState();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenterDisposable.dispose();
    }

    void onCoversChecked(boolean checked) {
        getViewState().showCoversChecked(checked);
        interactor.setCoversEnabled(checked);
    }

    void onCoversInNotificationChecked(boolean checked) {
        getViewState().showCoversInNotificationChecked(checked);
        interactor.setCoversInNotificationEnabled(checked);
    }

    void onColoredNotificationChecked(boolean checked) {
        getViewState().showColoredNotificationChecked(checked);
        interactor.setColoredNotificationEnabled(checked);
    }

    void onCoversOnLockScreenChecked(boolean checked) {
        getViewState().showCoversOnLockScreenChecked(checked);
        interactor.setCoversOnLockScreenEnabled(checked);
    }

    private void subscribeOnColoredNotificationEnabledState() {
        presenterDisposable.add(Observable.combineLatest(interactor.getCoversEnabledObservable(),
                interactor.getCoversInNotificationEnabledObservable(),
                (covers, notification) -> covers && notification)
                .subscribe(getViewState()::showColoredNotificationEnabled));
    }

    private void subscribeOnCoversEnabledState() {
        presenterDisposable.add(interactor.getCoversEnabledObservable()
                .subscribe(this::onCoversEnabled));
    }

    private void onCoversEnabled(boolean enabled) {
        getViewState().showCoversInNotificationEnabled(enabled);
        getViewState().showShowCoversOnLockScreenEnabled(enabled);
    }
}
