/**
 * Booz|Allen|Hamilton 
 * 8283 Greensboro Dr. 
 * McLean, VA 22102-3888
 * 
 * This software was developed by Booz|Allen|Hamilton under U.S
 * Government contracts, and may be reproduced by or for the U.S
 * Government pursuant to the copyright license under the clause at
 * DFARS 252.227-7013 5-06-05
 * 
 * @author Richard B. Lane
 */
package com.bbn.openmap.event;

import java.awt.Color;
import java.awt.Transparency;
import java.awt.event.MouseEvent;

import com.bbn.openmap.LatLonPoint;
import com.bbn.openmap.MapBean;
import com.bbn.openmap.proj.Mercator;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.proj.ProjectionFactory;

/**
 * This mouse mode responds to a double click with an animation effect
 * of zooming in the map.
 */
public class ZoomMouseMode extends CoordMouseMode {
    protected double squareWidth = 50;
    public final static transient String modeID = "Zoom".intern();
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
                    // int currCol = currX / ((int) squareWidth);
                    // int currRow = currY / ((int) squareWidth);
                    // int squareUpperLeftX =
                    // (int)(currCol*squareWidth);
                    // int squareUpperLeftY =
                    // (int)(currRow*squareWidth);
                    int squareCenterX = currX;
                    int squareCenterY = currY;
                    int squareUpperLeftX = currX - ((int) squareWidth / 2);
                    int squareUpperLeftY = currY - ((int) squareWidth / 2);
                    double aspect = (double) theMap.getHeight()
                            / (double) theMap.getWidth();
                    double squareWidth = this.squareWidth;
                    double squareHeight = this.squareWidth;
                    if (aspect > 1) {
                        squareHeight *= aspect;
                        squareUpperLeftY = squareCenterY
                                - (int) (squareHeight / 2);
                    } else {
                        squareWidth /= aspect;
                        squareUpperLeftX = squareCenterX
                                - (int) (squareWidth / 2);
                    }

                    Projection proj = theMap.getProjection();
                    LatLonPoint upperLeft = proj.inverse(squareUpperLeftX,
                            squareUpperLeftY);
                    LatLonPoint lowerRight = proj.inverse(squareUpperLeftX
                            + (int) (squareWidth), squareUpperLeftY
                            + (int) (squareHeight));
                    LatLonPoint center = proj.inverse(squareCenterX,
                            squareCenterY);
                    double necessaryScale = proj.getScale(upperLeft,
                            lowerRight,
                            proj.forward(upperLeft),
                            proj.forward(lowerRight));
                    final Projection newProj = ProjectionFactory.makeProjection(Mercator.class,
                            (float) center.getLatitude(),
                            (float) center.getLongitude(),
                            (float) necessaryScale,
                            theMap.getWidth(),
                            theMap.getHeight());

                    Thread delayThread = new Thread() {
                        public void run() {
                            theMap.setProjection(newProj);
                        }
                    };

                    java.awt.image.BufferedImage bi = theMap.getGraphicsConfiguration()
                            .createCompatibleImage(theMap.getWidth(),
                                    theMap.getHeight(),
                                    Transparency.OPAQUE);
                    theMap.paintAll(bi.getGraphics());
                    java.awt.image.BufferedImage bi2 = bi.getSubimage(squareUpperLeftX,
                            squareUpperLeftY,
                            (int) squareWidth,
                            (int) squareHeight);
                    java.awt.image.BufferedImage square = new java.awt.image.BufferedImage((int) squareWidth, (int) squareHeight, java.awt.image.BufferedImage.TYPE_INT_RGB);
                    square.getGraphics().drawImage(bi2,
                            0,
                            0,
                            (int) squareWidth,
                            (int) squareHeight,
                            null);
                    square.getGraphics().setColor(new Color(0, 255, 0, 255));
                    square.getGraphics().drawRect(0,
                            0,
                            square.getWidth() - 1,
                            square.getHeight() - 1);
                    delayThread.start();
                    double iterations = 10;
                    double widthIncrease = theMap.getWidth() - squareWidth;
                    double heightIncrease = theMap.getHeight() - squareHeight;
                    double widthInc = widthIncrease / iterations;
                    double heightInc = heightIncrease / iterations;
                    double leftInc = squareUpperLeftX / iterations;
                    double upInc = squareUpperLeftY / iterations;

                    for (int i = 0; i < iterations + 1; i++) {
                        theMap.getGraphics().drawImage(square,
                                squareUpperLeftX - (int) (leftInc * i),
                                squareUpperLeftY - (int) (upInc * i),
                                (int) squareWidth + (int) (widthInc * i),
                                (int) squareHeight + (int) (heightInc * i),
                                null);
                        try {
                            Thread.sleep(50);
                        } catch (Exception ex) {
                            ;
                        }
                    }
                }
            }
        }
    }

}
