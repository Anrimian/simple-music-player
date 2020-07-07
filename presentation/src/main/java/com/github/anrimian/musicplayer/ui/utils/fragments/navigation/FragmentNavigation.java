package com.github.anrimian.musicplayer.ui.utils.fragments.navigation;

import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.AnimRes;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.github.anrimian.musicplayer.domain.utils.ListUtils.mapList;

@SuppressWarnings("WeakerAccess")
public class FragmentNavigation {

    private static final String NAVIGATION_FRAGMENT_TAG = "navigation_fragment_tag";
    private static final String SCREENS = "screens";

    private final FragmentManagerProvider fragmentManagerProvider;
    private final LinkedList<FragmentMetaData> screens = new LinkedList<>();
    private final List<FragmentStackListener> stackListeners = new LinkedList<>();

    private final Handler actionHandler = new Handler(Looper.getMainLooper());
    private final Object backLock = new Object();
    private final ExecutorService actionExecutor = Executors.newSingleThreadExecutor();

    private JugglerView jugglerView;

    private boolean checkOnEqualityOnReplace = false;

    @AnimRes private int enterAnimation = 0;
    @AnimRes private int exitAnimation = 0;
    @AnimRes private int rootEnterAnimation = 0;
    @AnimRes private int rootExitAnimation = 0;

    private boolean isVisible = true;

    @Nullable
    private Runnable bottomFragmentRunnable;

    public static FragmentNavigation from(FragmentManager fm) {
        NavigationFragment container = (NavigationFragment) fm.findFragmentByTag(NAVIGATION_FRAGMENT_TAG);
        if (container == null) {
            container = new NavigationFragment();
            fm.beginTransaction()
                    .add(container, NAVIGATION_FRAGMENT_TAG)
                    .commitAllowingStateLoss();
        }
        return container.getFragmentNavigation();
    }

    FragmentNavigation(FragmentManagerProvider fragmentManagerProvider) {
        this.fragmentManagerProvider = fragmentManagerProvider;
    }

    public void initialize(@androidx.annotation.NonNull JugglerView jugglerView, @Nullable Bundle savedState) {
        this.jugglerView = jugglerView;
        jugglerView.initialize(savedState);

        hideBottomFragmentMenu();

        if (!screens.isEmpty()) {//just config change
            notifyFragmentMovedToTop(getFragmentOnTop());
            return;
        }
        if (savedState != null) {
            ArrayList<Bundle> bundleFragments = savedState.getParcelableArrayList(SCREENS);
            if (bundleFragments != null) {
                screens.addAll(mapList(bundleFragments, FragmentMetaData::new));
                notifyFragmentMovedToTop(getFragmentOnTop());
            }
        }
    }

    public void onSaveInstanceState(Bundle state) {
        jugglerView.saveInstanceState(state);
        state.putParcelableArrayList(SCREENS, getBundleScreens());
    }

    public void newRootFragmentStack(List<Fragment> fragments) {
        newRootFragmentStack(fragments, rootExitAnimation);
    }

    public void newRootFragmentStack(List<Fragment> fragments,
                                     @AnimRes int exitAnimation) {
        newRootFragmentStack(fragments, exitAnimation, rootEnterAnimation);
    }

    public void newRootFragmentStack(List<Fragment> fragments,
                                     @AnimRes int exitAnimation,
                                     @AnimRes int enterAnimation) {
        checkForInitialization();

        runForwardAction(() -> {
            if (fragments.isEmpty()) {
                return;
            }
            if (fragments.size() == 1) {
                newRootFragment(fragments.get(0), exitAnimation, enterAnimation);
                return;
            }

            screens.clear();
            screens.addAll(mapList(fragments, FragmentMetaData::new));
            int id = jugglerView.getTopViewId();

            Fragment fragment = fragments.get(fragments.size() - 1);
            fragment.setMenuVisibility(isVisible);
            fragmentManagerProvider.getFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(enterAnimation, exitAnimation)
                    .replace(id, fragment)
                    .runOnCommit(() -> {
                        hideBottomFragmentMenu();
                        notifyStackListeners();
                        notifyFragmentMovedToTop(getFragmentOnTop());

                        if (fragments.size() > 1) {
                            scheduleBottomFragmentReplacing(getAnimationDuration(enterAnimation));
                        }

                    })
                    .commitAllowingStateLoss();
        });
    }

