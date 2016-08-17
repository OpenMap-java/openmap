package com.bbn.openmap.layer.terrain.MultiLOS;

import java.awt.geom.Point2D;

import com.bbn.openmap.dataAccess.dted.DTEDDirectoryHandler;
import com.bbn.openmap.dataAccess.dted.DTEDFrameCache;
import com.bbn.openmap.event.ProgressSupport;
import com.bbn.openmap.gui.ProgressListenerGauge;
import com.bbn.openmap.layer.OMGraphicHandlerLayer;
import com.bbn.openmap.omGraphics.OMCircle;
import com.bbn.openmap.omGraphics.OMColor;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.OMPoint;
import com.bbn.openmap.proj.Length;
import com.bbn.openmap.proj.Planet;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.proj.coords.LatLonPoint;
import com.bbn.openmap.tools.terrain.LOSGenerator;
import com.bbn.openmap.util.PropUtils;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * An OpenMap Layer to display an LOS map for a number of viewpoints, from a given altitude
 * 
 * <pre><code>

 ############################
 # Example Properties for a MultiLOS layer
 multilos.class=com.bbn.openmap.layer.terrain.MultiLOS.MultiLOSLayer
 multilos.prettyName=MultiLOS
 
 # Properties for the los calculations
 # Altitude is MSL. This is the default altitude if it is not specified on a viewpoint
 multilos.altitude=500
 multilos.altitudeUnits=M
 # Max viable sensor distance, in KM
 multilos.maxRangeKM=200
 # viewpoints: Semicolon-separated list of lat,lon pairs separated by commas.
 # Optionally, lat,lon may have a third item, ",altitude" which overrides the default altitude
 multilos.viewPoints=22.3,116.0;24.3,119.7,100
 # Whether to outline horizons, and show view points
 multilos.showHorizons=TRUE
 multilos.showViewPoints=TRUE
 # color of fill. Leaving out means we won't fill that type [canSee or canNotSee]
 multilos.canSeeColor=4400ff00
 multilos.canNotSeeColor=44ff0000
 # DTED
 multilos.dtedLevel=0
 multilos.dtedDir=/data/dted/dted0
 ############################
 
 </code></pre>
 * 
 * @author Gary Briggs 
 */
public class MultiLOSLayer extends OMGraphicHandlerLayer {

    // Properties from the user
    double altitude = 500.0;
    Length altitudeUnits = Length.METER;
    Map<LatLonPoint, Double> viewPoints = new HashMap<LatLonPoint, Double>();
    boolean showHorizons = true;
    boolean showViewPoints = true;
    String dtedDir = "/data/dted/dted0";
    int dtedLevel = 0;
    Color canSeeColor = new Color(0, 255, 0, 100);
    Color canNotSeeColor = null;
    double maxRangeKM = 200;
    
    public final static String altProperty = "altitude";
    public final static String altUnitsProperty = "altitudeUnits";
    public final static String viewPointsProperty = "viewPoints";
    public final static String showHorizonsProperty = "showHorizons";
    public final static String showViewPointsProperty = "showViewPoints";
    public final static String canSeeColorProperty = "canSeeColor";
    public final static String canNotSeeColorProperty = "canNotSeeColor";
    public final static String dtedLevelProperty = "dtedLevel";
    public final static String dtedDirProperty = "dtedDir";
    public final static String maxRangeKMProperty = "maxRangeKM";
    
    // Internal use only members
    DTEDFrameCache dted;
    ProgressSupport progressSupport = null;
    
    public MultiLOSLayer() {
        dted = new DTEDFrameCache();
    }
    
