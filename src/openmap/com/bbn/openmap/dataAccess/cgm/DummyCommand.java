/* 
 */
package com.bbn.openmap.dataAccess.cgm;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * Filler object for Commands not yet implemented, in order to be able to see
 * what's going on.
 * 
 * @author dietrick
 */
public class DummyCommand
      extends Command {

   String commandDescription;

   public DummyCommand(int ec, int eid, int l, DataInputStream in, String desc)
         throws IOException {
      super(ec, eid, l, in);
      commandDescription = desc;
   }

   public String toString() {
      return "Unimplemented (" + ElementClass + "/" + ElementId + "): " + commandDescription;
   }
}
