package org.geotiff.image.jai;

import java.util.Iterator;
import java.util.TreeMap;

import org.libtiff.jai.codec.XTIFF;
import org.libtiff.jai.codec.XTIFFDirectory;
import org.libtiff.jai.codec.XTIFFField;

import com.sun.media.jai.codec.SeekableStream;

/**
 * An extension of the XTIFFDirectory that understands the structure of the
 * GeoTIFF key set
 * 
 * @author Niles D. Ritter
 */

public class GeoTIFFDirectory extends XTIFFDirectory implements java.io.Serializable {

    private TreeMap geoKeyIndex = new TreeMap();
    private int keyDirectoryVersion;
    private int majorRevision;
    private int minorRevision;
    private int numberOfKeys;
    private double[] tiepoints = null;
    private double[] scales = null;
    private double[] matrix = null;
    private boolean hasGeoKeys = false;

    /**
     * public constructor (for serializability)
     */
    public GeoTIFFDirectory() {
    }

    /**
     * Constructs a GeoTIFFDirectory by reading a SeekableStream. The ifd_offset
     * parameter specifies the stream offset from which to begin reading; this
     * mechanism is sometimes used to store private IFDs within a TIFF file that
     * are not part of the normal sequence of IFDs.
     */

    public GeoTIFFDirectory(SeekableStream stream, long ifd_offset) throws java.io.IOException {
        super(stream, ifd_offset);
        readGeoKeys();
        log("GeoTIFFDirectory constructor success.");
    }

    /**
     * Constructs a GeoTIFFDirectory from a SeekableStream. The directory
     * parameter specifies which directory to read from the linked list present
     * in the stream; directory 0 is normally read but it is possible to store
     * multiple images in a single TIFF file by maintaing multiple directories.
     */

    public GeoTIFFDirectory(SeekableStream stream, int directory) throws java.io.IOException {
        super(stream, directory);
        readGeoKeys();
        log("GeoTIFFDirectory constructor success.");
    }

    private void log(String msg) {
    }

    /**
     * Generates the TIFF fields from the GeoKey list
     */
    private void createGeoTags() {

        if (!hasGeoKeys)
            return;

        char numberOfKeys = (char) geoKeyIndex.size();
        char[] keys = new char[(numberOfKeys + 1) * 4];

        // Write the 4-entry header
        keys[0] = 1; // key version
        keys[1] = 1; // majorRevision
        keys[2] = 0; // minorRevision
        keys[3] = numberOfKeys;

        // Write the key directory out
        Iterator it = geoKeyIndex.values().iterator();
        double[] doubles = new double[numberOfKeys];
        String strings = "";
        int indx = 4;
        char numDoubles = 0;
        char tag = 0;
        char valueOrOffset = 0;
        while (it.hasNext()) {
            XTIFFField geoKey = (XTIFFField) it.next();
            switch (geoKey.getType()) {
            case XTIFFField.TIFF_SHORT:
                // short values are stored in the valueOrOffset
                tag = 0;
                valueOrOffset = (char) geoKey.getAsInt(0);
                break;
            case XTIFFField.TIFF_DOUBLE:
                tag = (char) XTIFF.TIFFTAG_GEO_DOUBLE_PARAMS;
                doubles[numDoubles] = geoKey.getAsDouble(0);
                valueOrOffset = numDoubles++;
                break;
            case XTIFFField.TIFF_ASCII:
                // strings are '|' pipe delimited
                tag = (char) XTIFF.TIFFTAG_GEO_ASCII_PARAMS;
                valueOrOffset = (char) strings.length();
                strings = strings + geoKey.getAsString(0) + "|";
                break;
            } // switch
            keys[indx++] = (char) geoKey.getTag();
            keys[indx++] = tag;
            keys[indx++] = (char) geoKey.getCount();
            keys[indx++] = valueOrOffset;
        } // while

        // Add the Directory tag
        addField(XTIFF.TIFFTAG_GEO_KEY_DIRECTORY, XTIFFField.TIFF_SHORT, keys.length, keys);

        // Add the Ascii tag if needed
        if (strings.length() > 0) {
            char zero = 0;
            strings = strings + zero;
            addField(XTIFF.TIFFTAG_GEO_ASCII_PARAMS, XTIFFField.TIFF_ASCII, strings.length(), new String[] { strings });
        }

        // Add the double tag if needed
        if (numDoubles > 0) {
            double[] doubleVals = new double[numDoubles];
            for (int i = 0; i < numDoubles; i++)
                doubleVals = doubles;
            addField(XTIFF.TIFFTAG_GEO_DOUBLE_PARAMS, XTIFFField.TIFF_DOUBLE, numDoubles, doubleVals);
        }

        // set up the other values stored in tags
        if (matrix != null)
            addField(XTIFF.TIFFTAG_GEO_TRANS_MATRIX, XTIFFField.TIFF_DOUBLE, matrix.length, matrix);
        if (tiepoints != null)
            addField(XTIFF.TIFFTAG_GEO_TIEPOINTS, XTIFFField.TIFF_DOUBLE, tiepoints.length, tiepoints);
        if (scales != null)
            addField(XTIFF.TIFFTAG_GEO_PIXEL_SCALE, XTIFFField.TIFF_DOUBLE, scales.length, scales);
    }

