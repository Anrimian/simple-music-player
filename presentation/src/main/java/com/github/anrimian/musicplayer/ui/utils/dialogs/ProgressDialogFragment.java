package com.github.anrimian.musicplayer.ui.utils.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.fragment.app.DialogFragment;

import com.github.anrimian.musicplayer.Constants;
import com.github.anrimian.musicplayer.R;

import static com.github.anrimian.musicplayer.Constants.Tags.MESSAGE_ARG;
import static com.github.anrimian.musicplayer.Constants.Tags.MESSAGE_RES_ARG;

public class ProgressDialogFragment extends DialogFragment {

    public static ProgressDialogFragment newInstance(String message) {
        Bundle args = new Bundle();
        args.putString(MESSAGE_ARG, message);
        ProgressDialogFragment fragment = new ProgressDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static ProgressDialogFragment newInstance(@StringRes int messageResId) {
        Bundle args = new Bundle();
        args.putInt(MESSAGE_RES_ARG, messageResId);
        ProgressDialogFragment fragment = new ProgressDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(false);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = View.inflate(getActivity(), R.layout.dialog_progress, null);

        TextView tvProgress = view.findViewById(R.id.tv_progress);
        tvProgress.setText(getMessage());

        return new AlertDialog.Builder(getActivity())
                .setView(view)
                .setCancelable(false)
                .create();
    }

    private String getMessage() {
        Bundle args = getArguments();
        assert args != null;
        int resId = args.getInt(MESSAGE_RES_ARG);
        if (resId != 0) {
            return getString(resId);
        }
        return args.getString(MESSAGE_ARG);
    }
}
