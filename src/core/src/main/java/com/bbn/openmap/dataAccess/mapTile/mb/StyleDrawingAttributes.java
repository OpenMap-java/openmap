package com.bbn.openmap.dataAccess.mapTile.mb;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.GeneralPath;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.bbn.openmap.omGraphics.DrawingAttributes;
import com.bbn.openmap.omGraphics.OMColor;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMShape;
import com.fasterxml.jackson.databind.JsonNode;

public class StyleDrawingAttributes extends DrawingAttributes {

	private static final long serialVersionUID = 1L;
	static String LAYOUT = "layout";
	static String PAINT = "paint";

	final static Stroke DEFAULT_STROKE = new BasicStroke(1);

	final static StyleFunction<Double> DEFAULT_OPACITY = new StyleFunction<Double>(1.0);
	final static StyleFunction<Paint> BLACK = new StyleFunction<Paint>(Color.black);
	final static StyleFunction<Paint> CLEAR = new StyleFunction<Paint>(OMColor.clear);

	// LAYOUT
	StyleFunction<StyleVisibility> visible = null;
	// PAINT
	final StyleFunction<DrawingAttributes> renderers = new StyleFunction<DrawingAttributes>(
			DrawingAttributes.getDefaultClone());

	StyleDrawingAttributes refRenderer = null;
	String layerID;

	JsonNode constants;

	static StyleDrawingAttributes EMPTY = new LINE(null, null);

	public static StyleDrawingAttributes getForType(JsonNode node, JsonNode constants) {
		return get(StyleLayerType.getFromLayerNode(node), node, constants);
	}

	/**
	 * We just need to grab any layout properties from the reference if one is
	 * provided. Each attributes type will pull out different layout properties,
	 * because each type has different layout properties.
	 * 
	 * @param reference
	 *            a StyleDrawingAttributes from a layer that is a refernce layer
	 *            to our layer.
	 */
	void configureFromReference(StyleDrawingAttributes reference) {
		this.refRenderer = reference;
		/*
		 * This may stomp on any settings that were there.
		 */
		if (reference != null && visible == null) {
			visible = reference.visible;
		}
	}

	public static StyleDrawingAttributes get(StyleLayerType slt, JsonNode node, JsonNode constants) {

		if (getLogger().isLoggable(Level.FINE)) {
			getLogger().fine("getting style for " + slt.name);
		}

		switch (slt) {
		case BACKGROUND:
			return new StyleDrawingAttributes.BACKGROUND(node, constants);
		case FILL:
			return new StyleDrawingAttributes.FILL(node, constants);
		case LINE:
			return new StyleDrawingAttributes.LINE(node, constants);
		case SYMBOL:
			return new StyleDrawingAttributes.SYMBOL(node, constants);
		case RASTER:
			return new StyleDrawingAttributes.RASTER(node, constants);
		case CIRCLE:
			return new StyleDrawingAttributes.CIRCLE(node, constants);
		default:
			return null;
		}
	}

	private StyleDrawingAttributes(JsonNode node, JsonNode constants) {

		this.constants = constants;

		for (int zoomLevel = StyleFunction.MIN_ZOOM_LEVEL; zoomLevel < StyleFunction.NUM_ZOOM_LEVELS; zoomLevel++) {
			renderers.add(zoomLevel, DrawingAttributes.getDefaultClone());
		}

		if (node != null) {
			JsonNode layout = node.get(LAYOUT);
			if (layout != null) {
				configureLayout(layout);
			}

			JsonNode paint = node.get(PAINT);
			if (paint != null) {
				configurePaint(paint);
			}
		}
	}

	OMGraphic getOMGraphic(GeneralPath gp, int zoomLevel) {
		OMShape oms = new OMShape.PROJECTED(gp);
		renderers.get(zoomLevel).setTo(oms);
		// oms.setVisible(visible.get(zoomLevel).isVisible());
		return oms;
	}

	/**
	 * Called when constructed, layout properties will be overwritten by
	 * reference layer values if one is provided.
	 * 
	 * @param layout
	 */
	void configureLayout(JsonNode layout) {
		visible = StyleVisibility.getFunction(layout);
	}

	void configurePaint(JsonNode node) {
		// Noop, each subclass does it differently. Should this be abstract?
	}

