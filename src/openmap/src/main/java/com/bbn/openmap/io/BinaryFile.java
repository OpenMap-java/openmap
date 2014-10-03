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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/io/BinaryFile.java,v $
// $RCSfile: BinaryFile.java,v $
// $Revision: 1.14 $
// $Date: 2009/01/21 01:24:42 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.io;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Vector;

import com.bbn.openmap.Environment;
import com.bbn.openmap.MoreMath;
import com.bbn.openmap.util.Debug;

/**
 * The BinaryFile is the standard object used to access data files. It acts like
 * a RandomAccessFile, but will work on jar file contents and URLs, too. The
 * source of the data is isolated through the InputReader interface.
 */
public class BinaryFile {
   private static int openCount = 0;
   private static int classCount = 0;

   private InputReader inputReader = null;

   /**
    * The byte order of the underlying file. (<code>true</code>== MSB-First ==
    * big-endian)
    */
   protected boolean MSBFirst = false;

   /**
    * Constructs a new BinaryFile with the specified file as the input. The
    * default byte-order is LSB first. Reads start at the first byte of the
    * file.
    * 
    * @param f the file to be opened for reading
    * @exception IOException pass-through errors from opening a RandomAccessFile
    *            with f
    * @see java.io.RandomAccessFile
    */
   public BinaryFile(File f)
         throws IOException {
      inputReader = new FileInputReader(f);
      classCount++;
      openCount++;
   }

   /**
    * Constructs a new BinaryFile with the specified inputReader as the input.
    * 
    * @param inputReader the input reader to be opened for reading
    */
   private BinaryFile(InputReader inputReader) {
      this.inputReader = inputReader;
      classCount++;
      openCount++;
   }

   /**
    * Constructs a new BinaryFile with the specified file as the input. The
    * byte-order is undefined. Reads start at the first byte of the file. This
    * constructor looks for the file with the string given, and will call the
    * correct constructor as appropriate. If the string represents a file
    * available locally, then the BinaryFile will be accessed with a
    * FileInputReader using a RandomAccessFile. If it's only available as a
    * resource, then a StreamInputReader will be used. The name should be a path
    * to a file, or the name of a resource that can be found in the classpath,
    * or a URL.
    * 
    * @param name the name of the file to be opened for reading
    * @exception IOException pass-through errors from opening the file.
    */
   public BinaryFile(String name)
         throws IOException {
      boolean showDebug = false;
      if (Debug.debugging("binaryfile")) {
         showDebug = true;
      }

      if (showDebug) {
         Debug.output("BinaryFile: trying to figure out how to handle " + name);
      }

      try {
         File file = null;
         URL url = null;

         if (!Environment.isApplet()) {
            file = new File(name);
         }

         if (file != null && file.exists()) {
            // If the string represents a file, then we want to
            // use the RandomAccessFile aspect of the BinaryFile.
            setInputReader(new FileInputReader(file));
         } else {
            // see JNLP deploy tip here
            // http://java.sun.com/javase/6/docs/technotes/guides/jweb/deployment_advice.html#ClassLoader_and_Resources
            final InputStream resourceStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(name);
            if (resourceStream != null) {
               if (showDebug) {
                  Debug.output("BinaryFile: loading " + name + " via getResourceAsStream");
               }
               final String urlName = name;
               setInputReader(new StreamInputReader() {
                  {
                     this.name = urlName;
                     this.inputStream = resourceStream;
                  }

                  @Override
                  protected void reopen()
                        throws IOException {
                     super.reopen();
                     inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(name);
                  }
               });
            } else {
               // If the getResourceAsStream didn't work, go back to the old way of doing things...
               // url = ClassLoader.getSystemResource(name);
               url = Thread.currentThread().getContextClassLoader().getResource(name);

               // OK, now we want to look around for the file, in the
               // classpaths, and as a resource. It may be a file in
               // a classpath, available for direct access.
               if (url != null) {

                  String newname = url.getFile();
                  if (showDebug) {
                     Debug.output("BinaryFile: looking for " + newname);
                  }

                  if (!Environment.isApplet()) {
                     file = new File(newname);
                  }

                  if (file != null && file.exists()) {
                     // It's still a file, available directly.
                     // Access it with the RandomAccessFile
                     setInputReader(new FileInputReader(file));
                  } else {
                     // Need to get it as a resource. Needs
                     // special handling if it's coming in a jar
                     // file. Jar file references have a "!" in
                     // them
                     if (!setJarInputReader(newname)) {
                        if (showDebug) {
                           Debug.output(" trying as url: " + url);
                        }
                        setInputReader(new URLInputReader(url));
                     }

                  }

               } else if (Environment.isApplet()) {
                  if (showDebug) {
                     Debug.output(" As applet, checking codebase...");
                  }
                  // Look in the codebase for applets...
                  URL[] cba = new URL[1];
                  cba[0] = Environment.getApplet().getCodeBase();

                  URLClassLoader ucl = URLClassLoader.newInstance(cba);
                  url = ucl.getResource(name);

                  if (url != null) {
                     setInputReader(new URLInputReader(url));
                  }
               }
            }

            // It's not in the classpath, so try it as a URL.
            if (inputReader == null) {

               if (showDebug) {
                  Debug.output(" lastly, trying as URL: " + name);
               }
               try {
                  setInputReader(new URLInputReader(new URL(name)));
               } catch (java.security.AccessControlException ace) {
                  Debug.output("BinaryFile: " + name + " couldn't be accessed.");
                  throw new IOException("AccessControlException trying to fetch " + name + " as a URL");
               }
            }

         }

         if (inputReader == null) {
            throw new FileNotFoundException("BinaryFile can't find: " + name);
         }

         classCount++;
         openCount++;

      } catch (IOException ioe) {
         throw ioe;
      }
   }

