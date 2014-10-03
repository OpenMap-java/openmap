/**
 * Booz|Allen|Hamilton 
 * 8283 Greensboro Dr. 
 * McLean, VA 22102-3888
 * 
 * This software was developed by Booz | Allen | Hamilton.
 * 
 * @author Richard B. Lane
 */
package com.bbn.openmap.event;

import java.awt.Color;
import java.awt.Transparency;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

import com.bbn.openmap.MapBean;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.proj.coords.LatLonPoint;

/**
 * This mouse mode responds to a double click with an animation effect of
 * zooming in the map.
 */
public class ZoomMouseMode
        extends CoordMouseMode {
    protected double squareWidth = 50;
    public final static transient String modeID = "Zoom";
    protected MapBean theMap = null;

    public ZoomMouseMode() {
        super(modeID, true);
    }

    /**
     * Process a mouseClicked event.
     * 
     * @param e mouse event.
     */
    public void mouseClicked(MouseEvent e) {
        if (e.getSource() instanceof MapBean) {
            final int currX = e.getX();
            final int currY = e.getY();
            theMap = (MapBean) e.getSource();
            if (e.getClickCount() == 2) {
                if (e.getButton() == 1) {
                    int squareCenterX = currX;
                    int squareCenterY = currY;

                    double aspect = (double) theMap.getHeight() / (double) theMap.getWidth();
                    double squareWidth = this.squareWidth;
                    double squareHeight = this.squareWidth;
                    if (aspect > 1) {
                        squareHeight *= aspect;
                    } else {
                        squareWidth /= aspect;
                    }
                    int squareUpperLeftX = squareCenterX - ((int) squareWidth / 2);
                    int squareUpperLeftY = squareCenterY - ((int) squareHeight / 2);

                    if (squareUpperLeftX < 1) {
                        squareUpperLeftX = 1;
                        squareCenterX = (int) (squareUpperLeftX + squareWidth / 2);
                    } else if (squareUpperLeftX + squareWidth >= theMap.getWidth()) {
                        squareUpperLeftX = (int) (theMap.getWidth() - squareWidth - 1);
                        squareCenterX = (int) (squareUpperLeftX + squareWidth / 2);
                    }
                    if (squareUpperLeftY < 1) {
                        squareUpperLeftY = 1;
                        squareCenterY = (int) (squareUpperLeftY + squareHeight / 2);
                    } else if (squareUpperLeftY + squareHeight >= theMap.getHeight()) {
                        squareUpperLeftY = (int) (theMap.getHeight() - squareHeight - 1);
                        squareCenterY = (int) (squareUpperLeftY + squareHeight / 2);
                    }
                    Projection proj = theMap.getProjection();
                    Point2D upperLeft = proj.inverse(squareUpperLeftX, squareUpperLeftY, new LatLonPoint.Double());
                    Point2D lowerRight =
                            proj.inverse(squareUpperLeftX + (int) (squareWidth), squareUpperLeftY + (int) (squareHeight),
                                         new LatLonPoint.Double());
                    Point2D center = proj.inverse(squareCenterX, squareCenterY);
                    double necessaryScale = proj.getScale(upperLeft, lowerRight, proj.forward(upperLeft), proj.forward(lowerRight));
                    final Projection newProj =
                            theMap.getProjectionFactory().makeProjection(proj.getClass(), center, (float) necessaryScale,
                                                                         theMap.getWidth(), theMap.getHeight());

                    Thread delayThread = new Thread() {
                        public void run() {
                            theMap.setProjection(newProj);
                        }
                    };

                    java.awt.image.BufferedImage bi =
                            theMap.getGraphicsConfiguration().createCompatibleImage(theMap.getWidth(), theMap.getHeight(),
                                                                                    Transparency.OPAQUE);
                    theMap.paintAll(bi.getGraphics());
                    java.awt.image.BufferedImage bi2 =
                            bi.getSubimage(squareUpperLeftX, squareUpperLeftY, (int) squareWidth, (int) squareHeight);
                    java.awt.image.BufferedImage square =
                            new java.awt.image.BufferedImage((int) squareWidth, (int) squareHeight,
                                                             java.awt.image.BufferedImage.TYPE_INT_RGB);
                    square.getGraphics().drawImage(bi2, 0, 0, (int) squareWidth, (int) squareHeight, null);
                    square.getGraphics().setColor(new Color(0, 255, 0, 255));
                    square.getGraphics().drawRect(0, 0, square.getWidth() - 1, square.getHeight() - 1);
                    delayThread.start();
                    double iterations = 10;
                    double widthIncrease = theMap.getWidth() - squareWidth;
                    double heightIncrease = theMap.getHeight() - squareHeight;
                    double widthInc = widthIncrease / iterations;
                    double heightInc = heightIncrease / iterations;
                    double leftInc = squareUpperLeftX / iterations;
                    double upInc = squareUpperLeftY / iterations;

                    for (int i = 0; i < iterations + 1; i++) {
                        theMap.getGraphics(true).drawImage(square, squareUpperLeftX - (int) (leftInc * i),
                                                           squareUpperLeftY - (int) (upInc * i),
                                                           (int) squareWidth + (int) (widthInc * i),
                                                           (int) squareHeight + (int) (heightInc * i), null);
                        try {
                            Thread.sleep(50);
                        } catch (Exception ex) {
                        }
                    }
                }
            }
        }
    }

}
