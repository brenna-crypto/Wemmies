package com.wemmies.app;

import com.wemmies.app.model.Wemmie;

import org.junit.Test;

import static org.junit.Assert.*;

public class WemmieModelTest {

    @Test
    public void testWemmieCreation_hasCorrectDefaults() {
        Wemmie wemmie = new Wemmie("thought", "sad");

        assertEquals(0, wemmie.getEmpathyCount());
        assertFalse(wemmie.isTransformed());
    }
@Test
public void testIsTransformed_falseAt4() {
    Wemmie wemmie = new Wemmie("thought", "sad");
    wemmie.setEmpathyCount(4);

    assertFalse(wemmie.isTransformed());
    }
    @Test
    public void testIsTransformed_trueAt5() {
        Wemmie wemmie = new Wemmie("thought", "sad");
        wemmie.setEmpathyCount(5);

        assertTrue(wemmie.isTransformed());
    }
    @Test
    public void testIsTransformed_trueAbove5() {
        Wemmie wemmie = new Wemmie("thought", "sad");
        wemmie.setEmpathyCount(10);

        assertTrue(wemmie.isTransformed());
    }
    @Test
    public void testNoArgConstructor_existsForFirestore() {
        Wemmie wemmie = new Wemmie();

        assertNotNull(wemmie);
    }
    @Test
    public void testId_nullBeforeSet() {
        Wemmie wemmie = new Wemmie();

        assertNull(wemmie.getId());
    }
    @Test
    public void testShamefulThought_getterMatchesSetter() {
        Wemmie wemmie = new Wemmie("my thought", "sad");

        assertEquals("my thought", wemmie.getShamefulThought());
    }
    @Test
    public void testUserId_storedCorrectly() {
        Wemmie wemmie = new Wemmie();
        wemmie.setUserId("uid123");

        assertEquals("uid123", wemmie.getUserId());
    }
    @Test
    public void testTimestamp_storedCorrectly() {
        Wemmie wemmie = new Wemmie();
        wemmie.setTimestamp(999L);

        assertEquals(999L, wemmie.getTimestamp());
    }
}