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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/rpf/RpfFrame.java,v $
// $RCSfile: RpfFrame.java,v $
// $Revision: 1.10 $
// $Date: 2006/08/17 15:19:05 $
// $Author: dietrick $
// 
// **********************************************************************

/*
 * Some of the ideas for this code is based on source code provided by
 * The MITRE Corporation, through the browse application source code.
 * Many thanks to Nancy Markuson who provided BBN with the software,
 * to Theron Tock, who wrote the software, and Daniel Scholten, who
 * revised it - (c) 1994 The MITRE Corporation for those parts, and
 * used/distributed with permission.  Namely, the frame file reading
 * mechanism is the part that has been modified.
 */

package com.bbn.openmap.layer.rpf;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.MemoryImageSource;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import com.bbn.openmap.io.BinaryBufferedFile;
import com.bbn.openmap.io.BinaryFile;
import com.bbn.openmap.io.FormatException;
import com.bbn.openmap.layer.nitf.NitfHeader;
import com.bbn.openmap.omGraphics.OMRasterObject;
import com.bbn.openmap.util.Debug;

/**
 * The object that organizes the information found within the RPF frame file.
 * The RpfFrame handles reading through the different sections, and holds on to
 * the compressed subframe data. The cache handler gets the compressed subframe
 * data and decompresses it before storing the uncompressed subframe in the
 * cache.
 */
public class RpfFrame {

   boolean valid = false;

   protected NitfHeader nitfHeader;
   protected RpfHeader header;
   protected RpfFileSections fileSections;
   protected RpfAttributes attributes;
   protected RpfFileSections.RpfCoverageSection coverage;
   protected RpfColortable colortable;
   String report;

   byte[][][] compressedSubframe = new byte[6][6][];
   byte[][][] table = new byte[4][4096][4];
   /* DKS NEW for CHUMMED subfr info. [y][x] */
   boolean[][] chummed = new boolean[6][6];
   /* DKS NEW for masked subfr info: WAS EXTERNAL. */
   boolean[][] masked = new boolean[6][6];

   /** Want to bother with Dchum? */
   boolean Dchum = false;
   int chumVersion; /* Chum version: 2,3,etc. */

   int numCharsInDesc; // ushort, # chars in DCHUM descriptor string
   // */
   int descCount; /* # descriptors */
   /** Array of descriptor strings */
   String[] descriptors; // char
   // desc_str[MAX_NUM_DESC][MAX_DESC_LEN];
   /** Array of descriptor dates */
   String[] descriptorDates; // char desc_date[MAX_NUM_DESC][9];

   protected boolean DEBUG_RPFDETAIL = false;
   protected boolean DEBUG_RPFFRAME = false;

   /** Loads the RpfFrame, given a complete path to the file. */
   public RpfFrame(String framePath) {
      DEBUG_RPFDETAIL = Debug.debugging("rpfdetail");
      DEBUG_RPFFRAME = Debug.debugging("rpfframe");
      initFile(framePath);
   }

   /**
    * Loads the RpfFrame, given the RpfFrameEntry that the RpfCacheHandler got
    * from the RpfTocHandler.
    */
   public RpfFrame(RpfFrameEntry rfe) {
      this(rfe.framePath);

      if (!isValid() && rfe.exists && rfe.rpfdirIndex != -1) {
         // Check lower case, if we think it exists and the rpf dir
         // is not null. If it is null, then the path we tried is
         // a complete file path (not a relative one) and should be
         // right.

         String lowerCaseFramePath = rfe.framePath.substring(rfe.rpfdirIndex + 3);
         lowerCaseFramePath = lowerCaseFramePath.toLowerCase();

         String rpfDir = rfe.framePath.substring(0, rfe.rpfdirIndex + 3);

         if (DEBUG_RPFFRAME) {
            Debug.output("RpfFrame " + rfe.framePath + " not found, checking " + rpfDir + lowerCaseFramePath);
         }

         if (initFile(rpfDir + lowerCaseFramePath)) {
            // Update it for the next time we check
            rfe.framePath = rpfDir + lowerCaseFramePath;
         } else {
            // Update check so we don't keep looking again.
            rfe.exists = false;
         }
      }

      Dchum = true;
   }

   // public void finalize() {
   // Debug.message("gc", "RpfFrame: getting GC'd");
   // }

