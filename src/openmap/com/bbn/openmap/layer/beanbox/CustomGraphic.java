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
import com.bbn.openmap.omGraphics.OMGraphicList;

/**
 * A custom graphic class for representing SimpleBeanObject beans on
 * the map. See
 * {@link com.bbn.openmap.layer.beanbox.ContainerGraphic}for an
 * example implementation.
 */
public abstract class CustomGraphic extends OMGraphicList {

    protected OMGraphic graphic;

    /**
     * Create a graphical representation of the object. The
     * constructor calls the abstract createGraphic method of this
     * class to create the custom graphic which is then added to the
     * OMGraphicList. This method also sets the SimpleBeanObject's id
     * as the appObject associated with the created graphic.
     */
    public CustomGraphic(SimpleBeanObject object) {

        super(1);

        setTraverseMode(OMGraphicList.LAST_ADDED_ON_TOP);

        graphic = createGraphic(object);

        super.add(graphic);

        setAppObject(new Long(object.getId()));
    }

    /**
     * Override this method to create and return the object's graphic
     * representation.
     */
    public abstract OMGraphic createGraphic(SimpleBeanObject object);

    /**
     * Change the graphic to reflect the current state of the object.
     * Default implementation does nothing. This method is called by
     * the SimpleBeanLayer to update a graphic in response to a user
     * action such as moving the object on the map.
     */
    public void updateGraphic(SimpleBeanObject object) {}

}