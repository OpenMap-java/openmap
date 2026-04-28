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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/util/http/SeparatorListener.java,v $
// $Revision: 1.5 $ $Date: 2004/10/14 18:06:07 $ $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.util.http;

import java.io.IOException;

/**
 * An HttpRequestListener that writes an HTML Separator ("&lt;HR&gt;")
 * to the client.
 */
public class SeparatorListener implements HttpRequestListener {
    public SeparatorListener() {}

    /**
     * Ignore the request, just write the separator.
     */
    public void httpRequest(HttpRequestEvent e) throws IOException {
        e.getWriter().write("<HR>");
    }
}