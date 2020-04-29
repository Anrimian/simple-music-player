package com.github.anrimian.musicplayer.data.storage.providers.genres;

import android.Manifest;
import android.content.Context;

import androidx.test.rule.GrantPermissionRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;

public class StorageGenresProviderTest {

    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(Manifest.permission.READ_EXTERNAL_STORAGE);

    private StorageGenresProvider albumsProvider;

    @Before
    public void setUp() {
        Context appContext = getInstrumentation().getTargetContext();
        albumsProvider = new StorageGenresProvider(appContext);
    }

    @Test
    public void testStorageReturnValues() {
//        LongSparseArray<StorageGenre> map = albumsProvider.getGenres();
//        for(int i = 0, size = map.size(); i < size; i++) {
//            StorageGenre item = map.valueAt(i);
//            LongSparseArray<StorageGenreItem> items = albumsProvider.getGenreItems(item.getId());
//            Log.i("TEST_STORAGE", "item: " + item + ", members: " + items);
//            System.out.println(item);
//        }
    }
}