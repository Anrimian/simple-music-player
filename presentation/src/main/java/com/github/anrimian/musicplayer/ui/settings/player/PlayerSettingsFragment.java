package com.github.anrimian.musicplayer.ui.settings.player;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.data.controllers.music.equalizer.EqualizerTypes;
import com.github.anrimian.musicplayer.databinding.FragmentSettingsPlayerBinding;
import com.github.anrimian.musicplayer.di.Components;
import com.github.anrimian.musicplayer.ui.common.toolbar.AdvancedToolbar;
import com.github.anrimian.musicplayer.ui.equalizer.EqualizerChooserDialogFragment;
import com.github.anrimian.musicplayer.ui.utils.slidr.SlidrPanel;

import moxy.MvpAppCompatFragment;
import moxy.presenter.InjectPresenter;
import moxy.presenter.ProvidePresenter;

import static com.github.anrimian.musicplayer.ui.utils.ViewUtils.onCheckChanged;
import static com.github.anrimian.musicplayer.ui.utils.ViewUtils.setChecked;

public class PlayerSettingsFragment extends MvpAppCompatFragment implements PlayerSettingsView {

    @InjectPresenter
    PlayerSettingsPresenter presenter;

    private FragmentSettingsPlayerBinding viewBinding;

    @ProvidePresenter
    PlayerSettingsPresenter providePresenter() {
        return Components.getSettingsComponent().playerSettingsPresenter();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        viewBinding = FragmentSettingsPlayerBinding.inflate(inflater, container, false);
        return viewBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        AdvancedToolbar toolbar = requireActivity().findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.settings);
        toolbar.setSubtitle(R.string.playing);
        toolbar.setTitleClickListener(null);

        SlidrPanel.simpleSwipeBack(viewBinding.nsvContainer, this, toolbar::onStackFragmentSlided);

        onCheckChanged(viewBinding.cbDecreaseVolume, presenter::onDecreaseVolumeOnAudioFocusLossChecked);

        viewBinding.flEqualizerClickableArea.setOnClickListener(v -> showEqualizerDialog());
    }

    @Override
    public void showDecreaseVolumeOnAudioFocusLossEnabled(boolean checked) {
        setChecked(viewBinding.cbDecreaseVolume, checked);
    }

    @Override
    public void showSelectedEqualizerType(int type) {
        viewBinding.tvEqualizerState.setText(getEqualizerTypeDescription(type));
    }

    private int getEqualizerTypeDescription(int type) {
        switch (type) {
            case EqualizerTypes.NONE: {
                return R.string.no_equalizer;
            }
            case EqualizerTypes.EXTERNAL: {
                return R.string.system_equalizer;
            }
            default: return R.string.no_equalizer;
        }
    }

    private void showEqualizerDialog() {
        new EqualizerChooserDialogFragment().show(getChildFragmentManager(), null);
    }

}
