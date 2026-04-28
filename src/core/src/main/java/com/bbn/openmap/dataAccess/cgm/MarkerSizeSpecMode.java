/* 
 * <copyright>
 *  Copyright 2010 BBN Technologies
 * </copyright>
 */
package com.bbn.openmap.dataAccess.cgm;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * Filler object for Commands not yet implemented, in order to be able to kinda
 * see what's going on.
 * 
 * @author dietrick
 */
public class MarkerSizeSpecMode
      extends Command {

   int mode;

   public MarkerSizeSpecMode(int ec, int eid, int l, DataInputStream in)
         throws IOException {
      super(ec, eid, l, in);
      mode = makeInt(0);
   }

   public String toString() {
      return "MarkerSizeSpecMode (" + mode + ")";
   }
}
