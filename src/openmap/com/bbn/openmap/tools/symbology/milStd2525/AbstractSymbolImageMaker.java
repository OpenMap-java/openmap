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
//$RCSfile: AbstractSymbolImageMaker.java,v $
//$Revision: 1.1 $
//$Date: 2004/12/10 14:17:11 $
//$Author: dietrick $
//
//**********************************************************************

package com.bbn.openmap.tools.symbology.milStd2525;

import java.awt.Dimension;
import java.awt.Paint;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import javax.swing.ImageIcon;

import com.bbn.openmap.OMComponent;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PropUtils;

public abstract class AbstractSymbolImageMaker extends OMComponent implements
        SymbolImageMaker {

    protected String dataPath;
    protected Paint background;

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
     * @return
     * @throws MalformedURLException
     */
    protected URL getFileURL(String code) throws MalformedURLException {
        code = massageCode(code);
        code = dataPath + ((dataPath != null && dataPath != "") ? "/" : "")
                + code + ".svg";
        if (Debug.debugging("symbology")) {
            Debug.output("AbstractSymbolImageMaker: code massaged to " + code);
        }
        URL ret = PropUtils.getResourceOrFileOrURL(code);
        return ret;
    }

    /**
     * @param code
     * @return
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
    public abstract ImageIcon getIcon(String code, Dimension di);

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