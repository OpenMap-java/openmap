package com.bbn.openmap.dataAccess.mapTile.mb;

import java.awt.Color;
import java.awt.Paint;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.bbn.openmap.util.ColorFactory;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Builds up an array of values to use at various zoom levels, based on a node
 * with a base value and step values. Set the base when you make it, add step
 * values. Then just call get to retrieve value for zoom level.
 * 
 * @author dietrick
 *
 */
public class StyleFunction<T> {
	public final static int MIN_ZOOM_LEVEL = 0;
	public final static int MAX_ZOOM_LEVEL = 20;
	public final static int NUM_ZOOM_LEVELS = 21;

	T base;
	T[] levels;
	T DEFAULT;
	boolean loaded = false;

	/**
	 * Constructor where base and default are the same.
	 * 
	 * @param basedflt
	 */
	public StyleFunction(T basedflt) {
		this(basedflt, basedflt);
	}

	/**
	 * Constructor with different values for base and default.
	 * 
	 * @param base
	 *            value used for calculated level values.
	 * @param dflt
	 *            value used for null level values.
	 */
	public StyleFunction(T base, T dflt) {
		this.base = base;
		this.DEFAULT = dflt;
	}

	public T get(int zoomLevel) {
		if (!loaded) {
			load();
		}

		if (levels == null) {
			return base;
		}

		if (zoomLevel <= MIN_ZOOM_LEVEL) {
			return levels[MIN_ZOOM_LEVEL];
		} else if (zoomLevel >= MAX_ZOOM_LEVEL) {
			return levels[MAX_ZOOM_LEVEL];
		}
		return levels[zoomLevel];
	}

	public void add(int zoomLevel, T val) {
		if (levels == null) {
			levels = initLevels();
		}

		loaded = false;

		if (zoomLevel <= MIN_ZOOM_LEVEL) {
			levels[MIN_ZOOM_LEVEL] = val;
		} else if (zoomLevel >= MAX_ZOOM_LEVEL) {
			levels[MAX_ZOOM_LEVEL] = val;
		} else {
			levels[zoomLevel] = val;
		}
	}

	public T[] initLevels() {
		List<T> ret = new ArrayList<T>();
		for (int i = 0; i < NUM_ZOOM_LEVELS; i++) {
			ret.add(DEFAULT);
		}
		return (T[]) ret.toArray();
	}

	public boolean isConstant() {
		return levels == null;
	}

	public void load() {

		if (levels == null) {
			loaded = true;
			return;
		}

		T levelVal = null;
		int bottom = MIN_ZOOM_LEVEL;
		int top = MIN_ZOOM_LEVEL;

		for (int i = MIN_ZOOM_LEVEL; i < NUM_ZOOM_LEVELS; i++) {
			if (levels[i] != DEFAULT) {
				levelVal = levels[i];
				bottom = i;
				break;
			}
		}

		for (int i = MAX_ZOOM_LEVEL; i > bottom; i--) {
			if (levels[i] != DEFAULT) {
				top = i;
				break;
			}
		}

		for (int i = MIN_ZOOM_LEVEL; i < NUM_ZOOM_LEVELS; i++) {
			if (levels[i] == DEFAULT) {
				if (i < bottom || i > top) {
					levels[i] = levelVal;
				} else {
					levels[i] = levelVal;
				}
			} else {
				levelVal = levels[i];
			}
		}

		loaded = true;
	}

	public String toString() {

		if (levels == null) {
			return "base: " + base;
		} else {
			if (!loaded) {
				load();
			}

			StringBuilder sb = new StringBuilder("levels[");
			for (int i = MIN_ZOOM_LEVEL; i < MAX_ZOOM_LEVEL; i++) {
				sb.append(levels[i]).append(",");
			}
			sb.append(levels[MAX_ZOOM_LEVEL]).append("]");
			return sb.toString();
		}
	}

