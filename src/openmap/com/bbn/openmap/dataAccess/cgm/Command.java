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

import java.io.DataInputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Command implements Cloneable {

    protected static Logger logger = Logger.getLogger("com.bbn.openmap.dataAccess.cgm.Command");

    int args[];
    int ElementClass;
    int ElementId;

    public Command(int ec, int eid, int l, DataInputStream in) throws IOException {

        ElementClass = ec;
        ElementId = eid;
        if (l != 31) {
            args = new int[l];
            for (int i = 0; i < l; i++)
                args[i] = in.read();
            if (l % 2 != 0)
                in.read();

        } else {
            l = read16(in);
            args = new int[l];
            for (int i = 0; i < l; i++)
                args[i] = in.read();
            if (l % 2 != 0)
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

    public void paint(CGMDisplay d) {
    }

    public void scale(CGMDisplay d) {
    }

    public static Command read(DataInputStream in) throws IOException {

        int k = in.read();
        if (k == -1)
            return null;
        k = (k << 8) | in.read();
        int ec = k >> 12;
        int eid = (k >> 5) & 127;
        int l = k & 31;

        switch (ec) {
        // Delimiter elements
        case 0:
            switch (eid) {
            case 0:
                // NOOP
                break;
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
            case 6:
                return new DummyCommand(ec, eid, l, in, "Begin Segment");
            case 7:
                return new DummyCommand(ec, eid, l, in, "End Segment");
            case 8:
                return new DummyCommand(ec, eid, l, in, "Begin Figure");
            case 9:
                return new DummyCommand(ec, eid, l, in, "End Figure");
            case 13:
                return new DummyCommand(ec, eid, l, in, "Being Protection Region");
            case 14:
                return new DummyCommand(ec, eid, l, in, "End Protection Region");
            case 15:
                return new DummyCommand(ec, eid, l, in, "Begin Compound Line");
            case 16:
                return new DummyCommand(ec, eid, l, in, "End Compound Line");
            case 17:
                return new DummyCommand(ec, eid, l, in, "Begin Compound Text Path");
            case 18:
                return new DummyCommand(ec, eid, l, in, "End Compound Text Path");
            case 19:
                return new DummyCommand(ec, eid, l, in, "Begin Tile Array");
            case 20:
                return new DummyCommand(ec, eid, l, in, "End Tile Array");
            case 21:
                return new DummyCommand(ec, eid, l, in, "Begin Application Structure");
            case 22:
                return new DummyCommand(ec, eid, l, in, "Begin Application Structure Body");
            case 23:
                return new DummyCommand(ec, eid, l, in, "End Application Structure");
            default:
                break;
            }
            break;
        // Metafile descriptor elements
        case 1:
            switch (eid) {
            case 1:
                return new MetafileVersion(ec, eid, l, in);
            case 2:
                return new MetafileDescription(ec, eid, l, in);
            case 3:
                return new DummyCommand(ec, eid, l, in, "VDC Type");
            case 4:
                return new DummyCommand(ec, eid, l, in, "Integer Precision");
            case 5:
                return new DummyCommand(ec, eid, l, in, "Real Precision");
            case 6:
                return new DummyCommand(ec, eid, l, in, "Index Precision");
            case 7:
                return new DummyCommand(ec, eid, l, in, "Color Precision");
            case 8:
                return new DummyCommand(ec, eid, l, in, "Color Index Precision");
            case 9:
                return new DummyCommand(ec, eid, l, in, "Maximum Color Index");
            case 10:
                return new DummyCommand(ec, eid, l, in, "Color Value Extent");
            case 11:
                return new MetafileElementList(ec, eid, l, in);
            case 12:
                return new DummyCommand(ec, eid, l, in, "Metafile Defaults Replacement");
            case 13:
                return new FontList(ec, eid, l, in);
            case 14:
                return new DummyCommand(ec, eid, l, in, "Character Set List");
            case 15:
                return new DummyCommand(ec, eid, l, in, "Character Coding Announcer");
            case 16:
                return new DummyCommand(ec, eid, l, in, "Name Precision");
            case 17:
                return new DummyCommand(ec, eid, l, in, "Maximum VDC Extent");
            case 18:
                return new DummyCommand(ec, eid, l, in, "Segment Priority Extent");
            case 19:
                return new ColorModel(ec, eid, l, in);
            case 20:
                return new DummyCommand(ec, eid, l, in, "Color Calibration");
            case 21:
                return new DummyCommand(ec, eid, l, in, "Font Properties");
            case 22:
                return new DummyCommand(ec, eid, l, in, "Glyph Mapping");
            case 23:
                return new DummyCommand(ec, eid, l, in, "Symbol Library List");
            case 24:
                return new DummyCommand(ec, eid, l, in, "Picture Directory");
            default:
                break;
            }
            break;
        // Picture Descriptor Elements
        case 2:
            switch (eid) {
            case 1:
                return new DummyCommand(ec, eid, l, in, "Scaling Mode");
            case 2:
                return new ColorSelectionMode(ec, eid, l, in);
            case 3:
                return new LineWidthMode(ec, eid, l, in);
            case 4:
                return new DummyCommand(ec, eid, l, in, "Marker Size Specification Mode");
            case 5:
                return new EdgeWidthMode(ec, eid, l, in);
            case 6:
                return new VDCExtent(ec, eid, l, in);
            case 7:
                return new DummyCommand(ec, eid, l, in, "Background Color");
            case 8:
                return new DummyCommand(ec, eid, l, in, "Device Viewport");
            case 9:
                return new DummyCommand(ec, eid, l, in, "Device Viewport Specification Mode");
            case 10:
                return new DummyCommand(ec, eid, l, in, "Device Viewport Mapping");
            case 11:
                return new DummyCommand(ec, eid, l, in, "Line Representation");
            case 12:
                return new DummyCommand(ec, eid, l, in, "Marker Representation");
            case 13:
                return new DummyCommand(ec, eid, l, in, "Text Representation");
            case 14:
                return new DummyCommand(ec, eid, l, in, "Fill Representation");
            case 15:
                return new DummyCommand(ec, eid, l, in, "Edge Representation");
            case 16:
                return new DummyCommand(ec, eid, l, in, "Interior Style Specification Mode");
            case 17:
                return new DummyCommand(ec, eid, l, in, "Line and Edge Type Definition");
            case 18:
                return new DummyCommand(ec, eid, l, in, "Hatch Style Definition");
            case 19:
                return new DummyCommand(ec, eid, l, in, "Geometric Pattern Definition");
            case 20:
                return new DummyCommand(ec, eid, l, in, "Application Structure Directory");
            default:
                break;
            }
            break;
        // Control Elements
        case 3:
            switch (eid) {
            case 1:
                return new DummyCommand(ec, eid, l, in, "VDC Integer Precision");
            case 2:
                return new DummyCommand(ec, eid, l, in, "VDC Real Precision");
            case 3:
                return new DummyCommand(ec, eid, l, in, "Auxiliary Color");
            case 4:
                return new DummyCommand(ec, eid, l, in, "Transparency");
            case 5:
                return new DummyCommand(ec, eid, l, in, "Clip Rectangle");
            case 6:
                return new DummyCommand(ec, eid, l, in, "Clip Indicator");
            case 7:
                return new DummyCommand(ec, eid, l, in, "Line Clipping Mode");
            case 8:
                return new DummyCommand(ec, eid, l, in, "Marker Clipping Mode");
            case 9:
                return new DummyCommand(ec, eid, l, in, "Edge Clipping Mode");
            case 10:
                return new DummyCommand(ec, eid, l, in, "New Region");
            case 11:
                return new DummyCommand(ec, eid, l, in, "Save Primitive Context");
            case 12:
                return new DummyCommand(ec, eid, l, in, "Restore Primitive Context");
            case 17:
                return new DummyCommand(ec, eid, l, in, "Protection Region Indicator");
            case 18:
                return new DummyCommand(ec, eid, l, in, "Generalized Text Path Mode");
            case 19:
                return new DummyCommand(ec, eid, l, in, "Mitre Limit");
            case 20:
                return new DummyCommand(ec, eid, l, in, "Transparent Cell Color");
            default:
                break;
            }
            break;
        // Graphical Primitive Elements
        case 4:
            switch (eid) {
            case 1:
                return new PolylineElement(ec, eid, l, in);
            case 2:
                return new DummyCommand(ec, eid, l, in, "Disjoint Polyline");
            case 3:
                return new DummyCommand(ec, eid, l, in, "Polymarker");
            case 4:
                return new TextElement(ec, eid, l, in);
            case 5:
                return new DummyCommand(ec, eid, l, in, "Text");
            case 6:
                return new DummyCommand(ec, eid, l, in, "Restricted Text");
            case 7:
                return new PolygonElement(ec, eid, l, in);
            case 8:
                return new DummyCommand(ec, eid, l, in, "Append Text");
            case 9:
                return new DummyCommand(ec, eid, l, in, "Cell Array");
            case 10:
                return new DummyCommand(ec, eid, l, in, "Generalized Drawing Primitive");
            case 11:
                return new RectangleElement(ec, eid, l, in);
            case 12:
                return new CircleElement(ec, eid, l, in);
            case 13:
                return new DummyCommand(ec, eid, l, in, "Circular Arc Point");
            case 14:
                return new DummyCommand(ec, eid, l, in, "Circular Arc 3 Point Close");
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
            case 20:
                return new DummyCommand(ec, eid, l, in, "Circular Arc Center Reversed");
            case 21:
                return new DummyCommand(ec, eid, l, in, "Connecting Edge");
            case 22:
                return new DummyCommand(ec, eid, l, in, "Hyperbolic Arc");
            case 23:
                return new DummyCommand(ec, eid, l, in, "Parabolic Arc");
            case 24:
                return new DummyCommand(ec, eid, l, in, "Non-Uniform B-Spline");
            case 25:
                return new DummyCommand(ec, eid, l, in, "Non-Uniform Rational B-Spline");
            case 26:
                return new DummyCommand(ec, eid, l, in, "Polybezier");
            case 27:
                return new DummyCommand(ec, eid, l, in, "Polysymbol");
            case 28:
                return new DummyCommand(ec, eid, l, in, "Bitonal Tile");
            case 29:
                return new DummyCommand(ec, eid, l, in, "Tile");
            default:
                break;
            }
            break;
        // Attribute Elements
        case 5:
            switch (eid) {
            case 1:
                return new DummyCommand(ec, eid, l, in, "Line Bundle Index");
            case 2:
                return new LineType(ec, eid, l, in);
            case 3:
                return new LineWidth(ec, eid, l, in);
            case 4:
                return new LineColor(ec, eid, l, in);
            case 5:
                return new DummyCommand(ec, eid, l, in, "Marker Bundle Index");
            case 6:
                return new DummyCommand(ec, eid, l, in, "Marker Type");
            case 7:
                return new DummyCommand(ec, eid, l, in, "Marker Size");
            case 8:
                return new DummyCommand(ec, eid, l, in, "Marker Color");
            case 9:
                return new DummyCommand(ec, eid, l, in, "Text Bundle Index");
            case 10:
                return new TextFontIndex(ec, eid, l, in);
            case 11:
                return new DummyCommand(ec, eid, l, in, "Text Precision");
            case 12:
                return new DummyCommand(ec, eid, l, in, "Character Expansion Factor");
            case 13:
                return new DummyCommand(ec, eid, l, in, "Character Spacing");
            case 14:
                return new TextColor(ec, eid, l, in);
            case 15:
                return new CharacterHeight(ec, eid, l, in);
            case 16:
                return new DummyCommand(ec, eid, l, in, "Character Orientation");
            case 17:
                return new DummyCommand(ec, eid, l, in, "Text Path");
            case 18:
                return new DummyCommand(ec, eid, l, in, "Text Alignment");
            case 19:
                return new DummyCommand(ec, eid, l, in, "Character Set Index");
            case 20:
                return new DummyCommand(ec, eid, l, in, "Alternate Character Set Index");
            case 21:
                return new DummyCommand(ec, eid, l, in, "Fill Bundle Index");
            case 22:
                return new InteriorStyle(ec, eid, l, in);
            case 23:
                return new FillColor(ec, eid, l, in);
            case 24:
                return new DummyCommand(ec, eid, l, in, "Hatch Index");
            case 25:
                return new DummyCommand(ec, eid, l, in, "Pattern Index");
            case 26:
                return new DummyCommand(ec, eid, l, in, "Edge Bundle Index");
            case 27:
                return new EdgeType(ec, eid, l, in);
            case 28:
                return new EdgeWidth(ec, eid, l, in);
            case 29:
                return new EdgeColor(ec, eid, l, in);
            case 30:
                return new EdgeVisibility(ec, eid, l, in);
            case 31:
                return new DummyCommand(ec, eid, l, in, "Fill Reference Point");
            case 32:
                return new DummyCommand(ec, eid, l, in, "Pattern Table");
            case 33:
                return new DummyCommand(ec, eid, l, in, "Pattern Size");
            case 34:
                return new ColorTable(ec, eid, l, in);
            case 35:
                return new DummyCommand(ec, eid, l, in, "Aspect Source Flags");
            case 36:
                return new DummyCommand(ec, eid, l, in, "Pick Indentifier");
            case 37:
                return new DummyCommand(ec, eid, l, in, "Line Cap");
            case 38:
                return new DummyCommand(ec, eid, l, in, "Line Join");
            case 39:
                return new DummyCommand(ec, eid, l, in, "Line Type Continuation");
            case 40:
                return new DummyCommand(ec, eid, l, in, "Line Type Initial Offset");
            case 41:
                return new DummyCommand(ec, eid, l, in, "Text Score Type");
            case 42:
                return new DummyCommand(ec, eid, l, in, "Restricted Text Type");
            case 43:
                return new DummyCommand(ec, eid, l, in, "Interpolated Interior");
            case 44:
                return new DummyCommand(ec, eid, l, in, "Edge Cap");
            case 45:
                return new DummyCommand(ec, eid, l, in, "Edge Join");
            case 46:
                return new DummyCommand(ec, eid, l, in, "Edge Type Continuation");
            case 47:
                return new DummyCommand(ec, eid, l, in, "Edge Type Initial Offset");
            case 48:
                return new DummyCommand(ec, eid, l, in, "Symbol Library Index");
            case 49:
                return new DummyCommand(ec, eid, l, in, "Symbol Color");
            case 50:
                return new DummyCommand(ec, eid, l, in, "Symbol Size");
            case 51:
                return new DummyCommand(ec, eid, l, in, "Symbol Orientation");
            default:
                break;
            }
            break;
        // Escape Element
        case 6:
            break;
        // External Elements
        case 7:
            break;
        // Segment Control and Segment Attribute Elements
        case 8:
            break;
        // Application Structure Descriptor Elements
        case 9:
            switch (eid) {
            case 1:
                return new ApplicationStructureAttribute(ec, eid, l, in);
            default:
                break;
            }
            break;
        default:
            break;
        }

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Ignored Opcode: " + ec + "/" + eid);
        }

        return new Command(ec, eid, l, in);
    }

    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
    }

}