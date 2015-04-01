/*
 * Licensed under the Apache License, Version 2.0 (the "License"): http://www.apache.org/licenses/LICENSE-2.0
 */

package org.jcruncher;

import java.io.File;
import java.io.FileFilter;
import java.util.List;


/**
 * @author Jeremy Chone
 **/
public interface Processor {

    
    
    
    
    /**
     * @param sourceFiles List of source files that need to be process
     * @param dest The target file or folder. 
     * @param asNeeded This tell the processor to only include source file that have a > 
     *        timestamp than their destination file
     */
    public void process(List<File> sourceFiles,File dest, boolean asNeeded);
    
    
    public FileFilter getFileFilter();
}
