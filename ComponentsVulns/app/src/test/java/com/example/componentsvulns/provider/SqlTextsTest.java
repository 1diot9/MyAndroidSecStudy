package com.example.componentsvulns.provider;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SqlTextsTest {

    @Test
    public void emptySelectionDoesNotAddWhere() {
        assertEquals(
                "SELECT _id, title, body, secret FROM notes ORDER BY _id ASC",
                SqlTexts.vulnerableNotesQuery(null));
        assertEquals(
                "SELECT _id, title, body, secret FROM notes ORDER BY _id ASC",
                SqlTexts.vulnerableNotesQuery("   "));
    }

    @Test
    public void callerTextIsConcatenatedIntoWhere() {
        String sql = SqlTexts.vulnerableNotesQuery("secret=0 OR 1=1");
        assertTrue(sql.contains("WHERE secret=0 OR 1=1"));
    }

    @Test
    public void deleteAndUpdateConcatenateSelection() {
        assertEquals("DELETE FROM notes", SqlTexts.vulnerableNotesDelete(null));
        assertEquals(
                "DELETE FROM notes WHERE secret=0 OR 1=1",
                SqlTexts.vulnerableNotesDelete("secret=0 OR 1=1"));
        assertEquals(
                "UPDATE notes SET body='已篡改' WHERE secret=1",
                SqlTexts.vulnerableNotesUpdate("secret=1"));
    }
}
