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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/shape/ShapeFileCrop.java,v $
// $RCSfile: ShapeFileCrop.java,v $
// $Revision: 1.5 $
// $Date: 2005/12/09 21:09:10 $
// $Author: dietrick $
// 
// **********************************************************************

/* ShapeFileCrop class - written by Eliot T. Lebsack of the MITRE Corp. 10/16/2002. */

package com.bbn.openmap.layer.shape;

import java.io.IOException;

import com.bbn.openmap.dataAccess.shape.ShapeUtils;

/**
 * Class that supports cropping of ESRI Shapefiles with a simple bounding box.
 * Does not yet update the .shx or .dbf files.
 * 
 * <p>
 * <H2>Usage:</H2>
 * <DT>java com.bbn.openmap.layer.shape.ShapeFileCrop -ul lat,lon -lr lat,lon -i
 * srcShapeFile -o destShapeFile</DT>
 * <DD>Crops the srcShapeFile, dumps the output into destShapeFile.</DD>
 * <DD>Note that this does simple rejection of entities based on their bounding
 * boxes.</DD>
 * <DD>A better scheme (unimplemented) would be to actually crop the line
 * segments.</DD>
 * <p>
 * 
 * @author Eliot Lebsack <elebsack@mitre.org>
 * @version $Revision: 1.5 $ $Date: 2005/12/09 21:09:10 $
 */
public class ShapeFileCrop {

    /** Input ShapeFile object. */
    public ShapeFile sfin = null;

    /** Output ShapeFile object. */
    public ShapeFile sfout = null;

    /** Bounding Box Object used for cropping */
    ESRIBoundingBox ebb = null;

    /**
     * Construct a <code>ShapeFileCrop</code> object from a pair of file names.
     * 
     * @exception IOException
     *                if something goes wrong opening or reading the file.
     */
    public ShapeFileCrop(String namein, String nameout) throws IOException {
        sfin = new ShapeFile(namein);
        sfout = new ShapeFile(nameout);
    }

    /**
     * Read the input <code>ShapeFile</code> object, and apply cropping rules to
     * the read entities. Writes the output <code>ShapeFile</code> object, and
     * then invokes the <code>ShapeFile</code> .verify method to fix the output
     * file header.
     * 
     * @exception IOException
     *                if something goes wrong opening or reading the file.
     */
    public void cropShapeFile() throws IOException {
        ESRIPolygonRecord pr;
        int nRecordNum = 0;
        int nRecords = 0;

        sfin.readHeader();

        sfout.setShapeType(sfin.fileShapeType);

        switch (sfin.fileShapeType) {
        case (ShapeUtils.SHAPE_TYPE_ARC):
        case (ShapeUtils.SHAPE_TYPE_POLYGON):
            while ((pr = (ESRIPolygonRecord) sfin.getNextRecord()) != null) {
                nRecords++;
                if (overlapBBTest(pr.bounds) != 0) {
                    pr.recordNumber = nRecordNum + 1;
                    sfout.add(pr);
                    nRecordNum++;
                    nRecords++;
                }
            }
        }

        System.out.println("Number of input records = " + (nRecords + 1));
        System.out.println("Number of candidate records = " + nRecordNum);

        if (nRecordNum > 0)
            sfout.verify(true, true);

        sfin.close();
        sfout.close();
    }

    private int overlapBBTest(ESRIBoundingBox bb) {
        int result = 0;

        result += boundaryTest(bb.min.x, bb.min.y);
        result += boundaryTest(bb.max.x, bb.min.y);
        result += boundaryTest(bb.min.x, bb.max.y);
        result += boundaryTest(bb.max.x, bb.max.y);

        return result;
    }

    private int boundaryTest(double x, double y) {
        int ns = 0;
        int ew = 0;

        if ((x >= ebb.min.x) && (x < ebb.max.x))
            ew++;

        if ((y >= ebb.min.y) && (y < ebb.max.y))
            ns++;

        return ns * ew;
    }

    public static void usage() {
        System.out
                .println("Usage: java ShapeFileCrop [args] -i <infile.shp> -o <outfile.shp>");
        System.out.println("Arguments:");
        System.out
                .println("-ul lat,lon     Coordinates of upper-left corner of the bounding box to use for cropping");
        System.out
                .println("-lr lat,lon     Coordinates of lower-right corner of the bounding box to use for cropping");
        System.exit(1);
    }

    /**
     * The driver for the command line interface. Reads the command line
     * arguments and executes appropriate calls.
     * <p>
     * See the file documentation for usage.
     * 
     * @param args
     *            the command line arguments
     * @exception IOException
     *                if something goes wrong reading or writing the file
     */

    public static void main(String[] args) throws IOException {
        String inpath = "";
        String outpath = "";
        String sllp;
        String[] sllpa;

        ShapeFileCrop sfc = null;

        ESRIPoint ul = null;
        ESRIPoint lr = null;
        int index = 0;

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-i")) {
                inpath = args[++i];
            } else if (args[i].equals("-o")) {
                outpath = args[++i];
            } else if (args[i].equals("-ul")) {
                sllp = args[++i];
                // sllpa = sllp.split(","); // jdk 1.4
                index = sllp.indexOf(",");

                if (index != -1) {
                    sllpa = new String[2];
                    sllpa[0] = sllp.substring(0, index);
                    sllpa[1] = sllp.substring(index + 1);
                    ul = new ESRIPoint(Double.valueOf(sllpa[1]).doubleValue(),
                            Double.valueOf(sllpa[0]).doubleValue());
                }

            } else if (args[i].equals("-lr")) {
                sllp = args[++i];
                // sllpa = sllp.split(","); // jdk 1.4
                index = sllp.indexOf(",");
                if (index != -1) {
                    sllpa = new String[2];
                    sllpa[0] = sllp.substring(0, index);
                    sllpa[1] = sllp.substring(index + 1);
                    lr = new ESRIPoint(Double.valueOf(sllpa[1]).doubleValue(),
                            Double.valueOf(sllpa[0]).doubleValue());
                }

            } else {
                usage();
            }
        }

        if ((ul == null) || (lr == null) || (inpath.length() == 0)
                || (outpath.length() == 0)) {
            usage();
        } else {
            sfc = new ShapeFileCrop(inpath, outpath);
            sfc.ebb = new ESRIBoundingBox(ul, lr);
        }

        if (sfc != null) {
            sfc.cropShapeFile();
        }
    }
}