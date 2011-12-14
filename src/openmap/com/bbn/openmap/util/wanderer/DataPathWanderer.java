package com.bbn.openmap.util.wanderer;

import java.util.ArrayList;
import java.util.List;


/**
 * A DataPathWanderer provides a list of valid data paths found from a parent
 * directory. This class is intended to be provided by a layer to report which
 * data files or directories can be used for that layer, given some parent
 * directory or file.
 * 
 * @author dfdietrick
 */
public abstract class DataPathWanderer
      extends Wanderer
      implements WandererCallback {

   protected List<String> dataPaths;

   /**
    * Which component class, like a specific layer type, will be using the data
    * path.
    * 
    * @return Class of using component.
    */
   public abstract Class<?> getDataUserClass();

   /**
    * @return pretty name of the using component.x
    */
   public abstract String getPrettyName();

   public DataPathWanderer() {

   }

   /**
    * Returns a list of file/directory paths.
    * 
    * @return a list of file/directory paths. If null, no required data paths
    *         were found and the layer is indicating that it needs paths. If a
    *         list is returned and its empty, then the layer doesn't require a
    *         data file.
    */
   public List<String> getDataPaths() {
      return dataPaths;
   }

   /**
    * Adds a data path to the path repository. Creates the repository list if it
    * doesn't yet exist. A call with a null path will get the repository list
    * created.
    * 
    * @param path
    */
   protected void addDataPath(String path) {
      if (dataPaths == null) {
         dataPaths = new ArrayList<String>();
      }

      if (path != null) {
         dataPaths.add(path);
      }
   }

   /**
    * True if layer being described can handle more than one data path, i.e. all
    * the data paths found can be added to a single layer.
    * 
    * @return false by default
    */
   public boolean isMultiPathLayer() {
      return false;
   }

}