    public void addNewFragmentStack(List<Fragment> fragments) {
        addNewFragmentStack(fragments, enterAnimation);
    }

    public void addNewFragmentStack(List<Fragment> fragments, @AnimRes int enterAnimation) {
        checkForInitialization();

        runForwardAction(() -> {
            if (fragments.isEmpty()) {
                return;
            }
            if (fragments.size() == 1) {
                addNewFragment(fragments.get(0), enterAnimation);
                return;
            }

            screens.addAll(mapList(fragments, FragmentMetaData::new));
            int id = jugglerView.prepareTopView();

            Fragment fragment = fragments.get(fragments.size() - 1);
            fragment.setMenuVisibility(isVisible);
            fragmentManagerProvider.getFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(enterAnimation, 0)
                    .replace(id, fragment)
                    .runOnCommit(() -> {
                        hideBottomFragmentMenu();
                        notifyStackListeners();
                        notifyFragmentMovedToTop(getFragmentOnTop());

                        if (fragments.size() > 1) {
                            scheduleBottomFragmentReplacing(getAnimationDuration(enterAnimation));
                        }

                    })
                    .commitAllowingStateLoss();
        });
    }

    public void addNewFragment(Fragment fragment) {
        addNewFragment(fragment, enterAnimation);
    }

    public void addNewFragment(Fragment fragment,
                               @AnimRes int enterAnimation) {
        checkForInitialization();

        runForwardAction(() -> {
            screens.add(new FragmentMetaData(fragment));
            int id = jugglerView.prepareTopView();
            fragment.setMenuVisibility(isVisible);
            fragmentManagerProvider.getFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(enterAnimation, 0)
                    .replace(id, fragment)
                    .runOnCommit(() -> {
                        hideBottomFragmentMenu();
                        notifyStackListeners();
                        notifyFragmentMovedToTop(getFragmentOnTop());
                    })
                    .commitAllowingStateLoss();
        });
    }

    public void newRootFragment(Fragment fragment) {
        newRootFragment(fragment, checkOnEqualityOnReplace, rootExitAnimation);
    }

    public void newRootFragment(Fragment fragment, boolean checkForEquality) {
        newRootFragment(fragment, checkForEquality, rootExitAnimation);
    }

    public void newRootFragment(Fragment fragment,
                                boolean checkForEquality,
                                @AnimRes int exitAnimation) {
        newRootFragment(fragment, checkForEquality, exitAnimation, rootEnterAnimation);
    }

    public void newRootFragment(Fragment fragment, @AnimRes int exitAnimation) {
        newRootFragment(fragment, checkOnEqualityOnReplace, exitAnimation, rootEnterAnimation);
    }

    public void newRootFragment(Fragment fragment,
                                @AnimRes int exitAnimation,
                                @AnimRes int enterAnimation) {
        newRootFragment(fragment, checkOnEqualityOnReplace, exitAnimation, enterAnimation);
    }

    public void newRootFragment(Fragment newRootFragment,
                                boolean checkForEquality,
                                @AnimRes int exitAnimation,
                                @AnimRes int enterAnimation) {
        checkForInitialization();

        runForwardAction(() -> {
            Fragment oldRootFragment = getFragmentOnTop();
            if (checkForEquality && equalClass(oldRootFragment, newRootFragment)) {
                return;
            }

            screens.clear();
            screens.add(new FragmentMetaData(newRootFragment));
            int topViewId = jugglerView.getTopViewId();
            newRootFragment.setMenuVisibility(isVisible);
            FragmentManager fm = fragmentManagerProvider.getFragmentManager();
            if (fm == null) {
                return;
            }
            fm.beginTransaction()
                    .setCustomAnimations(enterAnimation, exitAnimation)
                    .replace(topViewId, newRootFragment)
                    .runOnCommit(() -> {
                        notifyStackListeners();
                        notifyFragmentMovedToTop(getFragmentOnTop());
                        scheduleBottomFragmentClearing(getAnimationDuration(enterAnimation));
                    })
                    .commitAllowingStateLoss();
        });
    }

    public boolean goBack() {
        return goBack(exitAnimation);
    }

    /**
     *
     * @return if back accepted or not
     */
    //back+forward problem
    //back+root problem
    public boolean goBack(@AnimRes int exitAnimation) {
        checkForInitialization();
        if (screens.size() <= 1) {
            return false;
        }
        actionExecutor.execute(() -> runBackAction(exitAnimation));
        return true;
    }

