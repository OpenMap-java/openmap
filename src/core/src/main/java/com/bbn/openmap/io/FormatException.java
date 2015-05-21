// **********************************************************************
// 
// <copyright>
// 
//  BBN Technologies
//  10 Moulton Street
//  Cambridge, MA 02138
//  (617) 873-8000
// 
//  Copyright (C) BBNT Solutions LLC. All rights reserved.
// 
// </copyright>
// **********************************************************************
// 
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/io/FormatException.java,v $
// $RCSfile: FormatException.java,v $
// $Revision: 1.4 $
// $Date: 2004/10/14 18:05:51 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.io;

import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * This class is used for exceptions that result from some format
 * errors of the data when using the BinaryFile.
 */
public class FormatException extends Exception {
    /** nested exception that caused this one */
    final private Throwable rootCause;

    /**
     * Construct a FormatException without a detail message.
     */
    public FormatException() {
        super();
        rootCause = null;
    }

    /**
     * Construct a FormatException with a detail message.
     * 
     * @param s the detail message
     */
    public FormatException(String s) {
        super(s);
        rootCause = null;
    }

    /**
     * Construct a FormatException with a detail message and root
     * cause.
     * 
     * @param s the detail message
     * @param rootCause the root cause (not null)
     */
    public FormatException(String s, Throwable rootCause) {
        super(s + ": " + rootCause.getLocalizedMessage());
        this.rootCause = rootCause;
    }

    /**
     * Returns the exception that caused this one.
     * 
     * @return the root exception, or null if there isn't one
     */
    public Throwable getRootCause() {
        return rootCause;
    }

    /**
     * Prints a backtrace of this exception and the rootCause (if any)
     * to System.err.
     */
    public void printStackTrace() {
        super.printStackTrace();
        if (rootCause != null) {
            System.err.println("With Root Cause:");
            rootCause.printStackTrace();
        }
    }

    /**
     * Prints a backtrace of this exception and the rootCause (if any)
     * to a stream.
     * 
     * @param ps the stream to print to
     */
    public void printStackTrace(PrintStream ps) {
        super.printStackTrace(ps);
        if (rootCause != null) {
            ps.println("With Root Cause:");
            rootCause.printStackTrace(ps);
        }
    }

    /**
     * Prints a backtrace of this exception and the rootCause (if any)
     * to a writer.
     * 
     * @param pw the writer to print to
     */
    public void printStackTrace(PrintWriter pw) {
        super.printStackTrace(pw);
        if (rootCause != null) {
            pw.println("With Root Cause:");
            rootCause.printStackTrace(pw);
        }
    }
}