// Bart 20060831 -> i18n

/* **********************************************************************
 * $Source: /home/cvs/nodus/src/com/bbn/openmap/plugin/WebImagePlugIn.java,v $
 * $Revision: 1.2 $ 
 * $Date: 2006-10-25 12:21:54 $ 
 * $Author: jourquin $
 *
 * Code provided by Raj Singh from Syncline, rs@syncline.com
 * Updates provided by Holger Kohler, Holger.Kohler@dsto.defence.gov.au
 * *********************************************************************
 */

package com.bbn.openmap.plugin;

import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.bbn.openmap.Environment;
import com.bbn.openmap.I18n;
import com.bbn.openmap.Layer;
import com.bbn.openmap.gui.MiniBrowser;
import com.bbn.openmap.image.ImageServerConstants;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.OMRaster;
import com.bbn.openmap.omGraphics.OMScalingRaster;
import com.bbn.openmap.omGraphics.OMWarpingImage;
import com.bbn.openmap.proj.LLXY;
import com.bbn.openmap.proj.Proj;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.proj.coords.LatLonGCT;
import com.bbn.openmap.util.PropUtils;

/**
 * This class asks for an image from a web server. How it asks for that image is
 * what is abstract.
 */
public abstract class WebImagePlugIn extends AbstractPlugIn implements ImageServerConstants {

    /** For convenience. */
    protected PlugInLayer layer = null;

    /** The last projection object received. */
    protected Projection currentProjection = null;

    /**
     * Create the query to be sent to the server, based on current settings.
     */
    public abstract String createQueryString(Projection p);

    // I18N mechanism
    static I18n i18n = Environment.getI18n();

    public static Logger logger = Logger.getLogger("com.bbn.openmap.plugin.WebImagePlugIn");

    /**
     * The getRectangle call is the main call into the PlugIn module. The module
     * is expected to fill the graphics list with objects that are within the
     * screen parameters passed.
     * 
     * @param p projection of the screen, holding scale, center coords, height,
     *        width.
     */
    @Override
    public OMGraphicList getRectangle(Projection p) {
        OMGraphicList list = new OMGraphicList();
        currentProjection = p;

        Point2D ul = p.getUpperLeft();
        Point2D lr = p.getLowerRight();

        if (lr.getX() < ul.getX()) {
            // Dateline! Make 2 queries, one for each side.
            Point2D dateline1 = p.forward(ul.getY(), 179.9999);
            Point2D dateline2 = p.forward(ul.getY(), -179.9999);
            int w1 = (int) dateline1.getX();
            int w2 = p.getWidth() - (int) dateline2.getX() - 1;

            int h = p.getHeight();
            Point2D c1 = p.inverse(w1 / 2, h / 2);
            Point2D c2 = p.inverse((w2 / 2) + w1 + 1, h / 2);

            Proj p1 = (Proj) p.makeClone();
            Proj p2 = (Proj) p.makeClone();

            p1.setCenter(c1);
            p1.setWidth(w1);

            p2.setCenter(c2);
            p2.setWidth(w2);

            fetchImageAndAddToList(p1, list);
            fetchImageAndAddToList(p2, list);

        } else {
            fetchImageAndAddToList(p, list);
        }

        list.generate(p);
        return list;
    } // end prepare

    /**
     * Image fetching code, where the query is created based on the provided
     * projection.
     * 
     * @param p projection that image needs to cover
     * @param list the OMGraphicList that any new image OMGraphics need to be
     *        added to for the map
     */
    protected void fetchImageAndAddToList(Projection p, OMGraphicList list) {
        String urlString = createQueryString(p);

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("WebImagePlugIn.getRectangle() with \"" + urlString + "\"");
        }

        if (urlString == null) {
            return;
        }

        java.net.URL url = null;

