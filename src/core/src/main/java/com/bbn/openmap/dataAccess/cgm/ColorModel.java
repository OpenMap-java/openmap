/* 
 * <copyright>
 *  Copyright 2010 BBN Technologies
 * </copyright>
 */
package com.bbn.openmap.dataAccess.cgm;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * FIXME: Enter the description of this type here
 * 
 * @author dietrick
 */
public class ColorModel
      extends Command {

   protected int model;

   public final static int RGB = 1;
   public final static int CIELAB = 2;
   public final static int CIELUV = 3;
   public final static int CMYK = 4;
   public final static int RGB_related = 5;
   
   /**
    * @param ec
    * @param eid
    * @param l
    * @param in
    * @throws IOException
    */
   public ColorModel(int ec, int eid, int l, DataInputStream in)
         throws IOException {

      super(ec, eid, l, in);

      model = args[0];
   }

   public int getModel() {
      return model;
   }
   
   public String toString() {
      return "ColorModel is " + model;
   }
   
}
