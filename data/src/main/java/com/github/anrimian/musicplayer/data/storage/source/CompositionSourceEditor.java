package com.github.anrimian.musicplayer.data.storage.source;

import com.github.anrimian.musicplayer.data.storage.providers.music.StorageMusicProvider;
import com.github.anrimian.musicplayer.data.utils.file.FileUtils;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.composition.FullComposition;
import com.github.anrimian.musicplayer.domain.models.composition.source.CompositionSourceTags;
import com.github.anrimian.musicplayer.domain.models.image.ImageSource;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.CannotWriteException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.TagOptionSingleton;
import org.jaudiotagger.tag.datatype.Artwork;
import org.jaudiotagger.tag.id3.ID3v24Tag;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.annotation.Nullable;

import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;

public class CompositionSourceEditor {

    private static final char GENRE_DIVIDER = '\u0000';
    private static final int MAX_COVER_SIZE = 1024;

    private final StorageMusicProvider storageMusicProvider;
    private final FileSourceProvider fileSourceProvider;

    public CompositionSourceEditor(StorageMusicProvider storageMusicProvider,
                                   FileSourceProvider fileSourceProvider) {
        this.storageMusicProvider = storageMusicProvider;
        this.fileSourceProvider = fileSourceProvider;

        TagOptionSingleton.getInstance().setAndroid(true);
    }

    public Completable setCompositionTitle(FullComposition composition, String title) {
        return getPath(composition)
                .flatMapCompletable(path -> setCompositionTitle(path, title));
    }

    public Completable setCompositionAuthor(FullComposition composition, String author) {
        return getPath(composition)
                .flatMapCompletable(path -> setCompositionAuthor(path, author));
    }

    public Completable setCompositionAuthor(Composition composition, String author) {
        return getPath(composition)
                .flatMapCompletable(path -> setCompositionAuthor(path, author));
    }

    public Completable setCompositionAlbum(FullComposition composition, String author) {
        return getPath(composition)
                .flatMapCompletable(path -> setCompositionAlbum(path, author));
    }

    public Completable setCompositionAlbum(Composition composition, String author) {
        return getPath(composition)
                .flatMapCompletable(path -> setCompositionAlbum(path, author));
    }

    public Completable setCompositionAlbumArtist(FullComposition composition, String artist) {
        return getPath(composition)
                .flatMapCompletable(path -> setCompositionAlbumArtist(path, artist));
    }

    public Completable setCompositionAlbumArtist(Composition composition, String artist) {
        return getPath(composition)
                .flatMapCompletable(path -> setCompositionAlbumArtist(path, artist));
    }

    public Completable setCompositionLyrics(FullComposition composition, String text) {
        return getPath(composition)
                .flatMapCompletable(path -> setCompositionLyrics(path, text));
    }

    public Completable changeCompositionGenre(FullComposition composition,
                                              String oldGenre,
                                              String newGenre) {
        return getPath(composition)
                .flatMapCompletable(path -> changeCompositionGenre(path, oldGenre, newGenre));
    }

    public Completable changeCompositionGenre(Composition composition,
                                              String oldGenre,
                                              String newGenre) {
        return getPath(composition)
                .flatMapCompletable(path -> changeCompositionGenre(path, oldGenre, newGenre));
    }

    public Completable addCompositionGenre(FullComposition composition,
                                           String newGenre) {
        return getPath(composition)
                .flatMapCompletable(path -> addCompositionGenre(path, newGenre));
    }

    public Completable removeCompositionGenre(FullComposition composition, String genre) {
        return getPath(composition)
                .flatMapCompletable(path -> removeCompositionGenre(path, genre));
    }

    public Single<String[]> getCompositionGenres(FullComposition composition) {
        return getPath(composition)
                .flatMap(this::getCompositionGenres);
    }

    public Completable changeCompositionAlbumArt(FullComposition composition,
                                                 ImageSource imageSource) {
        return getPath(composition)
                .flatMapCompletable(path -> changeCompositionAlbumArt(path, imageSource));
    }

