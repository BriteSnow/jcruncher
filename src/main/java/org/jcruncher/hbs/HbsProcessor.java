/*
 * Licensed under the Apache License, Version 2.0 (the "License"): http://www.apache.org/licenses/LICENSE-2.0
 */

package org.jcruncher.hbs;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import jdk.nashorn.api.scripting.ScriptObjectMirror;
import org.jcruncher.Util;
import org.jcruncher.Processor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.newBufferedReader;

/**
 * Inspired from https://github.com/asual/lesscss-engine
 *
 **/
public class HbsProcessor implements Processor {

    private static final HbsProcessor instance = new HbsProcessor();

    public static HbsProcessor instance() {
        return instance;
    }

	private static final String HBS_ROOT = "jcruncher/hbs/";

	private ScriptObjectMirror jsHandlebars;
	private Invocable invocable;

	public HbsProcessor(){

		Path basePath = Util.getClasspathBasePath();
		Path envPath = basePath.resolve(HBS_ROOT + "env.js");
		Path hbsPath = basePath.resolve(HBS_ROOT + "handlebars.js");

		// create the nashorn engine
		ScriptEngineManager engineManager = new ScriptEngineManager();
		ScriptEngine engine = engineManager.getEngineByName("nashorn");
		invocable = (Invocable) engine;



		try {
			// eval the env.js to set the global js object(s) that Handlebars is expecting
			engine.eval(newBufferedReader(envPath, UTF_8));
			// load the handlebars library.
			engine.eval(newBufferedReader(hbsPath, UTF_8));

			// get the Handlebars JS object for future references
			jsHandlebars = (ScriptObjectMirror) engine.eval("Handlebars");
		} catch (ScriptException e) {
			throw new RuntimeException("Fail to init HbsProcessor because of script exception: " + e.getMessage());
		} catch (IOException e) {
			throw new RuntimeException("Fail to init HbsProcessor because of IO exception: " + e.getMessage());
		}

	}

	public static final FileFilter fileFilter = new FileFilter() {

		@Override
		public boolean accept(File file) {
			String pathname = file.getName();
			if (pathname.endsWith(".hbs") || pathname.endsWith(".tmpl")) {
				return true;
			} else {
				return false;
			}
		}

	};

	public FileFilter getFileFilter() {
		return fileFilter;
	}

    @Override
    public void process(List<File> sources, File dest, boolean asNeeded) {
        try {
            // --------- Build Item List To Be Compiled --------- //
            List<Item> items = null;
            Item singleItem = null;

            // if the destination is a folder, then, we have a list of Items
            if (dest.isDirectory()) {
                items = new ArrayList<Item>();
            }
            // otherwise, it is just a singleItem (file and the list of the parts)
            else {
                singleItem = new Item(sources, dest);
            }

            // build the Item
            for (File file : sources) {
                String content = Files.toString(file, Charsets.UTF_8);
                String contentName = Files.getNameWithoutExtension(file.getName());
                List<Part> parts = getParts(content, contentName);

                if (items != null) {
                    items.add(new Item(file, new File(dest, contentName + ".js"), parts));
                } else {
                    singleItem.addParts(parts);
                }
            }

            // in case of a singleItem, we update the items as it is the canonical form.
            if (items == null) {
                items = new ArrayList<Item>();
                items.add(singleItem);
            }
            // --------- /Build Item List To Be Compiled --------- //

            // --------- Save Items --------- //
            for (Item item : items) {
                if (!asNeeded || doesItemNeedRefresh(item)){
                    processItem(item);
                }

            }
            // --------- /Save Items --------- //
        } catch (Exception e) {
            System.out.println("\nERROR: hbs cannot process because: " + e.getMessage());
            e.printStackTrace();
        }

    }

    private boolean doesItemNeedRefresh(Item item) {

        for (File source : item.sources) {
            if (item.dest.lastModified() < source.lastModified()) {
                return true;
            }
        }
        return false;
    }

    private void processItem(Item item) throws Exception {
        StringBuffer sb = new StringBuffer();
        sb.append("Handlebars.templates = Handlebars.templates || {};\n\n");
        
        System.out.print("hbs  - processing to " + item.dest.getName() + " ... ");
        for (Part part : item.parts) {
            String tmplpc = precompile(part.content);
            sb.append("\n// template --- ").append(part.name).append(" ---\n");
            sb.append("Handlebars.templates['").append(part.name).append("'] = Handlebars.template(");
            sb.append(tmplpc);
            sb.append("\n);\n");
        }
        
        if (item.dest.getParentFile() != null && !item.dest.getParentFile().exists()){
            item.dest.getParentFile().mkdirs();
        }
        Files.write(sb.toString(), item.dest, Charsets.UTF_8);
        System.out.println("DONE");
    }

    public String precompile(String template) {
        try {
            Object templateFunc = invocable.invokeMethod(jsHandlebars, "precompile", template);
            return templateFunc.toString();
        }catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private static List<Part> getParts(String content, String contentName) {
        List<Part> parts = new ArrayList<Part>();

        // if we have a script
        if (content.indexOf("<script") > -1) {
            Document doc = Jsoup.parseBodyFragment(content);
            Element body = doc.body();

            Elements metaElements = body.select("script");
            for (Iterator<Element> ite = metaElements.iterator(); ite.hasNext();) {
                Element metaElem = ite.next();
                String tmpl = metaElem.html();
                String tmplId = metaElem.attr("id");
                parts.add(new Part(tmplId, tmpl));
            }
        }
        // otherwise, if just a template content, then, use the name as name
        else {
            parts.add(new Part(contentName, content));
        }

        return parts;
    }



}

class Item {

    List<File> sources;
    File       dest; // File only (cannot be folder)
    List<Part> parts;

    public Item(List<File> sources, File dest) {
        this.sources = sources;
        this.dest = dest;
        this.parts = new ArrayList<Part>();
    }

    public Item(File source, File dest, List<Part> parts) {
        this.sources = new ArrayList<File>();
        this.sources.add(source);
        this.dest = dest;
        this.parts = parts;
    }

    public void addParts(List<Part> parts) {
        this.parts.addAll(parts);
    }

}

class Part {

    String name;
    String content;

    public Part(String name, String content) {
        this.name = name;
        this.content = content;
    }
}
