package org.libtiff.jai.codec;

/*
 * XTIFF: eXtensible TIFF libraries for JAI.
 * 
 * The contents of this file are subject to the  JAVA ADVANCED IMAGING
 * SAMPLE INPUT-OUTPUT CODECS AND WIDGET HANDLING SOURCE CODE  License
 * Version 1.0 (the "License"); You may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.sun.com/software/imaging/JAI/index.html
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License. 
 *
 * The Original Code is JAVA ADVANCED IMAGING SAMPLE INPUT-OUTPUT CODECS
 * AND WIDGET HANDLING SOURCE CODE. 
 * The Initial Developer of the Original Code is: Sun Microsystems, Inc..
 * Portions created by: Niles Ritter 
 * are Copyright (C): Niles Ritter, GeoTIFF.org, 1999,2000.
 * All Rights Reserved.
 * Contributor(s): Niles Ritter
 */

import java.io.IOException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.Vector;

import org.libtiff.jai.util.JaiI18N;

import com.sun.media.jai.codec.SeekableStream;

/**
 * XTIFFDirectory is an extensible TIFF directory object. This class may be
 * extended without changing the XTIFF codec by overriding the XTIFFFactory
 * instance registered in this class. In addition, this class is the repository
 * of all XTIFFTileCodec's which may be augmented with new codecs, again without
 * overriding the ImageCodec. If the jai "tiff" codec has been overridden
 * through the <code>XTIFFDescriptor.register()</code> method, each XTIFF image
 * will possess a property called "tiff.directory" which will be an object of
 * the type created by the factory. The class is declared as serializable to
 * permit its transmission to remote images as a set of parameters to the codec,
 * and to be able to survive as an instantiated property of the RenderedImage.
 * 
 * 
 * @serializable
 * @author Niles Ritter
 * @see XTIFFDescriptor
 * @see XTIFFField
 * @see XTIFFTileCodec
 * @see XTIFFFactory
 */
