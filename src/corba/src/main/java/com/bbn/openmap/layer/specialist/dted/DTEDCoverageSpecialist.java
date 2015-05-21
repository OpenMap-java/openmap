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
// $Source: /cvs/distapps/openmap/src/corba/com/bbn/openmap/layer/specialist/dted/DTEDCoverageSpecialist.java,v $
// $RCSfile: DTEDCoverageSpecialist.java,v $
// $Revision: 1.9 $
// $Date: 2009/02/23 22:37:32 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.specialist.dted;

/*  Java Core  */
import java.awt.Color;
import java.io.File;
import java.util.Properties;
import java.util.StringTokenizer;

import org.omg.CORBA.StringHolder;

import com.bbn.openmap.corba.CSpecialist.CProjection;
import com.bbn.openmap.corba.CSpecialist.GraphicChange;
import com.bbn.openmap.corba.CSpecialist.LLPoint;
import com.bbn.openmap.corba.CSpecialist.UGraphic;
import com.bbn.openmap.corba.CSpecialist.XYPoint;
import com.bbn.openmap.corba.CSpecialist.CColorPackage.EColor;
import com.bbn.openmap.corba.CSpecialist.GraphicPackage.EGraphic;
import com.bbn.openmap.corba.CSpecialist.RectanglePackage.ERectangle;
import com.bbn.openmap.layer.dted.DTEDCoverageManager;
import com.bbn.openmap.layer.dted.DTEDFrameColorTable;
import com.bbn.openmap.layer.specialist.MakeProjection;
import com.bbn.openmap.layer.specialist.SGraphic;
import com.bbn.openmap.layer.specialist.Specialist;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.OMRect;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.Debug;

/**
 * HACK: this specialist copies functionality from the DTEDCoverageLayer.
 */