	void setLinePaint(StyleFunction<Paint> linePaints) {
		if (linePaints != null) {
			for (int zoomLevel = StyleFunction.MIN_ZOOM_LEVEL; zoomLevel < StyleFunction.NUM_ZOOM_LEVELS; zoomLevel++) {
				renderers.get(zoomLevel).setLinePaint(linePaints.get(zoomLevel));
			}
		}
	}

	void setLinePaint(StyleDrawingAttributes ref) {
		if (ref != null) {
			for (int zoomLevel = StyleFunction.MIN_ZOOM_LEVEL; zoomLevel < StyleFunction.NUM_ZOOM_LEVELS; zoomLevel++) {
				renderers.get(zoomLevel).setLinePaint(ref.getRenderer(zoomLevel).getLinePaint());
			}
		}
	}

	void setFillPaint(StyleFunction<Paint> fillPaints) {
		if (fillPaints != null) {
			for (int zoomLevel = StyleFunction.MIN_ZOOM_LEVEL; zoomLevel < StyleFunction.NUM_ZOOM_LEVELS; zoomLevel++) {
				renderers.get(zoomLevel).setFillPaint(fillPaints.get(zoomLevel));
			}
		}
	}

	void setFillPaint(StyleDrawingAttributes ref) {
		if (ref != null) {
			for (int zoomLevel = StyleFunction.MIN_ZOOM_LEVEL; zoomLevel < StyleFunction.NUM_ZOOM_LEVELS; zoomLevel++) {
				renderers.get(zoomLevel).setFillPaint(ref.getRenderer(zoomLevel).getFillPaint());
			}
		}
	}

	void setStroke(StyleFunction<Stroke> strokes) {
		if (strokes != null) {
			for (int zoomLevel = StyleFunction.MIN_ZOOM_LEVEL; zoomLevel < StyleFunction.NUM_ZOOM_LEVELS; zoomLevel++) {
				renderers.get(zoomLevel).setStroke(strokes.get(zoomLevel));
			}
		}
	}

	void setStroke(StyleDrawingAttributes ref) {
		if (ref != null) {
			for (int zoomLevel = StyleFunction.MIN_ZOOM_LEVEL; zoomLevel < StyleFunction.NUM_ZOOM_LEVELS; zoomLevel++) {
				renderers.get(zoomLevel).setStroke(ref.getRenderer(zoomLevel).getStroke());
			}
		}
	}

	void setMatting(boolean isMatted, StyleFunction<Paint> mattingPaints) {
		if (mattingPaints != null) {
			for (int zoomLevel = StyleFunction.MIN_ZOOM_LEVEL; zoomLevel < StyleFunction.NUM_ZOOM_LEVELS; zoomLevel++) {
				DrawingAttributes da = renderers.get(zoomLevel);
				da.setMatted(isMatted);
				da.setMattingPaint(mattingPaints.get(zoomLevel));
			}
		}
	}

	void setMatting(StyleDrawingAttributes ref) {
		if (ref != null) {
			for (int zoomLevel = StyleFunction.MIN_ZOOM_LEVEL; zoomLevel < StyleFunction.NUM_ZOOM_LEVELS; zoomLevel++) {

				DrawingAttributes da1 = renderers.get(zoomLevel);
				DrawingAttributes da2 = ref.getRenderer(zoomLevel);

				da1.setMatted(da2.isMatted());
				da1.setMattingPaint(da2.getMattingPaint());

				renderers.get(zoomLevel).setStroke(ref.getRenderer(zoomLevel).getStroke());
			}
		}
	}

	DrawingAttributes getRenderer(int zoomLevel) {
		return renderers.get(zoomLevel);
	}

	boolean isVisible(int zoomLevel) {
		return visible == null || visible.get(zoomLevel).isVisible();
	}

	public static class BACKGROUND extends StyleDrawingAttributes {
		private static final long serialVersionUID = 1L;
		// PAINT
		/**
		 * Optional color. Defaults to #000000. Disabled by background-pattern.
		 * The color with which the background will be drawn.
		 */
		static String BACKGROUND_COLOR = "background-color";
		/**
		 * Optional string. Name of image in sprite to use for drawing an image
		 * background. For seamless patterns, image width and height must be a
		 * factor of two (2, 4, 8, …, 512).
		 */
		static String BACKGROUND_PATTERN = "background-pattern";
		/**
		 * Optional number. Defaults to 1. The opacity at which the background
		 * will be drawn.
		 */
		static String BACKGROUND_OPACITY = "background-opacity";