	/**
	 * Get the StyleFunction class built from the node resulting from a key get
	 * query.
	 * 
	 * @param node
	 *            the value node is passed in.
	 * @param dflt
	 *            the default value.
	 * @param constants
	 *            the constants node.
	 * @return If null passed in, null will be passed out. This can indicate
	 *         that the key wasn't used.
	 */
	public static StyleFunction<Double> getDoubleFunction(JsonNode node, Double dflt, JsonNode constants) {
		if (node != null) {
			if (node.has("stops")) {

				JsonNode stopsNode = node.withArray("stops");
				if (stopsNode != null) {
					// System.out.println(node);
					Iterator<JsonNode> stops = stopsNode.iterator();
					if (stops != null) {
						double base = StyleNode.getAsDouble(node, "base", 1.0);
						StyleFunction<Double> styleFunction = new StyleFunction.EXP(base);
						while (stops.hasNext()) {
							JsonNode stop = stops.next();

							if (stop.isArray()) {
								int zoomLevel = stop.get(0).asInt();
								double val = stop.get(1).asDouble();

								styleFunction.add(zoomLevel, val);
							}
						}
						return styleFunction;
					}
				}
			} else if (node.isDouble()) {
				Double val = new Double(node.asDouble());
				return new StyleFunction<Double>(val);
			} else if (node.isTextual()) {

				String valString = node.asText();

				if (constants != null && refersToConstant(valString, constants)) {
					return getDoubleFunction(constants.get(valString), dflt, null);
				}
			}
		}

		if (dflt != null) {
			return new StyleFunction<Double>(dflt);
		}

		return null;
	}

	/**
	 * Get a StyleFunction holding float[].
	 * 
	 * @param node
	 *            the value node from a key query.
	 * @param constants
	 *            the constants node.
	 * @return null if node null or node isn't an array.
	 */
	public static StyleFunction<float[]> getFloatArrayFunction(JsonNode node, JsonNode constants) {
		if (node != null) {
			if (node.has("stops")) {

				JsonNode stopsNode = node.withArray("stops");
				if (stopsNode != null) {
					// System.out.println(node);
					Iterator<JsonNode> stops = stopsNode.iterator();
					if (stops != null) {

						StyleFunction<float[]> styleFunction = new StyleFunction<float[]>(new float[1]);
						while (stops.hasNext()) {
							JsonNode stop = stops.next();

							if (stop.isArray()) {
								int zoomLevel = stop.get(0).asInt();

								JsonNode arrayNode = stop.get(1);
								if (arrayNode.isArray()) {
									styleFunction.add(zoomLevel, getFloats(arrayNode));
								}

							}
						}
						return styleFunction;
					}
				}
			} else if (node.isArray()) {
				return new StyleFunction<float[]>(getFloats(node));
			} else if (node.isTextual()) {

				String valString = node.asText();

				if (constants != null && refersToConstant(valString, constants)) {
					return getFloatArrayFunction(constants.get(valString), null);
				}
			}
		}

		return null;
	}

	/**
	 * Parsing function that assumes node is an array of floats.
	 * 
	 * @param node
	 *            array of numbers
	 * @return float array
	 */
	static float[] getFloats(JsonNode node) {
		int size = node.size();
		float[] fArray = new float[size];
		for (int i = 0; i < size; i++) {
			fArray[i] = node.get(i).floatValue();
		}
		return fArray;
	}

	/**
	 * Check if the value starts with apersand, signifying a constant value
	 * link.
	 * 
	 * @param val
	 *            value string to test for key into constants list
	 * @param constants
	 *            constants list as JsonNode.
	 * @return true if value is found in constants.
	 */
	static boolean refersToConstant(String val, JsonNode constants) {
		boolean ret = constants != null && val != null && val.startsWith("@");

		if (ret && getLogger().isLoggable(Level.FINE)) {
			getLogger().fine("found constant " + val + ", " + constants.get(val).toString());
		}

		return ret;
	}

