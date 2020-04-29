package com.github.anrimian.musicplayer.ui.utils.fragments;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.github.anrimian.musicplayer.domain.utils.functions.Callback;

public class DialogFragmentRunner<T extends DialogFragment> {

    private final FragmentManager fragmentManager;
    private final String tag;
    private final Callback<T> fragmentInitializer;

    public DialogFragmentRunner(FragmentManager fragmentManager,
                                String tag,
                                Callback<T> fragmentInitializer) {
        this.fragmentManager = fragmentManager;
        this.tag = tag;
        this.fragmentInitializer = fragmentInitializer;

        @SuppressWarnings("unchecked")
        T fragment = (T) fragmentManager.findFragmentByTag(tag);
        if (fragment != null) {
            fragmentInitializer.call(fragment);
        }
    }

    public void show(T fragment) {
        fragmentInitializer.call(fragment);
        fragment.show(fragmentManager, tag);
    }
}
