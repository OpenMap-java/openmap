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

package com.bbn.openmap.layer.image;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.bbn.openmap.Layer;
import com.bbn.openmap.gui.MiniBrowser;
import com.bbn.openmap.image.ImageServerConstants;
import com.bbn.openmap.layer.OMGraphicHandlerLayer;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.OMRaster;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.PropUtils;

/**
 * This class asks for an image from a web server. How it asks for that image is
 * what is abstract.
 */
public abstract class WebImageLayer extends OMGraphicHandlerLayer implements ImageServerConstants {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static Logger logger = Logger.getLogger("com.bbn.openmap.layer.image.WebImageLayer");

    /**
     * Create the query to be sent to the server, based on current settings.
     */
    public abstract String createQueryString(Projection p);

    public abstract String getServerName();

    /**
     * OMGraphicHandlerLayer method that gets called when the projection
     * changes. For this class, it takes the query string, contacts the server
     * and handles the image data returned from the server.
     * 
     * @return OMGraphicList to display for the current projection.
     */
    public OMGraphicList prepare() {
        OMGraphicList list = new OMGraphicList();
        Projection p = getProjection();

        String urlString = createQueryString(p);

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("WebImagePlugIn.getRectangle() with \"" + urlString + "\"");
        }

        if (urlString == null) {
            return list;
        }

        java.net.URL url = null;

        try {
            url = new java.net.URL(urlString);
            java.net.HttpURLConnection urlc = (java.net.HttpURLConnection) url.openConnection();

            if (logger.isLoggable(Level.FINE)) {
                logger.fine("url content type: " + urlc.getContentType());
            }

            if (urlc == null || urlc.getContentType() == null) {
                fireRequestMessage(getName() + ": unable to connect to " + getServerName());
                return list;
            }

            // text
            if (urlc.getContentType().startsWith("text")) {
                java.io.BufferedReader bin = new java.io.BufferedReader(new java.io.InputStreamReader(urlc.getInputStream()));
                String st;
                StringBuffer message = new StringBuffer();
                while ((st = bin.readLine()) != null) {
                    message.append(st);
                }

                // Debug.error(message.toString());
                // How about we toss the message out to the user
                // instead?
                fireRequestMessage(message.toString());

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

                OMRaster image = new OMRaster((int) 0, (int) 0, ii);
                list.add(image);
            } // end if image
        } catch (java.net.MalformedURLException murle) {
            logger.warning("WebImagePlugIn: URL \"" + urlString + "\" is malformed.");
        } catch (java.io.IOException ioe) {
            JOptionPane.showMessageDialog(null, getName() + ":\n\n   Couldn't connect to "
                    + getServerName(), "Connection Problem", JOptionPane.INFORMATION_MESSAGE);

        }

        list.generate(p);
        return list;
    } // end prepare

    @Override
    public java.awt.Component getGUI() {
        JPanel panel = new JPanel(new GridLayout(0, 1));
        JButton parameterButton = new JButton(i18n.get(WebImageLayer.class, "Adjust_Parameters", "Adjust Parameters"));

        parameterButton.setActionCommand(Layer.DisplayPropertiesCmd);
        parameterButton.addActionListener(this);

        JButton viewQueryButton = new JButton(i18n.get(WebImageLayer.class, "View_Current_Query", "View Current Query"));
        JButton redrawButton = new JButton(i18n.get(WebImageLayer.class, "Query_Server", "Query Server"));

        viewQueryButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {

                String query = createQueryString(getProjection());
                List<String> queryStrings = PropUtils.parseMarkers(query, "&");
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
                            e.printStackTrace();
                        }
                    }

                } else {
                    JOptionPane.showMessageDialog(null, updatedQuery.toString(), "Current Query for "
                            + getName(), JOptionPane.INFORMATION_MESSAGE);
                }

            }
        });

        redrawButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                doPrepare();
            }
        });

        panel.add(parameterButton);
        panel.add(viewQueryButton);
        panel.add(redrawButton);
        return panel;
    }

} // end WebImageLayer
