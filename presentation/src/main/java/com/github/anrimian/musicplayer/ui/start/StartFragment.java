package com.github.anrimian.musicplayer.ui.start;

import android.Manifest;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.di.Components;
import com.github.anrimian.musicplayer.di.app.AppComponent;
import com.github.anrimian.musicplayer.ui.player_screen.PlayerFragment;
import com.github.anrimian.musicplayer.ui.utils.wrappers.ProgressViewWrapper;
import com.tbruyelle.rxpermissions2.RxPermissions;

import moxy.MvpAppCompatFragment;
import moxy.presenter.InjectPresenter;

/**
 * Created on 19.10.2017.
 */

public class StartFragment extends MvpAppCompatFragment implements StartView {

    @InjectPresenter
    StartPresenter presenter;

    private ProgressViewWrapper progressViewWrapper;

    private RxPermissions rxPermissions;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_start, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rxPermissions = new RxPermissions(this);

        progressViewWrapper = new ProgressViewWrapper(view);
        progressViewWrapper.onTryAgainClick(presenter::onTryAgainButtonClicked);
    }

    @Override
    public void requestFilesPermissions() {
        rxPermissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe(presenter::onFilesPermissionResult);
    }

    @Override
    public void showDeniedPermissionMessage() {
        progressViewWrapper.showMessage(R.string.can_not_work_without_file_permission, true);
    }

    @Override
    public void showStub() {
        progressViewWrapper.hideAll();
    }

    @Override
    public void startSystemServices() {
        AppComponent appComponent = Components.getAppComponent();
        appComponent.widgetUpdater().start();
        appComponent.notificationDisplayer().removeErrorNotification();
        appComponent.mediaScannerRepository().runStorageObserver();
    }

    @Override
    public void goToMainScreen() {
        requireFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.anim_alpha_appear, R.anim.anim_alpha_disappear)
                .replace(R.id.main_activity_container, PlayerFragment.newInstance())
                .commit();
    }
}
