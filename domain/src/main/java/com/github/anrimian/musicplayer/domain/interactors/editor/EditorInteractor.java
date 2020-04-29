package com.github.anrimian.musicplayer.domain.interactors.editor;

import com.github.anrimian.musicplayer.domain.models.albums.Album;
import com.github.anrimian.musicplayer.domain.models.composition.FullComposition;
import com.github.anrimian.musicplayer.domain.models.composition.source.CompositionSourceTags;
import com.github.anrimian.musicplayer.domain.models.genres.ShortGenre;
import com.github.anrimian.musicplayer.domain.repositories.EditorRepository;
import com.github.anrimian.musicplayer.domain.repositories.LibraryRepository;
import com.github.anrimian.musicplayer.domain.utils.Objects;

import java.util.LinkedList;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;

import static com.github.anrimian.musicplayer.domain.utils.TextUtils.isEmpty;
import static com.github.anrimian.musicplayer.domain.utils.TextUtils.nullIfEmpty;

public class EditorInteractor {

    private final EditorRepository editorRepository;
    private final LibraryRepository musicProviderRepository;

    public EditorInteractor(EditorRepository editorRepository,
                            LibraryRepository musicProviderRepository) {
        this.editorRepository = editorRepository;
        this.musicProviderRepository = musicProviderRepository;
    }

    public Completable changeCompositionGenre(FullComposition composition,
                                              ShortGenre oldGenre,
                                              String newGenre) {
        return editorRepository.changeCompositionGenre(composition, oldGenre, newGenre);
    }

    public Completable addCompositionGenre(FullComposition composition,
                                           String newGenre) {
        return editorRepository.addCompositionGenre(composition, newGenre);
    }

    public Completable removeCompositionGenre(FullComposition composition, ShortGenre genre) {
        return editorRepository.removeCompositionGenre(composition, genre);
    }

    public Completable editCompositionAuthor(FullComposition composition, String newAuthor) {
        return editorRepository.changeCompositionAuthor(composition, nullIfEmpty(newAuthor));
    }

    public Completable editCompositionAlbum(FullComposition composition, String newAlbum) {
        return editorRepository.changeCompositionAlbum(composition, nullIfEmpty(newAlbum));
    }

    public Completable editCompositionAlbumArtist(FullComposition composition, String newArtist) {
        return editorRepository.changeCompositionAlbumArtist(composition, nullIfEmpty(newArtist));
    }

    public Completable editCompositionTitle(FullComposition composition, String newTitle) {
        return editorRepository.changeCompositionTitle(composition, newTitle);
    }

    public Completable editCompositionFileName(FullComposition composition, String newFileName) {
        return editorRepository.changeCompositionFileName(composition, newFileName);
    }

    public Observable<FullComposition> getCompositionObservable(long id) {
        return musicProviderRepository.getCompositionObservable(id);
    }

    public Observable<List<ShortGenre>> getShortGenresInComposition(long compositionId) {
        return musicProviderRepository.getShortGenresInComposition(compositionId);
    }

    public Observable<Album> getAlbumObservable(long albumId) {
        return musicProviderRepository.getAlbumObservable(albumId);
    }

    /**
     * Album-artist in android system and in common file has conflicts. This function
     * updates media library by real file source tags.
     */
    public Completable updateTagsFromSource(FullComposition fullComposition) {
        return editorRepository.getCompositionFileTags(fullComposition)
                .flatMapCompletable(tags -> getDiffTasksFromSource(fullComposition, tags));
    }

    public Completable updateAlbumName(String name, long albumId) {
        return editorRepository.updateAlbumName(name, albumId);
    }

    public Completable updateAlbumArtist(String name, long albumId) {
        return editorRepository.updateAlbumArtist(name, albumId);
    }

    public Single<String[]> getAuthorNames() {
        return musicProviderRepository.getAuthorNames();
    }

    public Single<String[]> getAlbumNames() {
        return musicProviderRepository.getAlbumNames();
    }

    public Single<String[]> getGenreNames() {
        return musicProviderRepository.getGenreNames();
    }

    private Completable getDiffTasksFromSource(FullComposition fullComposition,
                                               CompositionSourceTags tags) {
        LinkedList<Completable> tasksList = new LinkedList<>();

        String tagTitle = tags.getTitle();
        if (!isEmpty(tagTitle) && !Objects.equals(fullComposition.getTitle(), tagTitle)) {
            tasksList.add(editCompositionTitle(fullComposition, tagTitle));
        }

        String tagArtist = tags.getArtist();
        if (!isEmpty(tagArtist) && !Objects.equals(fullComposition.getArtist(), tagArtist)) {
            tasksList.add(editCompositionAuthor(fullComposition, tagArtist));
        }

        String tagAlbum = tags.getAlbum();
        if (!isEmpty(tagAlbum) && !Objects.equals(fullComposition.getAlbum(), tagAlbum)) {
            tasksList.add(editCompositionAlbum(fullComposition, tagAlbum));
        }

        String tagAlbumArtist = tags.getAlbumArtist();
        if (!isEmpty(tagAlbumArtist) && !Objects.equals(fullComposition.getAlbumArtist(), tagAlbumArtist)) {
            tasksList.add(editCompositionAlbumArtist(fullComposition, tagAlbumArtist));
        }

        return Completable.concat(tasksList);
    }
}
