// **********************************************************************
// 
// <copyright>
// 
//  BBN Technologies
//  10 Moulton Street
//  Cambridge, MA 02138
//  (617) 873-8000
// 
//  Copyright (C) BBNT Solutions LLC. All rights reserved.
// 
// </copyright>
// **********************************************************************
// 
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/dataAccess/shape/input/ShxInputStream.java,v $
// $RCSfile: ShxInputStream.java,v $
// $Revision: 1.7 $
// $Date: 2006/08/25 15:36:15 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.dataAccess.shape.input;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Reads index data from a .shx file
 * 
 * @author Doug Van Auken
 */
public class ShxInputStream {

   /**
    * An input stream to process primitives in Little Endian or Big Endian
    */
   private LittleEndianInputStream _leis = null;

   /**
    * Chains an input stream with a Little EndianInputStream
    */
   public ShxInputStream(InputStream is) {
      BufferedInputStream bis = new BufferedInputStream(is);
      _leis = new LittleEndianInputStream(bis);
   }

   /**
    * Processes the SHX file to obtain a list of offsets, which classes derived
    * from AbstractSupport will use to iterate through the associated SHP file
    * 
    * @return an array of offsets, which will be passed into the open method of
    *         classes which extend AbstractSupport.
    */
   public int[][] getIndex() {
      int[][] indexData = null;
      try {
         /* int fileCode = */_leis.readInt();
         _leis.skipBytes(20);
         int fileLength = _leis.readInt();
         int numShapes = (fileLength - 50) / 4;
         indexData = new int[2][numShapes];
         _leis.skipBytes(4);
         /* _type = */_leis.readLEInt();
         _leis.skip(64);
         for (int i = 0; i <= numShapes - 1; i++) {
            indexData[0][i] = _leis.readInt();
            indexData[1][i] = _leis.readInt();
         }
      } catch (IOException e) {
         e.printStackTrace();
         return null;
      }
      return indexData;
   }

   public void close()
         throws IOException {
      _leis.close();
   }
}
