// **********************************************************************
//
// <copyright>
//
//  Integrity Applications Incorporated
//  5180 Parkstone Dr.
//  Chantilly, VA 20151
//  (703) 378 8672
//
//  Copyright (C) Integrity Applications Incorporated. All rights reserved.
//
// </copyright>
// **********************************************************************
/* Author: Matt Revelle
 Description: Class to handle ACC records in DTED.
 History: Created on 2004.07.18. (based on DTEDFrameUHL.java)
 */

package com.bbn.openmap.dataAccess.dted;

import java.io.FileNotFoundException;
import java.io.IOException;

import com.bbn.openmap.io.BinaryBufferedFile;
import com.bbn.openmap.io.BinaryFile;
import com.bbn.openmap.io.FormatException;
import com.bbn.openmap.util.Debug;

public class DTEDFrameACC {
    public int abs_horz_acc = -1; // in meters
    public int abs_vert_acc = -1; // in meters
    public int rel_horz_acc = -1; // in meters
    public int rel_vert_acc = -1; // in meters

    // Ignore all reserved fields and accuracy of sub regions.

    public DTEDFrameACC(BinaryFile binFile) {
        try {
            binFile.seek(DTEDFrame.UHL_SIZE + DTEDFrame.DSI_SIZE);
            /* String checkACC = */binFile.readFixedLengthString(3);

            String s_abs_horz_acc = binFile.readFixedLengthString(4);
            try {
                if ((s_abs_horz_acc.indexOf("NA") == -1)
                        && (s_abs_horz_acc.indexOf("N/A") == -1)) {
                    abs_horz_acc = Integer.parseInt(s_abs_horz_acc, 10);
                }
            } catch (NumberFormatException pExp) {
                Debug.message("dted",
                        "DTEDFrameACC: abs_horz_acc number bad, using 0");
                abs_horz_acc = 0;
            }

            String s_abs_vert_acc = binFile.readFixedLengthString(4);
            try {
                if ((s_abs_vert_acc.indexOf("NA") == -1)
                        && (s_abs_vert_acc.indexOf("N/A") == -1)) {
                    abs_vert_acc = Integer.parseInt(s_abs_vert_acc, 10);
                }
            } catch (NumberFormatException pExp) {
                Debug.message("dted",
                        "DTEDFrameACC: abs_vert_acc number bad, using 0");
                abs_vert_acc = 0;
            }

            String s_rel_horz_acc = binFile.readFixedLengthString(4);
            try {
                if ((s_rel_horz_acc.indexOf("NA") == -1)
                        && (s_rel_horz_acc.indexOf("N/A") == -1)) {
                    rel_horz_acc = Integer.parseInt(s_rel_horz_acc, 10);
                }
            } catch (NumberFormatException pExp) {
                Debug.message("dted",
                        "DTEDFrameACC: rel_horz_acc number bad, using 0");
                rel_horz_acc = 0;
            }

            String s_rel_vert_acc = binFile.readFixedLengthString(4);
            try {
                if ((s_rel_vert_acc.indexOf("NA") == -1)
                        && (s_rel_vert_acc.indexOf("N/A") == -1)) {
                    rel_vert_acc = Integer.parseInt(s_rel_vert_acc, 10);
                }
            } catch (NumberFormatException pExp) {
                Debug.message("dted",
                        "DTEDFrameACC: rel_vert_acc number bad, using 0");
                rel_vert_acc = 0;
            }

        } catch (IOException e) {
            Debug.error("DTEDFrameACC: File IO Error!\n" + e.toString());
        } catch (FormatException f) {
            Debug.error("DTEDFrameACC: File IO Format error!\n" + f.toString());
        }
    }

    public String toString() {
        StringBuffer s = new StringBuffer();

        s.append("***ACC***").append("\n");
        s.append("  abs_horz_acc: ").append(abs_horz_acc).append("\n");
        s.append("  abs_vert_acc: ").append(abs_vert_acc).append("\n");
        s.append("  rel_horz_acc: ").append(rel_horz_acc).append("\n");
        s.append("  rel_vert_acc: ").append(rel_vert_acc).append("\n");
        return s.toString();
    }

    public static void main(String[] args) {
        Debug.init();
        if (args.length < 1) {
            Debug.output("dtedframe_acc:  Need a path/filename");
            System.exit(0);
        }

        Debug.output("DTEDFrameACC: using frame " + args[0]);

        java.io.File file = new java.io.File(args[0]);

        try {
            BinaryFile binFile = new BinaryBufferedFile(file);

            // BinaryFile binFile = new BinaryFile(file);
            DTEDFrameACC dfa = new DTEDFrameACC(binFile);

            Debug.output(dfa.toString());

        } catch (FileNotFoundException e) {
            Debug.error("DTEDFrameACC: file " + args[0] + " not found");
            System.exit(-1);
        } catch (IOException e) {
            Debug.error("DTEDFrameACC: File IO Error!\n" + e.toString());
        }
    }
}
