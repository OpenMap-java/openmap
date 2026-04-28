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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/util/http/HttpConnection.java,v $
// $RCSfile: HttpConnection.java,v $
// $Revision: 1.8 $
// $Date: 2007/01/25 22:11:41 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.util.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;
import java.util.StringTokenizer;

import com.bbn.openmap.util.Debug;

/**
 * HttpConnection handles the communication with an HTTP client in its
 * own thread. An instance of this class is created by the
 * <code>HttpServer</code> each time a connection is made. The
 * instance exists only long enough to fulfill the request, then dies.
 * 
 * @author Tom Mitchell
 * @version 1.1, 06/17/97
 * @see HttpServer
 */
public class HttpConnection extends Thread {

    protected HttpServer server;
    protected Socket client;
    protected BufferedReader in;
    protected OutputStream out;
    protected boolean isConnectionOpen = true;

    public final static String CONTENT_JPEG = "image/jpeg";
    public final static String CONTENT_GIF = "image/gif";
    public final static String CONTENT_PPM = "image/ppm";
    public final static String CONTENT_PNG = "image/png";
    public final static String CONTENT_TIFF = "image/tiff";
    public final static String CONTENT_GEOTIFF = "image/geotiff";
    public final static String CONTENT_WBMP = "image/wbmp";
    public final static String CONTENT_SVG = "image/svg";
    public final static String CONTENT_HTML = "text/html";
    public final static String CONTENT_MOV = "video/quicktime";
    public final static String CONTENT_PLAIN = "text/plain";
    public final static String CONTENT_XML = "text/xml";
    public final static String CONTENT_JSON = "application/json";

    /**
     * Initialize the input <code>Reader</code> and output
     * <code>Writer</code> and start the connection thread.
     * 
     * @param client_socket the client's socket
     * @param server the server object
     */
    public HttpConnection(Socket client_socket, HttpServer server) {
        client = client_socket;
        this.server = server;
        InputStreamReader isr;
//        OutputStreamWriter osr;

        try {
            isr = new InputStreamReader(client.getInputStream());
            in = new BufferedReader(isr);
            out = client.getOutputStream();
//              osr = new OutputStreamWriter(client.getOutputStream());
//              out = new BufferedWriter(osr);
        } catch (IOException e) {
            try {
                close();
            } catch (IOException e2) {
            }
            System.err.println("Exception while getting socket streams: " + e);
            return;
        }
    }

    public static String[] getAllContentTypes() {
        return new String[] { CONTENT_JPEG, CONTENT_GIF, CONTENT_HTML,
                CONTENT_MOV, CONTENT_PLAIN, CONTENT_XML, CONTENT_PPM, 
                CONTENT_PNG, CONTENT_SVG, CONTENT_GEOTIFF, CONTENT_TIFF,
                CONTENT_WBMP};
    }

    /**
     * The running thread simply reads all the lines of input and
     * hands each line off to be parsed.
     */
    public void run() {
        String line;

        try {
            while (isConnectionOpen) {
                // read in a line
                line = in.readLine();
                if (line == null)
                    break;
                processLine(line);
            }
        } catch (IOException e) {
        } finally {
            try {
                close();
            } catch (IOException e2) {
            }
        }
        Debug.message("httpconnection", "Connection closed. Exiting thread");
    }

    /**
     * Processes a line of an HTTP request. The only interesting line
     * we really look at is a GET command, which starts with "GET".
     * 
     * @param line one line of an HTTP request
     */
    protected void processLine(String line) throws IOException {
        if (Debug.debugging("httpconnection")) {
            Debug.output("HttpConnection | processLine -- Processing command "
                    + line);
        }

        if (line.startsWith("GET")) {
            processGetCommand(line);
        } else if (line.startsWith("POST")) {
            Debug.message("httpconnection", "handling POST");
            handlePost();
        }
    }

    protected void handlePost() throws IOException {
        Debug.message("httpconnection", "HttpConnection | handlePost");
        String line;
        int contentLength = 0;
        try {
            while (isConnectionOpen) {
                // read in a line
                line = in.readLine();
                Debug.message("httpconnection", line);

                if (line == null)
                    break;

                String lineupp = line.toUpperCase();
                if (lineupp.startsWith("CONTENT-LENGTH")) {
                    contentLength = readContentLength(line);
                    Debug.message("httpconnection",
                            "HttpConnection -- Contentlength = "
                                    + contentLength);
                }

                if (line.length() == 0) {
                    readContent(contentLength);
                }
            }
        } catch (IOException e) {
        } finally {
            try {
                close();
            } catch (IOException e2) {
            }
        }
    }

    protected int readContentLength(String line) {
        // Find the content length and read it
        StringTokenizer tokenizer = new StringTokenizer(line, ":");
        // this is first token "Content-length"
        tokenizer.nextToken();
        // Actual length after ':' "Content-length: xxx"
        String strLength = tokenizer.nextToken();
        int length = Integer.parseInt(strLength.trim());
        return length;
    }

