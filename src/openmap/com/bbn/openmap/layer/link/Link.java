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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/link/Link.java,v $
// $RCSfile: Link.java,v $
// $Revision: 1.5 $
// $Date: 2004/10/14 18:05:55 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.link;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import com.bbn.openmap.Layer;
import com.bbn.openmap.omGraphics.grid.OMGridGenerator;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.Debug;

/**
 * The Link object is the main mechanism for communications between a
 * LinkClient (most likely a LinkLayer) and a LinkServer. This class
 * should only be used directly by a server - clients should use the
 * ClientLink object. This object defines the communications that
 * either side can make.
 * <P>
 * 
 * The ClientLink adds some control methods that the client should
 * use. The client needs to make sure that several queries are not
 * sent to the server at the same time. The ClientLink contains a lock
 * that can be checked, and set. It is up to the client to manage the
 * lock. The ClientLink also provides the method to close the link
 * down, since it makes that decision. The server should remain
 * connected until the client is finished. The server can request to
 * be disconnected, however, and the ClientLink provides a method for
 * the client to check if that request has been made.
 */
public class Link implements LinkConstants {
    /** The apparent maximum size of a header. */
    public final static int MAX_HEADER_LENGTH = 80;
    /** For outgoing traffic. */
    protected LinkOutputStream dos = null;
    /** For incoming traffic. */
    protected DataInputStream dis = null;
    /** Used to read/create strings from off the input stream. */
    protected char[] charArray = new char[MAX_HEADER_LENGTH];
    /**
     * Set for the client, by the server, to indicate whether the
     * socket should be closed. By default, this will be false, Used
     * when the server wants to run in a stateless mode, and doesn't
     * care to maintain a connection with the client. It's included in
     * the Link object because the server knows about it and sets it
     * in the client.
     */
    protected boolean closeLink = false;
    /**
     * Used to retrieve any potential graphics queries that came in
     * over the link.
     */
    protected LinkMapRequest mapRequest = null;
    /**
     * Used to retrieve any potential graphics responses that came in
     * over the link.
     */
    protected LinkGraphicList graphicList = null;
    /**
     * Used to retrieve any potential gesture queries that came in
     * over the link.
     */
    protected LinkActionRequest actionRequest = null;
    /**
     * Used to retrieve any potential gesture responses that came in
     * over the link.
     */
    protected LinkActionList actionList = null;
    /**
     * Used to retrieve any potential GUI queries that came in over
     * the link.
     */
    protected LinkGUIRequest guiRequest = null;
    /**
     * Used to retrieve any potential GUI responses that came in over
     * the link.
     */
    protected LinkGUIList guiList = null;
    /** The socket used for the link. Kept for convenience. */
    protected Socket socket = null;
    /**
     * The lock. This should only be changed within a synchronized
     * block of code, synchronized on the link object.!! Otherwise,
     * race conditions can result.
     */
    protected boolean locked = false;
    /**
     * Flag to control whether this side of the link will adhere to
     * shutdown commands issued from other side of the link. False by
     * default.
     */
    protected boolean obeyCommandToExit = false;

    /**
     * Open up a link over a socket.
     * 
     * @param socket the socket to open the Link on.
     * @throws IOException
     */
    public Link(Socket socket) throws IOException {
        this.socket = socket;
        InputStream is = socket.getInputStream();
        BufferedInputStream bis = new BufferedInputStream(is);
        this.dis = new DataInputStream(bis);

        OutputStream os = socket.getOutputStream();
        BufferedOutputStream bos = new BufferedOutputStream(os);
        this.dos = new LinkOutputStream(bos);
    }

    /**
     * Should be called by the server and/or client to release
     * resources when the link is through being used.
     */
    public void cleanUp() {
        try {
            this.dis.close();
            this.dos.close();
        } catch (IOException ioe) {
        }

        this.dis = null;
        this.dos = null;
    }

    /**
     * The method to call at the beginning of a request or response.
     * It writes the header given to the link. This header is expected
     * on the other side of the link.
     * 
     * @param messageHeader Header string, defined in the Link object,
     *        that describes the tranmission.
     * @throws IOException
     */
    public void start(String messageHeader) throws IOException {
        dos.write(messageHeader.getBytes());
    }

    /**
     * The method that needs to be called at the end of a
     * request/response or section. This places the END_TOTAL symbol
     * on the link to let the other side know that the transmission is
     * done, and it also flushes the output stream buffer.
     * 
     * @param endType use END_SECTION if you want to add more types of
     *        responses. Use END_TOTAL at the end of the total
     *        transmission.
     * @throws IOException
     */
    public void end(String endType) throws IOException {
        dos.write(endType.getBytes());
        if (END_TOTAL.equals(endType)) {
            dos.flush();
        }
    }

