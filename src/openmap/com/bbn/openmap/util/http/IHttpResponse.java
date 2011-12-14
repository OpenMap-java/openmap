package com.bbn.openmap.util.http;

import java.io.IOException;

/**
 */
public interface IHttpResponse {

    /**
     * Write a String response to the OutputStream.
     * 
     * @param contentType the content type of the response.
     * @param response the string containing the response.
     */
    public void writeHttpResponse(String contentType, String response)
            throws IOException;

    /**
     * Write a byte[] response to the OutputStream.
     * 
     * @param contentType the content type of the response.
     * @param response the byte array containing the response.
     */
    public void writeHttpResponse(String contentType, byte[] response)
            throws IOException;
}
