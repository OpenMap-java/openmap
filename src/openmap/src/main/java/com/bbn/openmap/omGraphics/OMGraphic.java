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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/OMGraphic.java,v $
// $RCSfile: OMGraphic.java,v $
// $Revision: 1.16 $
// $Date: 2009/01/21 01:24:41 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.omGraphics;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.TexturePaint;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;

import com.bbn.openmap.proj.Projection;

/**
 * Used to be the base class of OpenMap graphics, but now inherits from
 * BasicGeometry, which now contains all the information about the geometry of
 * the OMGraphic. The OMGraphic also contains information about how the geometry
 * should be drawn.
 * <P>
 * The OMGraphics are raster and vector graphic objects that know how to
 * position and render themselves on a given x-y window or lat-lon map
 * projection. All you have to do is supply the location data (x/y, lat/lon) and
 * drawing information (color, line width) and the graphic handles the rest.
 * <p>
 * 
 * This class contains parameters that are common to most types of graphics. If
 * a parameter doesn't make sense for a particular graphic type, it is ignored.
 * <p>
 * 
 * The OMGraphics are being updated to be able to provide java.awt.Shape
 * representations of themselves after they have been generated(). The
 * getShape() method returns a java.awt.Shape object. With the Shape object, you
 * can do some spatial analysis (object operations) on the projected OMGraphics.
 * 
 * NOTES:
 * <ul>
 * <li>Color values cannot be set to null, but can be set to OMGraphic.clear.
 * Actually, if you set them to null, they will set themselves to be clear.
 * <li>XY Rendering: Java specifies that the origin is the top left of the
 * window, x increases to the right, y increases down.
 * <li>LatLon Rendering: Defined by the Projection object. The center of the
 * window usually corresponds to the center of the projection. OMGraphics should
 * project themselves using the appropriate forward() method listed in the
 * Projection interface
 * <li>Offset Rendering: same as XY, but with origin set to a projected LatLon
 * point.
 * </ul>
 * 
 * @see OMBitmap
 * @see OMCircle
 * @see OMLine
 * @see OMPoly
 * @see OMRect
 * @see OMRaster
 * @see OMText
 * @see OMGraphicList
 * @see Projection
 */
public interface OMGraphic extends OMGeometry, OMGraphicConstants, Cloneable {

    /**
     * Set the render type of the graphic. Accepts RENDERTYPE_LATLON,
     * RENDERTYPE_XY and RENDERTYPE_OFFSET. All weird values get set to
     * RENDERTYPE_XY. See the definition on the renderType parameter.
     * 
     * @param value the rendertype for the object.
     */
    void setRenderType(int value);

    /**
     * Return the render type.
     * 
     * @return the rendertype of the object - RENDERTYPE_LATLON, RENDERTYPE_XY,
     *         RENDERTYPE_OFFSET and RENDERTYPE_UNKNOWN.
     */
    int getRenderType();

    /**
     * Set the declutter setting for the graphic. Accepts DECLUTTERTYPE_SPACE,
     * DECLUTTERTYPE_MOVE, DECLUTTERTYPE_LINE, and DECLUTTERTYPE_NONE. All weird
     * values are set to DECLUTTERTYPE_NONE.
     * <p>
     * Right now, this is unimplemented in OpenMap. But for information,
     * DECLUTTERTYPE_NONE means the object has no impact on the placement of
     * objects. DECLUTTERTYPE_SPACE means the object shouldn't have things
     * placed on it, but to draw it where the coordinates dictate.
     * DECLUTTERTYPE_MOVE means to put the object in an open space, and
     * DELCUTTERTYPE_LINE adds the feature that if the object is not drawn where
     * it's coordinates say it should be, then a line should be drawn showing
     * where the original position is.
     * <P>
     * Decluttering of geometries is not supported. This flag is not used.
     * 
     * @param value the declutter type value.
     */
    void setDeclutterType(int value);

    /**
     * Return the declutter type.
     * 
     * @return declutter type, see above.
     */
    int getDeclutterType();

