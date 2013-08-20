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

import edu.stanford.ejalbert.BrowserLauncher;
import edu.stanford.ejalbert.exception.BrowserLaunchingInitializingException;
import edu.stanford.ejalbert.exception.UnsupportedOperatingSystemException;

/**
 * A WebBrowser implementation that uses BrowserLauncher2. Requires the
 * BrowserLauncher2 jar to be in the classpath and the
 * InformationDelegator.webBrowserClass property set to this classname.
 *
 * @author dietrick
 */
public class BrowserLauncher2WebBrowser extends WebBrowser {

    BrowserLauncher browserLauncher;

    /**
     * Create a web browser that delegates launching to BrowserLauncher2.
     * 
     */
    public BrowserLauncher2WebBrowser() {
        try {
            browserLauncher = new BrowserLauncher();
        } catch (BrowserLaunchingInitializingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (UnsupportedOperatingSystemException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Creates a new web browser process, or points the current one to the url
     * argument.
     * 
     * @param urlString URL
     * 
     */
    public void launch(String urlString) {
        browserLauncher.openURLinBrowser(urlString);
    }

}