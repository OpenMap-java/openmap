/* ****************************************************************************
 * $Id: View8211.java,v 1.5 2005/08/04 18:12:00 dietrick Exp $
 *
 * Project:  SDTS Translator
 * Purpose:  Example program dumping data in 8211 data to stdout.
 * Author:   Frank Warmerdam, warmerda@home.com
 *
 ******************************************************************************
 * Copyright (c) 1999, Frank Warmerdam
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 ******************************************************************************
 */

package com.bbn.openmap.dataAccess.iso8211;

import java.io.IOException;
import java.util.Iterator;

import com.bbn.openmap.layer.vpf.MutableInt;
import com.bbn.openmap.util.Debug;

/**
 * Class that uses the DDF* classes to read an 8211 file and print out the
 * contents.
 */
public class View8211 {

   protected boolean bFSPTHack = false;
   protected String pszFilename = null;

   public View8211(String filename, boolean fspt_repeating) {
      pszFilename = filename;
      bFSPTHack = fspt_repeating;

      view();
   }

   protected void view() {
      DDFModule oModule;

      try {

         oModule = new DDFModule(pszFilename);

         if (bFSPTHack) {
            DDFFieldDefinition poFSPT = oModule.findFieldDefn("FSPT");

            if (poFSPT == null)
               Debug.error("View8211: unable to find FSPT field to set repeating flag.");
            else
               poFSPT.setRepeating(true);
         }

         /* -------------------------------------------------------------------- */
         /* Loop reading records till there are none left. */
         /* -------------------------------------------------------------------- */
         DDFRecord poRecord;
         int iRecord = 1;

         while ((poRecord = oModule.readRecord()) != null) {
            Debug.output("Record " + (iRecord++) + "(" + poRecord.getDataSize() + " bytes)");

            /* ------------------------------------------------------------ */
            /* Loop over each field in this particular record. */
            /* ------------------------------------------------------------ */
            for (Iterator it = poRecord.iterator(); it != null && it.hasNext();) {
               // Debug.output(((DDFField)it.next()).toString()));
               viewRecordField(((DDFField) it.next()));
            }
         }

      } catch (IOException ioe) {
         Debug.error(ioe.getMessage());
         ioe.printStackTrace();
      }
   }

   /**
    * Dump the contents of a field instance in a record.
    */
   protected void viewRecordField(DDFField poField) {
      DDFFieldDefinition poFieldDefn = poField.getFieldDefn();

      // Report general information about the field.
      Debug.output("    Field " + poFieldDefn.getName() + ": " + poFieldDefn.getDescription());

      // Get pointer to this fields raw data. We will move through
      // it consuming data as we report subfield values.

      byte[] pachFieldData = poField.getData();
      int nBytesRemaining = poField.getDataSize();

      /* -------------------------------------------------------- */
      /* Loop over the repeat count for this fields */
      /* subfields. The repeat count will almost */
      /* always be one. */
      /* -------------------------------------------------------- */
      for (int iRepeat = 0; iRepeat < poField.getRepeatCount(); iRepeat++) {
         if (iRepeat > 0) {
            Debug.output("Repeating (" + iRepeat + ")...");
         }
         /* -------------------------------------------------------- */
         /* Loop over all the subfields of this field, advancing */
         /* the data pointer as we consume data. */
         /* -------------------------------------------------------- */
         for (int iSF = 0; iSF < poFieldDefn.getSubfieldCount(); iSF++) {

            DDFSubfieldDefinition poSFDefn = poFieldDefn.getSubfieldDefn(iSF);
            int nBytesConsumed = viewSubfield(poSFDefn, pachFieldData, nBytesRemaining);
            nBytesRemaining -= nBytesConsumed;
            byte[] tempData = new byte[pachFieldData.length - nBytesConsumed];
            System.arraycopy(pachFieldData, nBytesConsumed, tempData, 0, tempData.length);
            pachFieldData = tempData;
         }
      }
   }

   protected int viewSubfield(DDFSubfieldDefinition poSFDefn, byte[] pachFieldData, int nBytesRemaining) {

      MutableInt nBytesConsumed = new MutableInt();

      DDFDataType ddfdt = poSFDefn.getType();

      if (ddfdt == DDFDataType.DDFInt) {
         Debug.output("        " + poSFDefn.getName() + " = "
               + poSFDefn.extractIntData(pachFieldData, nBytesRemaining, nBytesConsumed));
      } else if (ddfdt == DDFDataType.DDFFloat) {
         Debug.output("        " + poSFDefn.getName() + " = "
               + poSFDefn.extractFloatData(pachFieldData, nBytesRemaining, nBytesConsumed));
      } else if (ddfdt == DDFDataType.DDFString) {
         Debug.output("        " + poSFDefn.getName() + " = "
               + poSFDefn.extractStringData(pachFieldData, nBytesRemaining, nBytesConsumed));
      } else if (ddfdt == DDFDataType.DDFBinaryString) {
         poSFDefn.extractStringData(pachFieldData, nBytesRemaining, nBytesConsumed); // pabyBString

         Debug.output("        " + poSFDefn.getName());
      }

      return nBytesConsumed.value;
   }

   public static void main(String[] argv) {

      Debug.init();

      String pszFilename = null;
      boolean bFSPTHack = false;

      for (int iArg = 0; iArg < argv.length; iArg++) {
         if (argv[iArg].equals("-fspt_repeating")) {
            bFSPTHack = true;
         } else {
            pszFilename = argv[iArg];
         }
      }

      if (pszFilename == null) {
         Debug.output("Usage: View8211 filename\n");
         System.exit(1);
      }

      new View8211(pszFilename, bFSPTHack);

   }

}
