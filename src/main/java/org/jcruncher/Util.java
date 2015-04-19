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


	static public long getUsedMemory(){
		Runtime rt = Runtime.getRuntime();
		long usedMB = (rt.totalMemory() - rt.freeMemory()) / 1024 / 1024;
		return usedMB;
	}

	static public long getTotalMemory(){
		return Runtime.getRuntime().totalMemory() / 1024 / 1024;
	}

	static public long getFreeMemory(){
		return Runtime.getRuntime().freeMemory() / 1024 / 1024;
	}

	static public PerfCtx startPerf(){
		return new PerfCtx();
	}

	public static class PerfCtx{
		private final long startTime;
		private final long startTotalMemory;
		private final long startFreeMemory;

		private long endTime;
		private long endTotalMemory;
		private long endFreeMemory;

		PerfCtx(){
			startTime = System.currentTimeMillis();
			startTotalMemory = getTotalMemory();
			startFreeMemory = getFreeMemory();
		}

		public PerfCtx end(){
			endTime = System.currentTimeMillis();
			endTotalMemory = getTotalMemory();
			endFreeMemory = getFreeMemory();
			return this;
		}

		public String toString(){
			StringBuilder sb = new StringBuilder();
			sb.append("Before MEM used/total: ").append(startTotalMemory - startFreeMemory).append("MB/").append(startTotalMemory).append("MB");
			sb.append(" | After MEM used/total: ").append(endTotalMemory - endFreeMemory).append("MB/").append(endTotalMemory).append("MB");
			sb.append(" | Duration: " + (endTime - startTime) + "ms");
			return sb.toString();
		}

	}
}
