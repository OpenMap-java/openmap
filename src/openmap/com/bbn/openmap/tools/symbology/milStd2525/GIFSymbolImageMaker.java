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
//$Revision: 1.2 $
//$Date: 2004/12/10 14:17:11 $
//$Author: dietrick $
//
//**********************************************************************

package com.bbn.openmap.tools.symbology.milStd2525;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.swing.ImageIcon;

import com.bbn.openmap.image.BufferedImageHelper;
import com.bbn.openmap.util.Debug;

public class GIFSymbolImageMaker extends AbstractSymbolImageMaker {

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
    
}