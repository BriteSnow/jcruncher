/*
 * Licensed under the Apache License, Version 2.0 (the "License"): http://www.apache.org/licenses/LICENSE-2.0
 */

package org.jcruncher.less;

import java.io.IOException;

/**
 * ResourceLoader is used to locate and load stylesheets referenced with @include
 * directive.
 *
 * From https://github.com/asual/lesscss-engine 
 * @author Rafał Krzewski
 */
public interface ResourceLoader {

	/**
	 * Checks if the given resource exists.
	 *
	 * @param resource
	 *            relative resource file path.
	 * @param paths
	 *            paths to search for resource under.
	 * @return {@code true} if the resource exists.
	 * @throws IOException
	 *             when i/o error occurs while checking for resource existence.
	 */
	public boolean exists(String resource, String[] paths) throws IOException;

	/**
	 * Loads the given resource's contents.
	 *
	 * @param resource
	 *            relative resource file path.
	 * @param paths
	 *            paths to search for resource under.
	 * @param charset
	 *            character set name, valid with respect to
	 *            {@link java.nio.charset.Charset}.
	 * @return resource contents as a string.
	 * @throws IOException
	 *             when i/o error occurs while loading the resource, or charset
	 *             is invalid.
	 */
	public String load(String resource, String[] paths, String charset) throws IOException;
}