    /**
     * Given a java.awt.Graphics object, set the Stroke and Paint parameters of
     * it to match the OMGraphic's edge settings.
     * 
     * @param g java.awt.Graphics
     * @see #setGraphicsColor
     */
    void setGraphicsForEdge(Graphics g);

    /**
     * Given a java.awt.Graphics object, set the Paint to be the OMGraphic's
     * fillPaint setting.
     * 
     * @param g java.awt.Graphics
     * @see #setGraphicsColor
     */
    void setGraphicsForFill(Graphics g);

    /**
     * Set the Paint in the given Graphics. If the Graphics is not an instance
     * of Graphics2D, then the Color of the graphics is set if the Paint is an
     * instance of Color.
     * 
     * @param g java.awt.Graphics
     * @param paint java.awt.Paint
     */
    void setGraphicsColor(Graphics g, Paint paint);

    /**
     * Return the normal foreground color of the object.
     * 
     * @return the line color. Returns null if the Paint is not a Color.
     */
    Color getLineColor();

    /**
     * Set the line Paint. The line Paint is the normal display edge paint of
     * the graphic. This Paint is used as the display Paint when the object is
     * NOT selected (highlighted). The display Paint is set to the select Paint
     * in this method if <code>selected</code> boolean attribute is false.
     * 
     * @param paint the real line Paint
     */
    void setLinePaint(Paint paint);

    /**
     * Get the normal line Paint used for the graphic.
     * 
     * @return Line Paint.
     */
    Paint getLinePaint();

    /**
     * Return the selected color, which is the line or foreground color used
     * when the graphic is "selected".
     * 
     * @return the selected mode line color. Returns null if the select Paint is
     *         not a Color.
     */
    Color getSelectColor();

    /**
     * Set the select Paint. The select Paint is the display edge paint of the
     * graphic. This Paint is used as the display Paint when the object IS
     * selected (highlighted). The display Paint is set to the select Paint in
     * this method if <code>selected</code> boolean attribute is true.
     * 
     * @param paint the real select Paint
     */
    void setSelectPaint(Paint paint);

    /**
     * Get the normal select Paint used for the graphic.
     * 
     * @return Select Paint.
     */
    Paint getSelectPaint();

    /**
     * Return the color that should be used for display. This color changes,
     * depending on whether the object is selected or not. The display color is
     * also set when the line color or the select color is set, depending on the
     * statue of the <code>selected</code> boolean attribute.
     * 
     * @return the color used as the edge color or foreground color, in the
     *         present selected state. If the displayPaint is not a Color, this
     *         method returns null.
     */
    Color getDisplayColor();

    /**
     * Return the Paint that should be used for display. This Paint changes,
     * depending on whether the object is selected or not. The display Paint is
     * also set when the line Paint or the select Paint is set, depending on the
     * statue of the <code>selected</code> boolean attribute.
     * 
     * @return the Paint used as the edge Paint or foreground Paint, in the
     *         present selected state.
     */
    Paint getDisplayPaint();

    /**
     * Set the selected attribute to true, and sets the color to the select
     * color.
     */
    void select();

    /**
     * Set the selected attribute to false, sets the color to the line color.
     */
    void deselect();

    /**
     * Return whether the OMGraphic is selected.
     */
    boolean isSelected();

    /**
     * Calls select() or deselect() depending on the boolean (select is true).
     */
    void setSelected(boolean set);

    /**
     * Return whether the OMGraphic has matting around the edge.
     */
    boolean isMatted();

    /**
     * Set whether the OMGraphic should have matting around the edge.
     */
    void setMatted(boolean set);

    /**
     * Return the background color of the graphic object. If the fill Paint is
     * not a color, this method will return null.
     * 
     * @return the color used for the background.
     */
    Color getFillColor();

    /**
     * Set the fill Paint for this graphic. If the paint value is null, it will
     * be set to OMGraphicConstants.clear.
     * 
     * @param paint the Paint object.
     */
    void setFillPaint(Paint paint);

