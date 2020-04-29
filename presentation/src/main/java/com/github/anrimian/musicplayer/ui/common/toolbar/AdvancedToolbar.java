package com.github.anrimian.musicplayer.ui.common.toolbar;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.MenuRes;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.graphics.drawable.DrawerArrowDrawable;
import androidx.appcompat.widget.ActionMenuView;
import androidx.appcompat.widget.Toolbar;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.domain.utils.functions.Callback;
import com.github.anrimian.musicplayer.ui.utils.AndroidUtils;
import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.FragmentNavigation;
import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.FragmentStackListener;
import com.github.anrimian.musicplayer.ui.utils.views.menu.ActionMenuUtil;
import com.github.anrimian.musicplayer.ui.utils.views.text_view.SimpleTextWatcher;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;

import static android.animation.ObjectAnimator.ofFloat;
import static android.text.TextUtils.isEmpty;
import static com.github.anrimian.musicplayer.Constants.Animation.TOOLBAR_ARROW_ANIMATION_TIME;
import static com.github.anrimian.musicplayer.ui.utils.AndroidUtils.getColorFromAttr;
import static com.github.anrimian.musicplayer.ui.utils.AndroidUtils.setStatusBarColor;
import static com.github.anrimian.musicplayer.ui.utils.ViewUtils.getBackgroundAnimator;
import static com.github.anrimian.musicplayer.ui.utils.ViewUtils.getColorAnimator;
import static com.github.anrimian.musicplayer.ui.utils.ViewUtils.getVisibilityAnimator;
import static com.github.anrimian.musicplayer.ui.utils.views.menu.ActionMenuUtil.setupMenu;

public class AdvancedToolbar extends FrameLayout {

    private static final String IN_SEARCH_MODE = "in_search_mode";
    private static final String IN_SELECTION_MODE = "in_selection_mode";
    private static final String IS_KEYBOARD_SHOWN = "is_keyboard_shown";

    private final FragmentStackListener stackChangeListener = new StackChangeListenerImpl();

    private Window window;

    private Toolbar toolbar;
    private View clTitleContainer;
    private TextView tvTitle;
    private TextView tvSubtitle;
    private View actionIcon;
    private EditText etSearch;
    private ActionMenuView actionMenuView;
    private FrameLayout flTitleArea;

    private View selectionModeContainer;
    private TextView tvSelectionCount;
    private ActionMenuView acvSelection;

    @ColorInt
    private int controlButtonColor;
    @ColorInt
    private int controlButtonActionModeColor;

    @ColorInt
    private int backgroundColor;
    @ColorInt
    private int backgroundActionModeColor;

    @ColorInt
    private int statusBarColor;
    @ColorInt
    private int statusBarActionModeColor;

    private FragmentNavigation navigation;
    private DrawerArrowDrawable drawerArrowDrawable;
    private BottomSheetListener bottomSheetListener;

    private Callback<String> textChangeListener;
    private Callback<String> textConfirmListener;
    private final BehaviorSubject<Boolean> searchModeSubject = BehaviorSubject.createDefault(false);
    private final BehaviorSubject<Boolean> selectionModeSubject = BehaviorSubject.createDefault(false);

    private boolean inSearchMode;
    private boolean inSelectionMode;

    public AdvancedToolbar(Context context) {
        super(context);
    }

    public AdvancedToolbar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public AdvancedToolbar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void initializeViews(Window window) {
        this.window = window;
        toolbar = findViewById(R.id.toolbar_internal);
        actionMenuView = findViewById(R.id.acv_main);
        clTitleContainer = findViewById(R.id.title_container);
        tvTitle = findViewById(R.id.tv_title);
        tvSubtitle = findViewById(R.id.tv_subtitle);
        actionIcon = findViewById(R.id.action_icon);
        etSearch = findViewById(R.id.et_search);
        flTitleArea = findViewById(R.id.fl_title_area);
        selectionModeContainer = findViewById(R.id.selection_mode_container);
        tvSelectionCount = findViewById(R.id.tv_selection_count);
        acvSelection = findViewById(R.id.acv_selection);
        etSearch.addTextChangedListener(new SimpleTextWatcher(this::onSearchTextChanged));
        etSearch.setOnEditorActionListener(this::onSearchTextViewAction);
        etSearch.setVisibility(INVISIBLE);
        actionIcon.setVisibility(GONE);
        selectionModeContainer.setVisibility(INVISIBLE);

        controlButtonColor = getColorFromAttr(getContext(), R.attr.toolbarTextColorPrimary);
        controlButtonActionModeColor = getColorFromAttr(getContext(), R.attr.actionModeTextColor);

        backgroundColor = getColorFromAttr(getContext(), R.attr.colorPrimary);
        backgroundActionModeColor = getColorFromAttr(getContext(), R.attr.actionModeBackgroundColor);

        statusBarColor = getColorFromAttr(window.getContext(), android.R.attr.statusBarColor);
        statusBarActionModeColor = getColorFromAttr(window.getContext(), R.attr.actionModeStatusBarColor);
    }

