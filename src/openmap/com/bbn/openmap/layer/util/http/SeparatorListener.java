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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/util/http/SeparatorListener.java,v $
// $RCSfile: SeparatorListener.java,v $
// $Revision: 1.1.1.1 $
// $Date: 2003/02/14 21:35:48 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.layer.util.http;

import java.io.Writer;
import java.io.IOException;

/**
 * An HttpRequestListener that writes a HTML Separator ("<HR>")
 * to the client.
 *
 * @author Tom Mitchell
 * @version 1.0, 06/13/97
 */
public class SeparatorListener implements HttpRequestListener {
    public SeparatorListener () {
    }

    /**
     * Ignore the request, just write the separator.
     */
    public void httpRequest (HttpRequestEvent e) throws IOException {
	e.getWriter().write("<HR>");
    }
}
