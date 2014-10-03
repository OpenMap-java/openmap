/*
 * <copyright> Copyright 1997-2003 BBNT Solutions, LLC under
 * sponsorship of the Defense Advanced Research Projects Agency
 * (DARPA).
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Cougaar Open Source License as
 * published by DARPA on the Cougaar Open Source Website
 * (www.cougaar.org).
 * 
 * THE COUGAAR SOFTWARE AND ANY DERIVATIVE SUPPLIED BY LICENSOR IS
 * PROVIDED 'AS IS' WITHOUT WARRANTIES OF ANY KIND, WHETHER EXPRESS OR
 * IMPLIED, INCLUDING (BUT NOT LIMITED TO) ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, AND WITHOUT
 * ANY WARRANTIES AS TO NON-INFRINGEMENT. IN NO EVENT SHALL COPYRIGHT
 * HOLDER BE LIABLE FOR ANY DIRECT, SPECIAL, INDIRECT OR CONSEQUENTIAL
 * DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE OF DATA OR PROFITS,
 * TORTIOUS CONDUCT, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
 * PERFORMANCE OF THE COUGAAR SOFTWARE. </copyright>
 */
package com.bbn.openmap.dataAccess.cgm;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.io.DataInputStream;
import java.io.IOException;

import com.bbn.openmap.MoreMath;

/**
 * These are defined as a center point and two conjugate diameter points.
 */
public class EllipseElement
      extends Command {
   int centerX, centerY, endX1, endY1, endX2, endY2;
   int centerXS, centerYS, endXS1, endYS1, endXS2, endYS2;

   public EllipseElement(int ec, int eid, int l, DataInputStream in)
         throws IOException {
      super(ec, eid, l, in);
      centerX = makeInt(0);
      centerY = makeInt(1);
      endX1 = makeInt(2);
      endY1 = makeInt(3);
      endX2 = makeInt(4);
      endY2 = makeInt(5);
   }

   public String toString() {
      return "Ellipse [" + centerX + "," + centerY + "] [" + endX1 + "," + endY1 + "] [" + endX2 + "," + endY2 + "]";
   }

   public void scale(CGMDisplay d) {
      centerXS = d.x(centerX);
      centerYS = d.y(centerY);
      endXS1 = d.x(endX1);
      endYS1 = d.y(endY1);
      endXS2 = d.x(endX2);
      endYS2 = d.y(endY2);
   }

   public void paint(CGMDisplay d) {

      int x1 = endXS1 - centerXS;
      int x2 = endYS1 - centerYS;
      int y1 = endXS2 - centerXS;
      int y2 = endYS2 - centerYS;

      // Hypotenuse (Pythagorean theorem)
      double radiusX = Math.sqrt(x1 * x1 + x2 * x2);
      double radiusY = Math.sqrt(y1 * y1 + y2 * y2);

      // Angle (Trigonometric ratios in right triangles)
      double angle;
      if (x1 != 0) {
         angle = Math.toDegrees(Math.tan(x2 / x1));
      } else {
         angle = MoreMath.HALF_PI_D;
      }

      AffineTransform at = AffineTransform.getRotateInstance(angle, centerXS, centerYS);

      Graphics2D g2 = (Graphics2D) d.graphics().create();
      g2.setTransform(at);

      if (d.getFilled()) {
         g2.setColor(d.getFillColor());
         g2.fillOval((int) (centerXS - radiusX), (int) (centerYS - radiusY), (int) (2 * radiusX), (int) (2 * radiusY));
      } else {
         g2.setColor(d.getFillColor());
         if (!d.getEdge())
            d.graphics().drawOval((int) (centerXS - radiusX), (int) (centerYS - radiusY), (int) (2 * radiusX), (int) (2 * radiusY));
      }
      if (d.getEdge()) {
         g2.setColor(d.getEdgeColor());
         g2.drawOval((int) (centerXS - radiusX), (int) (centerYS - radiusY), (int) (2 * radiusX), (int) (2 * radiusY));
      }
   }
}