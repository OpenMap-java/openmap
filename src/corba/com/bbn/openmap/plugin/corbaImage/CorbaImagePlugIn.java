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
// $Source: /cvs/distapps/openmap/src/corba/com/bbn/openmap/plugin/corbaImage/CorbaImagePlugIn.java,v $
// $RCSfile: CorbaImagePlugIn.java,v $
// $Revision: 1.6 $
// $Date: 2005/12/09 21:09:15 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.plugin.corbaImage;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import javax.swing.ImageIcon;

import com.bbn.openmap.image.ImageServerConstants;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.OMRaster;
import com.bbn.openmap.plugin.WebImagePlugIn;
import com.bbn.openmap.plugin.corbaImage.corbaImageServer.Server;
import com.bbn.openmap.plugin.corbaImage.corbaImageServer.ServerHelper;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PropUtils;

/** 
 *
 * This class asks for an image from a CorbaImageServer.  There will
 * be some CORBA setup involved with using this PlugIn.  You'll need
 * to run some kind of name service, and register the CorbaImageServer
 * with it, or have the server write out its IOR file to a location
 * reachable from this client. <P>
 *
 * The query to the CorbaImageServer looks something like this:
 * <P>
 * REQUEST=MAP&PROJTYPE=projection_type_value&SCALE=scale_value&LAT=center_latitude&LON=center_longitude&HEIGHT=map_pixel_height&WIDTH=map_pixel_width&FORMAT=image_format&TRANSPARENT=true|false&BGCOLOR=background_color
 *
 * <P> The projection information will be entered automatically by the
 * plugin based on the projection it receives from the MapBean.  The
 * other parameters can be entered in the properties for the layer.
 *
 * #For the plugin layer
 * pluginlayer.class=com.bbn.openmap.plugin.PlugInLayer
 * pluginlayer.prettyName=Whatever
 * pluginlayer.plugin=com.bbn.openmap.plugin.CorbaImage.CorbaImagePlugIn
 * pluginlayer.plugin.name=Corba Naming Service Name (needed if ior is not provided)
 * pluginlayer.plugin.ior=URL to ior file (needed if name is not provided)
 * pluginlayer.plugin.format=image format (JPEG, GIF from WMTConstants.java)
 * pluginlayer.plugin.transparent=true or false, depends on imageformat
 * pluginlayer.plugin.backgroundColor=RGB hex string (RRGGBB) 
 */
public class CorbaImagePlugIn extends WebImagePlugIn implements ImageServerConstants {

    protected String queryHeader = null;
    protected String imageFormat = null;
    protected String backgroundColor = null;
    protected boolean transparent = false;

    public final static String ImageFormatProperty = "format";
    public final static String BackgroundColorProperty = "backgroundColor";
    public final static String TransparentProperty = "transparent";

    /** The property specifying the IOR URL. */
    public static final String iorUrlProperty = "ior";
    /** The name of the server, using the name service.*/
    public static final String namingProperty = "name";
    /** The Server. */
    protected transient Server server = null;
    /** The URL used for the IOR, to connect to the server that way. */
    protected URL iorURL = null;
    /** The string used for the CORBA naming service. */
    protected String naming = null;

    public CorbaImagePlugIn() {}

