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

import java.awt.Color;
import java.io.DataInputStream;
import java.io.IOException;

import com.bbn.openmap.omGraphics.OMColor;

public class ColorCommand
      extends Command {
   int R, G, B;
   Color C;
   Color Colors[] = {
      Color.black,
      Color.white,
      Color.green,
      Color.yellow,
      Color.blue,
      Color.magenta,
      Color.cyan,
      Color.red,
      Color.black.brighter(),
      Color.white.darker(),
      Color.green.darker(),
      Color.yellow.darker(),
      Color.blue.darker(),
      Color.magenta.darker(),
      Color.cyan.darker(),
      Color.red.darker(),
   };
   int index = -1;

   public ColorCommand(int ec, int eid, int l, DataInputStream in)
         throws IOException {
      super(ec, eid, l, in);

      if (args.length >= 3) {
         R = args[0];
         G = args[1];
         B = args[2];
         C = new Color(R, G, B);
      } else if (args.length > 0 && args[0] >= 1 && args[0] <= Colors.length) {
         C = Colors[args[0] - 1];
      } else if (args.length == 1) {
         index = args[0];
         logger.fine("index set for color: " + index);
      } else {
         C = new Color(128, 128, 128);
      }
   }

   public String toString() {
      return "Fill Color Input " + R + "," + G + "," + B;
   }

   public void paint(CGMDisplay d) {
      d.setFillColor(C);
   }

   /**
    * @param ct
    */
   public void setColorFromColorTable(ColorTable ct) {
      if (index != -1) {
         C = ct.get(index);
         if (C != null) {
            R = C.getRed();
            G = C.getGreen();
            B = C.getBlue();
         } else {
            C = OMColor.clear;
            R = 0;
            G = 0;
            B = 0;
         }
      }
   }
}