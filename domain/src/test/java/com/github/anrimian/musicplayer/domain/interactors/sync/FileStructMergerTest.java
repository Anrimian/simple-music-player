package com.github.anrimian.musicplayer.domain.interactors.sync;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.emptySet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FileStructMergerTest {

    @Test
    public void testMergeAddFromBothSide() {
        Map<Integer, String> localItems = new HashMap<>();
        localItems.put(1, "1");
        localItems.put(2, "2");

        Map<Integer, String> remoteItems = new HashMap<>();
        remoteItems.put(3, "3");
        remoteItems.put(4, "4");

        List<String> outLocalFilesToUpload = new ArrayList<>();
        List<String> outRemoteFileToDownload = new ArrayList<>();

        Map<Integer, String> mergedItemsMap = new HashMap<>();

        FileStructMerger.mergeFilesMap(localItems,
                remoteItems,
                emptySet(),
                emptySet(),
                emptySet(),
                Collections.emptySet(),
                (local, remote) -> true,
                String::valueOf,
                item -> {},
                item -> {},
                outLocalFilesToUpload::add,
                outRemoteFileToDownload::add,
                mergedItemsMap::put,
                mergedItemsMap::put);

        assertEquals(4, mergedItemsMap.size());
        assertEquals("1", mergedItemsMap.get(1));
        assertEquals("2", mergedItemsMap.get(2));
        assertEquals("3", mergedItemsMap.get(3));
        assertEquals("4", mergedItemsMap.get(4));

        assertTrue(outLocalFilesToUpload.contains("1"));
        assertTrue(outLocalFilesToUpload.contains("2"));

        assertTrue(outRemoteFileToDownload.contains("3"));
        assertTrue(outRemoteFileToDownload.contains("4"));
    }

    @Test
    public void testDeleteFromLocal() {
        Map<Integer, String> localItems = new HashMap<>();
        localItems.put(1, "1");
        localItems.put(2, "2");
        localItems.put(3, "3");

        Map<Integer, String> remoteItems = new HashMap<>();
        remoteItems.put(1, "1");
        remoteItems.put(2, "2");
        remoteItems.put(3, "3");
        remoteItems.put(4, "4");

        Set<Integer> localRemovedItems = new HashSet<>();
        localRemovedItems.add(4);

        List<String> outRemoteFilesToDelete = new ArrayList<>();

        Map<Integer, String> mergedItemsMap = new HashMap<>();

        FileStructMerger.mergeFilesMap(localItems,
                remoteItems,
                localRemovedItems,
                emptySet(),
                emptySet(),
                Collections.emptySet(),
                (local, remote) -> true,
                String::valueOf,
                item -> {},
                outRemoteFilesToDelete::add,
                item -> {},
                item -> {},
                mergedItemsMap::put,
                mergedItemsMap::put);

        assertEquals(3, mergedItemsMap.size());
        assertEquals("1", mergedItemsMap.get(1));
        assertEquals("2", mergedItemsMap.get(2));
        assertEquals("3", mergedItemsMap.get(3));

        assertTrue(outRemoteFilesToDelete.contains("4"));
    }

    @Test
    public void testDeleteFromRemote() {
        Map<Integer, String> localItems = new HashMap<>();
        localItems.put(1, "1");
        localItems.put(2, "2");
        localItems.put(3, "3");
        localItems.put(4, "4");

        Map<Integer, String> remoteItems = new HashMap<>();
        remoteItems.put(1, "1");
        remoteItems.put(2, "2");
        remoteItems.put(3, "3");

        Set<Integer> remoteRemovedItems = new HashSet<>();
        remoteRemovedItems.add(4);

        List<String> outLocalFilesToDelete = new ArrayList<>();

        Map<Integer, String> mergedItemsMap = new HashMap<>();

        FileStructMerger.mergeFilesMap(localItems,
                remoteItems,
                emptySet(),
                remoteRemovedItems,
                emptySet(),
                Collections.emptySet(),
                (local, remote) -> true,
                String::valueOf,
                outLocalFilesToDelete::add,
                item -> {},
                item -> {},
                item -> {},
                mergedItemsMap::put,
                mergedItemsMap::put);

        assertEquals(3, mergedItemsMap.size());
        assertEquals("1", mergedItemsMap.get(1));
        assertEquals("2", mergedItemsMap.get(2));
        assertEquals("3", mergedItemsMap.get(3));

        assertTrue(outLocalFilesToDelete.contains("4"));
    }

    @Test
    public void testUploadMissingFilesToRemote() {
        Map<Integer, String> localItems = new HashMap<>();
        localItems.put(1, "1");
        localItems.put(2, "2");
        localItems.put(3, "3");

        Map<Integer, String> remoteItems = new HashMap<>();
        remoteItems.put(1, "1");
        remoteItems.put(2, "2");
        remoteItems.put(3, "3");

        Set<Integer> remoteExistingFiles = new HashSet<>();
        remoteExistingFiles.add(1);

        List<String> outLocalFilesToUpload = new ArrayList<>();

        Map<Integer, String> mergedItemsMap = new HashMap<>();

        FileStructMerger.mergeFilesMap(localItems,
                remoteItems,
                emptySet(),
                emptySet(),
                emptySet(),
                remoteExistingFiles,
                (local, remote) -> true,
                String::valueOf,
                item -> {},
                item -> {},
                outLocalFilesToUpload::add,
                item -> {},
                mergedItemsMap::put,
                mergedItemsMap::put);

        assertEquals(3, mergedItemsMap.size());
        assertEquals("1", mergedItemsMap.get(1));
        assertEquals("2", mergedItemsMap.get(2));
        assertEquals("3", mergedItemsMap.get(3));

        assertTrue(outLocalFilesToUpload.contains("2"));
        assertTrue(outLocalFilesToUpload.contains("3"));
    }

    @Test
    public void testUploadMissedFilesToLocal() {
        Map<Integer, String> localItems = new HashMap<>();
        localItems.put(1, "1");
        localItems.put(2, "2");
        localItems.put(3, "3");

        Map<Integer, String> remoteItems = new HashMap<>();
        remoteItems.put(1, "1");
        remoteItems.put(2, "2");
        remoteItems.put(3, "3");

        Set<Integer> localExistingFiles = new HashSet<>();
        localExistingFiles.add(1);

        List<String> outRemoteFileToDownload = new ArrayList<>();

        Map<Integer, String> mergedItemsMap = new HashMap<>();

        FileStructMerger.mergeFilesMap(localItems,
                remoteItems,
                emptySet(),
                emptySet(),
                localExistingFiles,
                emptySet(),
                (local, remote) -> true,
                String::valueOf,
                item -> {},
                item -> {},
                item -> {},
                outRemoteFileToDownload::add,
                mergedItemsMap::put,
                mergedItemsMap::put);

        assertEquals(3, mergedItemsMap.size());
        assertEquals("1", mergedItemsMap.get(1));
        assertEquals("2", mergedItemsMap.get(2));
        assertEquals("3", mergedItemsMap.get(3));

        assertTrue(outRemoteFileToDownload.contains("2"));
        assertTrue(outRemoteFileToDownload.contains("3"));
    }

    @Test
    public void testDownloadNewFilesFromCloud() {
        Map<Integer, String> localItems = new HashMap<>();
        localItems.put(1, "1");

        Map<Integer, String> remoteItems = new HashMap<>();
        remoteItems.put(1, "1");

        Set<Integer> localExistingFiles = new HashSet<>();
        localExistingFiles.add(1);

        Set<Integer> remoteExistingFiles = new HashSet<>();
        remoteExistingFiles.add(1);
        remoteExistingFiles.add(2);

        List<String> outRemoteFileToDownload = new ArrayList<>();

        Map<Integer, String> mergedItemsMap = new HashMap<>();

        FileStructMerger.mergeFilesMap(localItems,
                remoteItems,
                emptySet(),
                emptySet(),
                localExistingFiles,
                remoteExistingFiles,
                (local, remote) -> true,
                String::valueOf,
                item -> {},
                item -> {},
                item -> {},
                outRemoteFileToDownload::add,
                mergedItemsMap::put,
                mergedItemsMap::put);

        assertEquals(2, mergedItemsMap.size());
        assertEquals("1", mergedItemsMap.get(1));
        assertEquals("2", mergedItemsMap.get(2));

        assertTrue(outRemoteFileToDownload.contains("2"));
    }

    @Test
    public void testReplaceRemoteByLocalItem() {
        Map<Integer, String> localItems = new HashMap<>();
        localItems.put(1, "12");

        Map<Integer, String> remoteItems = new HashMap<>();
        remoteItems.put(1, "1");

        Set<Integer> localExistingFiles = new HashSet<>();
        localExistingFiles.add(1);

        Set<Integer> remoteExistingFiles = new HashSet<>();
        remoteExistingFiles.add(1);

        List<String> outRemoteFileToDownload = new ArrayList<>();

        Map<Integer, String> mergedItemsMap = new HashMap<>();

        FileStructMerger.mergeFilesMap(localItems,
                remoteItems,
                emptySet(),
                emptySet(),
                localExistingFiles,
                remoteExistingFiles,
                (local, remote) -> false,
                String::valueOf,
                item -> {},
                item -> {},
                item -> {},
                outRemoteFileToDownload::add,
                mergedItemsMap::put,
                mergedItemsMap::put);

        assertEquals(1, mergedItemsMap.size());
        assertEquals("12", mergedItemsMap.get(1));
    }

    @Test
    public void testReplaceLocalByRemoteItem() {
        Map<Integer, String> localItems = new HashMap<>();
        localItems.put(1, "12");

        Map<Integer, String> remoteItems = new HashMap<>();
        remoteItems.put(1, "1");

        Set<Integer> localExistingFiles = new HashSet<>();
        localExistingFiles.add(1);

        Set<Integer> remoteExistingFiles = new HashSet<>();
        remoteExistingFiles.add(1);

        List<String> outRemoteFileToDownload = new ArrayList<>();

        Map<Integer, String> mergedItemsMap = new HashMap<>();

        FileStructMerger.mergeFilesMap(localItems,
                remoteItems,
                emptySet(),
                emptySet(),
                localExistingFiles,
                remoteExistingFiles,
                (local, remote) -> true,
                String::valueOf,
                item -> {},
                item -> {},
                item -> {},
                outRemoteFileToDownload::add,
                mergedItemsMap::put,
                mergedItemsMap::put);

        assertEquals(1, mergedItemsMap.size());
        assertEquals("1", mergedItemsMap.get(1));
    }
}