   /**
    * Takes a name of a file, and checks to see if it reflects an entry in a jar
    * file. (Check the filename and see if it looks like
    * "jarfile!jarfileentry".) If it is, it separates the path, and set the
    * inputReader to a JarInputReader and returns true. If not, it returns
    * false.
    */
   protected boolean setJarInputReader(String name)
         throws IOException {

      try {
         int index = name.indexOf("!");
         if (index != -1) {

            // Used to be this, modified by Erik Sanders to work
            // with jdk 1.4 plugin
            // String jarFileName =
            // name.substring(name.indexOf(":") + 1, index);

            // changed to this...
            String jarFileName;

            if (name.startsWith("file:")) {
               // java-plugin 1.3 returns local file: strip file:
               // from string
               jarFileName = name.substring(name.indexOf(":") + 1, index);
            } else {
               // java-plugin 1.4 returns reference to server, so
               // leave http:// part

               // Used to start the substring from 1, but changed
               // to 0 thanks to DGK
               jarFileName = name.substring(0, index);
            }

            // skip !/ "
            String jarEntryName = name.substring(index + 2);
            if (Debug.debugging("binaryfile")) {
               Debug.output(" got: \n" + jarFileName + "\n" + jarEntryName);
            }

            // If the jar doesn't exist, should return something
            // that indicates this. Should check the performance
            // implications of this call, though, at some point.

            // DGK added
            File f = new File(jarFileName);
            if (f.exists() == false) {
               return false;
            }

            setInputReader(new JarInputReader(jarFileName, jarEntryName));
            return true;
         }
      } catch (java.security.AccessControlException ace) {
         if (Debug.debugging("binaryfile")) {
            Debug.output("BinaryFile.setJarInputFile: AccessControlException for " + name);
         }
      }

      return false;
   }

