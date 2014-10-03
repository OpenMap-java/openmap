//**********************************************************************
//
//<copyright>
//
//BBN Technologies
//10 Moulton Street
//Cambridge, MA 02138
//(617) 873-8000
//
//Copyright (C) BBNT Solutions LLC. All rights reserved.
//
//</copyright>
//**********************************************************************
//
//$Source:
///cvs/darwars/ambush/aar/src/com/bbn/ambush/mission/MissionHandler.java,v
//$
//$RCSfile: GeoTIFFFile.java,v $
//$Revision: 1.3 $
//$Date: 2007/01/22 15:47:37 $
//$Author: dietrick $
//
//**********************************************************************

package com.bbn.openmap.dataAccess.image.geotiff;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotiff.image.KeyRegistry;
import org.geotiff.image.jai.GeoTIFFDescriptor;
import org.geotiff.image.jai.GeoTIFFDirectory;
import org.geotiff.image.jai.GeoTIFFFactory;
import org.libtiff.jai.codec.XTIFFDecodeParam;
import org.libtiff.jai.codec.XTIFFField;
import org.libtiff.jai.codecimpl.XTIFFImageDecoder;

import com.bbn.openmap.dataAccess.image.ImageTile;
import com.bbn.openmap.omGraphics.OMRaster;
import com.bbn.openmap.proj.CADRG;
import com.bbn.openmap.proj.coords.LatLonPoint;
import com.bbn.openmap.util.PropUtils;
import com.sun.media.jai.codec.SeekableStream;

/**
 * GeoTIFFFile is the main object for loading a GeoTIFF image file. Relies on
 * JAI being installed on the machine, because it needs the TIFF capabilities of
 * that package. You can ask for the BufferedImage representing the image in the
 * file, or ask for specific tag information. The GeoTIFFModelFactory can be
 * used to create specific geo-referenced ImageTile objects for display in
 * OpenMap.
 * 
 * @author dietrick
 */
public class GeoTIFFFile {

    public static Logger logger = Logger.getLogger("com.bbn.openmap.dataAccess.image.geotiff.GeoTIFFFile");

    protected GeoTIFFDirectory gtfDirectory;
    protected XTIFFField[] geoKeys;
    protected URL fileURL;

    public GeoTIFFFile(String filePath)
            throws MalformedURLException, IOException, IllegalArgumentException {
        this(PropUtils.getResourceOrFileOrURL(filePath));
    }

    public GeoTIFFFile(URL fileURL)
            throws MalformedURLException, IOException, IllegalArgumentException {
        if (fileURL == null) {
            throw new MalformedURLException("Null file provided as URL");
        }
        init(fileURL);
    }

    public void init(URL fileURL)
            throws IOException, IllegalArgumentException {
        this.fileURL = fileURL;
        SeekableStream ss = SeekableStream.wrapInputStream(fileURL.openStream(), true);
        GeoTIFFDescriptor.register();
        GeoTIFFFactory gtFactory = new GeoTIFFFactory();
        gtfDirectory = (GeoTIFFDirectory) gtFactory.createDirectory(ss, 0);
        geoKeys = gtfDirectory.getGeoKeys();
        ss.close();
    }

    /**
     * Uses the XTIFF Image Decoder to decode as rendered image, creating a new
     * Buffered Image. This is expensive, so if you need the image again, keep
     * it around. Unless it's really huge, I guess.
     * 
     * @return BufferedImage from GeoTIFF
     * @throws IOException if the file URL is null, or if there's a problem
     *         reading the file.
     */
    public BufferedImage getBufferedImage()
            throws IOException {
        if (fileURL == null) {
            throw new IOException("Image Decoder not created for retrieving image, need to init() GeoTIFFFile.");
        }

        SeekableStream ss = SeekableStream.wrapInputStream(fileURL.openStream(), true);
        XTIFFImageDecoder xtffImageDecoder = new XTIFFImageDecoder(ss, new XTIFFDecodeParam());
        RenderedImage ri = xtffImageDecoder.decodeAsRenderedImage();
        BufferedImage bi = new BufferedImage(ri.getColorModel(), ri.copyData(null), false, new Hashtable());
        ss.close();
        return bi;
    }