   protected boolean initFile(String framePath) {
      try {
         BinaryFile binFile = new BinaryBufferedFile(framePath);
         read(binFile);
         binFile.close();
      } catch (FileNotFoundException e) {
         Debug.error("RpfFrame: file " + framePath + " not found");
         valid = false;
      } catch (IOException ioe) {
         Debug.error("RpfFrame: File IO Error while handling NITF header:\n" + ioe);
         valid = false;
      } catch (NullPointerException npe) {
         Debug.error("RpfFrame: File IO Error NPE:\n" + npe);
         npe.printStackTrace();
         valid = false;
      }
      return valid;
   }

   public boolean isValid() {
      return valid;
   }

   /**
    * Create the screen text used on a subframe. The internal string is set.
    * 
    * @param Cib whether the frame is a Cib frame. The report is different if it
    *        is.
    */
   protected void setReport(boolean Cib) {
      if (attributes != null) {
         StringBuffer s = new StringBuffer();
         s.append("<br><b>RPF Currency Date:</b> ").append(attributes.currencyDate);
         s.append("<br><b>RPF Production Date:</b> ").append(attributes.productionDate);
         s.append("<br><b>Source Significant Date:</b> ").append(attributes.significantDate);
         if (Cib) {
            s.append("<br><b>Map Source:</b> ").append(attributes.dataSource);
         } else {
            s.append("<br><b>Map Designation:</b> ").append(attributes.mapDesignationCode);
            s.append("<br><b>Map Series:</b> ").append(attributes.chartSeriesCode);
            s.append("<br><b>Map Edition:</b> ").append(attributes.edition);
         }
         report = s.toString();
      }
   }

   /**
    * Get the attribute html text to display on the screen. This goes to the
    * RpfSubframe object. The RpfCacheHandler knows about the four variables.
    * 
    * @param x subframe index within the array from the TocEntry.
    * @param y subframe index within the array from the TocEntry
    * @param entry the RpfFrameEntry describing the frame.
    * @param Cib whether the frame is an imagery frame.
    */
   public String getReport(int x, int y, RpfFrameEntry entry, boolean Cib) {
      StringBuffer s = new StringBuffer("<html><body>");

      x %= 6;
      y %= 6;
      if (entry != null) {
         s.append("<br><b>Frame Name:</b> ");
         // s.append(entry.filename);
         s.append(entry.framePath.substring(entry.filenameIndex));
      } else {
         s.append("<br><b>Frame Name:</b> Unavailable.");
      }
      s.append("<br><b>Subframe</b> ").append(x).append(", ").append(y).append("<br>");

      if (report == null)
         setReport(Cib); // preset the attribute part of the info.
      if (report != null)
         s.append(report);

      if (entry != null) {
         s.append("<br><b>From Frame Dir:</b> ");

         String actualFilePath = entry.framePath.substring(0, entry.filenameIndex);

         if (actualFilePath.length() > 20) {
            int start = 0;
            int index = actualFilePath.indexOf("/", 15);
            while (index != -1) {
               s.append(actualFilePath.substring(start, index));
               s.append("/<br>    ");
               start = index + 1;
               index = actualFilePath.indexOf("/", start + 15);
            }
            s.append(actualFilePath.substring(start));
         }

         else
            s.append(actualFilePath);
      }
      s.append("<br></body></html>");
      return s.toString().trim();
   }

   /**
    * Get the NitfFile header.
    */
   public NitfHeader getNitfHeader() {
      return nitfHeader;
   }

   /**
    * Get the RpfFrame header.
    */
   public RpfHeader getHeader() {
      return header;
   }

   /**
    * Get the different file sections.
    */
   public RpfFileSections getFileSections() {
      return fileSections;
   }

   /**
    * Get the attributes for the RpfFrame.
    */
   public RpfAttributes getAttributes() {
      return attributes;
   }

   /**
    * Get the coverage section.
    */
   public RpfFileSections.RpfCoverageSection getCoverage() {
      return coverage;
   }

   /**
    * The only reason to call this is to read the colortable that is within the
    * frame file, and set the colors that you will be using for all the frames
    * accordingly. The RpfColortable is passed in so you can set the opaqueness,
    * number of colors, and other colortable variables inside your own
    * colortable object, and then read the color conversion tables as they apply
    * (inside the frame file). Since the frame file is read when the RpfFrame is
    * created, the fileSections object will (should) be valid.
    */
   public Color[] getColors(BinaryFile binFile, RpfColortable ct) {

      fileSections.parseColorSection(binFile, ct);
      return ct.colors;
   }