   /**
    * A simple test method to determine if a file or directory, represented by a
    * string, can be found by the current Java environment. Uses the same tests
    * as BinaryFile constructor for tracking down a file.
    * 
    * @param name A path to a file, a URL, or a path to a jar file entry.
    * @return true if the file can be found
    */
   public static boolean exists(String name) {
      boolean exists = false;
      try {
         File file = null;
         URL url = null;

         if (!Environment.isApplet()) {
            file = new File(name);
         }

         if (file != null && file.exists()) {
            exists = true;
         } else {
            // url = ClassLoader.getSystemResource(name);
            url = Thread.currentThread().getContextClassLoader().getResource(name);

            // OK, now we want to look around for the file, in the
            // classpaths, and as a resource. It may be a file in
            // a classpath, available for direct access.
            if (url != null) {
               exists = true;
            } else if (Environment.isApplet()) {
               if (Debug.debugging("binaryfile")) {
                  Debug.output(" As applet, checking codebase...");
               }
               // Look in the codebase for applets...
               URL[] cba = new URL[1];
               cba[0] = Environment.getApplet().getCodeBase();

               URLClassLoader ucl = URLClassLoader.newInstance(cba);
               if (ucl.getResource(name) != null) {
                  exists = true;

                  // This has been commented out because the
                  // AppletDataNugget has been deprecated, and
                  // is not needed.

                  // } else {
                  // url = AppletDataNugget.findResource(name);

                  // if (url != null) {
                  // exists = true;
                  // }
               }
            }

            // It's not in the classpath, so try it as a URL to a
            // webserver.
            if (!exists && name.indexOf("http:") != -1) {

               try {
                  InputStream stream = new URL(name).openStream();
                  stream.close();
                  exists = true;
               } catch (java.security.AccessControlException ace) {
                  exists = false;
               }
            }
         }

      } catch (IOException ioe) {
         Debug.message("binaryfile", "BinaryFile.exists() caught IOException");
         exists = false;
      }

      if (Debug.debugging("binaryfile")) {
         Debug.output("BinaryFile.exists(" + name + ") = " + exists);
      }

      return exists;
   }

   /**
    * Get the source name from the input reader.
    */
   public String getName() {
      if (inputReader != null) {
         return inputReader.getName();
      }
      return null;
   }

   /**
    * Get the inputReader used for accessing the file, for querying purposes.
    * Don't use it to get data, or the file pointers may get messed up.
    */
   public InputReader getInputReader() {
      return inputReader;
   }

   /**
    * Set the input reader used by the BinaryFile. Make sure it's initialized
    * properly.
    */
   public void setInputReader(InputReader reader) {
      if (Debug.debugging("binaryfile")) {
         Debug.output("Setting inputReader");
      }
      inputReader = reader;
   }

   /**
    * Set the byte-ordering used to read shorts, int, etc.
    * 
    * @param msbfirst <code>true</code>= MSB first, <code>false</code>= LSB
    *        first
    */
   public void byteOrder(boolean msbfirst) {
      MSBFirst = msbfirst;
   }

   /**
    * Accessor for the byte ordering used to read multibyte types.
    * 
    * @return byte ordering, true means MSB first.
    */
   public boolean byteOrder() {
      return MSBFirst;
   }

   /**
    * Skip over n bytes in the input file
    * 
    * @param n the number of bytes to skip
    * @return the actual number of bytes skipped. annoying, isn't it?
    * @exception IOException Any IO errors that occur in skipping bytes in the
    *            underlying file
    */
   public long skipBytes(long n)
         throws IOException {
      return inputReader.skipBytes(n);
   }

   /**
    * Get the index of the next character to be read
    * 
    * @return the index
    * @exception IOException Any IO errors that occur in accessing the
    *            underlying file
    */
   public long getFilePointer()
         throws IOException {
      return inputReader.getFilePointer();
   }

   /**
    * Set the index of the next character to be read.
    * 
    * @param pos the position to seek to.
    * @exception IOException Any IO Errors that occur in seeking the underlying
    *            file.
    */
   public void seek(long pos)
         throws IOException {
      inputReader.seek(pos);
   }

