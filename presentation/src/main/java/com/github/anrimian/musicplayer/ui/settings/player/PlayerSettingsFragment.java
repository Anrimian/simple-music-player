package com.github.anrimian.musicplayer.ui.settings.player;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.di.Components;
import com.github.anrimian.musicplayer.ui.common.toolbar.AdvancedToolbar;
import com.github.anrimian.musicplayer.ui.utils.slidr.SlidrPanel;

import butterknife.BindView;
import butterknife.ButterKnife;
import moxy.MvpAppCompatFragment;
import moxy.presenter.InjectPresenter;
import moxy.presenter.ProvidePresenter;

import static com.github.anrimian.musicplayer.ui.utils.ViewUtils.onCheckChanged;
import static com.github.anrimian.musicplayer.ui.utils.ViewUtils.setChecked;

public class PlayerSettingsFragment extends MvpAppCompatFragment implements PlayerSettingsView {

    @InjectPresenter
    PlayerSettingsPresenter presenter;

    @BindView(R.id.nsv_container)
    NestedScrollView nsvContainer;

    @BindView(R.id.cb_decrease_volume)
    CheckBox cbDecreaseVolume;

    @ProvidePresenter
    PlayerSettingsPresenter providePresenter() {
        return Components.getSettingsComponent().playerSettingsPresenter();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings_player, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        AdvancedToolbar toolbar = requireActivity().findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.settings);
        toolbar.setSubtitle(R.string.playing);
        toolbar.setTitleClickListener(null);

        SlidrPanel.simpleSwipeBack(nsvContainer, this, toolbar::onStackFragmentSlided);

        onCheckChanged(cbDecreaseVolume, presenter::onDecreaseVolumeOnAudioFocusLossChecked);
    }

    @Override
    public void showDecreaseVolumeOnAudioFocusLossEnabled(boolean checked) {
        setChecked(cbDecreaseVolume, checked);
    }

}
