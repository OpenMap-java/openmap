// **********************************************************************
//
// <copyright>
//
//  BBN Technologies, a Verizon Company
//  10 Moulton Street
//  Cambridge, MA 02138
//  (617) 873-8000
//
//  Copyright (C) BBNT Solutions LLC. All rights reserved.
//
// </copyright>
// **********************************************************************
//
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/dted/DTEDFrameUHL.java,v $
// $RCSfile: DTEDFrameUHL.java,v $
// $Revision: 1.2 $
// $Date: 2004/01/26 18:18:09 $
// $Author: dietrick $
//
// **********************************************************************
package com.bbn.openmap.layer.dted;

import com.bbn.openmap.io.*;
import com.bbn.openmap.util.Debug;

import java.io.IOException;
import java.io.FileNotFoundException;

public class DTEDFrameUHL {
    public int   abs_vert_acc = -1; // in meters
    public float lat_origin; // lower left, in degrees
    public int   lat_post_interval; // in seconds

    //UHL fields in order of appearance - filler has been left out.
    public float lon_origin; // lower left, in degrees
    public int   lon_post_interval; // in seconds
    public int    num_lat_points;
    public int    num_lon_lines;
    public String sec_code;
    public String u_ref;

    public DTEDFrameUHL(BinaryFile binFile) {
        try {
            binFile.seek(0);
            String checkUHL = binFile.readFixedLengthString(3);

            binFile.skipBytes(1);
            lon_origin = DTEDFrameUtil.stringToLon(binFile.readFixedLengthString(8));
            lat_origin = DTEDFrameUtil.stringToLat(binFile.readFixedLengthString(8));
            try {
                lon_post_interval = Integer.parseInt(binFile.readFixedLengthString(4), 10);
            } catch (NumberFormatException pExp) {
                Debug.message("dted", "DTEDFrameUHL: lon_post_interval number bad, using 0");
                lon_post_interval = 0;
            }
            try {
                lat_post_interval = Integer.parseInt(binFile.readFixedLengthString(4), 10);
            } catch (NumberFormatException pExp) {
                Debug.message("dted", "DTEDFrameUHL: lat_post_interval number bad, using 0");
                lat_post_interval = 0;
            }
            String s_abs_vert_acc = binFile.readFixedLengthString(4);

            try {
                if ((s_abs_vert_acc.indexOf("NA") == -1) && (s_abs_vert_acc.indexOf("N/A") == -1)) {
                    abs_vert_acc = Integer.parseInt(s_abs_vert_acc, 10);
                }
            } catch (NumberFormatException pExp) {
                Debug.message("dted", "DTEDFrameUHL: abs_vert_acc number bad, using 0");
                abs_vert_acc = 0;
            }

            sec_code = binFile.readFixedLengthString(3);
            u_ref = binFile.readFixedLengthString(12);
            try {
                num_lon_lines = Integer.parseInt(binFile.readFixedLengthString(4), 10);
            } catch (NumberFormatException pExp) {
                Debug.message("dted", "DTEDFrameUHL: num_lon_lines number bad, using 0");
                num_lon_lines = 0;
            }
            try {
                num_lat_points = Integer.parseInt(binFile.readFixedLengthString(4), 10);
            } catch (NumberFormatException pExp) {
                Debug.message("dted", "DTEDFrameUHL: num_lat_points number bad, using 0");
                num_lat_points = 0;
            }
        } catch (IOException e) {
            Debug.error("DTEDFrameUHL: File IO Error!\n" + e.toString());
        } catch (FormatException f) {
            Debug.error("DTEDFrameUHL: File IO Format error!\n" + f.toString());
        }
    }

    public String toString() {
        StringBuffer s = new StringBuffer();

        s.append("***UHL***" + "\n");
        s.append("  lon_origin: " + lon_origin + "\n");
        s.append("  lat_origin: " + lat_origin + "\n");
        s.append("  lon_post_interval: " + lon_post_interval + "\n");
        s.append("  lat_post_interval: " + lat_post_interval + "\n");
        s.append("  abs_vert_acc: " + abs_vert_acc + "\n");
        s.append("  sec_code: " + sec_code + "\n");
        s.append("  u_ref: " + u_ref + "\n");
        s.append("  num_lon_lines: " + num_lon_lines + "\n");
        s.append("  num_lat_points: " + num_lat_points + "\n");
        return s.toString();
    }

    public static void main(String[] args) {
        Debug.init();
        if (args.length < 1) {
            Debug.output("dtedframe_uhl:  Need a path/filename");
            System.exit(0);
        }

        Debug.output("DTEDFrameUHL: using frame " + args[0]);

        java.io.File file = new java.io.File(args[0]);

        try {
            BinaryFile binFile = new BinaryBufferedFile(file);

            //        BinaryFile binFile = new BinaryFile(file);
            DTEDFrameUHL dfu = new DTEDFrameUHL(binFile);

            Debug.output(dfu.toString());

        } catch (FileNotFoundException e) {
            Debug.error("DTEDFrameUHL: file " + args[0] + " not found");
            System.exit(-1);
        } catch (IOException e) {
            Debug.error("DTEDFrameUHL: File IO Error!\n" + e.toString());
        }
    }
}