   /**
    * Load the colortable with the colors from a particular frame file. Not
    * needed, really, since the frame file is now loading it's own colortable at
    * loadtime.
    */
   public static Color[] getColors(String framePath, RpfColortable ct) {
      BinaryFile binFile = null;
      try {
         binFile = new BinaryBufferedFile(framePath);
         // binFile = new BinaryFile(framePath);
         RpfFileSections rfs = new RpfFileSections();
         RpfHeader head = new RpfHeader();

         head.read(binFile);
         binFile.seek(head.locationSectionLocation);
         rfs.parse(binFile);

         Color[] ret = rfs.parseColorSection(binFile, ct);

         binFile.close();
         return ret;
      } catch (FileNotFoundException e) {
         Debug.error("RpfFrame: getColortable(): file " + framePath + " not found");
      } catch (IOException ioe) {
         Debug.error("RpfFrame: getColortable(); File IO Error!\n" + ioe);
      }
      return null;
   }

   /**
    * Get the colortable stored inside this RpfFrame.
    * 
    * @return RpfColortable
    */
   public RpfColortable getColortable() {
      return colortable;
   }

   /** Read the RPF frame. */
   public boolean read(BinaryFile binFile) {

      Compression compression;
      LookupTable[] lookupTable = new LookupTable[4];
      Image image;

      int[][] indices = new int[6][6]; // ushort
      int i, j;

      /* bool (uchar) */
      /* all subframes present indicator */
      boolean allSubframes;
      long currentPos; // uint
      long lookupOffsetTableOffset; // uint
      int lookupTableOffsetRecLen; // ushort

      long subframeMaskTableOffset; // uint
      /* subframe offset (mask section) */
      long[][] subframeOffset = new long[6][6];// uint[][]

      /* for DCHUM */
      long fsave; /* saved file loc */
      int chummedSubframe; // uint
      int attributeId; // ushort
      int attributeParamId; // uchar
      long attributeRecOffset; // uint
      int numAttributeOffsetRecs; // ushort
      int numSubframesChummed; // ushort

      if (DEBUG_RPFDETAIL) {
         Debug.output("ENTER RPFFRAME.READ");
      }

      try {
         // Let's start at the beginning, shall we?
         binFile.seek(0);

         // Read the NITF part of the file...
         nitfHeader = new NitfHeader();
         // If false, it might not be a NITF file, start over...
         if (!nitfHeader.read(binFile))
            binFile.seek(0);

         header = new RpfHeader();
         // Now, read the RPF header...
         if (!header.readHeader(binFile))
            return false;

         if (DEBUG_RPFDETAIL)
             Debug.output(header.toString());
         
         /* Check date for validity: date should be "1993xxxx" */
         if (!header.standardDate.startsWith("199") && !header.standardDate.startsWith("20")) {
            Debug.output("RpfFrame.read: Invalid date in header: " + header.standardDate);
            return false;
         }

         // Need to do this right after the header...
         binFile.seek(header.locationSectionLocation);
         fileSections = new RpfFileSections(binFile);

         RpfFileSections.RpfLocationRecord[] loc = fileSections.getLocations(RpfFileSections.FRAME_LOCATION_KEY);

         attributes = fileSections.parseAttributes(binFile);
         coverage = fileSections.parseCoverageSection(binFile);

         colortable = new RpfColortable();
         getColors(binFile, colortable);

         /* DKS: from index to componentLocation */
         if (loc[0] == null) {
            Debug.output("RpfFrame: No compression section!");
            return false;
         }

         /* Read the compression tables */
         binFile.seek(loc[0].componentLocation);
         compression = new Compression(binFile);
         if (DEBUG_RPFDETAIL)
            Debug.output(compression.toString());

         if (loc[2] == null) {
            Debug.output("Warning: Can't find compr. lookup subsection in FrameFile:");
            Debug.output("   Using alternate computation");
            /* length of compr. sect. subhdr = 10 */
            binFile.seek(loc[0].componentLocation + 10);
         } else {
            /*
             * DKS: Position at start of compression lookup table offset record
             */
            if (DEBUG_RPFDETAIL) {
               Debug.output("Comp lkup subsect: loc[2].componentLocation(264?): " + loc[2].componentLocation);
            }
            binFile.seek(loc[2].componentLocation);
         }

         /* 2 new hdr fields */

         lookupOffsetTableOffset = (long) binFile.readInteger();
         lookupTableOffsetRecLen = (int) binFile.readShort();

         if (DEBUG_RPFDETAIL) {
            Debug.output("lookupOffsetTableOffset(6): " + lookupOffsetTableOffset);
            Debug.output("lookupTableOffsetRecLen(14): " + lookupTableOffsetRecLen);
         }

         /* For each compression table */
         for (i = 0; i < 4; i++) {
            lookupTable[i] = new LookupTable(binFile);
            if (DEBUG_RPFDETAIL) {
               Debug.output("Compression lookup table offset record " + i);
               Debug.output(lookupTable[i].toString());
            }

            if (lookupTable[i].records != 4096 || lookupTable[i].values != 4 || lookupTable[i].bitLength != 8) {
               Debug.output("RpfFrame: Bad VQ info in compression record");
               return false;
            }
         } /* for i */

         for (i = 0; i < 4; i++) { /* Read compression lookup table */
            /*
             * new position from compression lookup subsection: loc[2]
             */
            binFile.seek(loc[2].componentLocation + lookupTable[i].offset);
            if (DEBUG_RPFDETAIL) {
               currentPos = binFile.getFilePointer();
               Debug.output("Read compr. lookup table (4x4096) at position: " + currentPos);
            }
            for (j = 0; j < 4096; j++)
               table[i][j] = binFile.readBytes(4, false);

         } /*
            * for i=1 to 4 (# compression tables, 1 for each pixel row)
            */

         /* seek to LOC_ATTRIB_SUBHEADER, ID=141 */
         if ((Dchum) && (chumVersion > 1)) { /*
                                              * Chum selected and file version >
                                              * 1
                                              */
            if (loc[6] == null) {
               Debug.output("RpfFrame: Can't find ATTRIBUTE_SUBHEADER section!");
               return false;
            }
            if (DEBUG_RPFDETAIL)
               Debug.output("ATTRIBUTE SUBHEADER location: " + loc[6].componentLocation);

            binFile.seek(loc[6].componentLocation);

            numAttributeOffsetRecs = (int) binFile.readShort();
            if (DEBUG_RPFDETAIL)
               Debug.output("numAttributeOffsetRecs: " + numAttributeOffsetRecs);

            /* Go to Attrib subsection */
            if (loc[7] == null) {
               Debug.output("RpfFrame: Can't find ATTRIBUTE_SECTION in Frame file");
               return false;
            }

            if (DEBUG_RPFDETAIL)
               Debug.output("ATTRIBUTE SECTION location: " + loc[7].componentLocation);

            binFile.seek(loc[7].componentLocation);

            descCount = 0; /* # descriptor strings so far */

            for (i = 0; i < numAttributeOffsetRecs; i++) {
               attributeId = (int) binFile.readShort();
               attributeParamId = binFile.read();
               /* tempc = */binFile.read();
               attributeRecOffset = (long) binFile.readInteger();

               /* # subframes impacted */
               if ((attributeId == 24) && (attributeParamId == 4)) {
                  /* save file loc */
                  fsave = binFile.getFilePointer();
                  /* Go to proper spot in attrib section */
                  binFile.seek(loc[7].componentLocation + attributeRecOffset);
                  /* read # subframes impacted */
                  numSubframesChummed = (int) binFile.readShort();

                  if (DEBUG_RPFDETAIL) {
                     Debug.output("n_attrib_chummedSubframe: " + numSubframesChummed);
                  }
                  /* Read list of subframes chummed */
                  /* Assume these are next in file */
                  for (j = 0; j < numSubframesChummed; j++) {
                     chummedSubframe = (int) binFile.readShort();

                     if (DEBUG_RPFDETAIL) {
                        Debug.output("chummedSubframe: " + chummedSubframe);
                     }

                     /* y,x */
                     chummed[chummedSubframe / 6][chummedSubframe % 6] = true;
                  } /* for j */
                  binFile.seek(fsave); /* restore file pos */
               } /* if 24,4 */

               /* Update date */
               if ((attributeId == 24) && (attributeParamId == 3)) {
                  /* save file loc */
                  fsave = binFile.getFilePointer();
                  /* Go to proper spot in attrib section */
                  binFile.seek(loc[7].componentLocation + attributeRecOffset);
                  /* read date */
                  descriptorDates[descCount] = binFile.readFixedLengthString(8);

                  if (DEBUG_RPFDETAIL)
                     Debug.output("descriptorDate: " + descriptorDates[descCount]);

                  binFile.seek(fsave); /* restore file pos */
               } /* if 24,3 */

               /* # chars in descriptor */
               if ((attributeId == 24) && (attributeParamId == 6)) {
                  /* save file loc */
                  fsave = binFile.getFilePointer();
                  /* Go to proper spot in attrib section */
                  binFile.seek(loc[7].componentLocation + attributeRecOffset);
                  /* read # chars in descriptor */

                  numCharsInDesc = (int) binFile.readShort();

                  if (DEBUG_RPFDETAIL) {
                     Debug.output("Prepare to fread descriptors[descCount]");
                     Debug.output("RpfFrame.read: descCount: " + descCount);
                  }

                  descriptors[descCount] = binFile.readFixedLengthString(numCharsInDesc);

                  /* Array of strings, not 2-d array !!!!???? */
                  if (DEBUG_RPFDETAIL) {
                     Debug.output("descriptors[descCount]: " + descriptors[descCount]);
                  }
                  descCount++; /* string number */

                  binFile.seek(fsave); /* restore file pos */
               } /* if 24,6 */
            } /* for i */
         } /* if Dchum */

         /* READ THE IMAGE DATA */
         if (DEBUG_RPFDETAIL) {
            Debug.output("Image descr. subheader location: loc[1].componentLocation(68576?): " + loc[1].componentLocation);
         }
         binFile.seek(loc[1].componentLocation);
         image = new Image(binFile);

         /* New, DKS. NULL (FF) if no subfr mask table */
         subframeMaskTableOffset = binFile.readInteger();

         if (DEBUG_RPFDETAIL) {
            Debug.output(image.toString());
            Debug.output("subframeMaskTableOffset: " + subframeMaskTableOffset);
         }

         if (subframeMaskTableOffset == 0) { /* ERROR Check */
            Debug.error("RpfFrame.read(): subframeMaskTableOffset==0.");
            return false;
         }

         if (subframeMaskTableOffset == 0xFFFFFFFF)
            allSubframes = true;
         else
            allSubframes = false;

         if (Debug.debugging("rpfframe")) {
            Debug.output("allSubframes: " + allSubframes);
         }

         if (!allSubframes) { /* Read mask data */
            /* fseek to LOC_MASK_SUBSECTION, ID=138 */
            if (loc[5] == null) {
               Debug.error("RpfFrame.read(): Can't find MASK_SUBSECTION section in Frame file");
               return false;
            }
            if (DEBUG_RPFDETAIL) {
               Debug.output("MASK SUBSECTION location: " + loc[5].componentLocation);
            }

            binFile.seek(loc[5].componentLocation + subframeMaskTableOffset);

            for (i = 0; i < 6; i++) { /* y */
               for (j = 0; j < 6; j++) {
                  subframeOffset[i][j] = (long) binFile.readInteger();
                  if (subframeOffset[i][j] == 0xFFFFFFFF)
                     masked[i][j] = true; /* subfr masked */

                  if (DEBUG_RPFDETAIL) {
                     Debug.output("i:" + i + ", j:" + j + ", masked[i][j]: " + masked[i][j]);
                  }

               } /* for j */
            } /* for i */
         } /* if !allSubframes */

         if (image.vertSubframes != 6 || image.horizSubframes != 6) {
            Debug.output("Not 6x6 subframes per frame: must be masked.");
         }

         // rowBytes = 256 / 4 * 3 / 2;

         // Is this section needed??
         /* fseek to LOC_IMAGE_DISPLAY_PARAM_SUBHEADER, ID=137 */
         if (loc[4] == null) {
            Debug.error("RpfFrame.read(): Can't find IMAGE_DISPLAY_PARAM_SUBHEADER section!");
            return false;
         }

         /* Image Display Parameters Subheader */
         if (DEBUG_RPFDETAIL) {
            Debug.output("IMAGE Display params subheader location: " + loc[4].componentLocation);
         }
         binFile.seek(loc[4].componentLocation);

         /* Go to start of image spatial data subsection */
         if (loc[3] == null) {
            Debug.output("WARNING: Can't find Image spatial data subsection in FrameFile:");
            Debug.output("   Using alternate computation");
            /*
             * DKS. skip 14 bytes of image display parameters subheader instead
             */
            binFile.seek(loc[4].componentLocation + 14);
         } else {
            /*
             * DKS: Position at start of image spatial data subsection
             */
            currentPos = binFile.getFilePointer();
            if (DEBUG_RPFDETAIL) {
               Debug.output("Current frame file position(68595?): " + currentPos);
               Debug.output("Image spatial data subsect: loc[3](68609?): " + loc[3].componentLocation);
            }

            binFile.seek(loc[3].componentLocation);
         } /* else */

         /* Read subframes from top left, row-wise */
         for (i = 0; i < 6; i++) { /* row */
            for (j = 0; j < 6; j++) { /* col */
               /* DKS. New: init indices to valid subframes */
               indices[i][j] = i * 6 + j;
               /* (256/4)=64. 64*64 * 12bits / 8bits = 6144 bytes */
               if (!masked[i][j]) {
                  compressedSubframe[i][j] = binFile.readBytes(6144, false);
                  if (DEBUG_RPFDETAIL)
                     Debug.output(" i:" + i + ", j:" + j + ", read image data. rc(6144):" + compressedSubframe[i][j].length);
               } else
                  compressedSubframe[i][j] = new byte[6144];
            }
         }

      } catch (IOException e) {
         Debug.error("RpfFrame: read(): File IO Error!\n" + e);
         return false;
      } catch (FormatException f) {
         Debug.error("RpfFrame: read(): File IO Format error!" + f);
         return false;
      }

      if (DEBUG_RPFDETAIL) {
         Debug.output("LEAVE RPFFRAME.READ");
      }

      valid = true;
      return valid;
   } /* read */

