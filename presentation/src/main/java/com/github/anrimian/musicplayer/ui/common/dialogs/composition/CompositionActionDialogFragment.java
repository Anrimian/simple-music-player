package com.github.anrimian.musicplayer.ui.common.dialogs.composition;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.AttrRes;
import androidx.annotation.MenuRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.databinding.DialogCompositionMenuBinding;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.utils.functions.BiCallback;
import com.github.anrimian.musicplayer.domain.utils.functions.TripleCallback;
import com.github.anrimian.musicplayer.ui.common.format.description.DescriptionSpannableStringBuilder;
import com.github.anrimian.musicplayer.ui.common.serialization.CompositionSerializer;
import com.github.anrimian.musicplayer.ui.utils.AndroidUtils;
import com.github.anrimian.musicplayer.ui.utils.ViewUtils;
import com.github.anrimian.musicplayer.ui.utils.dialogs.menu.MenuAdapter;
import com.github.anrimian.musicplayer.ui.utils.views.bottom_sheet.SimpleBottomSheetCallback;
import com.github.anrimian.musicplayer.ui.utils.views.delegate.BoundValuesDelegate;
import com.github.anrimian.musicplayer.ui.utils.views.delegate.SlideDelegate;
import com.github.anrimian.musicplayer.ui.utils.views.delegate.StatusBarColorDelegate;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import static android.view.View.MeasureSpec.makeMeasureSpec;
import static com.github.anrimian.musicplayer.Constants.Arguments.COMPOSITION_ARG;
import static com.github.anrimian.musicplayer.Constants.Arguments.EXTRA_DATA_ARG;
import static com.github.anrimian.musicplayer.Constants.Arguments.MENU_ARG;
import static com.github.anrimian.musicplayer.Constants.Arguments.STATUS_BAR_COLOR_ATTR_ARG;
import static com.github.anrimian.musicplayer.domain.models.utils.CompositionHelper.formatCompositionName;
import static com.github.anrimian.musicplayer.ui.common.dialogs.DialogUtils.setupBottomSheetDialogMaxWidth;
import static com.github.anrimian.musicplayer.ui.common.format.FormatUtils.formatCompositionAuthor;
import static com.github.anrimian.musicplayer.ui.common.format.FormatUtils.formatMilliseconds;
import static com.github.anrimian.musicplayer.ui.common.format.FormatUtils.formatSize;
import static com.github.anrimian.musicplayer.ui.utils.AndroidUtils.createMenu;
import static com.github.anrimian.musicplayer.ui.utils.AndroidUtils.getColorFromAttr;
import static com.github.anrimian.musicplayer.ui.utils.AndroidUtils.getContentView;
import static com.github.anrimian.musicplayer.ui.utils.AndroidUtils.getStatusBarHeight;
import static com.github.anrimian.musicplayer.ui.utils.views.recycler_view.RecyclerViewUtils.attachDynamicShadow;

public class CompositionActionDialogFragment extends BottomSheetDialogFragment {

    private DialogCompositionMenuBinding viewBinding;

    private Composition composition;

    private BiCallback<Composition, Integer> onCompleteListener;
    private TripleCallback<Composition, Integer, Bundle> onTripleCompleteListener;

    private SlideDelegate slideDelegate;

    public static CompositionActionDialogFragment newInstance(Composition composition,
                                                              @MenuRes int menu) {
        return newInstance(composition, menu, android.R.attr.statusBarColor);
    }

    public static CompositionActionDialogFragment newInstance(Composition composition,
                                                              @MenuRes int menu,
                                                              @AttrRes int statusBarColorAttr) {
        return newInstance(composition, menu, statusBarColorAttr, null);
    }

    public static CompositionActionDialogFragment newInstance(Composition composition,
                                                              @MenuRes int menu,
                                                              Bundle extra) {
        return newInstance(composition, menu, android.R.attr.statusBarColor, extra);
    }

