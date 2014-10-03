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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/util/wanderer/OneWaySync.java,v $
// $RCSfile: OneWaySync.java,v $
// $Revision: 1.3 $
// $Date: 2005/08/09 18:41:09 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.util.wanderer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;

import com.bbn.openmap.util.ArgParser;
import com.bbn.openmap.util.Debug;

/**
 * The OneWaySync is a class that copies files from one directory to another,
 * skipping specified extensions or only copying files and directories with
 * specified extensions. It's used by the OpenMap team to keep the internal CVS
 * tree in sync with the external one. The main() function has the avoid/limit
 * suffixes hard-coded, you can extend or change the settings in a different
 * class.
 */
public class OneWaySync
      extends Wanderer
      implements WandererCallback {

   /** The source directory. */
   protected File src;
   /** The target directory. */
   protected File tgt;
   /** The suffixes to skip over for directories. */
   public String[] dirSuffixAvoids = null;
   /** The suffixes to skip over for files. */
   public String[] fileSuffixAvoids = null;
   /** The suffixes to limit copying to for directories. */
   public String[] dirSuffixLimits = null;
   /** The suffixes to limit copying to for files. */
   public String[] fileSuffixLimits = null;
   /** The list of stuff skipped over. */
   protected LinkedList<File> notCopiedList = new LinkedList<File>();
   /** Flag for printing out activities. */
   protected boolean verbose = false;
   /** Flag for not doing the changes, just saying what would happen. */
   protected boolean fakeit = false;
   /** Flag to not have files that exist overwritten. */
   protected boolean overwrite = true;

   public OneWaySync(String srcDirName, String targetDirName) {
      super();
      setCallback(this);

      src = new File(srcDirName);
      tgt = new File(targetDirName);
   }

   /**
    * Check to see if a source directory name should be skipped, based on the
    * avoid and limit list.
    */
   protected boolean checkToSkipDirectory(String name) {
      if (dirSuffixAvoids != null) {
         for (int i = 0; i < dirSuffixAvoids.length; i++) {
            if (name.endsWith(dirSuffixAvoids[i])) {
               // Was on avoid list, skip it.
               return true;
            }
         }
      }

      if (dirSuffixLimits != null) {
         for (int i = 0; i < dirSuffixLimits.length; i++) {
            if (name.endsWith(dirSuffixLimits[i])) {
               return false;
            }
         }
         // Wasn't on limit list, skip it.
         return true;
      }

      return false;
   }

   /**
    * Check to see if a source file name should be skipped, based on the avoid
    * and limit list.
    */
   protected boolean checkToSkipFile(String name) {
      if (fileSuffixAvoids != null) {
         for (int i = 0; i < fileSuffixAvoids.length; i++) {
            if (name.endsWith(fileSuffixAvoids[i])) {
               // Was on avoid list, skip it.
               return true;
            }
         }
      }

      if (fileSuffixLimits != null) {
         for (int i = 0; i < fileSuffixLimits.length; i++) {
            if (name.endsWith(fileSuffixLimits[i])) {
               return false;
            }
         }
         // Wasn't on limit list, skip it.
         return true;
      }

      return false;
   }

   /**
    * Wanderer method handing directories.
    */
   public boolean handleDirectory(File directory, String[] contentNames) {
      String newDirName = getRelativePathFromSource(directory);

      if (newDirName == null) {
         if (directory != src) {
            notCopiedList.add(directory);
         }
         super.handleDirectory(directory, contentNames);
         return true;
      }

      if (!checkToSkipDirectory(newDirName)) {
         File newDir = getTargetFile(newDirName);
         if (!newDir.exists()) {
            if (verbose)
               Debug.output("Creating " + newDir);
            if (!fakeit && overwrite)
               newDir.mkdir();
         }
         super.handleDirectory(directory, contentNames);
      } else {
         notCopiedList.add(directory);
      }

      return true;
   }

   /**
    * WandererCallback method handing directories, not used.
    */
   public boolean handleDirectory(File file) {
      return true;
   }

   /**
    * WandererCallback method handing files, check and copy those that fit the
    * avoid and limit parameters.
    */
   public boolean handleFile(File file) {
      String newFileName = getRelativePathFromSource(file);

      if (!checkToSkipFile(newFileName)) {
         File newFile = getTargetFile(newFileName);
         if (verbose)
            Debug.output("Copying " + file + " to " + newFile);
         if (!fakeit && overwrite)
            copy(file, newFile);
      } else {
         notCopiedList.add(file);
      }
      return true;
   }

   /**
    * Copy files.
    */
   public void copy(File fromFile, File toFile) {
      try {
         FileInputStream fis = new FileInputStream(fromFile);
         FileOutputStream fos = new FileOutputStream(toFile);

         int num = 0;
         byte[] stuff = new byte[4096];
         while ((num = fis.read(stuff)) > 0) {
            fos.write(stuff, 0, num);
         }
         fis.close();
         fos.close();

      } catch (IOException ioe) {
         Debug.error("Exception reading from " + fromFile + " and writing to " + toFile);
      }
   }

   /**
    * Strip the source directory part of the path from the file, return what
    * remains.
    */
   public String getRelativePathFromSource(File file) {
      return subtractPathFromDirectory(src, file);
   }

   /**
    * Strip the target directory part of the path from the file, return what
    * remains.
    */
   public String getRelativePathFromTarget(File file) {
      return subtractPathFromDirectory(tgt, file);
   }

   /**
    * Tack the file path onto the source directory.
    */
   public File getSourceFile(String relativePath) {
      return new File(src, relativePath);
   }

   /**
    * Tack the file path onto the target directory.
    */
   public File getTargetFile(String relativePath) {
      return new File(tgt, relativePath);
   }

   /**
    * Print out the files/directories not copied.
    */
   public void writeUnsynched() {
      for (Iterator<File> it = notCopiedList.iterator(); it.hasNext();) {
         Debug.output("  " + it.next());
      }
   }

   /**
    * Create a BackCheck object that looks to see what files are in the target
    * but not in the source.
    */
   public void checkTargetSolos() {
      new BackCheck(tgt.getPath(), src.getPath());
   }

   /**
    * Take the source directory out of the path to the directory.
    */
   protected String subtractPathFromDirectory(File dir, File file) {
      String name = file.getPath();
      String dirName = dir.getPath();

      if (name.equals(dirName)) {
         if (verbose) {
            Debug.output("OneWaySync avoiding subtraction operation on top-level directory");
         }
         return null;
      }

      int index = name.indexOf(dirName);
      if (index != -1) {
         try {
            String relative = name.substring(index + dirName.length() + 1);
            if (Debug.debugging("sync")) {
               Debug.output("From " + file + ", returning " + relative);
            }
            return relative;
         } catch (StringIndexOutOfBoundsException sioobe) {
            Debug.output("Problem clipping first " + (dirName.length() + 1) + " characters off " + file);
            return null;
         }
      } else {
         Debug.error("File " + file + " is not in directory " + dir);
         return null;
      }
   }

   /**
    * Start copying files from the source directory to the target directory.
    */
   public void start() {
      String errorMessage = null;
      if (src == null) {
         errorMessage = "OneWaySync:  Source directory unspecified";
      } else if (!src.exists()) {
         errorMessage = "OneWaySync:  Source directory (" + src + ") doesn't exist!";
      }

      if (tgt != null) {
         if (!tgt.exists()) {
            if (verbose) {
               Debug.output("OneWaySync:  target directory (" + tgt + ") doesn't exist, creating...");
            }

            try {
               if (!fakeit && !tgt.mkdir()) {
                  errorMessage = "OneWaySync:  target directory (" + tgt + ") can't be created.";
               }
            } catch (SecurityException se) {
               errorMessage =
                     "OneWaySync:  creating target directory (" + tgt + ") isn't allowed, Security Exception: " + se.getMessage();
               se.printStackTrace();
            }
         }
      } else {
         errorMessage = "OneWaySync:  target directory unspecified";
      }

      if (errorMessage != null) {
         Debug.error(errorMessage);
         System.exit(0);
      }

      handleEntry(src);
   }

   public void setVerbose(boolean val) {
      verbose = val;
   }

   public boolean getVerbose() {
      return verbose;
   }

   public void setFakeit(boolean val) {
      fakeit = val;
   }

   public boolean getFakeit() {
      return fakeit;
   }

   public void setDirSuffixAvoids(String[] avoids) {
      dirSuffixAvoids = avoids;
   }

   public void setFileSuffixAvoids(String[] avoids) {
      fileSuffixAvoids = avoids;
   }

   public void setDirSuffixLimits(String[] limits) {
      dirSuffixLimits = limits;
   }

   public void setFileSuffixLimits(String[] limits) {
      fileSuffixLimits = limits;
   }

   /**
     */
   public static void main(String[] argv) {
      Debug.init();

      ArgParser ap = new ArgParser("OneWaySync");
      ap.add("source", "The source directory to copy files and directories from.", 1);
      ap.add("target", "The target directory to receive the updated files and directories.", 1);
      ap.add("verbose", "Announce all changes, failures will still be reported.");
      ap.add("fakeit", "Just print what would happen, don't really do anything.");
      ap.add("report", "Print out what didn't get copied, and what files exist only on the target side.");

      if (argv.length < 4) {
         ap.bail("", true);
      }

      ap.parse(argv);

      boolean verbose = false;
      String[] verb = ap.getArgValues("verbose");
      if (verb != null) {
         verbose = true;
      }

      boolean fakeit = false;
      verb = ap.getArgValues("fakeit");
      if (verb != null) {
         verbose = true;
         fakeit = true;
      }

      boolean report = false;
      verb = ap.getArgValues("report");
      if (verb != null) {
         report = true;
      }

      String[] sourceDir;
      sourceDir = ap.getArgValues("source");
      if (sourceDir != null && sourceDir.length >= 1) {
         if (verbose)
            Debug.output("Source directory is " + sourceDir[0]);
      } else {
         ap.bail("OneWaySync needs path to source directory", false);
      }

      String[] targetDir;
      targetDir = ap.getArgValues("target");
      if (targetDir != null && targetDir.length >= 1) {
         if (verbose)
            Debug.output("Target directory is " + targetDir[0]);
      } else {
         ap.bail("OneWaySync needs path to source directory", false);
      }

      // Should be 'since' instead of 'if'
      if (sourceDir != null && targetDir != null) {

         OneWaySync cc = new OneWaySync(sourceDir[0], targetDir[0]);
         cc.setVerbose(verbose);
         cc.setFakeit(fakeit);
         cc.setDirSuffixAvoids(new String[] {
            "CVS"
         });
         cc.setFileSuffixLimits(new String[] {
            ".java",
            "Makefile",
            ".cvsignore",
            ".html",
            ".properties",
            ".txt",
            ".c",
            ".h", ".png"
         });
         cc.start();
         if (report) {
            Debug.output("-------- Not Copied --------");
            cc.writeUnsynched();
            Debug.output("----------------------------");
            cc.checkTargetSolos();
         }
      }
   }

   public static class BackCheck
         extends OneWaySync {

      public BackCheck(String targetDirName, String srcDirName) {
         super(targetDirName, srcDirName);
         fakeit = true;
         overwrite = false;
         if (Debug.debugging("sync")) {
            verbose = true;
         }
         start();

         Debug.output("-------- Only In Target Directory--------");
         writeUnsynched();
         Debug.output("-----------------------------------------");
      }

      public boolean handleDirectory(File directory, String[] contentNames) {
         String newDirName = getRelativePathFromSource(directory);
         if (newDirName == null) {
            return super.handleDirectory(directory, contentNames);
         }

         File newDir = getTargetFile(newDirName);
         if (!newDir.exists()) {
            notCopiedList.add(directory);
         }
         return super.handleDirectory(directory, contentNames);
      }

      public boolean handleFile(File file) {
         if (!getTargetFile(getRelativePathFromSource(file)).exists()) {
            notCopiedList.add(file);
         }
         return true;
      }
   }
}