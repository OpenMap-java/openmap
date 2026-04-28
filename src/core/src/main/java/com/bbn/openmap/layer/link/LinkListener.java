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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/link/LinkListener.java,v $
// $RCSfile: LinkListener.java,v $
// $Revision: 1.4 $
// $Date: 2004/10/14 18:05:56 $
// $Author: dietrick $
//
// **********************************************************************

package com.bbn.openmap.layer.link;

/*  Java Core  */
import java.io.IOException;

import com.bbn.openmap.omGraphics.grid.OMGridGenerator;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.ISwingWorker;
import com.bbn.openmap.util.SwingWorker;

/**
 * The Link Listener is the object listening from input from the link
 * server, asynchronously. It is launched within its own thread to
 * handle a specific link layer.
 */
public class LinkListener extends Thread implements LinkPropertiesConstants {
    /** The Link to use to talk to the client. */
    protected LinkManager linkManager;
    /** The LinkLayer to use to do the work. */
    protected LinkLayer layer;
    /** The generator to use with LinkGrid objects. */
    protected OMGridGenerator currentGenerator = null;
    /**
     * Used by outsiders to find out if the listener should be
     * started.
     */
    protected boolean listening = false;

    /**
     * Default Constructor should not be used.
     */
    private LinkListener() {}

    /**
     * Create child thread that will handle the client.
     * 
     * @param linkManager the LinkManager to communicate over.
     * @param layer the LinkLayer to do the work.
     * @param generator the OMGridGenerator.
     */
    public LinkListener(LinkManager linkManager, LinkLayer layer,
            OMGridGenerator generator) {

        this.linkManager = linkManager;
        this.layer = layer;
        this.currentGenerator = generator;
    }

    /**
     * A method used by outsiders to figure out if the LinkListener is
     * listening to the server. If false, start() may need to be
     * called to get the listener listening.
     */
    public synchronized boolean isListening() {
        return listening;
    }

    protected synchronized void setListening(boolean value) {
        listening = value;
    }

    protected LinkListener getListener() {
        return this;
    }

    /**
     * Use a SwingWorker to launch the listener. Calls start() on the
     * LinkListener from a new thread.
     */
    public void startUp() {
        // Have to use a swing worker so that the calling thread
        // doesn't get hung up on launching the runnable.
        ISwingWorker sw = new SwingWorker() {
            public Object construct() {
                if (Debug.debugging("link")) {
                    Debug.output("LinkListener self-starting...");
                }
                getListener().start();
                return null;
            }
        };
        sw.execute();
    }

    /** From the Runnable interface. The thread starts here... */
    public void run() {
        try {
            Debug.message("link", "*** LinkListener starting up ***");
            setListening(true);
            listen();
            Debug.message("link", "...done listening");
        } catch (java.io.IOException ioe) {
            if (Debug.debugging("link")) {
                Debug.error(ioe.getMessage());
            }
            Debug.message("link", "LinkListener: Server disconnected");
        }
        layer.setListener(null);
    }

    /**
     * listen is a method that listens to the server and responds to
     * requests that are made.
     * 
     * @throws IOException
     */
    public void listen() throws IOException {

        Debug.message("link", "LinkListener: Asynchronously listening...");

        ClientLink link = linkManager.getLink(this);

        Debug.message("link", "LinkListener got link...");

        while (link != null) {
            Debug.message("link", "LinkListener: listening...");
            link.readAndParse(null, currentGenerator, layer);
            Debug.message("link", "LinkListener: received content from server");

            layer.handleLinkGraphicList(link.getGraphicList());
            layer.handleLinkActionRequest(link.getActionRequest());
            layer.handleLinkActionList(link.getActionList());

            link = linkManager.getLink(this);
        }
    }
}