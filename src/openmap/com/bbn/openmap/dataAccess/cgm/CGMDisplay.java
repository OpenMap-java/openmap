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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import com.bbn.openmap.omGraphics.OMColor;

public class CGMDisplay {
   Graphics G;

   protected double X = 0, Y = 0;
   protected double DX = 1, DY = 1;
   protected int W, H;
   protected CGM Cgm;
   Color FillColor = OMColor.clear, EdgeColor = Color.black, LineColor = Color.black, TextColor = Color.black;
   boolean Filled = true, Edge = true;
   int TextSize = 10;
   protected int Extent[] = {
      -30000,
      -30000,
      30000,
      30000
   };

   public CGMDisplay(CGM cgm) {
      load(cgm);
   }

   public void load(CGM cgm) {
      Cgm = cgm;
      int extent[] = cgm.extent();
      if (extent != null)
         Extent = extent;
   }

   public void paint(Graphics g) {
      G = g;
      Cgm.paint(this);
   }

   public int x(int x) {
      return W + (int) (X + x * DX);
   }

   public int y(int y) {
      return H - (int) (Y + y * DY);
   }

   public Graphics graphics() {
      return G;
   }

   public void setFillColor(Color c) {
      FillColor = c;
   }

   public Color getFillColor() {
      return FillColor;
   }

   public void setFilled(boolean flag) {
      Filled = flag;
   }

   public boolean getFilled() {
      return Filled;
   }

   public void setEdgeColor(Color c) {
      EdgeColor = c;
   }

   public Color getEdgeColor() {
      return EdgeColor;
   }

   public void setEdge(boolean flag) {
      Edge = flag;
   }

   public boolean getEdge() {
      return Edge;
   }

   public void setLineColor(Color c) {
      LineColor = c;
   }

   public Color getLineColor() {
      return LineColor;
   }

   public void setTextColor(Color c) {
      TextColor = c;
   }

   public Color getTextColor() {
      return TextColor;
   }

   public void setTextSize(int h) {
      TextSize = h;
   }

   public int getTextSize() {
      return TextSize;
   }

   public double factorX() {
      return DX;
   }

   public double factorY() {
      return DY;
   }

   public void scale(int w, int h) {
      if (Extent == null)
         return;
      double fx = (double) w / (Extent[2] - Extent[0]);
      if (fx * (Extent[3] - Extent[1]) > h) {
         fx = (double) h / (Extent[3] - Extent[1]);
      }
      fx *= 1.0; // 0.9;
      DX = fx;
      DY = fx;
      X = -Extent[0] * fx;
      Y = -Extent[1] * fx;
      W = (int) (w - fx * (Extent[2] - Extent[0])) / 2;
      H = (int) (h - (h - fx * (Extent[3] - Extent[1])) / 2);
      Cgm.scale(this);
   }
   
   public BufferedImage getBufferedImage(int w, int h) {
      BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
      Graphics g = image.getGraphics();
      scale(w, h);
      paint(g);
      return image;
   }

   public void frame(Graphics g) {
      if (Extent == null)
         return;
      g.setColor(Color.black);
      g.drawRect(x(Extent[0]) - 1, y(Extent[3]) - 1, (int) Math.abs((Extent[2] - Extent[0]) * DX) + 1,
                 (int) Math.abs((Extent[3] - Extent[1]) * DY) + 1);
   }

   public static void main(String args[])
         throws IOException {
      if (args.length == 0) {
         System.out.println("Need a path to a cgm file or directory containing cgm files.");
         System.exit(-1);
      }

      File file = new File(args[0]);
      File cgmFile;
      if (!file.exists()) {
         System.out.println("Can't find file: " + args[0]);
      }

      JPanel choicePanel = null;
      JComboBox comboBox = null;
      JButton nextButton = null;
      JButton prevButton = null;

      String[] files = null;
      if (file.isDirectory()) {

         files = file.list();
         cgmFile = new File(file, files[0]);
         choicePanel = new JPanel();
         comboBox = new JComboBox(files);
         nextButton = new JButton("Next");
         nextButton.setName("Next");
         prevButton = new JButton("Previous");
         prevButton.setName("Previous");
         choicePanel.add(prevButton);
         choicePanel.add(comboBox);
         choicePanel.add(nextButton);

      } else {
         cgmFile = file;
      }

      DataInputStream in = new DataInputStream(new FileInputStream(cgmFile));
      CGM cgm = new CGM();
      cgm.read(in);
      in.close();
      CGMDisplay d = new CGMDisplay(cgm);
      CGMPanel p = new CGMPanel(d);
      Frame f = new Frame();
      f.addWindowListener(new WindowAdapter() {
         public void windowClosing(WindowEvent e) {
            System.exit(0);
         }
      });
      f.setSize(600, 450);
      f.setLayout(new BorderLayout());
      f.add("Center", p);

      if (choicePanel != null) {
         f.add("North", choicePanel);
      }

      if (comboBox != null && nextButton != null && prevButton != null) {
         ServeChoice sc = new ServeChoice(file, comboBox, d);
         comboBox.addActionListener(sc);
         nextButton.addActionListener(sc);
         prevButton.addActionListener(sc);
         sc.setRepainter(p);
      }
      
      f.setVisible(true);
   }

   protected static class ServeChoice
         implements ActionListener {

      JButton label;
      File parent;
      CGMDisplay d;
      JComboBox jcb;
      
      Component repainter;
      
      public ServeChoice(File parent, JComboBox jcb, CGMDisplay d) {
         this.parent = parent;
         this.d = d;
         this.jcb = jcb;
      }

      /*
       * (non-Javadoc)
       * 
       * @see
       * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent
       * )
       */
      public void actionPerformed(ActionEvent e) {
         Object source = e.getSource();
         if (source instanceof JButton) {
            String name = ((JButton) source).getName();
            if (name.equals("Next")) {
               if (jcb != null) {
                  int index = jcb.getSelectedIndex();
                  if (index < jcb.getItemCount() - 1) {
                     jcb.setSelectedIndex(index + 1);
                  }
               }
            } else if (name.equals("Previous")) {
               if (jcb != null) {
                  int index = jcb.getSelectedIndex();
                  if (index > 1) {
                     jcb.setSelectedIndex(index - 1);
                  }
               }
            }
         } else if (source instanceof JComboBox) {
            JComboBox jcb = (JComboBox) source;

            String newName = (String) jcb.getSelectedItem();
            File cgmFile = new File(parent, newName);
            DataInputStream in;
            try {
               in = new DataInputStream(new FileInputStream(cgmFile));
               CGM cgm = new CGM();
               cgm.read(in);
               in.close();

               System.out.println("*********************");
               System.out.println(cgm.toString());
               System.out.println("*********************");

               d.load(cgm);
               
               Component repainter = getRepainter();
               if (repainter != null) {
                  repainter.repaint();
               }
               
            } catch (FileNotFoundException e1) {
               e1.printStackTrace();
            } catch (IOException ioe) {
               ioe.printStackTrace();
            }
         }
      }

      public Component getRepainter() {
         return repainter;
      }

      public void setRepainter(Component repainter) {
         this.repainter = repainter;
      }
      
   }

   public void changeColor(Color oldc, Color newc) {// actually
                                                    // changes the
                                                    // color in the
                                                    // cgm commands
                                                    // having this
                                                    // oldc,
                                                    // replacing
      // it with newc
      Cgm.changeColor(oldc, newc);
   }

}