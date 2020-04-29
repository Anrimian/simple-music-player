package com.github.anrimian.musicplayer.ui.utils.wrappers;

import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

import com.github.anrimian.musicplayer.R;

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static com.github.anrimian.musicplayer.ui.utils.ViewUtils.animateVisibility;


/**
 * Created on 21.02.2016.
 */
public class ProgressViewWrapper {

    private static final int NO_DRAWABLE = -1;

    private static final int PROGRESS_SHOW_DELAY_MILLIS = 500;

    private ProgressBar progressBar;
    private TextView tvMessage;
    private View btnTryAgain;
    private ImageView ivEmpty;
    private View progressStateContainer;

    private Handler handler = new Handler();

    public ProgressViewWrapper(View view) {
        progressBar = view.findViewById(R.id.psv_progress_bar);
        tvMessage = view.findViewById(R.id.psv_tv_message);
        btnTryAgain = view.findViewById(R.id.psv_btn_action);
        ivEmpty = view.findViewById(R.id.psv_iv_empty);
        progressStateContainer = view.findViewById(R.id.progress_state_container);
    }

    /**
     * @deprecated use {@link #onTryAgainClick(Runnable)} instead
     */
    @Deprecated
    public void setTryAgainButtonOnClickListener(View.OnClickListener listener) {
        btnTryAgain.setOnClickListener(listener);
    }

    public void onTryAgainClick(Runnable listener) {
        btnTryAgain.setOnClickListener(v -> listener.run());
    }

    public void hideAll(Runnable onHidden) {
        animateVisibility(progressStateContainer, INVISIBLE, onHidden);
        progressStateContainer.setClickable(false);
        handler.removeCallbacksAndMessages(null);
    }

    public void hideAll() {
        progressStateContainer.setVisibility(INVISIBLE);
        progressStateContainer.setClickable(false);
        handler.removeCallbacksAndMessages(null);
    }

    public void goneAll() {
        progressStateContainer.setVisibility(GONE);
        handler.removeCallbacksAndMessages(null);
    }

    public void showMessage(int messageId) {
        showMessage(messageId, false);
    }

    public void showMessage(int messageId, boolean showTryAgainButton) {
        showMessage(messageId, NO_DRAWABLE, showTryAgainButton);
    }

    public void showMessage(int messageId, @DrawableRes int emptyImageRes, boolean showTryAgainButton) {
        String message = getString(messageId);
        showMessage(message, emptyImageRes, showTryAgainButton);
    }

    public void showMessage(String message, boolean showTryAgainButton) {
        showMessage(message, NO_DRAWABLE, showTryAgainButton);
    }

    public void showMessage(String message, @DrawableRes int imageRes, boolean showTryAgainButton) {
        progressStateContainer.setVisibility(VISIBLE);
        progressStateContainer.setClickable(true);
        progressStateContainer.setContentDescription(message);
        progressBar.setVisibility(GONE);
        tvMessage.setVisibility(VISIBLE);

        if (message != null) {
            tvMessage.setText(message);
        }
        if (imageRes != NO_DRAWABLE) {
            ivEmpty.setImageResource(imageRes);
            ivEmpty.setVisibility(VISIBLE);
        } else {
            ivEmpty.setVisibility(GONE);
        }
        if (showTryAgainButton) {
            btnTryAgain.setVisibility(VISIBLE);
        } else {
            btnTryAgain.setVisibility(GONE);
        }

        handler.removeCallbacksAndMessages(null);
    }

    public void showProgress() {
        handler.postDelayed(this::showDelayedProgress, PROGRESS_SHOW_DELAY_MILLIS);
    }

    private void showDelayedProgress() {
        progressStateContainer.setContentDescription(getString(R.string.loading_progress));
        progressStateContainer.setVisibility(VISIBLE);
        progressStateContainer.setClickable(true);
        progressBar.setIndeterminate(true);
        animateVisibility(progressBar, VISIBLE);
        tvMessage.setVisibility(GONE);
        btnTryAgain.setVisibility(GONE);
        ivEmpty.setVisibility(GONE);
    }

    private String getString(@StringRes int resId) {
        return ivEmpty.getContext().getString(resId);
    }
}
