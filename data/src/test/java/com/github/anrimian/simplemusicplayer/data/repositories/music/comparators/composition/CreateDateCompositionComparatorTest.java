package com.github.anrimian.simplemusicplayer.data.repositories.music.comparators.composition;

import com.github.anrimian.simplemusicplayer.domain.models.composition.Composition;

import org.junit.Test;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import static com.github.anrimian.simplemusicplayer.data.TestDataProvider.fakeComposition;
import static java.util.Arrays.asList;
import static junit.framework.Assert.assertEquals;

public class CreateDateCompositionComparatorTest {

    @Test
    public void applyOrder() {
        Composition compositionOne = fakeComposition(1, "A", 1L);
        Composition compositionTwo = fakeComposition(2, "B", 2L);
        Composition compositionThree = fakeComposition(3, "C", 3L);

        List<Composition> compositions = asList(compositionOne, compositionTwo, compositionThree);

        Collections.sort(compositions, new CreateDateCompositionComparator());

        assertEquals(compositionOne, compositions.get(0));
        assertEquals(compositionTwo, compositions.get(1));
        assertEquals(compositionThree, compositions.get(2));
    }
}