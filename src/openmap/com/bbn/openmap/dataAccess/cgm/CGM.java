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
import java.awt.Graphics;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Vector;
import java.util.logging.Logger;

public class CGM
      implements Cloneable {
   protected Vector<Command> commandList;

   protected static Logger logger = Logger.getLogger("com.bbn.openmap.dataAccess.cgm.CGM");

   public CGM() {

   }

   public CGM(String path)
         throws IOException {
      DataInputStream in = new DataInputStream(new FileInputStream(path));
      read(in);
      in.close();
   }

   public void read(DataInputStream in)
         throws IOException {
      commandList = new Vector<Command>();
      while (true) {
         Command c = Command.read(in);
         if (c == null)
            break;
         commandList.addElement(c);
      }

      sortColors();
   }

   /**
    * The paint call managed by the CGMDisplay object, which holds changes to
    * the Graphics object based on different Commands held by this CGM.
    * 
    * @param d
    */
   protected void paint(CGMDisplay d) {
      for (Command c : commandList) {
         c.paint(d);
      }
   }

   /**
    * A direct call to paint on the CGM file, creates a CGMDisplay that marches
    * through the CGM commands and renders into the provided Graphics object.
    * 
    * @param g java Graphics object
    * @param width the pixel width of image to render into.
    * @param height the pixel height of image to render info.
    */
   public void paint(Graphics g, int width, int height) {
      CGMDisplay cgmDisplay = new CGMDisplay(this);
      cgmDisplay.scale(width, height);
      cgmDisplay.paint(g);
   }

   /**
    * Allows color commands to look up indexes in ColorTable to find real
    * values.
    */
   protected void sortColors() {
      ColorTable ct = null;
      boolean indexed = false;
      for (Command c : commandList) {
         if (c instanceof ColorSelectionMode) {
            if (((ColorSelectionMode) c).selectionMode == 0) {
               // indexed
               indexed = true;
            }
         } else if (c instanceof ColorTable) {
            ct = (ColorTable) c;
         } else if (c instanceof ColorModel) {
            logger.fine("Not handling other colormodels than rgb (indexed and direct)");
         }
      }

      if (indexed && ct != null) {
         for (Command c : commandList) {
            if (c instanceof ColorCommand) {
               ((ColorCommand) c).setColorFromColorTable(ct);
            }
         }
      }
   }

   public void scale(CGMDisplay d) {
      for (Command c : commandList) {
         c.scale(d);
      }
   }

   public int[] extent() {
      for (Command c : commandList) {
         if (c instanceof VDCExtent)
            return ((VDCExtent) c).extent();
      }
      return null;
   }

   public ColorTable getColorTable() {
      for (Command c : commandList) {
         if (c instanceof ColorTable)
            return (ColorTable) c;
      }
      return null;
   }

   public static void main(String args[])
         throws IOException {
      DataInputStream in = new DataInputStream(new FileInputStream(args[0]));
      CGM cgm = new CGM();
      cgm.read(in);
      in.close();
   }

   public Object clone() {
      CGM newOne = new CGM();
      // System.out.println("in cgm.clone");
      newOne.commandList = new Vector<Command>();
      for (int i = 0; i < this.commandList.size(); i++) {
         newOne.commandList.addElement((Command) (this.commandList.elementAt(i)).clone());
         // System.out.println("Command: " +
         // (Command)newOne.V.elementAt(i));
      }
      return newOne;
   }

   public void showCGMCommands() {
      for (Command c : commandList) {
         System.out.println("Command: " + c);
      }
   }

   public void changeColor(Color oldc, Color newc) {
      // actually changes the color in the cgm commands having this
      // oldc, replacing it with newc find each color command whose
      // color matches oldc, and substitute newc
      Command temp;
      Color currcolor;

      for (int i = 0; i < commandList.size(); i++) {
         temp = (Command) commandList.elementAt(i);
         if (temp instanceof ColorCommand) {// compare color to
                                            // oldc
            currcolor = ((ColorCommand) temp).C;
            if (currcolor.equals(oldc)) {
               ((ColorCommand) temp).C = new Color(newc.getRed(), newc.getGreen(), newc.getBlue());
            }
         }
      }
   }
   
   public String toString() {
      if (commandList != null) {
         StringBuffer buf = new StringBuffer();
         int count = 0;
         for (Command c : commandList) {
                buf.append("Command ").append(count++).append(": ").append(c.toString()).append("\n");
         }
         return buf.toString();
      } else {
         return "CGM: not read yet?";
      }
   }
}