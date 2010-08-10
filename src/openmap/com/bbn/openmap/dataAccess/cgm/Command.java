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

public class Command
      implements Cloneable {

   protected static Logger logger = Logger.getLogger("com.bbn.openmap.dataAccess.cgm.Command");

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

   public int read16(DataInputStream in)
         throws IOException {
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

   public static Command read(DataInputStream in)
         throws IOException {

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
               case 7:
               case 8:
               case 9:
               case 13:
               case 14:
               case 15:
               case 16:
               case 17:
               case 18:
               case 19:
               case 20:
               case 21:
               case 22:
               case 23:
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
               case 4:
               case 5:
               case 6:
               case 7:
               case 8:
               case 9:
               case 10:
                  break;
               case 11:
                  return new MetafileElementList(ec, eid, l, in);
               case 13:
                  return new FontList(ec, eid, l, in);
               case 19:
                  return new ColorModel(ec, eid, l, in);
               case 20:
               case 21:
               case 22:
               case 23:
               case 24:
               default:
                  break;
            }
            break;
         // Picture Descriptor Elements
         case 2:
            switch (eid) {
               case 1:
                  break;
               case 2:
                  return new ColorSelectionMode(ec, eid, l, in);
               case 3:
                  return new LineWidthMode(ec, eid, l, in);
               case 4:
                  break;
               case 5:
                  return new EdgeWidthMode(ec, eid, l, in);
               case 6:
                  return new VDCExtent(ec, eid, l, in);
               case 7:
               case 8:
               case 9:
               case 10:
               case 11:
               case 12:
               case 13:
               case 14:
               case 15:
               case 16:
               case 17:
               case 18:
               case 19:
               case 20:
               default:
                  break;
            }
            break;
         // Control Elements
         case 3:
            break;
         // Graphical Primitive Elements
         case 4:
            switch (eid) {
               case 1:
                  return new PolylineElement(ec, eid, l, in);
               case 2:
               case 3:
                  break;
               case 4:
                  return new TextElement(ec, eid, l, in);
               case 5:
               case 6:
                  break;
               case 7:
                  return new PolygonElement(ec, eid, l, in);
               case 8:
               case 9:
               case 10:
                  break;
               case 11:
                  return new RectangleElement(ec, eid, l, in);
               case 12:
                  return new CircleElement(ec, eid, l, in);
               case 13:
               case 14:
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
               case 21:
               case 22:
               case 23:
               case 24:
               case 25:
               case 26:
               case 27:
               case 28:
               case 29:
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
      } catch (Exception e) {
         return null;
      }
   }

}