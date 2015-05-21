// **********************************************************************
//
//<copyright>
//
//BBN Technologies, a Verizon Company
//10 Moulton Street
//Cambridge, MA 02138
//(617) 873-8000
//
//Copyright (C) BBNT Solutions LLC. All rights reserved.
//
//</copyright>
//**********************************************************************
//
//$Source:
///cvs/darwars/ambush/aar/src/com/bbn/ambush/mission/MissionHandler.java,v
//$
//$RCSfile: BasicSymbolImageMaker.java,v $
//$Revision: 1.5 $
//$Date: 2008/01/29 22:04:13 $
//$Author: dietrick $
//
//**********************************************************************

package com.bbn.openmap.tools.symbology.milStd2525;

import java.awt.Dimension;
import java.awt.Paint;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import javax.swing.ImageIcon;

import com.bbn.openmap.OMComponent;
import com.bbn.openmap.image.BufferedImageHelper;
import com.bbn.openmap.image.ImageScaler;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PropUtils;

public class BasicSymbolImageMaker extends OMComponent implements
        SymbolImageMaker {

    protected String dataPath;
    protected Paint background;

    public BasicSymbolImageMaker() {
        this(null);
    }

    public BasicSymbolImageMaker(String dataPath) {
        this.dataPath = (dataPath != null ? dataPath : "");
    }

    public void setProperties(String prefix, Properties props) {
        super.setProperties(prefix, props);
        prefix = PropUtils.getScopedPropertyPrefix(prefix);

        setBackground(PropUtils.parseColorFromProperties(props, prefix
                + BackgroundPaintProperty, "0x00000000"));
        String dataPathString = props.getProperty(prefix + DataPathProperty);
        if (dataPathString != null) {
            setDataPath(dataPathString);
        } else {
            if (Debug.debugging("symbology")) {
                Debug.output(getClass().getName()
                        + " data path ("
                        + prefix
                        + DataPathProperty
                        + ") not set in properties, might be OK if symbol data stored in jar in classpath.");
            }
        }
    }

    /**
     * @param code
     * @return URL for a file containing the symbol for the given code, null if
     *         it's not available.
     * @throws MalformedURLException
     */
    protected URL getFileURL(String code) throws MalformedURLException {
        code = massageCode(code);
        code = dataPath + ((dataPath != null && dataPath.length() > 0) ? "/" : "")
                + code + getFileExtension();
        if (Debug.debugging("symbology")) {
            Debug.output("AbstractSymbolImageMaker: code massaged to " + code);
        }
        URL ret = PropUtils.getResourceOrFileOrURL(code);
        return ret;
    }

    /**
     * Return the file extension of this particular SymbolImageMaker, added to
     * the symbol name after the code has been massaged into a file name. The
     * BasicSymbolImageLaker doesn't add an extension. If you override, include
     * the dot at the beginning of the return string.
     */
    public String getFileExtension() {
        return "";
    }

    /**
     * @param code
     * @return code that has any wildcard characters changed for the sake of the
     *         symbol database.
     */
    protected String massageCode(String code) {
        code = code.replace('*', '-').toLowerCase();

        return code;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.bbn.openmap.tools.symbology.milStd2525.SymbolImageMaker#getIcon(java.lang.String,
     *      java.awt.Dimension)
     */
    public ImageIcon getIcon(String code, Dimension di) {

        if (code == null) {
            return null;
        }

        try {
            URL fileURL = getFileURL(code);
            if (Debug.debugging("symbology")) {
                Debug.output("BasicSymbolImageMaker: Trying to create "
                        + fileURL);
            }

            BufferedImage bi = BufferedImageHelper.getBufferedImage(fileURL);
            return new ImageIcon(ImageScaler.getOptimalScalingImage(bi, (int) di.getWidth(),
                    (int) di.getHeight()));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException npe) {
            if (Debug.debugging("symbology")) {
                Debug.output("BasicSymbolImageMaker: didn't find data for image: " + code);
                if (Debug.debugging("symbologydetail")) {
                    npe.printStackTrace();
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.bbn.openmap.tools.symbology.milStd2525.SymbolImageMaker#setDataPath(java.lang.String)
     */
    public void setDataPath(String dataPath) {
        this.dataPath = dataPath;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.bbn.openmap.tools.symbology.milStd2525.SymbolImageMaker#setBackground(java.awt.Paint)
     */
    public void setBackground(Paint p) {
        this.background = p;
    }

    /**
     * @return Returns the background.
     */
    public Paint getBackground() {
        return background;
    }

    /**
     * @return Returns the dataPath.
     */
    public String getDataPath() {
        return dataPath;
    }
}