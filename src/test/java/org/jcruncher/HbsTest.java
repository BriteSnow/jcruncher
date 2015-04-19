/*
 * Licensed under the Apache License, Version 2.0 (the "License"): http://www.apache.org/licenses/LICENSE-2.0
 */

package org.jcruncher;

import org.jcruncher.hbs.HbsProcessor;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class HbsTest {


	@Test
	public void simpleHbsCompileTest() {
		HbsProcessor hbsProcess = new HbsProcessor();

		String templateStr = "<b>hello {{name}}</b>";
		String templateFunc = hbsProcess.precompile(templateStr);
		assertTrue(templateFunc.contains("return \"<b>hello \""));
		System.out.println("memory usage: " + Util.getUsedMemory() + "MB total: " + Util.getTotalMemory() + "MB" );
	}

	@Test
	public void compileFile(){
		//HbsProcessor hbsProcess = new HbsProcessor();
		// TODO:
	}
}