   static public class Compression {
      public int algorithm;// ushort
      /* New, dks */
      public int numOffsetRecs;// ushort
      public int numParmOffRecs;// ushort

      public Compression(BinaryFile binFile) {
         try {
            algorithm = (int) binFile.readShort();
            numOffsetRecs = (int) binFile.readShort();
            numParmOffRecs = (int) binFile.readShort();
         } catch (IOException e) {
            Debug.error("Compression: File IO Error!\n" + e);
         } catch (FormatException f) {
            Debug.error("Compression: File IO Format error!\n" + f);
         }
      }

      public String toString() {
         StringBuffer s = new StringBuffer();
         s.append("Compression.algorithm: ").append(algorithm).append("\n");
         s.append("Compression.numOffsetRecs: ").append(numOffsetRecs).append("\n");
         s.append("Compression.numParmOffRecs: ").append(numParmOffRecs).append("\n");
         return s.toString();
      }
   }

   static public class LookupTable {
      int id; // ushort
      long records; // uint
      int values; // ushort
      int bitLength; // ushort
      long offset; // uint

      public LookupTable(BinaryFile binFile) {
         try {
            id = (int) binFile.readShort();
            records = (long) binFile.readInteger();
            values = (int) binFile.readShort();
            bitLength = (int) binFile.readShort();
            offset = (long) binFile.readInteger();
         } catch (IOException e) {
            Debug.error("Compression: File IO Error!\n" + e);
         } catch (FormatException f) {
            Debug.error("Compression: File IO Format error!\n" + f);
         }
      }