public class XTIFFDirectory
      extends Object
      implements java.io.Serializable {
   private int imageType;

   /** default directory factory */
   protected static XTIFFFactory factory = new XTIFFFactory();

   protected static Hashtable tileCodecs = new Hashtable();

   /** The stream being read. Not persisted */
   transient protected SeekableStream stream;

   /** A boolean storing the endianness of the stream. */
   boolean isBigEndian;

   /** A boolean indicating tiled tagset */
   boolean _isTiled = false;

   /** for dynamically adding fields in sorted order */
   TreeMap fieldIndex = new TreeMap();

   /** The default constructor. Publicized for Serializability */
   public XTIFFDirectory() {
   }

   private static boolean isValidEndianTag(int endian) {
      return ((endian == 0x4949) || (endian == 0x4d4d));
   }

   /**
    * If true this image uses TIFF 6.0 tiling
    */
   public boolean isTiled() {
      return _isTiled;
   }

   /**
    * reads the TIFF header. Not likely to be overridden.
    */
   protected void readHeader()
         throws IOException {

      // Read the TIFF header
      stream.seek(0L);
      int endian = stream.readUnsignedShort();
      if (!isValidEndianTag(endian)) {
         throw new IllegalArgumentException(JaiI18N.getString("XTIFFDirectory1"));
      }
      isBigEndian = (endian == 0x4d4d);

      // Verify that Douglas Addams still has influence in software:
      int magic = readUnsignedShort(stream);
      if (magic != 42) {
         throw new IllegalArgumentException(JaiI18N.getString("XTIFFDirectory2"));
      }

   }

   /**
    * Constructs a XTIFFDirectory from a SeekableStream. The directory parameter
    * specifies which directory to read from the linked list present in the
    * stream; directory 0 is normally read but it is possible to store multiple
    * images in a single TIFF file by maintaing multiple directories.
    * 
    * @param stream a SeekableStream to read from.
    * @param directory the index of the directory to read.
    */
   protected XTIFFDirectory(SeekableStream stream, int directory)
         throws IOException {

      this.stream = stream;
      long global_save_offset = stream.getFilePointer();
      long ifd_offset;

      readHeader();

      // Get the initial ifd offset as an unsigned int (using a long)
      ifd_offset = readUnsignedInt(stream);

      for (int i = 0; i < directory; i++) {
         if (ifd_offset == 0L) {
            throw new IllegalArgumentException(JaiI18N.getString("XTIFFDirectory3"));
         }

         stream.seek(ifd_offset);
         int entries = readUnsignedShort(stream);
         stream.skip(12 * entries);

         ifd_offset = readUnsignedInt(stream);
      }

      stream.seek(ifd_offset);
      initialize();
      stream.seek(global_save_offset);
   }

   /**
    * Constructs a XTIFFDirectory by reading a SeekableStream. The ifd_offset
    * parameter specifies the stream offset from which to begin reading; this
    * mechanism is sometimes used to store private IFDs within a TIFF file that
    * are not part of the normal sequence of IFDs.
    * 
    * @param stream a SeekableStream to read from.
    * @param ifd_offset the long byte offset of the directory.
    */
   protected XTIFFDirectory(SeekableStream stream, long ifd_offset)
         throws IOException {
      this.stream = stream;
      long global_save_offset = stream.getFilePointer();

      readHeader();

      stream.seek(ifd_offset);
      initialize();
      stream.seek(global_save_offset);
   }

   private static final int[] _sizeOfType = {
      0, // 0 = n/a
      1, // 1 = byte
      1, // 2 = ascii
      2, // 3 = short
      4, // 4 = long
      8, // 5 = rational
      1, // 6 = sbyte
      1, // 7 = undefined
      2, // 8 = sshort
      4, // 9 = slong
      8, // 10 = srational
      4, // 11 = float
      8
   // 12 = double
         };

   /**
    * Return the size of a data type. Extend if you need to define new TIFF
    * field types. Also override the createField() method of the XTIFFFactory,
    * the XTIFFField class, and the readFieldValue() method here.
    * 
    * @param type the XTIFFField type code
    * @see XTIFFField
    * @see XTIFFFactory
    */
   public int sizeOfType(int type)
         throws ArrayIndexOutOfBoundsException {
      return _sizeOfType[type];
   }

   /**
    * Create and add a TIFF field to this directory.
    * 
    * @param tag the TIFF tag listed in XTIFF
    * @param type the TIFF field type listed in XTIFFField
    * @param count the number of values in array obj
    * @param obj the array of values
    * @see XTIFFField
    * @see XTIFF
    */
   public void addField(int tag, int type, int count, Object obj) {
      addField(factory.createField(tag, type, count, obj));
   }

   /**
    * Create a TIFF field
    * 
    * @param tag the TIFF tag listed in XTIFF
    * @param type the TIFF field type listed in XTIFFField
    * @param count the number of values in array obj
    * @param obj the array of values
    * @see XTIFFField
    * @see XTIFF
    */
   public static XTIFFField createField(int tag, int type, int count, Object obj) {
      return factory.createField(tag, type, count, obj);
   }

   /**
    * Add an existing TIFF field to this directory.
    * 
    * @param type the XTIFFField type code
    * @see XTIFFField
    */
   public void addField(XTIFFField field) {
      fieldIndex.put(new Integer(field.tag), field);
   }

   /**
    * Initialize the directory from a stream
    */
   protected void initialize()
         throws IOException {
      XTIFFField field;
      // long nextTagOffset;

      int numEntries = readUnsignedShort(stream);

      for (int i = 0; i < numEntries; i++) {

         try {
            field = readField();
         } catch (ArrayIndexOutOfBoundsException ae) {
            // if the data type is unknown we should skip this TIFF Field
            continue;
         }
         addField(field);
      }
   }

   /** Returns the number of directory entries. */
   public int getNumEntries() {
      return fieldIndex.size();
   }

   /**
    * Returns the value of a given tag as a XTIFFField, or null if the tag is
    * not present.
    */
   public XTIFFField getField(int tag) {
      return (XTIFFField) fieldIndex.get(new Integer(tag));
   }

   /**
    * Returns true if a tag appears in the directory.
    */
   public boolean isTagPresent(int tag) {
      return fieldIndex.containsKey(new Integer(tag));
   }

   /**
    * Returns an ordered array of ints indicating the tag values.
    */
   public int[] getTags() {
      int[] tags = new int[fieldIndex.size()];
      Iterator it = fieldIndex.keySet().iterator();
      int i = 0;
      while (it.hasNext()) {
         tags[i++] = ((Integer) it.next()).intValue();
      }
      return tags;
   }

   /**
    * Returns an array of XTIFFFields containing all the fields in this
    * directory.
    */
   public XTIFFField[] getFields() {
      XTIFFField[] fields = new XTIFFField[fieldIndex.size()];
      Iterator it = fieldIndex.values().iterator();
      int i = 0;
      while (it.hasNext()) {
         fields[i++] = (XTIFFField) it.next();
      }
      return fields;
   }

   /**
    * Returns the value of a particular index of a given tag as a byte. The
    * caller is responsible for ensuring that the tag is present and has type
    * XTIFFField.TIFF_SBYTE, TIFF_BYTE, or TIFF_UNDEFINED.
    */
   public byte getFieldAsByte(int tag, int index) {
      return (getField(tag).getAsBytes())[index];
   }

   /**
    * Returns the value of index 0 of a given tag as a byte. The caller is
    * responsible for ensuring that the tag is present and has type
    * XTIFFField.TIFF_SBYTE, TIFF_BYTE, or TIFF_UNDEFINED.
    */
   public byte getFieldAsByte(int tag) {
      return getFieldAsByte(tag, 0);
   }

   /**
    * Returns the value of a particular index of a given tag as a long. The
    * caller is responsible for ensuring that the tag is present and has type
    * TIFF_BYTE, TIFF_SBYTE, TIFF_UNDEFINED, TIFF_SHORT, TIFF_SSHORT, TIFF_SLONG
    * or TIFF_LONG.
    */
   public long getFieldAsLong(int tag, int index) {
      return getField(tag).getAsLong(index);
   }

   /**
    * Returns the value of index 0 of a given tag as a long. The caller is
    * responsible for ensuring that the tag is present and has type TIFF_BYTE,
    * TIFF_SBYTE, TIFF_UNDEFINED, TIFF_SHORT, TIFF_SSHORT, TIFF_SLONG or
    * TIFF_LONG.
    */
   public long getFieldAsLong(int tag) {
      return getFieldAsLong(tag, 0);
   }

   /**
    * Returns the value of a particular index of a given tag as a float. The
    * caller is responsible for ensuring that the tag is present and has numeric
    * type (all but TIFF_UNDEFINED and TIFF_ASCII).
    */
   public float getFieldAsFloat(int tag, int index) {
      return getField(tag).getAsFloat(index);
   }

   /**
    * Returns the value of index 0 of a given tag as a float. The caller is
    * responsible for ensuring that the tag is present and has numeric type (all
    * but TIFF_UNDEFINED and TIFF_ASCII).
    */
   public float getFieldAsFloat(int tag) {
      return getFieldAsFloat(tag, 0);
   }

   /**
    * Returns the value of a particular index of a given tag as a double. The
    * caller is responsible for ensuring that the tag is present and has numeric
    * type (all but TIFF_UNDEFINED and TIFF_ASCII).
    */
   public double getFieldAsDouble(int tag, int index) {
      return getField(tag).getAsDouble(index);
   }

   /**
    * Returns the value of index 0 of a given tag as a double. The caller is
    * responsible for ensuring that the tag is present and has numeric type (all
    * but TIFF_UNDEFINED and TIFF_ASCII).
    */
   public double getFieldAsDouble(int tag) {
      return getFieldAsDouble(tag, 0);
   }

   /**
    * TIFF field-value reader. Override if there are new field types. Also
    * override sizeOfType() and, possibly the createField method of the factory,
    * if the field needs new accessors.
    */
   public Object readFieldValue(int tag, int type, int count)
         throws IOException, ArrayIndexOutOfBoundsException {
      int j;
      Object obj = null;

      switch (type) {
         case XTIFFField.TIFF_BYTE:
         case XTIFFField.TIFF_SBYTE:
         case XTIFFField.TIFF_UNDEFINED:
         case XTIFFField.TIFF_ASCII:
            byte[] bvalues = new byte[count];
            stream.readFully(bvalues, 0, count);

            if (type == XTIFFField.TIFF_ASCII) {

               // Can be multiple strings
               int index = 0, prevIndex = 0;
               Vector v = new Vector();

               while (index < count) {
                  while ((index < count) && (bvalues[index++] != 0)) {
                      ;
                  }
                  // When we encountered zero, means one string has ended
                  v.add(new String(bvalues, prevIndex, (index - prevIndex)));
                  prevIndex = index;
               }

               count = v.size();
               String strings[] = new String[count];
               for (int c = 0; c < count; c++) {
                  strings[c] = (String) v.elementAt(c);
               }

               obj = strings;
            } else {
               obj = bvalues;
            }

            break;

         case XTIFFField.TIFF_SHORT:
            char[] cvalues = new char[count];
            for (j = 0; j < count; j++) {
               cvalues[j] = (char) (readUnsignedShort(stream));
            }
            obj = cvalues;
            break;

         case XTIFFField.TIFF_LONG:
            long[] lvalues = new long[count];
            for (j = 0; j < count; j++) {
               lvalues[j] = readUnsignedInt(stream);
            }
            obj = lvalues;
            break;

         case XTIFFField.TIFF_RATIONAL:
            long[][] llvalues = new long[count][2];
            for (j = 0; j < count; j++) {
               llvalues[j][0] = readUnsignedInt(stream);
               llvalues[j][1] = readUnsignedInt(stream);
            }
            obj = llvalues;
            break;

         case XTIFFField.TIFF_SSHORT:
            short[] svalues = new short[count];
            for (j = 0; j < count; j++) {
               svalues[j] = readShort(stream);
            }
            obj = svalues;
            break;

         case XTIFFField.TIFF_SLONG:
            int[] ivalues = new int[count];
            for (j = 0; j < count; j++) {
               ivalues[j] = readInt(stream);
            }
            obj = ivalues;
            break;

         case XTIFFField.TIFF_SRATIONAL:
            int[][] iivalues = new int[count][2];
            for (j = 0; j < count; j++) {
               iivalues[j][0] = readInt(stream);
               iivalues[j][1] = readInt(stream);
            }
            obj = iivalues;
            break;

         case XTIFFField.TIFF_FLOAT:
            float[] fvalues = new float[count];
            for (j = 0; j < count; j++) {
               fvalues[j] = readFloat(stream);
            }
            obj = fvalues;
            break;

         case XTIFFField.TIFF_DOUBLE:
            double[] dvalues = new double[count];
            for (j = 0; j < count; j++) {
               dvalues[j] = readDouble(stream);
            }
            obj = dvalues;
            break;

         default:
            System.err.println(JaiI18N.getString("XTIFFDirectory0"));
            break;
      }
      return obj;
   }

   /**
    * Method for reading a field from stream. Positions stream at the next field
    * location.
    */
   private XTIFFField readField()
         throws IOException, ArrayIndexOutOfBoundsException {
      // int j;
      int tag = readUnsignedShort(stream);
      int type = readUnsignedShort(stream);
      int count = (int) readUnsignedInt(stream);
      int value = 0;

      // The place to return to to read the next tag
      long nextTagOffset = stream.getFilePointer() + 4;

      try {
         // If the tag data can't fit in 4 bytes, the next 4 bytes
         // contain the starting offset of the data
         if (count * sizeOfType(type) > 4) {
            value = (int) (readUnsignedInt(stream));
            stream.seek(value);
         }
      } catch (ArrayIndexOutOfBoundsException ae) {
         System.err.println(tag + " " + JaiI18N.getString("XTIFFDirectory4"));
         // if the data type is unknown we should skip this TIFF Field
         stream.seek(nextTagOffset);
         throw ae;
      }

      Object obj = readFieldValue(tag, type, count);

      // Position stream at next field and return this one
      stream.seek(nextTagOffset);

      return createField(tag, type, count, obj);
   }

   // Methods to read primitive data types from the stream

   protected short readShort(SeekableStream stream)
         throws IOException {
      if (isBigEndian) {
         return stream.readShort();
      } else {
         return stream.readShortLE();
      }
   }

   protected int readUnsignedShort(SeekableStream stream)
         throws IOException {
      if (isBigEndian) {
         int val = stream.readUnsignedShort();
         return val;
      } else {
         int val = stream.readUnsignedShortLE();
         return val;
      }
   }

   protected int readInt(SeekableStream stream)
         throws IOException {
      if (isBigEndian) {
         return stream.readInt();
      } else {
         return stream.readIntLE();
      }
   }

   protected long readUnsignedInt(SeekableStream stream)
         throws IOException {
      if (isBigEndian) {
         return stream.readUnsignedInt();
      } else {
         return stream.readUnsignedIntLE();
      }
   }

   protected long readLong(SeekableStream stream)
         throws IOException {
      if (isBigEndian) {
         return stream.readLong();
      } else {
         return stream.readLongLE();
      }
   }

   protected float readFloat(SeekableStream stream)
         throws IOException {
      if (isBigEndian) {
         return stream.readFloat();
      } else {
         return stream.readFloatLE();
      }
   }

   protected double readDouble(SeekableStream stream)
         throws IOException {
      if (isBigEndian) {
         return stream.readDouble();
      } else {
         return stream.readDoubleLE();
      }
   }

   // Static methods used by the public static method below

   private static int readUnsignedShort(SeekableStream stream, boolean isBigEndian)
         throws IOException {
      if (isBigEndian) {
         return stream.readUnsignedShort();
      } else {
         return stream.readUnsignedShortLE();
      }
   }

   private static long readUnsignedInt(SeekableStream stream, boolean isBigEndian)
         throws IOException {
      if (isBigEndian) {
         return stream.readUnsignedInt();
      } else {
         return stream.readUnsignedIntLE();
      }
   }

   // Utilities

   /**
    * Returns the number of image directories (subimages) stored in a given TIFF
    * file, represented by a <code>SeekableStream</code>.
    */
   public static int getNumDirectories(SeekableStream stream)
         throws IOException {
      long pointer = stream.getFilePointer(); // Save stream pointer

      stream.seek(0L);
      int endian = stream.readUnsignedShort();
      if (!isValidEndianTag(endian)) {
         throw new IllegalArgumentException(JaiI18N.getString("XTIFFDirectory1"));
      }
      boolean isBigEndian = (endian == 0x4d4d);
      int magic = readUnsignedShort(stream, isBigEndian);
      if (magic != 42) {
         throw new IllegalArgumentException(JaiI18N.getString("XTIFFDirectory2"));
      }

      stream.seek(4L);
      long offset = readUnsignedInt(stream, isBigEndian);

      int numDirectories = 0;
      while (offset != 0L) {
         ++numDirectories;

         stream.seek(offset);
         int entries = readUnsignedShort(stream, isBigEndian);
         stream.skip(12 * entries);
         offset = readUnsignedInt(stream, isBigEndian);
      }

      stream.seek(pointer); // Reset stream pointer
      return numDirectories;
   }

   /**
    * Returns a boolean indicating whether the byte order used in the the TIFF
    * file is big-endian (i.e. whether the byte order is from the most
    * significant to the least significant)
    */
   public boolean isBigEndian() {
      return isBigEndian;
   }

   /**
    * Specifies the type of compression to be used. The compression type
    * specified will be honored only if it is compatible with the image being
    * written out.
    * 
    * @param compression The compression type.
    */
   public void setCompression(int compression) {
      // this.compression = compression;
      // Check to see if compression supported
      // Add Field
      addField(XTIFF.TIFFTAG_COMPRESSION, XTIFFField.TIFF_SHORT, 1, new char[] {
         (char) compression
      });
   }

   /**
    * Return the type of compression indicated in the TIFF fields, or
    * XTIFF.COMPRESSION_NON if not specified.
    */
   public int getCompression() {
      if (getField(XTIFF.TIFFTAG_COMPRESSION) == null)
         return XTIFF.COMPRESSION_NONE;
      return (int) getFieldAsLong(XTIFF.TIFFTAG_COMPRESSION);
   }

   /**
    * If set, the data will be written out in tiled format, instead of in
    * strips.
    * 
    * @param isTiled Specifies whether the image data should be wriiten out in
    *        tiled format.
    */
   public void setIsTiled(boolean isTiled) {
      this._isTiled = isTiled;
   }

   /**
    * Constructs a tile codec for decoding data, using the compression defined
    * in the current directory.
    * 
    * @param param the encoding param
    * @see XTIFFTileCodec
    */
   public XTIFFTileCodec createTileCodec(XTIFFDecodeParam param)
         throws IOException {
      int compression = getCompression();
      XTIFFTileCodec codec = getTileCodec(compression);
      if (codec == null)
         throw new IOException("Compression type (" + compression + ") not supported");
      return codec.create(param);
   }

   /**
    * Constructs a tile codec for encoding data, using the compression defined
    * in the current directory.
    * 
    * @param param the encoding param
    * @see XTIFFTileCodec
    */
   public XTIFFTileCodec createTileCodec(XTIFFEncodeParam param)
         throws IOException {
      int compression = getCompression();
      XTIFFTileCodec codec = getTileCodec(compression);
      if (codec == null)
         throw new IOException("Compression type (" + compression + ") not supported");
      return codec.create(param);
   }

   /**
    * Set the XTIFFFactory, which is used to construct the XTIFFDirectory object
    * assigned as a "tiff.directory" property in the resulting jai image.
    * 
    * @param fact the factory to register. The factory is guaranteed to always
    *        be non-null; if a null is passed in then the default XTIFFFactory
    *        is used. a null object is passed in
    * @see XTIFFFactory
    */
   public static void setFactory(XTIFFFactory fact) {
      if (fact == null)
         factory = new XTIFFFactory();
      else
         factory = fact;
   }

   /**
    * Constructs a XTIFFDirectory from a SeekableStream. The directory parameter
    * specifies which directory to read from the linked list present in the
    * stream; directory 0 is normally read but it is possible to store multiple
    * images in a single TIFF file by maintaing multiple directories.
    * 
    * @param stream a SeekableStream to read from.
    * @param directory the index of the directory to read.
    * @see XTIFFFactory
    */
   public static XTIFFDirectory create(SeekableStream stream, int directory)
         throws IOException {
      return factory.createDirectory(stream, directory);
   }

   /**
    * Constructs a TIFFDirectory by reading a SeekableStream. The ifd_offset
    * parameter specifies the stream offset from which to begin reading; this
    * mechanism is sometimes used to store private IFDs within a TIFF file that
    * are not part of the normal sequence of IFDs. Uses the XTIFFFactory to do
    * this, so to extend the directory class, the factory method should be
    * extended and registered instead of this one.
    * 
    * @param stream a SeekableStream to read from.
    * @param ifd_offset the long byte offset of the directory.
    * @see XTIFFFactory
    */
   public static XTIFFDirectory create(SeekableStream stream, long ifd_offset)
         throws IOException {
      return factory.createDirectory(stream, ifd_offset);
   }

   /**
    * Constructs an XTIFFDirectory from the currently. registered XTIFFDirectory
    * factory.
    * 
    * @see XTIFFFactory
    */
   public static XTIFFDirectory create() {
      return factory.createDirectory();
   }

   /**
    * Return the currently registered XTIFFTileCodec for this compression type.
    * Used by the XTIFFImage to decode the compression data.
    * 
    * @see XTIFFTileCodec
    */
   public static XTIFFTileCodec getTileCodec(int comp) {
      return (XTIFFTileCodec) tileCodecs.get(new Integer(comp));
   }

   /**
    * UnRegister the XTIFFTileCodec corresponding to the TIFF compression type.
    * 
    * @param comp The TIFF compression code indicated
    */
   public static void unRegisterTileCodec(int comp) {
      XTIFFTileCodec cod = getTileCodec(comp);
      tileCodecs.remove(cod);
   }

   /**
    * Register a new XTIFFTileCodec for encoding and decoding compressed TIFF
    * image data. This overrides any existing codec previously registered.
    * 
    * @param comp The TIFF compression code indicated by the
    * @param codec The codec to register. XTIFF.TIFFTAG_COMPRESSION field.
    * @see XTIFFTileCodec
    */
   public static void registerTileCodec(int comp, XTIFFTileCodec codec) {
      tileCodecs.put(new Integer(comp), codec);
   }

   /**
    * Get the JAI Image decoded type. This method is called by the
    * XTIFFTileCodeImpl object during the decode() method to determine what type
    * of colorspace and sample model to use.
    */
   public int getImageType() {
      return imageType;
   }

   /**
    * Set the JAI Image decoded type. This method is called by the XTIFFImage
    * constructor to indicate to the XTIFFTileCodec what type of colorspace and
    * sample model to use. The types are enumerated in the XTIFF class.
    * 
    * @see XTIFF
    * @see XTIFFImage
    * @see XTIFFTileCodec
    */
   public void setImageType(int image_type) {
      imageType = image_type;
   }
}