        try {
            url = new java.net.URL(urlString);
            java.net.HttpURLConnection urlc = (java.net.HttpURLConnection) url.openConnection();

            if (logger.isLoggable(Level.FINE)) {
                logger.fine("url content type: " + urlc.getContentType());
            }

            if (urlc == null || urlc.getContentType() == null) {
                logger.info(" unable to connect to " + urlString);
                return;
            }

            // text
            if (urlc.getContentType().startsWith("text")) {
                java.io.BufferedReader bin = new java.io.BufferedReader(new java.io.InputStreamReader(urlc.getInputStream()));
                String st;
                StringBuffer message = new StringBuffer();
                while ((st = bin.readLine()) != null) {
                    message.append(st);
                }

                logger.info("Received text from\n" + urlString + ":\n" + message.toString());

                // image
            } else if (urlc.getContentType().startsWith("image")) {

                // the best way, no reconnect, but can be an
                // additional 'in memory' image
                InputStream in = urlc.getInputStream();
                // ------- Testing without this
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                int buflen = 2048; // 2k blocks
                byte buf[] = new byte[buflen];
                int len = -1;
                while ((len = in.read(buf, 0, buflen)) != -1) {
                    out.write(buf, 0, len);
                }
                out.flush();
                out.close();
                ImageIcon ii = new ImageIcon(out.toByteArray());

                // -------- To here, replaced by two lines below...

                // DFD - I've seen problems with these lines below handling PNG
                // images, and with some servers with some coverages, like there
                // was something in the image under certain conditions that made
                // it tough to view. So while it might be more memory efficient
                // to do the code below, we'll error on the side of correctness
                // until we figure out what's going on.

                // FileCacheImageInputStream fciis = new
                // FileCacheImageInputStream(in, null);
                // BufferedImage ii = ImageIO.read(fciis);

                if (p instanceof LLXY) {
                    // EPSG:4326, just put it on the screen
                    OMRaster image = new OMRaster((int) 0, (int) 0, ii);
                    list.add(image);
                } else {
                    Point2D ul = p.getUpperLeft();
                    Point2D lr = p.getLowerRight();
                    OMScalingRaster omsr = new OMScalingRaster(ul.getY(), ul.getX(), lr.getY(), lr.getX(), ii);

                    OMWarpingImage omwi = new OMWarpingImage(omsr, LatLonGCT.INSTANCE);
                    list.add(omwi);
                }

            } // end if image
        } catch (java.net.MalformedURLException murle) {
            logger.warning("WebImagePlugIn: URL \"" + urlString + "\" is malformed.");
        } catch (java.io.IOException ioe) {
            handleMessage("Couldn't connect to " + getServerName());
        }
    }

    protected void handleMessage(final String message) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                JOptionPane.showMessageDialog(null, getName() + ":\n\n   "
                        + message, "Connection Problem", JOptionPane.INFORMATION_MESSAGE);
            }
        });
    }

    public abstract String getServerName();

    @Override
    public java.awt.Component getGUI() {
        JPanel panel = new JPanel(new GridLayout(0, 1));
        JButton parameterButton = new JButton(i18n.get(WebImagePlugIn.class, "Adjust_Parameters", "Adjust Parameters"));

        parameterButton.setActionCommand(Layer.DisplayPropertiesCmd);

        if (layer != null) {
            parameterButton.addActionListener(layer);
        }

        JButton viewQueryButton = new JButton(i18n.get(WebImagePlugIn.class, "View_Current_Query", "View Current Query"));

        viewQueryButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if (layer != null) {
                    String query = createQueryString(currentProjection);
                    Vector<String> queryStrings = PropUtils.parseMarkers(query, "&");
                    StringBuffer updatedQuery = new StringBuffer();
                    Iterator<String> it = queryStrings.iterator();
                    if (it.hasNext()) {
                        updatedQuery.append(it.next());
                    }
                    while (it.hasNext()) {
                        updatedQuery.append("&\n   ");
                        updatedQuery.append(it.next());
                    }

                    if (logger.isLoggable(Level.FINE)) {
                        String stb = "Send Query to Browser";
                        String ok = "OK";

                        Object[] options = new Object[] { stb, ok };
                        int selectedVal = JOptionPane.showOptionDialog(null, updatedQuery, "Current Query for "
                                + getName(), JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, ok);

                        if (selectedVal == 0) {
                            try {
                                new MiniBrowser(new URL(query));
                            } catch (MalformedURLException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }

                    } else {
                        JOptionPane.showMessageDialog(null, updatedQuery.toString(), "Current Query for "
                                + getName(), JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            }
        });

        redrawButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if (layer != null) {
                    layer.doPrepare();
                }
            }
        });

        redrawButton.setEnabled(layer != null);

        panel.add(parameterButton);
        panel.add(viewQueryButton);
        panel.add(redrawButton);
        return panel;
    }

    protected JButton redrawButton = new JButton(i18n.get(WebImagePlugIn.class, "Query_Server", "Query Server"));

    protected JOptionPane messageWindow = new JOptionPane();

    /**
     * Set the component that this PlugIn uses as a grip to the map.
     */
    @Override
    public void setComponent(Component comp) {
        super.setComponent(comp);
        if (comp instanceof PlugInLayer) {
            layer = (PlugInLayer) comp;
        } else {
            layer = null;
        }
        redrawButton.setEnabled(layer != null);
    }

} // end WMSPlugin
