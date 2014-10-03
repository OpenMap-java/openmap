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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/tools/icon/BasicAppIconPart.java,v $
// $RCSfile: BasicAppIconPart.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:06:27 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.tools.icon;

import java.awt.Shape;
import java.awt.geom.AffineTransform;

import com.bbn.openmap.omGraphics.DrawingAttributes;

/**
 * A BasicAppIconPart is a BasicIconPart that can be interested in a
 * set of DrawingAttributes on the fly. Good for an icon part that may
 * change under certain system conditions, like something reflecting a
 * current color, or an application icon reflecting a color theme in
 * the application. The BasicAppIconPart may have a default
 * DrawingAttributes that describe how it should be drawn if other
 * DrawingAttributes aren't provided at rendertime.
 */
public class BasicAppIconPart extends BasicIconPart implements IconPart, Cloneable {

    public BasicAppIconPart(Shape shape) {
        super(shape);
    }

    public BasicAppIconPart(Shape shape, AffineTransform transform) {
        super(shape, transform);
    }

    /**
     * Get the DrawingAttributes that should be used for rendering.
     * 
     * @param da DrawingAttributes passed in that may affect rendering
     *        choices. For the BasicAppIconPart, if this is not null,
     *        it is returned. Otherwise, the internal version is
     *        returned.
     * @return DrawingAttribute for this part.
     */
    protected DrawingAttributes getAttributesForRendering(DrawingAttributes da) {
        if (da == null) {
            return getRenderingAttributes();
        } else {
            return da;
        }
    }
}