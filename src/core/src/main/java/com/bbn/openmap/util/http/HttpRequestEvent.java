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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/util/http/HttpRequestEvent.java,v $
// $RCSfile: HttpRequestEvent.java,v $
// $Revision: 1.4 $
// $Date: 2004/10/14 18:06:07 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.util.http;

import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;

/**
 * An event corresponding to a single HTTP request ("GET" command).
 * 
 * @author Tom Mitchell
 * @version 1.0, 06/13/97
 */
public class HttpRequestEvent extends java.util.EventObject {

    protected String request;
    protected OutputStream output;
    protected Writer writer;

    /**
     * Creates an http request event.
     * 
     * @param source the source object
     * @param request the parsed target of the "GET" command
     * @param out the http client output stream
     */
    public HttpRequestEvent(Object source, String request, OutputStream out) {
        super(source);
        this.request = request;
        this.output = out;
    }

    /**
     * Gets the http request, which is the target of the "GET"
     * command.
     * <p>
     * For the URL
     * <code>http://www.bbn.com/openmap/docs/index.html</code>
     * <p>
     * the request string is <code>/openmap/docs/index.html</code>
     * 
     * @return the request string
     */
    public String getRequest() {
        return request;
    }

    /**
     * Gets the output stream connected to the http client.
     * 
     * @return the OutputStream associated with the http client.
     */
    public OutputStream getOutputStream() {
        return output;
    }

    /**
     * Get a Writer associated with the output stream connected to the
     * http client. If a writer hasn't been created, then one will be.
     * 
     * @return the Writer associated with the http client.
     * @see java.io.Writer
     */
    public Writer getWriter() {
        if (writer == null) {
            writer = new StringWriter();
        }
        return writer;
    }

    /**
     * Find out if anyone used the writer. If it has been used, then,
     * as a HttpServer, you can assume that the listeners were all
     * contributing text into the writer, and you now need to take the
     * Writer buffer, create a string, and write the contents as
     * output.
     * 
     * @return true if the writer has been created.
     */
    public boolean isWriterUsed() {
        return (writer != null);
    }
}