    public static CompositionActionDialogFragment newInstance(Composition composition,
                                                              @MenuRes int menu,
                                                              @AttrRes int statusBarColorAttr,
                                                              Bundle extra) {
        Bundle args = new Bundle();
        args.putBundle(COMPOSITION_ARG, CompositionSerializer.serialize(composition));
        args.putInt(MENU_ARG, menu);
        args.putInt(STATUS_BAR_COLOR_ATTR_ARG, statusBarColorAttr);
        args.putBundle(EXTRA_DATA_ARG, extra);
        CompositionActionDialogFragment fragment = new CompositionActionDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void setupDialog(@NonNull Dialog dialog, int style) {
        super.setupDialog(dialog, style);
        viewBinding = DialogCompositionMenuBinding.inflate(LayoutInflater.from(getContext()));
        View view = viewBinding.getRoot();
        dialog.setContentView(view);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        viewBinding.recyclerView.setLayoutManager(layoutManager);
        attachDynamicShadow(viewBinding.recyclerView, viewBinding.titleShadow);

        Bundle args = requireArguments();
        //noinspection ConstantConditions
        composition = CompositionSerializer.deserialize(args.getBundle(COMPOSITION_ARG));

        Menu menu = createMenu(requireContext(), args.getInt(MENU_ARG));
        MenuAdapter menuAdapter = new MenuAdapter(menu, R.layout.item_menu);
        menuAdapter.setOnItemClickListener(this::onActionItemClicked);
        viewBinding.recyclerView.setAdapter(menuAdapter);

        viewBinding.tvCompositionName.setText(formatCompositionName(composition));

        SpannableStringBuilder sb = new DescriptionSpannableStringBuilder(requireContext());
        sb.append(formatCompositionAuthor(composition, requireContext()));
        sb.append(formatMilliseconds(composition.getDuration()));
        sb.append(formatSize(requireContext(), composition.getSize()));
        viewBinding.tvCompositionInfo.setText(sb);

        view.measure(
                makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        );

        slideDelegate = buildSlideDelegate();
        BottomSheetBehavior bottomSheetBehavior = ViewUtils.findBottomSheetBehavior(dialog);
        bottomSheetBehavior.addBottomSheetCallback(new SimpleBottomSheetCallback(newState -> {
            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                dismissAllowingStateLoss();
            }
        }, this::showBottomSheetSlided));//slide offset not working as expected, strange values
        bottomSheetBehavior.setPeekHeight(view.getMeasuredHeight());
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

        AndroidUtils.setDialogNavigationBarColorAttr(dialog, R.attr.dialogBackground);
    }

    @Override
    public void onResume() {
        super.onResume();
        setupBottomSheetDialogMaxWidth(this);
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        slideDelegate.onSlide(0f);
    }

    private void showBottomSheetSlided(float slideOffset) {
        viewBinding.recyclerView.post(() -> {
            View contentView = getContentView(getActivity());
            if (contentView == null) {
                return;
            }
            float usableSlideOffset = slideOffset;
            int activityHeight = contentView.getHeight() - getStatusBarHeight(requireContext());
            int viewHeight = viewBinding.listContainer.getHeight();
            if (activityHeight > viewHeight) {
                usableSlideOffset = 0;
            }
            slideDelegate.onSlide(usableSlideOffset);
        });
    }

    public void setOnCompleteListener(BiCallback<Composition, Integer> onCompleteListener) {
        this.onCompleteListener = onCompleteListener;
    }

    public void setOnTripleCompleteListener(TripleCallback<Composition, Integer, Bundle> onTripleCompleteListener) {
        this.onTripleCompleteListener = onTripleCompleteListener;
    }

    private void onActionItemClicked(MenuItem menuItem) {
        if (onCompleteListener != null) {
            onCompleteListener.call(composition, menuItem.getItemId());
        }
        if (onTripleCompleteListener != null) {
            //noinspection ConstantConditions
            onTripleCompleteListener.call(composition,
                    menuItem.getItemId(),
                    getArguments().getBundle(EXTRA_DATA_ARG));
        }
        dismissAllowingStateLoss();
    }

    private SlideDelegate buildSlideDelegate() {
        return new BoundValuesDelegate(0.85f, 1f,
                new StatusBarColorDelegate(requireActivity().getWindow(),
                        getColorFromAttr(getContext(), getStatusBarColorAttr()),
                        getColorFromAttr(requireContext(), R.attr.colorPrimaryDarkSecondary))
        );
    }

    @AttrRes
    private int getStatusBarColorAttr() {
        Bundle args = getArguments();
        if (args != null) {
            int colorAttr = args.getInt(STATUS_BAR_COLOR_ATTR_ARG);
            if (colorAttr != 0) {
                return colorAttr;
            }
        }
        return android.R.attr.statusBarColor;
    }
}