    /**
     * Set the texture mask for the OMGraphic. If not null, then it will be
     * rendered on top of the fill paint. If the fill paint is clear, the
     * texture mask will not be used. If you just want to render the texture
     * mask as is, set the fill paint of the graphic instead. This is really to
     * be used to have a texture added to the graphic, with the fill paint still
     * influencing appearance.
     */
    void setTextureMask(TexturePaint texture);

    /**
     * Return the fill Paint for this graphic.
     */
    Paint getFillPaint();

    /**
     * Return the texture mask Paint for this graphic.
     */
    TexturePaint getTextureMask();

    /**
     * Set the Paint used for matting.
     */
    void setMattingPaint(Paint mPaint);

    /**
     * Get the Paint used for matting.
     */
    Paint getMattingPaint();

    /**
     * Set the Stroke that should be used for the graphic edges. Using a
     * BasicStroke, you can set a stroke that defines the line width, the dash
     * interval and phase. If a null value is passed in, a default BasicStroke
     * will be used.
     * 
     * @param s the stroke to use for the graphic edge.
     * @see java.awt.Stroke
     * @see java.awt.BasicStroke
     */
    void setStroke(Stroke s);

    /**
     * Get the Stroke used for the graphic edge.
     */
    Stroke getStroke();

    /**
     * Set whether an EditableOMGraphic modifying this graphic should show it's
     * palette.
     */
    void setShowEditablePalette(boolean set);

    /**
     * Get whether an EditableOMGraphic modifying this graphic should show it's
     * palette.
     */
    boolean getShowEditablePalette();

    /**
     * A function that takes a float distance, which presumably represents the
     * pixel distance from a point to a graphic, and subtracts half of the line
     * width of the graphic from the distance if the graphic line width is
     * greater than one. This should give a true pixel distance from the
     * graphic, taking into account an embellished line.
     * 
     * @param distance pixel distance to the graphic edge with a line width of
     *        one.
     * @return the pixel distance to the true display edge of the graphic.
     */
    float normalizeDistanceForLineWidth(float distance);

    // ////////////////////////////////////////////////////////////////////////

    /**
     * Sets the label location at the center of the polygon points. If the
     * hasLabel variable hasn't been set, it no-ops.
     * 
     * @param xpoints
     * @param ypoints
     */
    void setLabelLocation(int[] xpoints, int[] ypoints);

    /**
     * Sets the label location at the given point. If the hasLabel variable
     * hasn't been set, it no-ops.
     * 
     * @param p
     */
    void setLabelLocation(Point2D p);

    /**
     * Sets the label location at the center of the bounding box of the path. If
     * the hasLabel variable hasn't been set, it no-ops.
     * 
     * @param gp
     */
    void setLabelLocation(GeneralPath gp);

    /**
     * Checks to see if a label should be painted based on what methods were
     * called in generate(), and renders the label if necessary. If the label
     * wasn't set up, a quick no-op occurs.
     * 
     * @param g
     */
    void renderLabel(Graphics g);

    /**
     * Return true of the fill color/paint should be rendered (not clear).
     */
    boolean shouldRenderFill();

    /**
     * Return true if the edge color/paint should be rendered (not clear, or
     * doesn't match the fill color).
     */
    boolean shouldRenderEdge();

    /**
     * Invoke this to regenerate a "dirty" graphic. This method is a wrapper
     * around the <code>generate()</code> method. It invokes
     * <code>generate()</code> only if</code> needToRegenerate() </code> on the
     * graphic returns true. To force a graphic to be generated, call
     * <code>generate()</code> directly.
     * 
     * @param proj the Projection
     * @return true if generated, false if didn't do it (maybe a problem).
     * @see #generate
     */
    boolean regenerate(Projection proj);

    /**
     * Used by the GraphicAttributes object to provide a choice on whether the
     * line type choice can be changed.
     */
    boolean hasLineTypeChoice();
    
    /**
     * To support clone operations.  Might not be implemented to the depth desired.
     * @return Object cloned from this OMGraphic
     */
    Object clone();

}