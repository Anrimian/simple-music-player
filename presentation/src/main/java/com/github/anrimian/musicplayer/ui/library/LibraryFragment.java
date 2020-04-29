package com.github.anrimian.musicplayer.ui.library;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.di.Components;
import com.github.anrimian.musicplayer.domain.models.Screens;
import com.github.anrimian.musicplayer.domain.repositories.UiStateRepository;
import com.github.anrimian.musicplayer.ui.common.menu.PopupMenuWindow;
import com.github.anrimian.musicplayer.ui.common.toolbar.AdvancedToolbar;
import com.github.anrimian.musicplayer.ui.library.albums.list.AlbumsListFragment;
import com.github.anrimian.musicplayer.ui.library.artists.list.ArtistsListFragment;
import com.github.anrimian.musicplayer.ui.library.compositions.LibraryCompositionsFragment;
import com.github.anrimian.musicplayer.ui.library.folders.root.LibraryFoldersRootFragment;
import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.FragmentLayerListener;
import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.FragmentNavigation;

import moxy.MvpAppCompatFragment;

public class LibraryFragment extends MvpAppCompatFragment implements FragmentLayerListener {

    private UiStateRepository uiStateRepository;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        uiStateRepository = Components.getAppComponent().uiStateRepository();
    }

    @Override
    public void onFragmentMovedOnTop() {
        AdvancedToolbar toolbar = requireActivity().findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.library);
        toolbar.setTitleClickListener(this::onLibraryTitleClicked);
    }

    private void onLibraryTitleClicked(View view) {
        PopupMenuWindow.showPopup(view,
                R.menu.library_categories_menu,
                Gravity.BOTTOM,
                item -> {
                    FragmentManager fm = getFragmentManager();
                    if (fm == null) {//can be null in some state, don't know which
                        return;
                    }
                    FragmentNavigation navigation = FragmentNavigation.from(fm);
                    switch (item.getItemId()) {
                        case R.id.menu_compositions: {
                            uiStateRepository.setSelectedLibraryScreen(Screens.LIBRARY_COMPOSITIONS);
                            navigation.newRootFragment(new LibraryCompositionsFragment());
                            break;
                        }
                        case R.id.menu_files: {
                            navigation.newRootFragment(new LibraryFoldersRootFragment());
                            uiStateRepository.setSelectedLibraryScreen(Screens.LIBRARY_FOLDERS);
                            break;
                        }
                        case R.id.menu_artists: {
                            uiStateRepository.setSelectedLibraryScreen(Screens.LIBRARY_ARTISTS);
                            navigation.newRootFragment(new ArtistsListFragment());
                            break;
                        }
                        case R.id.menu_albums: {
                            uiStateRepository.setSelectedLibraryScreen(Screens.LIBRARY_ALBUMS);
                            navigation.newRootFragment(new AlbumsListFragment());
                            break;
                        }
                        //<return genres after deep scan implementation>
//                        case R.id.menu_genres: {
//                            uiStateRepository.setSelectedLibraryScreen(Screens.LIBRARY_GENRES);
//                            navigation.newRootFragment(new GenresListFragment());
//                            break;
//                        }
                    }
                });
    }

}