		public BACKGROUND(JsonNode node, JsonNode constants) {
			super(node, constants);
		}

		void configurePaint(JsonNode paint) {
			StyleFunction<Double> opacity = StyleFunction.getDoubleFunction(paint.get(BACKGROUND_OPACITY), 1.0, constants);
			StyleFunction<Paint> backgroundPaints = StyleFunction.getPaintFunction(paint.get(BACKGROUND_COLOR), opacity, BLACK, constants);

			setFillPaint(backgroundPaints);
			setLinePaint(CLEAR);

		}

	}

	public static class FILL extends StyleDrawingAttributes {
		private static final long serialVersionUID = 1L;
		// PAINT
		/**
		 * Optional boolean. Defaults to true. Whether or not the fill should be
		 * antialiased.
		 */
		static String FILL_ANTIALIAS = "fill-antialias";
		/**
		 * Optional number. Defaults to 1. The opacity given to the fill color.
		 */
		static String FILL_OPACITY = "fill-opacity";
		/**
		 * Optional color. Defaults to #000000. Disabled by fill-pattern. The
		 * color of the fill.
		 */
		static String FILL_COLOR = "fill-color";
		/**
		 * Optional color. Disabled by fill-pattern. Requires fill-antialias =
		 * true. The outline color of the fill. Matches the value of fill-color
		 * if unspecified.
		 */
		static String FILL_OUTLINE_COLOR = "fill-outline-color";
		/**
		 * Optional array. Units in pixels. Defaults to 0,0. The geometry’s
		 * offset. Values are [x, y] where negatives indicate left and up,
		 * respectively.
		 */
		static String FILL_TRANSLATE = "fill-translate";
		/**
		 * Optional enum. One of map, viewport. Defaults to map. Requires
		 * fill-translate. Control whether the translation is relative to the
		 * map (north) or viewport (screen)
		 */
		static String FILL_TRANSLATE_ANCHOR = "fill-translate-anchor";
		/**
		 * Optional string. Name of image in sprite to use for drawing image
		 * fills. For seamless patterns, image width and height must be a factor
		 * of two (2, 4, 8, …, 512).
		 */
		static String FILL_PATTERN = "fill-pattern";

		public FILL(JsonNode node, JsonNode constants) {
			super(node, constants);
		}

		void configurePaint(JsonNode paint) {
			StyleFunction<Double> opacity = StyleFunction.getDoubleFunction(paint.get(FILL_OPACITY), 1.0, constants);
			StyleFunction<Paint> fillPaints = StyleFunction.getPaintFunction(paint.get(FILL_COLOR), opacity, BLACK, constants);

			StyleFunction<Paint> outlinePaints = CLEAR;
			if (StyleNode.getAsBoolean(paint, FILL_ANTIALIAS, true)) {
				outlinePaints = StyleFunction.getPaintFunction(paint.get(FILL_OUTLINE_COLOR), opacity, outlinePaints, constants);
			}

			setFillPaint(refRenderer);
			setLinePaint(refRenderer);

			setFillPaint(fillPaints);
			setLinePaint(outlinePaints);
		}

		OMGraphic getOMGraphic(GeneralPath gp, int zoomLevel) {
			OMShape oms = new OMShape.PROJECTED(gp);
			renderers.get(zoomLevel).setTo(oms);
			// oms.setVisible(visible.get(zoomLevel).isVisible());
			return oms;
		}
	}

	public static class LINE extends StyleDrawingAttributes {