   /**
    * The length of the InputReader source.
    */
   public long length()
         throws IOException {
      return inputReader.length();
   }

   /**
    * Return how many bytes left to be read in the file.
    * 
    * @return the number of bytes remaining to be read (counted in bytes)
    * @exception IOException Any IO errors encountered in accessing the file
    */
   public long available()
         throws IOException {
      return inputReader.available();
   }

   /**
    * Closes the underlying file, but with a chance for re-opening if accessed
    * again.
    * 
    * @exception IOException Any IO errors encountered in accessing the file
    */
   public void close()
         throws IOException {
      if (inputReader != null) {
         inputReader.close();
         openCount--;
      }
   }

   /**
    * Closes underlying file, get rid of resources and knowledge of file. To be
    * called when you don't need the file any more.
    * 
    * @throws IOException
    */
   public void dispose()
         throws IOException {
      close();
      inputReader = null;
   }

   /**
    * Read from the file.
    * 
    * @return one byte from the file. -1 for EOF
    * @exception IOException Any IO errors encountered in reading from the file
    */
   public int read()
         throws IOException {
      return inputReader.read();
   }

   /**
    * Read from the file
    * 
    * @param b The byte array to read into
    * @param off the first array position to read into
    * @param len the number of bytes to read
    * @return the number of bytes read
    * @exception IOException Any IO errors encountered in reading from the file
    */
   public int read(byte b[], int off, int len)
         throws IOException {
      return inputReader.read(b, off, len);
   }

   /**
    * Read from the file.
    * 
    * @param b the byte array to read into. Equivalent to
    *        <code>read(b, 0, b.length)</code>
    * @return the number of bytes read
    * @exception IOException Any IO errors encountered in reading from the file
    * @see java.io.RandomAccessFile#read(byte[])
    */
   public int read(byte b[])
         throws IOException {
      return inputReader.read(b);
   }

   /**
    * Read from the file.
    * 
    * @param howmany the number of bytes to read
    * @param allowless if we can return fewer bytes than requested
    * @return the array of bytes read.
    * @exception FormatException Any IO Exceptions, plus an end-of-file
    *            encountered after reading some, but now enough, bytes when
    *            allowless was <code>false</code>
    * @exception EOFException Encountered an end-of-file while allowless was
    *            <code>false</code>, but NO bytes had been read.
    */
   public byte[] readBytes(int howmany, boolean allowless)
         throws EOFException, FormatException {

      return inputReader.readBytes(howmany, allowless);
   }

   /**
    * Reads and returns a single byte, cast to a char
    * 
    * @return the byte read from the file, cast to a char
    * @exception EOFException the end-of-file has been reached, so no chars
    *            where available
    * @exception FormatException a rethrown IOException
    */
   public char readChar()
         throws EOFException, FormatException {
      try {
         int retv = inputReader.read();

         if (retv == -1) {
            throw new EOFException("Error in ReadChar, EOF reached");
         }
         return (char) retv;
      } catch (IOException i) {
         throw new FormatException("readChar IOException: " + i.getMessage());
      }
   }

   /**
    * Read a byte from the file, return an unsigned integer.
    * 
    * @return one byte from the file. -1 for EOF causes EOFException
    * @exception IOException Any IO errors encountered in reading from the file
    */
   public int readUnsigned()
         throws IOException, EOFException {
      byte b = (byte) read();
      if (b == -1) {
         throw new EOFException();
      }
      return MoreMath.signedToInt(b);
   }

   /**
    * Reads and returns a short.
    * 
    * @return the 2 bytes merged into a short, according to the current byte
    *         ordering
    * @exception EOFException there were less than 2 bytes left in the file
    * @exception FormatException rethrow of IOExceptions encountered while
    *            reading the bytes for the short
    * @see #read(byte[])
    */
   public short readShort()
         throws EOFException, FormatException {
      // MSBFirst must be set when we are called
      return MoreMath.BuildShort(readBytes(2, false), MSBFirst);
   }

