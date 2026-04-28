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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/util/WebBrowser.java,v $
// $RCSfile: WebBrowser.java,v $
// $Revision: 1.7 $
// $Date: 2005/08/09 18:37:41 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import com.bbn.openmap.Environment;
import com.bbn.openmap.event.InfoDisplayEvent;
import com.bbn.openmap.event.InfoDisplayListener;

/**
 * WebBrower - handles the WebBrowser process on behalf of OM. This class should
 * be extended and implemented fully to handle browser requests as you like. You
 * can use BrowserLauncher2 if you like.
 */
public abstract class WebBrowser {

    InfoDisplayListener info = null;

    /**
     * Write temporary file to temporary directory, and generate URL.
     * 
     * @param text text String
     * @return String file URL
     */
    protected String writeFileAndGenerateURL(String text) {

        File tmpFile = null;

        try {
            tmpFile = File.createTempFile(Environment.OpenMapPrefix, ".html", new File(Environment.get(Environment.TmpDir)));

            tmpFile.deleteOnExit(); // get rid of it when the user
                                    // quits.

            FileOutputStream fs = new FileOutputStream(tmpFile);
            PrintWriter out = new PrintWriter((OutputStream) fs);
            out.println(text);
            fs.close(); // close the streams

            String urlString = tmpFile.toURI().toURL().toString();
            Debug.output("WebBrowser: created " + urlString);
            return urlString;

        } catch (SecurityException se) {
            Debug.error("WebBrowser.writeAndGenerateURL: " + se);
        } catch (IOException ioe) {
            Debug.error("WebBrowser.writeAndGenerateURL: " + ioe);
        }

        postErrorMessage("Cannot write to temp file:"
                + (tmpFile != null ? tmpFile.getAbsolutePath() : "unknown"));

        return null;
    }

    public void setInfoDelegator(InfoDisplayListener info) {
        this.info = info;
    }

    /**
     * Creates a new web browser process, or points the current one to the url
     * argument.
     * 
     * @param urlString URL
     * 
     */
    public abstract void launch(String urlString);

    /**
     * Writes out temporary text file, and creates a new web browser process or
     * points the current one at the file.
     * 
     * @param text String
     * 
     */
    public void writeAndLaunch(String text) {
        launch(writeFileAndGenerateURL(text));
    }

    private void postErrorMessage(String message) {
        info.requestMessage(new InfoDisplayEvent(this, message));
    }

}