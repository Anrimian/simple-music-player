package com.github.anrimian.musicplayer.ui.settings.headset;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.infrastructure.receivers.BluetoothConnectionReceiver;
import com.github.anrimian.musicplayer.ui.common.toolbar.AdvancedToolbar;
import com.github.anrimian.musicplayer.ui.utils.slidr.SlidrPanel;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.github.anrimian.musicplayer.ui.utils.ViewUtils.onCheckChanged;
import static com.github.anrimian.musicplayer.ui.utils.ViewUtils.setChecked;

public class HeadsetSettingsFragment extends Fragment {

    @BindView(R.id.nsv_container)
    NestedScrollView nsvContainer;

    @BindView(R.id.cb_play_on_connect)
    CheckBox cbDecreaseVolume;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings_headset, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        AdvancedToolbar toolbar = requireActivity().findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.settings);
        toolbar.setSubtitle(R.string.headset);
        toolbar.setTitleClickListener(null);

        SlidrPanel.simpleSwipeBack(nsvContainer, this, toolbar::onStackFragmentSlided);

        onCheckChanged(cbDecreaseVolume, checked ->
                BluetoothConnectionReceiver.setEnabled(requireContext(), checked)
        );

        setChecked(cbDecreaseVolume, BluetoothConnectionReceiver.isEnabled(requireContext()));
    }
}