    public Completable removeCompositionAlbumArt(FullComposition composition) {
        return getPath(composition)
                .flatMapCompletable(this::removeCompositionAlbumArt);
    }

    public Maybe<CompositionSourceTags> getFullTags(FullComposition composition) {
        return getPath(composition)
                .flatMapMaybe(this::getFullTags);
    }

    //genre not found case
    Completable changeCompositionGenre(String filePath,
                                       String oldGenre,
                                       String newGenre) {
        return Completable.fromAction(() -> {
            String genres = getFileTag(filePath).getFirst(FieldKey.GENRE);
            genres = genres.replace(oldGenre, newGenre);
            editFile(filePath, FieldKey.GENRE, genres);
        });
    }

    Completable setCompositionAlbumArtist(String filePath, String artist) {
        return Completable.fromAction(() -> editFile(filePath, FieldKey.ALBUM_ARTIST, artist));
    }

    Completable setCompositionAlbum(String filePath, String author) {
        return Completable.fromAction(() -> editFile(filePath, FieldKey.ALBUM, author));
    }

    Completable setCompositionTitle(String filePath, String title) {
        return Completable.fromAction(() -> editFile(filePath, FieldKey.TITLE, title));
    }

    Completable setCompositionLyrics(String filePath, String text) {
        return Completable.fromAction(() -> editFile(filePath, FieldKey.LYRICS, text));
    }

    Completable addCompositionGenre(String filePath,
                                    String newGenre) {
        return Completable.fromAction(() -> {
            AudioFile file = AudioFileIO.read(new File(filePath));
            Tag tag = file.getTag();
            if (tag == null) {
                tag = new ID3v24Tag();
                file.setTag(tag);
            }
            tag.addField(FieldKey.GENRE, newGenre);
            AudioFileIO.write(file);
//            String genres = getFileTag(filePath).getFirst(FieldKey.GENRE);
//            StringBuilder sb = new StringBuilder(genres);
//            if (sb.length() != 0) {
//                sb.append(GENRE_DIVIDER);
//            }
//            sb.append(newGenre);
//            sb.append(GENRE_DIVIDER);
//            editFile(filePath, FieldKey.GENRE, sb.toString());
        });
    }

    Completable removeCompositionGenre(String filePath, String genre) {
        return Completable.fromAction(() -> {
            String genres = getFileTag(filePath).getFirst(FieldKey.GENRE);
            int startIndex = genres.indexOf(genre);
            if (startIndex == -1) {
                return;
            }
            int endIndex = startIndex + genre.length();
            StringBuilder sb = new StringBuilder(genres);

            //clear divider at start
            if (startIndex == 1 && sb.charAt(0) == GENRE_DIVIDER) {
                startIndex = 0;
            }
            //clear divider at end or next if genre is at start or has divider before
            if ((endIndex == sb.length() - 2 || startIndex == 0 || sb.charAt(startIndex - 1) == GENRE_DIVIDER)
                    && (endIndex < sb.length() && sb.charAt(endIndex) == GENRE_DIVIDER)) {
                endIndex++;
            }

            sb.delete(startIndex, endIndex);

            editFile(filePath, FieldKey.GENRE, sb.toString());
        });
    }

    Maybe<String> getCompositionTitle(String filePath) {
        return Maybe.fromCallable(() -> getFileTag(filePath).getFirst(FieldKey.TITLE));
    }

    Maybe<String> getCompositionAuthor(String filePath) {
        return Maybe.fromCallable(() -> getFileTag(filePath).getFirst(FieldKey.ARTIST));
    }

    Maybe<String> getCompositionAlbum(String filePath) {
        return Maybe.fromCallable(() -> getFileTag(filePath).getFirst(FieldKey.ALBUM));
    }

    Maybe<String> getCompositionAlbumArtist(String filePath) {
        return Maybe.fromCallable(() -> getFileTag(filePath).getFirst(FieldKey.ALBUM_ARTIST));
    }

