package com.github.anrimian.musicplayer.domain.interactors.sync;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class StructMergerTest {

    //delete metadata case

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

        StructMerger.mergeFilesMap(localItems,
                remoteItems,
                emptyMap(),
                emptyMap(),
                emptySet(),
                Collections.emptySet(),
                (local, remote) -> false,
                (local, remote) -> false,
                (item, deletedItem) -> true,
                String::valueOf,
                item -> {},
                item -> {},
                outLocalFilesToUpload::add,
                outRemoteFileToDownload::add,
                localItems::put,
                (key, item) -> {},
                (key, oldItem, item) -> {},
                remoteItems::put,
                (key, item) -> {},
                (key, oldItem, item) -> {},
                (key, item) -> {},
                (key, item) -> {});

        assertEquals(4, localItems.size());
        assertEquals("3", localItems.get(3));
        assertEquals("4", localItems.get(4));

        assertEquals(4, remoteItems.size());
        assertEquals("1", remoteItems.get(1));
        assertEquals("2", remoteItems.get(2));

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

        Map<Integer, Object> localRemovedItems = new HashMap<>();
        localRemovedItems.put(4, new Object());

        List<String> outRemoteFilesToDelete = new ArrayList<>();

        StructMerger.mergeFilesMap(localItems,
                remoteItems,
                localRemovedItems,
                emptyMap(),
                emptySet(),
                Collections.emptySet(),
                (local, remote) -> false,
                (local, remote) -> false,
                (item, deletedItem) -> true,
                String::valueOf,
                item -> {},
                outRemoteFilesToDelete::add,
                item -> {},
                item -> {},
                localItems::put,
                (key, item) -> {},
                (key, oldItem, item) -> {},
                remoteItems::put,
                (key, item) -> remoteItems.remove(key),
                (key, oldItem, item) -> {},
                (key, item) -> {},
                (key, item) -> {});

        assertEquals(3, remoteItems.size());
        assertEquals("1", remoteItems.get(1));
        assertEquals("2", remoteItems.get(2));
        assertEquals("3", remoteItems.get(3));

        assertTrue(outRemoteFilesToDelete.contains("4"));
    }

    @Test
    public void testIrrelevantDeleteFromLocal() {
        Map<Integer, String> localItems = new HashMap<>();
        localItems.put(1, "1");
        localItems.put(2, "2");
        localItems.put(3, "3");

        Map<Integer, String> remoteItems = new HashMap<>();
        remoteItems.put(1, "1");
        remoteItems.put(2, "2");
        remoteItems.put(3, "3");
        remoteItems.put(4, "4");

        Map<Integer, String> localRemovedItems = new HashMap<>();
        localRemovedItems.put(4, "4R");

        List<String> outRemoteFilesToDelete = new ArrayList<>();

        List<String> outNotActualRemovedFiles = new ArrayList<>();

        StructMerger.mergeFilesMap(localItems,
                remoteItems,
                localRemovedItems,
                emptyMap(),
                emptySet(),
                Collections.emptySet(),
                (local, remote) -> false,
                (local, remote) -> false,
                (item, deletedItem) -> false,
                String::valueOf,
                item -> {},
                outRemoteFilesToDelete::add,
                item -> {},
                item -> {},
                localItems::put,
                (key, item) -> {},
                (key, oldItem, item) -> {},
                remoteItems::put,
                (key, item) -> remoteItems.remove(key),
                (key, oldItem, item) -> {},
                (key, item) -> outNotActualRemovedFiles.add(item),
                (key, item) -> {});

        assertEquals(4, remoteItems.size());
        assertEquals("4", remoteItems.get(4));

        assertEquals(4, localItems.size());
        assertEquals("4", localItems.get(4));

        assertEquals(1, outNotActualRemovedFiles.size());
        assertEquals("4R", outNotActualRemovedFiles.get(0));

        assertTrue(outRemoteFilesToDelete.isEmpty());
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

        Map<Integer, Object> remoteRemovedItems = new HashMap<>();
        remoteRemovedItems.put(4, new Object());

        List<String> outLocalFilesToDelete = new ArrayList<>();

        StructMerger.mergeFilesMap(localItems,
                remoteItems,
                emptyMap(),
                remoteRemovedItems,
                emptySet(),
                Collections.emptySet(),
                (local, remote) -> false,
                (local, remote) -> false,
                (item, deletedItem) -> true,
                String::valueOf,
                outLocalFilesToDelete::add,
                item -> {},
                item -> {},
                item -> {},
                localItems::put,
                (key, item) -> localItems.remove(key),
                (key, oldItem, item) -> {},
                remoteItems::put,
                (key, item) -> {},
                (key, oldItem, item) -> {},
                (key, item) -> {},
                (key, item) -> {});

        assertEquals(3, localItems.size());
        assertEquals("1", localItems.get(1));
        assertEquals("2", localItems.get(2));
        assertEquals("3", localItems.get(3));

        assertTrue(outLocalFilesToDelete.contains("4"));
    }

    @Test
    public void testIrrelevantDeleteFromRemote() {
        Map<Integer, String> localItems = new HashMap<>();
        localItems.put(1, "1");
        localItems.put(2, "2");
        localItems.put(3, "3");
        localItems.put(4, "4");

        Map<Integer, String> remoteItems = new HashMap<>();
        remoteItems.put(1, "1");
        remoteItems.put(2, "2");
        remoteItems.put(3, "3");

        Map<Integer, String> remoteRemovedItems = new HashMap<>();
        remoteRemovedItems.put(4, "4R");

        List<String> outLocalFilesToDelete = new ArrayList<>();

        List<String> outNotActualRemovedFiles = new ArrayList<>();

        StructMerger.mergeFilesMap(localItems,
                remoteItems,
                emptyMap(),
                remoteRemovedItems,
                emptySet(),
                Collections.emptySet(),
                (local, remote) -> false,
                (local, remote) -> false,
                (item, deletedItem) -> false,
                String::valueOf,
                outLocalFilesToDelete::add,
                item -> {},
                item -> {},
                item -> {},
                localItems::put,
                (key, item) -> localItems.remove(key),
                (key, oldItem, item) -> {},
                remoteItems::put,
                (key, item) -> {},
                (key, oldItem, item) -> {},
                (key, item) -> {},
                (key, item) -> outNotActualRemovedFiles.add(item));

        assertEquals(4, remoteItems.size());
        assertEquals("4", remoteItems.get(4));

        assertEquals(4, localItems.size());
        assertEquals("4", localItems.get(4));

        assertEquals(1, outNotActualRemovedFiles.size());
        assertEquals("4R", outNotActualRemovedFiles.get(0));

        assertTrue(outLocalFilesToDelete.isEmpty());
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

        StructMerger.mergeFilesMap(localItems,
                remoteItems,
                emptyMap(),
                emptyMap(),
                emptySet(),
                remoteExistingFiles,
                (local, remote) -> false,
                (local, remote) -> false,
                (item, deletedItem) -> true,
                String::valueOf,
                item -> {},
                item -> {},
                outLocalFilesToUpload::add,
                item -> {},
                localItems::put,
                (key, item) -> {},
                (key, oldItem, item) -> {},
                remoteItems::put,
                (key, item) -> {},
                (key, oldItem, item) -> {},
                (key, item) -> {},
                (key, item) -> {});

        assertEquals(3, remoteItems.size());
        assertEquals("1", remoteItems.get(1));
        assertEquals("2", remoteItems.get(2));
        assertEquals("3", remoteItems.get(3));

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

        StructMerger.mergeFilesMap(localItems,
                remoteItems,
                emptyMap(),
                emptyMap(),
                localExistingFiles,
                emptySet(),
                (local, remote) -> false,
                (local, remote) -> false,
                (item, deletedItem) -> true,
                String::valueOf,
                item -> {},
                item -> {},
                item -> {},
                outRemoteFileToDownload::add,
                localItems::put,
                (key, item) -> {},
                (key, oldItem, item) -> {},
                remoteItems::put,
                (key, item) -> {},
                (key, oldItem, item) -> {},
                (key, item) -> {},
                (key, item) -> {});

        assertEquals(3, localItems.size());
        assertEquals("1", localItems.get(1));
        assertEquals("2", localItems.get(2));
        assertEquals("3", localItems.get(3));

        assertEquals(3, remoteItems.size());
        assertEquals("1", remoteItems.get(1));
        assertEquals("2", remoteItems.get(2));
        assertEquals("3", remoteItems.get(3));

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

        StructMerger.mergeFilesMap(localItems,
                remoteItems,
                emptyMap(),
                emptyMap(),
                localExistingFiles,
                remoteExistingFiles,
                (local, remote) -> false,
                (local, remote) -> false,
                (item, deletedItem) -> true,
                String::valueOf,
                item -> {},
                item -> {},
                item -> {},
                outRemoteFileToDownload::add,
                localItems::put,
                (key, item) -> {},
                (key, oldItem, item) -> {},
                remoteItems::put,
                (key, item) -> {},
                (key, oldItem, item) -> {},
                (key, item) -> {},
                (key, item) -> {});

        assertEquals(2, localItems.size());
        assertEquals("1", localItems.get(1));
        assertEquals("2", localItems.get(2));

        assertEquals(2, remoteItems.size());
        assertEquals("1", remoteItems.get(1));
        assertEquals("2", remoteItems.get(2));

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

        List<String> outLocalFilesToUpload = new ArrayList<>();

        StructMerger.mergeFilesMap(localItems,
                remoteItems,
                emptyMap(),
                emptyMap(),
                localExistingFiles,
                remoteExistingFiles,
                (local, remote) -> true,
                (local, remote) -> true,
                (item, deletedItem) -> true,
                String::valueOf,
                item -> {},
                item -> {},
                outLocalFilesToUpload::add,
                item -> {},
                localItems::put,
                (key, item) -> {},
                (key, oldItem, item) -> {},
                remoteItems::put,
                (key, item) -> {},
                (key, oldItem, item) -> remoteItems.put(key, item),
                (key, item) -> {},
                (key, item) -> {});

        assertEquals(1, remoteItems.size());
        assertEquals("12", remoteItems.get(1));

        assertEquals(1, localItems.size());
        assertEquals("12", localItems.get(1));

        assert outLocalFilesToUpload.contains("12");
    }

    @Test
    public void testReplaceLocalByRemoteItem() {
        Map<Integer, String> localItems = new HashMap<>();
        localItems.put(1, "1");

        Map<Integer, String> remoteItems = new HashMap<>();
        remoteItems.put(1, "12");

        Set<Integer> localExistingFiles = new HashSet<>();
        localExistingFiles.add(1);

        Set<Integer> remoteExistingFiles = new HashSet<>();
        remoteExistingFiles.add(1);

        List<String> outRemoteFileToDownload = new ArrayList<>();

        StructMerger.mergeFilesMap(localItems,
                remoteItems,
                emptyMap(),
                emptyMap(),
                localExistingFiles,
                remoteExistingFiles,
                (local, remote) -> true,
                (local, remote) -> false,
                (item, deletedItem) -> true,
                String::valueOf,
                item -> {},
                item -> {},
                item -> {},
                outRemoteFileToDownload::add,
                localItems::put,
                (key, item) -> {},
                (key, oldItem, item) -> localItems.put(key, item),
                remoteItems::put,
                (key, item) -> {},
                (key, oldItem, item) -> {},
                (key, item) -> {},
                (key, item) -> {});

        assertEquals(1, remoteItems.size());
        assertEquals("12", remoteItems.get(1));

        assertEquals(1, localItems.size());
        assertEquals("12", localItems.get(1));

        assert outRemoteFileToDownload.contains("12");
    }
}