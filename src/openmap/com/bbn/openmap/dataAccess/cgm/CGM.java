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

import java.io.*;
import java.util.*;
import java.awt.Color;

public class CGM implements Cloneable {
    protected Vector V;
    public boolean changeCGMFill = false;

    public void setChangeFill(boolean custom) {
        changeCGMFill = custom;
    }

    public void read(DataInputStream in) throws IOException {
        V = new Vector();
        while (true) {
            Command c = Command.read(in);
            if (c == null)
                break;
            V.addElement(c);
        }
    }

    public void paint(CGMDisplay d) {
        Enumeration e = V.elements();
        while (e.hasMoreElements()) {
            Command c = (Command) e.nextElement();
            if (!((c instanceof FillColor || c instanceof ColorCommand) && changeCGMFill)) {
                // FillColor.paint changes the fill color of d
                c.paint(d);
            } else {
                /*
                 * System.out.println("Command not painted: " +
                 * c.toString());
                 */
            }
        }
    }

    public void scale(CGMDisplay d) {
        Enumeration e = V.elements();
        while (e.hasMoreElements()) {
            Command c = (Command) e.nextElement();
            c.scale(d);
        }
    }

    public int[] extent() {
        Enumeration e = V.elements();
        while (e.hasMoreElements()) {
            Command c = (Command) e.nextElement();
            if (c instanceof VDCExtent)
                return ((VDCExtent) c).extent();
        }
        return null;
    }

    public static void main(String args[]) throws IOException {
        DataInputStream in = new DataInputStream(new FileInputStream(args[0]));
        CGM cgm = new CGM();
        cgm.read(in);
        in.close();
    }

    public Object clone() {
        CGM newOne = new CGM();
        //System.out.println("in cgm.clone");
        newOne.V = new Vector();
        for (int i = 0; i < this.V.size(); i++) {
            newOne.V.addElement(((Command) this.V.elementAt(i)).clone());
            //System.out.println("Command: " +
            // (Command)newOne.V.elementAt(i));
        }
        return newOne;
    }

    public void showCGMCommands() {
        for (int i = 0; i < V.size(); i++)
            System.out.println("Command: " + (Command) V.elementAt(i));
    }

    public void changeColor(Color oldc, Color newc) {
        // actually changes the color in the cgm commands having this
        // oldc, replacing it with newc find each color command whose
        // color matches oldc, and substitute newc
        Command temp;
        Color currcolor;

        for (int i = 0; i < V.size(); i++) {
            temp = (Command) V.elementAt(i);
            if (temp instanceof ColorCommand) {// compare color to
                                               // oldc
                currcolor = ((ColorCommand) temp).C;
                if (currcolor.equals(oldc)) {
                    ((ColorCommand) temp).C = new Color(newc.getRed(), newc.getGreen(), newc.getBlue());
                }
            }
        }
    }
}