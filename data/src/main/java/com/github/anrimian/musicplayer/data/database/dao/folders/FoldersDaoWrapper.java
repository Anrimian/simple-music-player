package com.github.anrimian.musicplayer.data.database.dao.folders;


import androidx.annotation.Nullable;
import androidx.sqlite.db.SimpleSQLiteQuery;

import com.github.anrimian.musicplayer.data.database.AppDatabase;
import com.github.anrimian.musicplayer.data.database.dao.compositions.CompositionsDaoWrapper;
import com.github.anrimian.musicplayer.data.database.entities.folder.FolderEntity;
import com.github.anrimian.musicplayer.data.database.entities.folder.IgnoredFolderEntity;
import com.github.anrimian.musicplayer.data.database.entities.folder.StorageFolder;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.folders.CompositionFileSource;
import com.github.anrimian.musicplayer.domain.models.folders.FileSource;
import com.github.anrimian.musicplayer.domain.models.folders.FolderFileSource;
import com.github.anrimian.musicplayer.domain.models.folders.IgnoredFolder;
import com.github.anrimian.musicplayer.domain.models.order.Order;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Single;

import static com.github.anrimian.musicplayer.data.database.utils.DatabaseUtils.getSearchArgs;
import static com.github.anrimian.musicplayer.domain.utils.ListUtils.mapList;
import static com.github.anrimian.musicplayer.domain.utils.TextUtils.isEmpty;

public class FoldersDaoWrapper {

    private final AppDatabase appDatabase;
    private final FoldersDao foldersDao;
    private final CompositionsDaoWrapper compositionsDao;

    public FoldersDaoWrapper(AppDatabase appDatabase,
                             FoldersDao foldersDao,
                             CompositionsDaoWrapper compositionsDao) {
        this.appDatabase = appDatabase;
        this.foldersDao = foldersDao;
        this.compositionsDao = compositionsDao;
    }

    public Observable<List<FileSource>> getFilesObservable(Long parentFolderId,
                                                            Order order,
                                                            @Nullable String searchText) {
        Observable<List<FolderFileSource>> folderObservable = getFoldersObservable(
                parentFolderId,
                order,
                searchText);

        Observable<List<CompositionFileSource>> compositionsObservable =
                compositionsDao.getCompositionsInFolderObservable(
                        parentFolderId,
                        order,
                        searchText
                ).map(list -> mapList(list, CompositionFileSource::new));

        return Observable.combineLatest(folderObservable,
                compositionsObservable,
                (folders, compositions) -> {
                    List<FileSource> list = new ArrayList<>(folders.size() + compositions.size());
                    list.addAll(folders);
                    list.addAll(compositions);
                    return list;
                });
    }

    public Observable<FolderFileSource> getFolderObservable(long folderId) {
        return foldersDao.getFolderObservable(folderId)
                .takeWhile(list -> !list.isEmpty())
                .map(list -> list.get(0));
    }

    public List<StorageFolder> getAllFolders() {
        return foldersDao.getAllFolders();
    }

    public Single<List<Composition>> extractAllCompositionsFromFiles(Iterable<FileSource> fileSources) {
        return Observable.fromIterable(fileSources)
                .flatMap(this::fileSourceToComposition)
                .collect(ArrayList::new, List::add);
    }

    public Single<List<Composition>> extractAllCompositionsFromFiles(Iterable<FileSource> fileSources,
                                                                     Order order) {
        return Observable.fromIterable(fileSources)
                .flatMap(fileSource -> fileSourceToComposition(fileSource, order))
                .collect(ArrayList::new, List::add);
    }

    public List<Composition> getAllCompositionsInFolder(Long parentFolderId, Order order) {
        List<Composition> result = new LinkedList<>();

        String query = FoldersDao.getRecursiveFolderQuery(parentFolderId) +
                "SELECT id " +
                "FROM folders " +
                "WHERE parentId = " + parentFolderId + " OR (parentId IS NULL AND " + parentFolderId + " IS NULL)";
        query += getOrderQuery(order);
        SimpleSQLiteQuery sqlQuery = new SimpleSQLiteQuery(query);
        List<Long> folders = foldersDao.getFoldersIds(sqlQuery);
        for (Long id: folders) {
            result.addAll(getAllCompositionsInFolder(id, order));
        }

        result.addAll(compositionsDao.getCompositionsInFolder(parentFolderId, order));
        return result;
    }

    public IgnoredFolder insert(String path) {
        Date addDate = new Date();
        foldersDao.insert(new IgnoredFolderEntity(path, addDate));
        return new IgnoredFolder(path, addDate);
    }

    public void insert(IgnoredFolder folder) {
        foldersDao.insert(new IgnoredFolderEntity(folder.getRelativePath(), folder.getAddDate()));
    }

    public void deleteFolders(List<Long> ids) {
        foldersDao.deleteFolders(ids);
    }

    public void deleteFolder(Long folderId, List<Composition> childCompositions) {
        appDatabase.runInTransaction(() -> {
            compositionsDao.deleteAll(mapList(childCompositions, Composition::getId));
            foldersDao.deleteFolder(folderId);
        });
    }