		private static final long serialVersionUID = 1L;
		// LAYOUT
		/**
		 * Optional enum. One of butt, round, square. Defaults to butt. The
		 * display of line endings.
		 */
		static String LINE_CAP = "line-cap";
		/**
		 * Optional enum. One of bevel, round, miter. Defaults to miter. The
		 * display of lines when joining.
		 */
		static String LINE_JOIN = "line-join";
		/**
		 * Optional number. Defaults to 2. Requires line-join = miter. Used to
		 * automatically convert miter joins to bevel joins for sharp angles.
		 */
		static String LINE_MITER_LIMIT = "line-miter-limit";
		/**
		 * Optional number. Defaults to 1.05. Requires line-join = round. Used
		 * to automatically convert round joins to miter joins for shallow
		 * angles.
		 */
		static String LINE_ROUND_LIMIT = "line-round-limit";
		// PAINT
		/**
		 * Optional number. Defaults to 1. The opacity at which the line will be
		 * drawn.
		 */
		static String LINE_OPACITY = "line-opacity";
		/**
		 * Optional color. Defaults to #000000. Disabled by line-pattern. The
		 * color with which the line will be drawn.
		 */
		static String LINE_COLOR = "line-color";
		/**
		 * Optional array. Units in pixels. Defaults to 0,0. The geometry’s
		 * offset. Values are [x, y] where negatives indicate left and up,
		 * respectively.
		 */
		static String LINE_TRANSLATE = "line-translate";
		/**
		 * Optional enum. One of map, viewport. Defaults to map. Requires
		 * line-translate. Control whether the translation is relative to the
		 * map (north) or viewport (screen)
		 */
		static String LINE_TRANSLATE_ANCHOR = "line-translate-anchor";
		/**
		 * Optional number. Units in pixels. Defaults to 1. Stroke thickness.
		 */
		static String LINE_WIDTH = "line-width";
		/**
		 * Optional number. Units in pixels. Defaults to 0. Draws a line casing
		 * outside of a line’s actual path. Value indicates the width of the
		 * inner gap.
		 */
		static String LINE_GAP_WIDTH = "line-gap-width";
		/**
		 * Optional number. Units in pixels. Defaults to 0. The line’s offset
		 * perpendicular to its direction. Values may be positive or negative,
		 * where positive indicates “rightwards” (if you were moving in the
		 * direction of the line) and negative indicates “leftwards.”
		 */
		static String LINE_OFFSET = "line-offset";
		/**
		 * Optional number. Units in pixels. Defaults to 0. Blur applied to the
		 * line, in pixels.
		 */
		static String LINE_BLUR = "line-blur";
		/**
		 * Optional array. Units in line widths. Disabled by line-pattern.
		 * Specifies the lengths of the alternating dashes and gaps that form
		 * the dash pattern. The lengths are later scaled by the line width. To
		 * convert a dash length to pixels, multiply the length by the current
		 * line width.
		 */
		static String LINE_DASHARRAY = "line-dasharray";
		/**
		 * Optional string. Name of image in sprite to use for drawing image
		 * lines. For seamless patterns, image width must be a factor of two (2,
		 * 4, 8, …, 512).
		 */
		static String LINE_PATTERN = "line-pattern";

		public LINE(JsonNode node, JsonNode constants) {
			super(node, constants);
		}

		void configurePaint(JsonNode paint) {
			StyleFunction<Double> opacity = StyleFunction.getDoubleFunction(paint.get(LINE_OPACITY), 1.0, constants);
			StyleFunction<Paint> linePaints = StyleFunction.getPaintFunction(paint.get(LINE_COLOR), opacity, BLACK, constants);

			configureStroke(paint);

			setLinePaint(refRenderer);
			setLinePaint(linePaints);
		}

		void configureStroke(JsonNode paint) {

			StyleFunction<Double> lineWidths = StyleFunction.getDoubleFunction(paint.get(LINE_WIDTH), 1.0, constants);
			StyleFunction<Double> lineGaps = StyleFunction.getDoubleFunction(paint.get(LINE_GAP_WIDTH), 0.0, constants);
			StyleFunction<float[]> dashArrays = StyleFunction.getFloatArrayFunction(paint.get(LINE_DASHARRAY), constants);
			StyleFunction<StyleLineCap> lineCaps = StyleLineCap.getFunction(paint.get(LINE_CAP));
			StyleFunction<StyleLineJoin> lineJoins = StyleLineJoin.getFunction(paint.get(LINE_JOIN));

			StyleFunction<Stroke> strokes = new StyleFunction<Stroke>(DEFAULT_STROKE);

			for (int zoomLevel = StyleFunction.MIN_ZOOM_LEVEL; zoomLevel < StyleFunction.NUM_ZOOM_LEVELS; zoomLevel++) {
				float lineWidth = safeLineWidth(lineWidths, zoomLevel);
				int lineCap = lineCaps.get(zoomLevel).val;
				int lineJoin = lineJoins.get(zoomLevel).val;
				float[] dashArray = safeDashArray(dashArrays, zoomLevel);

				Stroke stroke = new BasicStroke(lineWidth, lineCap, lineJoin, 2, dashArray, 0f);

				// ref renderer might hold cap, join, miter-limit, round-limit.
				// All other aspects of stroke are in feature definition.

				/*
				 * if (refRenderer != null) { Stroke refStroke =
				 * refRenderer.getRenderer(zoomLevel).getStroke(); if (refStroke
				 * != DEFAULT_STROKE) {
				 * 
				 * } }
				 */

				renderers.get(zoomLevel).setStroke(stroke);
			}
		}

