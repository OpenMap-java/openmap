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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/proj/ProjectionFactory.java,v $
// $RCSfile: ProjectionFactory.java,v $
// $Revision: 1.4 $
// $Date: 2004/02/06 19:46:44 $
// $Author: dietrick $
// 
// **********************************************************************



package com.bbn.openmap.proj;

import com.bbn.openmap.LatLonPoint;


/**
 * Create Projections.
 */
public class ProjectionFactory {

    private ProjectionFactory(){}

    protected static LatLonPoint llp = new LatLonPoint();

    /**
     * Create a projection.
     * @param projType projection type
     * @param centerLat center latitude in decimal degrees
     * @param centerLon center latitude in decimal degrees
     * @param scale float scale
     * @param width pixel width of projection
     * @param height pixel height of projection
     * @return Projection
     */
    public static Projection makeProjection (int projType,
                                             float centerLat,
                                             float centerLon,
                                             float scale,
                                             int width,
                                             int height)
    {
        llp.setLatLon(centerLat, centerLon);
        switch (projType) {
            case CADRG.CADRGType:
                return new CADRG(llp, scale, width, height);
            case Mercator.MercatorType:
                return new Mercator(llp, scale, width, height);
            case MercatorView.MercatorViewType:
                return new MercatorView(llp, scale, width, height);
            case LLXY.LLXYType:
                return new LLXY(llp, scale, width, height);
            case LLXYView.LLXYViewType:
                return new LLXYView(llp, scale, width, height);
            case Orthographic.OrthographicType:
                return new Orthographic(llp, scale, width, height);
            case OrthographicView.OrthographicViewType:
                return new OrthographicView(llp, scale, width, height);
//          case MassStatePlane.MassStatePlaneType:
//              return new MassStatePlane(llp, scale, width, height);
            case Gnomonic.GnomonicType:
                return new Gnomonic(llp, scale, width, height);
            default:
                System.err.println("Unknown projection type " + projType +
                                   " in ProjectionFactory.create()");
                return null;
        }
    }


    /**
     * Makes a new projection based on the given projection and given type.
     * <p>
     * The <code>centerLat</code>, <code>centerLon</code>, <code>scale</code>,
     * <code>width</code>, and <code>height</code> parameters are taken from
     * the given projection, and the type is taken from the type argument.
     * @param newProjType the type for the resulting projection
     * @param p the projection from which to copy other parameters
     */
    public static Projection makeProjection (int newProjType, Projection p) {
        LatLonPoint ctr = p.getCenter();
        return makeProjection(newProjType,
                              ctr.getLatitude(), ctr.getLongitude(),
                              p.getScale(), p.getWidth(), p.getHeight());
    }

    /** 
     * Return an int representing the OpenMap projection, given the
     * name of the projection. Useful for setting a projection based
     * on the name stated in a properties file.
     */
    public static int getProjType(String projName){

        int projType = Mercator.MercatorType;

        if (projName == null){}
        else if (projName.equalsIgnoreCase(Mercator.MercatorName))
            projType = Mercator.MercatorType;
        else if (projName.equalsIgnoreCase(MercatorView.MercatorViewName))
            projType = MercatorView.MercatorViewType;
        else if (projName.equalsIgnoreCase(Orthographic.OrthographicName))
            projType = Orthographic.OrthographicType;
        else if (projName.equalsIgnoreCase(OrthographicView.OrthographicViewName))
            projType = OrthographicView.OrthographicViewType;
        else if (projName.equalsIgnoreCase(LLXY.LLXYName))
            projType = LLXY.LLXYType;
        else if (projName.equalsIgnoreCase(LLXYView.LLXYViewName))
            projType = LLXYView.LLXYViewType;
        else if (projName.equalsIgnoreCase(CADRG.CADRGName))
            projType = CADRG.CADRGType;
        else if (projName.equalsIgnoreCase(Gnomonic.GnomonicName))
            projType = Gnomonic.GnomonicType;

        return projType;
    }

    /**
     * Returns an array of Projection names available from this factory.
     */
    public static String[] getAvailableProjections() {
      // For all the possible projections we have available:
//      int nProjections = 8;
//      String projNames[] = new String[nProjections];
//      projNames[0] = Mercator.MercatorName;
//      projNames[1] = MercatorView.MercatorViewName;
//      projNames[2] = Orthographic.OrthographicName;
//      projNames[3] = OrthographicView.OrthographicViewName;
//      projNames[4] = LLXY.LLXYName;
//      projNames[5] = LLXYView.LLXYViewName;
//      projNames[6] = CADRG.CADRGName;
//      projNames[7] = Gnomonic.GnomonicName;

        int nProjections = 5;
        String projNames[] = new String[nProjections];
        projNames[0] = Mercator.MercatorName;
        projNames[1] = Orthographic.OrthographicName;
        projNames[2] = LLXY.LLXYName;
        projNames[3] = CADRG.CADRGName;
        projNames[4] = Gnomonic.GnomonicName;
        return projNames;
    }
}
