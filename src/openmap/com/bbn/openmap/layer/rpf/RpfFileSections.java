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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/rpf/RpfFileSections.java,v $
// $RCSfile: RpfFileSections.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:06:03 $
// $Author: dietrick $
// 
// **********************************************************************

/*
 * The meat of this code is based on source code provided by
 * The MITRE Corporation, through the browse application source
 * code.  Many thanks to Nancy Markuson who provided BBN with the
 * software, and to Theron Tock, who wrote the software, and
 * Daniel Scholten, who revised it - (c) 1994 The MITRE
 * Corporation for those parts, and used with permission.
 */

package com.bbn.openmap.layer.rpf;

import java.awt.Color;
import java.io.IOException;

import com.bbn.openmap.io.BinaryFile;
import com.bbn.openmap.io.FormatException;
import com.bbn.openmap.util.Debug;

/**
 * The class to use to get the locations of different sections of the
 * RPF files. This class will find out the section locations, and then
 * let you use it later to get to and read those sections.
 */
public class RpfFileSections {
    /* DKS switched 3 and 4 to match handbook */
    public static final int LOC_BOUNDARIES = 3;
    public static final int LOC_FRAMES = 4;
    public static final int LOC_COVERAGE = 6;
    public static final int LOC_COMPRESSION = 8;
    public static final int LOC_CLUT = 9;
    public static final int LOC_IMAGE = 10;

    /* DKS. New Location ID's for SUBsections */
    public static final int LOC_HEADER_SECTION = 128;
    public static final int LOC_LOCATION_SECTION = 129;
    public static final int LOC_COVERAGE_SECTION = 130;
    public static final int LOC_COMPRESSION_SECTION = 131;
    public static final int LOC_COMPRESSION_LOOKUP_SUBSECTION = 132;
    public static final int LOC_COMPRESSION_PARAMETER_SUBSECTION = 133;
    public static final int LOC_COLORGRAY_SECTION_SUBHEADER = 134;
    public static final int LOC_COLORMAP_SUBSECTION = 135;
    public static final int LOC_IMAGE_DESCR_SUBHEADER = 136;
    public static final int LOC_IMAGE_DISPLAY_PARAM_SUBHEADER = 137;
    public static final int LOC_MASK_SUBSECTION = 138;
    public static final int LOC_COLOR_CONVERTER_SUBSECTION = 139;
    public static final int LOC_SPATIAL_DATA_SUBSECTION = 140;
    public static final int LOC_ATTRIBUTE_SECTION_SUBHEADER = 141;
    public static final int LOC_ATTRIBUTE_SUBSECTION = 142;
    public static final int LOC_EXPLICIT_AREAL_TABLE = 143;
    public static final int LOC_RELATED_IMAGE_SECTION_SUBHEADER = 144;
    public static final int LOC_RELATED_IMAGE_SUBSECTION = 145;
    public static final int LOC_REPLACE_UPDATE_SECTION_SUBHEADER = 146;
    public static final int LOC_REPLACE_UPDATE_TABLE = 147;
    public static final int LOC_BOUNDARY_SECTION_SUBHEADER = 148;
    public static final int LOC_BOUNDARY_RECTANGLE_TABLE = 149;
    public static final int LOC_FRAME_FILE_INDEX_SUBHEADER = 150;
    public static final int LOC_FRAME_FILE_INDEX_SUBSECTION = 151;
    public static final int LOC_COLOR_TABLE_SECTION_SUBHEADER = 152;
    public static final int LOC_COLOR_TABLE_INDEX_RECORD = 153;

    // the key represents the number of records for the key
    public static final int TOC_LOCATION_KEY = 4;
    public static final int FRAME_LOCATION_KEY = 8;
    public static final int COLOR_LOCATION_KEY = 3;

    RpfLocationSection locationSection; // created in init()
    RpfLocationRecord[] locationRecords; // created as found in the
                                         // file

    protected boolean DEBUG_RPFDETAIL = false;

    public RpfFileSections() {}

    /**
     * Create the file sections object, and then go ahead and parse
     * the file section section of the RPF file. Assumes, the file
     * pointer is in the right place, at the
     * Rpfheader.locationSectionLocation.
     * 
     * @param binFile the binaryFile of the RPF frame file.
     */
    public RpfFileSections(BinaryFile binFile) {
        parse(binFile);
    }

    public void parse(BinaryFile binFile) {
        DEBUG_RPFDETAIL = Debug.debugging("rpfdetail");
        // Location section
        parseLocationSection(binFile);
    }