		float[] safeDashArray(StyleFunction<float[]> dashArrays, int zoomLevel) {
			float[] dashArray = null;
			if (dashArrays != null) {
				float[] da = dashArrays.get(zoomLevel);
				float sum = 0;
				for (float f : da) {
					sum += f;
				}
				if (sum > 0) {
					dashArray = da;
				}
			}
			return dashArray;
		}

		float safeLineWidth(StyleFunction<Double> lineWidths, int zoomLevel) {
			float lineWidth = lineWidths.get(zoomLevel).floatValue();
			if (lineWidth < 0f) {
				lineWidth = 0f;
			}
			return lineWidth;
		}

		/**
		 */
		void configureFromReference(StyleDrawingAttributes reference) {
			super.configureFromReference(reference);

			/**
			 * if (reference instanceof LINE) { LINE lineRef = (LINE) reference;
			 * if (lineRef.lineGapFunction != null) {
			 * setMattingPaint(getLinePaint());
			 * setLinePaint(lineRef.getLinePaint()); setMatted(true); } }
			 */
		}
	}

	public static class SYMBOL extends StyleDrawingAttributes {
		private static final long serialVersionUID = 1L;
		// LAYOUT
		/**
		 * Optional enum. One of point, line. Defaults to point. Label placement
		 * relative to its geometry. line can only be used on LineStrings and
		 * Polygons.
		 */
		static String SYMBOL_PLACEMENT = "symbol-placement";
		/**
		 * Optional number. Units in pixels. Defaults to 250. Requires
		 * symbol-placement = line. Distance between two symbol anchors.
		 */
		static String SYMBOL_SPACING = "symbol-spacing";
		/**
		 * Optional boolean. Defaults to false. If true, the symbols will )not
		 * cross tile edges to avoid mutual collisions. Recommended in layers
		 * that don’t have enough padding in the vector tile to prevent
		 * collisions, or if it is a point symbol layer placed after a line
		 * symbol layer.
		 */
		static String SYMBOL_AVOID_EDGES = "symbol-avoid-edges";
		/**
		 * Optional boolean. Defaults to false. Requires icon-image. If true,
		 * the icon will be visible even if it collides with other previously
		 * drawn symbols.
		 */
		static String ICON_ALLOW_OVERLAP = "icon-allow-overlap";
		/**
		 * Optional boolean. Defaults to false. Requires icon-image. If true,
		 * other symbols can be visible even if they collide with the icon.
		 */
		static String ICON_IGNORE_PLACEMENT = "icon-ignore-placement";
		/**
		 * Optional boolean. Defaults to false. Requires icon-image. Requires
		 * text-field. If true, text will display without their corresponding
		 * icons when the icon collides with other symbols and the text does
		 * not.
		 */
		static String ICON_OPTIONAL = "icon-optional";
		/**
		 * Optional enum. One of map, viewport. Defaults to viewport. Requires
		 * icon-image. Orientation of icon when map is rotated.
		 */
		static String ICON_ROATION_ALIGNMENT = "icon-rotation-alignment";
		/**
		 * Optional number. Defaults to 1. Requires icon-image. Scale factor for
		 * icon. 1 is original size, 3 triples the size.
		 */
		static String ICON_SIZE = "icon-size";
		/**
		 * Optional string. A string with {tokens} replaced, referencing the
		 * data property to pull from.
		 */
		static String ICON_IMAGE = "icon-image";
		/**
		 * Optional number. Units in degrees. Defaults to 0. Requires
		 * icon-image. Rotates the icon clockwise.
		 */
		static String ICON_ROTATE = "icon-rotate";
		/**
		 * Optional number. Units in pixels. Defaults to 2. Requires icon-image.
		 * Size of the additional area around the icon bounding box used for
		 * detecting symbol collisions.
		 */
		static String ICON_PADDING = "icon-padding";
		/**
		 * Optional boolean. Defaults to false. Requires icon-image. Requires
		 * icon-rotation-alignment = map. Requires symbol-placement = line. If
		 * true, the icon may be flipped to prevent it from being rendered
		 * upside-down.
		 */
		static String ICON_KEEP_UPRIGHT = "icon-keep-upright";
		/**
		 * Optional array. Defaults to 0,0. Requires icon-image. Offset distance
		 * of icon from its anchor. Positive values indicate right and down,
		 * while negative values indicate left and up.
		 */
		static String ICON_OFFSET = "icon-offset";
		/**
		 * Optional enum. One of map, viewport. Defaults to viewport. Requires
		 * text-field. Orientation of text when map is rotated.
		 */
		static String TEXT_ROTATION_ALIGNMENT = "text-rotation-alignment";
		/**
		 * Optional string. Value to use for a text label. Feature properties
		 * are specified using tokens like {field_name}.
		 */
		static String TEXT_FIELD = "text-field";
		/**
		 * Optional array. Defaults to Open Sans Regular,Arial Unicode MS
		 * Regular. Requires text-field. Font stack to use for displaying text.
		 */
		static String TEXT_FONT = "text-font";
		/**
		 * Optional number. Units in pixels. Defaults to 16. Requires
		 * text-field. Font size.
		 */
		static String TEXT_SIZE = "text-size";
		/**
		 * Optional number. Units in em. Defaults to 10. Requires text-field.
		 * The maximum line width for text wrapping.
		 */
		static String TEXT_MAX_WIDTH = "text-max-width";
		/**
		 * Optional number. Units in em. Defaults to 1.2. Requires text-field.
		 * Text leading value for multi-line text.
		 */
		static String TEXT_LINE_HEIGHT = "text-line-height";
		/**
		 * Optional number. Units in em. Defaults to 0. Requires text-field.
		 * Text tracking amount.
		 */
		static String TEXT_LETTER_SPACING = "text-letter-spacing";
		/**
		 * Optional enum. One of left, center, right. Defaults to center.
		 * Requires text-field. Text justification options.
		 */
		static String TEXT_JUSTIFY = "text-justify";
		/**
		 * Optional enum. One of center, left, right, top, bottom, top-left,
		 * top-right, bottom-left, bottom-right. Defaults to center. Requires
		 * text-field. Part of the text placed closest to the anchor.
		 */
		static String TEXT_ANCHOR = "text-anchor";
		/**
		 * Optional number. Units in degrees. Defaults to 45. Requires
		 * text-field. Requires symbol-placement = line. Maximum angle change
		 * between adjacent characters.
		 */
		static String TEXT_MAX_ANGLE = "text-max-angle";
		/**
		 * Optional number. Units in degrees. Defaults to 0. Requires
		 * text-field. Rotates the text clockwise.
		 */
		static String TEXT_ROTATE = "text-rotate";
		/**
		 * Optional number. Units in pixels. Defaults to 2. Requires text-field.
		 * Size of the additional area around the text bounding box used for
		 * detecting symbol collisions.
		 */
		static String TEXT_PADDING = "text-padding";
		/**
		 * Optional boolean. Defaults to true. Requires text-field. Requires
		 * text-rotation-alignment = map. Requires symbol-placement = line. If
		 * true, the text may be flipped vertically to prevent it from being
		 * rendered upside-down.
		 */
		static String TEXT_KEEP_UPRIGHT = "text-keep-upright";
		/**
		 * Optional enum. One of none, uppercase, lowercase. Defaults to none.
		 * Requires text-field. Specifies how to capitalize text, similar to the
		 * CSS text-transform property.
		 */
		static String TEXT_TRANSFORM = "text-transform";
		/**
		 * Optional array. Units in ems. Defaults to 0,0. Requires text-field.
		 * Offset distance of text from its anchor. Positive values indicate
		 * right and down, while negative values indicate left and up.
		 */
		static String TEXT_OFFSET = "text-offset";
		/**
		 * Optional boolean. Defaults to false. Requires text-field. If true,
		 * the text will be visible even if it collides with other previously
		 * drawn symbols.
		 */
		static String TEXT_ALLOW_OVERLAP = "text-allow-overlap";
		/**
		 * Optional boolean. Defaults to false. Requires text-field. If true,
		 * other symbols can be visible even if they collide with the text.
		 */
		static String TEXT_IGNORE_PLACEMENT = "text-ignore-placement";
		/**
		 * Optional boolean. Defaults to false. Requires text-field. Requires
		 * icon-image. If true, icons will display without their corresponding
		 * text when the text collides with other symbols and the icon does not.
		 */
		static String TEXT_OPTIONAL = "text-optional";
		// PAINT
		/**
		 * Optional number. Defaults to 1. Requires icon-image. The opacity at
		 * which the icon will be drawn.
		 */
		static String ICON_OPACITY = "icon-opacity";
		/**
		 * Optional color. Defaults to #000000. Requires icon-image. The color
		 * of the icon. This can only be used with sdf icons.
		 */
		static String ICON_COLOR = "icon-color";
		/**
		 * Optional color. Defaults to rgba(0, 0, 0, 0). Requires icon-image.
		 * The color of the icon’s halo. Icon halos can only be used with sdf
		 * icons.
		 */
		static String ICON_HALO_COLOR = "icon-halo-color";
		/**
		 * Optional number. Units in pixels. Defaults to 0. Requires icon-image.
		 * Distance of halo to the icon outline.
		 */
		static String ICON_HALO_WIDTH = "icon-halo-width";
		/**
		 * Optional number. Units in pixels. Defaults to 0. Requires icon-image.
		 * Fade out the halo towards the outside.
		 */
		static String ICON_HALO_BLUR = "icon-halo-blur";
		/**
		 * Optional array. Units in pixels. Defaults to 0,0. Requires
		 * icon-image. Distance that the icon’s anchor is moved from its
		 * original placement. Positive values indicate right and down, while
		 * negative values indicate left and up.
		 */
		static String ICON_TRANSLATE = "icon-translate";
		/**
		 * Optional enum. One of map, viewport. Defaults to map. Requires
		 * icon-image. Requires icon-translate. Control whether the translation
		 * is relative to the map (north) or viewport (screen).
		 */
		static String ICON_TRANSLATE_ANCHOR = "icon-translate-anchor";
		/**
		 * Optional number. Defaults to 1. Requires text-field. The opacity at
		 * which the text will be drawn.
		 */
		static String TEXT_OPACITY = "text-opacity";
		/**
		 * Optional color. Defaults to #000000. Requires text-field. The color
		 * with which the text will be drawn.
		 */
		static String TEXT_COLOR = "text-color";
		/**
		 * Optional color. Defaults to rgba(0, 0, 0, 0). Requires text-field.
		 * The color of the text’s halo, which helps it stand out from
		 * backgrounds.
		 */
		static String TEXT_HALO_COLOR = "text-halo-color";
		/**
		 * Optional number. Units in pixels. Defaults to 0. Requires text-field.
		 * Distance of halo to the font outline. Max text halo width is 1/4 of
		 * the font-size.
		 */
		static String TEXT_HALO_WIDTH = "text-halo-width";
		/**
		 * Optional number. Units in pixels. Defaults to 0. Requires text-field.
		 * The halo’s fadeout distance towards the outside.
		 */
		static String TEXT_HALO_BLUR = "text-halo-blur";
		/**
		 * Optional array. Units in pixels. Defaults to 0,0. Requires
		 * text-field. Distance that the text’s anchor is moved from its
		 * original placement. Positive values indicate right and down, while
		 * negative values indicate left and up.
		 */
		static String TEXT_TRANSLATE = "text-translate";
		/**
		 * Optional enum. One of map, viewport. Defaults to map. Requires
		 * text-field. Requires text-translate. Control whether the translation
		 * is relative to the map (north) or viewport (screen).
		 */
		static String TEXT_TRANSLATE_ANCHOR = "text-translate-anchor";

