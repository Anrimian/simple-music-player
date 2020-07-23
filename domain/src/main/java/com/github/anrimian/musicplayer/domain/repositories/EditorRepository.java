package com.github.anrimian.musicplayer.domain.repositories;

import com.github.anrimian.musicplayer.domain.models.composition.FullComposition;
import com.github.anrimian.musicplayer.domain.models.composition.source.CompositionSourceTags;
import com.github.anrimian.musicplayer.domain.models.folders.FileSource;
import com.github.anrimian.musicplayer.domain.models.genres.ShortGenre;
import com.github.anrimian.musicplayer.domain.models.image.ImageSource;

import java.util.Collection;

import javax.annotation.Nullable;

import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;

public interface EditorRepository {

    Completable changeCompositionGenre(FullComposition composition,
                                       ShortGenre oldGenre,
                                       String newGenre);

    Completable addCompositionGenre(FullComposition composition,
                                    String newGenre);

    Completable removeCompositionGenre(FullComposition composition, ShortGenre genre);

    Completable changeCompositionAuthor(FullComposition composition, String newAuthor);

    Completable changeCompositionAlbumArtist(FullComposition composition, String newAuthor);

    Completable changeCompositionAlbum(FullComposition composition, String newAlbum);

    Completable changeCompositionTitle(FullComposition composition, String title);

    Completable changeCompositionLyrics(FullComposition composition, String text);

    Completable changeCompositionFileName(FullComposition composition, String fileName);

    Completable changeFolderName(long folderId, String folderName);

    Completable moveFiles(Collection<FileSource> files,
                          @Nullable Long fromFolderId,
                          @Nullable Long toFolderId);

    Completable moveFilesToNewDirectory(Collection<FileSource> files,
                                        @Nullable Long fromFolderId,
                                        @Nullable Long targetParentFolderId,
                                        String directoryName);

    Completable updateAlbumName(String name, long id);

    Completable updateAlbumArtist(String name, long albumId);

    Completable updateArtistName(String name, long artistId);

    Completable updateGenreName(String name, long genreId);

    Maybe<CompositionSourceTags> getCompositionFileTags(FullComposition composition);

    Single<String[]> getCompositionFileGenres(FullComposition composition);

    Completable changeCompositionAlbumArt(FullComposition composition, ImageSource imageSource);

    Completable removeCompositionAlbumArt(FullComposition composition);
}
