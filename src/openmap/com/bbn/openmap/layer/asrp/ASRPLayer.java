// **********************************************************************
// 
// <copyright>
// 
//  BBN Technologies, a Verizon Company
//  10 Moulton Street
//  Cambridge, MA 02138
//  (617) 873-8000
// 
//  Copyright (C) BBNT Solutions LLC. All rights reserved.
// 
// </copyright>
// **********************************************************************
// 
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/asrp/ASRPLayer.java,v $
// $RCSfile: ASRPLayer.java,v $
// $Revision: 1.2 $
// $Date: 2004/03/05 02:25:58 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.layer.asrp;

import com.bbn.openmap.I18n;
import com.bbn.openmap.dataAccess.asrp.*;
import com.bbn.openmap.layer.OMGraphicHandlerLayer;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.OMRect;
import com.bbn.openmap.proj.EqualArc;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.DataBounds;
import com.bbn.openmap.util.DataBoundsProvider;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PropUtils;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

public class ASRPLayer extends OMGraphicHandlerLayer implements DataBoundsProvider {

    protected List asrpList = null;
    protected String[] thfPaths = null;
    protected String[] asrpDirs = null;

    /**
     * The ASRPDirectory can be used to view the images from ASRP
     * directories containing GEN, GER, SOU, QAL and IMG files.  This
     * property is only checked if the THFProperty is not set
     * (asrpDir).  Should contain a list of semi-colon separated paths
     * to ASRP directories containing the files specifed above.
     */
    public final static String ASRPDirectoryProperty = "asrpDirs";
    /**
     * A semi-colon separated set of paths to TRANSH01.THF files
     * containing information about ASRP images stored in directories
     * next to the TRANSH01.THF file.
     */
    public final static String THFProperty = "thf";

    public ASRPLayer() {
        setProjectionChangePolicy(new com.bbn.openmap.layer.policy.ListResetPCPolicy(this));
    }

    public synchronized OMGraphicList prepare() {

        Projection proj = getProjection();
        if (proj == null) {
            return null;
        }

        try {

            if (asrpList == null) {
                asrpList = initialize();
            }

            if (!(proj instanceof EqualArc)) {
                fireRequestInfoLine("ASRP data requires an Equal Arc projection (CADRG/LLXY)");
                return null;
            }

            OMGraphicList list = new OMGraphicList();
            for (Iterator it = asrpList.iterator(); it.hasNext(); ) {
                ASRPDirectory asrpDir = (ASRPDirectory) it.next();
                
                list.add(asrpDir.getTiledImages(proj));
            }

            return list;
        } catch (IOException ioe) {
            Debug.error("ASRPLayer(" + getName() + 
                        ") caught exception reading tiles:\n" + 
                        ioe.getMessage());
        } catch (ClassCastException cce) {
            Debug.error("ASRPLayer (" + getName() + ") given a list of something other than ASRP directories to fetch data from");
        }

        return null;
    }

    protected List initialize() {
        LinkedList list = new LinkedList();
        int i;
        if (thfPaths != null) {
            for (i = 0; i < thfPaths.length; i++) {
                try {
                    TransmittalHeaderFile thf = new TransmittalHeaderFile(thfPaths[i]);
                    list.addAll(thf.getASRPDirectories());
                } catch (IOException ioe) {
                    Debug.error("ASRPLayer (" + getName() + 
                                ") caught exception trying to read TRANSH02.THF: " + 
                                ioe.getMessage());
                }
            }
        } else if (asrpDirs != null) {
            for (i = 0; i < asrpDirs.length; i++) {
                list.add(new ASRPDirectory(asrpDirs[i]));
            }
        }

        return list;
    }

    public List getASRPList() {
        return asrpList;
    }

    public void setASRPList(List asrpListIn) {
        asrpList = asrpListIn;
    }

    /**
     * Set the paths used by the layer.  Clears out the ASRP List
     * currently set.  The contents of the provided paths will be
     * checked to see if they are paths to TRANSH01.THF files, and
     * will be set to the ASRP directory files if they aren't. Call
     * doPrepare() on this layer if you want the changes to take
     * effect immediately.
     */
    public void setPaths(String[] pathsIn) {
        if (pathsIn != null) {
            if (pathsIn[0].indexOf(ASRPConstants.TRANS) != -1) {
                thfPaths = pathsIn;
            } else {
                asrpDirs = pathsIn;
            }

            asrpList = null;// next projection change, we reload.
        }
    }