      public String toString() {
         StringBuffer s = new StringBuffer();
         s.append("LookupTable.id: ").append(id).append("\n");
         s.append("LookupTable.records: ").append(records).append("\n");
         s.append("LookupTable.values: ").append(values).append("\n");
         s.append("LookupTable.bitLength: ").append(bitLength).append("\n");
         s.append("LookupTable.offset: ").append(offset).append("\n");
         return s.toString();
      }
   }

   static public class Image {
      int spectralGroups; // ushort
      int subframeTables;// ushort
      int spectralTables;// ushort
      int spectralLines;// ushort
      int horizSubframes, vertSubframes;// ushort
      long outputColumns, outputRows;// uint

      public Image(BinaryFile binFile) {
         try {
            spectralGroups = (int) binFile.readShort();
            subframeTables = (int) binFile.readShort();
            spectralTables = (int) binFile.readShort();
            spectralLines = (int) binFile.readShort();
            horizSubframes = (int) binFile.readShort();
            vertSubframes = (int) binFile.readShort();
            outputColumns = (long) binFile.readInteger();
            outputRows = (long) binFile.readInteger();
         } catch (IOException e) {
            Debug.error("Compression: File IO Error!\n" + e);
         } catch (FormatException f) {
            Debug.error("Compression: File IO Format error!\n" + f);
         }
      }

