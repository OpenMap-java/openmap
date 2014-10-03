package com.bbn.openmap.layer.rpf;

import java.io.File;

import com.bbn.openmap.util.ArgParser;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.wanderer.DataPathWanderer;

/**
 * Adds RPF directories with a A.TOC file inside them to the data paths.
 * 
 * @author dfdietrick
 */
public class RpfDataPathWanderer
      extends DataPathWanderer {

   public RpfDataPathWanderer() {
      setCallback(this);
   }

   public Class<RpfLayer> getDataUserClass() {
      return RpfLayer.class;
   }

   /**
     * Management method for the wanderer, that steps through the children of
     * the directory and calls handleEntry for them.
    * 
    * @param directory the directory to handle
    * @param contentNames an array of Strings representing children of the
    *        directory
    * @return true if the wandering should continue.
    * @throws SecurityException
    */
   protected boolean handleDirectory(File directory, String[] contentNames)
         throws SecurityException {

      if (directory.getName().equalsIgnoreCase("RPF")) {
         boolean foundRPFDir = false;
         for (String childName : contentNames) {
            if (childName.equalsIgnoreCase("A.TOC")) {
               foundRPFDir = true;
               break;
            }
         }

         if (foundRPFDir) {
            addDataPath(directory.getAbsolutePath());
                // This stops the search from continuing on down in this RPF
                // directory.
            return true;
         }
      }

      return super.handleDirectory(directory, contentNames);
   }

   /**
    * NOOP, handle things in the overridden handleDirectory method, more
    * efficient.
    */
   public boolean handleDirectory(File directory) {
      return true;
   }

   /**
    * NOOP, work done in handleDirectory method.
    */
   public boolean handleFile(File file) {
      return true;
   }

   @Override
   public String getPrettyName() {
      return "RPF Layer";
   }

   @Override
   public boolean isMultiPathLayer() {
      return true;
   }

   /**
     * Given a set of files or directories, search them to find the parent RPF
     * directories to use for an RPF layer.
    * 
    * @param argv paths to files or directories, use -h to get a usage
    *        statement.
    */
   public static void main(String[] argv) {
      Debug.init();

        ArgParser ap = new ArgParser("RpfDataPathWanderer");

      if (argv.length == 0) {
         ap.bail("", true);
      }

      String[] dirs = argv;

      RpfDataPathWanderer wanderer = new RpfDataPathWanderer();

      // Assume that the arguments are paths to directories or
      // files.
      for (int i = 0; i < dirs.length; i++) {
         wanderer.handleEntry(new File(dirs[i]));
      }

      for (String path : wanderer.getDataPaths()) {
         System.out.println("found: " + path);
      }
   }

}
