package com.bbn.openmap.layer.location.csv;

import com.bbn.openmap.omGraphics.OMCircle;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.proj.Length;
import com.bbn.openmap.util.PropUtils;
import com.bbn.openmap.util.quadtree.QuadTree;
import java.util.List;
import java.util.Properties;

/**
 * CSVCircleHandler is a trivial extension to CSVLocationHandler that draws 
 * circles on the map instead of locations. Configuring it is similar to
 * CSVLocationLayer, except you must additionally provide a "radiusIndex"
 * property describing which CSV column the circle radius is in, measured in KM
 * 
 * <P>
 * Note that this *only* draws circles. If you want points/icons/labels/etc
 * drawn simply add another layer configured to draw them as you desire. The 
 * expected use case is to have the same CSV file used for both layers, and
 * simply ignore the "Radius" column for the location layer.
 * 
 * <pre>
 * 
 *       locationLayer.locationHandlers=csvcirclehandler 
 *       csvcirclehandler.class=com.bbn.openmap.layer.location.csv.CSVCircleHandler
 *       csvcirclehandler.locationFile=/data/worldpts/WorldLocs_point.csv
 *       csvcirclehandler.csvFileHasHeader=true
 *       csvcirclehandler.latIndex=8
 *       csvcirclehandler.lonIndex=10
 *       csvcirclehandler.radiusIndex=6
 * </pre>
 */
public class CSVCircleHandler extends CSVLocationHandler {
    /**
     * Property to use to designate the column of the CSV file to use as radius, specified in 
     */
    public static final String CircleRadiusProperty = "radiusIndex";
    
    /** Index of column in CSV to use as the circle radius */
    protected int radiusIndex = -1;

    @Override
    public void setProperties(String prefix, Properties properties) {
        super.setProperties(prefix, properties);
        radiusIndex = PropUtils.intFromProperties(properties, prefix + "." + CircleRadiusProperty, -1);
    }

    @Override
    public Properties getProperties(Properties props) {
        props = super.getProperties(props);
        String prefix = PropUtils.getScopedPropertyPrefix(this);
        props.put(prefix + CircleRadiusProperty, (radiusIndex != -1 ? Integer.toString(radiusIndex) : ""));
        return props;
    }
    
    @Override
    public Properties getPropertyInfo(Properties list) {
        list = super.getPropertyInfo(list);
        list.put(CircleRadiusProperty, "The column index, in the location file, of circle radius.");
        return list;
    }

    @Override
    protected boolean checkIndexSettings() {
        if (radiusIndex == -1) {
            logger.warning("CSVCircleLocationHandler: createData(): Index properties for Circle are not set properly!"
                    + " Circle index:" + radiusIndex);
            return false;
        }
        return super.checkIndexSettings();
    }

    @Override
    protected void createLocation(List recordList, QuadTree<OMGraphic> qt) {
        final double recordlat = tokenToDouble(recordList, latIndex, 0.0);
        final double recordlon = tokenToDouble(recordList, lonIndex, 0.0, eastIsNeg);
        final double recordradius = tokenToDouble(recordList, radiusIndex, 1.0);
        
        final OMCircle circle = new OMCircle(recordlat, recordlon, Length.KM.toRadians(recordradius));
        getLocationDrawingAttributes().setTo(circle);
        qt.put(recordlat, recordlon, circle);
    }
}
