package com.github.anrimian.musicplayer.domain.interactors.library;

import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.order.Order;
import com.github.anrimian.musicplayer.domain.repositories.LibraryRepository;
import com.github.anrimian.musicplayer.domain.repositories.SettingsRepository;

import java.util.List;

import io.reactivex.Observable;

public class LibraryCompositionsInteractor {

    private final LibraryRepository musicProviderRepository;
    private final SettingsRepository settingsRepository;

    public LibraryCompositionsInteractor(LibraryRepository musicProviderRepository,
                                         SettingsRepository settingsRepository) {
        this.musicProviderRepository = musicProviderRepository;
        this.settingsRepository = settingsRepository;
    }

    public Observable<List<Composition>> getCompositionsObservable(String searchText) {
        return musicProviderRepository.getAllCompositionsObservable(searchText);
    }

    public void setOrder(Order order) {
        settingsRepository.setCompositionsOrder(order);
    }

    public Order getOrder() {
        return settingsRepository.getCompositionsOrder();
    }


}