   /**
    * Code for reading shorts that are two-byte integers, high order first, and
    * negatives are signed magnitude. Users may have to switch the bytes and
    * convert negatives to the complement they use. This can be done by putting
    * the low order byte first, then turning off bit 15 (the high order bit),
    * and then multiplying by -1." Basically they are encoded as positive
    * numbers, but bit 15 is set to 1.
    * 
    * @return 2 bytes merged into a short
    * @throws EOFException
    * @throws FormatException
    */
   public short readShortData()
         throws EOFException, FormatException {
      // read in the two bytes
      byte[] bytevec = readBytes(2, false);

      // check for negative values - bit 7 of byte 0
      if (bytevec[0] < 0) {
         // mask bit 7
         bytevec[0] &= 0x7f;
         // create the short and multiply the result by -1
         return ((short) (MoreMath.BuildShort(bytevec, true) * -1));
      }
      return MoreMath.BuildShort(bytevec, true);
   }

   /**
    * Reads and returns a integer from 2 bytes.
    * 
    * @return the 2 bytes merged into a short, according to the current byte
    *         ordering, and then unsigned to int.
    * @exception EOFException there were less than 2 bytes left in the file
    * @exception FormatException rethrow of IOExceptions encountered while
    *            reading the bytes for the short
    * @see #read(byte[])
    */
   public int readUnsignedShort()
         throws EOFException, FormatException {
      // MSBFirst must be set when we are called
      return MoreMath.signedToInt(readShort());
   }

   /**
    * Reads and returns a long
    * 
    * @return the 4 bytes merged into a long, according to the current byte
    *         ordering
    * @exception EOFException there were less than 4 bytes left in the file
    * @exception FormatException rethrow of IOExceptions encountered while
    *            reading the bytes for the integer
    * @see #read(byte[])
    */
   public int readInteger()
         throws EOFException, FormatException {
      // MSBFirst must be set when we are called
      return MoreMath.BuildInteger(readBytes(4, false), MSBFirst);
   }

   public void readIntegerArray(int vec[], int offset, int len)
         throws EOFException, FormatException {
      for (int i = 0; i < len; i++) {
         vec[offset++] = readInteger();
      }
   }

   /**
    * Reads and returns a long
    * 
    * @return the 8 bytes merged into a long, according to the current byte
    *         ordering
    * @exception EOFException there were less than 8 bytes left in the file
    * @exception FormatException rethrow of IOExceptions encountered while
    *            reading the bytes for the long
    * @see #read(byte[])
    */
   public long readLong()
         throws EOFException, FormatException {
      return MoreMath.BuildLong(readBytes(8, false), MSBFirst);
   }

   /**
    * Reads and returns a float
    * 
    * @return the 4 bytes merged into a float, according to the current byte
    *         ordering
    * @exception EOFException there were less than 4 bytes left in the file
    * @exception FormatException rethrow of IOExceptions encountered while
    *            reading the bytes for the float
    * @see #read(byte[])
    */
   public float readFloat()
         throws EOFException, FormatException {
      return Float.intBitsToFloat(readInteger());
   }

   public void readFloatArray(float vec[], int offset, int len)
         throws EOFException, FormatException {
      for (int i = 0; i < len; i++) {
         vec[offset++] = readFloat();
      }
   }

   public void readFloatArray(double vec[], int offset, int len)
         throws EOFException, FormatException {
      for (int i = 0; i < len; i++) {
         vec[offset++] = readFloat();
      }
   }

   /**
    * Reads and returns a double
    * 
    * @return the 8 bytes merged into a double, according to the current byte
    *         ordering
    * @exception EOFException there were less than 8 bytes left in the file
    * @exception FormatException rethrow of IOExceptions encountered while
    *            reading the bytes for the short
    * @see #read(byte[])
    */
   public double readDouble()
         throws EOFException, FormatException {
      return Double.longBitsToDouble(readLong());
   }

