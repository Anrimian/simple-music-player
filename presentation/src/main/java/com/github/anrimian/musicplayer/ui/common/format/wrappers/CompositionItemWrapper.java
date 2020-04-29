package com.github.anrimian.musicplayer.ui.common.format.wrappers;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.di.Components;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.composition.CorruptionType;
import com.github.anrimian.musicplayer.domain.utils.functions.Callback;
import com.github.anrimian.musicplayer.ui.common.compat.CompatUtils;
import com.github.anrimian.musicplayer.ui.common.format.description.DescriptionSpannableStringBuilder;
import com.github.anrimian.musicplayer.ui.utils.AndroidUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.github.anrimian.musicplayer.domain.Payloads.ARTIST;
import static com.github.anrimian.musicplayer.domain.Payloads.CORRUPTED;
import static com.github.anrimian.musicplayer.domain.Payloads.DURATION;
import static com.github.anrimian.musicplayer.domain.Payloads.FILE_NAME;
import static com.github.anrimian.musicplayer.domain.Payloads.TITLE;
import static com.github.anrimian.musicplayer.domain.models.utils.CompositionHelper.formatCompositionName;
import static com.github.anrimian.musicplayer.ui.common.format.ColorFormatUtils.getItemDragColor;
import static com.github.anrimian.musicplayer.ui.common.format.ColorFormatUtils.getPlayingCompositionColor;
import static com.github.anrimian.musicplayer.ui.common.format.FormatUtils.formatCompositionAuthor;
import static com.github.anrimian.musicplayer.ui.common.format.FormatUtils.formatMilliseconds;
import static com.github.anrimian.musicplayer.ui.utils.AndroidUtils.getColorFromAttr;
import static com.github.anrimian.musicplayer.ui.utils.ViewUtils.animateBackgroundColor;
import static com.github.anrimian.musicplayer.ui.utils.ViewUtils.animateVisibility;

public class CompositionItemWrapper {

    @BindView(R.id.tv_composition_name)
    TextView tvMusicName;

    @BindView(R.id.tv_additional_info)
    TextView tvAdditionalInfo;

    @BindView(R.id.clickable_item)
    FrameLayout clickableItem;

    @Nullable
    @BindView(R.id.iv_play)
    ImageView ivPlay;

    @Nullable
    @BindView(R.id.iv_music_icon)
    ImageView ivMusicIcon;

    @BindView(R.id.divider)
    View divider;

    @Nullable
    @BindView(R.id.btn_actions_menu)
    ImageView btnActionsMenu;

    @Nullable
    @BindView(R.id.icon_clickable_area)
    View iconClickableArea;

    private Composition composition;

    private boolean isCurrent;
    private boolean isDragging;

    public CompositionItemWrapper(View itemView,
                                  Callback<Composition> onIconClickListener,
                                  Callback<Composition> onClickListener) {
        ButterKnife.bind(this, itemView);
        if (iconClickableArea != null) {
            iconClickableArea.setOnClickListener(v -> onIconClickListener.call(composition));
        }
        clickableItem.setOnClickListener(v -> onClickListener.call(composition));

        if (btnActionsMenu != null) {
            CompatUtils.setSecondaryButtonStyle(btnActionsMenu);
        }
    }

    public void bind(Composition composition, boolean showCovers) {
        this.composition = composition;
        showCompositionName();
        showAdditionalInfo();
        showCorrupted();
        showCompositionImage(showCovers);

        showAsPlaying(false, false);
    }

    public void update(Composition composition, List<Object> payloads) {
        this.composition = composition;
        for (Object payload: payloads) {
            if (payload instanceof List) {
                //noinspection SingleStatementInBlock,unchecked
                update(composition, (List) payload);
            }
            if (payload == FILE_NAME || payload == TITLE) {
                showCompositionName();
            }
            if (payload == ARTIST || payload == DURATION) {
                showAdditionalInfo();
            }
            if (payload == CORRUPTED) {
                showCorrupted();
                showAdditionalInfo();
            }
        }
    }