    public RpfLocationRecord[] getLocations(int key) {

        if (locationRecords == null)
            return null;

        RpfLocationRecord[] locations = new RpfLocationRecord[key];
        int[] ids = new int[key];

        switch (key) {
        case TOC_LOCATION_KEY:
            ids[0] = LOC_BOUNDARY_SECTION_SUBHEADER; /* 148 */
            ids[1] = LOC_BOUNDARY_RECTANGLE_TABLE; /* 149 */
            ids[2] = LOC_FRAME_FILE_INDEX_SUBHEADER; /* 150 */
            ids[3] = LOC_FRAME_FILE_INDEX_SUBSECTION; /* 151 */
            break;
        case FRAME_LOCATION_KEY:
            ids[0] = LOC_COMPRESSION_SECTION; /* 131 */
            ids[1] = LOC_IMAGE_DESCR_SUBHEADER; /* 136 */
            ids[2] = LOC_COMPRESSION_LOOKUP_SUBSECTION; /* 132 */
            ids[3] = LOC_SPATIAL_DATA_SUBSECTION; /* 140 */
            ids[4] = LOC_IMAGE_DISPLAY_PARAM_SUBHEADER; /* 137 */
            ids[5] = LOC_MASK_SUBSECTION; /* 138 */
            ids[6] = LOC_ATTRIBUTE_SECTION_SUBHEADER; /* 141 */
            ids[7] = LOC_ATTRIBUTE_SUBSECTION; /* 142 */
            break;
        case COLOR_LOCATION_KEY:
            ids[0] = LOC_COLORGRAY_SECTION_SUBHEADER; /* 134 */
            ids[1] = LOC_COLORMAP_SUBSECTION; /* 135 */
            ids[2] = LOC_COLOR_CONVERTER_SUBSECTION; /* 139 */
            break;
        default:
        }

        for (int i = 0; i < key; i++)
            locations[i] = getRpfLocationRecord(ids[i]);

        return locations;
    }

    /**
     * This function returns the location record, which provides the
     * file location for a particular section. The LOC ids are in this
     * file.
     */
    public RpfLocationRecord getRpfLocationRecord(int locationRecordId) {
        if (locationRecords != null) {
            for (int i = 0; i < locationRecords.length; i++) {
                if (locationRecordId == locationRecords[i].id) {
                    return locationRecords[i];
                }
            }
        }
        return null;
    }

    /**
     * This function returns the RpfLocationSection for the file.
     * Since we don't really know the location of the location section
     * yet, we really have to have the file pointer set to the right
     * place. This function should be called right after the RpfHeader
     * has been read.
     */
    protected boolean parseLocationSection(BinaryFile binFile) {

        locationSection = new RpfLocationSection();

        try {
            locationSection.length = (int) binFile.readShort();
            locationSection.tableOffset = (long) binFile.readInteger();
            locationSection.numberRecords = (int) binFile.readShort();
            locationSection.recordLength = (int) binFile.readShort();
            locationSection.aggregateLength = (long) binFile.readInteger();

            if (DEBUG_RPFDETAIL) {
                Debug.output(locationSection.toString());
            }

            locationRecords = new RpfLocationRecord[locationSection.numberRecords];

            // Now go find the ones we want
            for (int i = 0; i < locationSection.numberRecords; i++) {
                locationRecords[i] = new RpfLocationRecord();
                locationRecords[i].id = binFile.readShort();
                locationRecords[i].componentLength = (long) binFile.readInteger();
                locationRecords[i].componentLocation = (long) (binFile.readInteger());

                if (DEBUG_RPFDETAIL) {
                    Debug.output("** record " + i + ": "
                            + locationRecords[i].toString());
                }

            }

        } catch (IOException ioe) {
            Debug.error("RpfFileSections: IO ERROR parsing locations!\n" + ioe);
            return false;
        } catch (FormatException fe) {
            Debug.error("RpfFileSections: Format ERROR parsing locations!\n"
                    + fe);
            return false;
        }

        return true;
    }

    /**
     * Read the location and information about the coverage section.
     * The method will find out where to start reading from inside the
     * file.
     */
    public RpfCoverageSection parseCoverageSection(BinaryFile binFile) {
        RpfLocationRecord lr = getRpfLocationRecord(LOC_COVERAGE_SECTION);
        if (lr == null)
            return null;

        try {
            binFile.seek(lr.componentLocation);

            RpfCoverageSection coverage = new RpfCoverageSection();

            if (coverage.read(binFile)) {
                if (DEBUG_RPFDETAIL)
                    Debug.output(coverage.toString());
                return coverage;
            }
        } catch (IOException ioe) {
            Debug.error("RpfFileSections: IO ERROR parsing coverage!\n" + ioe);
        }
        return null;

    }

