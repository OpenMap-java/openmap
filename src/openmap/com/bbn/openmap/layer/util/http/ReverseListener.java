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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/util/http/ReverseListener.java,v $
// $RCSfile: ReverseListener.java,v $
// $Revision: 1.1.1.1 $
// $Date: 2003/02/14 21:35:48 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.layer.util.http;

import java.io.Writer;
import java.io.IOException;


/**
 * An HttpRequestListener that reverses its input.
 *
 * @author Tom Mitchell
 * @version 1.0, 06/13/97
 */
public class ReverseListener implements HttpRequestListener {
    public ReverseListener() {
    }

    /**
     * Reverse the input and send it back to the client.
     */
    public void httpRequest(HttpRequestEvent e) throws IOException {
	// reverse the input
	int len = e.getRequest().length();
	StringBuffer revline = new StringBuffer(len);
	for(int i = len-1; i >= 0; i--) 
	    revline.insert(len-1-i, e.getRequest().charAt(i));

	// and write out the reversed request
	e.getWriter().write(revline.toString());
    }
}