    /**
     * Called to begin reading the information coming off the link.
     * Since the information can be coming in different sections, this
     * method figures out how to read the different sections and get
     * ready for requests on what was read. After the link is read,
     * you can then request the link to find out what was sent back -
     * for graphics, GUI components, or actions. When this method is
     * called, the link resets the objects that are returned by
     * getGraphics(), getGUI and getActions(). These methods are meant
     * to be used after read() to find out what was returned.
     * 
     * @throws IOException
     */
    public void readAndParse() throws IOException {
        readAndParse(null, null);
    }

    /**
     * Called to begin reading the information coming off the link.
     * 
     * @param proj a projection for graphics
     * @param generator an OMGridGenerator that knows how to render
     *        grid objects.
     * @throws IOException
     */
    public void readAndParse(Projection proj, OMGridGenerator generator)
            throws IOException {
        readAndParse(proj, generator, null);
    }

    /**
     * Called to begin reading the information coming off the link.
     * 
     * @param proj pass in a projection if you are expecting graphics
     *        to arrive, and they will be projected as they come off
     *        the link.
     * @param generator an OMGridGenerator that knows how to render
     *        grid objects.
     * @param layer a layer that is interested in gesture reactions.
     * @throws IOException
     */
    public void readAndParse(Projection proj, OMGridGenerator generator,
                             Layer layer) throws IOException {

        // Reset everything //

        // Keep this here, so if there is more than one graphics
        // section, then all the graphics get added to one list.
        LinkOMGraphicList graphics = new LinkOMGraphicList();

        graphicList = null;
        mapRequest = null;
        actionRequest = null;
        actionList = null;
        guiRequest = null;
        guiList = null;
        closeLink = false;

        String delimiter = null;

        if (Debug.debugging("link")) {
            System.out.println("Link|readAndParse: listening to link:");
            System.out.println((proj == null ? " without " : " with ")
                    + "a projection and");
            System.out.println((layer == null ? " without " : " with ")
                    + "a layer");
        }

        while (true) {
            delimiter = readDelimiter(true);
            if (Debug.debugging("link")) {
                System.out.println("Link:reading section: " + delimiter);
            }
            if (delimiter == GRAPHICS_HEADER) {
                if (layer != null) {
                    graphicList = new LinkGraphicList(this, graphics, layer.getProjection(), generator);
                } else {
                    graphicList = new LinkGraphicList(this, graphics, proj, generator);
                }
                delimiter = graphicList.getLinkStatus();
            } else if (delimiter == ACTIONS_HEADER) {
                actionList = new LinkActionList(this, layer, proj, generator);
                delimiter = actionList.getLinkStatus();
            } else if (delimiter == GUI_HEADER) {
                guiList = new LinkGUIList(this);
                delimiter = guiList.getLinkStatus();
            } else if (delimiter == CLOSE_LINK_HEADER) {
                closeLink = true;
            } else if (delimiter == SHUTDOWN_HEADER) {
                Debug.message("link", "Link.received command to exit");
                if (obeyCommandToExit) {
                    System.exit(0);
                }
            } else if (delimiter == HUH_HEADER) {
                delimiter = readDelimiter(true);
            } else if (delimiter == MAP_REQUEST_HEADER) {
                mapRequest = new LinkMapRequest(this);
                delimiter = mapRequest.getLinkStatus();
            } else if (delimiter == ACTION_REQUEST_HEADER) {
                actionRequest = new LinkActionRequest(this);
                delimiter = actionRequest.getLinkStatus();
            } else if (delimiter == GUI_REQUEST_HEADER) {
                guiRequest = new LinkGUIRequest(this);
                delimiter = guiRequest.getLinkStatus();
            } else if (delimiter == PING_REQUEST_HEADER) {
                start(PING_RESPONSE_HEADER);
                end(END_TOTAL);
                delimiter = readDelimiter(false);
            }

            if (delimiter == END_TOTAL) {
                return;
            }
        }
    }

    public void setObeyCommandToExit(boolean value) {
        obeyCommandToExit = value;
    }

    public boolean getAcceptCommandToExit() {
        return obeyCommandToExit;
    }

    /**
     * After a readAndParse() has been called on a link, this can be
     * called to retrieve a graphics request, if one was sent.
     * 
     * @return LinkMapRequest containing the request.
     */
    public LinkMapRequest getMapRequest() {
        return mapRequest;
    }

    /**
     * After a readAndParse() has been called on a link, this can be
     * called to retrieve graphics in an LinkOMGraphicList, if any
     * graphics were sent.
     * 
     * @return GraphicLinkRsponse containing the information. If no
     *         graphics were sent the list will be empty.
     */
    public LinkGraphicList getGraphicList() {
        return graphicList;
    }

