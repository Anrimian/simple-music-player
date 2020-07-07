package com.github.anrimian.musicplayer.ui.equalizer;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.data.controllers.music.equalizer.EqualizerController;
import com.github.anrimian.musicplayer.data.controllers.music.equalizer.EqualizerTypes;
import com.github.anrimian.musicplayer.data.controllers.music.equalizer.ExternalEqualizer;
import com.github.anrimian.musicplayer.databinding.DialogEqualizerChooserBinding;
import com.github.anrimian.musicplayer.di.Components;
import com.github.anrimian.musicplayer.ui.common.compat.CompatUtils;

import moxy.MvpAppCompatDialogFragment;

public class EqualizerChooserDialogFragment extends MvpAppCompatDialogFragment {

    private DialogEqualizerChooserBinding viewBinding;

    private EqualizerController equalizerController;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        viewBinding = DialogEqualizerChooserBinding.inflate(LayoutInflater.from(getContext()));

        equalizerController = Components.getAppComponent().equalizerController();

        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.equalizer)
                .setView(viewBinding.getRoot())
                .setNegativeButton(R.string.close, (dialog1, which) -> {})
                .create();
        dialog.show();

        viewBinding.rbUseSystemEqualizer.setOnClickListener(v -> enableSystemEqualizer());
        viewBinding.btnOpenSystemEqualizer.setOnClickListener(v -> openSystemEqualizer());
        viewBinding.rbDisableEqualizer.setOnClickListener(v -> disableEqualizer());

        showActiveEqualizer(equalizerController.getSelectedEqualizerType());

        CompatUtils.setOutlineButtonStyle(viewBinding.btnOpenSystemEqualizer);

        return dialog;
    }

    @Override
    public void onResume() {
        super.onResume();

        int textResId;
        boolean enabled;
        if (ExternalEqualizer.isExternalEqualizerExists(requireContext())) {
            textResId = R.string.external_equalizer_description;
            enabled = true;
        } else {
            textResId = R.string.external_equalizer_not_found;
            enabled = false;
        }
        viewBinding.btnOpenSystemEqualizer.setEnabled(enabled);
        viewBinding.rbUseSystemEqualizer.setEnabled(enabled);
        viewBinding.tvSystemEqualizerDescription.setEnabled(enabled);
        viewBinding.tvSystemEqualizerDescription.setText(getString(textResId));
    }

    private void enableSystemEqualizer() {
        equalizerController.enableExternalEqualizer(getActivity());
        showActiveEqualizer(EqualizerTypes.EXTERNAL);
    }

    private void openSystemEqualizer() {
        equalizerController.launchExternalEqualizerSetup(getActivity());
        showActiveEqualizer(EqualizerTypes.EXTERNAL);
    }

    private void disableEqualizer() {
        equalizerController.disableExternalEqualizer(requireContext());
        showActiveEqualizer(EqualizerTypes.NONE);
    }

    private void showActiveEqualizer(int type) {
        viewBinding.rbUseSystemEqualizer.setChecked(type == EqualizerTypes.EXTERNAL);
        viewBinding.rbDisableEqualizer.setChecked(type == EqualizerTypes.NONE);
    }

}