    /**
     * Helper function designed to make it easier to get the XTIFF field for a
     * given tag number. The easiest way to use this is to ask the KeyRegistry
     * for the code of a tag from a specific map, i.e.
     * 
     * <pre>
     * int tagNumber = KeyRegistry.getCode(KeyRegistry.GEOKEY, &quot;GTModelTypeGeoKey&quot;);
     * 
     * </pre>
     * 
     * Once you have the XTIFFField, you can figure out what type it is, and
     * then ask for its values as that type.
     * 
     * @param tagNumber
     * @return XTIFFField, or null if not found in file.
     */
    public XTIFFField getFieldWithTag(int tagNumber) {
        XTIFFField ret = null;
        XTIFFField[] gtfFields = gtfDirectory.getFields();
        for (int i = 0; i < gtfFields.length; i++) {
            XTIFFField xtff = gtfFields[i];
            int tag = xtff.getTag();
            if (tag == tagNumber) {
                ret = xtff;
                break;
            }
        }
        return ret;
    }

    /**
     * Very handy class from the file. Contains all the XTIFFFields with the tag
     * information.
     * 
     * @return GeoTIFFDirectory that holds GeoTIFF fields.
     */
    public GeoTIFFDirectory getGtfDirectory() {
        return gtfDirectory;
    }

    /**
     * Ask specifically for the array of XTIFFFields pertaining to
     * georeferencing.
     * 
     * @return XTIFFField array for keys.
     */
    public XTIFFField[] getGeoKeys() {
        return geoKeys;
    }

    /**
     * Searches for tag in geo keys. Doesn't go through all of the tags in the
     * file like the getFieldWithTag function, just the fields that have been
     * pre-fetched as geotags.
     * 
     * @return XTIFFField, or null if not found in file.
     */
    protected XTIFFField getGeoFieldForCode(int code) {
        if (geoKeys != null) {
            for (int i = 0; i < geoKeys.length; i++) {
                XTIFFField f = geoKeys[i];
                if (f.getTag() == code) {
                    return f;
                }
            }
        }
        return null;
    }

    /**
     * Pixels derived from scanners or other optical devices represent areas,
     * and most commonly will use the RasterPixelIsArea coordinate system. Pixel
     * data such as digital elevation models represent points, and will probably
     * use RasterPixelIsPoint coordinates.
     * 
     * @return RasterPixelIsArea = 1, RasterPixelIsPoint = 2
     */
    public int getRasterType() {
        return getGeoKeyIntValue(KeyRegistry.getCode(KeyRegistry.GEOKEY, "GTRasterTypeGeoKey"));
    }

    /**
     * Determine which class of model space coordinates are most natural for
     * this dataset:Geographic, Geocentric, or Projected Coordinate System.
     * Usually this will be PCS.
     * 
     * @return ModelTypeProjected = 1 (Projection Coordinate System)
     *         ModelTypeGeographic = 2 Geographic latitude-longitude System)
     *         ModelTypeGeocentric = 3 (Geocentric (X,Y,Z) Coordinate System)
     */
    public int getModelType() {
        return getGeoKeyIntValue(KeyRegistry.getCode(KeyRegistry.GEOKEY, "GTModelTypeGeoKey"));
    }