    /**
     * stores a single geoKey in the index table, from the existing field
     * information
     */
    private void storeGeoKey(int keyID, int tiffTag, int valueCount, int valueOrOffset)
            throws java.io.IOException {
        int type = XTIFFField.TIFF_SHORT;
        Object value = null;
        if (tiffTag > 0) {
            // Values are in another tag:
            XTIFFField values = getField(tiffTag);
            if (values != null) {
                type = values.getType();
                if (type == XTIFFField.TIFF_ASCII) {
                    String svalue = values.getAsString(0).substring(valueOrOffset, valueOrOffset
                            + valueCount - 1);
                    value = new String[] { svalue };
                } else if (type == XTIFFField.TIFF_DOUBLE) {
                    // we shouldn't have valueCount != 1 here
                    double dvalue = values.getAsDouble(valueOrOffset);
                    value = new double[] { dvalue };
                }
            } else {
                throw new java.io.IOException("GeoTIFF tag not found");
            } // values tag found
        } else {
            // value is SHORT, stored in valueOrOffset
            type = XTIFFField.TIFF_SHORT;
            value = new char[] { (char) valueOrOffset };
        } // tiffTag
        addGeoKey(keyID, type, valueCount, value);
    }

    /**
     * Returns an array of XTIFFFields containing all the fields in this
     * directory. Prior to returning array, determine if there are any GeoKeys,
     * and if so, set up the corresponding GeoTIFF fields.
     */
    public XTIFFField[] getFields() {
        if (hasGeoKeys)
            createGeoTags();
        return super.getFields();
    }

    /**
     * populates the geoKeyIndex table from the values stored in the current
     * TIFF fields.
     */
    private void readGeoKeys() throws java.io.IOException {

        // read in the keys
        XTIFFField geoKeyTag = getField(XTIFF.TIFFTAG_GEO_KEY_DIRECTORY);
        if (geoKeyTag != null) {
            char[] keys = geoKeyTag.getAsChars();

            // Set up header info
            keyDirectoryVersion = keys[0]; // should be 1 forever.
            majorRevision = keys[1]; // currently 1
            minorRevision = keys[2]; // 0,1, or 2...
            numberOfKeys = keys[3];

            // Parse out keys and values
            for (int i = 4; i < keys.length; i += 4) {
                int keyID = keys[i];
                int tiffTag = keys[i + 1];
                int valueCount = keys[i + 2];
                int valueOrOffset = keys[i + 3];
                storeGeoKey(keyID, tiffTag, valueCount, valueOrOffset);
            }

        }

        // set up the values stored in tags
        // read in the data stored as real tags
        XTIFFField matrixTag = getField(XTIFF.TIFFTAG_GEO_TRANS_MATRIX);
        XTIFFField tiepointTag = getField(XTIFF.TIFFTAG_GEO_TIEPOINTS);
        XTIFFField scaleTag = getField(XTIFF.TIFFTAG_GEO_PIXEL_SCALE);
        if (tiepointTag != null) {
            tiepoints = tiepointTag.getAsDoubles();
        }
        if (scaleTag != null) {
            scales = scaleTag.getAsDoubles();
        }
        if (matrixTag != null) {
            matrix = matrixTag.getAsDoubles();
        }
    } // readGeoKeys

    /**
     * Add a geoKey to the directory
     */
    public void addGeoKey(int key, int type, int count, Object data) {
        XTIFFField geoKey = createField(key, type, count, data);
        addGeoKey(geoKey);
    }

    /**
     * Add an existing geoKey to the directory.
     */
    public void addGeoKey(XTIFFField geoKey) {
        geoKeyIndex.put(new Integer(geoKey.getTag()), geoKey);
        hasGeoKeys = true;
    }

    /**
     * Returns an array of XTIFFFields containing all the fields corresponding
     * to the GeoKeys.
     */
    public XTIFFField[] getGeoKeys() {
        XTIFFField[] keys = new XTIFFField[geoKeyIndex.size()];
        Iterator it = geoKeyIndex.values().iterator();
        int i = 0;
        while (it.hasNext()) {
            keys[i++] = (XTIFFField) it.next();
        }
        return keys;
    }

    /**
     * Indexed Accessor to the Geokeys,indexed by the key values.
     */
    public XTIFFField getGeoKey(int key) {
        return (XTIFFField) geoKeyIndex.get(new Integer(key));
    }

    /**
     * return the tiepoint tag values
     */
    public double[] getTiepoints() {
        return tiepoints;
    }

    /**
     * return the pixel scale tag values
     */
    public double[] getPixelScale() {
        return scales;
    }

    /**
     * return the transformation matrix tag values
     */
    public double[] getTransformationMatrix() {
        return matrix;
    }

    /**
     * set the tiepoint tag values
     */
    public void setTiepoints(double[] tiepoints) {
        this.tiepoints = tiepoints;
    }

    /**
     * return the pixel scale tag values
     */
    public void setPixelScale(double[] scales) {
        this.scales = scales;
    }

    /**
     * return the pixel scale tag values
     */
    public void setTransformationMatrix(double[] matrix) {
        this.matrix = matrix;
    }

    public int getKeyDirectoryVersion() {
        return keyDirectoryVersion;
    }

    public void setKeyDirectoryVersion(int keyDirectoryVersion) {
        this.keyDirectoryVersion = keyDirectoryVersion;
    }

    public int getMajorRevision() {
        return majorRevision;
    }

    public void setMajorRevision(int majorRevision) {
        this.majorRevision = majorRevision;
    }

    public int getMinorRevision() {
        return minorRevision;
    }

    public void setMinorRevision(int minorRevision) {
        this.minorRevision = minorRevision;
    }

    public int getNumberOfKeys() {
        return numberOfKeys;
    }

    public void setNumberOfKeys(int numberOfKeys) {
        this.numberOfKeys = numberOfKeys;
    }
}
