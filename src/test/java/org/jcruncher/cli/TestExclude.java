package org.jcruncher.cli;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

public class TestExclude {

    @Test
    public void testSingleLess(){
        try {
            String[] args = "--less src/test/resources/jcruncher/less/folder1/ test-exclude-css/ -e lesshat.less".split(" ");
            JCruncherMain.main(args);
            deleteDir(new File("test-exclude-css/"));
        } catch (Throwable t) {
            t.printStackTrace();
            Assert.fail("JCruncherExTest.testTMPL failed: " + t.getMessage());
        }        
    }
    
    
    private void deleteDir(File dir){
        for (File file : dir.listFiles()){
            if (file.isFile()){
                file.delete();
            }
        }
        dir.delete();
        dir.deleteOnExit();
    }
}
