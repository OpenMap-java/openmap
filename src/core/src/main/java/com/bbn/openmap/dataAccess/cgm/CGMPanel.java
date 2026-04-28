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

import java.awt.Graphics;
import java.awt.Panel;

/**
 * A simple Panel that manages and renders a CGMDisplay object (which contains a
 * CGM).
 * 
 * @author dietrick
 */
public class CGMPanel
      extends Panel {

   private static final long serialVersionUID = 1L;
   CGMDisplay D;
   int W = 0, H = 0;

   public CGMPanel(CGMDisplay d) {
      D = d;
   }

   public void paint(Graphics g) {
      int W0 = getSize().width, H0 = getSize().height;
//      if (W0 != W || H0 != H) {
         W = W0;
         H = H0;
         D.scale(W, H);
//      }
      D.frame(g);
      D.paint(g);
   }
}