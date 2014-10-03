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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/graphicLoader/netmap/NetMapEvent.java,v $
// $RCSfile: NetMapEvent.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:05:47 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.graphicLoader.netmap;

import java.util.Properties;

/**
 * Event that represents a status update from a NetMap server. The
 * NetMap output is provided in lines that contain a bunch of fields.
 * The line is parsed, with each token interpreted by the NetMapReader
 * and put into a Properties object. The NetMapEvent can be examined
 * to get fields - the field names are listed in the NetMapConstants
 * interface, you can query for a field and use it if it's there.
 */
public class NetMapEvent {

    protected Properties properties;
    protected Object source;

    public NetMapEvent(Object source, Properties eventProps) {
        this.source = source;
        this.properties = eventProps;
    }

    public Properties getProperties() {
        return properties;
    }

    public Object getSource() {
        return source;
    }
}