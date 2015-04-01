package org.jcruncher.cli;

import org.junit.Test;

public class JCruncherMainInteractiveTest {

    @Test
    public void emptyTest(){
        
    }
    
    //@Test
    public void testTMPLInteractive(){
        String[] args = "-i --hbs src/test/resources/jcruncher/hbs/tmpl/ templates.js".split(" ");
        JCruncherMain.main(args);
    }    
}