      public String toString() {
         StringBuffer s = new StringBuffer();
         s.append("Image.spectralGroups: ").append(spectralGroups).append("\n");
         s.append("Image.subframeTables: ").append(subframeTables).append("\n");
         s.append("Image.spectralTables: ").append(spectralTables).append("\n");
         s.append("Image.spectralLines: ").append(spectralLines).append("\n");
         s.append("Image.horizSubframes: ").append(horizSubframes).append("\n");
         s.append("Image.vertSubframes: ").append(vertSubframes).append("\n");
         s.append("Image.outputColumns: ").append(outputColumns).append("\n");
         s.append("Image.outputRows: ").append(outputRows).append("\n");
         return s.toString();
      }
   }

   /**
    * Decompress a subframe into a cache entry OMRaster (RpfSubframe). The
    * RpfSubframe is returned, too, to emphasize what's happening.
    * 
    * @param x the x coord for the subframe
    * @param y the y coord for the subframe
    * @param subframe the subframe to create the image for. The resulting image
    *        will be loaded into the RpfSubframe. If null, a new RpfSubframe
    *        will be created.
    * @param colortable the colortable to use with this image. If null, the
    *        colortable from this RpfFrame will be used.
    * @param viewAttributes our image generation parameters.
    * @return RpfSubframe containing the image data.
    */
   public RpfSubframe decompressSubframe(int x, int y, RpfSubframe subframe, RpfColortable colortable,
                                         RpfViewAttributes viewAttributes) {

      boolean isDirectColorModel = (viewAttributes.colorModel == OMRasterObject.COLORMODEL_DIRECT);

      if (subframe == null) {
         subframe = new RpfSubframe();
      }

//      if (viewAttributes != null) {
//         subframe.setColorModel(viewAttributes.colorModel);
//      }

      if (colortable == null) {
         colortable = this.colortable;
      }

      if (!isDirectColorModel) {
         if (DEBUG_RPFDETAIL) {
            Debug.output("RpfFrame: decompress to byte[]");
         }
         byte[] pixels = decompressSubframe(x, y);
         subframe.setBitsAndColors(pixels, colortable.colors);
      } else {
         int[] pixels = decompressSubframe(x, y, colortable);
         subframe.setPixels(pixels);
      }
      return subframe;
   }

