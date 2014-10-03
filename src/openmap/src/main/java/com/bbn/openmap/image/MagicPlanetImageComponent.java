// **********************************************************************
//
//<copyright>
//
//BBN Technologies, a Verizon Company
//10 Moulton Street
//Cambridge, MA 02138
//(617) 873-8000
//
//Copyright (C) BBNT Solutions LLC. All rights reserved.
//
//</copyright>
//**********************************************************************
//
//$Source:
///cvs/darwars/ambush/aar/src/com/bbn/ambush/mission/MissionHandler.java,v
//$
//$RCSfile: MagicPlanetImageComponent.java,v $
//$Revision: 1.7 $
//$Date: 2006/09/15 14:10:43 $
//$Author: dietrick $
//
//**********************************************************************

package com.bbn.openmap.image;

import java.awt.Paint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Properties;

import javax.swing.JOptionPane;
import javax.swing.Timer;

import com.bbn.openmap.Environment;
import com.bbn.openmap.I18n;
import com.bbn.openmap.Layer;
import com.bbn.openmap.LayerHandler;
import com.bbn.openmap.MapBean;
import com.bbn.openmap.OMComponent;
import com.bbn.openmap.event.LayerEvent;
import com.bbn.openmap.event.LayerListener;
import com.bbn.openmap.proj.LLXY;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.proj.coords.LatLonPoint;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PropUtils;

/**
 * The MagicPlanetImageComponent is an OpenMap Component designed to create
 * images for Global Imagination's MagicPlanet Globe. This component, when added
 * to an OpenMap MapHandler, will find the LayerHandler so it can find out when
 * the Layer given to the MapBean change, so it knows which ones to use when
 * creating an image file. This component also connects to the MapBean as a
 * PropertyChangeListener to find out when the ocean color has changed. The
 * MagicPlanet software (Storyteller) has the option of displaying images stored
 * in a particular directory, either displaying the latest (lexically) image or
 * cycling through a set of images in the directory to create a movie on the
 * globe.
 * <p>
 * 
 * The class has options that change the format of the images created, where the
 * images are stored, how often they are created, and the scale of the images.
 * The scale of the image dictates its pixel size, since the proportion of the
 * projection has to be constant for it to work on the globe. The projection
 * used for the images is always the OpenMap LLXY projection, that's what the
 * MagicPlanet expects.
 * <p>
 * 
 * The properties for this component are:
 * 
 * <pre>
 *               outputDirectory=path_to_directory_for_writing_images
 *               
 *               # Milliseconds between image creation, 60000 is the default, representing 1 minute
 *               updateInterval=60000
 *               
 *               # Milliseconds after the timer is created before the MagicPlanetImageComponent takes
 *               # its first image and starts updating according to the updateInterval.  Default is 0,
 *               # so the first image is taken as soon as the component finds the layers.
 *               initialDelay=180000
 *               
 *               # The scale of the image, it determines the size of the image.  This 
 *               # may be important for certain layers to show particular details.  
 *               # The default is 60000000F, which represents an image approximately 2kx1k
 *               scale=60000000F
 *               
 *               # Property to tell the component to create a new image and reset the timer if the 
 *               # layers on the MapBean change.  True by default.
 *               autoUpdate=true
 *               
 *               # Property to tell the component to remove old images, default is true
 *               cleanup=true
 *               
 *               # Property to set the wait time before deleting old images, represented 
 *               # in milliseconds. The default is 86400000, representing one day. 
 *               cleanupInterval=86400000
 *               
 *               # Properties for setting the pixel width and height of the images. These properties 
 *               # provide a more precise way to control the image size, and tell the component to 
 *               # scale the image created with the scale setting set above.  The closer you get 
 *               # the scale to provide you the image size you want, the higher quality image 
 *               # you will have.  The default values for these properties are -1, which tells
 *               # the component to not change the size of the image resulting from the scale setting.
 *               width=-1 
 *               height=-1
 *               
 *               # Property to set the name of the last image written in a file, so other programs 
 *               # can more easily figure out what it was.  The property should reflect the path 
 *               # to the file to be written, which will contain 'MagicPlanet.lastFile=YYYYMMDDhhmmss.ext',
 *               # where YYYYMMDDhhmmss are year, month, day, hour, minute and second the file was created, 
 *               # and ext is the extension for the image type.  This information, combined with the directory 
 *               # information stored above, will let you know where the file is.  If this property is not set, 
 *               # no text file will be written.
 *               lastImageFile=path_to_text_file 
 *               
 *               # Property that describes a system command that should be run each time an image is created.
 *               # The property should contain exactly what would be typed into a command line for a script 
 *               # to be run, in the same environment this component is being run in.  There are special arguments
 *               # that can be inserted into this property string that the component will use to replace the current
 *               # image file name:
 *               #
 *               # %FILEPATH% gets replaced with the complete path of the new image file.
 *               # %FILENAME% gets replaced with the file name if the image file.
 *               # %FILENAME_WITHOUT_EXTENSION% gets replaced with the file name without a '.' or anything after that.
 *               #
 *               # The default is no value being set for the script, which means nothing will happen.  Here is an example for
 *               # creating a .dds file from the current image, using nvidia's nvdxt script.
 *               postProcessingScript=&quot;c:/Program Files/NVIDIA Corporation/NVIDIA DDS Utilities/nvdxt.exe&quot; -swap -dxt1c -file %FILEPATH% -output c:/%FILENAME_WITHOUT_EXTENSION%.dds
 * </pre>
 * 
 * @author dietrick
 */