    public void setupWithActivity(AppCompatActivity activity) {
        //now its only using for back button
        activity.setSupportActionBar(toolbar);
        ActionBar actionBar = activity.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    public void setupWithNavigation(FragmentNavigation navigation,
                                    DrawerArrowDrawable drawerArrowDrawable,
                                    BottomSheetListener bottomSheetListener) {
        this.navigation = navigation;
        this.drawerArrowDrawable = drawerArrowDrawable;
        this.bottomSheetListener = bottomSheetListener;

        onFragmentStackChanged(navigation.getScreensCount(), true);
        navigation.addStackChangeListener(stackChangeListener);
    }

    public void setSearchModeEnabled(boolean enabled) {
        setSearchModeEnabled(enabled, true, false);
    }

    public void setSearchModeEnabled(boolean enabled,
                                     boolean showKeyboard,
                                     boolean jumpToState) {
        if (bottomSheetListener == null) {
            return;//uninitialized state
        }

        inSearchMode = enabled;
        searchModeSubject.onNext(enabled);

        etSearch.setVisibility(enabled? VISIBLE: GONE);
//        clTitleContainer.post(() -> clTitleContainer.setVisibility(enabled? INVISIBLE: VISIBLE));
        clTitleContainer.setVisibility(enabled? INVISIBLE: VISIBLE);
        getActionMenuView().setVisibility(enabled? GONE: VISIBLE);
        if (!isDrawerArrowLocked()) {
            setCommandButtonMode(!enabled, !jumpToState);
        }
        if (enabled) {
            etSearch.requestFocus();
            if (showKeyboard){
                AndroidUtils.showKeyboard(etSearch);
            }
        } else {
            etSearch.setText(null);
            AndroidUtils.hideKeyboard(etSearch);
        }
    }

    public void setupOptionsMenu(@MenuRes int menuResId, Callback<MenuItem> listener) {
        ActionMenuUtil.setupMenu(actionMenuView, menuResId, listener);
    }

    public void clearOptionsMenu() {
        ActionMenuUtil.setupMenu(actionMenuView, R.menu.empty_stub_menu, null);
    }

    public void release() {
        navigation.removeStackChangeListener(stackChangeListener);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable("superState", super.onSaveInstanceState());
        bundle.putBoolean(IN_SEARCH_MODE, inSearchMode);
        bundle.putBoolean(IN_SELECTION_MODE, inSelectionMode);
        bundle.putBoolean(IS_KEYBOARD_SHOWN, AndroidUtils.isKeyboardWasShown(etSearch));
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;

            boolean isInSearchMode = bundle.getBoolean(IN_SEARCH_MODE);
            boolean isKeyboardShown = bundle.getBoolean(IS_KEYBOARD_SHOWN);
            setSearchModeEnabled(isInSearchMode, isKeyboardShown, true);

            // disabled because folder screens action mode issues
//            boolean inSelectionMode = bundle.getBoolean(IN_SELECTION_MODE);
//            setSelectionModeEnabled(inSelectionMode, false);

            state = bundle.getParcelable("superState");
        }
        super.onRestoreInstanceState(state);
    }

    public CharSequence getTitle() {
        return tvTitle.getText();
    }

    public void setTitle(@StringRes int titleId) {
        setTitle(getContext().getString(titleId));
    }

    public void setTitle(CharSequence title) {
        tvTitle.setVisibility(isEmpty(title) ? GONE : VISIBLE);
        tvTitle.setText(title);
        flTitleArea.setContentDescription(title);
    }