   /**
    * Decompress a subframe into an array of bytes suitable for in indexed color
    * model image.
    * 
    * @param x the x coord for the subframe
    * @param y the y coord for the subframe
    */
   public byte[] decompressSubframe(int x, int y) {
      // Convert x,y to the subframe index in the frame - they come
      // in as
      // cache subframe indexes
      x %= 6;
      y %= 6;

      // used to keep track of location into compressedSubframe
      // array.
      int readptr = 0;

      // and the compressedSubframe array
      byte[] compressedSubframe = this.compressedSubframe[y][x];

      /*
       * This should never occur since all subframes should be present
       */
      /*
       * But if it does occur, just put up black pixels on the screen
       */
      if ((compressedSubframe == null) || masked[y][x]) {
         return null;
      } else { // Normal pixel */
         byte[] pixels = new byte[256 * 256];
         for (int i = 0; i < 256; i += 4) {
            for (int j = 0; j < 256; j += 8) {
               int firstByte = compressedSubframe[readptr++] & 0xff;
               int secondByte = compressedSubframe[readptr++] & 0xff;
               int thirdByte = compressedSubframe[readptr++] & 0xff;

               // because dealing with half-bytes is hard, we
               // uncompress two 4x4 tiles at the same time. (a
               // 4x4 tile compressed is 12 bits )

               /* Get first 12-bit value as index into VQ table */
               int val1 = (firstByte << 4) | (secondByte >> 4);
               /* Get second 12-bit value as index into VQ table */
               int val2 = ((secondByte & 0x000F) << 8) | thirdByte;
               for (int t = 0; t < 4; t++) {
                  for (int e = 0; e < 4; e++) {
                     int tableVal1 = table[t][val1][e] & 0xff;
                     int tableVal2 = table[t][val2][e] & 0xff;
                     if (tableVal1 >= RpfColortable.CADRG_COLORS) {
                        tableVal1 = RpfColortable.CADRG_COLORS - 1;
                     }
                     if (tableVal2 >= RpfColortable.CADRG_COLORS) {
                        tableVal2 = RpfColortable.CADRG_COLORS - 1;
                     }
                     int pixindex = (i + t) * 256 + j + e;
                     pixels[pixindex] = (byte) tableVal1;
                     pixels[pixindex + 4] = (byte) tableVal2;
                  } // for e
               } // for t
            } /* for j */
         } // for i
         return pixels;
      } /* else */
   }