    /**
     * When a projection is received, translate it into a valid
     * request for a SimpleHttpImageServer, and then return the image
     * received back from it.
     * 
     * @param p projection of the screen, holding scale, center
     * coords, height, width.  
     * @return an OMGraphicList with an image received from a CorbaImage.  
     */
    public String createQueryString(Projection p) {

        if (queryHeader == null) {
            return null;
        }

        StringBuffer buf = new StringBuffer(queryHeader);
        buf.append(REQUEST + "=" + MAP + "&");

        if (p != null) {
            Point2D center = p.getCenter();
            buf.append(PROJTYPE + "=" + p.getName() + "&" +
                       SCALE + "=" + p.getScale() + "&" +
                       LAT + "=" + center.getY() + "&" +
                       LON + "=" + center.getX() + "&" +
                       HEIGHT + "=" + p.getHeight() + "&" +
                       WIDTH + "=" + p.getWidth());
        } else {
            buf.append(PROJTYPE + "=name_undefined&" +
                       SCALE + "=scale_undefined&" +
                       LAT + "=center_lat_undefined&" +
                       LON + "=center_lon_undefined&" +
                       HEIGHT + "=height_undefined&" +
                       WIDTH + "=width_undefined");
        }

        if (imageFormat != null) {
            buf.append("&" + FORMAT + "=" + imageFormat);
        }

        if (transparent) {
            buf.append("&" + TRANSPARENT + "=true");
        }

        if (backgroundColor != null) {
            buf.append("&" + BGCOLOR + "=" + backgroundColor);
        }

        String layers = getLayerMarkers();
        if (layers != null) {
            buf.append("&" + layers);
        }

        return buf.toString();
    }

    public String getServerName() {
        return queryHeader;
    }

    public String getLayerMarkers() {
        // Not implemented - should be a list that can be set by the user.
        return null;
    }

    /**
     * The getRectangle call is the main call into the PlugIn module.
     * The module is expected to fill the graphics list with objects
     * that are within the screen parameters passed.
     *
     * @param p projection of the screen, holding scale, center
     * coords, height, width.
     */
    public OMGraphicList getRectangle(Projection p) {
        OMGraphicList list = new OMGraphicList();
        
        currentProjection = p;

        String urlString = createQueryString(p);

        if (Debug.debugging("cis")) {
            Debug.output("CorbaImagePlugIn.getRectangle() with \"" + urlString + "\"");
        }

        if (urlString == null) {
            return list;
        }

        Server serv = getServer();
        if (serv == null) return null;
        byte[] imageData;

        Debug.message("cis", "CorbaImagePlugIn: requesting image data from server...");

        try {
            imageData = serv.getImage(urlString);

            if (Debug.debugging("cis")){
                Debug.output("CorbaImagePlugIn: got image data length " + 
                             imageData.length);
            }
            ImageIcon ii = new ImageIcon(imageData);
            OMRaster image = new OMRaster((int)0, (int)0, ii);
            list.add(image);

        } catch (org.omg.CORBA.SystemException e){
            handleCORBAError(e);
            server = null;
        }

        list.generate(p);
        return list;
    } //end getRectangle

    /**
     * PropertyConsumer method.
     */
    public void setProperties(String prefix, Properties setList) {
        super.setProperties(prefix, setList);

        prefix = PropUtils.getScopedPropertyPrefix(prefix);
        
        imageFormat = setList.getProperty(prefix + ImageFormatProperty);
        transparent =  PropUtils.booleanFromProperties(setList, prefix + TransparentProperty, false);

        backgroundColor = setList.getProperty(prefix + BackgroundColorProperty);

        String url = setList.getProperty(prefix + iorUrlProperty);
        if (url != null) {
            try {
                iorURL = PropUtils.getResourceOrFileOrURL(url);
            } catch (MalformedURLException e) {
                throw new IllegalArgumentException("\"" + url + "\""
                                                   + " is malformed.");
            }
        }

        naming = setList.getProperty(prefix + namingProperty);
        Debug.message("cis", "CorbaImagePlugIn.setProperties(): naming = " + naming);

        queryHeader = "";

        if (Debug.debugging("plugin")){
            Debug.output("CorbaImagePlugIn: set up with header \"" + queryHeader + "\"");
        }
    }

