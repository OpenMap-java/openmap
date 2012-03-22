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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/link/LinkActionRequest.java,v $
// $RCSfile: LinkActionRequest.java,v $
// $Revision: 1.4 $
// $Date: 2004/10/14 18:05:56 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.link;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.IOException;

import com.bbn.openmap.util.Debug;

/**
 * Class used to send a gesture to a server. In this case, a gesture
 * can be a mouse click event, a mouse event, or a key event. This
 * class defines how such notification is formatted and sent. The
 * client can use the static write methods to put the object on the
 * link, and the server creates an instance of this object to read the
 * object off the link, and find out what the fields were.
 */
public class LinkActionRequest implements LinkActionConstants,
        LinkPropertiesConstants {
    /** The horizontal screen location of the mouse event, in pixels. */
    protected int x = 0;
    /** The vertical screen location of the mouse event, in pixels. */
    protected int y = 0;
    /** The latitide location of the mouse event, in decimal degrees. */
    protected float lat = 0;
    /** The longitude location of the mouse event, in decimal degrees. */
    protected float lon = 0;
    /** The click count of the mouse event. */
    protected int clickCount = 0;
    /**
     * The modifier of the event, that describes any keys that may
     * have been pressed while the event occurred.
     */
    protected int modifiers = 0;
    /** The character of the key that was pressed in a key event. */
    protected char key;
    /** The mask describing the event. */
    protected int descriptor = 0;
    /** Graphic ID of an object selected by a gesture. */
    protected String id = null;
    /** The properties object that contains any pertinent arguments. */
    protected LinkProperties properties = null;
    /**
     * The terminator of the graphics section when receiving gesture
     * queries.
     */
    String linkStatus = Link.END_TOTAL;
    /** Version Number of request format. */
    protected static float version = Link.LINK_VERSION;

    public LinkActionRequest(Link link) throws IOException {
        linkStatus = read(link);
    }

    /**
     * Constructor used by a server to let the client know what types
     * of gestures the server can receive. This constructor
     * automatically writes out the header and descriptor to the link.
     * It does not call the link.end() method to end the transmission,
     * since this will probably be sent along with other sections in a
     * communication to the client.
     * 
     * @param link the link to write to.
     * @param descriptor a masked integer (see constants) describing
     *        the event to receive.
     * @param sectionEnder the endType string to use for this
     *        description, either Link.END_TOTAL if this is the last
     *        section to the client, or END_SECTION if there are more
     *        sections following.
     */
    public LinkActionRequest(Link link, int descriptor, String sectionEnder)
            throws IOException {
        link.start(Link.ACTION_REQUEST_HEADER);
        link.dos.writeFloat(version);
        descriptor = LinkUtil.setMask(descriptor, CLIENT_NOTIFICATION_MASK);
        link.dos.writeInt(descriptor);
        link.end(sectionEnder);
    }

    public String getType() {
        return Link.ACTION_REQUEST_HEADER;
    }

    /**
     * After reading the gesture response, this returns the section
     * ending string terminating the gesture section, either
     * Link.END_TOTAL or Link.END_SECTION.
     * 
     * @return either Link.END_TOTAL or Link.END_SECTION.
     */
    public String getLinkStatus() {
        return linkStatus;
    }

    /**
     * Write a MouseEvent on the link to the server.
     * 
     * @param descriptor the MASK that describes the event.
     * @param me the MouseEvent
     * @param latPoint the latitude of the mouse event.
     * @param lonPoint the longitude of the mouse event.
     * @param props an array of strings representing key-value pairs.
     * @param link the link to write the gesture to.
     */
    public static void write(int descriptor, MouseEvent me, float latPoint,
                             float lonPoint, LinkProperties props, Link link)
            throws IOException {

        if (props.getProperty(LPC_GRAPHICID) != null) {
            descriptor = LinkUtil.setMask(descriptor, GRAPHIC_ID_MASK);
        }

        link.start(Link.ACTION_REQUEST_HEADER);
        link.dos.writeFloat(version);
        link.dos.writeInt(descriptor);
        link.dos.writeInt(me.getX());
        link.dos.writeInt(me.getY());
        link.dos.writeInt(me.getClickCount());
        link.dos.writeInt(me.getModifiers());
        link.dos.writeFloat(latPoint);
        link.dos.writeFloat(lonPoint);

        props.write(link);

        link.end(Link.END_TOTAL);
    }

    /**
     * Write a KeyEvent on the link to the server.
     * 
     * @param descriptor the MASK that describes the event.
     * @param ke the KeyEvent
     * @param props Properties representing attributes.
     * @param link the Link to write the gesture to.
     */
    public static void write(int descriptor, KeyEvent ke, LinkProperties props,
                             Link link) throws IOException {

        link.start(Link.ACTION_REQUEST_HEADER);
        link.dos.writeFloat(version);
        link.dos.writeInt(descriptor);
        link.dos.writeChar(ke.getKeyChar());
        link.dos.writeInt(ke.getModifiers());

        props.write(link);

        link.end(Link.END_TOTAL);
    }

    /**
     * Read the link and pull off the gesture, filling in the fields
     * of this object.
     * 
     * @param link the link to read from.
     * @return Link.END_TOTAL or Link.END_SECTION
     */
    public String read(Link link) throws IOException {

        float ver = link.dis.readFloat();

        if (ver != version) {
            if (ver == .1) {// Big difference....
                throw new IOException("LinkActionRequest: Versions do not match! DANGER!");
            } else {
                Debug.message("link",
                        "LinkActionRequest: Versions do not match");
            }
        }

        // the second thing we get is the descriptor
        descriptor = link.dis.readInt();
        if (isClientNotification()) {
            // In case it is passed back later to the server - we
            // really don't need to know that it was a notification
            // mask after this, right??
            descriptor = LinkUtil.unsetMask(descriptor,
                    CLIENT_NOTIFICATION_MASK);
            return link.readDelimiter(false);
        } else if (isKeyEvent()) {
            // key event
            key = link.dis.readChar();
            modifiers = link.dis.readInt();
        } else {
            // Mouse event
            x = link.dis.readInt();
            y = link.dis.readInt();
            clickCount = link.dis.readInt();
            modifiers = link.dis.readInt();
            lat = link.dis.readFloat();
            lon = link.dis.readFloat();
        }

        properties = new LinkProperties(link);

        if (LinkUtil.isMask(descriptor, GRAPHIC_ID_MASK)) {
            id = properties.getProperty(LPC_GRAPHICID);
        }

        return link.readDelimiter(false);
    }

    /** Get latitude of mouse gesture, in decimal degrees. */
    public float getLat() {
        return lat;
    }

    /** Get longitude of mouse gesture, in decimal degrees. */
    public float getLon() {
        return lon;
    }

    /**
     * Get horizontal pixel location of mouse gesture, from left side
     * of map.
     */
    public int getX() {
        return x;
    }

    /** Get vertical pixel location of mouse gesture, from top of map. */
    public int getY() {
        return y;
    }

    /** Get the keyboard key that was pressed from a key gesture. */
    public char getKey() {
        return key;
    }

    /** Get the masked int that describes the gesture. */
    public int getDescriptor() {
        return descriptor;
    }

    /**
     * If this is null, it means that the gesture occurred on the map
     * background and is not affiliated with a particular graphic.
     * Otherwise, the graphic ID string is returned for the affiliated
     * graphic.
     */
    public String getGraphicID() {
        return id;
    }

    /**
     * Get the masked int that describes any modifing keys pressed
     * with the gesture.
     */
    public int getModifiers() {
        return modifiers;
    }

    /** Get the key-value args sent with the gesture. */
    public LinkProperties getProperties() {
        return properties;
    }

    /**
     * Returns true if the query is to let the client know what
     * gestures the server is interested in receiving.
     */
    protected boolean isClientNotification() {
        return ((descriptor & CLIENT_NOTIFICATION_MASK) != 0);
    }

    /** Returns true if the query is representing a key event. */
    protected boolean isKeyEvent() {
        return ((descriptor & KEY_RELEASED_MASK) != 0)
                || ((descriptor & KEY_PRESSED_MASK) != 0);
    }

    public String toString() {
        StringBuffer s = new StringBuffer();
        s.append("LinkActionRequest:\n");
        if (isKeyEvent()) {
            s.append(" Key Event\n");
            s.append(" Key: ").append(key).append("\n");
        } else {
            s.append(" Mouse Event\n");
            s.append(" X: ").append(x).append("\n");
            s.append(" Y: ").append(y).append("\n");
            s.append(" Lat: ").append(lat).append("\n");
            s.append(" Lon: ").append(lon).append("\n");
        }
        s.append(" Modifiers: ").append(modifiers).append("\n");

        return s.toString();
    }
}