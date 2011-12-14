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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/util/http/SieveListener.java,v $
// $RCSfile: SieveListener.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:06:07 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.util.http;

import java.io.IOException;

/**
 * An HttpRequestListener that returns the request to the client.
 * 
 * @author Tom Mitchell
 * @version 1.0, 06/13/97
 */
public class SieveListener implements HttpRequestListener {
    public SieveListener() {}

    /**
     * Just write the request out to the client.
     */
    public void httpRequest(HttpRequestEvent e) throws IOException {
        e.getWriter().write(e.getRequest());
    }
}