	/**
	 * Create a Paint StyleFunction.
	 * 
	 * @param node
	 *            The value node from a color key query, holding the color value
	 *            or step definition.
	 * @param opacity
	 *            the opacity StyleFunction to be applied to Paint StyleFunction
	 *            values.
	 * @param dflt
	 *            the default base color in case a color isn't defined.
	 * @param constants
	 *            A constants definition node.
	 * @return the Paint StyleFunction defined by the value node provided, dflt.
	 *         The colors have been modified with opacity settings.
	 */
	public static StyleFunction<Paint> getPaintFunction(JsonNode node, StyleFunction<Double> opacity,
			StyleFunction<Paint> dflt, JsonNode constants) {

		if (opacity == null) {
			opacity = StyleDrawingAttributes.DEFAULT_OPACITY;
		}

		if (node != null) {

			// Test for step-wise color definitions.
			if (node.has("stops")) {

				JsonNode stopsNode = node.withArray("stops");
				if (stopsNode != null) {
					// System.out.println(node);
					Iterator<JsonNode> stops = stopsNode.iterator();
					if (stops != null) {
						Color base = applyOpacity(parseColor(node, "base", constants), opacity.base);
						StyleFunction<Paint> styleFunction = new StyleFunction<Paint>(base);
						while (stops.hasNext()) {
							JsonNode stop = stops.next();

							if (stop.isArray()) {
								int zoomLevel = stop.get(0).asInt();
								Color val = parseColor(stop.get(1).asText());
								if (val != null) {
									styleFunction.add(zoomLevel, applyOpacity(val, opacity.get(zoomLevel)));
								}
							}
						}

						return styleFunction;
					}
				}
			} else {

				// The color is defined directly for all zoom levels.
				String valString = node.asText();

				if (constants != null && refersToConstant(valString, constants)) {
					return getPaintFunction(constants.get(valString), opacity, dflt, null);
				}

				Color val = parseColor(valString);
				if (val != null) {
					if (opacity.isConstant()) {
						val = applyOpacity(val, opacity.base);
					}
					return new StyleFunction<Paint>(val);
				}

			}
		}

		return dflt;
	}

	/**
	 * Applies opacity to the base color, returning an appropriate Paint
	 * StyleFunction.
	 * 
	 * @param baseColor
	 *            starting color
	 * @param opacity
	 *            opaqueness from 0 to 1.0
	 * @return new color with opacity applied.
	 */
	static StyleFunction<Paint> applyOpacity(Color baseColor, StyleFunction<Double> opacity) {
		if (opacity == null) {
			opacity = StyleDrawingAttributes.DEFAULT_OPACITY;
		}

		if (!opacity.isConstant()) {
			StyleFunction<Paint> ret = new StyleFunction<Paint>(baseColor);
			for (int i = 0; i < 20; i++) {
				ret.add(i, applyOpacity(baseColor, opacity.get(i)));
			}
			return ret;
		}

		return new StyleFunction<Paint>(applyOpacity(baseColor, opacity.base));
	}

