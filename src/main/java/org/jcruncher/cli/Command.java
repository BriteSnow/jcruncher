/*
 * Licensed under the Apache License, Version 2.0 (the "License"): http://www.apache.org/licenses/LICENSE-2.0
 */

package org.jcruncher.cli;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Jeremy Chone
 */
public class Command {

    private List<CommandBlock> blocks = new ArrayList<CommandBlock>();

    public boolean interactive = false;
    
    /**
     * @param block nullable, if null nothing get added.
     * @return
     */
    public Command add(CommandBlock block){
        if (block != null){
            blocks.add(block);
        }
        return this;
    }
    
    public void process(boolean asNeeded){
        for (CommandBlock block : blocks){
            block.process(asNeeded);
        }
    }
    
    
    public List<CommandBlock> getBlocks(){
        return blocks;
    }
    
    
    
    
    public String toString(){
        StringBuilder sb = new StringBuilder();
        
        sb.append("Command (auto:").append(interactive).append(")");
        for (CommandBlock block : blocks){
            sb.append("\n\tblock: ").append(block.getType());
            sb.append("\n\t  ").append("source: ").append(block.getSourceType());
            sb.append("\n\t  ").append("dest: ").append(block.getDestType());
        }
        
        return sb.toString();
    }
}
