// **********************************************************************
// 
// <copyright>
// 
//  BBN Technologies, a Verizon Company
//  10 Moulton Street
//  Cambridge, MA 02138
//  (617) 873-8000
// 
//  Copyright (C) BBNT Solutions LLC. All rights reserved.
// 
// </copyright>
// **********************************************************************
// 
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/util/http/FileListener.java,v $
// $RCSfile: FileListener.java,v $
// $Revision: 1.1.1.1 $
// $Date: 2003/02/14 21:35:48 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.layer.util.http;

import java.io.*;

import com.bbn.openmap.util.Debug;

/**
 * An HttpRequestListener that sends a file back.
 *
 * @author Tom Mitchell
 * @version 1.0, 06/13/97
 */
public class FileListener implements HttpRequestListener {

    public FileListener () {
    }

    /**
     * Reverse the input and send it back to the client.
     */
    public void httpRequest(HttpRequestEvent e) throws IOException {
	String filename = e.getRequest().substring(1);
	if (Debug.debugging("http")) {
	    Debug.output("FileListener: Looking for file " + filename);
	}

	FileInputStream requestedfile;
	try {
	    requestedfile = new FileInputStream(filename);
	} catch (java.io.FileNotFoundException exception) {
	    Debug.error("FileListener: Unable to find file " + filename);
	    return;
	    // Need a return value here
	}

	// Read in the file's bytes.  This doesn't seem super efficient
	int bytes = requestedfile.available();
	byte [] b = new byte[bytes];
	int bytes_read = requestedfile.read(b);
	if (bytes_read != bytes) {
	    Debug.error("FileListener: Did not read the correct number of bytes for " + 
			filename);
	}
	// and write out the raw bytes
	e.getWriter().write(new String(b));
    }
}
