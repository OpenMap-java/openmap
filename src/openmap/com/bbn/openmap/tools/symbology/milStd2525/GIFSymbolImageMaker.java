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
//$RCSfile: GIFSymbolImageMaker.java,v $
//$Revision: 1.1 $
//$Date: 2004/12/08 01:08:31 $
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

import javax.swing.ImageIcon;

import com.bbn.openmap.image.BufferedImageHelper;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PropUtils;

public class GIFSymbolImageMaker implements SymbolImageMaker {

    protected String dataPath;

    /**
     *  
     */
    public GIFSymbolImageMaker() {
        this("");
    }

    public GIFSymbolImageMaker(String dataPath) {
        this.dataPath = (dataPath != null ? dataPath : "");
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.bbn.openmap.tools.symbology.milStd2525.SymbolImageMaker#getIcon(java.lang.String,
     *      java.awt.Dimension)
     */
    public ImageIcon getIcon(String code, Dimension di) {

        try {
            URL fileURL = getFileURL(code);
            if (Debug.debugging("symbology")) {
                Debug.output("GIFSymbolImageMaker: Trying to create " + fileURL);
            }
            
            BufferedImage bi = BufferedImageHelper.getBufferedImage(fileURL);
            return new ImageIcon(bi.getScaledInstance((int)di.getWidth(), (int)di.getHeight(), java.awt.Image.SCALE_SMOOTH));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException npe) {
            if (Debug.debugging("symbology")) {
                Debug.output("GIFSymbolImageMaker: didn't find data for image");
                npe.printStackTrace();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * @param code
     * @return
     * @throws MalformedURLException
     */
    protected URL getFileURL(String code) throws MalformedURLException {
        code = massageCode(code);
        code = dataPath + code + ".gif";
        if (Debug.debugging("symbology")) {
            Debug.output("GIFSymbolImageMaker: code massaged to " + code);
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
     * @see com.bbn.openmap.tools.symbology.milStd2525.SymbolImageMaker#setBackground(java.awt.Paint)
     */
    public void setBackground(Paint p) {

    }

}