    public void deleteFolders(List<Long> folders, List<Composition> childCompositions) {
        appDatabase.runInTransaction(() -> {
            compositionsDao.deleteAll(mapList(childCompositions, Composition::getId));
            foldersDao.deleteFolders(folders);
        });
    }

    public void changeFolderName(long folderId, String newName) {
        foldersDao.changeFolderName(folderId, newName);
    }

    public String getFullFolderPath(long folderId) {
        return foldersDao.getFullFolderPath(folderId);
    }

    public List<Long> getAllChildFoldersId(Long parentId) {
        return foldersDao.getAllChildFoldersId(parentId);
    }

    public List<Long> getAllParentFoldersId(Long currentFolder) {
        List<Long> result = new LinkedList<>();
        result.add(null);//null folder is always first
        result.addAll(foldersDao.getAllParentFoldersId(currentFolder));
        return result;
    }

    public String getFolderName(long folderId) {
        return foldersDao.getFolderName(folderId);
    }

    public List<String> getChildFoldersNames(Long toFolderId) {
        return foldersDao.getChildFoldersNames(toFolderId);
    }

    public void updateFolderId(Collection<FileSource> files, Long toFolderId) {
        appDatabase.runInTransaction(() -> {
            for (FileSource fileSource: files) {
                if (fileSource instanceof CompositionFileSource) {
                    long id = ((CompositionFileSource) fileSource).getComposition().getId();
                    compositionsDao.updateFolderId(id, toFolderId);
                }
                if (fileSource instanceof FolderFileSource) {
                    long id = ((FolderFileSource) fileSource).getId();
                    foldersDao.updateParentId(id, toFolderId);
                }
            }
        });
    }

    public long createFolder(Long parentId, String name) {
        return foldersDao.insertFolder(new FolderEntity(parentId, name));
    }

    public String[] getIgnoredFolders() {
        return foldersDao.getIgnoredFolders();
    }

    public Observable<List<IgnoredFolder>> getIgnoredFoldersObservable() {
        return foldersDao.getIgnoredFoldersObservable();
    }

    public void deleteIgnoredFolder(IgnoredFolder folder) {
        foldersDao.deleteIgnoredFolder(folder.getRelativePath());
    }

    private Observable<List<FolderFileSource>> getFoldersObservable(Long parentFolderId,
                                                                    Order order,
                                                                    @Nullable String searchText) {
        String query = FoldersDao.getRecursiveFolderQuery(parentFolderId) +
                "SELECT id, name, " +
                "(SELECT count() FROM compositions WHERE folderId IN (SELECT childFolderId FROM allChildFolders WHERE rootFolderId = folders.id)) as filesCount " +
                "FROM folders " +
                "WHERE parentId = " + parentFolderId + " OR (parentId IS NULL AND " + parentFolderId + " IS NULL)";

        query += getSearchQuery();
        query += getOrderQuery(order);
        SimpleSQLiteQuery sqlQuery = new SimpleSQLiteQuery(query, getSearchArgs(searchText, 2));
        return foldersDao.getFoldersObservable(sqlQuery);
    }

    private String getOrderQuery(Order order) {
        StringBuilder orderQuery = new StringBuilder(" ORDER BY ");
        switch (order.getOrderType()) {
            case ALPHABETICAL: {
                orderQuery.append("name");
                break;
            }
            case ADD_TIME: {
                orderQuery.append("(SELECT max(dateAdded) FROM compositions WHERE folderId IN (SELECT childFolderId FROM allChildFolders WHERE rootFolderId = folders.id))");
                break;
            }
            case DURATION: {
                orderQuery.append("(SELECT sum(duration) FROM compositions WHERE folderId IN (SELECT childFolderId FROM allChildFolders WHERE rootFolderId = folders.id))");
                break;
            }
            case SIZE: {
                orderQuery.append("(SELECT sum(size) FROM compositions WHERE folderId IN (SELECT childFolderId FROM allChildFolders WHERE rootFolderId = folders.id))");
                break;
            }
            default: throw new IllegalStateException("unknown order type" + order);
        }
        orderQuery.append(" ");
        orderQuery.append(order.isReversed()? "DESC" : "ASC");
        return orderQuery.toString();
    }

    private String getSearchQuery() {
        return " AND (? IS NULL OR (name NOTNULL AND name LIKE ?))";
    }

    private Observable<Composition> fileSourceToComposition(FileSource fileSource, Order order) {
        if (fileSource instanceof CompositionFileSource) {
            return Observable.just(((CompositionFileSource) fileSource).getComposition());
        }
        if (fileSource instanceof FolderFileSource) {
            return Observable.fromIterable(getAllCompositionsInFolder(
                    ((FolderFileSource) fileSource).getId(),
                    order
            ));
        }
        throw new IllegalStateException("unexpected file source: " + fileSource);
    }

    private Observable<Composition> fileSourceToComposition(FileSource fileSource) {
        if (fileSource instanceof CompositionFileSource) {
            return Observable.just(((CompositionFileSource) fileSource).getComposition());
        }
        if (fileSource instanceof FolderFileSource) {
            return Observable.fromIterable(compositionsDao.getAllCompositionsInFolder(
                    ((FolderFileSource) fileSource).getId()
            ));
        }
        throw new IllegalStateException("unexpected file source: " + fileSource);
    }

}
