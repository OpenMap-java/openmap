package com.bbn.openmap.layer.terrain.MultiLOS;

import java.awt.geom.Point2D;
import java.util.List;

import com.bbn.openmap.dataAccess.dted.DTEDDirectoryHandler;
import com.bbn.openmap.dataAccess.dted.DTEDFrameCache;
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
import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
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
 *
 * ############################
 * # Example Properties for a MultiLOS layer
 * multilos.class=com.bbn.openmap.layer.terrain.MultiLOS.MultiLOSLayer
 * multilos.prettyName=MultiLOS
 * 
 * # Properties for the los calculations
 * # Altitude, in MSL meters
 * multilos.altitudeM=500
 * # Max viable sensor distance, in KM
 * multilos.maxRangeKM=200
 * # viewpoints: Semicolon-separated list of lat,lon pairs separated by commas
 * multilos.viewPoints=22.3,116.0;24.3,119.7
 * # Whether to outline horizons, and show view points
 * multilos.showHorizons=TRUE
 * multilos.showViewPoints=TRUE
 * # color of fill. Leaving out means we won't fill that type [canSee or canNotSee]
 * multilos.canSeeColor=4400ff00
 * multilos.canNotSeeColor=44ff0000
 * # DTED
 * multilos.dtedLevel=0
 * multilos.dtedDir=/data/dted/dted0
 * ############################
 * 
 * </code></pre>
 * 
 * @author Gary Briggs 
 */
public class MultiLOSLayer extends OMGraphicHandlerLayer {

    // Properties from the user
    double altitudeM = 500.0;
    List<LatLonPoint> viewPoints = new ArrayList<LatLonPoint>();
    boolean showHorizons = true;
    boolean showViewPoints = true;
    String dtedDir = "/data/dted/dted0";
    int dtedLevel = 0;
    Color canSeeColor = new Color(0, 255, 0, 100);
    Color canNotSeeColor = null;
    double maxRangeKM = 200;
    
    public final static String altMProperty = "altitudeM";
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
    
    public MultiLOSLayer() {
        dted = new DTEDFrameCache();
    }
    
    @Override
    public void setProperties(String prefix, Properties props) {
        super.setProperties(prefix, props);
        String realPrefix = PropUtils.getScopedPropertyPrefix(this);

        altitudeM = PropUtils.doubleFromProperties(props, realPrefix + altMProperty, altitudeM);
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
        viewPoints = new ArrayList<LatLonPoint>();
        String viewPointSource = props.getProperty(realPrefix + viewPointsProperty, null);
        String[] viewPointStrings = viewPointSource.split(";");
        for(String s : viewPointStrings) {
            String[] oneLL = s.split(",");
            if(oneLL.length != 2) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Cannot parse \"" + s + "\" into a single LL pair");
                continue;
            }
            try {
                Double lat = Double.valueOf(oneLL[0]);
                Double lon = Double.valueOf(oneLL[1]);
                viewPoints.add(new LatLonPoint.Double(lat, lon, false));
            } catch(NumberFormatException ex) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Cannot parse \"" + s + "\" numerically");
            }
        }
    }
    
    
    @Override
    public synchronized OMGraphicList prepare() {
        OMGraphicList l = new OMGraphicList();
        
        if(showHorizons) {
            final double horizonKM = Length.KM.fromRadians(calculateHorizonDistRad());
            for (LatLonPoint tLoc : viewPoints) {
                OMCircle circ = new OMCircle(tLoc.getLatitude(), tLoc.getLongitude(), horizonKM, Length.KM);
                circ.setLinePaint(Color.BLACK);
                l.add(circ);
            }
        }
        if(showViewPoints) {
            for (LatLonPoint tLoc : viewPoints) {
                OMPoint p = new OMPoint(tLoc.getLatitude(), tLoc.getLongitude());
                l.add(p);
            }
        }
        createMultiLOS(l);
        l.generate(getProjection());
        return l;
    }

    @Override
    public Component getGUI() {
        JPanel pan = new JPanel(new GridLayout(0, 2));
        
        pan.add(new JLabel("Altitude (M): "));
        final JSpinner altSpinner = new JSpinner(new SpinnerNumberModel(altitudeM, 0.0, 10000.0, 20.0));
        pan.add(altSpinner);
        altSpinner.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                altitudeM = (Double)altSpinner.getValue();
                doPrepare();
            }
        });
        
        pan.add(new JLabel("Max Range (KM): "));
        final JSpinner maxRangeSpinner = new JSpinner(new SpinnerNumberModel(maxRangeKM, 0.0, 10000.0, 20.0));
        pan.add(maxRangeSpinner);
        maxRangeSpinner.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                maxRangeKM = (Double)maxRangeSpinner.getValue();
                doPrepare();
            }
        });
        
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
        
        for (double testLon = ll1.getX(); testLon < ll2.getX(); testLon+=dLon) {
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

                    for (LatLonPoint oneVP : viewPoints) {
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
                        
                        if (los.isLOS(oneVP, (int) altitudeM, false, testp, 0,
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
        System.out.println("Last Render, " + seenPoints + "/" + checkedPoints + " points seen/total");
    }

    private double calculateHorizonDistRad() {
        final double horizonDistM = Math.sqrt((2 * Planet.wgs84_earthEquatorialRadiusMeters_D * altitudeM) + (altitudeM * altitudeM));
        final double horizonDistRad = Length.METER.toRadians(horizonDistM);
        return horizonDistRad;
    }

}
