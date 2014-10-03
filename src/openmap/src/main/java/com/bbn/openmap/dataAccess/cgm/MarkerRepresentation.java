/* 
 * <copyright>
 *  Copyright 2010 BBN Technologies
 * </copyright>
 */
package com.bbn.openmap.dataAccess.cgm;

import java.awt.Color;
import java.io.DataInputStream;
import java.io.IOException;

/**
 * Filler object for Commands not yet implemented, in order to be able to kinda
 * see what's going on.
 * 
 * @author dietrick
 */
public class MarkerRepresentation
      extends Command {

   int bundleIndex;
   int type;
   int size;
   Color color;

   public MarkerRepresentation(int ec, int eid, int l, DataInputStream in)
         throws IOException {
      super(ec, eid, l, in);
      bundleIndex = makeInt(0);
      type = makeInt(1);
      size = makeInt(2);
      
   }

   public String toString() {
      return "MarkerRepresentation (" + bundleIndex + "," + type + "," + size + ")";
   }
}