public class MagicPlanetImageComponent extends OMComponent implements
        LayerListener, PropertyChangeListener, ActionListener {

    public final static String OutputDirectoryProperty = "outputDirectory";

    public final static String UpdateIntervalProperty = "updateInterval";

    public final static String InitialDelayProperty = "initialDelay";

    public final static String ScaleProperty = "scale";

    public final static String AutoUpdateProperty = "autoUpdate";

    public final static String CleanupProperty = "cleanup";

    public final static String CleanupIntervalProperty = "cleanupInterval";

    public final static String HeightProperty = "height";

    public final static String WidthProperty = "width";

    public final static String LastImageFileProperty = "lastImageFile";

    public final static String PostProcessingScriptProperty = "postProcessingScript";

    public final static String LAST_IMAGE_FILE_KEY = "MagicPlanet.lastFile";

    public final static String REPLACE_FILEPATH_MARKER = "%FILEPATH%";

    public final static String REPLACE_FILENAME_MARKER = "%FILENAME%";

    public final static String REPLACE_FILENAME_WOEXT_MARKER = "%FILENAME_WITHOUT_EXTENSION%";

    protected boolean DEBUG = false;

    // Kept in case replacements are added to the application, so we
    // remember who to disconnect from.
    protected LayerHandler layerHandler;

    protected MapBean mapBean;

    /**
     * Parent directory for images.
     */
    protected String outputDirectoryString;

    protected int updateInterval = 60000;

    protected int initialDelay = 0;

    protected float scale = 60000000F; // Produces 2k x 1k image

    protected Projection proj;

    protected Paint background;

    protected Layer[] layers;

    protected boolean autoUpdate = true;

    protected ImageFormatter imageFormatter = new SunJPEGFormatter();

    protected boolean cleanup = true;

    protected int cleanupInterval = 86400000; // one day

    protected int height = -1;// unscaled, go with scale

    protected int width = -1; // unscaled, go with scale

    protected String lastImageFile = null;

    protected String postProcessingScript = null;

    protected Timer timer;

    public MagicPlanetImageComponent() {
        DEBUG = Debug.debugging("magicplanet");
    }

    /**
     * MapHandlerChild method extended through the OMComponent hierarchy. This
     * is the method called by the MapHandler with objects added to the
     * MapHandler.
     */
    public void findAndInit(Object someObj) {
        if (someObj instanceof LayerHandler) {
            setLayerHandler((LayerHandler) someObj);
        }

        if (someObj instanceof MapBean) {
            setMapBean((MapBean) someObj);
        }
    }

    /**
     * MapHandlerChild method extended through the OMComponent hierarchy. This
     * is the method called by the MapHandler with objects removed from the
     * MapHandler.
     */
    public void findAndUndo(Object someObj) {
        if (someObj instanceof LayerHandler && someObj == getLayerHandler()) {
            setLayerHandler(null);
        }

        if (someObj instanceof MapBean && someObj == getMapBean()) {
            setMapBean(null);
        }
    }

    /**
     * Get the timer being used for automatic updates. May be null if a timer is
     * not set.
     */
    public Timer getTimer() {
        return timer;
    }

    /**
     * If you want the layer to update itself at certain intervals, you can set
     * the timer to do that. Set it to null to disable it. If the current timer
     * is not null, the graphic loader is removed as an ActionListener. If the
     * new one is not null, the graphic loader is added as an ActionListener.
     */
    public void setTimer(Timer t) {
        if (timer != null) {
            timer.removeActionListener(this);
            timer.stop();
        }

        timer = t;
        if (timer != null) {
            timer.addActionListener(this);
        }
    }

    /**
     * Creates a timer with the current updateInterval and calls setTimer().
     */
    public void createTimer() {
        Timer t = new Timer(updateInterval, null);
        t.setInitialDelay(initialDelay);
        setTimer(t);
    }

    /**
     * The delay between timer pulses, in milliseconds.
     */
    public void setUpdateInterval(int delay) {
        updateInterval = delay;
        if (timer != null) {
            timer.setDelay(updateInterval);
            if (timer.isRunning()) {
                timer.restart();
            }
        }
    }

    public int getUpdateInterval() {
        return updateInterval;
    }

    public int getInitialDelay() {
        return initialDelay;
    }

    public void setInitialDelay(int initialDelay) {
        this.initialDelay = initialDelay;
    }

    /*
     * Called when the timer kicks off.
     * 
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
        if (false && DEBUG) {
            Debug.output("MPIC.actionPerformed("
                    + e.getSource().getClass().getName() + ")");
        }
        createImage();
    }

    /**
     * @return the object currently known as the LayerHandler by this object.
     */
    protected LayerHandler getLayerHandler() {
        return layerHandler;
    }

    /**
     * Set the LayerHandler, become a LayerListener object to it to know when
     * the layers on the MapBean change. If there is already a LayerHandler
     * known to this component, this component will remove itself as a listener
     * to the previous LayerHandler.
     * 
     * @param lh LayerHandler.
     */
    protected void setLayerHandler(LayerHandler lh) {
        if (layerHandler != null) {
            layerHandler.removeLayerListener(this);
        }

        layerHandler = lh;

        if (layerHandler != null) {
            layerHandler.addLayerListener(this);
            // calling setLayers() will kick off an image creation.
            // Don't want that right now, just setting the layers for
            // initialization purposes, we'll let events or timer
            // create the image.
            layers = layerHandler.getMapLayers();

            Timer timer = getTimer();
            if (timer == null) {
                createTimer();
            }

        }
    }

    /**
     * @return the object currently known as the MapBean by this object.
     */
    protected MapBean getMapBean() {
        return mapBean;
    }

    /**
     * Set the MapBean, become a PropertyChangeListener object to it to know
     * when the background color on the MapBean changes. If there is already a
     * MapBean known to this component, this component will remove itself as a
     * listener to the previous MapBean.
     * 
     * @param mb MapBean.
     */
    protected void setMapBean(MapBean mb) {
        if (mapBean != null) {
            mapBean.removePropertyChangeListener(this);
        }

        mapBean = mb;

        if (mapBean != null) {
            mapBean.addPropertyChangeListener(this);
        }
    }

    public void setProperties(String prefix, Properties props) {
        super.setProperties(prefix, props);
        prefix = PropUtils.getScopedPropertyPrefix(prefix);

        setOutputDirectoryString(props.getProperty(prefix
                + OutputDirectoryProperty));
        setAutoUpdate(PropUtils.booleanFromProperties(props, prefix
                + AutoUpdateProperty, isAutoUpdate()));

        setHeight(PropUtils.intFromProperties(props,
                prefix + HeightProperty,
                getHeight()));
        setWidth(PropUtils.intFromProperties(props,
                prefix + WidthProperty,
                getWidth()));

        setScale(PropUtils.floatFromProperties(props,
                prefix + ScaleProperty,
                scale));

        setUpdateInterval(PropUtils.intFromProperties(props, prefix
                + UpdateIntervalProperty, getUpdateInterval()));

        setInitialDelay(PropUtils.intFromProperties(props, prefix
                + InitialDelayProperty, getInitialDelay()));

        setCleanup(PropUtils.booleanFromProperties(props, prefix
                + CleanupProperty, isCleanup()));
        setCleanupInterval(PropUtils.intFromProperties(props, prefix
                + CleanupIntervalProperty, getCleanupInterval()));

        setLastImageFile(props.getProperty(prefix + LastImageFileProperty,
                getLastImageFile()));

        setPostProcessingScript(props.getProperty(prefix
                + PostProcessingScriptProperty, getPostProcessingScript()));
    }

    public Properties getProperties(Properties props) {
        props = super.getProperties(props);
        String prefix = PropUtils.getScopedPropertyPrefix(this);
        props.put(prefix + OutputDirectoryProperty,
                PropUtils.unnull(getOutputDirectoryString()));
        props.put(prefix + AutoUpdateProperty, Boolean.toString(isAutoUpdate()));
        props.put(prefix + HeightProperty, Integer.toString(getHeight()));
        props.put(prefix + WidthProperty, Integer.toString(getWidth()));
        props.put(prefix + ScaleProperty, Float.toString(getScale()));
        props.put(prefix + UpdateIntervalProperty,
                Integer.toString(getUpdateInterval()));
        props.put(prefix + InitialDelayProperty,
                Integer.toString(getInitialDelay()));
        props.put(prefix + CleanupProperty, Boolean.toString(isCleanup()));
        props.put(prefix + CleanupIntervalProperty,
                Integer.toString(getCleanupInterval()));
        props.put(prefix + LastImageFileProperty,
                PropUtils.unnull(getLastImageFile()));
        props.put(prefix + PostProcessingScriptProperty,
                PropUtils.unnull(getPostProcessingScript()));

        return props;
    }

    public Properties getPropertyInfo(Properties props) {
        props = super.getPropertyInfo(props);

        String interString;

        // -------
        interString = i18n.get(MagicPlanetImageComponent.class,
                OutputDirectoryProperty,
                I18n.TOOLTIP,
                "Path to directory that holds created images.");
        props.put(OutputDirectoryProperty, interString);
        props.put(OutputDirectoryProperty + ScopedEditorProperty,
                "com.bbn.openmap.util.propertyEditor.DirectoryPropertyEditor");
        interString = i18n.get(MagicPlanetImageComponent.class,
                OutputDirectoryProperty,
                "Directory Path");
        props.put(OutputDirectoryProperty + LabelEditorProperty, interString);
        // -------
        interString = i18n.get(MagicPlanetImageComponent.class,
                AutoUpdateProperty,
                I18n.TOOLTIP,
                "Immediately create new images when the layers/background color changes.");
        props.put(AutoUpdateProperty, interString);
        props.put(AutoUpdateProperty + ScopedEditorProperty,
                "com.bbn.openmap.util.propertyEditor.YesNoPropertyEditor");
        interString = i18n.get(MagicPlanetImageComponent.class,
                AutoUpdateProperty,
                "Auto-Update");
        props.put(AutoUpdateProperty + LabelEditorProperty, interString);
        // -------
        interString = i18n.get(MagicPlanetImageComponent.class,
                HeightProperty,
                I18n.TOOLTIP,
                "Image pixel height (-1 defers to scale setting).");
        props.put(HeightProperty, interString);
        interString = i18n.get(MagicPlanetImageComponent.class,
                HeightProperty,
                "Image Height");
        props.put(HeightProperty + LabelEditorProperty, interString);
        // -------
        interString = i18n.get(MagicPlanetImageComponent.class,
                WidthProperty,
                I18n.TOOLTIP,
                "Image pixel width (-1 defers to scale setting).");
        props.put(WidthProperty, interString);
        interString = i18n.get(MagicPlanetImageComponent.class,
                WidthProperty,
                "Image Width");
        props.put(WidthProperty + LabelEditorProperty, interString);
        // -------
        interString = i18n.get(MagicPlanetImageComponent.class,
                ScaleProperty,
                I18n.TOOLTIP,
                "Scale to use for image projection (larger numbers make smaller maps).");
        props.put(ScaleProperty, interString);
        interString = i18n.get(MagicPlanetImageComponent.class,
                ScaleProperty,
                "Projection Scale");
        props.put(ScaleProperty + LabelEditorProperty, interString);
        // -------
        interString = i18n.get(MagicPlanetImageComponent.class,
                UpdateIntervalProperty,
                I18n.TOOLTIP,
                "Number of milliseconds until next image.");
        props.put(UpdateIntervalProperty, interString);
        interString = i18n.get(MagicPlanetImageComponent.class,
                UpdateIntervalProperty,
                "Update Interval");
        props.put(UpdateIntervalProperty + LabelEditorProperty, interString);
        //      -------
        interString = i18n.get(MagicPlanetImageComponent.class,
                InitialDelayProperty,
                I18n.TOOLTIP,
                "Number of milliseconds until the first image is created.");
        props.put(InitialDelayProperty, interString);
        interString = i18n.get(MagicPlanetImageComponent.class,
                InitialDelayProperty,
                "Initial Delay");
        props.put(InitialDelayProperty + LabelEditorProperty, interString);
        // -------
        interString = i18n.get(MagicPlanetImageComponent.class,
                CleanupProperty,
                I18n.TOOLTIP,
                "Delete old images automatically.");
        props.put(CleanupProperty, interString);
        props.put(CleanupProperty + ScopedEditorProperty,
                "com.bbn.openmap.util.propertyEditor.YesNoPropertyEditor");
        interString = i18n.get(MagicPlanetImageComponent.class,
                CleanupProperty,
                "Delete Old Images");
        props.put(CleanupProperty + LabelEditorProperty, interString);
        // -------
        interString = i18n.get(MagicPlanetImageComponent.class,
                CleanupIntervalProperty,
                I18n.TOOLTIP,
                "Number of milliseconds to keep old images (86400000 is one day).");
        props.put(CleanupIntervalProperty, interString);
        interString = i18n.get(MagicPlanetImageComponent.class,
                CleanupIntervalProperty,
                "Cleanup Interval");
        props.put(CleanupIntervalProperty + LabelEditorProperty, interString);
        // -------
        interString = i18n.get(MagicPlanetImageComponent.class,
                LastImageFileProperty,
                I18n.TOOLTIP,
                "Path to file containing name of last image file created.");
        props.put(LastImageFileProperty, interString);
        interString = i18n.get(MagicPlanetImageComponent.class,
                LastImageFileProperty,
                "Last Image Name");
        props.put(LastImageFileProperty + LabelEditorProperty, interString);
        // -------
        interString = i18n.get(MagicPlanetImageComponent.class,
                PostProcessingScriptProperty,
                I18n.TOOLTIP,
                "Script to run on the image file after it's been created.");
        props.put(PostProcessingScriptProperty, interString);
        interString = i18n.get(MagicPlanetImageComponent.class,
                PostProcessingScriptProperty,
                "Post Processing Script");
        props.put(PostProcessingScriptProperty + LabelEditorProperty,
                interString);

        props.put(initPropertiesProperty, OutputDirectoryProperty + " "
                + ScaleProperty + " " + InitialDelayProperty + " "
                + UpdateIntervalProperty + " " + AutoUpdateProperty + " "
                + CleanupProperty + " " + CleanupIntervalProperty + " "
                + HeightProperty + " " + WidthProperty + " "
                + LastImageFileProperty + " " + PostProcessingScriptProperty);

        return props;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.bbn.openmap.event.LayerListener#setLayers(com.bbn.openmap.event.LayerEvent)
     */
    public void setLayers(LayerEvent evt) {
        if (evt.getType() == LayerEvent.REPLACE) {
            setLayers(evt.getLayers());
        }
    }

    /**
     * Checks to see if there is a timer, and if the component wants to
     * automatically update the current image. If the timer isn't running, it's
     * started.
     */
    public void handleUpdate() {
        Timer timer = getTimer();

        if (timer != null && (isAutoUpdate() || !timer.isRunning())) {
            timer.restart();
        }
        // Else do nothing, the timer is running and will pick up the
        // changes.
    }

    /**
     * Create a new image.
     */
    public void createImage() {

        if (isCleanup()) {
            cleanup(false);
        }

        String fileName = getFileNameForTime(System.currentTimeMillis());
        String filePath = getOutputDirectoryString() + "/" + fileName;

        if (DEBUG) {
            Debug.output("MagicPlanetImageComponent: creating image: "
                    + filePath);
        }

        Layer[] layers = getLayers();
        if (layers == null) {
            return;
        }

        ImageServer is = new ImageServer(layers, new SunJPEGFormatter());
        try {
            is.setBackground(getBackground());
        } catch (NoSuchMethodError nsme) {
            // Older version of OpenMap, going to just use what the
            // MapBean has
        }
        byte[] imageBytes = is.createImage(getProj(), getWidth(), getHeight());

        FileOutputStream fos;
        try {
            fos = new FileOutputStream(filePath);
            fos.write(imageBytes);
            fos.flush();
            fos.close();

            if (DEBUG) {
                Debug.output("  MP: done writing image");
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mapBean.setProjection(mapBean.getProjection());

        String launchCmd = generatePostProcessingCmd(postProcessingScript,
                filePath);

        if (launchCmd != null) {
            try {

                if (DEBUG)
                    Debug.output("MP post processing: " + launchCmd);
                Runtime.getRuntime().exec(launchCmd);
            } catch (IOException e) {
                System.err.println("MP post processing:  " + e);
            }
        }

        if (lastImageFile != null) {
            try {
                File lastImageFileFile = new File(lastImageFile);
                fos = new FileOutputStream(lastImageFileFile);
                fos.write(new String(LAST_IMAGE_FILE_KEY + "=" + fileName).getBytes());
                fos.flush();
                fos.close();

                if (DEBUG) {
                    Debug.output("  MP: done writing file noting last image file name: "
                            + lastImageFile);
                }
            } catch (IOException ioe) {
                Debug.error("MP: error writing file to note last image file name:\n"
                        + ioe.getMessage());
                ioe.printStackTrace();
                lastImageFile = null;
            }
        }
    }

    protected String generatePostProcessingCmd(String script, String filePath) {
        String ret = null;

        if (script != null && filePath != null) {
            // nvdxt.exe -file Image.jpg -all -swap -dxt1c -output
            // Image.dds
            // nvdxt.exe -file %FILENAME% -all -swap -dxt1c -output
            // %FILENAME_WITHOUT_EXTENSION%.dds
            if (DEBUG) {
                Debug.output(" Replacing script: |" + script + "|" + filePath);
            }
            ret = script.replaceAll(REPLACE_FILEPATH_MARKER, filePath);
            ret = ret.replaceAll(REPLACE_FILENAME_MARKER,
                    filePath.substring(filePath.lastIndexOf('/') + 1));
            ret = ret.replaceAll(REPLACE_FILENAME_WOEXT_MARKER,
                    filePath.substring(filePath.lastIndexOf('/') + 1,
                            filePath.lastIndexOf('.')));

            try {
                if (Environment.get("os.name").startsWith("Windows")) {
                    ret = ret.replace('/', '\\');
                }
            } catch (NullPointerException npe) {
                // Applet, or Environment not set up.
            }

            if (DEBUG) {
                Debug.output(" returning script: " + ret);
            }
        }

        return ret;
    }

    /**
     * @param l unix time in milliseconds
     * @return String representing file name for the given time.
     */
    protected String getFileNameForTime(long l) {
        Calendar cal = new GregorianCalendar();
        cal.setTimeInMillis(l);

        DecimalFormat twoDigits = new DecimalFormat("00");

        String tMarker = Integer.toString(cal.get(Calendar.YEAR))
                + twoDigits.format(cal.get(Calendar.MONTH) + 1)
                + twoDigits.format(cal.get(Calendar.DAY_OF_MONTH))
                + twoDigits.format(cal.get(Calendar.HOUR_OF_DAY))
                + twoDigits.format(cal.get(Calendar.MINUTE))
                + twoDigits.format(cal.get(Calendar.SECOND));

        return tMarker + "."
                + getImageFormatter().getFormatLabel().toLowerCase();
    }

    /**
     * Decode the file name to see what time the file was created.
     * 
     * @param fileName
     * @return milliseconds from unix epoch.
     * @throws NumberFormatException if the filename can't be decoded.
     */
    protected long getTimeForFileName(String fileName)
            throws NumberFormatException {

        int dotIndex = fileName.indexOf(".");
        if (dotIndex == -1) {
            // Not something we care about
            throw new NumberFormatException();
        }
        fileName = fileName.substring(0, dotIndex);
        if (fileName.length() == 14) {
            // Fits our naming convention.
            int year = Integer.parseInt(fileName.substring(0, 4));
            int month = Integer.parseInt(fileName.substring(4, 6));
            int day = Integer.parseInt(fileName.substring(6, 8));
            int hour = Integer.parseInt(fileName.substring(8, 10));
            int minute = Integer.parseInt(fileName.substring(10, 12));
            int sec = Integer.parseInt(fileName.substring(12));

            if (false && DEBUG) {
                Debug.output(year + " " + month + " " + day + " " + hour + " "
                        + minute + " " + sec);
            }

            return new GregorianCalendar(year, month - 1, day, hour, minute, sec).getTimeInMillis();
        }

        throw new NumberFormatException();

    }

    /**
     * Remove old files. Checks the current time against the timestamps decoded
     * by the names of files found in the output directory, and deletes them if
     * the difference between those times is greater than the cleanupInterval.
     * 
     * @param deleteAll if true, all images will be deleted, regardless of when
     *        they were created.
     */
    public void cleanup(boolean deleteAll) {
        long currentTime = System.currentTimeMillis();
        File file = new File(getOutputDirectoryString());
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                File f = files[i];
                if (!deleteAll) {
                    try {
                        long ft = getTimeForFileName(f.getName());
                        long tdiff = currentTime - ft;

                        if (DEBUG) {
                            Debug.output("MagicPlanetImageComponent considering deleting "
                                    + f.getName()
                                    + ", file time:"
                                    + ft
                                    + ", current time:"
                                    + currentTime
                                    + ", interval:"
                                    + getCleanupInterval()
                                    + ", diff:" + tdiff);
                        }

                        if (tdiff > getCleanupInterval()) {
                            if (DEBUG)
                                Debug.output("   deleting...");
                            f.delete();
                        }

                    } catch (NumberFormatException nfe) {
                        // skip it.
                    }
                } else {
                    f.delete();
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName() == MapBean.BackgroundProperty) {
            setBackground((Paint) evt.getNewValue());
        }
    }

    /**
     * Set the 'ocean' color of the planet. The MagicPlanetImageComponent
     * listens for events from the MapBean and will call this method if the
     * ocean color on the MapBean is changed.
     * 
     * @param paint
     */
    protected void setBackground(Paint paint) {
        background = paint;
        handleUpdate();
    }

    /**
     * Return the 'ocean' color of the planet.
     * 
     * @return Returns the background.
     */
    public Paint getBackground() {
        return background;
    }

    public Layer[] getLayers() {
        return layers;
    }

    public void setLayers(Layer[] layers) {
        this.layers = layers;

        handleUpdate();
    }

    public String getOutputDirectoryString() {
        return outputDirectoryString;
    }

    /**
     * Set the directory where the images should be written to.
     * 
     * @param outputDirectoryString
     */
    public void setOutputDirectoryString(String outputDirectoryString) {
        this.outputDirectoryString = outputDirectoryString;

        try {
            File dir = new File(outputDirectoryString);
            if (dir.exists() || dir.mkdirs()) {
                return;
            }
        } catch (SecurityException se) {
        }

        // Ran into a problem
        JOptionPane.showMessageDialog(getMapBean(),
                "I can't access this directory to store the Magic Planet images in:\n"
                        + outputDirectoryString
                        + "\n\nPlease check the permissions for that directory.",
                "Problem Creating Directory",
                JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Get the image projection.
     * 
     * @return current Projection of image.
     */
    public Projection getProj() {
        return proj;
    }

    /**
     * Set the image projection.
     * 
     * @param proj
     */
    public void setProj(Projection proj) {
        this.proj = proj;
        handleUpdate();
    }

    /**
     * @return the scale value of the projection.
     */
    public float getScale() {
        return scale;
    }

    /**
     * Sets the scale for the projection, which directly affects the size of the
     * image. Larger numbers make smaller images. Calling this method causes the
     * setProj() method to be called with the new projection to use for images.
     * 
     * @param scale
     */
    public void setScale(float scale) {
        this.scale = scale;

        LatLonPoint center = new LatLonPoint.Double();
        LLXY llxy = new LLXY(center, scale, 2000, 1000);
        Point2D p1 = llxy.forward(90f, -180f);
        Point2D p2 = llxy.forward(-90f, 180f);

        int w = (int) (p2.getX() - p1.getX());
        int h = (int) (p2.getY() - p1.getY());
        Projection proj = new LLXY(center, scale, w, h);
        setProj(proj);

        if (DEBUG) {
            Debug.output("Created projection " + proj + " from " + p1 + ", "
                    + p2);
        }
    }

    /**
     * @return check if a new image should be created if the layers or
     *         background color changes.
     */
    public boolean isAutoUpdate() {
        return autoUpdate;
    }

    /**
     * Set whether a new image should be created immediately if the MapBean's
     * layers change, or if the MapBean's background color changes. If false, a
     * new image will be created on the next normal timer cycle.
     * 
     * @param autoUpdate
     */
    public void setAutoUpdate(boolean autoUpdate) {
        this.autoUpdate = autoUpdate;
    }

    public ImageFormatter getImageFormatter() {
        return imageFormatter;
    }

    /**
     * Set the ImageFormatter to use for creating the image files.
     * 
     * @param iFormatter
     */
    public void setImageFormatter(ImageFormatter iFormatter) {
        imageFormatter = iFormatter;
        if (imageFormatter == null) {
            imageFormatter = new SunJPEGFormatter();
        }
    }

    public boolean isCleanup() {
        return cleanup;
    }

    /**
     * Set whether the component should delete old images.
     * 
     * @param cleanup
     */
    public void setCleanup(boolean cleanup) {
        this.cleanup = cleanup;
    }

    public int getCleanupInterval() {
        return cleanupInterval;
    }

    /**
     * Set the interval, in milliseconds, between the current time and the time
     * old images were created before they are deleted. This setting only
     * matters if isCleanup() returns true.
     * 
     * @param cleanupInterval
     */
    public void setCleanupInterval(int cleanupInterval) {
        this.cleanupInterval = cleanupInterval;
    }

    public int getHeight() {
        return height;
    }

    /**
     * Set the scaled pixel height of the images.-1 maintains what the scale
     * setting decides.
     * 
     * @param height pixels.
     */
    public void setHeight(int height) {
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    /**
     * Set the scaled pixel width of the images. -1 maintains what the scale
     * setting decides.
     * 
     * @param width pixels.
     */
    public void setWidth(int width) {
        this.width = width;
    }

    /**
     * Get the location of a file that can be read to find out the name of the
     * last image to be created. If null, that means no such file is being
     * created.
     * 
     * @return the file name.
     */
    public String getLastImageFile() {
        return lastImageFile;
    }

    /**
     * Set the location of a file that can be read to find out the name of the
     * last image to be created. If null, that means no such file is being
     * created.
     * 
     * @param lastImageFile
     */
    public void setLastImageFile(String lastImageFile) {
        this.lastImageFile = checkTrimAndNull(lastImageFile);
    }

    public String getPostProcessingScript() {
        return postProcessingScript;
    }

    public void setPostProcessingScript(String postProcessingScript) {
        this.postProcessingScript = checkTrimAndNull(postProcessingScript);
    }

    protected String checkTrimAndNull(String s) {
        if (s != null) {
            s = s.trim();
            if (s.length() == 0) {
                s = null;
            }
        }
        return s;
    }
}