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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/vpf/DcwThematicIndex.java,v $
// $RCSfile: DcwThematicIndex.java,v $
// $Revision: 1.5 $
// $Date: 2005/08/09 19:29:39 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.vpf;

import java.io.EOFException;
import java.io.IOException;
import java.util.Arrays;
import java.util.BitSet;

import com.bbn.openmap.io.BinaryBufferedFile;
import com.bbn.openmap.io.BinaryFile;
import com.bbn.openmap.io.FormatException;
import com.bbn.openmap.util.Debug;

/** Read a VPF thematic index file. (VPF *.?ti files) */
public class DcwThematicIndex {
   /** the file we read from */
   private BinaryFile inputFile = null;
   /** read from file - length of header */
   final private int headerSize;
   /** read from file - number of indexes (codes) */
   final private int numberOfCodes;
   /** read from file - total number of rows indexed */
   final private int numberOfRows;
   /** read from file - type of index */
   final private char typeOfIndex; // T hematic
   /** read from file - field type of index */
   final private char fieldTypeOfIndex;
   /** read from file - number of elements composing the index value */
   final private int numberOfDataElement;
   /** read from file - the type of the index */
   final private char dataTypeSpecifier;
   /** read from file - the table indexed */
   final private String tableIndexed;
   /** read from file - the column indexed */
   final private String columnIndexed;
   /** read from file (vpf 2407 only) - are the codes sorted */
   final private boolean sorted;
   /** the list of index records */
   private IndexRecord[] indexData;
   /** the name of the file being read */
   // final protected File filename;
   final protected String filename;
   /** the byte order of the file */
   protected boolean byteOrder;

   /**
    * A utility class used to record index records.
    */
   public static class IndexRecord
         implements Comparable<Object> {
      /** the index (code) */
      final Object index;
      /** the offset of the data */
      final int offset;
      /** the number of values - 0 means the offset is the only value */
      final int numvals;

      /**
       * Construct an index record
       * 
       * @param index the index object
       * @param offset the offset of the data
       * @param numvals the number of values
       */
      public IndexRecord(Object index, int offset, int numvals) {
         this.index = index;
         this.offset = offset;
         this.numvals = numvals;
      }

      @SuppressWarnings("unchecked")
      public int compareTo(Object obj) {
         Object realobj = (obj instanceof IndexRecord) ? ((IndexRecord) obj).index : obj;
         return ((Comparable<Object>) index).compareTo(realobj);
      }
   }

   /**
    * Construct an index, assumes this is pre-VPF2407 format.
    * 
    * @param filename the file to oped
    * @param border the byteorder
    */
   public DcwThematicIndex(String filename, boolean border)
         throws FormatException {
      this(filename, border, false);
   }

