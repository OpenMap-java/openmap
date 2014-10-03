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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/link/amp/AmpLinkLayer.java,v $
// $RCSfile: AmpLinkLayer.java,v $
// $Revision: 1.6 $
// $Date: 2006/03/06 16:14:00 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.link.amp;

import java.awt.Color;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.net.UnknownHostException;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.bbn.openmap.gui.Tool;
import com.bbn.openmap.layer.link.ClientLink;
import com.bbn.openmap.layer.link.Link;
import com.bbn.openmap.layer.link.LinkActionList;
import com.bbn.openmap.layer.link.LinkGraphic;
import com.bbn.openmap.layer.link.LinkLayer;
import com.bbn.openmap.layer.link.LinkOMGraphicList;
import com.bbn.openmap.layer.link.LinkProperties;
import com.bbn.openmap.omGraphics.GraphicAttributes;
import com.bbn.openmap.omGraphics.OMAction;
import com.bbn.openmap.omGraphics.OMCircle;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.OMRangeRings;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.tools.drawing.DrawingTool;
import com.bbn.openmap.util.Debug;

/**
 * The AmpLinkLayer is an extension to the LinkLayer. The difference
 * is that this layer provides some components to the ToolBar for the
 * creation of certain graphics to be sent to the LinkServer. This
 * layer also wants access to the DrawingTool.
 * <P>
 * 
 * The entry in the openmap.properties file looks like this:
 * <P>
 * 
 * <pre>
 * 
 *  
 *   
 *    # port number of server
 *    link.port=3031
 *    # host name of server
 *    link.host=host.com
 *    # URL of properties file for server attributes
 *    link.propertiesURL=http://location.of.properties.file.com
 *    
 *   
 *  
 * </pre>
 */
public class AmpLinkLayer extends LinkLayer implements Tool {

    public final static String RRIntervalUnitsProperty = ".rangeRingIntervalUnits";
    public final static String RRIntervalProperty = ".rangeRingInterval";

    protected OMGraphicList extraGraphics = new OMGraphicList();

    protected DrawingTool drawingTool = null;
    protected int orientation = SwingConstants.HORIZONTAL;

    /**
     * The default constructor for the Layer. All of the attributes
     * are set to their default values.
     */
    public AmpLinkLayer() {}

    /**
     * Constructor to use when LinkLayer is not being used with
     * OpenMap application.
     * 
     * @param host the hostname of the server's computer.
     * @param port the port number of the server.
     * @param propertiesURL the URL of a properties file that contains
     *        parameters for the server.
     */
    public AmpLinkLayer(String host, int port, String propertiesURL) {
        super(host, port, propertiesURL);
    }

    /**
     * Set all the Link properties from a properties object.
     * 
     * @param prefix the prefix to the properties that might
     *        individualize it to a particular layer.
     * @param properties the properties for the layer.
     */
    public void setProperties(String prefix, java.util.Properties properties) {
        super.setProperties(prefix, properties);
        setAddToBeanContext(true);
    }

    /**
     * Prepares the graphics for the layer. This is where the
     * getRectangle() method call is made on the link.
     * <p>
     * Occasionally it is necessary to abort a prepare call. When this
     * happens, the map will set the cancel bit in the LayerThread,
     * (the thread that is running the prepare). If this Layer needs
     * to do any cleanups during the abort, it should do so, but
     * return out of the prepare asap.
     * 
     * @return a list of graphics.
     */
    public synchronized OMGraphicList prepare() {
        Projection projection = getProjection();
        if (projection != null) {
            extraGraphics.generate(projection);
        }
        return super.prepare();
    }

    /**
     * Paints the layer.
     * 
     * @param g the Graphics context for painting
     */
    public void paint(java.awt.Graphics g) {
        if (Debug.debugging("link")) {
            System.out.println(getName() + "|AmpLinkLayer.paint()");
        }

        if (extraGraphics != null) {
            extraGraphics.render(g);
        }

        super.paint(g);
    }

    /**
     * Just need to check here if one of the new extra graphics (range
     * ring) was clicked on, so that it can be sent to the drawing
     * tool for modification if desired.
     */
    public boolean mouseClicked(MouseEvent e) {
        Debug.message("link", "AmpLinkLayer mouseClicked");
        LinkOMGraphicList graphics = getGraphicList(); // Get old list
        OMGraphic gesGraphic = null;

        gesGraphic = graphics.findClosest(e.getX(), e.getY(), distanceLimit);
        if (gesGraphic == null) {
            gesGraphic = extraGraphics.findClosest(e.getX(),
                    e.getY(),
                    distanceLimit);
        }

        if (gesGraphic != null && drawingTool != null) {
            DrawingTool dt = getDrawingTool();
            OMGraphic graphic = null;
            if (dt != null) {
                graphic = dt.edit(gesGraphic, layer);
            }

            if (graphic != null) {
                Debug.message("link", "AmpLinkLayer editing graphic");
                return true;
            } else {
                Debug.message("link", "AmpLinkLayer unable to edit graphic");
            }
        }
        return super.mouseClicked(e);
    }

    protected final com.bbn.openmap.tools.drawing.DrawingToolRequestor layer = this;