		public SYMBOL(JsonNode node, JsonNode constants) {
			super(node, constants);
			visible = null;
		}

		boolean isVisible(int zoomLevel) {
			return false;
		}
	}

	public static class RASTER extends StyleDrawingAttributes {
		private static final long serialVersionUID = 1L;
		// PAINT
		/**
		 * Optional number. Defaults to 1. The opacity at which the image will
		 * be drawn.
		 */
		static String RASTER_OPACITY = "raster-opacity";
		/**
		 * Optional number. Units in degrees. Defaults to 0. Rotates hues around
		 * the color wheel.
		 */
		static String RASTER_HUE_ROTATE = "raster-hue-rotate";
		/**
		 * Optional number. Defaults to 0. Increase or reduce the brightness of
		 * the image. The value is the minimum brightness.
		 */
		static String RASTER_BRIGHTNESS_MIN = "raster-brightness-min";
		/**
		 * Optional number. Defaults to 1. Increase or reduce the brightness of
		 * the image. The value is the maximum brightness.
		 */
		static String RASTER_BRIGHTNESS_MAX = "raster-brightness-max";
		/**
		 * Optional number. Defaults to 0. Increase or reduce the saturation of
		 * the image.
		 */
		static String RASTER_SATURATION = "raster-saturation";
		/**
		 * Optional number. Defaults to 0. Increase or reduce the contrast of
		 * the image.
		 */
		static String RASTER_CONTRAST = "raster-contrast";
		/**
		 * Optional number. Units in milliseconds. Defaults to 300. Fade
		 * duration when a new tile is added.
		 */
		static String RASTER_FADE_DURATION = "raster-fade-duration";