    public void clearFragmentStack(@AnimRes int exitAnimation) {
        checkForInitialization();
        runForwardAction(() -> {
            if (screens.size() < 1) {
                return;
            }
            Fragment fragmentOnTop = getFragmentOnTop();
            if (fragmentOnTop == null) {
                return;
            }
            Fragment fragmentOnBottom = getFragmentOnBottom();

            screens.removeLast();
            FragmentTransaction ft = fragmentManagerProvider.getFragmentManager()
                    .beginTransaction();
            ft.setCustomAnimations(0, exitAnimation)
                    .remove(fragmentOnTop);
            if (fragmentOnBottom != null) {
                ft.remove(fragmentOnBottom);
            }
            ft.runOnCommit(this::notifyStackListeners)
                    .commitAllowingStateLoss();
        });
    }

    /**
     *
     * Don't forget to remove listener if you don't need it more
     *
     * @param listener to notify
     */
    public void addStackChangeListener(FragmentStackListener listener) {
        stackListeners.add(listener);
    }

    public void removeStackChangeListener(FragmentStackListener listener) {
        stackListeners.remove(listener);
    }

    public void clearStackChangeListeners() {
        stackListeners.clear();
    }

    public void checkForEqualityOnReplace(boolean checkOnEqualityOnReplace) {
        this.checkOnEqualityOnReplace = checkOnEqualityOnReplace;
    }

    public void setEnterAnimation(int enterAnimation) {
        this.enterAnimation = enterAnimation;
    }

    public void setExitAnimation(int exitAnimation) {
        this.exitAnimation = exitAnimation;
    }

    public void setRootEnterAnimation(int rootEnterAnimation) {
        this.rootEnterAnimation = rootEnterAnimation;
    }

    public void setRootExitAnimation(int rootExitAnimation) {
        this.rootExitAnimation = rootExitAnimation;
    }

    public int getScreensCount() {
        return screens.size();
    }

    public int getStackScreensCount() {
        int count = screens.size() - 1;
        return Math.max(count, 0);
    }

    public boolean hasScreens() {
        return !screens.isEmpty();
    }

    @Nullable
    public Fragment getFragmentOnTop() {
        FragmentManager fm = fragmentManagerProvider.getFragmentManager();
        if (fm == null) {
            return null;
        }
        return fm.findFragmentById(jugglerView.getTopViewId());
    }

