/*
 * Licensed under the Apache License, Version 2.0 (the "License"): http://www.apache.org/licenses/LICENSE-2.0
 */

package org.jcruncher.less;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * From https://github.com/asual/lesscss-engine
 *
 * A {@link ResourceLoader} that loads resources from the file system.
 * 
 * @author Rafał Krzewski
 */
public class FilesystemResourceLoader extends StreamResourceLoader {

	private final static String SCHEMA = "file";

	@Override
	protected String getSchema() {
		return SCHEMA;
	}

	/**
	 * Note that path should be absolute, otherwise the results are dependent on
	 * the VM's {@code user.dir}.
	 */
	@Override
	protected InputStream openStream(String path) throws IOException {
		File file = new File(path);
		if (file.isFile() && file.canRead()) {
			return new FileInputStream(path);
		} else {
			return null;
		}
	}
}