	/**
	 * @param baseColor
	 *            starting color
	 * @param opacity
	 *            opaqueness from 0 to 1.0
	 * @return new color with opacity applied.
	 */
	static Color applyOpacity(Color baseColor, double opacity) {
		if (baseColor != null) {
			return new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), (int) (opacity * 255));
		}

		return baseColor;
	}

	/**
	 * Parse the color given the paint node. The value will be from the prop key
	 * of the provided node.
	 * 
	 * @param node
	 *            node holding key and value.
	 * @param prop
	 *            the key for the color value
	 * @param constants
	 *            node holding constant values.
	 * @return Color
	 */
	public static Color parseColor(JsonNode node, String prop, JsonNode constants) {
		return parseColor(node.get(prop), constants);
	}

	/**
	 * Parse the Color given the color value node.
	 * 
	 * @param node
	 *            color value node.
	 * @param constants
	 *            node holding constant values.
	 * @return Color
	 */
	public static Color parseColor(JsonNode node, JsonNode constants) {
		Color baseColor = null;
		if (node != null) {
			String valString = node.asText();

			if (constants != null && refersToConstant(valString, constants)) {
				return parseColor(constants.get(valString), null);
			}

			baseColor = parseColor(node.asText());
		}

		return baseColor;
	}

	/**
	 * Given value string, check various formats to determine a color.
	 * 
	 * @param colorString
	 *            rgb, rgba, #, color name
	 * @return Color if it can be determined, null otherwise.
	 */
	public static Color parseColor(String colorString) {
		Color baseColor = null;

		if (colorString != null) {

			if (colorString.startsWith("#")) {
				baseColor = ColorFactory.parseColor(colorString.substring(1), true);
			} else if (colorString.startsWith("rgba")) {
				String tokens = colorString.substring(5, colorString.length() - 1); // parans
				String[] values = tokens.split(",");

				if (values.length == 4) {
					try {
						baseColor = ColorFactory.createColor(Integer.parseInt(values[0]), Integer.parseInt(values[1]),
								Integer.parseInt(values[2]), Integer.parseInt(values[3]));
					} catch (NumberFormatException nfe) {

					}
				}
			} else if (colorString.startsWith("rgb")) {
				String tokens = colorString.substring(4, colorString.length() - 1); // parans
				String[] values = tokens.split(",");
				if (values.length == 3) {
					try {
						baseColor = ColorFactory.createColor(Integer.parseInt(values[0]), Integer.parseInt(values[1]),
								Integer.parseInt(values[2]), 255);
					} catch (NumberFormatException nfe) {

					}
				}
			} else {
				baseColor = ColorFactory.getNamedColor(colorString, null);
			}
		}

		return baseColor;
	}

	/**
	 * Class used by StyleFunction Double to interpolate values between defined
	 * values. A regular Double StyleFunction will create stepped values.
	 * 
	 * @author dietrick
	 *
	 */
	public static class EXP extends StyleFunction<Double> {

		public EXP(Double base) {
			super(base, Double.NaN);
		}

		@Override
		public Double[] initLevels() {
			Double[] ret = new Double[NUM_ZOOM_LEVELS];
			for (int i = MIN_ZOOM_LEVEL; i < NUM_ZOOM_LEVELS; i++) {
				ret[i] = DEFAULT;
			}
			return ret;
		}

		@Override
		public void load() {

			if (levels == null) {
				return;
			}

			double levelVal = Double.NaN;
			int bottom = MIN_ZOOM_LEVEL;
			int top = MIN_ZOOM_LEVEL;
			double maxVal = Double.MAX_VALUE;

			for (int i = MIN_ZOOM_LEVEL; i < NUM_ZOOM_LEVELS; i++) {
				if (!Double.isNaN(levels[i])) {
					levelVal = levels[i];
					bottom = i;
					maxVal = levelVal;
					break;
				}
			}

			for (int i = MAX_ZOOM_LEVEL; i > bottom; i--) {
				if (!Double.isNaN(levels[i])) {
					top = i;
					maxVal = levels[i];
					break;
				}
			}

			int indexForLevelVal = MIN_ZOOM_LEVEL;
			for (int i = MIN_ZOOM_LEVEL; i < NUM_ZOOM_LEVELS; i++) {
				if (Double.isNaN(levels[i])) {
					if (i < bottom || i > top) {
						levels[i] = levelVal;
						indexForLevelVal = i;
					} else {
						// we have to figure out this level.
						int nextValidIndex = top;

						for (int j = i; j < top; j++) {
							if (!Double.isNaN(levels[j])) {
								nextValidIndex = j;
								break;
							}
						}

						double nextLevelVal = levels[nextValidIndex];

						int zoomDiff = nextValidIndex - indexForLevelVal;
						double valueDiff = nextLevelVal - levelVal;
						double slope = valueDiff / zoomDiff;

						double val = Math.pow(base, i - indexForLevelVal) * slope + levels[i - 1];

						if (val > maxVal) {
							val = maxVal;
						}

						levels[i] = val;
					}
				} else {
					levelVal = levels[i];
					indexForLevelVal = i;
				}
			}

			loaded = true;
		}

	}

	// <editor-fold defaultstate="collapsed" desc="Logger Code">
	/**
	 * Holder for this class's Logger. This allows for lazy initialization of
	 * the logger.
	 */
	private static final class LoggerHolder {
		/**
		 * The logger for this class
		 */
		private static final Logger LOGGER = Logger.getLogger(StyleFunction.class.getName());

		/**
		 * Prevent instantiation
		 */
		private LoggerHolder() {
			throw new AssertionError("The LoggerHolder should never be instantiated");
		}
	}

	/**
	 * Get the logger for this class.
	 *
	 * @return logger for this class
	 */
	private static Logger getLogger() {
		return LoggerHolder.LOGGER;
	}
	// </editor-fold>
}