    public CharSequence getSubtitle() {
        return tvSubtitle.getText();
    }

    public void setSubtitle(@StringRes int titleId) {
        setSubtitle(getContext().getString(titleId));
    }

    public void setSubtitle(CharSequence subtitle) {
        tvSubtitle.setVisibility(isEmpty(subtitle) ? GONE : VISIBLE);
        tvSubtitle.setText(subtitle);
        if (!isEmpty(subtitle)) {
            flTitleArea.setContentDescription(getTitle() + ", " + subtitle);
        }
    }

    public void setTitleClickListener(View.OnClickListener listener) {
        actionIcon.setVisibility(listener == null? GONE : VISIBLE);
        flTitleArea.setEnabled(listener != null);
        flTitleArea.setOnClickListener(listener);
    }

    public void onStackFragmentSlided(float offset) {
        if (navigation.getScreensCount() <= 2) {
            drawerArrowDrawable.setProgress(offset);
        }
    }

    public boolean isInSearchMode() {
        return inSearchMode;
    }

    public boolean isInActionMode() {
        return inSelectionMode;
    }

    public ActionMenuView getActionMenuView() {
        return actionMenuView;
    }

    public void setupSearch(Callback<String> textChangeListener, String text) {
        this.textChangeListener = textChangeListener;
        this.textConfirmListener = textChangeListener;
        etSearch.setText(text);
        setSearchModeEnabled(!isEmpty(text));
    }

    public Observable<Boolean> getSearchModeObservable() {
        return searchModeSubject;
    }

    public Observable<Boolean> getSelectionModeObservable() {
        return selectionModeSubject;
    }

    private void onFragmentStackChanged(int stackSize, boolean jumpToState) {
        boolean isRoot = stackSize <= 1;
        //hmm, not sure about search mode, check how it works
        if (isRoot && (bottomSheetListener.isExpanded() || isInSearchMode())) {
            return;
        }
        setCommandButtonMode(isRoot, !jumpToState);
    }

    private void setCommandButtonMode(boolean isBase, boolean animate) {
        float end = isBase? 0f : 1f;
        if (animate) {
            ValueAnimator objectAnimator = getControlButtonAnimator(isBase);
            objectAnimator.setDuration(TOOLBAR_ARROW_ANIMATION_TIME);
            objectAnimator.start();
        } else {
            drawerArrowDrawable.setProgress(end);
        }
    }

    private ValueAnimator getControlButtonAnimator(boolean isArrow) {
        float start = drawerArrowDrawable.getProgress();
        float end = isArrow? 0f : 1f;
        ValueAnimator objectAnimator = ofFloat(start, end);
        objectAnimator.addUpdateListener(animation ->
                drawerArrowDrawable.setProgress((float) animation.getAnimatedValue())
        );
        return objectAnimator;
    }

    private boolean onSearchTextViewAction(TextView v, int actionId, KeyEvent event) {
        if (textConfirmListener != null) {
            textConfirmListener.call(v.getText().toString());
            return true;
        }
        return true;
    }

    private void onSearchTextChanged(String text) {
        if (textChangeListener != null) {
            textChangeListener.call(text);
        }
    }

    public void setControlButtonProgress(float slideOffset) {
        if (!(navigation.getScreensCount() > 1 || inSearchMode || inSelectionMode)) {
            drawerArrowDrawable.setProgress(slideOffset);
        }
    }

    public void setControlButtonColor(@ColorInt int color) {
        drawerArrowDrawable.setColor(color);
    }

    public void setupSelectionModeMenu(@MenuRes int menuResource, Callback<MenuItem> listener) {
        setupMenu(acvSelection, menuResource, listener, 1);
    }

    public void editActionMenu(Callback<Menu> callback) {
        callback.call(acvSelection.getMenu());
    }

    public void showSelectionMode(int count) {
        if (count == 0 && inSelectionMode) {
            setSelectionModeEnabled(false, true);
        }
        if (count > 0) {
            if (!inSelectionMode) {
                setSelectionModeEnabled(true, true);
            }
            tvSelectionCount.setText(String.valueOf(count));
        }
    }

    public void setContentVisibility(int visibility) {
        clTitleContainer.setVisibility(visibility);
    }

