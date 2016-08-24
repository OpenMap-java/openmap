package com.bbn.openmap.layer.terrain.MultiLOS;

import java.awt.geom.Point2D;

import com.bbn.openmap.dataAccess.dted.DTEDDirectoryHandler;
import com.bbn.openmap.dataAccess.dted.DTEDFrameCache;
import com.bbn.openmap.layer.OMGraphicHandlerLayer;
import com.bbn.openmap.omGraphics.OMCircle;
import com.bbn.openmap.omGraphics.OMColor;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.OMPoint;
import com.bbn.openmap.omGraphics.OMText;
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
import java.util.ArrayList;
import java.util.List;
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
 # Max viable sensor distance
 # This is the default range if it is not specified on a viewpoint
 multilos.maxRange=200
 multilos.maxRangeUnits=KM
 # viewpoints: Semicolon-separated list of lat,lon pairs separated by commas.
 # lat,lon[,alt[,sensorRange]]
 multilos.viewPoints=22.3,116.0;24.3,119.7,100,1000
 # If you don't want to specify a list of viewpoints, use this to specify a series of lines:
 # semicolon-separated list, StartLat,StartLon,EndLat,EndLon,NumPointsBetweenStartEnd
 multilos.viewPointLines=
 # Whether to indicate viewpoint properties
 multilos.showHorizons=TRUE
 multilos.showViewPoints=TRUE
 multilos.showMaxRanges=TRUE
 multilos.showNumberPoints=TRUE
 # LOS is calculated per-screen-point, with
 #  n_samples = pixels_between_viewpoint_and_screen_point / pixelSkip
 # Number of pixels per point - high numbers are faster/lower resolution. don't go below 1
 multilos.pixelsPerPoint=2
 # Multiplier to make rendering faster but less accurate. 1 == "slowest, most accurate"
 multilos.pixelSkip=3
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
    // Class that represents a viewpoint
    private class MultiLOSViewPoint {
        LatLonPoint p;
        double altitude;
        double maxRange;

        public MultiLOSViewPoint(LatLonPoint p, double altitude, double maxRange) {
            this.p = p;
            this.altitude = altitude;
            this.maxRange = maxRange;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(p.getLatitude());
            sb.append(",");
            sb.append(p.getLongitude());
            sb.append(",");
            sb.append(altitude);
            sb.append(",");
            sb.append(maxRange);
            return sb.toString();
        }
        
        
    };
    
    // Properties from the user
    double altitude = 500.0;
    Length altitudeUnits = Length.METER;
    List<MultiLOSViewPoint> viewPoints = new ArrayList<MultiLOSViewPoint>();
    boolean showHorizons = true;
    boolean showViewPoints = true;
    boolean showMaxRanges = true;
    boolean showNumberPoints = true;
    String dtedDir = "/data/dted/dted0";
    int dtedLevel = 0;
    int pixelsPerPoint = 2;
    double pixelSkip = 2.0;
    Color canSeeColor = new Color(0, 255, 0, 100);
    Color canNotSeeColor = null;
    double maxRange = 200;
    Length maxRangeUnits = Length.KM;
    
    public final static String altProperty = "altitude";
    public final static String altUnitsProperty = "altitudeUnits";
    public final static String viewPointsProperty = "viewPoints";
    public final static String viewPointLinesProperty = "viewPointLines";
    public final static String showHorizonsProperty = "showHorizons";
    public final static String showViewPointsProperty = "showViewPoints";
    public final static String showMaxRangesProperty = "showMaxRanges";
    public final static String canSeeColorProperty = "canSeeColor";
    public final static String canNotSeeColorProperty = "canNotSeeColor";
    public final static String dtedLevelProperty = "dtedLevel";
    public final static String dtedDirProperty = "dtedDir";
    public final static String maxRangeProperty = "maxRange";
    public final static String maxRangeUnitsProperty = "maxRangeUnits";
    public final static String pixelsPerPointProperty = "pixelsPerPoint";
    public final static String pixelSkipProperty = "pixelSkip";
    public final static String showNumberPointsProperty = "showNumberPoints";
    
    // Internal use only members
    DTEDFrameCache dted;
    
    public MultiLOSLayer() {
        dted = new DTEDFrameCache();
    }
    
    @Override
    public void setProperties(String prefix, Properties props) {
        super.setProperties(prefix, props);
        String realPrefix = PropUtils.getScopedPropertyPrefix(this);

        altitude = PropUtils.doubleFromProperties(props, realPrefix + altProperty, altitude);
        altitudeUnits = Length.get(props.getProperty(realPrefix + altUnitsProperty, altitudeUnits.getAbbr()));
        maxRange = PropUtils.doubleFromProperties(props, realPrefix + maxRangeProperty, maxRange);
        maxRangeUnits = Length.get(props.getProperty(realPrefix + maxRangeUnitsProperty, maxRangeUnits.getAbbr()));
        
        showHorizons = PropUtils.booleanFromProperties(props, realPrefix + showHorizonsProperty, showHorizons);
        showMaxRanges = PropUtils.booleanFromProperties(props, realPrefix + showMaxRangesProperty, showMaxRanges);
        showViewPoints = PropUtils.booleanFromProperties(props, realPrefix + showViewPointsProperty, showViewPoints);
        showNumberPoints = PropUtils.booleanFromProperties(props, realPrefix + showNumberPointsProperty, showNumberPoints);
        
        pixelSkip = PropUtils.doubleFromProperties(props, realPrefix + pixelSkipProperty, pixelSkip);
        pixelsPerPoint = Math.max(1, PropUtils.intFromProperties(props, realPrefix + pixelsPerPointProperty, pixelsPerPoint));
        
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
        
        viewPoints = new ArrayList<MultiLOSViewPoint>();
        
        // Viewpoints are semicolon-separated lat,lon pairs separated by commas
        String viewPointSource = props.getProperty(realPrefix + viewPointsProperty, null);
        if(null != viewPointSource) {
            String[] viewPointStrings = viewPointSource.split(";");
            for(String s : viewPointStrings) {
                String trimmed = s.trim();
                if(0 == trimmed.length()) {
                    continue;
                }
                String[] oneLL = trimmed.split(",");
                if(oneLL.length < 2) {
                    Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Error parsing \"" + trimmed + "\": must have at least lat,lon ");
                    continue;
                }
                try {
                    Double lat = Double.valueOf(oneLL[0]);
                    Double lon = Double.valueOf(oneLL[1]);
                    double thisAlt = altitude;
                    double thisMaxRange = maxRange;
                    if(3 <= oneLL.length) {
                        thisAlt = Double.valueOf(oneLL[2]);
                    }
                    if(4 <= oneLL.length) {
                        thisMaxRange = Double.valueOf(oneLL[3]);
                    }
                    viewPoints.add(new MultiLOSViewPoint(new LatLonPoint.Double(lat, lon, false), thisAlt, thisMaxRange));
                } catch(NumberFormatException ex) {
                    Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Cannot parse \"" + trimmed + "\" numerically");
                }
            }
        }
        
        
        String viewPointLinesSource = props.getProperty(realPrefix + viewPointLinesProperty, null);
        if(null != viewPointLinesSource) {
            for(String oneViewPointLine : viewPointLinesSource.split(";")) {
                String trimmed = oneViewPointLine.trim();
                if(0 == trimmed.length()) {
                    continue;
                }
                String[] linePieces = trimmed.split(",");
                if(5 != linePieces.length) {
                    Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "ViewPointsLine \"" + trimmed + "\" must be formatted l,l,l,l,n");
                    continue;
                }

                try {
                    Double startLat = Double.valueOf(linePieces[0]);
                    Double startLon = Double.valueOf(linePieces[1]);
                    Double endLat = Double.valueOf(linePieces[2]);
                    Double endLon = Double.valueOf(linePieces[3]);
                    // Always do the two end points. Bonus: we can skip worrying about div0
                    int piececnt = 2 + Integer.valueOf(linePieces[4]);

                    double dLat = ((endLat-startLat)/piececnt);
                    double dLon = ((endLon-startLon)/piececnt);

                    for(int i = 0; i < piececnt; i++) {
                        double lat = startLat + (i * dLat);
                        double lon = startLon + (i * dLon);
                        LatLonPoint.Double p = new LatLonPoint.Double(lat, lon, false);
                        viewPoints.add(new MultiLOSViewPoint(p, altitude, maxRange));
                    }
                } catch(NumberFormatException ex) {
                    Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Cannot parse \"" + trimmed + "\" numerically");
                }
            }
        }
    }
    
    @Override
    public Properties getProperties(Properties props) {
        props = super.getProperties(props);

        String prefix = PropUtils.getScopedPropertyPrefix(this);

        props.put(prefix + "class", this.getClass().getName());
        props.put(prefix + altProperty, altitude);
        props.put(prefix + altUnitsProperty, altitudeUnits.getAbbr());
        props.put(prefix + showNumberPointsProperty, Boolean.toString(showNumberPoints));
        props.put(prefix + pixelSkipProperty, pixelSkip);
        props.put(prefix + pixelsPerPointProperty, pixelsPerPoint);
        props.put(prefix + showHorizonsProperty, Boolean.toString(showHorizons));
        props.put(prefix + showMaxRangesProperty, Boolean.toString(showMaxRanges));
        props.put(prefix + showViewPointsProperty, Boolean.toString(showViewPoints));
        if(null != canSeeColor) {
            props.put(prefix + canSeeColorProperty, canSeeColor.toString());
        } 
        if(null != canNotSeeColor) {
            props.put(prefix + canNotSeeColorProperty, canNotSeeColor.toString());
        }
        props.put(prefix + dtedLevelProperty, dtedLevel);
        props.put(prefix + dtedDirProperty, dtedDir);
        props.put(prefix + maxRangeProperty, maxRange);
        props.put(prefix + maxRangeUnitsProperty, maxRangeUnits.getAbbr());

        StringBuilder vp_prop = new StringBuilder();
        for(MultiLOSViewPoint mlvp : viewPoints) {
            vp_prop.append(mlvp.toString());
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
        list.put(showMaxRangesProperty, "Whether to indicate max ranges");
        list.put(showViewPointsProperty, "Whether to indicate viewpoints");
        list.put(showNumberPointsProperty, "Whether to indicate numbers next to points");
        list.put(canSeeColorProperty, "Color to indicate if a point can be seen.");
        list.put(canNotSeeColorProperty, "Color to indicate if a point cannot be seen. Leave blank to not include points");
        list.put(dtedLevelProperty, "DTED Level");
        list.put(dtedDirProperty, "DTED data directory");
        list.put(pixelsPerPointProperty, "Pixels per point");
        list.put(pixelsPerPointProperty, "Pixel Skip");
        list.put(maxRangeProperty, "Maximum sensor range");
        list.put(maxRangeUnitsProperty, "Maximum sensor range units");
        list.put(viewPointsProperty, "Semicolon-separated list of lat,lon pairs with option ',alt' suffix");
        
        return list;
    }
    @Override
    public OMGraphicList prepare() {
        OMGraphicList l = new OMGraphicList();
        
        if(showHorizons) {
            for(MultiLOSViewPoint mlvp : viewPoints) {
                LatLonPoint vp = mlvp.p;
                final double thisAlt = mlvp.altitude;
                final double thisAltM = Length.METER.fromRadians(altitudeUnits.toRadians(thisAlt));
                final double horizonRad = calculateHorizonDistRad(thisAltM);
                OMCircle circ = new OMCircle(vp.getLatitude(), vp.getLongitude(), horizonRad, Length.RADIAN);
                circ.setLinePaint(Color.BLACK);
                l.add(circ);
            }
        }
        if(showMaxRanges) {
            for(MultiLOSViewPoint mlvp : viewPoints) {
                LatLonPoint vp = mlvp.p;
                final double maxRangeRad = maxRangeUnits.toRadians(mlvp.maxRange);
                OMCircle circ = new OMCircle(vp.getLatitude(), vp.getLongitude(), maxRangeRad, Length.RADIAN);
                circ.setLinePaint(Color.GRAY);
                l.add(circ);
            }
        }
        if(showViewPoints) {
            for(int i = 0; i < viewPoints.size(); i++) {
                MultiLOSViewPoint mlvp = viewPoints.get(i);
                LatLonPoint vp = mlvp.p;
                OMPoint p = new OMPoint(vp.getLatitude(), vp.getLongitude());
                l.add(p);
                if(showNumberPoints) {
                    OMText t = new OMText(vp.getLatitude(), vp.getLongitude(), Integer.toString(i), OMText.JUSTIFY_LEFT);
                    l.add(t);
                }
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
        final SpinnerNumberModel altSpinnerModel = new SpinnerNumberModel(altitude, 0.0, 1000000.0, 1.0);
        final JSpinner altSpinner = new JSpinner(altSpinnerModel);
        pan.add(altSpinner);
        
        final JButton setRangesButton = new JButton("Set all viewpoint ranges to (" + maxRangeUnits.getAbbr() + "):");
        pan.add(setRangesButton);
        final SpinnerNumberModel maxRangeSpinnerModel = new SpinnerNumberModel(maxRange, 0.0, 1000000.0, 20.0);
        final JSpinner maxRangeSpinner = new JSpinner(maxRangeSpinnerModel);
        pan.add(maxRangeSpinner);
        
        pan.add(new JLabel("Pixel Skip"));
        final SpinnerNumberModel pixelSkipSpinnerModel = new SpinnerNumberModel(pixelSkip, 0.01, 1000.0, 1);
        final JSpinner pixelSkipSpinner = new JSpinner(pixelSkipSpinnerModel);
        pan.add(pixelSkipSpinner);
        
        pan.add(new JLabel("Pixels per Point"));
        final SpinnerNumberModel pixelsPerPointSpinnerModel = new SpinnerNumberModel(pixelsPerPoint, 1, 1000, 1);
        final JSpinner pixelPerPointSpinner = new JSpinner(pixelsPerPointSpinnerModel);
        pan.add(pixelPerPointSpinner);
        
        pan.add(new JLabel("Show horizons"));
        final JCheckBox showHorizonCB = new JCheckBox((String)null, showHorizons);
        pan.add(showHorizonCB);
        
        pan.add(new JLabel("Show max ranges"));
        final JCheckBox showMaxRangesCB = new JCheckBox((String)null, showMaxRanges);
        pan.add(showMaxRangesCB);
        
        pan.add(new JLabel("Show viewpoints"));
        final JCheckBox showViewPointsCB = new JCheckBox((String)null, showViewPoints);
        pan.add(showViewPointsCB);
        
        pan.add(new JLabel("Show Point Numbering"));
        final JCheckBox showNumberPointsCB = new JCheckBox((String)null, showNumberPoints);
        pan.add(showNumberPointsCB);
        
        ActionListener altAl = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Double newAlt = altSpinnerModel.getNumber().doubleValue();
                for(MultiLOSViewPoint mlvp : viewPoints) {
                    mlvp.altitude = newAlt;
                }
                doPrepare();
            }
        };
        setAltsButton.addActionListener(altAl);
        
        ActionListener rangeAl = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Double newMaxRange = maxRangeSpinnerModel.getNumber().doubleValue();
                for(MultiLOSViewPoint mlvp : viewPoints) {
                    mlvp.maxRange = newMaxRange;
                }
                doPrepare();
            }
        };
        setRangesButton.addActionListener(rangeAl);

        final ActionListener al = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showNumberPoints = showNumberPointsCB.isSelected();
                showHorizons = showHorizonCB.isSelected();
                showMaxRanges = showMaxRangesCB.isSelected();
                showViewPoints = showViewPointsCB.isSelected();
                doPrepare();
            }
        };
        
        final ChangeListener spinnerListener = new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                pixelsPerPoint = pixelsPerPointSpinnerModel.getNumber().intValue();
                pixelSkip = pixelSkipSpinnerModel.getNumber().doubleValue();
                doPrepare();
            }
        };
        pixelPerPointSpinner.addChangeListener(spinnerListener);
        pixelSkipSpinner.addChangeListener(spinnerListener);
        
        showNumberPointsCB.addActionListener(al);
        showHorizonCB.addActionListener(al);
        showMaxRangesCB.addActionListener(al);
        showViewPointsCB.addActionListener(al);
        return pan;
    }
    
    public void createMultiLOS(OMGraphicList l) {
        LOSGenerator los = new LOSGenerator(dted);

        Projection proj = getProjection();
        
        int checkedPoints = 0;
        int seenPoints = 0;
        
        for (int x = 0; x < proj.getWidth(); x+=pixelsPerPoint) {
//            System.out.println(String.format("MultiLOS Render: %d/%d", x, proj.getWidth()));
            for (int y = 0; y < proj.getHeight(); y+=pixelsPerPoint) {
                if(Thread.currentThread().isInterrupted()) {
                    // eg, if we're mid-render and someone moves the map again
                    return;
                }
                
                checkedPoints++;
                
                LatLonPoint testp = new LatLonPoint.Double();
                proj.inverse(x, y, testp);
                double testLat = testp.getLatitude();
                double testLon = testp.getLongitude();
                
                int elevation = dted.getElevation((float) testLat, (float) testLon, dtedLevel);
                if(elevation > 0) {
                    int losCount = 0;

                    for (MultiLOSViewPoint mlvp : viewPoints) {
                        
                        LatLonPoint oneVP = mlvp.p;
                        double thisAlt = mlvp.altitude;
                        double thisMaxRangeRad = maxRangeUnits.toRadians(mlvp.maxRange);
        
                        double thisAltM = Length.METER.fromRadians(altitudeUnits.toRadians(thisAlt));
                        
                        final double distanceRad = oneVP.distance(testp);
                        
                        if(distanceRad > thisMaxRangeRad) {
                            // Broadphase - skip anything outside our sensor horizon
                            continue;
                        }
//                        
                        Point2D tXY = proj.forward(oneVP.getLatitude(), oneVP.getLongitude());
                        int numPixBetween = (int) (Math.sqrt(
                                Math.pow(tXY.getX() - x, 2) +
                                        Math.pow(tXY.getY() - y, 2)
                                ) / pixelSkip);
                        
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
                        p.setRadius(pixelsPerPoint / 2);
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

    private double calculateHorizonDistRad(Double altM) {
        final double horizonDistM = Math.sqrt((2 * Planet.wgs84_earthEquatorialRadiusMeters_D * altM) + (altM * altM));
        final double horizonDistRad = Length.METER.toRadians(horizonDistM);
        return horizonDistRad;
    }

}
