package org.jcruncher.cli;

import java.io.File;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class JCruncherMainTest {

    //@Test
    public void testHBS() {
        try {
            String[] args = "--hbs src/test/resources/jcruncher/hbs/hbs/ test-hbs/".split(" ");
            JCruncherMain.main(args);
            deleteDir(new File("test-hbs/"));
        } catch (Throwable t) {
            Assert.fail("JCruncherExTest.testHBS failed: " + t.getMessage());
        }
    }

    @Test
    public void testTMPL() {
        try {
            String[] args = "--hbs  src/test/resources/jcruncher/hbs/tmpl/ test-tmpl/templates.js".split(" ");
            JCruncherMain.main(args);
            deleteDir(new File("test-tmpl/"));
        } catch (Throwable t) {
            Assert.fail("JCruncherExTest.testTMPL failed: " + t.getMessage());
        }
    }

    @Test
    public void testWithLessHatImport() {
        try {
            String[] args = "--less src/test/resources/jcruncher/less/folder1/less-with-import.less test-css/test-with-lesshat-import.css".split(" ");
            JCruncherMain.main(args);
            deleteDir(new File("test-css/"));
        } catch (Throwable t) {
            t.printStackTrace();
            Assert.fail("JCruncherExTest.testTMPL failed: " + t.getMessage());
        }
    }

    @Test
    public void testWithSimpleUtilsImport() {
        try {
            String[] args = "--less src/test/resources/jcruncher/less/folder1/with-utils-import.less test-css/test-with-simple-utils-import.css".split(" ");
            JCruncherMain.main(args);
            String cssContent = Files.toString(new File("test-css/test-with-simple-utils-import.css"),Charsets.UTF_8);
            assertTrue(cssContent.contains(".myDiv {\n  position: absolute"));
            deleteDir(new File("test-css/"));
        } catch (Throwable t) {
            t.printStackTrace();
            Assert.fail("JCruncherExTest.testTMPL failed: " + t.getMessage());
        }
    }

    // Try to remove the test files, somehow, very inconsistent. test-tmpl/ does not get removed?
    private void deleteDir(File dir) {
        System.out.println("Will delete: " + dir.getName());
        System.gc();
        if (dir.isFile()) {
            dir.delete();
            dir.deleteOnExit();
        } else {
            // to help the delete
            for (File file : dir.listFiles()) {
                if (file.isFile()) {
                    file.delete();
                    file.deleteOnExit();
                }
            }
            dir.delete();
            dir.deleteOnExit();
        }
    }
}
