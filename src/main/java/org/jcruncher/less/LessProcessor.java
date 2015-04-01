/*
 * Licensed under the Apache License, Version 2.0 (the "License"): http://www.apache.org/licenses/LICENSE-2.0
 */

package org.jcruncher.less;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

import org.jcruncher.Processor;
import org.jcruncher.Util;
import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
import com.google.common.io.Files;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import jdk.nashorn.internal.objects.NativeArray;
import jdk.nashorn.internal.runtime.ECMAException;
import jdk.nashorn.internal.runtime.ScriptObject;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import static java.nio.file.Files.newBufferedReader;


/**
 * Most of the code comes from https://github.com/asual/lesscss-engine 
 * 
 * If you plan to take or look at some lesscss rhino code, 
 * look at the https://github.com/asual/lesscss-engine. This is just 
 * a quick copy of some of the files. (planning to use lesscss-engine soon) 
 *
 */

public class LessProcessor implements Processor {

    private static final LessProcessor instance   = new LessProcessor();

    public static final FileFilter     fileFilter = new FileFilter() {

                                                      @Override
                                                      public boolean accept(File file) {
                                                          String pathname = file.getName();
                                                          if (pathname.endsWith(".less")) {
                                                              return true;
                                                          } else {
                                                              return false;
                                                          }
                                                      }

                                                  };

    private static final String        JS_ROOT    = "jcruncher/less/";
    // private static final String JS_ROOT = "META-INF/";
    private static final String        CHARSET    = "UTF-8";
    
    private ScriptObjectMirror compileFunc;

    private ResourceLoader loader = new FilesystemResourceLoader(); 

    public LessProcessor() {
        try {

			Path basePath = Util.getClasspathBasePath();
			//System.out.println("basePath .... " + basePath);
			Path lessPath = basePath.resolve(JS_ROOT + "less.js");
			Path envPath = basePath.resolve(JS_ROOT + "env.js");
			Path enginePath = basePath.resolve(JS_ROOT + "engine.js");

            ScriptEngineManager engineManager = new ScriptEngineManager();
            ScriptEngine nashornEngine = engineManager.getEngineByName("nashorn");
            Invocable invocable = (Invocable) nashornEngine;

            ResourceLoader loader = new FilesystemResourceLoader();

            nashornEngine.eval(newBufferedReader(envPath, UTF_8));
            ScriptObjectMirror lessenv = (ScriptObjectMirror) nashornEngine.get("lessenv");
            lessenv.put("charset", "UTF-8");
            lessenv.put("css", false);
            lessenv.put("loader",loader);

            nashornEngine.eval(newBufferedReader(lessPath, UTF_8));

            nashornEngine.eval(newBufferedReader(enginePath, UTF_8));

            compileFunc = (ScriptObjectMirror) nashornEngine.get("compile");

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Cannot intialize LessProcessor:\n" + e.getMessage());
        }
    }

    public FileFilter getFileFilter() {
        return fileFilter;
    }

    public static LessProcessor instance() {
        return instance;
    }

    @Override
    public void process(List<File> sourceFiles, File dest, boolean asNeeded) {
        List<Item> items = new ArrayList<Item>();

        // --------- Build the Item List --------- //
        if (dest.isDirectory()) {
            for (File source : sourceFiles) {
                String baseName = Files.getNameWithoutExtension(source.getName());
                items.add(new Item(source, new File(dest, baseName + ".css")));
            }
        } else {
            items.add(new Item(sourceFiles, dest));
        }
        // --------- /Build the Item List --------- //

        // --------- Process and Save --------- //
        try {
            for (Item item : items) {
                if (!asNeeded || doesItemNeedRefresh(item)) {
                    processItem(item);
                }
            }
        } catch (Exception e) {
            System.out.println("\nERROR: less - cannot process because " + e.getMessage());
            e.printStackTrace();
        }
        // --------- Process and Save --------- //
    }

    private void processItem(Item item) throws IOException {
        StringBuilder contentSB = new StringBuilder();
        System.out.print("less - processing to " + item.dest.getName() + " (");
        int c = 0;
        boolean printFileName = true;
        if (item.sources.size() > 4){
            System.out.print(item.sources.size() + " files");
            printFileName = false;
        }
        for (File file : item.sources) {
            String content = compile(file);
            if (printFileName){
                System.out.print( ((c > 0)?",":"") + file.getName());
            }            
            contentSB.append(content).append("\n");
            c++;
        }
        
        if (item.dest.getParentFile() != null && !item.dest.getParentFile().exists()){
            item.dest.getParentFile().mkdirs();
        }        
        Files.write(contentSB.toString(), item.dest, Charsets.UTF_8);
        System.out.println(") DONE");
    }

    private boolean doesItemNeedRefresh(Item item) {
        for (File source : item.sources) {
            if (item.dest.lastModified() < source.lastModified()) {
                return true;
            }
        }
        return false;
    }

    private String compile(File input) {
        try {
            String result = null;
            // long time = System.currentTimeMillis();
            // logger.debug("Compiling File: " + "file:" + input.getAbsolutePath());
            //result = call(compileFile, new Object[] { "file:" + input.getAbsolutePath(), classLoader });
            
            // logger.debug("The compilation of '" + input + "' took " + (System.currentTimeMillis () - time) + " ms.");
            String location = input.getAbsolutePath();
            String source = Files.toString(input, Charsets.UTF_8);
            boolean compress = false;
            result = call(compileFunc, source, location, compress);
            return result;
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    private synchronized String call(ScriptObjectMirror fn,String source, String location, boolean compress) throws LessException {
        try {
            return (String) fn.call(null, source, location, compress);
        }catch (Exception e) {
            throw new LessException(parseLessException(e));
        }

    }

//    private boolean hasProperty(Scriptable value, String name) {
//        Object property = ScriptableObject.getProperty(value, name);
//        return property != null && !property.equals(UniqueTag.NOT_FOUND);
//    }

    private Exception parseLessException(Exception root) {
        //logger.debug("Parsing LESS Exception", root);
        if (root instanceof ECMAException) {
            ECMAException e = (ECMAException) root;
            Object thrown = e.getThrown();
            String type = null;
            String message = null;
            String filename = null;
            int line = -1;
            int column = -1;
            List<String> extractList = new ArrayList<String>();
            if (thrown instanceof ScriptObject) {
                ScriptObject so = (ScriptObject) e.getThrown();
                type = so.get("type").toString() + " Error";
                message = so.get("message").toString();
                filename = "";
                if (so.has("filename")) {
                    filename = so.get("filename").toString();
                }
                if (so.has("line")) {
                    line = ((Long) so.get("line")).intValue();
                }
                if (so.has("column")) {
                    column = ((Double) so.get("column")).intValue();
                }
                if (so.has("extract")) {
                    NativeArray extract = (NativeArray) so.get("extract");
                    for (int i = 0; i < extract.size(); i++) {
                        if (extract.get(i) instanceof String) {
                            extractList.add(((String) extract.get(i))
                                    .replace("\t", " "));
                        }
                    }
                }
            } else {
                type = thrown.getClass().getSimpleName() + " Error";
                message = e.getMessage().replaceFirst("[^:]+: ", "");
            }
            return new LessException(message, type, filename, line, column,
                    extractList);
        }
        return root;
    }

}

class Item {

    List<File> sources;
    File       dest;   // fileOnly, cannot be folder

    public Item(File source, File dest) {
        this.sources = new ArrayList<File>();
        this.sources.add(source);
        this.dest = dest;
    }

    public Item(List<File> sources, File dest) {
        this.sources = sources;
        this.dest = dest;
    }
}
