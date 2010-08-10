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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/vpf/Server.java,v $
// $Revision: 1.4 $ $Date: 2004/10/14 18:06:09 $ $Author: dietrick $
// **********************************************************************

package com.bbn.openmap.layer.vpf;

import java.util.ArrayList;
import java.util.List;

import com.bbn.openmap.util.Debug;

/**
 * Poorly named, this class just parses VPF format files and dumps them to
 * System.out(). If you want output to come from the applicable objects that
 * parse the format files, and you only want that output to come when this
 * Server is run, use the Debug flag "vpfserver" in those object classes.
 */
public class Server {

   /**
    * Just a test main to parse vpf datafiles
    * 
    * @param args files to parse, plus other command line flags
    */
   public static void main(String[] args) {
      Debug.init(System.getProperties());
      boolean schemaonly = false;
      boolean printall = false;
      boolean parseall = false;

      System.out.println("This class just decodes and print VPF files.");
      System.out.println("use the DcwSpecialist class to run the specialist");

      Debug.put("vpfserver");

      if (Debug.debugging("vpf")) {
         Debug.output("This file doesn't have debugging info.");
      }

      for (int i = 0; i < args.length; i++) {
         System.out.println(args[i]);
         if (args[i].equals("-schemaOnly")) {
            schemaonly = !schemaonly;
         } else if (args[i].equals("-printAll")) {
            printall = !printall;
         } else if (args[i].equals("-parseAll")) {
            parseall = !parseall;
         } else if (args[i].equals("-help")) {
            System.out.println(" -schemaOnly -printAll -parseAll -help [files]");
         }
         if (args[i].startsWith("-")) {
            continue;
         }
         try {
            String f = args[i];
            if (args[i].endsWith("x") || args[i].endsWith("x.")) {
               System.out.println("Skipping VLI format");
            } else if (args[i].endsWith("ti")) {
               System.out.println("Trying Thematic Index format");
               DcwThematicIndex ff = new DcwThematicIndex(f, false);
               ff.close();
            } else if (args[i].endsWith("si") || args[i].endsWith("si.")) {
               System.out.println("Trying Spatial Index format");
               DcwSpatialIndex ff = new DcwSpatialIndex(f, false);
               ff.close();
            } else if (args[i].endsWith(".doc")) {
               DcwRecordFile foo = new DcwRecordFile(f);
               String colname[] = {
                  "text"
               };
               char tschema[] = {
                  'T'
               };
               int lschema[] = {
                  -1
               };
               int cols[];
               try {
                  cols = foo.lookupSchema(colname, true, tschema, lschema, false);
               } catch (com.bbn.openmap.io.FormatException e) {
                  foo.printSchema();
                  throw e;
               }

               for (List<Object> l = new ArrayList<Object>(); foo.parseRow(l);) {
                  System.out.println(l.get(cols[0]));
               }
               System.out.println();
               foo.close();
            } else {
               DcwRecordFile foo = new DcwRecordFile(f);
               foo.printSchema();
               if (!schemaonly) {
                  if (printall) {

                     for (List<Object> l = new ArrayList<Object>(); foo.parseRow(l);) {
                        System.out.println(VPFUtil.listToString(l));
                     }
                  } else if (parseall) {
                     foo.parseAllRowsAndPrintSome();
                  } else {
                     foo.parseSomeRowsAndPrint();
                  }
               }
               foo.close();
            }
         } catch (com.bbn.openmap.io.FormatException f) {
            System.err.println("****************************************");
            System.err.println("*--------------------------------------*");
            System.err.println("Format error in dealing with " + args[i]);
            System.err.println(f.getMessage());
            System.err.println("*--------------------------------------*");
            System.err.println("****************************************");
         }
      }
   }
}