public class DTEDCoverageSpecialist
      extends Specialist {

   /** The paths to the DTED directories, telling where the data is. */
   protected String[] paths = {
      "/mnt/cdrom/dted",
      "/mnt/disk/dted"
   };

   /** Flag to tell the cache to return the coverage for level 0 dted. */
   protected boolean showDTEDLevel0 = true;
   /** Flag to tell the cache to return the coverage for level 1 dted. */
   protected boolean showDTEDLevel1 = true;
   /** Flag to tell the cache to return the coverage for level 0 dted. */
   protected boolean showDTEDLevel2 = true;

   /** The default line color for level 0. */
   public final static Color defaultLevel0Color = new Color(0xCE4F3F); // redish
   /** The default line color for level 1. */
   public final static Color defaultLevel1Color = new Color(0x339159); // greenish
   /** The default line color for level 2. */
   public final static Color defaultLevel2Color = new Color(0x0C75D3); // bluish

   /** The color to outline the shapes for level 0. */
   protected Color level0Color = defaultLevel0Color;
   /** The color to outline the shapes for level 1. */
   protected Color level1Color = defaultLevel1Color;
   /** The color to outline the shapes for level 2. */
   protected Color level2Color = defaultLevel2Color;

   /**
    * A setting for how transparent to make the images. The default is 255,
    * which is totally opaque.
    */
   protected int opaqueness = DTEDFrameColorTable.DEFAULT_OPAQUENESS;
   /** Flag to fill the coverage rectangles. */
   protected boolean fillRects = false;
   /**
    * Location of coverage summary file. If it doesn't exists, one will be
    * created here for later use.
    */
   protected String coverageFile = "/mnt/disk/coverage.dted";

   /**
    * Location of coverage summary file, if supplied as a URL. If it doesn't
    * exists, a coverage file will be used instead.
    */
   protected String coverageURL = null;

   protected DTEDCoverageManager coverageManager = null;

   /** The array of coverage for level 0 data. */
   protected boolean[][] level0Frames = new boolean[180][360];
   /** The array of coverage for level 1 data. */
   protected boolean[][] level1Frames = new boolean[180][360];
   /** The array of coverage for level 2 data. */
   protected boolean[][] level2Frames = new boolean[180][360];

   // private final static transient String showLevel0Command =
   // "showLevel0";
   // private final static transient String showLevel1Command =
   // "showLevel1";
   // private final static transient String showLevel2Command =
   // "showLevel2";
   private final static transient XYPoint nullP1 = new XYPoint((short) 0, (short) 0);

   /** The property describing the locations of level 0 and 1 data. */
   public static final String DTEDPathsProperty = ".paths";

   /** Property setting to show level 0 data on startup. */
   public static final String ShowLevel0Property = ".level0.showcov";
   /**
    * Property to use to change the color for coverage of level 0 data.
    */
   public static final String Level0ColorProperty = ".level0.color";

   /** Property setting to show level 1 data on startup. */
   public static final String ShowLevel1Property = ".level1.showcov";
   /**
    * Property to use to change the color for coverage of level 1 data.
    */
   public static final String Level1ColorProperty = ".level1.color";

   /** Property setting to show level 2 data on startup. */
   public static final String ShowLevel2Property = ".level2.showcov";
   /**
    * Property to use to change the color for coverage of level 2 data.
    */
   public static final String Level2ColorProperty = ".level2.color";
   /** Property to use for filled rectangles (when java supports it). */
   public static final String OpaquenessProperty = ".opaque";
   /** Property to use to fill rectangles. */
   public static final String FillProperty = ".fill";
   /**
    * The file to read/write coverage summary. If it doesn't exist here, it will
    * be created and placed here.
    */
   public static final String CoverageFileProperty = ".coverageFile";
   /**
    * A URL to read coverage summary. If it doesn't exist, the coverage file
    * will be tried.
    */
   public static final String CoverageURLProperty = ".coverageURL";

   /**
    * The default constructor for the Layer. All of the attributes are set to
    * their default values.
    */
   public DTEDCoverageSpecialist() {
      super("DTEDCoverageSpecialist", (short) 2, false);
   }

   protected void init() {
      System.out.println("DTEDCoverageSpecialist: Figuring out which DTED frames exist! (This is a one-time operation!)");
      System.out.println("Scanning for frames - This could take over (2) minutes!");

      coverageManager = new DTEDCoverageManager(paths);
   }

   /**
    * Set all the DTED properties from a properties object.
    * 
    * @param prefix string prefix used in the properties file for this layer.
    * @param properties the properties set in the properties file.
    */
   public void setProperties(String prefix, java.util.Properties properties) {

      paths = initPathsFromProperties(properties.getProperty(prefix + DTEDPathsProperty));

      coverageFile = properties.getProperty(prefix + CoverageFileProperty);
      coverageURL = properties.getProperty(prefix + CoverageURLProperty);

      String fillString = properties.getProperty(prefix + FillProperty);
      if (fillString != null)
         fillRects = Boolean.valueOf(fillString).booleanValue();

      String opaqueString = properties.getProperty(prefix + OpaquenessProperty);
      try {
         opaqueness = Integer.valueOf(opaqueString).intValue();
      } catch (NumberFormatException e) {
         System.err.println("Unable to parse " + OpaquenessProperty + " = " + opaqueString);
         opaqueness = DTEDFrameColorTable.DEFAULT_OPAQUENESS;
      }

      level0Color = parseColor(properties, prefix + Level0ColorProperty, defaultLevel0Color);

      level1Color = parseColor(properties, prefix + Level1ColorProperty, defaultLevel1Color);

      level2Color = parseColor(properties, prefix + Level2ColorProperty, defaultLevel2Color);

      String showLevel0String = properties.getProperty(prefix + ShowLevel0Property);
      if (showLevel0String != null)
         showDTEDLevel0 = Boolean.valueOf(showLevel0String).booleanValue();

      String showLevel1String = properties.getProperty(prefix + ShowLevel1Property);
      if (showLevel1String != null)
         showDTEDLevel1 = Boolean.valueOf(showLevel1String).booleanValue();

      String showLevel2String = properties.getProperty(prefix + ShowLevel2Property);
      if (showLevel2String != null)
         showDTEDLevel2 = Boolean.valueOf(showLevel2String).booleanValue();
   }

   /**
    * Takes a String of File.separator separated paths, and returns an array of
    * strings instead.
    * 
    * @param rawPaths the string of paths separated by a File.separator.
    * @return Array of strings representing paths to dted directories.
    */
   protected String[] initPathsFromProperties(String rawPaths) {
      String[] retPaths = null;
      if (rawPaths != null) {

         try {
            StringTokenizer token = new StringTokenizer(rawPaths, File.pathSeparator);
            int numPaths = token.countTokens();

            retPaths = new String[numPaths];
            for (int i = 0; i < numPaths; i++) {
               retPaths[i] = token.nextToken();
            }
            return retPaths;
         } catch (java.util.NoSuchElementException e) {
            e.printStackTrace();
         }
      }
      return retPaths;
   }

   /**
    * Take a string, representing the hex values for a color, and convert it to
    * a java Color.
    * 
    * @param p properties.
    * @param propName the name of the property.
    * @param dfault the default color to use if the property value doesn't work.
    * @return the java Color.
    */
   protected Color parseColor(Properties p, String propName, Color dfault) {
      String colorString = p.getProperty(propName);
      if (colorString == null) {
         return dfault;
      } else {
         try {
            return parseColor(colorString);
         } catch (NumberFormatException e) {
            System.err.println("Unparseable number \"" + colorString + "\" in property \"" + propName + "\"");
            return dfault;
         }
      }
   }

   /**
    * Take a string, representing the hex values for a color, and convert it to
    * a java Color.
    * 
    * @param colorString the hex string value (RGB)
    * @return the java Color.
    */
   protected Color parseColor(String colorString)
         throws NumberFormatException {
      // parse color as hexidecimal RGB value
      int colorSpec = Integer.parseInt(colorString, 16);
      if (colorSpec < 0) {
         return OMGraphic.clear;
      } else {
         return new Color(colorSpec);
      }
   }

   public UGraphic[] fillRectangle(CProjection p, LLPoint ll1, LLPoint ll2, String staticArgs, StringHolder dynamicArgs,
                                   GraphicChange notifyOnChange, String uniqueID) {
      System.out.println("DTEDCoverageSpecialist.fillRectangle()");

      Projection proj = MakeProjection.getProjection(p);

      OMGraphicList omGraphicLists = coverageManager.getCoverageRects(proj);

      // ///////////////////
      // safe quit
      if (omGraphicLists != null) {

         UGraphic[] level0UGraphics = new UGraphic[] {};
         UGraphic[] level1UGraphics = new UGraphic[] {};
         UGraphic[] level2UGraphics = new UGraphic[] {};

         if (showDTEDLevel0) {
            level0UGraphics = createUGraphics((OMGraphicList) omGraphicLists.get(0));
         }
         if (showDTEDLevel1) {
            level1UGraphics = createUGraphics((OMGraphicList) omGraphicLists.get(1));
         }
         if (showDTEDLevel2) {
            level2UGraphics = createUGraphics((OMGraphicList) omGraphicLists.get(2));
         }

         UGraphic[] ugraphics = new UGraphic[level0UGraphics.length + level1UGraphics.length + level2UGraphics.length];
         int off = 0;
         System.arraycopy(level0UGraphics, 0, ugraphics, off, level0UGraphics.length);
         off += level0UGraphics.length;
         System.arraycopy(level1UGraphics, 0, ugraphics, off, level1UGraphics.length);
         off += level1UGraphics.length;
         System.arraycopy(level2UGraphics, 0, ugraphics, off, level2UGraphics.length);

         System.out.println("DTEDCoverageSpecialist.fillRectangle(): returning " + ugraphics.length + " graphics");
         return ugraphics;
      } else {
         System.out.println("DTEDCoverageSpecialist.fillRectangle(): finished with null graphics list");
         return new UGraphic[0];
      }
   }

   public void signOff(String uniqueID) {
      System.out.println("DTEDCoverageSpecialist.signOff()");
   }

   protected UGraphic[] createUGraphics(OMGraphicList omgraphics) {
      int len = omgraphics.size();
      UGraphic[] ugraphics = new UGraphic[len];
      ERectangle er;
      OMGraphic gr;
      OMRect omr;
      int lineColor, fillColor;
      EGraphic eg;
      UGraphic ug;
      for (int i = 0; i < len; i++) {
         gr = omgraphics.getOMGraphicAt(i);
         if (gr instanceof OMRect) {
            omr = (OMRect) gr;
            eg = SGraphic.createEGraphic();
            lineColor = omr.getLineColor().getRGB();
            eg.color =
                  new EColor(null, (short) ((lineColor & 0xff0000) >> 8), (short) (lineColor & 0x00ff00),
                             (short) ((lineColor & 0x0000ff) << 8));
            if (fillRects) {
               fillColor = omr.getFillColor().getRGB();
               eg.fillColor =
                     new EColor(null, (short) ((fillColor & 0xff0000) >> 8), (short) (fillColor & 0x00ff00),
                                (short) ((fillColor & 0x0000ff) << 8));
            }
            er =
                  new ERectangle(eg, nullP1, nullP1, new LLPoint((float) omr.getNorthLat(), (float) omr.getWestLon()),
                                 new LLPoint((float) omr.getSouthLat(), (float) omr.getEastLon()));
            ug = new UGraphic();
            ug.erect(er);
            ugraphics[i] = ug;
         } else {
            // Deal with embeded OMGraphicList. HACK this is
            // inefficient if there are a bunch of embedded
            // OMGrahicLists...
            UGraphic[] x_ugraphics = createUGraphics((OMGraphicList) gr);
            len = x_ugraphics.length + ugraphics.length - 1;
            UGraphic[] new_ugraphics = new UGraphic[len];
            System.arraycopy(ugraphics, 0, new_ugraphics, 0, i);
            System.arraycopy(x_ugraphics, 0, new_ugraphics, i, x_ugraphics.length);

            i += x_ugraphics.length;
            ugraphics = new_ugraphics;
         }
      }
      return ugraphics;
   }

   public void printHelp() {
      System.err.println("usage: java [java/vbj args] <specialist class> [specialist args]");
      System.err.println("");
      System.err.println("       Java Args:");
      System.err.println("       -mx<NUM>m               Set max Java heap in Megs");
      System.err.println("       ...");
      System.err.println("");
      System.err.println("       VBJ Args:");
      System.err.println("       -DORBmbufSize=4194304   Define the VBJ buffer size");
      System.err.println("       -DORBdebug              Enable VBJ debugging");
      System.err.println("       ...");
      System.err.println("");
      System.err.println("       Specialist Args:");
      System.err.println("       -ior <iorfile>                  IOR file (MUST SPECIFY)");
      System.err.println("       -covfile <covfile>              Coverage file (R/W)");
      System.err.println("       -dtedpaths \"<path1> ...\"      Path to search for DTED data");
      System.err.println("       -dted2paths \"<path1> ...\"     Path to search for DTED level2 data");
      System.exit(1);
   }

   private String[] getPaths(String str) {
      StringTokenizer tok = new StringTokenizer(str);
      int len = tok.countTokens();
      String[] paths = new String[len];
      for (int j = 0; j < len; j++) {
         paths[j] = tok.nextToken();
      }
      return paths;
   }

   public void parseArgs(String[] args) {
      for (int i = 0; i < args.length; i++) {
         if (args[i].equalsIgnoreCase("-dtedpaths")) {
            paths = getPaths(args[++i]);
         } else if (args[i].equalsIgnoreCase("-covfile")) {
            coverageFile = args[++i];
         }
      }
      super.parseArgs(args);
   }

   public static void main(String[] args) {
      Debug.init(System.getProperties());

      // Create the specialist server
      DTEDCoverageSpecialist srv = new DTEDCoverageSpecialist();
      srv.parseArgs(args);
      srv.init();
      srv.start(null);
   }
}