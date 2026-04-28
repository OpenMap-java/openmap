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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/io/URLInputReader.java,v $
// $RCSfile: URLInputReader.java,v $
// $Revision: 1.4 $
// $Date: 2007/06/21 21:39:03 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.io;

import java.io.IOException;
import java.net.URL;

import com.bbn.openmap.util.Debug;

/**
 * An InputReader to handle files at a URL.
 */
public class URLInputReader extends StreamInputReader {

    /** Where to go to hook up with a resource. */
    protected URL inputURL = null;

    /**
     * Construct a URLInputReader from a URL.
     */
    public URLInputReader(java.net.URL url) throws IOException {
        if (Debug.debugging("binaryfile")) {
            Debug.output("URLInputReader created from URL ");
        }
        inputURL = url;
        reopen();
        name = url.getProtocol() + "://" + url.getHost() + url.getFile();
    }

    /**
     * Reset the InputStream to the beginning, by closing the current
     * connection and reopening it.
     */
    public void reopen() throws IOException {
        super.reopen();
        inputStream = inputURL.openStream();
    }
}