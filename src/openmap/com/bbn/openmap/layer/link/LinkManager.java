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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/link/LinkManager.java,v $
// $RCSfile: LinkManager.java,v $
// $Revision: 1.6 $
// $Date: 2004/10/14 18:05:56 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.link;

import java.io.IOException;
import java.net.Socket;

import com.bbn.openmap.util.Debug;

/**
 * The LinkManager..
 */
public class LinkManager {

    protected String host;
    protected int port;
    protected boolean obeyCommandToExit;

    /**
     * volatile because we want internal methods to get the message
     * that a link was nulled out.
     */
    protected volatile ClientLink link;

    /** Constructor. */
    protected LinkManager() {}

    /** Constructor. */
    public LinkManager(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void setObeyCommandToExit(boolean value) {
        obeyCommandToExit = value;
    }

    public boolean getObeyCommandToExit() {
        return obeyCommandToExit;
    }

    /**
     * This should be the only method a multi-threaded object uses to
     * gain use of the thread, i.e., on the client side where a GUI
     * can start a lot of requests. If the link was not able to be
     * retained for the requestor, then null will be returned. Null
     * should be tested for by the callers, so that they can handle
     * the rejection properly.
     * 
     * @param waitForLock if true, the caller will block in this
     *        method until the link has been locked for the caller. If
     *        false, a null will be returned if the lock on the link
     *        couldn't be set for the caller's use.
     * @return a link if the link is locked for the caller's use, null
     *         if the link is not available.
     */
    public ClientLink getLink(boolean waitForLock) throws java.io.IOException {

        // NOTE: This should be the only place that the link
        // object gets assigned. Otherwise, the layer can end up
        // using two different links via different threads.
        if (link == null) {
            synchronized (this) {
                if (link == null) {
                    link = getLink();
                    link.setObeyCommandToExit(obeyCommandToExit);
                }
            }
        }

        try {
            while (!link.setLocked(true)) {

                // This handles the case where we don't want to wait
                // for the link to become available.
                if (!waitForLock) {
                    return null;
                }

                // We will wait here for the link to not be in use.
                // Catch a link == null in case the link was shut down
                // in finLink() from another thread. IF we didn't
                // catch the lock, we stay in the loop.
                try {
                    Thread.sleep(300);
                } catch (java.lang.InterruptedException ie) {
                }
            }
        } catch (NullPointerException npe) {
            // since probably means link is null, so just return null
            // in case some other thread tries to do something
            // tricky..
            return null;
        }

        return link;
    }

    /**
     * Called for a LayerListener that will not write to the Link,
     * only read from it. Doesn't effect the lock.
     * 
     * @return a link if the link is locked for the caller's use, null
     *         if the link is not available.
     */
    protected ClientLink getLink(LinkListener ll) throws java.io.IOException {

        // NOTE: This should be the only place that the link
        // object gets assigned. Otherwise, the layer can end up
        // using two different links via different threads.
        if (link == null) {
            synchronized (this) {
                if (link == null) {
                    link = getLink();
                    link.setObeyCommandToExit(obeyCommandToExit);
                }
            }
        }

        return link;
    }

    /**
     * Get the ClientLink however it is appropriate for this
     * LinkManager. In this case, the LinkManager will just use the
     * host and port assigned.
     */
    protected ClientLink getLink() throws java.io.IOException {

        ClientLink tmplink = null;
        try {
            if (Debug.debugging("link")) {
                Debug.output("LinkManager.getLink(): establishing link to "
                        + host + " on port " + port);
            }
            Socket socket = new Socket(host, port);
            tmplink = new ClientLink(socket);
        } catch (java.net.UnknownHostException uhe) {
            Debug.error("LinkLayer: error trying to contact host:" + host);
            tmplink = null;
            throw new java.io.IOException("No Contact with host:" + host
                    + " on port:" + port);
        }

        return tmplink;
    }

    /**
     * When a getLink() is called, and the link is reserved for that
     * caller, finLink() MUST be called to release the link for
     * others. If it is not called, no one else will be able to use it.
     */
    public void finLink() throws IOException {
        if (link.isCloseLink()) {
            Debug.message("link", "LinkManager.finLink: closing Link");
            link.close();
            link = null;
        } else {
            Debug.message("link", "LinkManager.finLink: releasing lock on Link");
            link.setLocked(false);
        }
    }

    /**
     * Set the link to null.
     */
    public void resetLink() {
        if (link != null) {
            try {
                link.cleanUp();
                link.close();
            } catch (IOException ioe) {
                // Nice try...
            }
        }
        link = null;
    }
}