    Maybe<String> getCompositionGenre(String filePath) {
        return Maybe.fromCallable(() -> getFileTag(filePath).getFirst(FieldKey.GENRE));
    }

    Maybe<String> getCompositionLyrics(String filePath) {
        return Maybe.fromCallable(() -> getFileTag(filePath).getFirst(FieldKey.LYRICS));
    }

    private Completable changeCompositionAlbumArt(String filePath, ImageSource imageSource) {
        return Completable.fromAction(() -> {
            AudioFile file = AudioFileIO.read(new File(filePath));
            Tag tag = file.getTag();
            if (tag == null) {
                tag = new ID3v24Tag();
                file.setTag(tag);
            }

            try (InputStream stream = fileSourceProvider.getImageStream(imageSource)) {
                if (stream == null) {
                    return;
                }
                byte[] data = FileUtils.getScaledBitmapByteArray(stream, MAX_COVER_SIZE);
                Artwork artwork = new Artwork();
                artwork.setBinaryData(data);
                tag.deleteArtworkField();
                tag.setField(artwork);
                AudioFileIO.write(file);
            }
        });
    }

    private Completable removeCompositionAlbumArt(String filePath) {
        return Completable.fromAction(() -> {
            AudioFile file = AudioFileIO.read(new File(filePath));
            Tag tag = file.getTag();
            if (tag == null) {
                tag = new ID3v24Tag();
                file.setTag(tag);
            }
            tag.deleteArtworkField();
            AudioFileIO.write(file);
        });
    }

    private Completable setCompositionAuthor(String filePath, String author) {
        return Completable.fromAction(() -> editFile(filePath, FieldKey.ARTIST, author));
    }

    private Single<String[]> getCompositionGenres(String filePath) {
        return Single.fromCallable(() -> {
            String genres =  getFileTag(filePath).getFirst(FieldKey.GENRE);
            if (genres == null) {
                return new String[0];
            }
            return genres.split(String.valueOf(GENRE_DIVIDER));
        });
    }

    private Maybe<CompositionSourceTags> getFullTags(String filePath) {
        return Maybe.fromCallable(() -> {
            Tag tag = getFileTag(filePath);
            if (tag == null) {
                return null;
            }
            return new CompositionSourceTags(tag.getFirst(FieldKey.TITLE),
                    tag.getFirst(FieldKey.ARTIST),
                    tag.getFirst(FieldKey.ALBUM),
                    tag.getFirst(FieldKey.ALBUM_ARTIST),
                    tag.getFirst(FieldKey.LYRICS));
        });
    }

    private Single<String> getPath(Composition composition) {
        return getPath(composition.getStorageId());
    }

    private Single<String> getPath(FullComposition composition) {
        return getPath(composition.getStorageId());
    }

    private Single<String> getPath(@Nullable Long storageId) {
        return Single.fromCallable(() -> {
            if (storageId == null) {
                throw new RuntimeException("composition not found");
            }
            String path = storageMusicProvider.getCompositionFilePath(storageId);
            if (path == null) {
                throw new RuntimeException("composition path not found in system media store");
            }
            return storageMusicProvider.getCompositionFilePath(storageId);
        });
    }

    private Tag getFileTag(String filePath) throws TagException, ReadOnlyFileException,
            CannotReadException, InvalidAudioFrameException, IOException {
        AudioFile file = AudioFileIO.read(new File(filePath));
        return file.getTag();
    }

    private void editFile(String filePath, FieldKey genericKey, String value) throws IOException,
            TagException, ReadOnlyFileException, CannotReadException, InvalidAudioFrameException,
            CannotWriteException {
        AudioFile file = AudioFileIO.read(new File(filePath));
        Tag tag = file.getTag();
        if (tag == null) {
            tag = new ID3v24Tag();
            file.setTag(tag);
        }
        tag.setField(genericKey, value == null? "" : value);
        AudioFileIO.write(file);
    }
}