    /**
     * <pre>
     *                     Here is a summary of the index ranges for the various coding systems used by EPSG in their tables. A copy of this index may be acquired at the FTP sites mentioned in the references in section 5. The &quot;value&quot; table entries below describe how values from one table are related to codes from another table.
     *                    
     *                         Summary
     *                         --------
     *                         Entity                        digit   Range
     *                         ----------------------------  ------- -------------- 
     *                         Prime Meridian                8       8000 thru 8999
     *                         Ellipsoid                     7       7000 thru 7999
     *                         Geodetic Datum                6       6000 thru 6999
     *                         Vertical datum                5       5000 thru 5999
     *                         Geographic Coordinate System  4       4000 thru 4999
     *                         Projected Coordinate Systems  2 or 3  20000 thru 32760
     *                         Map Projection                1       10000 - 19999
     *                         Geodetic Datum Codes
     *                         --------------------
     *                         Datum Type                 Value     Range            Currently Defined
     *                         -------------------------- --------- --------------   -----------------
     *                         Unspecified Geodetic Datum [EC-1000] 6000 thru 6099   6001 thru 6035
     *                         Geodetic Datum                       6100 thru 6321   6200 thru 6315
     *                         WGS 72; WGS 72BE and WGS84           6322 thru 6327   6322 thru 6327
     *                         Geodetic Datum (ancient)             6900 thru 6999   6901 thru 6902
     *                         Note for Values: EC = corresponding Ellipsoid Code.
     *                         Vertical Datum Codes
     *                         --------------------
     *                         Datum Type                 Value     Range            Currently Defined
     *                         -------------------------- --------- --------------   -----------------     
     *                         Ellipsoidal                [EC-1000] 5000 thru 5099   5001 thru 5035
     *                         Orthometric                          5100 thru 5899   5101 thru 5106
     *                         Note for Values: EC = corresponding Ellipsoid Code.
     *                         Geographic Coordinate System Codes    
     *                         ----------------------------------
     *                         GCS Type                    Value      Range           Currently Defined
     *                         -----------------------     ---------- --------------  -----------------        
     *                         Unknown geodetic datum      [GDC-2000] 4000 thru 4099  4001 thru 4045
     *                         Known datum (Greenwich)     [GDC-2000] 4100 thru 4321  4200 thru 4315
     *                         WGS 72; WGS 72BE and WGS84             4322 thru 4327  4322 thru 4327
     *                         Known datum (not Greenwich)            4800 thru 4899  4801 thru 4812
     *                         Known datum (ancient)       [GDC-2000] 4900 thru 4999  4901 thru 4902
     *                         Note for Values: GDC = corresponding Geodetic Datum Code
     *                         Map Projection System Codes
     *                         ---------------------------
     *                         US State Plane  ( 10000-15999 )
     *                         Format:     1sszz            
     *                         where ss is USC&amp;GS State code  01 thru 59  
     *                         zz is (USC&amp;GS zone code)      for NAD27 zones               
     *                         zz is (USC&amp;GS zone code + 30) for NAD83 zones
     *                         
     *                         Larger zoned systems ( 16000-17999 ) 
     *                         System                            Format  zz Range
     *                         --------------------------------  ------- -------
     *                         UTM (North)                       160zz   01   60   
     *                         UTM (South)                       161zz   01   60   
     *                         zoned Universal Gauss-Kruger      162zz   04   32
     *                         Universal Gauss-Kruger (unzoned)  163zz   04   3                  
     *                         Australian Map Grid               174zz   48   58   
     *                         Southern African STM              175zz   13   35 
     *                         Smaller zoned systems  ( 18000-18999 ) 
     *                         Format:  18ssz           
     *                         where ss is sequential system number  01   18   
     *                         z is zone code               
     *                         
     *                         Single zone projections ( 19900-19999 )
     *                         Format:   199ss          
     *                         where ss is sequential system number  00   25
     *                         Projected Coordinate Systems
     *                         ----------------------------      
     *                         For PCS utilizing GeogCS with code in range 4201 through 4321 
     *                         (i.e. geodetic datum code 6201 through 6319):
     *                         As far as is possible the PCS code will be of the format 
     *                         gggzz where ggg is (geodetic datum code -6000) and zz is zone.               
     *                         
     *                         For PCS utilizing GeogCS with code out of range 4201 through 4321
     *                         (i.e.geodetic datum code 6201 through 6319):
     *                         PCS code 20xxx where xxx is a sequential number               
     *                         WGS72 / UTM North     322zz where zz is UTM zone number   32201   32260   
     *                         WGS72 / UTM South     323zz where zz is UTM zone number   32301   32360
     *                         WGS72BE / UTM North   324zz where zz is UTM zone number   32401   32460
     *                         WGS72BE / UTM South   325zz where zz is UTM zone number   32501   32560
     *                         WGS84 / UTM North     326zz where zz is UTM zone number   32601   32660
     *                         WGS84 / UTM South     327zz where zz is UTM zone number   32701   32760
     *                         US State Plane (NAD27)   267xx or 320xx where xx is a sequential number            
     *                         US State Plane (NAD83)   269xx or 321xx where xx is a sequential number
     * 
     * </pre>
     * 
     * @return type code for coordinate system
     */
    public int getProjectedCSType() {
        return getGeoKeyIntValue(KeyRegistry.getCode(KeyRegistry.GEOKEY, "ProjectedCSTypeGeoKey"));
    }

    /**
     * @return type code for geographic type
     */
    public int getGeographicType() {
        return getGeoKeyIntValue(KeyRegistry.getCode(KeyRegistry.GEOKEY, "GeographicTypeGeoKey"));
    }

