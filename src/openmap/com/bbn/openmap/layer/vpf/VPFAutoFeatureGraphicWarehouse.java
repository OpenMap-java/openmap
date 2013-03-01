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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/vpf/VPFFeatureGraphicWarehouse.java,v $
// $RCSfile: VPFFeatureGraphicWarehouse.java,v $
// $Revision: 1.9 $
// $Date: 2009/01/21 01:24:41 $
// $Author: dietrick $
//
// **********************************************************************

package com.bbn.openmap.layer.vpf;

import java.awt.BasicStroke;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.bbn.openmap.Environment;
import com.bbn.openmap.I18n;
import com.bbn.openmap.PropertyConsumer;
import com.bbn.openmap.dataAccess.cgm.CGM;
import com.bbn.openmap.dataAccess.cgm.CGMDisplay;
import com.bbn.openmap.geo.BoundingCircle;
import com.bbn.openmap.geo.Geo;
import com.bbn.openmap.geo.GeoSegment;
import com.bbn.openmap.io.CSVFile;
import com.bbn.openmap.io.FormatException;
import com.bbn.openmap.omGraphics.OMColor;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.OMPoint;
import com.bbn.openmap.omGraphics.OMPoly;
import com.bbn.openmap.omGraphics.OMRasterObject;
import com.bbn.openmap.omGraphics.OMScalingIcon;
import com.bbn.openmap.omGraphics.OMText;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.proj.coords.LatLonPoint;
import com.bbn.openmap.util.ComponentFactory;
import com.bbn.openmap.util.DataBounds;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PropUtils;

/**
 * Implement a graphic factory that builds OMGraphics from VPF. Designed to work
 * closely with the VPFFeatureLayer, using GeoSymAttExpression objects to figure
 * out how features are rendered. Uses two files to help manage features. The
 * first file is a symbol lookup file that ties FACC codes and attribute
 * settings with a set of CGM files. This file should cover entries for a
 * particular data set. The second file is a priority file, that lists the order
 * that features should be rendered, by feature type, facc code and attribute
 * settings. If you want to change which features are displayed, or the order in
 * which they are displayed, this is the file to modify.
 * <p>
 * 
 * Both of these files are CSV files, and their fields are important. The lookup
 * file is of the format:
 * 
 * <pre>
 * facc,type,symbol,conditions
 * AK160,A,0804,
 * AL005,A,0081 0734,
 * AL015,P,0002,bfc=81ANDsta=0or2or3or6or11
 * AL015,P,0010,bfc=7ANDhwt=0or2or3or4or7or22
 * AL015,P,0011,bfc=7ANDhwt=11or14or15or16or20or21
 * </pre>
 * 
 * Note that the conditions field can be empty.The first field is the 5
 * character FACC code, the second field is the type (P, A, L) and the third
 * field is the CGM file name.
 * <P>
 * 
 * The priority file is similar:
 * 
 * <pre>
 * priority,type,facc,conditions,description
 * 0,Area,BA040,
 * 0,Area,BE010,cvl=99999
 * 0,Area,BE010,idsm=0 AND cvl>=msdcand<>99999
 * 0,Area,BE010,idsm=0 AND cvl>=ssdcand<msdc
 * </pre>
 * 
 * The priority field really isn't important, the order of the overall file is.
 * 
 * @see com.bbn.openmap.omGraphics.OMGraphic
 */
