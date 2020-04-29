package com.github.anrimian.musicplayer.data.storage.providers.artist;

import android.Manifest;
import android.content.Context;

import androidx.test.rule.GrantPermissionRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;

public class StorageArtistsProviderTest {

    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(Manifest.permission.READ_EXTERNAL_STORAGE);

    private StorageArtistsProvider artistsProvider;

    @Before
    public void setUp() {
        Context appContext = getInstrumentation().getTargetContext();
        artistsProvider = new StorageArtistsProvider(appContext);
    }

    @Test
    public void testStorageReturnValues() {
//        LongSparseArray<StorageArtist> map = artistsProvider.getArtists();
//        for(int i = 0, size = map.size(); i < size; i++) {
//            StorageArtist item = map.valueAt(i);
//            System.out.println(item);
//        }
    }
}