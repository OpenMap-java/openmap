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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/vpf/DescribeDB.java,v $
// $RCSfile: DescribeDB.java,v $
// $Revision: 1.4 $
// $Date: 2004/12/08 01:04:24 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.vpf;

import java.util.Hashtable;
import java.util.List;

import com.bbn.openmap.io.FormatException;

/**
 * This class will print out some basic information about a VPF
 * database.
 * 
 * <pre>
 * 
 *  Usage:
 *  java com.bbn.openmap.layer.vpf.DescribeDB /path/to/vpf/database
 *  
 * </pre>
 * 
 * It will then print out a description of the coverages for the
 * database to the command line. (no GUI)
 */

public class DescribeDB {
    /**
     * Prints a newline to System.out
     */
    public static void println() {
        System.out.println();
    }

    /**
     * Prints a string to System.out with a newline
     * 
     * @param s the string to print
     */
    public static void println(String s) {
        System.out.println(s);
    }

    /**
     * Prints two strings to System.out with a newline
     * 
     * @param s1 the string to print
     * @param s2 the string to print
     */
    public static void println(String s1, String s2) {
        println(s1 + s2);
    }

    /**
     * Prints two strings to System.out without a newline
     * 
     * @param s1 the string to print
     * @param s2 the string to print
     */
    public static void print(String s1, String s2) {
        print(s1);
        print(s2);
    }

    /**
     * Prints a string to System.out without a newline
     * 
     * @param s the string to print
     */
    public static void print(String s) {
        System.out.print(s);
    }

    /**
     * The main program. Takes path arguments, and prints the DB it
     * finds
     * 
     * @param args the paths to print
     */
    public static void main(String[] args) throws FormatException {
        for (int argsi = 0; argsi < args.length; argsi++) {
            String rootpath = args[argsi];
            LibrarySelectionTable lst = new LibrarySelectionTable(rootpath);
            println("Path to database: " + rootpath);
            println("Database Name: " + lst.getDatabaseName());
            println("Database Description: " + lst.getDatabaseDescription());
            List<String> libraries = lst.getLibraryNames();
            print("Database Libraries: ");
            for (String libraryName : libraries) {
                print(libraryName, " ");
            }
            println();
            println();
            for (String libraryName : libraries) {
                String prefix = libraryName + ":";
                printLibrary(prefix, lst.getCAT(libraryName));
                println();
            }
        }
    }

    /**
     * Prints a VPF Library
     * 
     * @param prefix lines get printed with this prefix
     * @param cat the CoverageAttributeTable (Library) to print
     */
    public static void printLibrary(String prefix, CoverageAttributeTable cat) {
        if (cat == null) {
            println(prefix, "Library doesn't exist");
            return;
        }
        println(prefix);
        String[] coverages = cat.getCoverageNames();
        println(prefix, "uses " + (cat.isTiledData() ? "tiled" : "untiled")
                + " data");
        print(prefix, "Coverage names:");
        for (int i = 0; i < coverages.length; i++) {
            print(coverages[i]);
            print(" ");
        }
        println();
        for (int i = 0; i < coverages.length; i++) {
            printCoverage(prefix + coverages[i] + ":", cat, coverages[i]);
        }
    }

    /**
     * Prints a VPF Coverage
     * 
     * @param prefix lines get printed with this prefix
     * @param covname the name of the coverage to print
     * @param cat the CoverageAttributeTable to get the Coverage from
     */
    public static void printCoverage(String prefix, CoverageAttributeTable cat,
                                     String covname) {
        println(prefix, "Coverage Description: "
                + cat.getCoverageDescription(covname));
        println(prefix, "Coverage Topology Level: "
                + cat.getCoverageTopologyLevel(covname));
        CoverageTable ct = cat.getCoverageTable(covname);
        print(prefix, "FeatureClassNames: ");
        println();
        Hashtable<String, CoverageTable.FeatureClassRec> info = ct.getFeatureTypeInfo();
        for (CoverageTable.FeatureClassRec fcr : info.values()) {

            String tstring = "[unknown] ";
            if (fcr.type == CoverageTable.TEXT_FEATURETYPE) {
                tstring = "[text feature] ";
            } else if (fcr.type == CoverageTable.EDGE_FEATURETYPE) {
                tstring = "[edge feature] ";
            } else if (fcr.type == CoverageTable.AREA_FEATURETYPE) {
                tstring = "[area feature] ";
            } else if (fcr.type == CoverageTable.UPOINT_FEATURETYPE) {
                FeatureClassInfo fci = ct.getFeatureClassInfo(fcr.feature_class);
                if (fci == null) {
                    tstring = "[point feature] ";
                } else if (fci.getFeatureType() == CoverageTable.EPOINT_FEATURETYPE) {
                    tstring = "[entity point feature] ";
                } else if (fci.getFeatureType() == CoverageTable.CPOINT_FEATURETYPE) {
                    tstring = "[connected point feature] ";
                } else {
                    tstring = "[point feature] ";
                }
            } else if (fcr.type == CoverageTable.COMPLEX_FEATURETYPE) {
                tstring = "[complex feature] ";
            }
            println(prefix, fcr.feature_class + ": " + tstring
                    + fcr.description);
        }
    }
}