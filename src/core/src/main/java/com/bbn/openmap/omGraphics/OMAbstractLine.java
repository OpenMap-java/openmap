/**
 * Created on Jul 13, 2005
 * 
 * @author srosset
 * @version $Revision: 1.3 $
 */
package com.bbn.openmap.omGraphics;

import java.awt.Color;
import java.awt.geom.GeneralPath;

/**
 * The abstract base class of OMPoly and OMLine.
 * 
 * @see OMLine
 * @see OMPoly
 * 
 * @author Sebastien Rosset
 */
public abstract class OMAbstractLine extends OMGraphicAdapter implements OMGraphic {

    /**
     * X coordinate arrays of the projected points.
     */
    protected float[][] xpoints = new float[0][0];

    /**
     * Y coordinate arrays of the projected points.
     */
    protected float[][] ypoints = new float[0][0];

    /**
     * Number of segments to draw (used only for LINETYPE_GREATCIRCLE
     * or LINETYPE_RHUMB lines).
     */
    protected int nsegs = -1;

    /**
     * The awt object representing the arrowhead.
     */
    protected OMArrowHead arrowhead = null;

    /**
     * Construct a default OMAbstractLine.
     */
    public OMAbstractLine() {
        super();
    }

    /**
     * Construct an OMAbstractLine. Standard simple constructor that
     * the child OMAbstractLine usually call. All of the other
     * parameters get set to their default values.
     * 
     * @param rType render type
     * @param lType line type
     * @param dcType declutter type
     */
    public OMAbstractLine(int rType, int lType, int dcType) {
        super(rType, lType, dcType);
    }

    /**
     * Construct an OMAbstractLine. More complex constructor that lets
     * you set the rest of the parameters.
     * 
     * @param rType render type
     * @param lType line type
     * @param dcType declutter type
     * @param lc line color
     * @param fc fill color
     * @param sc select color
     */
    public OMAbstractLine(int rType, int lType, int dcType, Color lc, Color fc,
            Color sc) {
        super(rType, lType, dcType, lc, fc, sc);
    }

    /**
     * Turn the ArrowHead on/off. The ArrowHead is placed on the
     * finishing end.
     * 
     * @param value on/off
     */
    public void addArrowHead(boolean value) {
        if (value) {
            setArrowHead(new OMArrowHead(OMArrowHead.ARROWHEAD_DIRECTION_FORWARD, 100));
        } else {
            arrowhead = null;
        }
    }

    /**
     * Set the OMArrowHead object on the OMAbstractLine.
     * 
     * @param omah
     */
    public void setArrowHead(OMArrowHead omah) {
        arrowhead = omah;
    }

    public OMArrowHead getArrowHead() {
        return arrowhead;
    }

    public boolean hasArrowHead() {
        return arrowhead != null;
    }

    /**
     * Turn the ArrowHead on. The ArrowHead is placed on the finishing
     * end (OMArrowHead.ARROWHEAD_DIRECTION_FORWARD), beginning end
     * (OMArrowHead.ARROWHEAD_DIRECTION_BACKWARD), or both
     * ends(OMArrowHead.ARROWHEAD_DIRECTION_BOTH).
     * 
     * @param directionType which way to point the arrow head.
     */
    public void addArrowHead(int directionType) {
        setArrowHead(new OMArrowHead(directionType, 100));
    }

    /**
     * Turn the ArrowHead on. The ArrowHead is placed on the finishing
     * end (OMArrowHead.ARROWHEAD_DIRECTION_FORWARD), beginning end
     * (OMArrowHead.ARROWHEAD_DIRECTION_BACKWARD), or both
     * ends(OMArrowHead.ARROWHEAD_DIRECTION_BOTH).
     * 
     * @param directionType which way to point the arrow head.
     * @param location where on the line to put the arrow head - 0 for
     *        the starting point, 100 for the end.
     */
    public void addArrowHead(int directionType, int location) {
        setArrowHead(new OMArrowHead(directionType, location));
    }

    /**
     * Turn the ArrowHead on. The ArrowHead is placed on the finishing
     * end (OMArrowHead.ARROWHEAD_DIRECTION_FORWARD), beginning end
     * (OMArrowHead.ARROWHEAD_DIRECTION_BACKWARD), or both
     * ends(OMArrowHead.ARROWHEAD_DIRECTION_BOTH).
     * 
     * @param directionType which way to point the arrow head.
     * @param location where on the line to put the arrow head - 0 for
     *        the starting point, 100 for the end.
     * @param tipWidth the width factor for the base of the arrowhead,
     *        on one side of the line. (Default is 5)
     * @param arrowLength the length factor of the arrowhead, from the
     *        tip of the line to the base of the arrowhead. (Default
     *        is 20)
     */
    public void addArrowHead(int directionType, int location, int tipWidth,
                             int arrowLength) {
        setArrowHead(new OMArrowHead(directionType, location, tipWidth, arrowLength));
    }

    /**
     * Arrowhead function, to find out the wing tip width.
     */
    public int getWingTip() {
        if (arrowhead != null) {
            return arrowhead.getWingTip();
        }
        return 0;
    }

    /**
     * Arrowhead function, to find out the arrowhead length.
     */
    public int getWingLength() {
        if (arrowhead != null) {
            return arrowhead.getWingLength();
        }
        return 0;
    }

    /**
     * Arrowhead function, to find out the arrowhead location.
     */
    public int getArrowLocation() {
        if (arrowhead != null) {
            return arrowhead.getLocation();
        }
        return 0;
    }

    /**
     * Arrowhead function, to find out the arrowhead direction.
     */
    public int getArrowDirectionType() {
        if (arrowhead != null) {
            return arrowhead.getArrowDirectionType();
        }
        return 0;
    }

    /**
     * This is a method that you can extend to create the GeneralPath
     * for the arrowheads, if you want a different way of doing it. By
     * default, it calls OMArrowHead.createArrowHeads(), using the
     * different arrowhead variables set in the OMLine.
     * 
     * @deprecated Create an OMArrowHead and set it on the
     *             OMAbstractLine instead.
     */
    public GeneralPath createArrowHeads() {
        return OMArrowHead.createArrowHeads(OMArrowHead.ARROWHEAD_DIRECTION_FORWARD,
                100,
                this,
                OMArrowHead.DEFAULT_WINGTIP,
                OMArrowHead.DEFAULT_WINGLENGTH);
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * com.bbn.openmap.omGraphics.OMGraphic#restore(com.bbn.openmap.omGraphics
     * .OMGraphic)
     */
    public void restore(OMGraphic source) {
        super.restore(source);
        if (source instanceof OMAbstractLine) {
            OMAbstractLine lineSource = (OMAbstractLine) source;
            OMArrowHead sah = lineSource.getArrowHead();
            if (sah != null) {
                OMArrowHead destArrowHead = new OMArrowHead(sah.getArrowDirectionType(), sah.getLocation(), sah.getWingTip(), sah.getWingLength());
                setArrowHead(destArrowHead);
            }
            
            nsegs = lineSource.nsegs;
        }
    }

}
