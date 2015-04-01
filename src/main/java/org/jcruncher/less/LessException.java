/*
 * Licensed under the Apache License, Version 2.0 (the "License"): http://www.apache.org/licenses/LICENSE-2.0
 */
package org.jcruncher.less;

import java.util.List;


/**
 * From https://github.com/asual/lesscss-engine
 *
 * @author Rostislav Hristov
 * @author Uriah Carpenter
 */
public class LessException extends Exception {

    private static final long serialVersionUID = 662552833197468936L;

    private String type;
    private String filename;
    private int line;
    private int column;
    private List<String> extract;

    public LessException() {
        super();
    }

    public LessException(String message) {
        super(message);
    }

    public LessException(String message, Throwable e) {
        super(message, e);
    }

    public LessException(String message, String errorType, String filename,
                         int line, int column, List<String> extract) {
        super(message);
        this.type = errorType != null ? errorType : "LESS Error";
        this.filename = filename;
        this.line = line;
        this.column = column;
        this.extract = extract;
    }

    public LessException(Throwable e) {
        super(e);
    }

    @Override
    public String getMessage() {
        if (type != null) {
            String msg = String.format("%s: %s (line %s, column %s)", type,
                    super.getMessage(), line, column);
            if (!(extract == null) && !extract.isEmpty()) {
                msg += " near";
                for (String l : extract) {
                    msg += "\n" + l;
                }
            }
            return msg;
        }

        return super.getMessage();
    }

    /**
     * Type of error as reported by less.js
     */
    public String getType() {
        return type;
    }

    /**
     * Filename that error occured in as reported by less.js
     */
    public String getFilename() {
        return filename;
    }

    /**
     * Line number where error occurred as reported by less.js or -1 if unknown.
     */
    public int getLine() {
        return line;
    }

    /**
     * Column number where error occurred as reported by less.js or -1 if
     * unknown.
     */
    public int getColumn() {
        return column;
    }

    /**
     * Lines around error as reported by less.js
     */
    public List<String> getExtract() {
        return extract;
    }

}