   /**
    * Reads <code>length</code> bytes and returns a string composed of the bytes
    * cast to chars
    * 
    * @param length the number of bytes to read into the string
    * @return the composed string
    * @exception EOFException there were less than <code>length</code> bytes
    *            left in the file
    * @exception FormatException rethrow of IOExceptions encountered while
    *            reading the bytes for the short
    */
   public String readFixedLengthString(int length)
         throws EOFException, FormatException {

      byte foo[] = readBytes(length, false);
      return new String(foo, 0, length);
   }

   /**
    * Read a bytes and throw an InvalidCharException if it doesn't match
    * <code>expected</code>
    * 
    * @param expected what the next char is claimed to be
    * @exception EOFException there wasn't a byte, so we can't check for a match
    * @exception InvalidCharException throws when the character read doesn't
    *            match <code>expected</code> The .c member of the thrown
    *            exception is the actual char read
    * @exception FormatException some other error from reading the file
    */
   public void assertChar(char expected)
         throws EOFException, FormatException {
      char c = readChar();
      if (c != expected) {
         throw new InvalidCharException("AssertChar: expected " + expected + " got " + c, c);
      }
   }

   /**
    * Reads a string until the specified delimiter or EOF is encountered
    * 
    * @param delim the end-of-string delimiter
    * @return the string that was read
    * @exception FormatException rethrow of IOExceptions from the read methods
    */
   public String readToDelimiter(char delim)
         throws FormatException {
      StringBuffer buildretval = new StringBuffer();
      char tmp;
      try {
         while ((tmp = readChar()) != delim)
            buildretval.append(tmp);
      } catch (EOFException e) {
         // allowable
      } catch (FormatException fe) {
         if (buildretval.length() == 0) {
            throw fe;
         }
      }
      return buildretval.toString();
   }

   /**
    * Makes sure that the file has been closed.
    * 
    * @exception Throwable what it throws.
    */
   protected void finalize()
         throws Throwable {
      close();
      classCount--;
   }

   /**
    * Maintains a list of objects that can be closed so that other files can be
    * opened.
    */
   private static Vector<WeakReference<Closable>> closableList = new Vector<WeakReference<Closable>>();

   /**
    * Add an object that can be closed if needed. Duplicates are allowed. Only
    * holds a WeakReference, so that the object can still be garbage-collected.
    * 
    * @param it the object that can be closed
    */
   public static synchronized void addClosable(Closable it) {
      closableList.addElement(new WeakReference<Closable>(it));
   }

   /**
    * Remove an object from the closable list.
    * 
    * @param it the object to remove
    */
   public static synchronized void removeClosable(Closable it) {
      for (int i = 0; i < closableList.size(); i++) {
         Object o = closableList.elementAt(i).get();
         if ((o == it) || (o == null)) {
            closableList.removeElementAt(i);
            i--; // in case its in the list more than once
         }
      }
   }

   public static synchronized void closeClosable() {
      System.out.println("closeClosable " + closableList.size());
      for (int i = 0; i < closableList.size(); i++) {
         Closable c = (Closable) ((WeakReference<Closable>) closableList.elementAt(i)).get();
         if ((c == null) || !c.close(false)) {
            closableList.removeElementAt(i);
            i--;
         }
      }
   }

   /**
    * Read the {@link BinaryFile} into memory and return a new
    * {@link BinaryFile} instance working on that in-memory version of the file.
    * 
    * @return BinaryFile object pointing to memory version. 
    * @throws IOException
    */
   public BinaryFile readFully()
         throws IOException {

      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      byte[] buf = new byte[4096];

      seek(0);
      int len;
      while (available() > 0) {
         len = read(buf);
         baos.write(buf, 0, len);
      }

      return new BinaryFile(new ByteArrayInputReader(baos.toByteArray()));
   }

}