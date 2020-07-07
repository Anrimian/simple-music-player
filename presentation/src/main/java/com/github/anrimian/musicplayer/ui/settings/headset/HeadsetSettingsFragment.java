package com.github.anrimian.musicplayer.ui.settings.headset;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.databinding.FragmentSettingsHeadsetBinding;
import com.github.anrimian.musicplayer.infrastructure.receivers.BluetoothConnectionReceiver;
import com.github.anrimian.musicplayer.ui.common.toolbar.AdvancedToolbar;
import com.github.anrimian.musicplayer.ui.utils.slidr.SlidrPanel;

import static com.github.anrimian.musicplayer.ui.utils.ViewUtils.onCheckChanged;
import static com.github.anrimian.musicplayer.ui.utils.ViewUtils.setChecked;

public class HeadsetSettingsFragment extends Fragment {

    private FragmentSettingsHeadsetBinding viewBinding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        viewBinding = FragmentSettingsHeadsetBinding.inflate(inflater, container, false);
        return viewBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        AdvancedToolbar toolbar = requireActivity().findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.settings);
        toolbar.setSubtitle(R.string.headset);
        toolbar.setTitleClickListener(null);

        SlidrPanel.simpleSwipeBack(viewBinding.nsvContainer, this, toolbar::onStackFragmentSlided);

        onCheckChanged(viewBinding.cbPlayOnConnect, checked ->
                BluetoothConnectionReceiver.setEnabled(requireContext(), checked)
        );

        setChecked(viewBinding.cbPlayOnConnect, BluetoothConnectionReceiver.isEnabled(requireContext()));
    }
}
