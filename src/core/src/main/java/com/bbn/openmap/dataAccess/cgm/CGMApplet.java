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

import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Color;
import java.io.DataInputStream;
import java.net.URL;

public class CGMApplet extends Applet {
    public void init() {
        String file = getParameter("File");
        if (file == null)
            return;
        setBackground(new Color(244, 244, 242));
        try {
            URL url = new URL(getCodeBase(), file);
            DataInputStream in = new DataInputStream(url.openStream());
            CGM cgm = new CGM();
            cgm.read(in);
            in.close();
            setLayout(new BorderLayout());
            CGMDisplay d = new CGMDisplay(cgm);
            CGMPanel p = new CGMPanel(d);
            add("Center", p);
        } catch (Exception e) {
            System.out.println(e);
            return;
        }
        repaint();
    }
}