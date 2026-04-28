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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/mif/MIFLayer.java,v $
// $RCSfile: MIFLayer.java,v $
// $Revision: 1.10 $
// $Date: 2005/05/23 20:06:01 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.mif;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Properties;

import com.bbn.openmap.layer.OMGraphicHandlerLayer;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PropUtils;

/**
 * An OpenMap Layer that displays MapInfo Interchange Format (MIF)
 * files Currently only a subset of the possible MIF options is
 * supported. Specifically the PLine and Region options with their
 * associated parameters however maps will be reproduced exactly as
 * they appear in the MapInfo Professional product.
 * 
 * 26th January 2004 - added support for TEXT and POINT
 */
public class MIFLayer extends OMGraphicHandlerLayer {
    public final static String MIF_FileProperty = "mifFile";
    public final static String textVisibleProperty = "textVisible";
    public final static String pointVisibleProperty = "pointVisible";

    boolean accurate = true;
    MIFLoader mifl = null;

    public MIFLayer() {
        setProjectionChangePolicy(new com.bbn.openmap.layer.policy.ListResetPCPolicy(this));
    }

    /**
     * Initializes this layer from the given properties.
     * 
     * @param props the <code>Properties</code> holding settings for
     *        this layer. Only the file property is used by the layer.
     *        This is the MIF file that we will decode.
     */
    public void setProperties(String prefix, Properties props) {
        super.setProperties(prefix, props);

        prefix = PropUtils.getScopedPropertyPrefix(prefix);

        String mifFileName = props.getProperty(prefix + MIF_FileProperty);
        float textVisible = PropUtils.floatFromProperties(props, prefix
                + textVisibleProperty, -1f);
        float pointVisible = PropUtils.floatFromProperties(props, prefix
                + pointVisibleProperty, -1f);

        try {

            URL fileURL = PropUtils.getResourceOrFileOrURL(null, mifFileName);
            BufferedReader bfr = new BufferedReader(new InputStreamReader(fileURL
                      .openStream()));

            mifl = new MIFLoader(bfr, accurate, textVisible, pointVisible);
        } catch (IOException ioe) {
            Debug.error("MIFLayer: didn't find file " + mifFileName);
            return;
        }
    }

    /**
     * Sets the accuracy of the rendering. The default is true. If set
     * to false then the regions will not always be drawn correctly
     * (ie. as they appear in MapInfo) however processing will be much
     * faster. This option effects the drawing of Regions which can
     * have nested regions. Nested regions appear as holes in the
     * outer region and it is this that can take a lot of processing
     * time. Usually something like a street layout will take much
     * more time to draw. The more holes then the longer it takes. By
     * setting accuracy to false the regions are drawn as lines
     * instead of filled which is much faster.
     */
    public void setAccuracy(boolean accurate) {
        this.accurate = accurate;
    }

    /**
     * OMGraphicHandlerLayer method for gathering data.
     */
    public synchronized OMGraphicList prepare() {
        if (mifl != null) {
            OMGraphicList list = mifl.getList();
            if (list != null) {
                list.generate(getProjection());
            }
            return list;
        } else {
            return new OMGraphicList();
        }
    }
}

/* Last line of file */
