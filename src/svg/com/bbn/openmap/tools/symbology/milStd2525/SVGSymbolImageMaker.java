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
//$RCSfile: SVGSymbolImageMaker.java,v $
//$Revision: 1.5 $
//$Date: 2005/02/11 22:17:38 $
//$Author: dietrick $
//
//**********************************************************************

package com.bbn.openmap.tools.symbology.milStd2525;

import java.awt.Dimension;
import java.io.IOException;
import java.net.URL;

import javax.swing.ImageIcon;

import org.apache.batik.transcoder.TranscoderException;

import com.bbn.openmap.tools.svg.SVGRasterizer;
import com.bbn.openmap.util.Debug;

public class SVGSymbolImageMaker extends BasicSymbolImageMaker {

    protected SVGRasterizer rasterizer;

    /**
     *  
     */
    public SVGSymbolImageMaker() {
        this("");
    }

    public SVGSymbolImageMaker(String dataPath) {
        rasterizer = new SVGRasterizer();
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
                Debug.output("SVGSymbolImageMaker: Trying to create " + fileURL);
            }
            rasterizer = new SVGRasterizer(fileURL);
            return new ImageIcon(rasterizer.createBufferedImage(di));
        } catch (TranscoderException e) {
            Debug.output("FYI (exception handled):");
            e.printStackTrace();
        } catch (IOException e) {
            Debug.output("FYI (exception handled):");
            e.printStackTrace();
        } catch (NullPointerException npe) {
            if (Debug.debugging("symbology")) {
                Debug.output("SVGSymbolImageMaker: didn't find data for image");
                npe.printStackTrace();
            }
        }
        return null;
    }
    
    public String getFileExtension() {
        return ".svg";
    }
}