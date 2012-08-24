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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/rpf/RpfTocEntry.java,v $
// $RCSfile: RpfTocEntry.java,v $
// $Revision: 1.7 $
// $Date: 2006/12/13 16:45:25 $
// $Author: dietrick $
// 
// **********************************************************************

/*
 * The meat of this code is based on source code provided by
 * The MITRE Corporation, through the browse application source
 * code.  Many thanks to Nancy Markuson who provided BBN with the
 * software, and to Theron Tock, who wrote the software, and
 * Daniel Scholten, who revised it - (c) 1994 The MITRE
 * Corporation for those parts, and used/distributed with permission.
 */

package com.bbn.openmap.layer.rpf;

import com.bbn.openmap.io.BinaryFile;
import com.bbn.openmap.io.FormatException;

/**
 * Reads and holds an entry in the RPF table of contents file.
 */
public class RpfTocEntry {

    /** Degrees/pixel. */
    public double vertInterval, horizInterval;
    /** meters/pixel. */
    public double vertResolution, horizResolution;
    public int horizFrames, vertFrames;
    public RpfFrameEntry[][] frames;
    public char zone; /* DKS new 7/94 */
    public char version; /* 1-9: DKS new 5/3/95 for Dchum */
    public boolean Cib; /* Cib vs. cadrg flag */
    public String compressionRatio;
    public String producer;
    public String scale;
    public RpfCoverageBox coverage;
    /**
     * Not determined at readtime. RpfTocHandler sets this later, when
     * the frames are evaluated for their existance.
     */
    public RpfProductInfo info;

    public RpfTocEntry(BinaryFile binFile, int entryNumber)
            throws java.io.EOFException, FormatException {
        this(binFile, 0, entryNumber);
    }

    public RpfTocEntry(BinaryFile binFile, int TOCNumber, int entryNumber)
            throws java.io.EOFException, FormatException {
        coverage = new RpfCoverageBox();
        coverage.tocNumber = TOCNumber;
        coverage.entryNumber = entryNumber;
        read(binFile);

        // Figure out the CADRG projection zone for the coverage.
        coverage.zone = com.bbn.openmap.proj.CADRG.getProjZone(zone);
    }

    public void setInfo(String seriesCode) {
        info = (RpfProductInfo) RpfProductInfo.getCatalog().get(seriesCode.toUpperCase());
        if (info != null) {
            if (info.dataType.equalsIgnoreCase("CIB"))
                Cib = true;
            coverage.chartCode = info.seriesCode;
        } else {
            info = RpfConstants.UK;
        }
    }

    public void read(BinaryFile binFile) throws java.io.EOFException,
            FormatException {
        /*
         * e.g. "CADRG" , for type - deduced later, via framename of
         * entry, and using RpfProductInfo.
         */
        /*String type = */binFile.readFixedLengthString(5);
        compressionRatio = binFile.readFixedLengthString(5);
        /*
         * Same as type - deduced via RpfProductInfo. There is a float
         * scale inside the info, and a scaleString.
         */
        scale = binFile.readFixedLengthString(12);
        coverage.scale = RpfTocHandler.textScaleToLong(scale);

        zone = binFile.readChar(); /* char: 1-9 A-J */
        producer = binFile.readFixedLengthString(5);

        coverage.nw_lat = binFile.readDouble();
        coverage.nw_lon = binFile.readDouble();
        /*double sw_lat = */binFile.readDouble();
        /*double sw_lon = */binFile.readDouble();
        /*double ne_lat = */binFile.readDouble();
        /*double ne_lon = */binFile.readDouble();
        coverage.se_lat = binFile.readDouble();
        coverage.se_lon = binFile.readDouble();
        vertResolution = binFile.readDouble();
        horizResolution = binFile.readDouble();
        vertInterval = binFile.readDouble();
        horizInterval = binFile.readDouble();
        vertFrames = binFile.readInteger();
        horizFrames = binFile.readInteger();

        coverage.subframeLatInterval = vertInterval * 256.0;
        coverage.subframeLonInterval = horizInterval * 256.0;
    }
    
    protected boolean isFramesLoaded() {
        return frames != null;
    }
    
    protected RpfFrameEntry[][] getFrames() {
        if (frames == null) {
            frames = new RpfFrameEntry[vertFrames][horizFrames];

            for (int j = 0; j < vertFrames; j++) {
                for (int k = 0; k < horizFrames; k++) {
                    frames[j][k] = new RpfFrameEntry();
                }
            }
        }
        
        return frames;
    }
    
    protected RpfFrameEntry getFrame(int row, int column) {
        RpfFrameEntry[][] frames = getFrames();
        return frames[row][column];
    }

    public String toString() {
        StringBuffer s = new StringBuffer();
        s.append("RpfTocEntry ##################").append("\n");
        s.append(" vertInterval ").append(vertInterval)
                .append(", horizInterval ").append(horizInterval).append("\n");
        s.append(" vertResolution ").append(vertResolution)
                .append(", horizResolution ").append(horizResolution).append("\n");
        s.append(" horizFrames ").append(horizFrames)
                .append(", vertFrames ").append(vertFrames).append("\n");
        s.append(" zone ").append(zone).append("\n");
        s.append(" scale ").append(scale).append("\n");
        s.append(" version ").append(version).append("\n");
        s.append(" Cib ").append(Cib).append("\n");
        s.append(" compressionRatio ").append(compressionRatio).append("\n");
        s.append(" producer ").append(producer).append("\n");
        s.append(coverage);
        return s.toString();
    }
}