    public void setContentAlpha(float alpha) {
        clTitleContainer.setAlpha(alpha);
    }

    private void setSelectionModeEnabled(boolean enabled, boolean animate) {
        inSelectionMode = enabled;
        selectionModeSubject.onNext(enabled);

        boolean isHamburger = !enabled;
        if ((!enabled && inSearchMode) || navigation.getScreensCount() > 1) {
            isHamburger = false;
        }

        int modeElementsVisibility = enabled? VISIBLE: INVISIBLE;
        int anotherElementsVisibility = enabled? INVISIBLE: VISIBLE;

        int startControlButtonColor = enabled? controlButtonColor: controlButtonActionModeColor;
        int endControlButtonColor = enabled? controlButtonActionModeColor: controlButtonColor;

        int startBackgroundColor = enabled? backgroundColor: backgroundActionModeColor;
        int endBackgroundColor = enabled? backgroundActionModeColor: backgroundColor;

        int startStatusBarColor = enabled? statusBarColor: statusBarActionModeColor;
        int endStatusBarColor = enabled? statusBarActionModeColor: statusBarColor;

        if (animate) {
            int duration = 300;

            AnimatorSet mainAnimatorSet = new AnimatorSet();
            mainAnimatorSet.setDuration(duration);
            mainAnimatorSet.play(getControlButtonAnimator(isHamburger))
                    .with(getColorAnimator(startControlButtonColor,
                            endControlButtonColor,
                            drawerArrowDrawable::setColor)
                    )
                    .with(getBackgroundAnimator(this, startBackgroundColor, endBackgroundColor))
                    .with(getColorAnimator(startStatusBarColor,
                            endStatusBarColor,
                            color -> setStatusBarColor(window, color)));

            List<Animator> baseAnimators = new ArrayList<>();
            if (inSearchMode) {
                baseAnimators.add(getVisibilityAnimator(etSearch, anotherElementsVisibility));
            } else {
                baseAnimators.add(getVisibilityAnimator(clTitleContainer, anotherElementsVisibility));
                baseAnimators.add(getVisibilityAnimator(getActionMenuView(), anotherElementsVisibility));
            }
            AnimatorSet baseElementsAnimator = new AnimatorSet();
            baseElementsAnimator.playTogether(baseAnimators);
            baseElementsAnimator.setDuration(duration/2);

            List<Animator> modeAnimators = new ArrayList<>();
            modeAnimators.add(getVisibilityAnimator(selectionModeContainer, modeElementsVisibility));
            AnimatorSet modeElementsAnimator = new AnimatorSet();
            modeElementsAnimator.playTogether(modeAnimators);
            modeElementsAnimator.setDuration(duration/2);

            AnimatorSet combinedAnimator = new AnimatorSet();
            if (enabled) {
                combinedAnimator.play(baseElementsAnimator).before(modeElementsAnimator);
            } else {
                combinedAnimator.play(modeElementsAnimator).before(baseElementsAnimator);
            }

            AnimatorSet finalAnimatorSet = new AnimatorSet();
            finalAnimatorSet.play(mainAnimatorSet)
                    .with(combinedAnimator);
            finalAnimatorSet.setInterpolator(enabled? new DecelerateInterpolator(): new AccelerateInterpolator());

            finalAnimatorSet.start();
        } else {
            setCommandButtonMode(isHamburger, false);
            if (inSearchMode) {
                etSearch.setVisibility(anotherElementsVisibility);
            } else {
                clTitleContainer.setVisibility(anotherElementsVisibility);
                getActionMenuView().setVisibility(anotherElementsVisibility);
            }
            selectionModeContainer.setVisibility(modeElementsVisibility);
            drawerArrowDrawable.setColor(endControlButtonColor);
            setBackgroundColor(endBackgroundColor);
            setStatusBarColor(window, endStatusBarColor);
        }
    }

    private boolean isDrawerArrowLocked() {
        return bottomSheetListener.isExpanded() || navigation.getScreensCount() > 1;
    }

    public interface BottomSheetListener {
        boolean isExpanded();
    }

    private class StackChangeListenerImpl implements FragmentStackListener {

        @Override
        public void onStackChanged(int stackSize) {
            onFragmentStackChanged(navigation.getScreensCount(), false);
        }
    }
}
