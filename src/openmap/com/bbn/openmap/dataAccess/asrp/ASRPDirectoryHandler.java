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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/dataAccess/asrp/ASRPDirectoryHandler.java,v $
// $RCSfile: ASRPDirectoryHandler.java,v $
// $Revision: 1.3 $
// $Date: 2009/01/21 01:24:41 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.dataAccess.asrp;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.bbn.openmap.omGraphics.DrawingAttributes;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.OMRect;
import com.bbn.openmap.proj.EqualArc;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.DataBounds;
import com.bbn.openmap.util.Debug;

/**
 * The ASRPDirectoryHandler is the main object a layer would use when
 * trying to deal with a bunch of ASRP image directories and/or a THF
 * file that refers to many ASRP image directories. This object will
 * make decisions, based on the coverage of the imagery of a certain
 * ASRP directory image and its "scale" (determined by the number of
 * pixels E-W of the projection of the image) what images are suitable
 * for a given EqualArc projection.
 */
public class ASRPDirectoryHandler {

    protected List asrpDirs;

    public ASRPDirectoryHandler() {}

    public OMGraphicList getCoverageBounds(Projection proj, DrawingAttributes da) {
        OMGraphicList list = new OMGraphicList();
        List asrps = getASRPDirs();

        for (Iterator it = asrps.iterator(); it.hasNext();) {
            OMRect rect = ((ASRPDirectory) it.next()).getBounds();
            da.setTo(rect);
            rect.generate(proj);

            list.add(rect);
        }
        return list;
    }

    public OMGraphicList getImagesForProjection(EqualArc proj)
            throws IOException {
        OMGraphicList ret = new OMGraphicList();

        if (proj == null) {
            return null;
        }

        List asrps = getASRPDirs();
        List currentBestASRPs = new LinkedList();
        double bestScaleDiff = Double.MAX_VALUE;
        Iterator it;
        ASRPDirectory current;

        for (it = asrps.iterator(); it.hasNext();) {
            try {
                current = (ASRPDirectory) it.next();
            } catch (ClassCastException cce) {
                Debug.message("asrp",
                        "ASRPDirectoryHandler.getImagesForProjection:  ASRP directory list contains something other than ASRPDirectory objects");
                continue;
            }

            if (current.isOnMap(proj) && current.validScale(proj)) {
                // Need to check to see if the ASRP Directory is the
                // best fit for the current projection scale, since
                // it's on the map and within the scale limits.

                double scaleDiff = Math.abs(proj.getXPixConstant()
                        - current.arv);

                if (scaleDiff < bestScaleDiff) {
                    if (Debug.debugging("asrp")) {
                        Debug.output("ASRPDirHandler: SETTING new diff ("
                                + scaleDiff + ") adding ASRPDirectory "
                                + current.dir);
                    }
                    bestScaleDiff = scaleDiff;
                    currentBestASRPs.clear();
                    currentBestASRPs.add(current);
                } else if (scaleDiff == bestScaleDiff) {
                    if (Debug.debugging("asrp")) {
                        Debug.output("ASRPDirHandler: USING current diff ("
                                + scaleDiff + ") adding ASRPDirectory "
                                + current.dir);
                    }
                    currentBestASRPs.add(current);
                }
            }
        }

        // OK, now currentBestASRPs should contain the ASRPDirectories
        // that best fit the current projection. If its empty, we
        // just return an empty list.
        for (it = currentBestASRPs.iterator(); it.hasNext();) {
            current = (ASRPDirectory) it.next();
            if (Debug.debugging("asrp")) {
                Debug.output("ASRPDirHandler: getting images from "
                        + current.dir);
            }

            OMGraphicList subList = current.getTiledImages(proj);

            if (!subList.isEmpty()) {
                ret.add(subList);
            } else {
                if (Debug.debugging("asrp")) {
                    Debug.output("ASRPDirHandler: no images retrieved ("
                            + subList.size() + ")");
                }
            }
        }

        if (ret.isEmpty())
            ret = null;

        return ret;
    }

    public void add(TransmittalHeaderFile thf) {
        if (thf != null) {
            getASRPDirs().addAll(thf.getASRPDirectories());
        }
    }

    public void add(ASRPDirectory asrpDir) {
        getASRPDirs().add(asrpDir);
    }

    public void remove(TransmittalHeaderFile thf) {
        if (thf != null) {
            getASRPDirs().removeAll(thf.getASRPDirectories());
        }
    }

    public void remove(ASRPDirectory asrpDir) {
        getASRPDirs().remove(asrpDir);
    }

    public void clear() {
        getASRPDirs().clear();
    }

    public List getASRPDirs() {
        if (asrpDirs == null) {
            asrpDirs = new LinkedList();
        }
        return asrpDirs;
    }

    public void setASRPDirs(List list) {
        asrpDirs = list;
    }

    /**
     * DataBounds interface method, although this object doesn't
     * implement the complete interface because it doesn't have a
     * name.
     */
    public DataBounds getDataBounds() {
        DataBounds box = null;

        double minx = 180;
        double miny = 90;
        double maxx = -180;
        double maxy = -90;

        boolean set = false;

        List asrps = getASRPDirs();

        for (Iterator it = asrps.iterator(); it.hasNext();) {
            OMRect rect = ((ASRPDirectory) it.next()).getBounds();
            double n = rect.getNorthLat();
            double s = rect.getSouthLat();
            double w = rect.getWestLon();
            double e = rect.getEastLon();

            if (n < miny)
                miny = n;
            if (n > maxy)
                maxy = n;
            if (s < miny)
                miny = s;
            if (s > maxy)
                maxy = s;
            if (w < minx)
                minx = w;
            if (w > maxx)
                maxx = w;
            if (e < minx)
                minx = e;
            if (e > maxx)
                maxx = e;

            set = true;
        }

        if (set) {
            box = new DataBounds(minx, miny, maxx, maxy);

            if (Debug.debugging("asrp")) {
                Debug.output("ASRPDirectoryHandler.getDataBounds(): "
                        + box.toString());
            }
        }

        return box;
    }

}