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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/util/http/ReverseListener.java,v $
// $RCSfile: ReverseListener.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:06:07 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.util.http;

import java.io.IOException;

/**
 * An HttpRequestListener that reverses its input.
 * 
 * @author Tom Mitchell
 * @version 1.0, 06/13/97
 */
public class ReverseListener implements HttpRequestListener {
    public ReverseListener() {}

    /**
     * Reverse the input and send it back to the client.
     */
    public void httpRequest(HttpRequestEvent e) throws IOException {
        // reverse the input
        int len = e.getRequest().length();
        StringBuffer revline = new StringBuffer(len);
        for (int i = len - 1; i >= 0; i--)
            revline.insert(len - 1 - i, e.getRequest().charAt(i));

        // and write out the reversed request
        e.getWriter().write(revline.toString());
    }
}