    // DrawingToolRequestor method
    public void drawingComplete(OMGraphic omg, OMAction action) {
        ////////////// send the new graphic, along with instructions
        //on what to do with it, to the server.
        String id = null; // unknown

        Object obj = omg.getAppObject();
        LinkProperties lp = null;

        if (obj instanceof LinkProperties) {
            lp = (LinkProperties) obj;
            id = lp.getProperty(LPC_GRAPHICID);
            Debug.message("link",
                    "AmpLinkLayer: received modified server graphic " + lp);
        } else {
            Debug.message("link", "AmpLinkLayer: received new graphic from dt");
        }

        if (id == null) {
            // Doesn't look like it was a modified graphic already
            // received from the server, so we should tell the server
            // to add it to its list.
            action.setMask(OMAction.ADD_GRAPHIC_MASK);
        } else {
            action.setMask(OMAction.UPDATE_GRAPHIC_MASK);
        }

        if (omg instanceof OMRangeRings) {
            extraGraphics.doAction(omg, action);
            repaint();
            return;
        }

        try {
            // We do want the link object here... If another thread is
            // using the link, wait.
            ClientLink l = linkManager.getLink(true);

            if (l == null) {
                System.err.println("LinkLayer.drawingComplete: unable to get link.");
                return;
            }

            synchronized (l) {
                LinkActionList lal = new LinkActionList(l, new LinkProperties());

                if (action.isMask(OMAction.ADD_GRAPHIC_MASK)
                        || action.isMask(OMAction.UPDATE_GRAPHIC_MASK)) {
                    lal.writeGraphicGestureHeader(action.getValue());
                    LinkGraphic.write(omg, l);
                } else {
                    // This shouldn't ever get called with a null lp
                    // properties object. If the object is new or
                    // doesn't have an ID, the upper paragraph will
                    // get called.
                    lal.modifyGraphic(action.getValue(), lp);
                }
                lal.end(Link.END_TOTAL);
            }

            l.readAndParse(getProjection(), currentGenerator);
            linkManager.finLink();

        } catch (UnknownHostException uhe) {
            Debug.error("LinkLayer: unknown host!");
        } catch (java.io.IOException ioe) {
            Debug.error("LinkLayer: Communication error between " + getName()
                    + " layer\nand Link Server: Host: " + host + ", Port: "
                    + port + "LinkLayer: IOException contacting server!\n"
                    + ioe.getMessage());

            linkManager.resetLink();

            if (!quiet) {
                fireRequestMessage("Communication error between " + getName()
                        + " layer\nand Link Server: Host: " + host + ", Port: "
                        + port);
            }

        }
        doPrepare();
    }

    ///////////////////////////////////////////////////
    // Tool interface - to put controls for the layer
    // on the tool bar.
    protected Container gui;
    protected JButton rrButton, eZone;

    /**
     * The retrieval tool's interface. This is added to the tool bar.
     * 
     * @return String The key for this tool.
     */
    public Container getFace() {
        if (gui == null) {
            gui = new JPanel();

            rrButton = new JButton("RR");
            rrButton.setToolTipText("Create Range Ring");
            rrButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    DrawingTool dt = getDrawingTool();
                    GraphicAttributes ga = new GraphicAttributes();
                    ga.setLinePaint(Color.yellow);
                    if (dt != null) {
                        OMRangeRings rr = (OMRangeRings) getDrawingTool().create("com.bbn.openmap.omGraphics.OMRangeRings",
                                ga,
                                layer);
                        if (rr != null) {
                            //                          rr.setInterval(25, Length.MILE);
                        } else {
                            Debug.error("AmpLinkLayer: Drawing tool can't create OMRangeRings");
                        }
                    } else {
                        Debug.output("AmpLinkLayer can't find a drawing tool");
                    }
                }
            });

            eZone = new JButton("EZ");
            eZone.setToolTipText("Create Exclusion Zone");
            eZone.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    DrawingTool dt = getDrawingTool();
                    GraphicAttributes ga = new GraphicAttributes();
                    ga.setRenderType(OMGraphic.RENDERTYPE_LATLON);
                    ga.setLinePaint(Color.red);
                    if (dt != null) {
                        OMCircle circle = (OMCircle) getDrawingTool().create("com.bbn.openmap.omGraphics.OMCircle",
                                ga,
                                layer);
                        if (circle == null) {
                            Debug.error("AmpLinkLayer: Drawing tool can't create Exclusion Zones");
                        }
                    } else {
                        Debug.output("AmpLinkLayer can't find a drawing tool");
                    }
                }
            });

            gui.add(rrButton);
            gui.add(eZone);
        }
        return gui;
    }

    protected String key = "AMPControls";

    /**
     * The retrieval key for this tool
     * 
     * @return String The key for this tool.
     */
    public String getKey() {
        return key;
    }

    /**
     * Set the retrieval key for this tool
     * 
     * @param aKey The key for this tool.
     */
    public void setKey(String aKey) {
        key = aKey;
    }

    public void findAndInit(Object someObj) {
        if (someObj instanceof DrawingTool) {
            Debug.message("link", "AmpLinkLayer: found a drawing tool");
            setDrawingTool((DrawingTool) someObj);
        }
    }

    public void findAndUndo(Object someObj) {
        if (someObj instanceof DrawingTool) {
            if (getDrawingTool() == (DrawingTool) someObj) {
                setDrawingTool(null);
            }
        }
    }

    public void setDrawingTool(DrawingTool dt) {
        drawingTool = dt;
    }

    public DrawingTool getDrawingTool() {
        return drawingTool;
    }

    public int getOrientation() {
        return orientation;
    }

    public void setOrientation(int orientation) {
        this.orientation = orientation;
    }
}