package com.github.anrimian.musicplayer.data.database.dao.artist;

import androidx.collection.LongSparseArray;
import androidx.sqlite.db.SimpleSQLiteQuery;

import com.github.anrimian.musicplayer.data.database.entities.artist.ArtistEntity;
import com.github.anrimian.musicplayer.data.storage.providers.artist.StorageArtist;
import com.github.anrimian.musicplayer.data.utils.collections.AndroidCollectionUtils;
import com.github.anrimian.musicplayer.domain.models.artist.Artist;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.composition.order.Order;

import java.util.List;

import io.reactivex.Observable;

import static com.github.anrimian.musicplayer.domain.utils.ListUtils.mapList;
import static com.github.anrimian.musicplayer.domain.utils.TextUtils.isEmpty;

public class ArtistsDaoWrapper {

    private final ArtistsDao artistsDao;

    public ArtistsDaoWrapper(ArtistsDao artistsDao) {
        this.artistsDao = artistsDao;
    }

    public void insertArtists(List<StorageArtist> artists) {
        artistsDao.insertAll(mapList(artists, this::toEntity));
    }

    public LongSparseArray<StorageArtist> selectAllAsStorageArtists() {
        return AndroidCollectionUtils.mapToSparseArray(
                artistsDao.selectAllAsStorageArtists(),
                StorageArtist::getId);
    }

    public Observable<List<Artist>> getAllObservable(Order order, String searchText) {
        String query = "SELECT id as id," +
                "name as name, " +
                "(SELECT count() FROM compositions WHERE artistId = artists.id) as compositionsCount " +
                "FROM artists";
        query += getSearchQuery(searchText);
        query += getOrderQuery(order);
        SimpleSQLiteQuery sqlQuery = new SimpleSQLiteQuery(query);
        return artistsDao.getAllObservable(sqlQuery);
    }

    public Observable<List<Composition>> getCompositionsByArtistObservable(long artistId) {
        return artistsDao.getCompositionsByArtistObservable(artistId);
    }

    public List<Composition> getCompositionsByArtist(long artistId) {
        return artistsDao.getCompositionsByArtist(artistId);
    }

    public Observable<Artist> getArtistObservable(long artistId) {
        return artistsDao.getArtistObservable(artistId)
                .takeWhile(list -> !list.isEmpty())
                .map(list -> list.get(0));
    }

    public String[] getAuthorNames() {
        return artistsDao.getAuthorNames();
    }

    public void updateArtistName(String name, long id) {
        artistsDao.updateArtistName(name, id);
    }

    private ArtistEntity toEntity(StorageArtist artist) {
        return new ArtistEntity(
                artist.getId(),
                artist.getArtist()
        );
    }

    private String getOrderQuery(Order order) {
        StringBuilder orderQuery = new StringBuilder(" ORDER BY ");
        switch (order.getOrderType()) {
            case ALPHABETICAL: {
                orderQuery.append("name");
                break;
            }
            case COMPOSITION_COUNT: {
                orderQuery.append("compositionsCount");
                break;
            }
            default: throw new IllegalStateException("unknown order type" + order);
        }
        orderQuery.append(" ");
        orderQuery.append(order.isReversed()? "DESC" : "ASC");
        return orderQuery.toString();
    }

    private String getSearchQuery(String searchText) {
        if (isEmpty(searchText)) {
            return "";
        }
        StringBuilder sb = new StringBuilder(" WHERE ");
        sb.append("name LIKE '%");
        sb.append(searchText);
        sb.append("%'");

        return sb.toString();
    }
}
