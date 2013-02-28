/* **********************************************************************
 * 
 *    Use, duplication, or disclosure by the Government is subject to
 *           restricted rights as set forth in the DFARS.
 *  
 *                         BBNT Solutions LLC
 *                             A Part of 
 *                  Verizon      
 *                          10 Moulton Street
 *                         Cambridge, MA 02138
 *                          (617) 873-3000
 *
 *    Copyright (C) 2002 by BBNT Solutions, LLC
 *                 All Rights Reserved.
 * ********************************************************************** */

package com.bbn.openmap.layer.beanbox;

import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicConstants;
import com.bbn.openmap.omGraphics.OMRect;

/**
 * A custom graphic class for representing objects of type
 * {@link com.bbn.openmap.layer.beanbox.SimpleBeanContainer}as a
 * rectangular box.
 */
public class ContainerGraphic extends CustomGraphic {

    public ContainerGraphic(SimpleBeanObject object) {
        super(object);
        this.setRenderType(OMGraphicConstants.RENDERTYPE_LATLON);
    }

    /**
     * Returns an OMRect object with dimensions equal to the width and
     * height of the SimpleBeanContainer and position equal to the
     * center lat/lon position of the SimpleBeanContainer object.
     */
    public OMGraphic createGraphic(SimpleBeanObject object) {

        if (!(object instanceof SimpleBeanContainer)) {
            throw new IllegalArgumentException(object
                    + " not instance of SimpleBeanContainer");
        }

        SimpleBeanContainer bc = (SimpleBeanContainer) object;

        return new OMRect(bc.getTopLatitude(), bc.getLeftLongitude(), bc.getBottomLatitude(), bc.getRightLongitude(), OMGraphicConstants.LINETYPE_RHUMB);
    }

    /**
     * Updates the width, height and position of OMRect object used to
     * represent the SimpleBeanContainer object with the corresponding
     * values in the SimpleBeanContainer object.
     */
    public void updateGraphic(SimpleBeanObject object) {

        if (!(object instanceof SimpleBeanContainer)) {
            throw new IllegalArgumentException(object
                    + " not instance of SimpleBeanContainer");
        }

        SimpleBeanContainer bc = (SimpleBeanContainer) object;

        OMGraphic graphic = super.getOMGraphicAt(0);

        if (graphic instanceof OMRect) {

            OMRect rect = (OMRect) graphic;

            rect.setLocation(bc.getTopLatitude(),
                    bc.getLeftLongitude(),
                    bc.getBottomLatitude(),
                    bc.getRightLongitude(),
                    OMGraphicConstants.LINETYPE_RHUMB);

        }

    }

}