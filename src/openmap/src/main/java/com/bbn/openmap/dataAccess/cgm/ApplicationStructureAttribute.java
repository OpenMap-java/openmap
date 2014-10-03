/* 
 * <copyright>
 *  Copyright 2010 BBN Technologies
 * </copyright>
 */
package com.bbn.openmap.dataAccess.cgm;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * Application Structure Attribute
 * 
 * @author dietrick
 */
public class ApplicationStructureAttribute
      extends Command {

   String S;

   public ApplicationStructureAttribute(int ec, int eid, int l, DataInputStream in)
         throws IOException {
      super(ec, eid, l, in);
      S = makeString(0);
   }

   public String toString() {
      return "Application Structure Attribute " + S;
   }
}
