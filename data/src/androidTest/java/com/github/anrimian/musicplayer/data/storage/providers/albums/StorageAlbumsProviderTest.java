package com.github.anrimian.musicplayer.data.storage.providers.albums;

import android.Manifest;
import android.content.Context;

import androidx.test.rule.GrantPermissionRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;

public class StorageAlbumsProviderTest {

    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(Manifest.permission.READ_EXTERNAL_STORAGE);

    private StorageAlbumsProvider albumsProvider;

    @Before
    public void setUp() {
        Context appContext = getInstrumentation().getTargetContext();
        albumsProvider = new StorageAlbumsProvider(appContext);
    }

    @Test
    public void testStorageReturnValues() {
//        LongSparseArray<StorageAlbum> map = albumsProvider.getAlbums();
//        for(int i = 0, size = map.size(); i < size; i++) {
//            StorageAlbum item = map.valueAt(i);
//            Log.i("TEST_STORAGE", "item: " + item);
//            System.out.println(item);
//        }
    }
}