public class VPFAutoFeatureGraphicWarehouse
        implements VPFFeatureWarehouse, PropertyConsumer {

    protected static Logger logger = Logger.getLogger("com.bbn.openmap.layer.vpf.VPFAutoFeatureGraphicWarehouse");

    public final static String CGM_DIR_PROPERTY = "cgmDirectory";
    public final static String SYMBOL_LOOKUP_FILE_PROPERTY = "faccLookupFile";
    public final static String PRIORITY_FILE_PROPERTY = "priorityFile";
    public final static String FEATURE_INFO_HANDLER_PROPERTY = "featureInfoHandler";
    public final static String FACC_DEBUG_PROPERTY = "debug";
    public final static String ICON_SIZE_PROPERTY = "iconSize";

    public final static int DEFAULT_ICON_SIZE = 20;

    protected List<FeaturePriorityHolder> priorities;
    protected Hashtable<String, List<FeaturePriorityHolder>> faccLookup;
    protected String priorityFilePath;
    protected String faccLookupFilePath;
    protected String geoSymDirectory;
    protected VPFFeatureInfoHandler featInfoHandler;
    protected int iconSize = DEFAULT_ICON_SIZE;

    protected String[] compositeFeatureFaccs = new String[] {
        "BC010",
        "BC020",
        "BC040",
        "BC070"
    };

    /**
     * If set, the warehouse will limit visibility to specified facc and print
     * out decision making process.
     */
    protected String debugFacc = null;

    /**
     * Set which library to use. If null, all applicable libraries in database
     * will be searched.
     */
    private List<String> useLibrary = null;
    /**
     * The property prefix for scoping properties.
     */
    protected String prefix;

    public final static String EV_ISDM = "isdm";
    public final static String EV_IDSM = "idsm";
    public final static String EV_SSDC = "ssdc";
    public final static String EV_MSDC = "msdc";
    public final static String EV_MSSC = "mssc";

    /**
     * Interactive Shallow Display Mode:ECDIS defines the display mode of
     * shallow water areas (shallow depth zones) to be one of two symbology
     * scenarios. The attribute values are 1 and 0, which toggle the shallow
     * display mode to be on or off respectively. When ISDM is set on (1), the
     * display of all depth zones shallower than the defined values of the Ships
     * Safety Depth Contour (SSDC) are overprinted with a lattice pattern. This
     * mode can be initiated in the four- or two-depth zone display modes (not
     * including the drying line), defined by the Interactive Display Selection
     * Mode, (IDSM). The shallow display mode is made available due to viewing
     * limitations of the shallow depth zones in night displays.
     */
    protected double isdm = 0;
    /**
     * Interactive Display Selection Mode:ECDIS defines the display of depth
     * zones to be divided into two or four depth areas. This variable allows
     * for the mariner to interactively set either display mode. The two-zone
     * mode uses only the ships safety depth contour (SSDC) as a zone separator,
     * whereas the four-zone mode further divides zones based on the mariner
     * selected deep and shallow contours (MSDC, MSSC). Attribute values are 0
     * and 1 meaning four- and two-zone modes respectively.
     */
    protected double idsm = 0;
    /**
     * Ship's Safety Depth Contour: The ships safety depth contour represents a
     * safe contour based on the draft of the ship. This value must be entered
     * by the mariner using an application interface. This interface must ensure
     * that if a contour value does not exist within the data, that a next
     * deeper value is specified as the SSDC. This checking must be dynamic as
     * one traverses tile boundaries within the data..
     */
    protected double ssdc = 5;
    /**
     * Mariner Specified Deep Contour: The four-zone display mode requires the
     * establishment of a deep contour that must be specified by the mariner
     * through application inquiry. A default value may be implemented at 30m
     * according to the ISO Color and Symbol Specification directives.
     */
    protected double msdc = 30;
    /**
     * Mariner Specified Shallow Contour - The four-zone display mode requires
     * the establishment of a shallow contour that must be specified by the
     * mariner through application inquiry. A default value may be implemented
     * at 2m according to the ISO Color and Symbol Specification directives.
     */
    protected double mssc = 1;

    /**
     *
     */
    public VPFAutoFeatureGraphicWarehouse() {

    }

    /**
     * The warehouse is initialized the first time features are fetched.
     */
    protected void init() {

        CSVFile priorityFile;
        CSVFile symbolLookupFile;
        try {

            symbolLookupFile = new CSVFile(faccLookupFilePath);
            symbolLookupFile.setHeadersExist(true);
            symbolLookupFile.loadData(true);

            priorityFile = new CSVFile(priorityFilePath);
            priorityFile.setHeadersExist(true);
            priorityFile.loadData(true);

            faccLookup = new Hashtable<String, List<FeaturePriorityHolder>>();
            Hashtable<String, FeaturePriorityHolder.Compound> composites = new Hashtable<String, FeaturePriorityHolder.Compound>();

            // Build up the priority holder list - this keeps the features in
            // proper rendering order, according to the priority csv file.
            // composite features are just held once at the first location they
            // are
            // found.

            int numPriorities = priorityFile.getNumberOfRecords();
            priorities = new ArrayList<FeaturePriorityHolder>();
            for (Vector<Object> row : priorityFile) {
                String lineCheck = null;
                String type = null;
                String facc = null;
                String conditions = null;

                try {
                    lineCheck = row.get(0).toString();

                    if (lineCheck.startsWith("#")) {
                        continue;
                    }

                    type = row.get(1).toString();
                    facc = row.get(2).toString();
                    conditions = row.get(3).toString();

                } catch (ArrayIndexOutOfBoundsException aioobe) {
                    logger.warning("Bad entry in priority file: " + lineCheck + "," + type + "," + facc + "," + conditions);
                    continue;
                }

                // If the debugFacc is defined, just add that particular facc
                // type
                if (debugFacc != null && !debugFacc.equals(facc)) {
                    continue;
                }

                // Now we need to check if the facc is a composite symbol, like
                // a
                // buoy.
                boolean composite = false;
                if (type.charAt(0) == CoverageTable.UPOINT_FEATURETYPE) {
                    for (String compFacc : compositeFeatureFaccs) {
                        if (compFacc.equals(facc)) {
                            composite = true;
                            break;
                        }
                    }
                }

                FeaturePriorityHolder.Basic ph = new FeaturePriorityHolder.Basic(type, facc, conditions, this);
                ph.setCGMPath(geoSymDirectory, ".cgm");

                // If it is a composite, we need to add it to a
                // FeaturePriorityHolder.Compound object, so that all the little
                // parts will be added to any symbols, based on how the parts
                // conditions match up to the feature entry in the attribute
                // table.
                if (composite) {

                    FeaturePriorityHolder.Compound compound = composites.get(facc);
                    if (compound == null) {
                        compound = new FeaturePriorityHolder.Compound(type, facc, this);
                        composites.put(facc, compound);
                        priorities.add(compound);

                        // faccLookup doesn't have this facc if we're here...
                        List<FeaturePriorityHolder> list = new ArrayList<FeaturePriorityHolder>();
                        faccLookup.put(facc, list);
                        list.add(compound);
                    }

                    compound.addPart(ph);

                } else {
                    priorities.add(ph);

                    List<FeaturePriorityHolder> list = faccLookup.get(facc);
                    if (list == null) {
                        list = new ArrayList<FeaturePriorityHolder>();
                        faccLookup.put(facc, list);
                    }
                    list.add(ph);
                }
            }

            // The priority file doesn't know anything about the symbol that is
            // going to be used for the different conditions in the feature
            // holders. Need to disperse the symbol information to the feature
            // holders.

            // OK, time killer - loop through
            int numSymbols = symbolLookupFile.getNumberOfRecords();
            int foundRecords = 0;
            for (Vector<Object> row : symbolLookupFile) {
                int numArgs = row.size();
                String facc = row.get(0).toString();
                if (numArgs != 7) {
                    logger.warning("Problem with facc entry, not correct number of args in csv file:" + facc);
                    continue;
                }

                char type = getType(row.get(1).toString());
                String symbolCode = row.get(2).toString();
                String conditions = row.get(3).toString().trim();
                String size = row.get(4).toString().trim();
                String xoff = row.get(5).toString().trim();
                String yoff = row.get(6).toString().trim();

                if (conditions.length() > 0) {
                    conditions = conditions.replace(" ", "");
                }

                List<FeaturePriorityHolder> faccList = faccLookup.get(facc);
                if (faccList != null) {
                    boolean found = false;
                    for (FeaturePriorityHolder ph : faccList) {
                        if (ph.matches(facc, type, conditions, symbolCode, size, xoff, yoff)) {
                            found = true;
                            foundRecords++;
                            break;
                        }
                    }

                    if (!found) {
                        // This really shouldn't be triggered, if it is,
                        // something
                        // happened to the data files. But that might be
                        // intentional,
                        // to keep some feature types off the map.
                        if (logger.isLoggable(Level.FINE)) {
                            logger.fine("didn't find matching PriorityHolder for " + facc + "|" + type + "|" + symbolCode + "|"
                                    + conditions);
                        }
                    }

                } else {
                    // Since we've turned off loading faccs for everything but
                    // the
                    // debug facc, of course other things will complain.
                    if (debugFacc == null) {
                        if (logger.isLoggable(Level.FINE)) {
                            logger.fine("can't find faccLookup for " + facc + " for" + type + "|" + symbolCode + "|" + conditions);
                        }
                    }
                }

            }

            if (logger.isLoggable(Level.FINE)) {
                logger.fine("matched up " + foundRecords + " of " + numSymbols + " symbols, " + numPriorities + " priority entries");
            }

        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public static char getType(String type) {
        if (type == null) {
            logger.warning("unknown type!");
        } else {
            switch (type.charAt(0)) {
                case 'P':
                    return CoverageTable.UPOINT_FEATURETYPE;
                case 'A':
                    return CoverageTable.AREA_FEATURETYPE;
                case 'L':
                    return CoverageTable.EDGE_FEATURETYPE;
                default:
            }
        }
        return CoverageTable.SKIP_FEATURETYPE;
    }

    /**
     * Set the VPF libraries to use, by name. If null, all libraries will be
     * searched. Null is default.
     */
    public void setUseLibraries(List<String> libNames) {
        useLibrary = libNames;
    }

    /**
     * Get a list of VPF library names that should be used, specified at
     * configuration.
     */
    public List<String> getUseLibraries() {
        return useLibrary;
    }

    /**
     * Utility method to check if the specified library name has been set by the
     * configuration as one to use.
     * 
     * @param libName the library name to test
     * @return true if the useLibrary list has not been set, is empty, or if the
     *         provided name starts with the specified string entry (Good for
     *         specifying sets of like-libraries).
     */
    public boolean checkLibraryForUsage(String libName) {
        boolean useLibrary = true;
        List<String> libraryNames = getUseLibraries();
        if (libraryNames != null && !libraryNames.isEmpty()) {
            useLibrary = false;
            for (String libraryName : libraryNames) {
                if (libName.startsWith(libraryName)) {
                    useLibrary = true;
                    break;
                }
            }
        }
        return useLibrary;
    }

    /**
     * Create an OMPoly for an area described by the facevec.
     */
    public OMGraphic createArea(CoverageTable covtable, AreaTable areatable, List<Object> facevec, LatLonPoint ll1,
                                LatLonPoint ll2, double dpplat, double dpplon, String featureType, int primID) {

        List<CoordFloatString> ipts = new ArrayList<CoordFloatString>();

        int totalSize = 0;
        try {
            totalSize = areatable.computeEdgePoints(facevec, ipts);
        } catch (FormatException f) {
            Debug.output("FormatException in computeEdgePoints: " + f);
            return null;
        }
        if (totalSize == 0) {
            return null;
        }

        OMPoly py =
                LayerGraphicWarehouseSupport.createAreaOMPoly(ipts, totalSize, ll1, ll2, dpplat, dpplon,
                                                              covtable.doAntarcticaWorkaround);
        py.setIsPolygon(true);
        return py;
    }

    protected String info = null;

    /**
     *
     */
    public OMGraphic createEdge(CoverageTable c, EdgeTable edgetable, List<Object> edgevec, LatLonPoint ll1, LatLonPoint ll2,
                                double dpplat, double dpplon, CoordFloatString coords, String featureType, int primID) {

        OMPoly py = LayerGraphicWarehouseSupport.createEdgeOMPoly(coords, ll1, ll2, dpplat, dpplon);
        py.setFillPaint(OMColor.clear);
        py.setIsPolygon(false);
        return py;
    }

    /**
     *
     */
    public OMGraphic createText(CoverageTable c, TextTable texttable, List<Object> textvec, double latitude, double longitude,
                                String text, String featureType, int primID) {

        OMText txt = LayerGraphicWarehouseSupport.createOMText(text, latitude, longitude);
        return txt;
    }

    /**
     * Method called by the VPF reader code to construct a node feature.
     */
    public OMGraphic createNode(CoverageTable c, NodeTable t, List<Object> nodeprim, double latitude, double longitude,
                                boolean isEntityNode, String featureType, int primID) {
        // OMPoint pt = new OMPoint.Image(latitude, longitude);

        OMScalingIcon pt = new OMScalingIcon(latitude, longitude, (Image) null);
        pt.setBaseScale(500000);
        pt.setMinScale(500000);
        pt.setMaxScale(2000000);
        return pt;
    }

    public boolean needToFetchTileContents(String libraryName, String currentFeature, TileDirectory currentTile) {
        return true;
    }

    /**
     * This is where the magic happens.
     * 
     * @param lst LibrarySelectionTable that lets the warehouse know where the
     *        data is and what's in it.
     * @param ll1 upper left coordinate of the desired area.
     * @param ll2 lower right coordinate of the desired area.
     * @param proj the projection for the area, used to generate OMGraphics
     *        added to the list.
     * @param omgList the list to add OMGraphics to. One will be created and
     *        returned if this is null.
     * @return the OMGraphicList with OMGraphics for features over desired area.
     * @throws FormatException
     */
    public OMGraphicList getFeatures(LibrarySelectionTable lst, LatLonPoint ll1, LatLonPoint ll2, Projection proj,
                                     OMGraphicList omgList)
            throws FormatException {

        if (priorities == null) {
            init();
        }

        // handle Dateline
        if (ll1.getX() > ll2.getX()) {
            omgList = getFeatures(lst, ll1, new LatLonPoint.Double(ll2.getY(), 180 - .00001),// 180-epsilon
                                  proj, omgList);
            omgList = getFeatures(lst, new LatLonPoint.Double(ll1.getY(), -180f), ll2, proj, omgList);
            return omgList;
        }

        if (omgList == null) {
            omgList = new OMGraphicList();
        }

        omgList.setTraverseMode(OMGraphicList.LAST_ADDED_ON_TOP);

        int screenheight = proj.getHeight();
        int screenwidth = proj.getWidth();
        double dpplat = Math.abs((ll1.getY() - ll2.getY()) / screenheight);
        double dpplon = Math.abs((ll1.getX() - ll2.getX()) / screenwidth);

        /*
        BoundingCircle screenBounds = new GeoSegment.Impl(new Geo[] {
            new Geo(ll1.getLatitude(), ll1.getLongitude()),
            new Geo(ll2.getLatitude(), ll2.getLongitude())
        }).getBoundingCircle();
        */
        
        DataBounds screenBounds = new DataBounds(ll1, ll2);

        for (String libraryName : lst.getLibraryNames()) {

            if (!checkLibraryForUsage(libraryName)) {
                continue;
            }

            if (logger.isLoggable(Level.FINE)) {
                logger.fine("reading library: " + libraryName);

            }

            CoverageAttributeTable cat = lst.getCAT(libraryName);

            if (cat == null) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("no CoverageAttributeTable for " + libraryName + ", skipping...");
                }
                continue;
            }

            // Do a quick bounds check, so we can just skip the tiles for this
            // CAT
            // if nothing is on the map.
            DataBounds bounds = cat.getBounds();
            if (bounds != null) {
                /*
                Point2D min = bounds.getMin();
                Point2D max = bounds.getMax();
                BoundingCircle catCircle = new GeoSegment.Impl(new Geo[] {
                    new Geo(min.getY(), min.getX()),
                    new Geo(max.getY(), max.getX())
                }).getBoundingCircle();
                 */
                if (!screenBounds.intersects(bounds)) {
                    logger.fine("CoverageAttributeTable for " + libraryName + " not on map, skipping...");
                    continue;
                }
            }

            for (String covname : cat.getCoverageNames()) {
                if (logger.isLoggable(Level.FINER)) {
                    logger.finer("for coverage: " + covname + ", coverage topology level: " + cat.getCoverageTopologyLevel(covname));
                }

                CoverageTable coverageTable = cat.getCoverageTable(covname);

                coverageTable.getFeatures(this, ll1, ll2, dpplat, dpplon, omgList);

            }
        }

        // Go through PriorityHolders and build up OMGraphicList, in order for
        // rendering to map. Moved this from inside the for loop above, so that
        // feature order is preserved across libraries.
        for (FeaturePriorityHolder ph : priorities) {
            OMGraphicList list = ph.getList();
            if (list != null) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("Adding features from " + ph.toString() + ": " + list.size() + " features");
                }
                list.generate(proj);
                omgList.addAll(list);

                list.setVisible(debugFacc == null || ph.getDebugFacc() != null);

                // Now that the OMGraphics are part of the main list, clean them
                // out for the next request. Doing it here saves from having to
                // do another loop through at the beginning of getFeatures.
                ph.resetList();
            }
        }

        logger.fine("returning from prepare ************");

        return omgList;
    }

    /**
     * Return true, this is a NOOP for this warehouse.
     */
    public boolean drawEdgeFeatures() {
        return true;
    }

    /**
     * Return true, this is a NOOP for this warehouse.
     */
    public boolean drawTextFeatures() {
        return true;
    }

    /**
     * Return true, this is a NOOP for this warehouse.
     */
    public boolean drawAreaFeatures() {
        return true;
    }

    /**
     * Return true, this is a NOOP for this warehouse.
     */
    public boolean drawEPointFeatures() {
        return true;
    }

    /**
     * Return true, this is a NOOP for this warehouse.
     */
    public boolean drawCPointFeatures() {
        return true;
    }

    /**
     * @param rightSide the string pulled out of the VPF data for attribute
     *        comparisons.
     * @return value greater than 0 for valid strings.
     */
    public double getExternalAttribute(String rightSide) {
        double ret = -1;
        if (rightSide != null) {
            if (rightSide.equals(EV_IDSM)) {
                ret = idsm;
            } else if (rightSide.equals(EV_ISDM)) {
                ret = isdm;
            } else if (rightSide.equals(EV_MSDC)) {
                ret = msdc;
            } else if (rightSide.equals(EV_MSSC)) {
                ret = mssc;
            } else if (rightSide.equals(EV_SSDC)) {
                ret = ssdc;
            }
        }

        return ret;
    }

    /**
     * Given an OMGraphic that is going to be added to the map, use the
     * FeatureClassInfo to gather attribute information from the fcirow
     * contents.
     * 
     * @param omg The OMGraphic representing a feature.
     * @param fci The Description of the columns of the fcirow.
     * @param fcirow The attributes for the feature.
     */
    public void handleInformationForOMGraphic(OMGraphic omg, FeatureClassInfo fci, List<Object> fcirow) {

        if (featInfoHandler != null) {
            featInfoHandler.updateInfoForOMGraphic(omg, fci, fcirow);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.bbn.openmap.PropertyConsumer#setProperties(java.util.Properties)
     */
    public void setProperties(Properties setList) {
        setProperties(null, setList);
    }

    /**
     * Set properties of the warehouse.
     * 
     * @param prefix the prefix to use for looking up properties.
     * @param props the properties file to look at.
     */
    public void setProperties(String prefix, Properties props) {
        setPropertyPrefix(prefix);
        prefix = PropUtils.getScopedPropertyPrefix(prefix);

        faccLookupFilePath = props.getProperty(prefix + SYMBOL_LOOKUP_FILE_PROPERTY, faccLookupFilePath);
        priorityFilePath = props.getProperty(prefix + PRIORITY_FILE_PROPERTY, priorityFilePath);
        debugFacc = props.getProperty(prefix + FACC_DEBUG_PROPERTY, debugFacc);
        geoSymDirectory = props.getProperty(prefix + CGM_DIR_PROPERTY, geoSymDirectory);

        iconSize = PropUtils.intFromProperties(props, prefix + ICON_SIZE_PROPERTY, iconSize);

        String fihString = props.getProperty(prefix + FEATURE_INFO_HANDLER_PROPERTY);
        if (fihString != null) {
            Object obj = ComponentFactory.create(fihString, prefix, props);
            if (obj instanceof VPFFeatureInfoHandler) {
                featInfoHandler = (VPFFeatureInfoHandler) obj;
            }
        }

        isdm = PropUtils.doubleFromProperties(props, prefix + EV_ISDM, isdm);
        idsm = PropUtils.doubleFromProperties(props, prefix + EV_IDSM, idsm);
        msdc = PropUtils.doubleFromProperties(props, prefix + EV_MSDC, msdc);
        mssc = PropUtils.doubleFromProperties(props, prefix + EV_MSSC, mssc);
        ssdc = PropUtils.doubleFromProperties(props, prefix + EV_SSDC, ssdc);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.bbn.openmap.PropertyConsumer#getProperties(java.util.Properties)
     */
    public Properties getProperties(Properties getList) {
        if (getList == null) {
            getList = new Properties();
        }

        String prefix = PropUtils.getScopedPropertyPrefix(this);

        getList.put(prefix + SYMBOL_LOOKUP_FILE_PROPERTY, faccLookupFilePath);
        getList.put(prefix + PRIORITY_FILE_PROPERTY, priorityFilePath);
        getList.put(prefix + CGM_DIR_PROPERTY, geoSymDirectory);
        if (featInfoHandler != null) {
            getList.put(prefix + FEATURE_INFO_HANDLER_PROPERTY, featInfoHandler.getClass().getName());

            if (featInfoHandler instanceof PropertyConsumer) {
                ((PropertyConsumer) featInfoHandler).getProperties(getList);
            }
        }

        getList.put(prefix + ICON_SIZE_PROPERTY, Integer.toString(iconSize));

        if (debugFacc != null && debugFacc.length() > 0) {
            getList.put(prefix + FACC_DEBUG_PROPERTY, debugFacc);
        }

        getList.put(prefix + EV_ISDM, Double.toString(isdm));
        getList.put(prefix + EV_IDSM, Double.toString(idsm));
        getList.put(prefix + EV_MSDC, Double.toString(msdc));
        getList.put(prefix + EV_MSSC, Double.toString(mssc));
        getList.put(prefix + EV_SSDC, Double.toString(ssdc));

        return getList;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.bbn.openmap.PropertyConsumer#getPropertyInfo(java.util.Properties)
     */
    public Properties getPropertyInfo(Properties list) {
        if (list == null) {
            list = new Properties();
        }
        I18n i18n = Environment.getI18n();
        PropUtils.setI18NPropertyInfo(i18n, list, VPFAutoFeatureGraphicWarehouse.class, SYMBOL_LOOKUP_FILE_PROPERTY,
                                      "Symbol Lookup File", "The path to the file containing symbol lookup information",
                                      "com.bbn.openmap.util.propertyEditor.FilePropertyEditor");
        PropUtils.setI18NPropertyInfo(i18n, list, VPFAutoFeatureGraphicWarehouse.class, PRIORITY_FILE_PROPERTY, "Priority File",
                                      "The path to the file containing feature type and order to use for display",
                                      "com.bbn.openmap.util.propertyEditor.FilePropertyEditor");
        PropUtils.setI18NPropertyInfo(i18n, list, VPFAutoFeatureGraphicWarehouse.class, CGM_DIR_PROPERTY, "CGM Directory Path",
                                      "The path to the directory containing GeoSym CGM files",
                                      "com.bbn.openmap.util.propertyEditor.DirectoryPropertyEditor");
        PropUtils.setI18NPropertyInfo(i18n, list, VPFAutoFeatureGraphicWarehouse.class, FACC_DEBUG_PROPERTY, "FACC Debug",
                                      "A FACC code to use to debug problems with data set", null);
        PropUtils.setI18NPropertyInfo(i18n, list, VPFAutoFeatureGraphicWarehouse.class, ICON_SIZE_PROPERTY, "Icon Size",
                                      "The pixel size of icons for point features", null);
        return list;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.bbn.openmap.PropertyConsumer#setPropertyPrefix(java.lang.String)
     */
    public void setPropertyPrefix(String prefix) {
        this.prefix = prefix;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.bbn.openmap.PropertyConsumer#getPropertyPrefix()
     */
    public String getPropertyPrefix() {
        return prefix;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.bbn.openmap.layer.vpf.VPFWarehouse#resetForCAT()
     */
    public void resetForCAT() {
        // NOOP
    }

    public int getIconSize() {
        return iconSize;
    }

    public void setIconSize(int iconSize) {
        this.iconSize = iconSize;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.bbn.openmap.layer.vpf.VPFWarehouse#getGUI(com.bbn.openmap.layer.vpf
     * .LibrarySelectionTable)
     */
    public Component getGUI(LibrarySelectionTable lst) {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc) NOOP
     * 
     * @see com.bbn.openmap.layer.vpf.VPFWarehouse#getFeatures()
     */
    public List<String> getFeatures() {
        return Collections.emptyList();
    }

    /**
     * Set the object used to manage attribute formatting and display for
     * features on the map.
     * 
     * @return VPFFeatureInfoHandler being used.
     */
    public VPFFeatureInfoHandler getFeatInfoHandler() {
        return featInfoHandler;
    }

    public void setFeatInfoHandler(VPFFeatureInfoHandler featInfoHandler) {
        this.featInfoHandler = featInfoHandler;
    }

    /**
     * 
     * A FeaturePriorityHolder represents a rendering order slot in a list of
     * feature types to be rendered. It is responsible for evaluating attributes
     * of a VPF feature and determining if a particular feature matches what
     * this priority holder represents. It can then provide an OMGraphicList for
     * those features that match its attribute conditions.
     * 
     * @author dietrick
     */
    protected static abstract class FeaturePriorityHolder {
        /**
         * The type of the feature, i.e. point, line, area
         */
        protected char type;
        /**
         * The feature code FACC for this kind of feature.
         */
        protected String facc;
        /**
         * The OMGraphicList containing all the matching feature OMGraphics.
         */
        protected OMGraphicList list;
        /**
         * The dimension of icons created for point OMGraphics.
         */
        protected int dim = DEFAULT_ICON_SIZE;

        protected float sizePercent = 1f;
        protected float xoffPercent = 0f;
        protected float yoffPercent = 0f;

        /**
         * A handle to any debug FACC code listed by the warehouse, so that a
         * specific type of feature can be singled out for debugging.
         */
        protected String debugFacc = null;

        protected FeaturePriorityHolder(String type, String facc, VPFAutoFeatureGraphicWarehouse warehouse) {
            this.type = getType(type);
            this.facc = facc;
            this.dim = warehouse.getIconSize();

            if (warehouse.debugFacc != null && warehouse.debugFacc.equals(facc)) {
                debugFacc = warehouse.debugFacc;
            }
        }

        public OMGraphicList getList() {
            if (debugFacc != null && list != null) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine(list.getDescription());
                }
            }
            return list;
        }

        public String getFacc() {
            return facc;
        }

        String getDebugFacc() {
            return debugFacc;
        }

        public void resetList() {
            if (list != null) {
                list.clear();
            }
        }

        public void updateLocation(String size, String xoff, String yoff) {
            sizePercent = getValue(size, 1f);
            xoffPercent = getValue(xoff, 0f);
            yoffPercent = getValue(yoff, 0f);
        }

        protected float getValue(String s, float def) {
            float ret = def;
            if (s != null) {
                try {
                    ret = Float.parseFloat(s);
                } catch (NumberFormatException nfe) {
                }
            }
            return ret;
        }

        /**
         * Used to match feature entries with PriorityHolder.
         * 
         * @param facc
         * @param fci
         * @param row
         * @return true if feature entry matches PriorityHolder conditions.
         */
        public abstract boolean matches(String facc, FeatureClassInfo fci, List<Object> row);

        /**
         * Used to match symbol codes with PriorityHolder during initialization
         * of PriorityHolders.
         * 
         * @param facc
         * @param type
         * @param conditions
         * @param size percent of dim setting to use for size of symbol (0-1f)
         * @param xoff percent off center of dim setting to use for x origin of
         *        symbol (0 is centered, positive is right)
         * @param yoff percent off center of dim setting to use for x origin of
         *        symbol (0 is centered, positive is down)
         * @return true of feature entry matches PriorityHolder conditions.
         */
        public abstract boolean matches(String facc, char type, String conditions, String symbolFileName, String size, String xoff,
                                        String yoff);

        protected abstract void add(OMGraphic omg);

        protected static class Basic
                extends FeaturePriorityHolder {

            protected GeoSymAttExpression expression;
            protected String conditions;
            protected String symbolParentDir;
            protected String symbolExt;

            protected String[] cgmTitle;
            protected CGMDisplay[] cgmDisplay;
            protected BufferedImage icon;

            protected Basic(String type, String facc, String cond, VPFAutoFeatureGraphicWarehouse warehouse) {
                super(type, facc, warehouse);

                if (cond != null && cond.trim().length() > 0) {
                    this.conditions = cond.replace(" ", "");
                    expression = new GeoSymAttExpression(this.conditions, warehouse);
                }

            }

            public String toString() {
                return type + "|" + facc + "|" + conditions;
            }

            /**
             * Needs to be called before matches is called in init().
             */
            public void setCGMPath(String parent, String append) {
                symbolParentDir = parent;
                symbolExt = append;
            }

            public Image getIcon() {
                if (icon == null) {
                    try {
                        if (debugFacc != null) {
                            logger.info("initializing cgm for " + toString());
                        }

                        if (cgmTitle == null) {
                            logger.fine("no title for " + toString());
                        } else {

                            cgmDisplay = new CGMDisplay[cgmTitle.length];
                            for (int i = 0; i < cgmTitle.length; i++) {
                                CGM cgm = new CGM(cgmTitle[i]);
                                if (debugFacc != null) {
                                    logger.info("  using " + cgmTitle[i]);
                                }
                                cgmDisplay[i] = new CGMDisplay(cgm);
                                // Rendering the icon will load cgmDisplay with
                                // cgm
                                // parameters
                                // (fill paint, line paint, etc);
                                icon = cgmDisplay[i].getBufferedImage((int) (dim * sizePercent), (int) (dim * sizePercent));
                            }
                        }
                    } catch (IOException ioe) {
                        logger.fine("Couldn't load CGM files: " + cgmTitle[0] + "; first of " + cgmTitle.length);
                    }
                }

                return icon;
            }

            /**
             * Used to match feature entries with PriorityHolder.
             * 
             * @param facc
             * @param fci
             * @param row
             * @return true of feature matches conditions of PriorityHolder.
             */
            public boolean matches(String facc, FeatureClassInfo fci, List<Object> row) {
                boolean ret = false;
                char type = fci.getFeatureType();
                if (type == CoverageTable.EPOINT_FEATURETYPE || type == CoverageTable.CPOINT_FEATURETYPE) {
                    type = CoverageTable.UPOINT_FEATURETYPE;
                }
                if (facc.equals(this.facc) && this.type == type) {
                    if (expression != null) {
                        ret = expression.evaluate(fci, row);
                    } else {
                        ret = true;
                    }
                }
                return ret;
            }

            /**
             * Used to match symbol codes with PriorityHolder.
             * 
             * @param facc
             * @param type
             * @param conditions
             * @return true if feature matches conditions of PriorityHolder.
             */
            public boolean matches(String facc, char type, String conditions, String symbolFileName, String size, String xoff,
                                   String yoff) {
                boolean basicMatch = this.facc.equals(facc) && type == this.type;

                boolean conditionMatch =
                        ((this.conditions == null || this.conditions.trim().length() == 0) && (conditions == null || conditions.trim()
                                                                                                                               .length() == 0))
                                || (this.conditions != null && this.conditions.equals(conditions));

                boolean ret = basicMatch && conditionMatch;

                if (ret) {
                    Vector<String> names = PropUtils.parseSpacedMarkers(symbolFileName);
                    cgmTitle = new String[names.size()];
                    for (int i = 0; i < names.size(); i++) {
                        cgmTitle[i] = symbolParentDir + "/" + names.get(i) + symbolExt;
                        updateLocation(size, xoff, yoff);
                    }
                }

                return ret;
            }

            public void add(OMGraphic omg) {
                if (list == null) {
                    list = new OMGraphicList();
                }

                // Also makes sure cgmDisplay is initialized
                Image icon = getIcon();

                if (cgmDisplay != null) {
                    if (omg instanceof OMPoint.Image) {
                        ((OMPoint.Image) omg).setImage(icon);
                    } else if (omg instanceof OMRasterObject) {
                        ((OMRasterObject) omg).setImage(icon);
                    } else if (omg instanceof OMPoly) {
                        OMPoly omp = (OMPoly) omg;
                        if (!omp.isPolygon()) {
                            // This check is necessary of the cgms are not found
                            if (cgmDisplay[0] != null) {
                                omp.setLinePaint(cgmDisplay[0].getLineColor());
                                omp.setStroke(new BasicStroke(1));
                                omp.setFillPaint(OMColor.clear);
                            }
                        } else {

                            if (cgmDisplay.length == 1 && cgmDisplay[0] != null) {
                                omp.setFillPaint(cgmDisplay[0].getFillColor());
                                omp.setLinePaint(cgmDisplay[0].getFillColor());
                            } else if (cgmDisplay.length > 1 && cgmDisplay[1] != null) {
                                omp.setFillPaint(cgmDisplay[1].getFillColor());
                                omp.setLinePaint(cgmDisplay[1].getFillColor());
                            }
                        }
                    }
                }

                list.add(omg);
            }

            /*
             * (non-Javadoc)
             * 
             * @see com.bbn.openmap.layer.vpf.VPFAutoFeatureGraphicWarehouse.
             * PriorityHolder #getConditions()
             */
            public String getConditions() {
                return conditions;
            }
        }

        /**
         * A Compound FeaturePriorityHolder is used for buoys and other features
         * that have parts added to their representation based on their feature
         * attributes. It contains a list of Basic FeaturePriorityHolders, and
         * each one adds its touch to the resulting OMGraphic as needed.
         * 
         * @author dietrick
         */
        protected static class Compound
                extends FeaturePriorityHolder
                implements ImageObserver {

            protected List<FeaturePriorityHolder.Basic> parts = new ArrayList<FeaturePriorityHolder.Basic>();
            protected BufferedImage icon;

            protected Compound(String type, String facc, VPFAutoFeatureGraphicWarehouse warehouse) {
                super(type, facc, warehouse);

            }

            public String toString() {
                return "Compound: " + type + "|" + facc;
            }

            public void addPart(FeaturePriorityHolder.Basic part) {
                parts.add(part);
            }

            /**
             * Used to match features with PriorityHolder. We need to do a
             * little more work here, to build up an image that matches the all
             * of the attributes set on this feature. So if the feature matches
             * at the first level, walk through the parts and draw on top of it.
             * 
             * @param facc
             * @param fci
             * @param row
             * @return true if feature matches conditions of PriorityHolder.
             */
            public boolean matches(String facc, FeatureClassInfo fci, List<Object> row) {
                boolean ret = false;
                int partCount = 0;

                char type = fci.getFeatureType();
                if (type == CoverageTable.EPOINT_FEATURETYPE || type == CoverageTable.CPOINT_FEATURETYPE) {
                    type = CoverageTable.UPOINT_FEATURETYPE;
                }

                /**
                 * Building up the current image based on the attributes. Give
                 * each part a chance to evaluate whether it should be rendered
                 * into the image or not.
                 */
                if (facc.equals(this.facc) && this.type == type) {

                    BufferedImage image = new BufferedImage(dim, dim, BufferedImage.TYPE_INT_ARGB);
                    Graphics2D g = (Graphics2D) image.getGraphics();
                    for (FeaturePriorityHolder.Basic part : parts) {

                        if (part.expression != null) {
                            boolean partRet = part.expression.evaluate(fci, row);

                            if (partRet) {
                                Image im = part.getIcon();
                                g.drawImage(im, (int) (part.xoffPercent * dim), (int) (part.yoffPercent * dim), this);
                                ret = true;
                                partCount++;
                            }

                        } else {
                            Image im = part.getIcon();
                            g.drawImage(im, (int) (part.xoffPercent * dim), (int) (part.yoffPercent * dim), this);
                            ret = true;
                            partCount++;
                        }

                    }

                    icon = image;
                }

                return ret;
            }

            /**
             * Used to match symbol codes with PriorityHolder.
             * 
             * @param facc
             * @param type
             * @param conditions
             * @return true if feature matches conditions of PriorityHolder.
             */
            public boolean matches(String facc, char type, String conditions, String symbolFileName, String size, String xoff,
                                   String yoff) {
                boolean basicMatch = this.facc.equals(facc) && type == this.type;

                boolean conditionMatch = false;

                /**
                 * We need to step through each part so that each part can find
                 * it's symbol.
                 */
                for (FeaturePriorityHolder.Basic part : parts) {

                    conditionMatch = part.matches(facc, type, conditions, symbolFileName, size, xoff, yoff);

                    if (conditionMatch) {
                        break;
                    }
                }

                return basicMatch && conditionMatch;
            }

            public void add(OMGraphic omg) {

                if (list == null) {
                    list = new OMGraphicList();
                }

                if (icon != null) {
                    if (omg instanceof OMPoint.Image) {
                        ((OMPoint.Image) omg).setImage(icon);
                        list.add(omg);
                    } else if (omg instanceof OMRasterObject) {
                        ((OMRasterObject) omg).setImage(icon);
                        list.add(omg);
                    }
                }
            }

            /*
             * (non-Javadoc)
             * 
             * @see java.awt.image.ImageObserver#imageUpdate(java.awt.Image,
             * int, int, int, int, int)
             */
            public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
                return false;// all set
            }
        }
    }

}