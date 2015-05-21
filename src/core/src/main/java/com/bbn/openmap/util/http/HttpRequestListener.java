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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/util/http/HttpRequestListener.java,v $
// $RCSfile: HttpRequestListener.java,v $
// $Revision: 1.2 $
// $Date: 2004/10/14 18:06:07 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.util.http;

/**
 * The listener interface for receiving http requests.
 * 
 * @author Tom Mitchell
 * @version 1.0, 06/13/97
 */
public interface HttpRequestListener extends java.util.EventListener {

    /**
     * Invoked when an http request is received.
     */
    public void httpRequest(HttpRequestEvent e) throws java.io.IOException;
}