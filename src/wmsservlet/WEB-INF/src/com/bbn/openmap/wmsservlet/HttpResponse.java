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
// $Source: /cvs/distapps/openmap/src/wmsservlet/WEB-INF/src/com/bbn/openmap/wmsservlet/HttpResponse.java,v $
// $RCSfile: HttpResponse.java,v $
// $Revision: 1.2 $
// $Date: 2007/01/25 22:11:42 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.wmsservlet;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import javax.servlet.http.HttpServletResponse;
import com.bbn.openmap.layer.util.http.IHttpResponse;

/**
 */
public class HttpResponse implements IHttpResponse {

    protected HttpServletResponse httpResponse;

    /**
     * Initialize the input <code>Reader</code> and output <code>Writer</code>
     * and start the connection thread.
     * 
     * @param client_socket
     *            the client's socket
     * @param server
     *            the server object
     */
    public HttpResponse(HttpServletResponse response) {
        this.httpResponse = response;
    }

    /**
     * Write a String response to the OutputStream.
     * 
     * @param out
     *            the OutputStream of the response.
     * @param contentType
     *            the content type of the response.
     * @param response
     *            the string containing the response.
     */
    public void writeHttpResponse(String contentType, String response) throws IOException {
        httpResponse.setContentType(contentType);
        OutputStreamWriter osw = new OutputStreamWriter(httpResponse.getOutputStream());
        osw.write(response);
        osw.flush();
    }

    /**
     * Write a byte[] response to the OutputStream.
     * 
     * @param out
     *            the OutputStream of the response.
     * @param contentType
     *            the content type of the response.
     * @param response
     *            the byte array containing the response.
     */
    public void writeHttpResponse(String contentType, byte[] response) throws IOException {
        httpResponse.setContentType(contentType);
        OutputStream out = httpResponse.getOutputStream();
        out.write(response, 0, response.length);
        out.flush();
    }

}