    /**
     * Read the location and information about the attribute section.
     * The method will find out where to start reading from inside the
     * file.
     */
    public RpfAttributes parseAttributes(BinaryFile binFile) {
        RpfLocationRecord lr = getRpfLocationRecord(LOC_ATTRIBUTE_SECTION_SUBHEADER);
        if (lr == null) {
            if (DEBUG_RPFDETAIL) {
                Debug.error("RpfFileSections: attribute section not found!");
            }
            return null;
        }
        RpfAttributes attributes = new RpfAttributes();
        if (attributes.read(binFile, lr.componentLocation)) {
            if (DEBUG_RPFDETAIL)
                Debug.output(attributes.toString());
            return attributes;
        }
        return null;
    }

    /**
     * This kicks back an array of OMColors, representing the
     * colortable. By going through the RpfFileSections function, the
     * file locations for the color sections are taken care of, and
     * the RpfColortable.parseColorLookUpTable function is called
     * correctly.
     */
    public Color[] parseColorSection(BinaryFile binFile,
                                       RpfColortable colortable) {
        if (locationRecords == null)
            return null;
        return colortable.parseColorLookUpTable(binFile,
                getLocations(COLOR_LOCATION_KEY));
    }

    static public class RpfCoverageSection {
        public double nwlat;
        public double nwlon;
        public double swlat;
        public double swlon;
        public double nelat;
        public double nelon;
        public double selat;
        public double selon;
        public double nsVertRes;
        public double ewHorRes;
        public double latInterval;
        public double lonInterval;

        public synchronized boolean read(BinaryFile binFile) {
            try {

                nwlat = binFile.readDouble();
                nwlon = binFile.readDouble();
                swlat = binFile.readDouble();
                swlon = binFile.readDouble();
                nelat = binFile.readDouble();
                nelon = binFile.readDouble();
                selat = binFile.readDouble();
                selon = binFile.readDouble();
                nsVertRes = binFile.readDouble();
                ewHorRes = binFile.readDouble();
                latInterval = binFile.readDouble();
                lonInterval = binFile.readDouble();

            } catch (IOException ioe) {
                Debug.error("RpfFileSections: IO ERROR parsing coverage!\n"
                        + ioe);
                return false;
            } catch (FormatException fe) {
                Debug.error("RpfFileSections: Format ERROR parsing coverage!\n"
                        + fe);
                return false;
            }
            return true;
        }

        public String toString() {
            StringBuffer s = new StringBuffer();
            s.append("Coverage Section:\n");
            s.append(" nwlat = ").append(nwlat).append("\n");
            s.append(" nwlon = ").append(nwlon).append("\n");
            s.append(" swlat = ").append(swlat).append("\n");
            s.append(" swlon = ").append(swlon).append("\n");
            s.append(" nelat = ").append(nelat).append("\n");
            s.append(" nelon = ").append(nelon).append("\n");
            s.append(" selat = ").append(selat).append("\n");
            s.append(" selon = ").append(selon).append("\n");
            s.append(" ns vertical resolution = ").append(nsVertRes).append("\n");
            s.append(" ew horizontal resolution = ").append(ewHorRes).append("\n");
            s.append(" lat interval = ").append(latInterval).append("\n");
            s.append(" lon interval = ").append(lonInterval).append("\n");
            return s.toString();
        }
    }

    static public class RpfLocationSection {
        public int length; // ushort
        public long tableOffset; // ulong
        public int numberRecords; //ushort
        public int recordLength; //ushort
        public long aggregateLength; //ulong

        public String toString() {
            StringBuffer s = new StringBuffer();
            s.append("RpfLocationSection:\n");
            s.append(" length = ").append(length).append("\n");
            s.append(" table offset = ").append(tableOffset).append("\n");
            s.append(" number of records = ").append(numberRecords).append("\n");
            s.append(" record length = ").append(recordLength).append("\n");
            s.append(" aggregate length = ").append(aggregateLength).append("\n");
            return s.toString();
        }
    }

    static public class RpfLocationRecord {
        public short id;
        public long componentLength;
        public long componentLocation;

        public String toString() {
            StringBuffer s = new StringBuffer();
            s.append("RpfLocationRecord:\n");
            s.append(" id = ").append(id).append("\n");
            s.append(" component length  = ").append(componentLength).append("\n");
            s.append(" component location = ").append(componentLocation).append("\n");
            return s.toString();
        }
    }
}