   /**
    * Decompress a subframe into an array of ints suitable for a direct color
    * model image. (argb format)
    * 
    * @param x the x coord for the subframe
    * @param y the y coord for the subframe
    * @param colortable the colortable to use with this image. If null, the
    *        RpfColortable from the frame will be used.
    */
   public int[] decompressSubframe(int x, int y, RpfColortable colortable) {
      // Convert x,y to the subframe index in the frame - they come
      // in as
      // cache subframe indexes
      x %= 6;
      y %= 6;

      // used to keep track of location into compressedSubframe
      // array.
      int readptr = 0;

      // and the compressedSubframe array
      byte[] compressedSubframe = this.compressedSubframe[y][x];

      if (colortable == null) {
         colortable = this.colortable;
      }

      /*
       * This should never occur since all subframes should be present
       */
      /*
       * But if it does occur, just put up black pixels on the screen
       */
      if ((compressedSubframe == null) || masked[y][x]) {
         return null;
      } else { // Normal pixel */
         int[] pixels = new int[256 * 256];
         for (int i = 0; i < 256; i += 4) {
            for (int j = 0; j < 256; j += 8) {
               int firstByte = compressedSubframe[readptr++] & 0xff;
               int secondByte = compressedSubframe[readptr++] & 0xff;
               int thirdByte = compressedSubframe[readptr++] & 0xff;

               // because dealing with half-bytes is hard, we
               // uncompress two 4x4 tiles at the same time. (a
               // 4x4 tile compressed is 12 bits )

               /* Get first 12-bit value as index into VQ table */
               int val1 = (firstByte << 4) | (secondByte >> 4);
               /* Get second 12-bit value as index into VQ table */
               int val2 = ((secondByte & 0x000F) << 8) | thirdByte;
               for (int t = 0; t < 4; t++) {
                  for (int e = 0; e < 4; e++) {
                     int tableVal1 = table[t][val1][e] & 0xff;
                     int tableVal2 = table[t][val2][e] & 0xff;
                     if (tableVal1 >= RpfColortable.CADRG_COLORS) {
                        tableVal1 = RpfColortable.CADRG_COLORS - 1;
                     }
                     if (tableVal2 >= RpfColortable.CADRG_COLORS) {
                        tableVal2 = RpfColortable.CADRG_COLORS - 1;
                     }
                     int pixindex = (i + t) * 256 + j + e;
                     pixels[pixindex] = colortable.colors[tableVal1].getRGB();
                     pixels[pixindex + 4] = colortable.colors[tableVal2].getRGB();
                  } // for e
               } // for t
            } /* for j */
         } // for i
         return pixels;
      } /* else */
   }

   public static void main(String[] argv) {
      Debug.init();

      com.bbn.openmap.util.ArgParser ap = new com.bbn.openmap.util.ArgParser("RpfFrame");

      ap.add("attributes", "Only write out the attributes for this frame.");
      ap.add("view", "Only bring up a window with the frame image.");
      ap.add("frame", "Path to the frame to view. \"-frame\" only needed if other arguments are used.", 1);

      if (!ap.parse(argv)) {
         ap.printUsage();
         System.exit(0);
      }

      String arg[];
      boolean viewAttributes = false;
      arg = ap.getArgValues("attributes");
      if (arg != null) {
         viewAttributes = true;
         Debug.put("rpfframe");
         Debug.put("rpfdetail");
      }

      boolean viewFrame = false;
      arg = ap.getArgValues("view");
      if (arg != null) {
         viewFrame = true;
      }

      RpfFrame rpfFrame;

      arg = ap.getArgValues("frame");
      if (arg != null) {
         rpfFrame = new RpfFrame(arg[0]);
         if (viewFrame) {
            rpfFrame.view();
         }
      } else {
         if (viewAttributes == false) {
            Debug.put("rpfframe");
            Debug.put("rpfdetail");
         }
         rpfFrame = new RpfFrame(argv[0]);
         rpfFrame.view();
      }
   }

   /**
    * A quick hack to pop up a window that displays the entire frame image.
    */
   public void view() {
      int height = 256;
      int width = 256;

      BufferedImage bigImage = new BufferedImage(width * 6, height * 6, BufferedImage.TYPE_INT_RGB);
      GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
      Graphics g = ge.createGraphics(bigImage);
      Toolkit tk = Toolkit.getDefaultToolkit();

      for (int x = 0; x < 6; x++) {
         for (int y = 0; y < 6; y++) {
            int[] pixels = decompressSubframe(x, y, colortable);

            java.awt.Image bitmap = tk.createImage(new MemoryImageSource(width, height, pixels, 0, width));

            g.drawImage(bitmap, x * 256, y * 256, null);
         }
      }

      JLabel picture = new JLabel(new ImageIcon(bigImage));
      JFrame frame = com.bbn.openmap.util.PaletteHelper.getPaletteWindow(picture, "RPF Frame", null);
      frame.setSize(new Dimension(500, 500));
      frame.setVisible(true);
   }
}