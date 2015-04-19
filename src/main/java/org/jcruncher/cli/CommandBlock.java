/*
 * Licensed under the Apache License, Version 2.0 (the "License"): http://www.apache.org/licenses/LICENSE-2.0
 */

package org.jcruncher.cli;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.jcruncher.Processor;
import org.jcruncher.hbs.HbsProcessor;
import org.jcruncher.less.LessProcessor;

/**
 * @author Jeremy Chone
 */
public class CommandBlock {
    public enum Type {
        LESS, HBS;
    }

    public enum FileType {
        NULL, FILE, FOLDER, NOEXIST;
    }
    
    private Type type;
    private File source;
    private File dest;
    private String[] excludes;
    

    public CommandBlock(Type type) {
        this.type = type;
    }
    
    public int setNextArg(String[] args,int idx){
        int offset = 1;
        String arg = args[idx];
        
        if (arg.startsWith("-")){
            if (arg.equals("-e")){
                
                if (args.length < idx + 1){
                    throw new RuntimeException("Wrong -e params, must be -e name[;otherName]");
                }
                String arg2 = args[idx + 1];
                excludes = arg2.split(";");
                offset = 2;
            }
        }else{
            if (source == null){
                setSource(new File(arg));
            }else if (dest == null){
                File dest = new File(arg);
                if (arg.endsWith(File.separator)){
                    dest.mkdirs();
                }
                setDest(dest);            
            }
        }
        
        return offset;
    }
    
    
    public void process(boolean asNeeded){
        Processor proc = getProcessor();
        
        // --------- build the sources --------- //
        List<File> sources = new ArrayList<>();
        FileType sourceType = getSourceType();
        File source = getSource();
        if (sourceType == FileType.FILE){
            if (!accept(source)){
                throw new RuntimeException("ERROR: " + type + " file " + getSource() + " does not match extension (should be .tmpl/.hbs, or .less) or does not exists");
            }
            sources.add(getSource());
        }else if (sourceType == FileType.FOLDER){
            for (File sourceFile : source.listFiles()){
                if (accept(sourceFile)){
                    sources.add(sourceFile);
                }
            }
        }else{
            throw new RuntimeException("ERROR: " + type + " file " + getSource() + " not found: " + sourceType);
        }
        // --------- /build the sources --------- //
        
        // check the destination folder
        File dest = getDest();
        dest = (dest != null)?dest:getSourceFolder();
        
        proc.process(sources, dest, asNeeded);
    }
    
    public boolean accept(File file){
        String filePath = file.getPath();
        Processor proc = getProcessor();
        
        if (!proc.getFileFilter().accept(file)){
            return false;
        }
        
        if (excludes != null){
            for (String excludePath : excludes){
                if (filePath.endsWith(excludePath)){
                    return false;
                }
            }
        }
        
        return true;
    }
    

    // --------- Properties Getters & Setters --------- //
    public Type getType() {
        return type;
    }

    public File getSource() {
        return source;
    }
    
    public File getSourceFolder(){
        if (source != null){
            if (source.isDirectory()){
                return source;
            }else{
                return source.getParentFile();
            }
        }else{
            return null;
        }
    }
    
    public File getFileToMonitor(){
        return getSource();
    }

    public CommandBlock setSource(File source) {
        this.source = source;
        return this;
    }

    public FileType getSourceType() {
        return CommandBlock.getFileType(source);
    }

    public File getDest() {
        return dest;
    }

    public CommandBlock setDest(File dest) {
        this.dest = dest;
        return this;
    }

    public FileType getDestType() {
        return getFileType(dest);
    }
    // --------- /Properties Getters & Setters --------- //

    public Processor getProcessor(){
        switch(type){
            case HBS:
                return HbsProcessor.instance();
            case LESS:
                return LessProcessor.instance();
            default:
                return null;
        }
    }
    
    static private FileType getFileType(File file) {
        if (file == null) {
            return FileType.NULL;
        }
        if (!file.exists()) {
            return FileType.NOEXIST;
        }
        if (file.isDirectory()) {
            return FileType.FOLDER;
        } else if (file.isFile()) {
            return FileType.FILE;
        } else {
            return FileType.NOEXIST;
        }
    }

}
