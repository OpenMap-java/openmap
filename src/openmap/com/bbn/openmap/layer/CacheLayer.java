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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/CacheLayer.java,v $
// $RCSfile: CacheLayer.java,v $
// $Revision: 1.4 $
// $Date: 2005/08/09 19:20:29 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;

import com.bbn.openmap.Layer;
import com.bbn.openmap.event.MapMouseListener;
import com.bbn.openmap.event.ProjectionEvent;
import com.bbn.openmap.event.SelectMouseMode;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PropUtils;

/**
 * A Layer that gets it's graphics from a URL containing a serialized
 * OMGraphicList. This layer does respond to gesturing on the
 * graphics, but doesn't do anything. You can extend this class to be
 * more useful to you. It has one property that needs to be set in the
 * properties file:
 * <P># CacheLayer property: <BR># The layer should figure out
 * whether it's a file or URL. <BR>
 * cachelayer.cacheFile= <url of cachefile> <BR>
 */
public class CacheLayer extends Layer implements ActionListener,
        MapMouseListener {

    public static final String CacheFileProperty = "cacheFile";

    /** Used by the gui */
    private static final String READ_DATA_COMMAND = "ReadData";

    /**
     * URL to read data from. This data will be in the form of a
     * serialized stream of OMGraphics.
     */
    protected URL cacheURL;

    /**
     * A list of graphics to be painted on the map.
     */
    protected OMGraphicList omgraphics = new OMGraphicList();

    /**
     * Construct a default CacheLayer.
     */
    public CacheLayer() {}

    /**
     * Read a cache of OMGraphics
     */
    public void readGraphics() throws java.io.IOException {

        if (Debug.debugging("cachelayer")) {
            Debug.output("Reading cached graphics");
        }

        if (omgraphics == null) {
            omgraphics = new OMGraphicList();
        }

        if (cacheURL != null) {
            omgraphics.readGraphics(cacheURL);
        }
    }

    /**
     * Initializes this layer from the given properties.
     * 
     * @param props the <code>Properties</code> holding settings for
     *        this layer
     */
    public void setProperties(String prefix, Properties props) {
        super.setProperties(prefix, props);

        prefix = PropUtils.getScopedPropertyPrefix(prefix);

        String cacheFile = props.getProperty(prefix + CacheFileProperty);

        try {
            if (cacheFile != null) {
                if (Debug.debugging("cachelayer")) {
                    Debug.output("Getting cachefile: " + cacheFile);
                }

                // First find the resource, if not, then try as a
                // file-URL... b
                cacheURL = PropUtils.getResourceOrFileOrURL(this, cacheFile);

                if (cacheURL != null) {
                    readGraphics();
                }
            }
        } catch (java.net.MalformedURLException mue) {
            mue.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    //----------------------------------------------------------------------
    // Layer overrides
    //----------------------------------------------------------------------

    /**
     * Renders the graphics list. It is important to make this routine
     * as fast as possible since it is called frequently by Swing, and
     * the User Interface blocks while painting is done.
     */
    public void paint(java.awt.Graphics g) {
        omgraphics.render(g);
    }

    //----------------------------------------------------------------------
    // ProjectionListener interface implementation
    //----------------------------------------------------------------------

    /**
     * Handler for <code>ProjectionEvent</code>s. This function is
     * invoked when the <code>MapBean</code> projection changes. The
     * graphics are reprojected and then the Layer is repainted.
     * <p>
     * 
     * @param e the projection event
     */
    public void projectionChanged(ProjectionEvent e) {
        omgraphics.project(e.getProjection(), true);
        repaint();
    }

    //----------------------------------------------------------------------
    /**
     * Provides the palette widgets to control the options of showing
     * maps, or attribute text.
     * 
     * @return Component object representing the palette widgets.
     */
    public Component getGUI() {

        JButton rereadFilesButton = new JButton("ReRead OMGraphics");
        rereadFilesButton.setActionCommand(READ_DATA_COMMAND);
        rereadFilesButton.addActionListener(this);

        JLabel fileLabel = new JLabel("Read from: ");
        JTextField pathText = new JTextField(cacheURL.toString());

        Box filebox = Box.createHorizontalBox();
        filebox.add(fileLabel);
        filebox.add(pathText);

        Box box = Box.createVerticalBox();
        box.add(rereadFilesButton);
        box.add(filebox);
        return box;
    }

    //----------------------------------------------------------------------
    // ActionListener interface implementation
    //----------------------------------------------------------------------

    /**
     * The Action Listener method, that reacts to the palette widgets
     * actions.
     */
    public void actionPerformed(java.awt.event.ActionEvent e) {
        String cmd = e.getActionCommand();
        if (cmd == READ_DATA_COMMAND) {
            Debug.message("cachelayer",
                    "CacheLayer: Reading serialized graphics");
            try {
                readGraphics();
            } catch (java.io.IOException exc) {
                exc.printStackTrace();
            }
        } else {
            Debug.error("Unknown action command \"" + cmd
                    + "\" in SaveShapeLayer.actionPerformed().");
        }
    }

    //----------------------------------------------------------------------
    // MapMouseListener interface implementation
    //----------------------------------------------------------------------

    private OMGraphic selectedGraphic;

    /**
     * Indicates which mouse modes should send events to this
     * <code>Layer</code>.
     * 
     * @return An array mouse mode names
     * 
     * @see com.bbn.openmap.event.MapMouseListener
     * @see com.bbn.openmap.MouseDelegator
     */
    public String[] getMouseModeServiceList() {
        String[] ret = { SelectMouseMode.modeID };
        return ret;
    }

    /**
     * Called whenever the mouse is pressed by the user and one of the
     * requested mouse modes is active.
     * 
     * @param e the press event
     * @return true if event was consumed (handled), false otherwise
     * @see #getMouseModeServiceList
     */
    public boolean mousePressed(MouseEvent e) {
        return false;
    }

    /**
     * Called whenever the mouse is released by the user and one of
     * the requested mouse modes is active.
     * 
     * @param e the release event
     * @return true if event was consumed (handled), false otherwise
     * @see #getMouseModeServiceList
     */
    public boolean mouseReleased(MouseEvent e) {
        return false;
    }

    /**
     * Called whenever the mouse is clicked by the user and one of the
     * requested mouse modes is active.
     * 
     * @param e the click event
     * @return true if event was consumed (handled), false otherwise
     * @see #getMouseModeServiceList
     */
    public boolean mouseClicked(MouseEvent e) {
        if (selectedGraphic != null) {
            switch (e.getClickCount()) {
            case 1:
                if (Debug.debugging("cachelayer")) {
                    Debug.output("CacheLayer: Show Info: "
                            + selectedGraphic.getAppObject());
                }
                break;
            case 2:
                if (Debug.debugging("cachelayer")) {
                    Debug.output("CacheLayer: Request URL: " + selectedGraphic);
                }
                break;
            default:
                break;
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * Called whenever the mouse enters this layer and one of the
     * requested mouse modes is active.
     * 
     * @param e the enter event
     * @see #getMouseModeServiceList
     */
    public void mouseEntered(MouseEvent e) {}

    /**
     * Called whenever the mouse exits this layer and one of the
     * requested mouse modes is active.
     * 
     * @param e the exit event
     * @see #getMouseModeServiceList
     */
    public void mouseExited(MouseEvent e) {}

    /**
     * Called whenever the mouse is dragged on this layer and one of
     * the requested mouse modes is active.
     * 
     * @param e the drag event
     * @return true if event was consumed (handled), false otherwise
     * @see #getMouseModeServiceList
     */
    public boolean mouseDragged(MouseEvent e) {
        return false;
    }

    private Color oldFillColor = java.awt.Color.yellow;

    /**
     * Called whenever the mouse is moved on this layer and one of the
     * requested mouse modes is active.
     * <p>
     * Tries to locate a graphic near the mouse, and if it is found,
     * it is highlighted and the Layer is repainted to show the
     * highlighting.
     * 
     * @param e the move event
     * @return true if event was consumed (handled), false otherwise
     * @see #getMouseModeServiceList
     */
    public boolean mouseMoved(MouseEvent e) {
        OMGraphic newSelectedGraphic = omgraphics.selectClosest(e.getX(),
                e.getY(),
                2.0f);

        if (newSelectedGraphic != selectedGraphic) {
            if (selectedGraphic != null)
                selectedGraphic.setFillPaint(oldFillColor);

            selectedGraphic = newSelectedGraphic;
            if (newSelectedGraphic != null) {
                oldFillColor = newSelectedGraphic.getFillColor();
                newSelectedGraphic.setFillPaint(Color.white);
                fireRequestInfoLine(newSelectedGraphic.getAppObject()
                        .toString());
            }
            repaint();
        }

        return true;
    }

    /**
     * Called whenever the mouse is moved on this layer and one of the
     * requested mouse modes is active, and the gesture is consumed by
     * another active layer. We need to deselect anything that may be
     * selected.
     * 
     * @see #getMouseModeServiceList
     */
    public void mouseMoved() {
        omgraphics.deselect();
        repaint();
    }

    /**
     * Returns self as the <code>MapMouseListener</code> in order to
     * receive <code>MapMouseEvent</code>s. If the implementation
     * would prefer to delegate <code>MapMouseEvent</code>s, it
     * could return the delegate from this method instead.
     * 
     * @return The object to receive <code>MapMouseEvent</code> s or
     *         null if this layer isn't interested in
     *         <code>MapMouseEvent</code> s
     */
    public MapMouseListener getMapMouseListener() {
        return this;
    }
}