    @Override
    public void setProperties(String prefix, Properties props) {
        super.setProperties(prefix, props);
        String realPrefix = PropUtils.getScopedPropertyPrefix(this);

        altitude = PropUtils.doubleFromProperties(props, realPrefix + altProperty, altitude);
        altitudeUnits = Length.get(props.getProperty(realPrefix + altUnitsProperty, altitudeUnits.getAbbr()));
        maxRangeKM = PropUtils.doubleFromProperties(props, realPrefix + maxRangeKMProperty, maxRangeKM);
        
        showHorizons = PropUtils.booleanFromProperties(props, realPrefix + showHorizonsProperty, showHorizons);
        showViewPoints = PropUtils.booleanFromProperties(props, realPrefix + showViewPointsProperty, showViewPoints);
       
        dtedLevel = PropUtils.intFromProperties(props, realPrefix + dtedLevelProperty, dtedLevel);
        dtedDir = props.getProperty(realPrefix + dtedDirProperty, dtedDir);
        dted.addDTEDDirectoryHandler(new DTEDDirectoryHandler(dtedDir));
        
        String csc = props.getProperty(realPrefix + canSeeColorProperty, 
                (null == canSeeColor?null:canSeeColor.toString()));
        if(null == csc) {
            canSeeColor = null;
        } else {
            canSeeColor = PropUtils.parseColor(csc, true);
        }
        
        String cnsc = props.getProperty(realPrefix + canNotSeeColorProperty, 
                (null == canNotSeeColor?null:canNotSeeColor.toString()));
        if(null == cnsc) {
            canNotSeeColor = null;
        } else {
            canNotSeeColor = PropUtils.parseColor(cnsc, true);
        }
        
        // Viewpoints are semicolon-separated lat,lon pairs separated by commas
        viewPoints = new HashMap<LatLonPoint, Double>();
        String viewPointSource = props.getProperty(realPrefix + viewPointsProperty, null);
        String[] viewPointStrings = viewPointSource.split(";");
        for(String s : viewPointStrings) {
            String trimmed = s.trim();
            if(0 == trimmed.length()) {
                continue;
            }
            String[] oneLL = trimmed.split(",");
            if(oneLL.length != 2 && oneLL.length != 3) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Cannot parse \"" + trimmed + "\" into a single LL pair");
                continue;
            }
            try {
                Double lat = Double.valueOf(oneLL[0]);
                Double lon = Double.valueOf(oneLL[1]);
                double thisAlt = altitude;
                if(3 == oneLL.length) {
                    thisAlt = Double.valueOf(oneLL[2]);
                }
                viewPoints.put(new LatLonPoint.Double(lat, lon, false), thisAlt);
            } catch(NumberFormatException ex) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Cannot parse \"" + trimmed + "\" numerically");
            }
        }
        if(null == progressSupport) {
            progressSupport = new ProgressSupport(this);
            progressSupport.add(new ProgressListenerGauge("MultiLOS"));
        }
    }
    
    @Override
    public Properties getProperties(Properties props) {
        props = super.getProperties(props);

        String prefix = PropUtils.getScopedPropertyPrefix(this);

        props.put(prefix + "class", this.getClass().getName());
        props.put(prefix + altProperty, altitude);
        props.put(prefix + altUnitsProperty, altitudeUnits.getAbbr());
        props.put(prefix + showHorizonsProperty, Boolean.toString(showHorizons));
        props.put(prefix + showViewPointsProperty, Boolean.toString(showViewPoints));
        if(null != canSeeColor) {
            props.put(prefix + canSeeColorProperty, canSeeColor.toString());
        } 
        if(null != canNotSeeColor) {
            props.put(prefix + canNotSeeColorProperty, canNotSeeColor.toString());
        }
        props.put(prefix + dtedLevelProperty, dtedLevel);
        props.put(prefix + dtedDirProperty, dtedDir);
        props.put(prefix + maxRangeKMProperty, maxRangeKM);

        StringBuilder vp_prop = new StringBuilder();
        for(Entry<LatLonPoint, Double> ent : viewPoints.entrySet()) {
            LatLonPoint vp = ent.getKey();
            Double thisAlt = ent.getValue();
            
            vp_prop.append(vp.getLatitude());
            vp_prop.append(",");
            vp_prop.append(vp.getLongitude());
            vp_prop.append(",");
            vp_prop.append(thisAlt);
            vp_prop.append(";");
        }
        props.put(prefix + viewPointsProperty, vp_prop.toString());

        return props;
    }

    @Override
    public Properties getPropertyInfo(Properties list) {
        list = super.getPropertyInfo(list);

        list.put(altProperty, "Default altitude for viewpoints, MSL");
        list.put(altUnitsProperty, "Units for altitude");
        list.put(showHorizonsProperty, "Whether to indicate horizons");
        list.put(showViewPointsProperty, "Whether to indicate viewpoints");
        list.put(canSeeColorProperty, "Color to indicate if a point can be seen.");
        list.put(canNotSeeColorProperty, "Color to indicate if a point cannot be seen. Leave blank to not include points");
        list.put(dtedLevelProperty, "DTED Level");
        list.put(dtedDirProperty, "DTED data directory");
        list.put(maxRangeKMProperty, "Maximum sensor range, in KM");
        list.put(viewPointsProperty, "Semicolon-separated list of lat,lon pairs with option ',alt' suffix");
        
        return list;
    }
    @Override
    public OMGraphicList prepare() {
        OMGraphicList l = new OMGraphicList();
        
        if(showHorizons) {
            for(Entry<LatLonPoint, Double> ent : viewPoints.entrySet()) {
                LatLonPoint vp = ent.getKey();
                Double thisAlt = ent.getValue();
                final double horizonKM = Length.KM.fromRadians(calculateHorizonDistRad(thisAlt));
                OMCircle circ = new OMCircle(vp.getLatitude(), vp.getLongitude(), horizonKM, Length.KM);
                circ.setLinePaint(Color.BLACK);
                l.add(circ);
            }
        }
        if(showViewPoints) {
            for(LatLonPoint vp : viewPoints.keySet()) {
                OMPoint p = new OMPoint(vp.getLatitude(), vp.getLongitude());
                l.add(p);
            }
        }
        createMultiLOS(l);
        l.generate(getProjection());
        return l;
    }

    @Override
    public Component getGUI() {
        JPanel pan = new JPanel(new GridLayout(0, 2, 2, 2));
        
        final JButton setAltsButton = new JButton("Set all viewpoint alts to (" + altitudeUnits.getAbbr() + "):");
        pan.add(setAltsButton);
        final JSpinner altSpinner = new JSpinner(new SpinnerNumberModel(altitude, 0.0, 10000.0, 1.0));
        pan.add(altSpinner);
        
        pan.add(new JLabel("Max Range (KM)"));
        final JSpinner maxRangeSpinner = new JSpinner(new SpinnerNumberModel(maxRangeKM, 0.0, 10000.0, 20.0));
        pan.add(maxRangeSpinner);
        
        pan.add(new JLabel("Show horizons"));
        final JCheckBox showHorizonCB = new JCheckBox((String)null, showHorizons);
        pan.add(showHorizonCB);
        
        pan.add(new JLabel("Show viewpoints"));
        final JCheckBox showViewPointsCB = new JCheckBox((String)null, showViewPoints);
        pan.add(showViewPointsCB);
        
        ActionListener altAl = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Double newAlt = (Double)altSpinner.getValue();
                for(LatLonPoint vp : viewPoints.keySet()) {
                    viewPoints.put(vp, newAlt);
                }
                doPrepare();
            }
        };
        setAltsButton.addActionListener(altAl);
        
        final ChangeListener cl = new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                maxRangeKM = (Double)maxRangeSpinner.getValue();
                doPrepare();
            }
        };
        final ActionListener al = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showHorizons = showHorizonCB.isSelected();
                showViewPoints = showViewPointsCB.isSelected();
                doPrepare();
            }
        };
        
        maxRangeSpinner.addChangeListener(cl);
        showHorizonCB.addActionListener(al);
        showViewPointsCB.addActionListener(al);
        return pan;
    }
    
    public void createMultiLOS(OMGraphicList l) {
        LOSGenerator los = new LOSGenerator(dted);

        Projection proj = getProjection();
        
        Point2D ll1 = proj.getUpperLeft();
        Point2D ll2 = proj.getLowerRight();
        
        double dLon = (ll2.getX() - ll1.getX()) / (proj.getWidth() / 4);
        double dLat = (ll1.getY() - ll2.getY()) / (proj.getHeight() / 4);
        
        double maxRangeRad = Length.KM.toRadians(maxRangeKM);
        
        int checkedPoints = 0;
        int seenPoints = 0;
        
        final int maxProgress = (int) ((ll2.getX() - ll1.getX())/dLon);
        int currProgress = 0;
        final String taskName = "MultiLOS Render";
//        progressSupport.fireUpdate(ProgressEvent.START, taskName, currProgress, maxProgress);
        for (double testLon = ll1.getX(); testLon < ll2.getX(); testLon+=dLon) {
            currProgress++;
            // Need it to be final so it can be seen from the inner subclass
//            final int progress = currProgress;
//            SwingUtilities.invokeLater(new Runnable() {
//                public void run() {
//                    progressSupport.fireUpdate(ProgressEvent.UPDATE, taskName, progress, maxProgress);
//                }
//            });
            
            for (double testLat = ll2.getY(); testLat < ll1.getY(); testLat+=dLat) {
                
                if(Thread.currentThread().isInterrupted()) {
                    // eg, if we're mid-render and someone moves the map again
                    return;
                }
                
                checkedPoints++;
                
                LatLonPoint testp = new LatLonPoint.Double(testLat, testLon);
                Point2D xyp = proj.forward(testp);
                
                int elevation = dted.getElevation((float) testLat, (float) testLon, dtedLevel);
                if(elevation > 0) {
                    int losCount = 0;

                    for (Entry<LatLonPoint, Double> ent : viewPoints.entrySet()) {
                        LatLonPoint oneVP = ent.getKey();
                        Double thisAlt = ent.getValue();
                        double thisAltM = Length.METER.fromRadians(altitudeUnits.toRadians(thisAlt));
                        
                        final double distanceRad = oneVP.distance(testp);
                        
                        if(distanceRad > maxRangeRad) {
                            // Broadphase - skip anything outside our sensor horizon
                            continue;
                        }
//                        
                        Point2D tXY = proj.forward(oneVP.getLatitude(), oneVP.getLongitude());
                        int numPixBetween = (int) (Math.sqrt(
                                Math.pow(tXY.getX() - xyp.getX(), 2) +
                                        Math.pow(tXY.getY() - xyp.getY(), 2)
                                ) / 5);
                        
                        if (los.isLOS(oneVP, (int) thisAltM, false, testp, 0,
                                (int) numPixBetween)) {
                            losCount++;
                            // If one can see, that's sufficient for this layer's see/not see metric
                            break;
                        }
                    }

                    if(0 < losCount && null != canSeeColor) {
                        OMPoint p = new OMPoint(testLat, testLon);
                        p.setLinePaint(OMColor.clear);
                        p.setFillPaint(canSeeColor);
                        l.add(p);
                        seenPoints++;
                    } else if(0 == losCount && null != canNotSeeColor) {
                        OMPoint p = new OMPoint(testLat, testLon);
                        p.setLinePaint(OMColor.clear);
                        p.setFillPaint(canNotSeeColor);
                        l.add(p);
                    }
                } else {
                    // Skipped a point because it's elevation was zero or smaller
                    // System.out.println("elevation " + elevation);
                }
            }
        }
//        progressSupport.fireUpdate(ProgressEvent.DONE, taskName, currProgress, maxProgress);
        System.out.println("Last Render, " + seenPoints + "/" + checkedPoints + " points seen/total");
    }

    private double calculateHorizonDistRad(Double alt) {
        final double horizonDistM = Math.sqrt((2 * Planet.wgs84_earthEquatorialRadiusMeters_D * alt) + (alt * alt));
        final double horizonDistRad = Length.METER.toRadians(horizonDistM);
        return horizonDistRad;
    }

}
