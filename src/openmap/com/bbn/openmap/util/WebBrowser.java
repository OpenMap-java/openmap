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
import java.net.URL;

import com.bbn.openmap.Environment;
import com.bbn.openmap.event.InfoDisplayEvent;
import com.bbn.openmap.event.InfoDisplayListener;

/** WebBrower - handles the WebBrowser process on behalf of OM. */
public class WebBrowser {

    Process proc = null;
    InfoDisplayListener info = null;

    boolean oldWay = false;

    /**
     * Create a webbrowser.
     *  
     */
    public WebBrowser() {}

    /**
     * Get the launch cmd.
     * 
     * @param url URL to show
     * @return String
     *  
     */
    protected String generateLaunchCmd(String url) {
        //HACK, needs to be OS/web-browser specific
        return Environment.get(Environment.WebBrowser) + " " + url;
    }

    /**
     * Write temporary file to temporary directory, and generate URL.
     * 
     * @param text text String
     * @return String file URL
     */
    protected String writeFileAndGenerateURL(String text) {

        File tmpFile = null;

        try {
            tmpFile = File.createTempFile(Environment.OpenMapPrefix,
                    ".html",
                    new File(Environment.get(Environment.TmpDir)));

            tmpFile.deleteOnExit(); // get rid of it when the user
                                    // quits.

            FileOutputStream fs = new FileOutputStream(tmpFile);
            PrintWriter out = new PrintWriter((OutputStream) fs);
            out.println(text);
            out.close(); // close the streams

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

    /**
     * Points a web browser that's already running where to go next.
     * 
     * @param url URL to go
     */
    protected void sendTo(String url) {

        if (!oldWay) {
            try {
                edu.stanford.ejalbert.BrowserLauncher.openURL(url);
            } catch (IOException ioe) {
                Debug.error("WebBrowser caught IOException loading webpage ("
                        + url + ")\n" + ioe.getMessage());
            }
            return;

        } else {

            //Should work for Unix or Windows.

            String cmd;
            String arch = Environment.get("os.arch");
//            String osname = Environment.get("os.name");

            if (Environment.isApplet()) {
                try {
                    java.applet.Applet applet = Environment.getApplet();
                    java.applet.AppletContext ac = applet.getAppletContext();
                    ac.showDocument(new URL(url), "otherFrame");
                } catch (java.net.MalformedURLException e) {
                    System.err.println("WebBrowser.sendTo: " + e);
                    postErrorMessage("Cannot show document: "
                            + Environment.get("line.separator") + e);
                }
                return;
            }

            if (arch.equals("x86")) {
                // Windows HACK
                cmd = Environment.get(Environment.WebBrowser) + " " + url;
            } else {
                // Assume Unix HACK
                cmd = Environment.get(Environment.WebBrowser)
                        + " -remote OpenURL(" + url + ")";
            }

            try {
                Debug.message("www", "WebBrowser.sendTo: " + cmd);
                Runtime.getRuntime().exec(cmd).waitFor();
            } catch (IOException e) {
                System.err.println("WebBrowser.sendTo: " + e);
                postErrorMessage("Cannot start WebBrowser: "
                        + Environment.get("line.separator") + e);
            } catch (InterruptedException f) {
                System.err.println("WebBrowser.sendTo: interrupted");
            }
        }
    }

    public void setInfoDelegator(InfoDisplayListener info) {
        this.info = info;
    }

    /**
     * Creates a new web browser process, or points the current one to
     * the url argument.
     * 
     * @param urlString URL
     *  
     */
    public void launch(String urlString) {
        String launchCmd = null;

        // launch the program with the url as an argument
        if (oldWay && (proc == null) && !(Environment.isApplet())) {
            try {
                launchCmd = generateLaunchCmd(urlString);
                Debug.message("www", "WebBrowser.launch: " + launchCmd);
                proc = Runtime.getRuntime().exec(launchCmd);
            } catch (IOException e) {
                System.err.println("WebBrowser.launch: " + e);
                postErrorMessage("Cannot start WebBrowser: "
                        + Environment.get("line.separator") + "\"" + launchCmd
                        + "\"");
            }
        }

        // send the new url to the web browser that's already running
        else {
            sendTo(urlString);
        }
    }

    private void postErrorMessage(String message) {
        info.requestMessage(new InfoDisplayEvent(this, message));
    }

    /**
     * Writes out temporary text file, and creates a new web browser
     * process or points the current one at the file.
     * 
     * @param text String
     *  
     */
    public void writeAndLaunch(String text) {
        String cmd = null;

        // launch the program with the url as an argument
        if (oldWay && (proc == null) && !(Environment.isApplet())) {
            try {
                cmd = generateLaunchCmd(writeFileAndGenerateURL(text));
                proc = Runtime.getRuntime().exec(cmd);
            } catch (IOException e) {
                System.err.println("WebBrowser.writeAndLaunch: " + e);
                postErrorMessage("Cannot start WebBrowser: "
                        + Environment.get("line.separator") + "\"" + cmd + "\"");
            }
        }

        // send the new url to the web browser that's already running
        else
            sendTo(writeFileAndGenerateURL(text));
    }

    /**
     * Calls the Process function of the same name to determine if the
     * process has finished, and what its exit value was.
     * <p>
     * If it is finished, then it removes the temporary files and
     * nullifies itself.
     */
    public void exitValue() {
        if (proc == null)
            return;

        try {
            proc.exitValue();
            Debug.message("www", "WebBrowser.exitValue: WebBrowser died");
            proc = null; // go down
        } catch (IllegalStateException e) {
        } catch (IllegalThreadStateException f) {
        }
    }

    public static void main(String[] argv) {
        if (argv.length == 0) {
            System.out.println("Give WebBrowser a URL, and it'll launch it.");
            System.exit(0);
        }

        String url = argv[0];

        try {
            edu.stanford.ejalbert.BrowserLauncher.openURL(url);
        } catch (IOException ioe) {
            Debug.error("WebBrowser caught IOException loading webpage ("
                    + url + ")\n" + ioe.getMessage());
        }

    }

}