    // check if it is an environment key - value pair
    // ASSUME actual content won't have ':'
    protected boolean isEnvarLine(String line) {
        if (line.indexOf(":") != -1) {
            return true;
        }
        return false;
    }

    protected void readContent(int length) throws IOException {

        char[] c_content = new char[length];
        int nread = 0;
        int remaining = length;
        try {
            while (nread < length) {
                //nread += in.read(c_content, nread, length);
                int tmp = in.read(c_content, nread, remaining);
                nread += tmp;
                remaining -= tmp;
                Debug.message("httpconnection", "Length of content read "
                        + nread + " length=" + length + " remaining="
                        + remaining);
            }

        } catch (java.io.IOException ioe) {
            Debug.error("HttpConnection | readContent -- Exception while reading content(key-values from POST) "
                    + ioe.getMessage());
        }

        String content = new String(c_content);
        content = content.trim();

        if (Debug.debugging("httpconnection")) {
            Debug.output("HttpConection showing Content :-- " + content);
        }

        //processGetCommand(content);

        HttpRequestEvent event = server.fireHttpRequestEvent(content, out);

        // Check to see if the Writer in the Event was used. If it
        // was, then the listeners contributed concatenated text, and
        // the result needs to be retrieved and sent back.
        if (event.isWriterUsed()) {
            Writer writer = event.getWriter();
            String result = writer.toString();
            writeHttpResponse(out, null, result);
        }

        // else - assume that the binary response listeners took care
        // of writing things back on their own,

        out.flush();
        close();
    }

    /**
     * Process a "GET" HTTP command. The leading "GET " and the
     * trailing HTTP version information are stripped off and a
     * HttpRequestEvent is fired via the HttpServer.
     * 
     * @param cmd a "GET" HTTP command
     */
    protected void processGetCommand(String cmd) throws IOException {
        // Command looks like: "GET /thisURL HTTP/1.0"
        String location = cmd.substring(4); // remove the "GET "
        int locationEnd = location.indexOf(" "); // end at first space
        location = location.substring(0, locationEnd);

        // Figure out what type of file to figure Content-type
        // string contents
        String contentType;
        if (location.endsWith(".gif") || location.endsWith(".GIF")) {

            contentType = CONTENT_GIF;

        } else if (location.endsWith(".htm") || location.endsWith(".html")
                || location.endsWith(".HTM") || location.endsWith(".HTML")) {

            contentType = CONTENT_HTML;

        } else if (location.endsWith(".jpg") || location.endsWith(".JPG")
                || location.endsWith(".jpeg") || location.endsWith(".JPEG")) {

            contentType = CONTENT_JPEG;

        } else if (location.endsWith(".mov") || location.endsWith(".MOV")) {

            contentType = CONTENT_MOV;

        } else if (location.indexOf('?') != -1) {

            contentType = null;

        } else {
            contentType = CONTENT_PLAIN;
        }

        HttpRequestEvent event = server.fireHttpRequestEvent(location, out);

        // Check to see if the Writer in the Event was used. If it
        // was, then the listeners contributed concatenated text, and
        // the result needs to be retrieved and sent back.
        if (event.isWriterUsed()) {
            Writer writer = event.getWriter();
            String result = writer.toString();
            writeHttpResponse(out, contentType, result);
        }

        // else - assume that the binary response listeners took care
        // of writing things back on their own,

        out.flush();
        close();
    }

    /**
     * Can be used to write the header to an HttpResponse. You need to
     * create a Writer tied to the OutputStream in order to write this
     * text.
     * 
     * @param out Writer to place text on the OutputStream.
     * @param contentType the mime type for your response.
     * @param contentLength the byte length of your response.
     */
    public static void writeHttpResponseHeader(Writer out, String contentType,
                                               int contentLength)
            throws IOException {

        out.write("HTTP/1.0 200 \n"); // return status
        out.write("Content-type: " + contentType + "\n"); // important!
        out.write("Content-Length: " + contentLength + "\n"); // important!
        out.write("\n");
    }

    /**
     * Write a String response to the OutputStream.
     * 
     * @param out the OutputStream of the response.
     * @param contentType the content type of the response.
     * @param response the string containing the response.
     */
    public static void writeHttpResponse(OutputStream out, String contentType,
                                         String response) throws IOException {

        OutputStreamWriter osw = new OutputStreamWriter(out);
        writeHttpResponseHeader(osw, contentType, response.length());
        osw.write(response);
        osw.flush();
    }

    /**
     * Write a byte[] response to the OutputStream.
     * 
     * @param out the OutputStream of the response.
     * @param contentType the content type of the response.
     * @param response the byte array containing the response.
     */
    public static void writeHttpResponse(OutputStream out, String contentType,
                                         byte[] response) throws IOException {
        OutputStreamWriter osw = new OutputStreamWriter(out);
        writeHttpResponseHeader(osw, contentType, response.length);
        osw.flush();
        out.write(response, 0, response.length);
        osw.flush();
    }

    /**
     * Close the socket connection that we have opened
     */
    public void close() throws IOException {
        client.close();
        isConnectionOpen = false;
    }
}

