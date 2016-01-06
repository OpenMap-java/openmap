package com.bbn.openmap;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Paint;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Properties;

import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.PropUtils;

/**
 * The MapBeanGradientBackgroundPolicy adds a delicate touch to the background
 * of the map, a slight gradient paint from one point on the map to another.
 * Those points are defined as a percentage of the height and width of the map.
 * This class can be defined in the properties as;
 * 
 * <pre>
 * mapBeanGradientBackgroundPolicy.class=com.bbn.openmap.MapBeanGradientBackgroundPolicy
 * # bright x bright y dark x dark y as percentage of window location
 * mapBeanGradientBackgroundPolicy.brightDarkPercentage=.3 .3 .95 .95
 * # how many times to make brighter than original color.
 * mapBeanGradientBackgroundPolicy.howBright=1
 * # how many times to make darker than original color.
 * mapBeanGradientBackgroundPolicy.howDark=2
 * </pre>
 * 
 * @author dietrick
 *
 */
public class MapBeanGradientBackgroundPolicy extends OMComponent implements
		MapBeanBackgroundPolicy, PropertyChangeListener {

	/**
	 * A property that describes the location of the bright point and dark point
	 * of the gradient paint, defined as a percentage of the window width and
	 * height. This should be a space separated list of 4 numbers between 0 and
	 * 1, reflecting bright x, bright y, dark x, dark y. i.e. .3 .3 .95 .95 will
	 * give you brightness in the upper left area of the map, and darkness in
	 * the lower right.
	 */
	public final static String BRIGHT_DARK_PERCENTAGE_PROPERTY = "brightDarkPercentage";
	public final static String HOW_BRIGHT_PROPERTY = "howBright";
	public final static String HOW_DARK_PROPERTY = "howDark";

	/**
	 * bright x, bright y, dark x, dark y, in percentages of window measurement.
	 */
	protected final double[] brightPointDarkPoint = new double[] { .3, .3, .95,
			.95 };
	protected int howBright = 1;
	protected int howDark = 1;
	protected Paint originalPaint; // the original Paint
	protected Projection proj; // the current background
	protected Paint gradientPaint; // to return for background
	protected MapBean mapBean; // handle to know if it's removed from MapHandler

	protected void updatePaint() {
		// Gradient paint in MapBean. Needs to be handled in Pan mode.
		Projection projection = proj; // local copy
		if (originalPaint instanceof Color && projection != null) {
			Color baseColor = (Color) originalPaint;
			Point2D p1 = new Point2D.Double(projection.getWidth()
					* brightPointDarkPoint[0], projection.getHeight()
					* brightPointDarkPoint[1]);
			Point2D p2 = new Point2D.Double(projection.getWidth()
					* brightPointDarkPoint[2], projection.getHeight()
					* brightPointDarkPoint[3]);
			gradientPaint = new GradientPaint(p1,
					brighten(baseColor, howBright), p2, darken(baseColor,
							howDark), false);
		} else {
			// Since it's something else like a gradient paint or maybe image,
			// just return it.
			gradientPaint = originalPaint;
		}
	}

	protected Color darken(Color color, int times) {
		for (int i = 0; i < times; i++) {
			color = color.darker();
		}
		return color;
	}

	protected Color brighten(Color color, int times) {
		for (int i = 0; i < times; i++) {
			color = color.brighter();
		}
		return color;
	}

	public boolean includeInPan() {
		return false;
	}

	public void propertyChange(PropertyChangeEvent evt) {
		String propertyName = evt.getPropertyName();
		Object obj = evt.getNewValue();
		if (MapBean.BackgroundProperty.equals(propertyName)
				&& obj instanceof Paint) {
			setOriginalPaint((Paint) obj);
		} else if (MapBean.ProjectionProperty.equals(propertyName)
				&& obj instanceof Projection) {
			setProjection((Projection) obj);
		}
	}

	protected void setOriginalPaint(Paint p) {
		originalPaint = p;
		updatePaint();
	}

	public Paint getOriginalPaint() {
		return originalPaint;
	}

	public void setProjection(Projection p) {
		proj = p;
		updatePaint();
	}

	public Projection getProjection() {
		return proj;
	}

	public Paint getBckgrnd() {
		return gradientPaint;
	}

	public void findAndInit(Object obj) {
		if (obj instanceof MapBean) {
			mapBean = (MapBean) obj;
			mapBean.addPropertyChangeListener(this);
			mapBean.setBackgroundPolicy(this);
		}
	}

	public void findAndUndo(Object obj) {
		if (obj == mapBean) {
			mapBean.removePropertyChangeListener(this);
			mapBean.setBackgroundPolicy(null);
			mapBean = null;
		}
	}

	public void setProperties(String prefix, Properties props) {
		super.setProperties(prefix, props);

		prefix = PropUtils.getScopedPropertyPrefix(prefix);

		howBright = PropUtils.intFromProperties(props, prefix
				+ HOW_BRIGHT_PROPERTY, howBright);
		howDark = PropUtils.intFromProperties(props,
				prefix + HOW_DARK_PROPERTY, howDark);

		String[] stringValues = PropUtils.stringArrayFromProperties(props,
				prefix + BRIGHT_DARK_PERCENTAGE_PROPERTY, " ");
		
		if (stringValues != null && stringValues.length == 4) {
			try {
				double[] values = new double[4];
				values[0] = Double.parseDouble(stringValues[0]);
				values[1] = Double.parseDouble(stringValues[1]);
				values[2] = Double.parseDouble(stringValues[2]);
				values[3] = Double.parseDouble(stringValues[3]);
				
				System.arraycopy(values, 0, brightPointDarkPoint, 0, 4);
			} catch (NumberFormatException nfe) {
				// if anything goes wrong, just boot!  Too bad, pilot error!
			}
		}
	}
	
	public Properties getProperties(Properties props) {
		props = super.getProperties(props);
		String prefix = PropUtils.getScopedPropertyPrefix(this);
		
		props.setProperty(prefix + HOW_BRIGHT_PROPERTY, Double.toString(howBright));
		props.setProperty(prefix + HOW_DARK_PROPERTY, Double.toString(howDark));
		
		StringBuilder buf = new StringBuilder();
		buf.append(Double.toString(brightPointDarkPoint[0])).append(" ");
		buf.append(Double.toString(brightPointDarkPoint[1])).append(" ");
		buf.append(Double.toString(brightPointDarkPoint[2])).append(" ");
		buf.append(Double.toString(brightPointDarkPoint[3]));
		props.setProperty(prefix + BRIGHT_DARK_PERCENTAGE_PROPERTY, buf.toString());
		
		return props;
	}
	
	public Properties getPropertyInfo(Properties props) {
		props = super.getPropertyInfo(props);
		PropUtils.setI18NPropertyInfo(i18n, props, MapBeanGradientBackgroundPolicy.class, HOW_BRIGHT_PROPERTY, "Brightness", "How many times brighter than the source color is the bright point", null);
		PropUtils.setI18NPropertyInfo(i18n, props, MapBeanGradientBackgroundPolicy.class, HOW_DARK_PROPERTY, "Darkness", "How many times darker than the source color is the dark point", null);		
		PropUtils.setI18NPropertyInfo(i18n, props, MapBeanGradientBackgroundPolicy.class, BRIGHT_DARK_PERCENTAGE_PROPERTY, "Location", "The location of the bright/dark points in percentage (bright x, bright y, dark x, dark y)", null);		
		return props;
	}

}