    /**
     * Returns THF paths if they are set, otherwise returns ASRP
     * directory paths.
     */
    public String[] getPaths() {
        if (thfPaths != null) {
            return thfPaths;
        } else {
            return asrpDirs;
        }
    }

    public void setProperties(String prefix, Properties props) {
        super.setProperties(prefix, props);

        prefix = PropUtils.getScopedPropertyPrefix(prefix);
        thfPaths = PropUtils.initPathsFromProperties(props, prefix + THFProperty);
        if (thfPaths == null) {
            asrpDirs = PropUtils.initPathsFromProperties(props, prefix + ASRPDirectoryProperty);
        }
    }

    public Properties getProperties(Properties props) {
        props = super.getProperties(props);
        String prefix = PropUtils.getScopedPropertyPrefix(this);

        // find out paths...
        if (thfPaths != null) {
            String[] p = thfPaths;
            StringBuffer pathString = new StringBuffer();
            if (p != null) {
                for (int i = 0; i < p.length; i++) {
                    if (p[i] != null) {
                        pathString.append(p[i]);
                        if (i < p.length - 1) {
                            pathString.append(";"); // separate paths with ;
                        }
                    }
                }
            }
            props.put(prefix + THFProperty, pathString.toString());
        } else {
            props.put(prefix + THFProperty, "");
        }

        if (asrpDirs != null) {
            String[] p = asrpDirs;
            StringBuffer pathString = new StringBuffer();
            if (p != null) {
                for (int i = 0; i < p.length; i++) {
                    if (p[i] != null) {
                        pathString.append(p[i]);
                        if (i < p.length - 1) {
                            pathString.append(";"); // separate paths with ;
                        }
                    }
                }
            }
            props.put(prefix + ASRPDirectoryProperty, pathString.toString());
        } else {
            props.put(prefix + ASRPDirectoryProperty, "");
        }

        return props;
    }

    public Properties getPropertyInfo(Properties props) {
        props = super.getPropertyInfo(props);
        String interString;

        interString = i18n.get(ASRPLayer.class,THFProperty,I18n.TOOLTIP,"Paths to TRANSH01.THF files, takes precedence over ASRP property.  Separated by ;");
        props.put(THFProperty, interString);
        interString = i18n.get(ASRPLayer.class, THFProperty, "TRANSH01.THF files");
        props.put(THFProperty + LabelEditorProperty,interString);
        props.put(THFProperty + ScopedEditorProperty, 
                  "com.bbn.openmap.util.propertyEditor.MultiDirectoryPropertyEditor");

        interString = i18n.get(ASRPLayer.class,ASRPDirectoryProperty,I18n.TOOLTIP,"Paths to ASRP Directories (if no TRANSH01.THF files)");
        props.put(ASRPDirectoryProperty, interString);
        interString = i18n.get(ASRPLayer.class, ASRPDirectoryProperty, "ASRP directories");
        props.put(ASRPDirectoryProperty + LabelEditorProperty,interString);
        props.put(ASRPDirectoryProperty + ScopedEditorProperty, 
                  "com.bbn.openmap.util.propertyEditor.MultiDirectoryPropertyEditor");
        return props;
    }

    /**
     * DataBoundsInformer interface.
     */
    public DataBounds getDataBounds() {
        DataBounds box = null;

        double minx = 180;
        double miny = 90;
        double maxx = -180;
        double maxy = -90;
        if (asrpList != null) {

            for (Iterator it = asrpList.iterator(); it.hasNext();) {
                OMRect rect = ((ASRPDirectory)it.next()).getBounds();
                float n = rect.getNorthLat();
                float s = rect.getSouthLat();
                float w = rect.getWestLon();
                float e = rect.getEastLon();

                if (n < miny) miny = n;
                if (n > maxy) maxy = n;
                if (s < miny) miny = s;
                if (s > maxy) maxy = s;
                if (w < minx) minx = w;
                if (w > maxx) maxx = w;
                if (e < minx) minx = e;
                if (e > maxx) maxx = e;

            }

            box = new DataBounds(minx, miny, maxx, maxy);
        }
        Debug.output(box.toString());
        return box;
    }

}
