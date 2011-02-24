/* **********************************************************************
 *
 *    Use, duplication, or disclosure by the Government is subject to
 * 	     restricted rights as set forth in the DFARS.
 *
 * 			   BBN Technologies
 * 			    A Division of
 * 			   BBN Corporation
 * 			  10 Moulton Street
 * 			 Cambridge, MA 02138
 * 			    (617) 873-3000
 *
 * 	  Copyright 1998 by BBN Technologies, A Division of
 * 		BBN Corporation, all rights reserved.
 *
 * **********************************************************************
 *
 * $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/vpf/VPFRoadLayer.java,v $
 * $RCSfile: VPFRoadLayer.java,v $
 * $Revision: 1.3 $
 * $Date: 2005/08/12 21:47:49 $
 * $Author: dietrick $
 *
 * **********************************************************************
 */

package com.bbn.openmap.layer.vpf;

import java.awt.Graphics;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import com.bbn.openmap.event.ProjectionEvent;
import com.bbn.openmap.event.ProjectionListener;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.proj.coords.LatLonPoint;
import com.bbn.openmap.tools.roads.LayerView;
import com.bbn.openmap.tools.roads.RoadFinder;
import com.bbn.openmap.tools.roads.RoadServices;
import com.bbn.openmap.tools.roads.Route;
import com.bbn.openmap.util.PropUtils;

/**
 * Imposes a road layer on the VPF layer.
 * 
 * The important method here is getPathOnRoad (implemented for the
 * RoadServices interface) which returns a list of points on the road
 * found between a start and an end point.
 * 
 * You can see more about what the road layer is doing by setting
 * drawIntersections to true, which will reveal what the road finder
 * thinks are roads on the road layer, and drawResults to true, which
 * will show each road path request and its result. The results shown
 * accumulate over time.
 */
public class VPFRoadLayer extends VPFLayer implements ProjectionListener,
        RoadServices, LayerView {

    Logger logger = Logger.getLogger(this.getClass().getName());
    RoadFinder helper;

    /**
     * list of extra OMGraphics that represent intersections or
     * results
     */
    List toDraw = new ArrayList();
    boolean drawIntersections = false;
    boolean drawResults = false;

    /**
     * Property 'drawIntersections' will display the intersections on
     * the road layer False by default.
     */
    public static final String DrawIntersectionsProperty = "drawIntersections";

    /**
     * Property 'drawResults' will display the results of each road
     * request on the road layer False by default.
     */
    public static final String DrawResultsProperty = "drawResults";

    public void setProperties(String prefix, Properties props) {
        super.setProperties(prefix, props);
        String realPrefix = PropUtils.getScopedPropertyPrefix(prefix);

        setDrawIntersections(PropUtils.booleanFromProperties(props, realPrefix
                + DrawIntersectionsProperty, drawIntersections));
        setDrawResults(PropUtils.booleanFromProperties(props, realPrefix
                + DrawResultsProperty, drawResults));
        setHelper();
    }

    protected void setDrawIntersections(boolean val) {
        drawIntersections = val;
    }

    protected void setDrawResults(boolean val) {
        drawResults = val;
    }

    protected void setHelper() {
        logger.info("draw inter " + drawIntersections);
        helper = new RoadFinder((LayerView) this, drawIntersections, drawResults);
    }

    /**
     * Get points on the road between start and end
     * 
     * Implemented for the RoadService interface
     * 
     * @param start from here
     * @param end to there
     * @param segments populated with road segments
     * @return list of points on path
     */
    public List getPathOnRoad(Point start, Point end, List segments) {
        return helper.getPathOnRoad(start, end, segments);
    }

    /**
     * @see com.bbn.openmap.tools.roads.RoadServices#getPathOnRoad(LatLonPoint, LatLonPoint)
     */
    public Route getPathOnRoad(LatLonPoint start, LatLonPoint end) {
      return helper.getPathOnRoad(start, end);
    }

    /**
     * @see RoadServices#displayPathOnRoad(Point, Point, Route, List)
     */
    public List displayPathOnRoad(Point start, Point end, Route route, List segments) {
      return helper.displayPathOnRoad(start, end, route, segments);
    }
    
    /**
     * Returns the RoadServices instance.
     * <p>
     * 
     * @return the RoadServices instance.
     */
    public RoadServices getRoadServices() {
      return helper;
    }
    
    /**
     * Implemented for ProjectionListener
     */
    public void projectionChanged(ProjectionEvent e) {
        super.projectionChanged(e);
        logger.info("calling helper - projection changed.");
        synchronized (this) {
            if (helper == null)
                setHelper();
            helper.projectionChanged(e);
        }
    }

    /**
     * Called from RoadFinder to tell it what extra to render (e.g.
     * intersections, roads).
     */
    public void setExtraGraphics(List toDraw) {
        logger.info("setting to draw " + toDraw.size() + " new graphics.");
        this.toDraw = toDraw;
    }

    /**
     * If drawIntersections or drawResults is true, will add
     * intersection markers or returned road lines to what is
     * rendered.
     */
    public void paint(Graphics g) {
        super.paint(g);
        if (drawIntersections || drawResults) {
            OMGraphicList graphics;
            graphics = new OMGraphicList(toDraw);
            graphics.generate(getProjection(), true);//all new
            // graphics
            logger.info("rendering toDraw " + toDraw.size() + " items");
            graphics.render(g);
        }
    }

    /**
     * Creates an OMGraphicList containing graphics from all
     * SpatialIndex objects and shapefiles.
     * 
     * Synchonized to avoid problems if prepare called from two
     * threads at once.
     * 
     * @return OMGraphicList containing an OMGraphicList containing
     *         shapes from a particular shape file.
     */
    public OMGraphicList prepare() {
        synchronized (this) {
            return super.prepare();
        }
    }

    /**
     * Gets the original list of graphics items from getRectangle.
     * 
     * @return List of OMGraphic items that will be used to create
     *         roads
     */
    public List getGraphicList() {
        OMGraphicList omgl = getList();

        if (omgl != null) {
            return omgl.getTargets();
        }
        return new ArrayList();
    }

}