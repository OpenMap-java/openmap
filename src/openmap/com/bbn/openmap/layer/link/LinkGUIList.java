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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/link/LinkGUIList.java,v $
// $RCSfile: LinkGUIList.java,v $
// $Revision: 1.1.1.1 $
// $Date: 2003/02/14 21:35:48 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.layer.link;

import com.bbn.openmap.omGraphics.*;
import com.bbn.openmap.util.Debug;

import java.awt.Color;
import java.io.*;

import java.awt.Color;
import javax.swing.ImageIcon;
import java.awt.Image;
import java.awt.image.PixelGrabber;

import javax.swing.JComponent;

/**
 */
public class LinkGUIList {

    /** Link used for the transmission/reception of widgets. */
    Link link = null;
    /** GUI widget list received. */
    JComponent widgets = null;
    /** The terminator of the graphics section when receiving graphics. */
    String linkStatus = Link.END_TOTAL;
    /** Version Number of request format. */
    protected static float version = Link.LINK_VERSION;


    /** Write a gui section to the link. */
    public LinkGUIList(Link link) throws IOException {
	this.link = link;
	link.start(Link.GUI_HEADER);	
	link.dos.writeFloat(version);
    }

    /**
     */
    public JComponent getGUI(){
	return widgets;
    }

    /** 
     * After reading the graphics response, this returns the section
     * ending string terminating the graphics section, either
     * Link.END_TOTAL or Link.END_SECTION.
     * 
     * @return either Link.END_TOTAL or Link.END_SECTION. 
     */
    public String getLinkStatus(){
	return linkStatus;
    }

    /**  
     * The server method that needs to be called at the end of sending
     * a gui response.  This will tell the link what type of
     * teminator to put on the end of the graphics response section,
     * and also tell the link to fluxh the output stream..
     *
     * @param endType use Link.END_SECTION if you want to add more
     * types of response sections.  Use Link.END_TOTAL at the end of
     * the total transmission.
     * @throws IOException 
     */
    public void end(String endType) throws IOException {
	link.end(endType);
    }

    /**
     * @throws IOException
     * @throws EOFException
     */
    protected String readWidgets()
	throws IOException, EOFException {

	JComponent widget;
	long startTime = System.currentTimeMillis();
	String header = null;
	int widgetType;
	boolean moreData = true;

	float ver = link.dis.readFloat();
	Debug.message("link", "LinkGUIList: reading graphics:");

	while (true){
	    widget = null;
	    // Just consume the header, don't create a useless
	    // string object.
	    header = link.readDelimiter(false);
	  
	    if (header == Link.END_TOTAL || header == Link.END_SECTION){
		
		long endTime = System.currentTimeMillis();
		Debug.message("link", "LinkGUIList: received bytes in "
			      + (float)(endTime - startTime)/1000.0f + 
			      " seconds");
		
		return header;
	    }
	    
	    widgetType = link.dis.readInt();
	    
	    switch (widgetType){

	    default:
		System.err.println("LinkGUIList: received unknown graphic type.");
	    }
	}
    }
}
