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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/link/LinkGUIRequest.java,v $
// $RCSfile: LinkGUIRequest.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:05:56 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.link;

import java.io.IOException;

/** 
 */
public class LinkGUIRequest {

    String linkStatus = Link.END_TOTAL;

    /** Version Number of request format. */
    protected static float version = Link.LINK_VERSION;

    public LinkGUIRequest(Link link) throws IOException {}

    public String getType() {
        return Link.GUI_REQUEST_HEADER;
    }

    /**
     * After reading the gesture response, this returns the section
     * ending string terminating the gesture section, either
     * Link.END_TOTAL or Link.END_SECTION.
     * 
     * @return either Link.END_TOTAL or Link.END_SECTION.
     */
    public String getLinkStatus() {
        return linkStatus;
    }

    public static void write(String[] args, Link link) throws IOException {

        // Do a check to make sure the arguments are set in key
        // value pairs. If there is a leftover arg, leave it off.
        int normedNumArgs = (args.length / 2) * 2;
        link.dos.writeFloat(version);
        link.dos.writeInt(normedNumArgs);
        for (int i = 0; i < normedNumArgs; i++) {
            link.dos.writeInt(args[i].length());
            link.dos.writeChars(args[i]);
        }

        link.end(Link.END_TOTAL);
    }

    /**
     * Read the link and pull off the gesture, filling in the fields
     * of this object.
     * 
     * @param link the link to read from.
     * @return Link.END_TOTAL or Link.END_SECTION
     */
    public String read(Link link) throws IOException {
        return link.readDelimiter(false);
    }

}