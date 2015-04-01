/*
 * Licensed under the Apache License, Version 2.0 (the "License"): http://www.apache.org/licenses/LICENSE-2.0
 */

package org.jcruncher;

import java.net.URI;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
import com.google.common.io.Resources;

/**
 * Various utils for jcruncher.
 * @author Jeremy Chone
 **/
public class Util {

	static private Path classpathBasePath;

	/**
	 * Return the base Path of the class path.
	 * This allows to run this library on executable jar, or when resource files are in file system folder.
	 *
	 * @return
	 */
	static public synchronized Path getClasspathBasePath(){
		FileSystem fs;
		if (classpathBasePath == null){
			try {
				ClassLoader classLoader = Util.class.getClassLoader();
				URL root = classLoader.getResource("jcruncher/less/less.js");
				String urlStr = root.toString();
				if (urlStr.startsWith("jar")){
					fs = FileSystems.newFileSystem(URI.create(urlStr.split("!")[0]), Collections.emptyMap());
					classpathBasePath = fs.getPath("./");
				}else{
					fs = FileSystems.getDefault();
					classpathBasePath = Paths.get(root.toURI()).getParent().getParent().getParent();
				}
			}catch (Throwable t){
				throw new RuntimeException("Failed to find classpathBasePath " + t.getClass().getName() + " : " + t.getMessage());
			}
		}

		return classpathBasePath;
	}

}
