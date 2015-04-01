/*
 * Licensed under the Apache License, Version 2.0 (the "License"): http://www.apache.org/licenses/LICENSE-2.0
 */

package org.jcruncher;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>Very basic folder monitoring service.</p>
 *
 * <p>TODO: Needs to use the new java.nio WhatService capability.</p>
 *
 * @author Jeremy Chone
 */
public class FileMonitor extends Thread {

    // canonicalFilePath:Mon Map
    private Map<String, Mon>                monMap      = new HashMap<String, Mon>();

    // canonicalFilePath:List of FileListeners Map
    private Map<String, List<FileListener>> listenerMap = new HashMap<String, List<FileListener>>();

    static final FileMonitor                instance    = new FileMonitor();

    static public FileMonitor instance() {
        return instance;
    }

    /**
     * @param file
     * @param fileFilter Only in the case the file is as folder.
     * @param listener
     */
    synchronized public void add(File file, FileFilter fileFilter, FileListener listener) {
        if (file != null) {

            try {
                String filePath = file.getCanonicalPath();
                Mon mon = new Mon(filePath,fileFilter);
                monMap.put(filePath, mon);
                addListener(filePath, listener);
            } catch (IOException e) {
                throw new RuntimeException("Cannot get canonicalPath path for " + file + " " + e.getMessage(), e);
            }
        }
    }

    synchronized private void addListener(String fileCanonicalPath, FileListener listener) {
        if (listener != null) {
            List<FileListener> listeners = listenerMap.get(fileCanonicalPath);
            if (listeners == null) {
                listeners = new ArrayList<FileListener>();
                listenerMap.put(fileCanonicalPath, listeners);
            }
            listeners.add(listener);
        }
    }

    synchronized public void checkAll(){
        for (Mon mon : monMap.values()){
            mon.check();
        }
    }
    
    synchronized private void checkAllAndNotify(){
        
        for (String filePath : monMap.keySet()){
            Mon mon = monMap.get(filePath);
            
            boolean hasChanged = mon.check();
            
            if (hasChanged){
                notifyListeners(filePath);
            }
        }
    }
    
    
    private void notifyListeners(String filePath){
        List<FileListener> listeners = listenerMap.get(filePath);
        if (listeners != null){
            for (FileListener listener : listeners){
                listener.changed(filePath);
            }
        }else{
            System.out.println("\n WARNING: no listeners for " + filePath);
        }
    }
    
    public void run() {
        while (true) {
            try {
                Thread.sleep(1000);
                checkAllAndNotify();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
    }
}

class Mon {

    private FileFilter fileFilter;
    private File       file;
    private boolean    isFolder;

    private long       lastUpdate;
    private int        count = -1;

    Mon(String path, FileFilter fileFilter) {
        this.fileFilter = fileFilter;

        file = new File(path);
        isFolder = file.isDirectory();
    }

    // keep track of the folder/file and return true if it has change since last time.
    synchronized public boolean check() {
        boolean updated = false;
        if (file.exists()) {
            if (isFolder) {
                File[] files = file.listFiles(fileFilter);
                int newCount = files.length;
                long newTime = 0;
                for (File file : files) {
                    long fileTime = file.lastModified();
                    newTime = (fileTime > newTime) ? fileTime : newTime;
                }
                
                if (newCount != count || newTime > lastUpdate){
                    count = newCount;
                    lastUpdate = newTime;
                    return true;
                }
                    
            } else {
               long newTime = file.lastModified();
               if (newTime > lastUpdate){
                   lastUpdate = newTime;
                   return true;
               }
            }
        }
        
        // for now, if the file does not exist, just return false, as we do not delete files

        return updated;
    }
}