    public void showCompositionImage(boolean showCovers) {
        if (ivMusicIcon != null) {
            if (showCovers) {
                Components.getAppComponent().imageLoader().displayImage(ivMusicIcon, composition);
            } else {
                ivMusicIcon.setImageResource(R.drawable.ic_music_placeholder_simple);
            }
        }
    }

    public void showAsDraggingItem(boolean dragging) {
        if (this.isDragging != dragging) {
            this.isDragging = dragging;

            animateVisibility(divider, dragging? View.INVISIBLE: View.VISIBLE);
            if (!dragging && isCurrent) {
                showAsCurrentCompositionInternal(true);
            } else {
                showAsDragging(dragging);
            }
        }
    }

    public void showAsCurrentComposition(boolean isCurrent) {
        if (this.isCurrent != isCurrent) {
            this.isCurrent = isCurrent;
            showAsCurrentCompositionInternal(isCurrent);
        }
    }

    public void showAsPlaying(boolean isPlaying, boolean animate) {
        if (ivPlay != null) {
            AndroidUtils.setAnimatedVectorDrawable(ivPlay,
                    isPlaying? R.drawable.anim_play_to_pause: R.drawable.anim_pause_to_play,
                    animate);
        }
    }

    private void showAsCurrentCompositionInternal(boolean isPlaying) {
        if (!isPlaying && isDragging) {
            showAsDragging(true);
            return;
        }
        int endColor = getPlayingCompositionColor(getContext(), isPlaying? 20: 0);
        animateBackgroundColor(clickableItem, endColor);
        clickableItem.setClickable(!isPlaying);
    }

    private void showCompositionName() {
        String compositionName = formatCompositionName(composition);
        tvMusicName.setText(compositionName);
        clickableItem.setContentDescription(compositionName);
        if (iconClickableArea != null) {
            iconClickableArea.setContentDescription(compositionName);
        }
    }

    private void showCorrupted() {
        float alpha = composition.getCorruptionType() == null? 1f: 0.5f;
        tvMusicName.setAlpha(alpha);
        tvAdditionalInfo.setAlpha(alpha);
        if (ivMusicIcon != null) {
            ivMusicIcon.setAlpha(alpha);
        }
        if (ivPlay != null) {
            ivPlay.setAlpha(alpha);
        }
        if (btnActionsMenu != null) {
            btnActionsMenu.setAlpha(alpha);
        }
    }

    private void showAsDragging(boolean dragging) {
        int endColor = getItemDragColor(getContext(), dragging? 20: 0);
        animateBackgroundColor(clickableItem, endColor);
    }

    private void showAdditionalInfo() {
        SpannableStringBuilder sb = new DescriptionSpannableStringBuilder(getContext());
        sb.append(formatCompositionAuthor(composition, getContext()));
        sb.append(formatMilliseconds(composition.getDuration()));
        String corruptionHint = getCorruptionTypeHint();
        if (corruptionHint != null) {
            sb.append(corruptionHint);
            int start = sb.length() - corruptionHint.length();
            int end = sb.length();

            ForegroundColorSpan fcs = new ForegroundColorSpan(getColorFromAttr(getContext(), R.attr.colorError));
            sb.setSpan(fcs, start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        }
        tvAdditionalInfo.setText(sb);
    }

    private String getCorruptionTypeHint() {
        CorruptionType corruptionType = composition.getCorruptionType();
        if (corruptionType == null) {
            return null;
        }
        switch (composition.getCorruptionType()) {
            case UNSUPPORTED: return getContext().getString(R.string.unsupported_format_hint);
            case NOT_FOUND: return getContext().getString(R.string.file_not_found);
            default: return null;
        }
    }

    private Context getContext() {
        return clickableItem.getContext();
    }
}