    /**
     * Helper function for taking a code from the Geo KeyRegistry, and getting
     * the field value as a single int. Should be called for codes that have
     * only one int value, you should read the GeoTIFF spec to know what they
     * are.
     * 
     * @param codeFromKeyRegistry
     * @return the code value, or -1 if not found.
     */
    protected int getGeoKeyIntValue(int codeFromKeyRegistry) {
        XTIFFField field = getGeoFieldForCode(codeFromKeyRegistry);

        if (field != null) {
            int type = field.getType();
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("field type is " + getStringOfType(type));
            }
            if (type == XTIFFField.TIFF_SHORT) {
                return field.getAsInt(0);
            }
        }
        return -1;
    }

    /**
     * Helper function for taking a code from the TIFF spec, and getting the
     * field value as a single int. Should be called for codes that have only
     * one int value, you should read the TIFF spec to know what they are.
     * 
     * @param tiffCode
     * @return the code value, or -1 if not found.
     */
    protected int getFieldIntValue(int tiffCode) {
        XTIFFField field = getFieldWithTag(tiffCode);

        if (field != null) {
            int type = field.getType();
            if (type == XTIFFField.TIFF_SHORT) {
                return field.getAsInt(0);
            }
        }
        return -1;
    }

    /**
     * Prints out the values of the XTIFF Fields provided to it.
     * 
     * @param gtfFields You can get all of the XTIFFFields from the directory
     *        object, or ask this class for the geokeys.
     */
    public void dumpTags(XTIFFField[] gtfFields) {

        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < gtfFields.length; i++) {
            XTIFFField xtff = gtfFields[i];

            int type = xtff.getType();
            int tag = xtff.getTag();
            buf.append("\n\tfield (").append(i).append(") - ").append(tag).append(" (")
               .append(KeyRegistry.getKey(KeyRegistry.GEOKEY, tag)).append("): [");

            switch (type) {
                case XTIFFField.TIFF_ASCII:
                    String[] fieldStrings = xtff.getAsStrings();
                    for (int j = 0; j < fieldStrings.length; j++) {
                        buf.append(fieldStrings[j]);
                        if (j < fieldStrings.length - 1) {
                            buf.append(", ");
                        }
                    }
                    buf.append("]");
                    break;
                case XTIFFField.TIFF_DOUBLE:
                    double[] fieldDoubles = xtff.getAsDoubles();
                    for (int j = 0; j < fieldDoubles.length; j++) {
                        buf.append(fieldDoubles[j]);
                        if (j < fieldDoubles.length - 1) {
                            buf.append(", ");
                        }
                    }
                    buf.append("]");
                    break;
                case XTIFFField.TIFF_FLOAT:
                    double[] fieldFloats = xtff.getAsDoubles();
                    for (int j = 0; j < fieldFloats.length; j++) {
                        buf.append(fieldFloats[j]);
                        if (j < fieldFloats.length - 1) {
                            buf.append(", ");
                        }
                    }
                    buf.append("]");
                    break;
                case XTIFFField.TIFF_BYTE:
                case XTIFFField.TIFF_SBYTE:
                    byte[] fieldBytes = xtff.getAsBytes();
                    for (int j = 0; j < fieldBytes.length; j++) {
                        buf.append(fieldBytes[j]);
                        if (j < fieldBytes.length - 1) {
                            buf.append(", ");
                        }
                    }
                    buf.append("]");
                    break;
                case XTIFFField.TIFF_SSHORT:
                    short[] fieldShorts = xtff.getAsShorts();
                    for (int j = 0; j < fieldShorts.length; j++) {
                        buf.append(fieldShorts[j]);
                        if (j < fieldShorts.length - 1) {
                            buf.append(", ");
                        }
                    }
                    buf.append("]");
                    break;
                case XTIFFField.TIFF_LONG:
                case XTIFFField.TIFF_SHORT:
                    long[] fieldLongs = xtff.getAsLongs();
                    for (int j = 0; j < fieldLongs.length; j++) {
                        buf.append(fieldLongs[j]);
                        if (j < fieldLongs.length - 1) {
                            buf.append(", ");
                        }
                    }
                    buf.append("]");
                    break;
                case XTIFFField.TIFF_SLONG:
                    int[] fieldInts = xtff.getAsInts();
                    for (int j = 0; j < fieldInts.length; j++) {
                        buf.append(fieldInts[j]);
                        if (j < fieldInts.length - 1) {
                            buf.append(", ");
                        }
                    }
                    buf.append("]");
                    break;
                case XTIFFField.TIFF_RATIONAL:
                    long[][] fieldRationals = xtff.getAsRationals();
                    for (int k = 0; k < fieldRationals.length; k++) {
                        buf.append("\n\t");
                        for (int j = 0; j < fieldRationals[0].length; j++) {
                            buf.append(fieldRationals[k][j]);
                            if (j < fieldRationals[k].length - 1) {
                                buf.append(", ");
                            }
                        }
                    }
                    buf.append("\n]");
                    break;
                case XTIFFField.TIFF_SRATIONAL:
                    int[][] fieldSRationals = xtff.getAsSRationals();
                    for (int k = 0; k < fieldSRationals.length; k++) {
                        buf.append("\n\t");
                        for (int j = 0; j < fieldSRationals[0].length; j++) {
                            buf.append(fieldSRationals[k][j]);
                            if (j < fieldSRationals[k].length - 1) {
                                buf.append(", ");
                            }
                        }
                    }
                    buf.append("\n]");
                    break;
                default:
                    // TIFF_UNDEFINED
                    buf.append("Can't handle ").append(type).append(" type.]");
            }
        }
        logger.info(buf.toString());
    }

    /**
     * Helper function that coverts type codes to string representation.
     * 
     * @param type code from XTIFFField.
     * @return String interpretation of type code.
     */
    public String getStringOfType(int type) {
        switch (type) {
            case XTIFFField.TIFF_ASCII:
                return "ASCII";
            case XTIFFField.TIFF_DOUBLE:
                return "double";
            case XTIFFField.TIFF_FLOAT:
                return "float";
            case XTIFFField.TIFF_BYTE:
                return "byte";
            case XTIFFField.TIFF_SBYTE:
                return "sbyte";
            case XTIFFField.TIFF_SSHORT:
                return "sshort";
            case XTIFFField.TIFF_LONG:
                return "long";
            case XTIFFField.TIFF_SHORT:
                return "short";
            case XTIFFField.TIFF_SLONG:
                return "slong";
            case XTIFFField.TIFF_RATIONAL:
                return "rational";
            case XTIFFField.TIFF_SRATIONAL:
                return "srational";
            default:
                return "unknown";
        }
    }

    /**
     * Uses a GeoTIFFModelFactory to create a georeferenced ImageTile image.
     * Only handles 4326 projection right now (WGS84).
     * 
     * @return ImageTile from file
     * @throws IOException
     */
    public ImageTile getImageTile()
            throws IOException {
        GeoTIFFModelFactory gtmf = new GeoTIFFModelFactory(this);
        return gtmf.getImageTile();
    }

    public ImageTile getImageTile(GeoTIFFImageReader id, ImageTile.Cache cache)
            throws IOException {
        GeoTIFFModelFactory gtmf = new GeoTIFFModelFactory(this);
        return gtmf.getImageTile(id, cache);
    }

    public static void main(String[] args) {

        if (args.length < 1) {
            System.out.println("GeoTIFFFile:  Need a path/filename");
            System.exit(0);
        }

        logger.info("GeoTIFFFile: " + args[0]);

        String filePath = null;

        if (args.length > 0) {
            filePath = args[0];
        }

        if (filePath != null) {
            try {
                URL fileURL = PropUtils.getResourceOrFileOrURL(filePath);
                if (fileURL != null) {

                    GeoTIFFFile gtfFile = new GeoTIFFFile(fileURL);
                    BufferedImage bi = gtfFile.getBufferedImage();
                    GeoTIFFDirectory gtfd = gtfFile.getGtfDirectory();
                    double[] tiePoints = gtfd.getTiepoints();

                    System.out.println("------ Tie Point Values ------");
                    for (int i = 0; i < tiePoints.length; i++) {
                        System.out.println(tiePoints[i]);
                    }

                    double[] scaleMatrix = gtfd.getPixelScale();
                    System.out.println("------ Pixel Scale Values ------");
                    for (int i = 0; i < scaleMatrix.length; i++) {
                        System.out.println(scaleMatrix[i]);
                    }

                    System.out.println("----- Geo Keys -------");
                    gtfFile.dumpTags(gtfFile.getGeoKeys());
                    System.out.println("------------");

                    System.out.println("----- All Keys -------");
                    gtfFile.dumpTags(gtfFile.getGtfDirectory().getFields());
                    System.out.println("------------");

                    CADRG crg = new CADRG(new LatLonPoint.Double(0f, 0f), 1500000, 600, 600);

                    final OMRaster omsr = new OMRaster(0, 0, bi);
                    omsr.generate(crg);

                    java.awt.Frame window = new java.awt.Frame(filePath) {
                        public void paint(java.awt.Graphics g) {
                            if (omsr != null) {
                                omsr.render(g);
                            }
                        }
                    };

                    window.addWindowListener(new java.awt.event.WindowAdapter() {
                        public void windowClosing(java.awt.event.WindowEvent e) {
                            // need a shutdown event to notify other gui beans
                            // and
                            // then exit.
                            System.exit(0);
                        }
                    });

                    window.setSize(omsr.getWidth(), omsr.getHeight());
                    window.setVisible(true);
                    window.repaint();

                }

            } catch (MalformedURLException murle) {

            } catch (IOException ioe) {

            }
        }
    }

}