   /**
    * Construct an index, assumes this is pre-VPF2407 format.
    * 
    * @param filename the file to oped
    * @param border the byteorder
    * @param vpf2407 true for MILSTD-2407 format thematic index. false will
    *        properly read a VPF2407 format index, but will ignore one header
    *        field (sorted). true will improperly read old-style data.
    */
   public DcwThematicIndex(String filename, boolean border, boolean vpf2407)
         throws FormatException {

      this.filename = filename;
      byteOrder = border;

      reopen(0);

      if (Debug.debugging("vpfserver")) {
         System.out.println("DTI: opened the file " + filename);
      }

      try {
         headerSize = inputFile.readInteger();
         numberOfCodes = inputFile.readInteger();
         numberOfRows = inputFile.readInteger();
         typeOfIndex = inputFile.readChar();
         fieldTypeOfIndex = inputFile.readChar();
         numberOfDataElement = inputFile.readInteger();
         dataTypeSpecifier = inputFile.readChar();
         tableIndexed = trim(inputFile.readFixedLengthString(12)).toLowerCase();
         columnIndexed = trim(inputFile.readFixedLengthString(25)).toLowerCase();
         sorted = (inputFile.readChar() == 'S') && vpf2407;
         inputFile.seek(60); // skips 3 unused bytes

         indexData = new IndexRecord[numberOfCodes];

         if (Debug.debugging("vpfserver")) {
            System.out.println("HeaderSize = " + headerSize);
            System.out.println("Number of Codes = " + numberOfCodes);
            System.out.println("Number of Rows = " + numberOfRows);
            System.out.println("Type of Index = " + typeOfIndex);
            // if (typeOfIndex != 'T')
            // System.out.println(" *** Strange - dcw spec says it
            // will be T ***");
            System.out.println("Field Type of Index = " + fieldTypeOfIndex);
            System.out.println("Number of Data Element = " + numberOfDataElement);
            System.out.println("Data Type Specifier = " + dataTypeSpecifier);
            System.out.println("Table Indexed  = " + tableIndexed);
            System.out.println("Column Indexed = " + columnIndexed);
            System.out.println("Sorted = " + sorted);
         }

         StringBuffer pr = new StringBuffer();
         for (int i = 0; i < numberOfCodes; i++) {
            indexData[i] =
                  new IndexRecord(readIndexField(fieldTypeOfIndex, numberOfDataElement), inputFile.readInteger(),
                                  inputFile.readInteger());

            if (Debug.debugging("vpfserver")) {
               pr = new StringBuffer("i = ").append(i);
               pr.append("; val = ").append(indexData[i].index.toString());
               pr.append("; offset = ").append(indexData[i].offset);
               pr.append("; number of elts = ").append(indexData[i].numvals);
               if (i < 40) {
                  System.out.println(pr.toString());
               }
            }
         }

         if (!sorted) {
            Arrays.sort(indexData);
         }

         if (Debug.debugging("vpfserver") && (numberOfCodes > 40)) {
            System.out.println(pr.toString());
         }

         Debug.message("vpfserver", "*** Finished Header Read ***");

         if (Debug.debugging("vpfserver")) {
            if ((typeOfIndex == 'T') || (typeOfIndex == 'I')) {
               Debug.output("Normal Inverted Index Format");
            } else if ((typeOfIndex == 'B') || (typeOfIndex == 'G')) {
               Debug.output("Scary Bitmap Index Format");
            } else {
               throw new FormatException("Unidentified TMI format");
            }

            Object[] indexes = getValueIndexes();
            // We just know that these values are tile IDs.
            for (int j = 0; j < indexes.length; j++) {
               // int[] row = get(indexes[j]);
               // If you want to do some scary printout, code it
               // up here.
            }
         }
         close();
      } catch (EOFException e) {
         throw new FormatException("Hit Premature EOF in thematic index");
      } catch (IOException i) {
         throw new FormatException("Encountered IO Exception: " + i.getMessage());
      }
   }

   /**
    * Returns the set of values indexed by this thematic index.
    * 
    * @return the set of values indexed
    */
   public Object[] getValueIndexes() {
      Object[] values = null;

      if (indexData != null) {
         values = new Object[indexData.length];

         for (int i = 0; i < indexData.length; i++) {
            values[i] = indexData[i].index;
         }
      }
      return values;
   }

   /**
    * Returns the list of rows listed for this index
    * 
    * @return an array of rows
    * @param valueIndex the value to look up
    */
   public synchronized int[] get(Object valueIndex)
         throws FormatException {
      int[] values = null;

      try {
         int index = Arrays.binarySearch(indexData, valueIndex);
         if (index >= 0) {
            IndexRecord ir = indexData[index];
            int offset = ir.offset;
            int numvals = ir.numvals;

            if ((typeOfIndex == 'T') || (typeOfIndex == 'I')) {
               if (numvals == 0) {
                  values = new int[1];
                  values[0] = offset;
               } else {
                  values = new int[numvals];
                  reopen(offset);

                  for (int j = 0; j < numvals; j++) {
                     values[j] = readIndexWithFieldType(dataTypeSpecifier);
                  }
               }

               return values;

            } else if ((typeOfIndex == 'B') || (typeOfIndex == 'G')) {
               // Don't really do anything with this type of
               // index...

               int shortread = numberOfRows / 16;
               if ((numberOfRows % 16) != 0) {
                  shortread++;
               }
               if (Debug.debugging("vpfserver")) {
                  System.out.println("Reading a bunch of shorts: " + shortread);
                  System.out.println("Starting at offset: " + inputFile.getFilePointer());
               }

               BitSet bits = new BitSet(numberOfRows);
               int cnt = 0;
               for (int shortcnt = 0; shortcnt < shortread; shortcnt++) {
                  short s = inputFile.readShort();

                  for (int k = 0; k < 16; k++) {
                     cnt++;
                     if ((s & 0x1) == 1) {
                        bits.set(cnt);
                     }
                     s >>= 1;
                  }
               }
               StringBuffer prt = new StringBuffer();
               for (int j = 1; j <= bits.size(); j++) {
                  if (bits.get(j)) {
                     prt.append(", ").append(j);
                  }
               }
               System.out.println(prt);

            } else {
               throw new FormatException("Unidentified TMI format");
            }
         }
      } catch (EOFException e) {
         throw new FormatException("Hit Premature EOF in thematic index");
      } catch (IOException i) {
         throw new FormatException("Encountered IO Exception: " + i.getMessage());
      }

      return values;
   }

