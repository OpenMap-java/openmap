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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.TexturePaint;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import com.bbn.openmap.omGraphics.geom.BasicGeometry;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.Debug;

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
public abstract class OMGraphicAdapter
        extends BasicGeometry
        implements OMGraphic, OMGraphicConstants, Cloneable, Serializable {

    /**
     * The Java2D Stroke. This is used for lineWidth, and dashing of the lines
     * and polygon edges.
     */
    protected transient Stroke stroke = BASIC_STROKE;

    /**
     * This color is the real foreground color of the object. It is kept so that
     * the object knows how to de-highlight itself. It defaults to black.
     */
    protected Paint linePaint = Color.black;

    /**
     * This paint is used for the matting area around the edge of an OMGraphic
     * painted when the matted variable is set to true.
     */
    protected Paint mattingPaint = Color.black;

    /**
     * The color that the object is displayed with. This color changes back and
     * forth between the selectColor and the lineColor, depending on the if the
     * object is selected or not.
     */
    protected Paint displayPaint = linePaint;

    /**
     * This color is the fill color of the object. It defaults to a black color
     * that is transparent.
     */
    protected Paint fillPaint = clear;

    /**
     * This Paint object is the fill texture mask of the object. It defaults to
     * null. If this texture mask is set, the fill paint will still be used to
     * fill the OMGraphic shape, but then this paint will be rendered on top. If
     * the textureMask has transparency, the fill paint still influences
     * appearance.
     */
    protected TexturePaint textureMask = null;

    /**
     * This color is the highlight color that can be used when the object is
     * selected. The default color is black, just like the line color.
     */
    protected Paint selectPaint = Color.black;

    /**
     * Flag to indicate that the object has/hasnot been put in a special mode as
     * a result of some event. Set through the select()/deselect methods().
     */
    protected boolean selected = false;

    /**
     * A flag for whether an EditableOMGraphic should show it's palette if the
     * OMGraphic is modified.
     */
    protected boolean showEditablePalette = true;

    /**
     * Flag to note if the current edge color matches the fill color. Can be
     * used to save from rendering the edge if rendering the filled area already
     * takes care of it.
     */
    protected boolean edgeMatchesFill = false;

    /**
     * The renderType describes the relation of the object to the window.
     * RENDERTYPE_LATLON means the object is positioned relative to lat/lon
     * points. RENDERTYPE_XY means the object is positioned relative to window
     * pixel coordinates. RENDERTYPE_OFFSET means the object is drawn at a pixel
     * offset to a lat/lon point.
     */
    protected int renderType = RENDERTYPE_UNKNOWN;

    /**
     * Decluttering is not supported by OpenMap yet. But, when it is, these
     * parameters will describe the way the object is manipulated on the window
     * relative to its neighbors. DECLUTTERTYPE_NONE means the object will be
     * drawn where its attributes say it should be. DECLUTTERTYPE_SPACE
     * indicates that the window space of the object should be marked as taken,
     * but the object should not be moved. DECLUTTERTYPE_MOVE means the object
     * should be moved to the nearest open location closest to the position
     * indicated by its attributes. DECLUTTERTYPE_LINE is the same as MOVE, but
     * in addition, a line is drawn from the current position to its original
     * position.
     */
    protected int declutterType = DECLUTTERTYPE_NONE;

    /**
     * Flag for determining when the matting around the edge of an OMGraphic.
     * Matting is a line, two pixels wider than the edge, painted under the
     * edge. It makes the OMGraphic stand out on busy backgrounds.
     */
    protected boolean matted = false;

    /**
     * The flag set in generate that causes the OMGraphic to look for an
     * OMLabeler attribute in render. This flag prevents an unnecessary
     * hashtable lookup every render call.
     */
    protected transient boolean hasLabel = false;

    /**
     * Checks if the Paint is clear.
     * 
     * @param paint Paint or null.
     * @return true if Paint is null or is a Color with a 0 alpha value.
     */
    public boolean isClear(Paint paint) {
        return DrawingAttributes.isClear(paint);
        // if (paint instanceof Color) {
        // return ((((Color) paint).getRGB() & 0xff000000) == 0);
        // } else {
        // return false;
        // }
    }

    // ////////////////////////////////////////////////////////

    /**
     * Construct a default OMGraphic.
     */
    protected OMGraphicAdapter() {
    }

    /**
     * Construct an OMGraphic. Standard simple constructor that the child
     * OMGraphics usually call. All of the other parameters get set to their
     * default values.
     * 
     * @param rType render type
     * @param lType line type
     * @param dcType declutter type
     */
    protected OMGraphicAdapter(int rType, int lType, int dcType) {
        setRenderType(rType);
        setLineType(lType);
        setDeclutterType(dcType);
    }

    /**
     * Construct an OMGraphic. More complex constructor that lets you set the
     * rest of the parameters.
     * 
     * @param rType render type
     * @param lType line type
     * @param dcType declutter type
     * @param lc line color
     * @param fc fill color
     * @param sc select color
     */
    public OMGraphicAdapter(int rType, int lType, int dcType, Color lc, Color fc, Color sc) {
        this(rType, lType, dcType);
        setLinePaint(lc);
        setSelectPaint(sc);
        setFillPaint(fc);
    }

    /**
     * Set the render type of the graphic. Accepts RENDERTYPE_LATLON,
     * RENDERTYPE_XY and RENDERTYPE_OFFSET. All weird values get set to
     * RENDERTYPE_XY. See the definition on the renderType parameter.
     * 
     * @param value the rendertype for the object.
     */
    public void setRenderType(int value) {
        if (renderType == value)
            return;
        setNeedToRegenerate(true); // flag dirty

        renderType = value;
    }

    /**
     * Return the render type.
     * 
     * @return the rendertype of the object - RENDERTYPE_LATLON, RENDERTYPE_XY,
     *         RENDERTYPE_OFFSET and RENDERTYPE_UNKNOWN.
     */
    public int getRenderType() {
        return renderType;
    }

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
    public void setDeclutterType(int value) {
        if (declutterType == value)
            return;
        setNeedToRegenerate(true); // flag dirty

        declutterType = value;
    }

    /**
     * Return the declutter type.
     * 
     * @return declutter type, see above.
     */
    public int getDeclutterType() {
        return declutterType;
    }

    /**
     * Given a java.awt.Graphics object, set the Stroke and Paint parameters of
     * it to match the OMGraphic's edge settings.
     * 
     * @param g java.awt.Graphics
     * @see #setGraphicsColor
     */
    public void setGraphicsForEdge(Graphics g) {
        if (g instanceof Graphics2D) {
            ((Graphics2D) g).setStroke(getStroke());
        }
        setGraphicsColor(g, getDisplayPaint());
    }

    /**
     * Given a java.awt.Graphics object, set the Paint to be the OMGraphic's
     * fillPaint setting.
     * 
     * @param g java.awt.Graphics
     * @see #setGraphicsColor
     */
    public void setGraphicsForFill(Graphics g) {
        if (g instanceof Graphics2D) {
            ((Graphics2D) g).setStroke(BASIC_STROKE);
        }
        setGraphicsColor(g, getFillPaint());
    }

    /**
     * Set the Paint in the given Graphics. If the Graphics is not an instance
     * of Graphics2D, then the Color of the graphics is set if the Paint is an
     * instance of Color.
     * 
     * @param g java.awt.Graphics
     * @param paint java.awt.Paint
     */
    public void setGraphicsColor(Graphics g, Paint paint) {
        if (g instanceof Graphics2D) {
            ((Graphics2D) g).setPaint(paint);
        } else if (paint instanceof Color) {
            g.setColor((Color) paint);
        }
    }

    /**
     * Set the line color of the graphic object. The line color is the normal
     * display edge color of the object. This color is used as the display color
     * when the object is NOT selected (highlighted). The display color is set
     * to the select color in this method if <code>selected</code> boolean
     * attribute is false.
     * 
     * @param value the real line color
     * @deprecated Use setLinePaint instead. Now taking advantage of the Java2D
     *             API.
     */
    public void setLineColor(Color value) {
        setLinePaint(value);
    }

    /**
     * Return the normal foreground color of the object.
     * 
     * @return the line color. Returns null if the Paint is not a Color.
     */
    public Color getLineColor() {
        if (linePaint instanceof Color) {
            return (Color) linePaint;
        } else {
            return null;
        }
    }

    /**
     * Set the line Paint. The line Paint is the normal display edge paint of
     * the graphic. This Paint is used as the display Paint when the object is
     * NOT selected (highlighted). The display Paint is set to the select Paint
     * in this method if <code>selected</code> boolean attribute is false.
     * 
     * @param paint the real line Paint
     */
    public void setLinePaint(Paint paint) {
        if (paint != null) {
            linePaint = paint;
        } else {
            linePaint = Color.black;
        }

        if (!selected) {
            displayPaint = linePaint;
        }
        setEdgeMatchesFill();
    }

    /**
     * Get the normal line Paint used for the graphic.
     * 
     * @return Line Paint.
     */
    public Paint getLinePaint() {
        return linePaint;
    }

    /**
     * Set the select color of the graphic object. The selected color is used as
     * the display color when the object is selected (highlighted). The display
     * color is set to the select color in this method if <code>selected</code>
     * boolean attribute is true.
     * 
     * @param value the selected color.
     * @deprecated Use setSelectPaint instead. Now taking advantage of the
     *             Java2D API.
     */
    public void setSelectColor(Color value) {
        setSelectPaint(value);
    }

    /**
     * Return the selected color, which is the line or foreground color used
     * when the graphic is "selected".
     * 
     * @return the selected mode line color. Returns null if the select Paint is
     *         not a Color.
     */
    public Color getSelectColor() {
        if (selectPaint instanceof Color) {
            return (Color) selectPaint;
        } else {
            return null;
        }
    }

    /**
     * Set the select Paint. The select Paint is the display edge paint of the
     * graphic. This Paint is used as the display Paint when the object IS
     * selected (highlighted). The display Paint is set to the select Paint in
     * this method if <code>selected</code> boolean attribute is true.
     * 
     * @param paint the real select Paint
     */
    public void setSelectPaint(Paint paint) {
        if (paint != null) {
            selectPaint = paint;
        } else {
            selectPaint = Color.black;
        }

        if (selected) {
            displayPaint = selectPaint;
        }
        setEdgeMatchesFill();
    }

    /**
     * Get the normal select Paint used for the graphic.
     * 
     * @return Select Paint.
     */
    public Paint getSelectPaint() {
        return selectPaint;
    }

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
    public Color getDisplayColor() {
        if (displayPaint instanceof Color) {
            return (Color) displayPaint;
        } else {
            return null;
        }
    }

    /**
     * Return the Paint that should be used for display. This Paint changes,
     * depending on whether the object is selected or not. The display Paint is
     * also set when the line Paint or the select Paint is set, depending on the
     * statue of the <code>selected</code> boolean attribute.
     * 
     * @return the Paint used as the edge Paint or foreground Paint, in the
     *         present selected state.
     */
    public Paint getDisplayPaint() {
        return displayPaint;
    }

    /**
     * Set the selected attribute to true, and sets the color to the select
     * color.
     */
    public void select() {
        selected = true;
        displayPaint = getSelectPaint();
        setEdgeMatchesFill();
    }

    /**
     * Set the selected attribute to false, sets the color to the line color.
     */
    public void deselect() {
        selected = false;
        displayPaint = getLinePaint();
        setEdgeMatchesFill();
    }

    /**
     * Return whether the OMGraphic is selected.
     */
    public boolean isSelected() {
        return selected;
    }

    /**
     * Calls select() or deselect() depending on the boolean (select is true).
     */
    public void setSelected(boolean set) {
        if (set) {
            select();
        } else {
            deselect();
        }
    }

    /**
     * Return whether the OMGraphic has matting around the edge.
     */
    public boolean isMatted() {
        return matted;
    }

    /**
     * Set whether the OMGraphic should have matting around the edge.
     */
    public void setMatted(boolean set) {
        matted = set;
    }

    /**
     * Set the background color of the graphic object.
     * 
     * @param value java.awt.Color.
     * @deprecated Use setFillPaint instead. Now taking advantage of the Java2D
     *             API.
     */
    public void setFillColor(Color value) {
        setFillPaint(value);
    }

    /**
     * Return the background color of the graphic object. If the fill Paint is
     * not a color, this method will return null.
     * 
     * @return the color used for the background.
     */
    public Color getFillColor() {
        if (fillPaint instanceof Color) {
            return (Color) fillPaint;
        } else {
            return null;
        }
    }

    /**
     * Set the fill Paint for this graphic. If the paint value is null, it will
     * be set to OMGraphicConstants.clear.
     * 
     * @param paint the Paint object.
     */
    public void setFillPaint(Paint paint) {
        if (paint != null) {
            fillPaint = paint;
            if (Debug.debugging("omGraphics")) {
                Debug.output("OMGraphic.setFillPaint(): fillPaint= " + fillPaint);
            }
        } else {
            fillPaint = clear;
            if (Debug.debugging("omGraphics")) {
                Debug.output("OMGraphic.setFillPaint(): fillPaint is clear");
            }
        }
        setEdgeMatchesFill();
    }

    /**
     * Set the texture mask for the OMGraphic. If not null, then it will be
     * rendered on top of the fill paint. If the fill paint is clear, the
     * texture mask will not be used. If you just want to render the texture
     * mask as is, set the fill paint of the graphic instead. This is really to
     * be used to have a texture added to the graphic, with the fill paint still
     * influencing appearance.
     */
    public void setTextureMask(TexturePaint texture) {
        textureMask = texture;
    }

    /**
     * Return the fill Paint for this graphic.
     */
    public Paint getFillPaint() {
        return fillPaint;
    }

    /**
     * Return the texture mask Paint for this graphic.
     */
    public TexturePaint getTextureMask() {
        return textureMask;
    }

    protected void setEdgeMatchesFill() {
        Paint paint = getDisplayPaint();
        if (fillPaint instanceof Color && paint instanceof Color && !isClear(fillPaint)) {
            edgeMatchesFill = ((Color) fillPaint).equals((Color) paint);
        } else {
            edgeMatchesFill = false;
        }
    }

    public boolean getEdgeMatchesFill() {
        return edgeMatchesFill;
    }

    /**
     * Set the Paint used for matting.
     */
    public void setMattingPaint(Paint mPaint) {
        mattingPaint = mPaint;
    }

    /**
     * Get the Paint used for matting.
     */
    public Paint getMattingPaint() {
        return mattingPaint;
    }

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
    public void setStroke(Stroke s) {
        if (s != null) {
            stroke = s;
        } else {
            stroke = BASIC_STROKE;
        }
    }

    /**
     * Get the Stroke used for the graphic edge.
     */
    public Stroke getStroke() {
        if (stroke == null) {
            stroke = BASIC_STROKE;
        }
        return stroke;
    }

    /**
     * Set whether an EditableOMGraphic modifying this graphic should show it's
     * palette.
     */
    public void setShowEditablePalette(boolean set) {
        showEditablePalette = set;
    }

    /**
     * Get whether an EditableOMGraphic modifying this graphic should show it's
     * palette.
     */
    public boolean getShowEditablePalette() {
        return showEditablePalette;
    }

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
    public float normalizeDistanceForLineWidth(float distance) {

        float lineWidth = 1;

        if (stroke instanceof BasicStroke) {
            lineWidth = ((BasicStroke) stroke).getLineWidth();
        }

        if (lineWidth > 1) {
            // extra calculation for lineWidth
            distance -= lineWidth / 2;
            if (distance < 0f) {
                distance = 0f;
            }
        }
        return distance;
    }

    // ////////////////////////////////////////////////////////////////////////

    /**
     * Prepare the graphic for rendering. This must be done before calling
     * <code>render()</code>! If a vector graphic has lat-lon components, then
     * we project these vertices into x-y space. For raster graphics we prepare
     * in a different fashion.
     * <p>
     * If the generate is unsuccessful, it's usually because of some oversight,
     * (for instance if <code>proj</code> is null), and if debugging is enabled,
     * a message may be output to the controlling terminal.
     * <p>
     * 
     * @param proj Projection
     * @return boolean true if successful, false if not.
     * @see #regenerate
     */
    public abstract boolean generate(Projection proj);

    /**
     * Paint the graphic. This paints the graphic into the Graphics context.
     * This is similar to <code>paint()</code> function of java.awt.Components.
     * Note that if the graphic has not been generated, it will not be rendered.
     * <P>
     * 
     * This method used to be abstract, but with the conversion of OMGraphics to
     * internally represent themselves as java.awt.Shape objects, it's a more
     * generic method. If the OMGraphic hasn't been updated to use Shape
     * objects, it should have its own render method.
     * 
     * @param g Graphics2D context to render into.
     */
    public void render(Graphics g) {

        Shape s = getShape();
        
        if (!isRenderable(s)) {
            return;
        }
        
        if (matted) {
            if (g instanceof Graphics2D && stroke instanceof BasicStroke) {
                BasicStroke bs = (BasicStroke) stroke;
                ((Graphics2D) g).setStroke(new BasicStroke(bs.getLineWidth() + 2f, bs.getEndCap(), bs.getLineJoin()));

                setGraphicsColor(g, mattingPaint);
                draw(g, s);
            }
        }

        if (shouldRenderFill()) {
            setGraphicsForFill(g);
            fill(g, s);

            if (textureMask != null && textureMask != fillPaint) {
                setGraphicsColor(g, textureMask);
                fill(g, s);
            }
        }

        if (shouldRenderEdge()) {
            setGraphicsForEdge(g);
            draw(g, s);
        }

        renderLabel(g);
    }

    /**
     * Calls super.setShape(), but also checks the attributes for a label and
     * moves the label accordingly. The label will be placed in the center of
     * the bounding box around the path.
     */
    public void setShape(GeneralPath gp) {
        super.setShape(gp);

        hasLabel = false;

        // Go ahead and set the label location if the shape exists.
        if (gp != null) {
            OMLabeler labeler = (OMLabeler) getAttribute(LABEL);
            if (labeler != null) {
                labeler.setLocation(gp);
                hasLabel = true;
            }
        }
    }

    protected void setHasLabel(boolean val) {
        hasLabel = val;
    }

    /**
     * Quick check of the flag to see if a label attribute has been set. Labels
     * are stored in the attribute table, and that table should only be checked
     * in a generate() method call, and not in the render(). The setShape() and
     * initLabelingDuringGenerate() method calls set this flag which is used to
     * opt-out of labeling methods for better performance.
     * 
     * @return true if OMGraphic has label set.
     */
    public boolean getHasLabel() {
        return hasLabel;
    }

    /**
     * The method only needs to be called in an OMGraphic's generate method if
     * the setShape() method isn't called there. The appropriate
     * setLabelLocation method for where the label should be set should be
     * called if this method is going to be used.
     */
    protected void initLabelingDuringGenerate() {
        setHasLabel(getAttribute(LABEL) != null);
    }

    /**
     * Sets the label location at the center of the polygon points. If the
     * hasLabel variable hasn't been set, it no-ops.
     * 
     * @param xpoints
     * @param ypoints
     */
    public void setLabelLocation(int[] xpoints, int[] ypoints) {
        if (hasLabel) {
            OMLabeler oml = (OMLabeler) getAttribute(LABEL);
            if (oml != null) {
                oml.setLocation(xpoints, ypoints);
            }
        }
    }

    /**
     * @see #setLabelLocation(int[], int[])
     */
    public void setLabelLocation(float[] xpoints, float[] ypoints) {
        int[] xs = new int[xpoints.length];
        int[] ys = new int[ypoints.length];
        for (int i = 0; i < xpoints.length; i++) {
            xs[i] = (int) xpoints[i];
            ys[i] = (int) ypoints[i];
        }
        setLabelLocation(xs, ys);
    }

    /**
     * Sets the label location at the given point. If the hasLabel variable
     * hasn't been set, it no-ops.
     * 
     * @param p
     */
    public void setLabelLocation(Point2D p) {
        if (hasLabel) {
            OMLabeler oml = (OMLabeler) getAttribute(LABEL);
            if (oml != null) {
                oml.setLocation(p);
            }
        }
    }

    /**
     * Sets the label location at the center of the bounding box of the path. If
     * the hasLabel variable hasn't been set, it no-ops.
     * 
     * @param gp
     */
    public void setLabelLocation(GeneralPath gp) {
        if (hasLabel) {
            OMLabeler oml = (OMLabeler) getAttribute(LABEL);
            if (oml != null) {
                oml.setLocation(gp);
            }
        }
    }

    /**
     * Checks to see if a label should be painted based on what methods were
     * called in generate(), and renders the label if necessary. If the label
     * wasn't set up, a quick no-op occurs.
     * 
     * @param g
     */
    public void renderLabel(Graphics g) {
        if (hasLabel) {
            OMLabeler labeler = (OMLabeler) getAttribute(LABEL);
            if (labeler != null) {
                labeler.render(g);
            }
        }
    }

    /**
     * Return true of the fill color/paint should be rendered (not clear).
     */
    public boolean shouldRenderFill() {
        return !isClear(getFillPaint());
    }

    /**
     * Return true if the edge color/paint should be rendered (not clear, or
     * doesn't match the fill color).
     */
    public boolean shouldRenderEdge() {
        // OK, so isClear on the displayPaitn could be inaccurate if
        // another thread changes the display paint on the graphic
        // before it actually gets rendered.
        return !isClear(getDisplayPaint()) || !edgeMatchesFill;
    }

    /**
     * Return the shortest distance from the graphic to an XY-point.
     * <p>
     * 
     * This method used to be abstract, but with the conversion of OMGraphics to
     * internally represent themselves as java.awt.Shape objects, it's a more
     * generic method. If the OMGraphic hasn't been updated to use Shape
     * objects, it should have its own distance method.
     * 
     * @param x X coordinate of the point.
     * @param y Y coordinate of the point.
     * @return float distance, in pixels, from graphic to the point. Returns
     *         Float.POSITIVE_INFINITY if the graphic isn't ready (ungenerated).
     */
    public float distance(double x, double y) {
        float distance = Float.POSITIVE_INFINITY;
        if (shouldRenderFill()) {
            distance = super.distance(x, y);
        } else {
            distance = super.distanceToEdge(x, y);
        }

        if (distance != Float.POSITIVE_INFINITY) {
            distance = normalizeDistanceForLineWidth(distance);
        }

        if (hasLabel) {
            OMLabeler labeler = (OMLabeler) getAttribute(LABEL);
            if (labeler != null) {
                float lDistance = labeler.distance(x, y);
                if (lDistance < distance) {
                    distance = lDistance;
                }
            }
        }

        return distance;
    }

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
    public boolean regenerate(Projection proj) {
        boolean ret = false;

        if (proj != null) {

            ret = super.regenerate(proj);

            // handle extra case: OMRasterObject.getNeedToReposition()
            if (!ret && this instanceof OMRasterObject) {
                ret = generate(proj);
            }
        }

        return ret;
    }

    /**
     * Used by the GraphicAttributes object to provide a choice on whether the
     * line type choice can be changed.
     */
    public boolean hasLineTypeChoice() {
        return true;
    }

    /**
     * Generic return of SinkGraphic for subclasses that don't implement clone
     * properly.
     */
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            return SinkGraphic.getSharedInstance();
        }
    }

    /**
     * Write this object to a stream.
     */
    private void writeObject(ObjectOutputStream oos)
            throws IOException {
        oos.defaultWriteObject();

        // Now write the Stroke. Take into account the stroke member
        // could be null.
        writeStroke(oos, stroke, OMGraphicAdapter.BASIC_STROKE);
    }

    protected void writeStroke(ObjectOutputStream oos, Stroke stroke, Stroke defStroke)
            throws IOException {

        boolean writeStroke = (stroke != defStroke) && stroke != null;

        if (writeStroke) {
            // First write a flag indicating if a Stroke is on the
            // stream.
            oos.writeBoolean(true);
            if (stroke instanceof BasicStroke) {
                BasicStroke s = (BasicStroke) stroke;

                // Then write flag indicating stroke is a BasicStroke
                oos.writeBoolean(true);

                // Write the Stroke data if a Stroke is on this
                // object.
                if (s != null) {
                    oos.writeFloat(s.getLineWidth());
                    oos.writeInt(s.getEndCap());
                    oos.writeInt(s.getLineJoin());
                    oos.writeFloat(s.getMiterLimit());
                    oos.writeObject(s.getDashArray());
                    oos.writeFloat(s.getDashPhase());
                }
            } else if (stroke instanceof Serializable) {
                oos.writeBoolean(false);
                oos.writeObject((Serializable) stroke);
            }

        } else {
            oos.writeBoolean(false);
        }
    }

    /**
     * Read this object from a stream.
     */
    private void readObject(ObjectInputStream ois)
            throws ClassNotFoundException, IOException {
        ois.defaultReadObject();

        // Read the Stroke
        stroke = readStroke(ois, OMGraphicAdapter.BASIC_STROKE);
    }

    protected Stroke readStroke(ObjectInputStream ois, Stroke defStroke)
            throws ClassNotFoundException, IOException {
        Stroke stroke = defStroke;

        // Get the flag indicating a stroke was streamed
        boolean streamHasStroke = ois.readBoolean();

        // Read and create the stroke
        if (streamHasStroke) {
            boolean isBasicStroke = ois.readBoolean();
            if (isBasicStroke) {
                float linewidth = ois.readFloat();
                int endcap = ois.readInt();
                int linejoin = ois.readInt();
                float miterlimit = ois.readFloat();
                float dasharray[] = (float[]) ois.readObject();
                float dashphase = ois.readFloat();
                stroke = new BasicStroke(linewidth, endcap, linejoin, miterlimit, dasharray, dashphase);
            } else {
                stroke = (Stroke) ois.readObject();
            }
        }

        return stroke;
    }

    /**
     * Takes the generic OMGraphic settings from another OMGraphic and pushes
     * them to this one.
     */
    public void restore(OMGeometry source) {
        super.restore(source);

        this.renderType = source.getRenderType();
        if (source instanceof OMGraphic) {
            OMGraphic omgSource = (OMGraphic) source;
            this.declutterType = omgSource.getDeclutterType();
            this.selected = omgSource.isSelected();
            this.showEditablePalette = omgSource.getShowEditablePalette();
            DrawingAttributes.sTransfer(omgSource, this);
        }
    }

}