    /**
     * After a readAndParse() has been called on a link, this can be
     * called to retrieve a gesture notification/request, if one was
     * sent.
     * 
     * @return LinkActionRequest containing the request.
     */
    public LinkActionRequest getActionRequest() {
        return actionRequest;
    }

    /**
     * After a readAndParse() has been called on a link, this can be
     * called to retrieve the gesture response.
     * 
     * @return LinkActionList containing the information.
     */
    public LinkActionList getActionList() {
        return actionList;
    }

    /**
     * After a readAndParse() has been called on a link, this can be
     * called to retrieve a gesture notification/request, if one was
     * sent.
     * 
     * @return LinkGUIRequest containing the request.
     */
    public LinkGUIRequest getGUIRequest() {
        return guiRequest;
    }

    /**
     * After a readAndParse() has been called on a link, this can be
     * called to retrieve the GUI response, if any GUI components were
     * sent.
     *  
     */
    public LinkGUIList getGUIList() {
        return guiList;
    }

    /**
     * readDelimiter is a function designed to read a header string
     * off the data input stream in the Link object. It expects that
     * the next byte off the link will be a ' <' in the stream, and
     * then reads through the stream until it finds the '>' expected
     * at the end of the string. It will also return a string version
     * of END_TOTAL or END_SECTION if it is encountered instead. If
     * desired, an intern version of the string is returned.
     * 
     * @param returnString if true, an intern String version of the
     *        characters is returned.
     * @throws IOException
     * @throws ArrayIndexOutOfBoundsException
     */
    protected String readDelimiter(boolean returnString) throws IOException,
            ArrayIndexOutOfBoundsException {
        String ret = END_TOTAL;

        char END_TOTAL_CHAR = END_TOTAL.charAt(0);
        char END_SECTION_CHAR = END_SECTION.charAt(0);

        char c = (char) dis.readByte();

        // NOTE: possibility of early exits here...
        if (c == END_TOTAL_CHAR) {
            Debug.message("link", "Link|readDelimiter: Found END_TOTAL");
            return END_TOTAL;
        } else if (c == END_SECTION_CHAR) {
            Debug.message("link", "Link|readDelimiter: Found END_SECTION");
            return END_SECTION;
        } else if (c != '<') {
            if (Debug.debugging("link")) {
                System.out.println("Link|readDelimiter: unexpected protocol data read '"
                        + c + "'");
            }
            throw new IOException("readDelimiter: unexpected protocol data read.");
        }

        // The byte read does indeed equal '<'
        int charCount = 0;

        // c should == '<'
        charArray[charCount++] = c;
        // Get the rest of the header information
        c = (char) dis.readByte();
        while (c != '>' && charCount < MAX_HEADER_LENGTH - 1) {
            charArray[charCount++] = c;
            c = (char) dis.readByte();
        }

        // c should == '>' or uh-oh - too many characters between
        // them. Exit with a faulty return if this is the case.
        if (c != '>') {
            throw new IOException("readDelimiter: header is too long.");
        }

        charArray[charCount++] = c;

        // OK, got it - return string
        if (returnString) {
            ret = new String(charArray, 0, charCount).intern();
        } else {
            ret = "";
        }
        return ret;
    }

    /**
     * Other threads can check to see if the link is in use.
     * 
     * @return true if link in use and unavailable.
     */
    public boolean isLocked() {
        return locked;
    }

    /**
     * Set the lock. Should only be called in a synchronized block of
     * code, where you have control over the link.
     * 
     * @param set true if the lock should be turned on, false if the
     *        link should be released.
     */
    public synchronized boolean setLocked(boolean set) {
        if (set == true) {
            if (locked == true) {
                // The lock was NOT set for the caller - unsuccessful.
                return false;
            } else {
                locked = true;
                // The lock was set for the caller, successfully.
                return true;
            }
        } else {
            locked = set;
            // The state was set to false, successfully.
            return true;
        }
    }

    /**
     * This method is provided for those who want to optimize how they
     * write the graphical objects to the output stream. Look in the
     * Link <graphics>API to find out the order of the pieces for
     * each graphic type. Not recommended for the faint of heart.
     */
    public DataOutput getDOS() {
        return dos;
    }

    /**
     * This method complements getDOS().
     */
    public DataInput getDIS() {
        return dis;
    }

    /**
     * Returns the number of bytes written since the last
     * clearBytesWritten() call.
     */
    public int getBytesWritten() {
        return dos.size();
    }

    /**
     * Reset the bytes written count to 0.
     * 
     * @return the old byte count
     */
    public int clearBytesWritten() {
        return dos.clearWritten();
    }
}