   /**
    * Utility method to read rows.
    * 
    * @param ft the field type
    * @returns the value read from the file
    */
   private int readIndexWithFieldType(char ft)
         throws EOFException, FormatException {
      switch (ft) {
         case 'S':
            return (int) inputFile.readShort();
         case 'I':
            return inputFile.readInteger();
      }
      throw new FormatException("Unrecognized FieldTypeOfIndex");
   }

   private Object readIndexField(char dts, int textlen)
         throws EOFException, FormatException {
      switch (dts) {
         case 'I':
            return new Integer(inputFile.readInteger());
         case 'T':
            return inputFile.readFixedLengthString(textlen);
         case 'S':
            return new Short(inputFile.readShort());
         case 'F':
            return new Float(inputFile.readFloat());
         case 'R':
            return new Double(inputFile.readDouble());
      }
      throw new FormatException("Unrecognized field index type");
   }

   private String trim(String s) {
      StringBuffer ns = new StringBuffer();
      char foo[] = s.toCharArray();
      for (int i = 0; i < foo.length; i++) {
         if ((foo[i] == ' ') || (foo[i] == 0)) {
            break;
         }
         ns.append(foo[i]);
      }
      return ns.toString();
   }

   /**
    * Returns the number of distinct indexed values
    * 
    * @return the number of distinct indexed values
    */
   public int getNumberOfCodes() {
      return numberOfCodes;
   }

   /**
    * Returns the number of rows indexed
    * 
    * @return the number of rows indexed
    */
   public int getNumberOfRows() {
      return numberOfRows;
   }

   /**
    * Returns the type of index (refer to VPF spec for valid values)
    * 
    * @return the type of index (refer to VPF spec for valid values)
    */
   public char getTypeOfIndex() {
      return typeOfIndex;
   }

   /**
    * Returns the type of the field being indexed
    * 
    * @return the type of the field being indexed
    */
   public char getFieldTypeOfIndex() {
      return fieldTypeOfIndex;
   }

   /**
    * Returns the number of elements in the index field
    * 
    * @return the number of elements in the index field
    */
   public int getNumberOfDataElements() {
      return numberOfDataElement;
   }

   /**
    * Returns the datatype specifier
    * 
    * @return the datatype specifier
    */
   public char getDataTypeSpecifier() {
      return dataTypeSpecifier;
   }

   /**
    * Returns the name of the table being indexed
    * 
    * @return the name of the table being indexed
    */
   public String getTableIndexed() {
      return tableIndexed;
   }

   /**
    * Returns the name of the column being indexed
    * 
    * @return the name of the column being indexed
    */
   public String getColumnIndexed() {
      return columnIndexed;
   }

   public boolean getSorted() {
      return sorted;
   }

   /** Closes the associated input file. (may later get reopened) */
   public synchronized void close()
         throws FormatException {
      try {
         if (inputFile != null) {
            inputFile.close();
         }
         inputFile = null;
      } catch (IOException i) {
         throw new FormatException("DcwThematicIndex: Can't close file " + filename + ": " + i.getMessage());
      }
   }

   /**
    * Reopen the associated input file.
    * 
    * @param offset the byte offset to seek to upon reopening the file. If
    *        offset is invalid (less than 1), then the input stream is in an
    *        undefined location.
    * @exception FormatException some error was encountered in reopening file or
    *            seeking to the desired row.
    * @see #close()
    */
   public synchronized void reopen(int offset)
         throws FormatException {
      try {
         if (inputFile == null) {
            inputFile = new BinaryBufferedFile(filename);
            inputFile.byteOrder(byteOrder);
         }
         if (offset > 0) {
            inputFile.seek(offset);
         }
      } catch (IOException i) {
         throw new FormatException("DcwThematicIndex: Can't open file " + filename + ": " + i.getMessage());
      }
   }

}