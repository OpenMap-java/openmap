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

public class Command implements Cloneable {
    int args[];
    int ElementClass;
    int ElementId;

    public Command(int ec, int eid, int l, DataInputStream in)
            throws IOException {

        ElementClass = ec;
        ElementId = eid;
        if (l != 31) {
            args = new int[l];
            for (int i = 0; i < l; i++)
                args[i] = in.read();
            if (l % 2 == 1)
                in.read();

        } else {
            l = read16(in);
            args = new int[l];
            for (int i = 0; i < l; i++)
                args[i] = in.read();
            if (l % 2 == 1)
                in.read();
        }
    }

    public int read16(DataInputStream in) throws IOException {
        return (in.read() << 8) | in.read();
    }

    public String toString() {
        return ElementClass + "," + ElementId + " (" + args.length + ")";
    }

    public String makeString() {
        if (args.length <= 0)
            return "";
        char a[] = new char[args.length - 1];
        for (int i = 0; i < a.length; i++)
            a[i] = (char) args[i + 1];
        return new String(a);
    }

    public String makeString(int k) {
        char a[] = new char[args[k]];
        for (int i = 0; i < a.length; i++)
            a[i] = (char) args[k + i + 1];
        return new String(a);
    }

    public int makeInt(int i) {
        return (int) ((short) (args[2 * i] << 8) + args[2 * i + 1]);
    }

    public void paint(CGMDisplay d) {}

    public void scale(CGMDisplay d) {}

    public static Command read(DataInputStream in) throws IOException {

        int k = in.read();
        if (k == -1)
            return null;
        k = (k << 8) | in.read();
        int ec = k >> 12;
        int eid = (k >> 5) & 127;
        int l = k & 31;
        switch (ec) {
        case 0:
            switch (eid) {
            case 1:
                return new BeginMetafile(ec, eid, l, in);
            case 2:
                return new EndMetafile(ec, eid, l, in);
            case 3:
                return new BeginPicture(ec, eid, l, in);
            case 4:
                return new BeginPictureBody(ec, eid, l, in);
            case 5:
                return new EndPicture(ec, eid, l, in);
            default:
                return new Command(ec, eid, l, in);
            }
        case 1:
            switch (eid) {
            case 1:
                return new MetafileVersion(ec, eid, l, in);
            case 2:
                return new MetafileDescription(ec, eid, l, in);
            case 11:
                return new MetafileElementList(ec, eid, l, in);
            case 13:
                return new FontList(ec, eid, l, in);
            default:
                return new Command(ec, eid, l, in);
            }
        case 2:
            switch (eid) {
            case 2:
                return new ColorSelectionMode(ec, eid, l, in);
            case 3:
                return new LineWidthMode(ec, eid, l, in);
            case 5:
                return new EdgeWidthMode(ec, eid, l, in);
            case 6:
                return new VDCExtent(ec, eid, l, in);
            default:
                return new Command(ec, eid, l, in);
            }
        case 4:
            switch (eid) {
            case 1:
                return new PolylineElement(ec, eid, l, in);
            case 4:
                return new TextElement(ec, eid, l, in);
            case 7:
                return new PolygonElement(ec, eid, l, in);
            case 11:
                return new RectangleElement(ec, eid, l, in);
            case 12:
                return new CircleElement(ec, eid, l, in);
            case 15:
                return new CircularArcElement(ec, eid, l, in);
            case 16:
                return new CircularArcClosedElement(ec, eid, l, in);
            case 17:
                return new EllipseElement(ec, eid, l, in);
            case 18:
                return new EllipticalArcElement(ec, eid, l, in);
            case 19:
                return new EllipticalArcClosedElement(ec, eid, l, in);
            default:
                return new Command(ec, eid, l, in);
            }
        case 5:
            switch (eid) {
            case 2:
                return new LineType(ec, eid, l, in);
            case 3:
                return new LineWidth(ec, eid, l, in);
            case 4:
                return new LineColor(ec, eid, l, in);
            case 10:
                return new TextFontIndex(ec, eid, l, in);
            case 14:
                return new TextColor(ec, eid, l, in);
            case 15:
                return new CharacterHeight(ec, eid, l, in);
            case 22:
                return new InteriorStyle(ec, eid, l, in);
            case 23:
                return new FillColor(ec, eid, l, in);
            case 27:
                return new EdgeType(ec, eid, l, in);
            case 28:
                return new EdgeWidth(ec, eid, l, in);
            case 29:
                return new EdgeColor(ec, eid, l, in);
            case 30:
                return new EdgeVisibility(ec, eid, l, in);
            case 40:
                return new EdgeType(ec, eid, l, in);
            case 48:
                return new LineColor(ec, eid, l, in);
            default:
                return new Command(ec, eid, l, in);
            }
        default:
            return new Command(ec, eid, l, in);
        }
    }

    public Object clone() {
        try {
            return super.clone();
        } catch (Exception e) {
            return null;
        }
    }

}