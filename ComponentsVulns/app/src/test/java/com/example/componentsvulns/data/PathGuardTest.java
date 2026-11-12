package com.example.componentsvulns.data;

import org.junit.Test;

import java.io.File;
import java.nio.file.Files;

import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class PathGuardTest {

    @Test
    public void nameInsideDemoIsAllowed() throws Exception {
        File root = Files.createTempDirectory("path-guard").toFile();
        File target = PathGuard.requireInside(root, "readme.txt");
        assertTrue(target.getPath().startsWith(root.getCanonicalPath() + File.separator));
    }

    @Test
    public void parentSegmentIsRejected() throws Exception {
        File root = Files.createTempDirectory("path-guard").toFile();
        assertThrows(SecurityException.class, () -> PathGuard.requireInside(root, "../pin.txt"));
    }
}
