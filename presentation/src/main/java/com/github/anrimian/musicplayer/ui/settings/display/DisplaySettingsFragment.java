package com.github.anrimian.musicplayer.ui.settings.display;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.databinding.FragmentSettingsDisplayBinding;
import com.github.anrimian.musicplayer.di.Components;
import com.github.anrimian.musicplayer.ui.common.toolbar.AdvancedToolbar;
import com.github.anrimian.musicplayer.ui.utils.slidr.SlidrPanel;

import moxy.MvpAppCompatFragment;
import moxy.presenter.InjectPresenter;
import moxy.presenter.ProvidePresenter;

import static com.github.anrimian.musicplayer.ui.utils.ViewUtils.onCheckChanged;
import static com.github.anrimian.musicplayer.ui.utils.ViewUtils.setChecked;

public class DisplaySettingsFragment extends MvpAppCompatFragment implements DisplaySettingsView {

    @InjectPresenter
    DisplaySettingsPresenter presenter;

    private FragmentSettingsDisplayBinding viewBinding;

    @ProvidePresenter
    DisplaySettingsPresenter providePresenter() {
        return Components.getSettingsComponent().displaySettingsPresenter();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        viewBinding = FragmentSettingsDisplayBinding.inflate(inflater, container, false);
        return viewBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        AdvancedToolbar toolbar = requireActivity().findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.settings);
        toolbar.setSubtitle(R.string.display);
        toolbar.setTitleClickListener(null);

        SlidrPanel.simpleSwipeBack(viewBinding.nsvContainer, this, toolbar::onStackFragmentSlided);

        viewBinding.cbColoredNotification.setVisibility(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O? View.VISIBLE: View.GONE);

        onCheckChanged(viewBinding.cbCovers, presenter::onCoversChecked);
        onCheckChanged(viewBinding.cbCoversInNotification, presenter::onCoversInNotificationChecked);
        onCheckChanged(viewBinding.cbColoredNotification, presenter::onColoredNotificationChecked);
        onCheckChanged(viewBinding.cbNotificationOnLockScreen, presenter::onCoversOnLockScreenChecked);
    }

    @Override
    public void showCoversChecked(boolean checked) {
        setChecked(viewBinding.cbCovers, checked);
    }

    @Override
    public void showCoversInNotificationChecked(boolean checked) {
        setChecked(viewBinding.cbCoversInNotification, checked);
    }

    @Override
    public void showColoredNotificationChecked(boolean checked) {
        setChecked(viewBinding.cbColoredNotification, checked);
    }

    @Override
    public void showCoversOnLockScreenChecked(boolean checked) {
        setChecked(viewBinding.cbNotificationOnLockScreen, checked);
    }

    @Override
    public void showCoversInNotificationEnabled(boolean enabled) {
        viewBinding.cbCoversInNotification.setEnabled(enabled);
    }

    @Override
    public void showColoredNotificationEnabled(boolean enabled) {
        viewBinding.cbColoredNotification.setEnabled(enabled);
    }

    @Override
    public void showShowCoversOnLockScreenEnabled(boolean enabled) {
        viewBinding.cbNotificationOnLockScreen.setEnabled(enabled);
    }
}
