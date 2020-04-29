package com.github.anrimian.musicplayer.domain.utils;

import org.junit.Test;

import static org.junit.Assert.*;

public class FileUtilsTest {

    @Test
    public void getChangedFilePath() {
        String result = FileUtils.getNewPath("1/2/3/same/4/5/same", "new");
        assertEquals("1/2/3/same/4/5/new", result);
    }
}