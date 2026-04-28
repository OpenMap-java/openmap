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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/tools/icon/IconFactoryTestingTool.java,v $
// $RCSfile: IconFactoryTestingTool.java,v $
// $Revision: 1.6 $
// $Date: 2006/03/06 16:01:31 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.tools.icon;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Container;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;

import com.bbn.openmap.gui.Tool;
import com.bbn.openmap.omGraphics.DrawingAttributes;

/**
 * An example class and example for how to use the OMIconFactory and IconParts
 * to create Icons. Can be run as a class, or can be used as an OpenMap Tool to
 * show up on the OpenMap ToolPanel.
 */
public class IconFactoryTestingTool
      implements Tool {

   public IconFactoryTestingTool() {
   }

   /**
    * The retrieval tool's interface. This is added to the tool bar.
    * 
    * @return String The key for this tool.
    */
   public Container getFace() {
      JToolBar jtb = new JToolBar();
      jtb.setFloatable(false);

      DrawingAttributes da = new DrawingAttributes();
      da.setLinePaint(Color.blue);
      da.setFillPaint(Color.blue);
      da.setStroke(new BasicStroke(2));
      DrawingAttributes da2 = new DrawingAttributes();
      da2.setFillPaint(Color.lightGray);
      da2.setLinePaint(Color.lightGray);
      da2.setStroke(new BasicStroke(2));

      int[] xpoints = new int[] {
         15,
         15,
         50,
         50,
         90,
         50,
         50,
         15
      };
      int[] ypoints = new int[] {
         30,
         70,
         70,
         90,
         50,
         10,
         30,
         30
      };
      Shape shape = new Polygon(xpoints, ypoints, xpoints.length);

      BasicIconPart testPart = new BasicIconPart(shape);
      testPart.setRenderingAttributes(da);
      testPart.setGradient(true);

      Shape shape2 = new Ellipse2D.Double(5, 5, 90, 90);
      BasicIconPart testPart2 = new BasicIconPart(shape2);
      testPart2.setRenderingAttributes(da2);
      testPart2.setGradient(true);

      IconPartList parts = new IconPartList();
      parts.add(testPart2);
      parts.add(testPart);

      BasicIconPart testPart3 = new BasicIconPart(shape, AffineTransform.getRotateInstance(Math.PI / 4, 50, 50));
      testPart3.setRenderingAttributes(da);
      testPart3.setGradient(true);

      IconPartList parts2 = new IconPartList();
      parts2.add(testPart2);
      parts2.add(testPart3);

      jtb.add(new JButton(OMIconFactory.getIcon(10, 10, parts)));
      jtb.add(new JButton(OMIconFactory.getIcon(20, 20, parts)));
      jtb.add(new JButton(OMIconFactory.getIcon(50, 50, parts)));
      jtb.add(new JButton(OMIconFactory.getIcon(50, 50, parts2)));
      jtb.add(new JButton(OMIconFactory.getIcon(10, 20, parts2)));

      return jtb;
   }

   /**
    * The retrieval key for this tool
    * 
    * @return String The key for this tool.
    */
   public String getKey() {
      return "IconFactoryTestingTool";
   }

   /**
    * Set the retrieval key for this tool
    * 
    * @param aKey The key for this tool.
    */
   public void setKey(String aKey) {
   }

   public static void main(String[] argv) {
      JFrame frame = new JFrame("IconFactoryTestingTool");
      frame.getContentPane().add(new IconFactoryTestingTool().getFace());
      frame.pack();
      frame.addWindowListener(new WindowAdapter() {
         public void windowClosing(WindowEvent e) {
            // need a shutdown event to notify other gui beans and
            // then exit.
            System.exit(0);
         }
      });

      frame.setVisible(true);
   }

   public void setOrientation(int orientation) {
   }

   public int getOrientation() {
      return SwingConstants.HORIZONTAL;
   }
}