		public RASTER(JsonNode node, JsonNode constants) {
			super(node, constants);
			visible = null;
		}

		boolean isVisible(int zoomLevel) {
			return false;
		}
	}

	public static class CIRCLE extends StyleDrawingAttributes {
		private static final long serialVersionUID = 1L;
		// PAINT
		/** Optional number. Units in pixels. Defaults to 5. Circle radius. */
		static String CIRCLE_RADIUS = "circle-radius";
		/** Optional color. Defaults to #000000. The color of the circle. */
		static String CIRCLE_COLOR = "circle-color";
		/**
		 * Optional number. Defaults to 0. Amount to blur the circle. 1 blurs
		 * the circle such that only the centerpoint is full opacity.
		 */
		static String CIRCLE_BLUR = "circle-blur";
		/**
		 * Optional number. Defaults to 1. The opacity at which the circle will
		 * be drawn.
		 */
		static String CIRCLE_OPACITY = "circle-opacity";
		/**
		 * Optional array. Units in pixels. Defaults to 0,0. The geometry’s
		 * offset. Values are [x, y] where negatives indicate left and up,
		 * respectively.
		 */
		static String CIRCLE_TRANSLATE = "circle-translate";
		/**
		 * Optional enum. One of map, viewport. Defaults to map. Requires
		 * circle-translate. Control whether the translation is relative to the
		 * map (north) or viewport (screen)
		 */
		static String CIRCLE_TRANSLATE_ANCHOR = "circle-translate-anchor";

		public CIRCLE(JsonNode node, JsonNode constants) {
			super(node, constants);
			visible = null;
		}

		void configurePaint(JsonNode paint) {
			StyleFunction<Double> opacity = StyleFunction.getDoubleFunction(paint.get(CIRCLE_OPACITY), 1.0, constants);
			StyleFunction<Paint> circlePaints = StyleFunction.getPaintFunction(paint.get(CIRCLE_COLOR), opacity, BLACK, constants);
			StyleFunction<Double> radius = StyleFunction.getDoubleFunction(paint.get(CIRCLE_RADIUS), 5.0, constants);

			setFillPaint(circlePaints);
			setLinePaint(CLEAR);
			setCircleStuff(true, radius);

		}

		void setCircleStuff(boolean pointOval, StyleFunction<Double> radius) {
			for (int zoomLevel = StyleFunction.MIN_ZOOM_LEVEL; zoomLevel < StyleFunction.NUM_ZOOM_LEVELS; zoomLevel++) {
				DrawingAttributes da = renderers.get(zoomLevel);
				da.setPointOval(pointOval);
				da.setPointRadius(radius.get(zoomLevel).intValue());
			}
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
		private static final Logger LOGGER = Logger.getLogger(StyleDrawingAttributes.class.getName());

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