    /**
     * PropertyConsumer method.
     */
    public Properties getProperties(Properties getList) {
        getList = super.getProperties(getList);

        String prefix = PropUtils.getScopedPropertyPrefix(this);
        String iorString = null;
        if (iorURL != null) {
            iorString = iorURL.toString();
        }
        getList.put(prefix + iorUrlProperty, PropUtils.unnull(iorString));
        getList.put(prefix + namingProperty, PropUtils.unnull(naming));
        getList.put(prefix + ImageFormatProperty, PropUtils.unnull(imageFormat));
        getList.put(prefix + TransparentProperty, new Boolean(transparent).toString());
        getList.put(prefix + BackgroundColorProperty, PropUtils.unnull(backgroundColor));
        return getList;
    }    

    /**
     * PropertyConsumer method.
     */
    public Properties getPropertyInfo(Properties list) {
        list = super.getPropertyInfo(list);
        list.put(iorUrlProperty, "The URL of the ior file for the server.");
        list.put(namingProperty, "The Naming Services Name of the server.");
        list.put(ImageFormatProperty, "Image format (JPEG|GIF|PPM|PNG)");
        list.put(TransparentProperty, "Whether the background should be transparent");
        list.put(TransparentProperty + ScopedEditorProperty,
                 "com.bbn.openmap.util.propertyEditor.YesNoPropertyEditor");

        list.put(BackgroundColorProperty, "Background color for image.");
        list.put(BackgroundColorProperty + ScopedEditorProperty, 
                 "com.bbn.openmap.util.propertyEditor.ColorPropertyEditor");
        return list;
    }

    /**
     * Set the name used for the CORBA naming service.
     */
    public void setNaming(String CORBAName){
        naming = CORBAName;
    }

    /**
     * Get the name used for the CORBA naming service.
     */
    public String getNaming(){
        return naming;
    }

    /**
     * If you want to connect to the server using an ior, set the URL
     * where it is located.  
     */
    public void setIorURL(URL iorurl){
        iorURL = iorurl;
    }

    /**
     * Get the URL for the ior.
     */
    public URL getIorURL(){
        return iorURL;
    }

    ////////////////  Corba management

    /**
     * get the server proxy.
     *
     * @return Server server or null if error.
     *
     */
    public Server getServer () {
        if (server == null)
            initServer();
        return server;
    }

    /**
     * bind to the server.
     */
    private void initServer() {
        String ior = null;
        org.omg.CORBA.Object object = null;

        com.bbn.openmap.util.corba.CORBASupport cs = 
            new com.bbn.openmap.util.corba.CORBASupport();

        try {
            object = cs.readIOR(iorURL);
            server = ServerHelper.narrow(object);
        } catch (IOException ioe) {
            if (Debug.debugging("cis")) {
                Debug.output(getName() + "(CIS).initServer() IO Exception with ior: " + iorURL);
            }
            server = null;
            return;
        }

        if (server == null) {
            object = cs.resolveName(naming);
            
            if (object != null) {
                server = ServerHelper.narrow(object);
                if (Debug.debugging("cis")) {
                    Debug.output("Have a CorbaImageServer:" );
                    Debug.output("*** Server: is a " + 
                                 server.getClass().getName() + "\n" + 
                                 server);
                }
            } 
        }

        if (Debug.debugging("cis")) {
            if (server == null) {
                Debug.error("CIS.initServer: null server!\n  IOR=" + ior + "\n  Name = " + naming);
            } else {
                Debug.output("CIS: server is golden.");
            }
        }
    }
    
    protected void handleCORBAError(org.omg.CORBA.SystemException e){
        // don't freak out if we were only interrupted...
        if (e.toString().indexOf("InterruptedIOException") != -1) {
            Debug.error("CorbaImagePlugIn server communication interrupted!");
        } else {
            Debug.error("CorbaImagePlugIn caught CORBA exception: " + e + "\n" +
                        "CorbaImagePlugIn Exception class: " + 
                        e.getClass().getName() + "\nSpecific Message: " +
                        e.getMessage());
            e.printStackTrace();
        }

        server = null;// dontcha just love CORBA? reinit later
    }

}