    @Nullable
    public Fragment getFragmentOnBottom() {
        FragmentManager fm = fragmentManagerProvider.getFragmentManager();
        if (fm == null) {
            return null;
        }
        return fm.findFragmentById(jugglerView.getBottomViewId());
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void setMenuVisible(boolean visible) {
        isVisible = visible;
        Fragment fragment = getFragmentOnTop();
        if (fragment != null) {
            fragment.setMenuVisibility(visible);
        }
    }

    public void dispatchMovedToTop() {
        Fragment fragment = getFragmentOnTop();
        if (fragment != null) {
            notifyFragmentMovedToTop(fragment);
        }
    }

    public boolean isInitialized() {
        return jugglerView != null;
    }

    private void notifyFragmentMovedToTop(Fragment fragment) {
        if (isVisible && fragment instanceof FragmentLayerListener) {
            ((FragmentLayerListener) fragment).onFragmentMovedOnTop();
        }
    }

    private void notifyStackListeners() {
        for (FragmentStackListener listener: stackListeners) {
            listener.onStackChanged(getScreensCount());
        }
    }

    private void scheduleBottomFragmentReplacing(long delay) {
        if (bottomFragmentRunnable != null) {
            actionHandler.removeCallbacks(bottomFragmentRunnable);
        }
        bottomFragmentRunnable = this::silentlyReplaceBottomFragment;
        actionHandler.postDelayed(bottomFragmentRunnable, delay);
    }

    private void silentlyReplaceBottomFragment() {
        if (screens.size() > 1) {
            FragmentMetaData bottomFragment = screens.get(screens.size() - 2);
            FragmentManager fm = fragmentManagerProvider.getFragmentManager();
            if (fm == null) {
                //can be null in very fast create-close case
                return;
            }
            fm.beginTransaction()
                    .replace(jugglerView.getBottomViewId(), createFragment(bottomFragment))
                    .runOnCommit(this::hideBottomFragmentMenu)
                    .commitAllowingStateLoss();
        }
    }

    private void scheduleBottomFragmentClearing(long delay) {
        if (bottomFragmentRunnable != null) {
            actionHandler.removeCallbacks(bottomFragmentRunnable);
        }
        bottomFragmentRunnable = this::silentlyClearBottomFragment;
        actionHandler.postDelayed(bottomFragmentRunnable, delay);
    }

    private void silentlyClearBottomFragment() {
        Fragment fragment = getFragmentOnBottom();
        if (fragment != null) {
            fragmentManagerProvider.getFragmentManager()
                    .beginTransaction()
                    .remove(fragment)
                    .runOnCommit(() -> fragment.setMenuVisibility(false))
                    .commitAllowingStateLoss();
        }
    }

    private void runForwardAction(Runnable runnable) {
        actionExecutor.execute(() -> actionHandler.post(runnable));
    }

    private void runBackAction(int exitAnimation) {
        synchronized (backLock) {
            actionHandler.post(() -> {
                synchronized (backLock) {
                    if (screens.size() <= 1) {
                        backLock.notify();
                        return;
                    }
                    Fragment fragmentOnTop = getFragmentOnTop();
                    if (fragmentOnTop == null) {
                        backLock.notify();
                        return;
                    }

                    screens.removeLast();

                    fragmentManagerProvider.getFragmentManager()
                            .beginTransaction()
                            .setCustomAnimations(0, exitAnimation)
                            .remove(fragmentOnTop)
                            .runOnCommit(() -> {
                                Fragment fragment = getFragmentOnBottom();
                                if (fragment != null) {
                                    fragment.setMenuVisibility(true);
                                    notifyFragmentMovedToTop(fragment);
                                }
                                notifyStackListeners();
                                scheduleFragmentAtBottomReplacing(getAnimationDuration(exitAnimation));
                            })
                            .commitAllowingStateLoss();
                }
            });
            try {
                backLock.wait();
            } catch (InterruptedException ignored) {}
        }
    }

    private void scheduleFragmentAtBottomReplacing(long delay) {
        jugglerView.postDelayed(this::replaceFragmentAtBottom, delay);
    }

    private void replaceFragmentAtBottom() {
        int id = jugglerView.prepareBottomView();
        if (screens.size() > 1) {
            FragmentMetaData metaData = screens.get(screens.size() - 2);
            Fragment bottomFragment = createFragment(metaData);//can be null here
            bottomFragment.setMenuVisibility(false);
            fragmentManagerProvider.getFragmentManager()
                    .beginTransaction()
                    .replace(id, bottomFragment)
                    .commitAllowingStateLoss();
        }
        synchronized (backLock) {
            backLock.notify();
        }
    }

    private void hideBottomFragmentMenu() {
        Fragment fragment = getFragmentOnBottom();
        if (fragment != null) {
            fragment.setMenuVisibility(false);
        }
    }

    private long getAnimationDuration(@AnimRes int exitAnimation) {
        if (exitAnimation == 0) {
            return 0;
        }
        try {
            Animation animation = AnimationUtils.loadAnimation(jugglerView.getContext(), exitAnimation);
            return animation.getDuration();
        } catch (Resources.NotFoundException e) {
            return 0;
        }
    }

    private void checkForInitialization() {
        if (jugglerView == null) {
            throw new IllegalStateException("FragmentNavigator must be initialized first");
        }
    }

    private Fragment createFragment(FragmentMetaData metaData) {
        Fragment fragment = fragmentManagerProvider.getFragmentManager()//can be null?
                .getFragmentFactory()
                .instantiate(jugglerView.getContext().getClassLoader(), metaData.getFragmentClassName());
        Bundle args = metaData.getArguments();
        if (args != null) {
            fragment.setArguments(args);
        }
        return fragment;
    }

    private ArrayList<Bundle> getBundleScreens() {
        ArrayList<Bundle> screens = new ArrayList<>(this.screens.size());
        for (FragmentMetaData metaData: this.screens) {
            screens.add(metaData.toBundle());
        }
        return screens;
    }

    private boolean equalClass(@Nullable Object first, @androidx.annotation.NonNull Object second) {
        return (first != null && first.getClass().equals(second.getClass()));
    }
}

