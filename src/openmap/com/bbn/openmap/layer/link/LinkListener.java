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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/link/LinkListener.java,v $
// $RCSfile: LinkListener.java,v $
// $Revision: 1.1 $
// $Date: 2003/08/14 22:28:46 $
// $Author: dietrick $
//
// **********************************************************************


package com.bbn.openmap.layer.link;


/*  Java Core  */
import java.awt.event.*;
import java.awt.Container;
import java.util.*;
import java.io.*;
import java.net.*;

/*  OpenMap  */
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PaletteHelper;
import com.bbn.openmap.util.SwingWorker;
import com.bbn.openmap.layer.link.LinkLayer;
import com.bbn.openmap.*;
import com.bbn.openmap.event.*;
import com.bbn.openmap.proj.*;
import com.bbn.openmap.omGraphics.*;
import com.bbn.openmap.omGraphics.grid.*;
import com.bbn.openmap.tools.drawing.DrawingToolRequestor;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PropUtils;

/**
 *  The Link Listener is the object communicating with the link server.
 *  It is launched within its own thread to handle a specific link layer.
 */
public class LinkListener extends Thread implements LinkPropertiesConstants {
    /** The Link to use to talk to the client. */
    protected LinkManager linkManager;
    /** The LinkLayer to use to do the work. */
    protected LinkLayer layer;
    /** The generator to use with LinkGrid objects. */
    protected OMGridGenerator currentGenerator = null;
    /**
     * Used by outsiders to find out if the listener should be started.
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
    public LinkListener(LinkManager linkManager,
                        LinkLayer layer, OMGridGenerator generator) {

        this.linkManager = linkManager;
        this.layer = layer;
        this.currentGenerator = generator;
    }

    /**
     * A method used by outsiders to figure out if the LinkListener is
     * listening to the server.  If false, start() may need to be
     * called to get the listener listening.
     */
    public synchronized boolean isListening() {
	return listening;
    }

    protected synchronized void setListening(boolean value) {
	listening = value;
    }

    /** From the Runnable interface.  The thread starts here... */
    public void run() {
        try {
	    Debug.message("link","*** LinkListener starting up ***");
//             sleep(2000); // give it some time to start
	    setListening(true);
            listen();
	    Debug.message("link","...done listening");
        } catch (java.io.IOException ioe) {
            if (Debug.debugging("link")) {
                Debug.error(ioe.getMessage());
            }
            Debug.output("LinkListener: Server disconnected");
            System.gc();
        }
	setListening(false);
    }

    /**
     * listen is a method that listens to the server
     * and responds to requests that are made.
     *
     * @throws IOException
     */
    public void listen() throws IOException {

	Debug.message("link","LinkListener: Asynchronously listening...");

	ClientLink link = linkManager.getLink(this);

	Debug.message("link", "LinkListener got link...");

	while (true) {
	    Debug.message("link","LinkListener: listening...");
// 	    link.readAndParse(layer.getProjection(), currentGenerator, layer);
	    link.readAndParse(null, currentGenerator, layer);
	    Debug.message("link","LinkListener: received content from server");

	    layer.handleLinkGraphicList(link.getGraphicList());
	    layer.handleLinkActionRequest(link.getActionRequest());
	    layer.handleLinkActionList(link.getActionList());
	}
    }
}
