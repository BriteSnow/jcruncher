/*
 * Licensed under the Apache License, Version 2.0 (the "License"): http://www.apache.org/licenses/LICENSE-2.0
 */

package org.jcruncher.cli;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.jcruncher.FileListener;
import org.jcruncher.FileMonitor;
import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.io.Files;

/**
 * 
 * @author Jeremy Chone
 */
public class JCruncherMain {
    private static final String       version             = "0.9.3";
    private static final String       LESS                = "--less";
    private static final String       HBS                 = "--hbs";

    private static final String       INTERACTIVE_MESSAGE = "--- Mode Intractive: 'q' for quit, 'r' for force refresh: ";

    private static Command            command = null;

    private static final FileMonitor fileMonitor         = FileMonitor.instance();

    private static final FileListener fileListener        = new FileListener() {

                                                              @Override
                                                              public void changed(String filePath) {
                                                                  System.out.println("\n Change(s) detected");
                                                                  command.process(true);
                                                                  System.out.print(INTERACTIVE_MESSAGE);
                                                              }

                                                          };

    public static void main(String[] args) {
        try {
            command = parseArgs(args, command);

            // if not blocks, then, attempt to parse the "jcruncher.cfg"
            if (command.getBlocks().size() == 0) {
                File cfg = new File("jcruncher.cfg");
                
                if (!cfg.exists()){
                    throw new RuntimeException("Don't know what to do. Must run with either a 'jcruncher.cfg' file, or some '--less' or '--hbs' arguments");
                }
                args = parseArgsFile(cfg);

                command = parseArgs(args, command);
            }

            // register the CommandBlock with the monitor
            for (CommandBlock block : command.getBlocks()) {
                fileMonitor.add(block.getSource(), block.getProcessor().getFileFilter(), fileListener);
            }

            fileMonitor.checkAll();

            System.out.print("jcruncher version " + version);
            if (command.interactive) {
                System.out.println(" (mode interative)");
            }
            System.out.println();

            // we force the generation
            command.process(false);

            if (command.interactive) {
                FileMonitor.instance().start();
                // open up standard input
                BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                System.out.print(INTERACTIVE_MESSAGE);
                try {
                    boolean on = true;
                    while (on) {
                        String l = br.readLine();
                        if ("q".equals(l)) {
                            System.exit(-1);
                        } else if ("r".equals(l)) {
                            command.process(false);
                        }
                        System.out.print(INTERACTIVE_MESSAGE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        } catch (Throwable t) {
            System.err.println( t.getMessage());
        }

    }

    private static String[] parseArgsFile(File argsFile) throws IOException {

        List<String> argsList = new ArrayList<String>();

        List<String> lines = Files.readLines(argsFile, Charsets.UTF_8);

        // remove the comment line (starting with #);
        for (String line : lines) {
            if (!line.startsWith("#")) {
                argsList.addAll(Lists.newArrayList(Splitter.on(" ").split(line)));
            }
        }

        return argsList.toArray(new String[0]);
    }

    private static Command parseArgs(String[] args, Command command) {
        if (command == null) {
            command = new Command();
        }

        CommandBlock block = null;
        boolean inBlock = false;
        int idx = 0;
        while (idx < args.length) {
            String arg = args[idx];
            arg = arg.trim();
            // if it is not empty , we process it, otherwise, we pass.
            if (!Strings.isNullOrEmpty(arg)){
                CommandBlock.Type newType = getCommandType(arg);

                // if it is an option (before CommandBlock)
                if (newType == null && !inBlock) {
                    setOption(command, arg);
                }

                // if this is a new block type, save the old, and create the new;
                if (newType != null) {
                    block = new CommandBlock(newType);
                    command.add(block);
                    inBlock = true;
                    idx++;
                } else if (block != null) {
                    int offset = block.setNextArg(args, idx);
                    idx += offset;
                } else {
                    idx++;
                }
            }else{
                idx++;
            }


        }

        return command;
    }

    private static void setOption(Command command, String arg) {
        if ("-i".equals(arg)) {
            command.interactive = true;
        }
    }

    private static CommandBlock.Type getCommandType(String arg) {
        if (LESS.equals(arg)) {
            return CommandBlock.Type.LESS;
        } else if (HBS.equals(arg)) {
            return CommandBlock.Type.HBS;
        } else {
            return null;
        }
    }
}
