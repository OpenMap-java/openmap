/* 
 * <copyright>
 *  Copyright 2015 BBN Technologies
 * </copyright>
 */
package com.bbn.openmap.omGraphics.rule;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.bbn.openmap.OMComponent;
import com.bbn.openmap.omGraphics.DrawingAttributes;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicConstants;
import com.bbn.openmap.omGraphics.OMText;
import com.bbn.openmap.omGraphics.OMTextLabeler;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PropUtils;

/**
 * A Rule is an attribute inspector that makes decisions about rendering
 * attributes, information line contents, tooltips and visibility based on
 * scale.
 *
 * @author dietrick
 */
public abstract class Rule<T> extends OMComponent {
    /**
     * The property name where the testing value can be found for the rule to
     * compare against the value.
     */
    protected String keyField;
    protected List<String> tooltipFields;
    protected List<String> infolineFields;
    protected List<String> labelFields;

    /**
     * The value that the query runs the operation against.
     */
    protected Object val;
    protected RuleOp op = RuleOp.NONE;

    protected DrawingAttributes drawingAttributes = new DrawingAttributes();

    protected float displayMinScale = Float.MIN_VALUE;
    protected float displayMaxScale = Float.MAX_VALUE;
    protected float labelMinScale = Float.MIN_VALUE;
    protected float labelMaxScale = Float.MAX_VALUE;

    /**
     * <pre>
     * layer.rules=rule1 rule2 rule3 
     * layer.rule1.key=CAPITAL
     * layer.rule1.op=equals
     * layer.rule1.val=Y
     * layer.rule1.actions=render tooltip infoline 
     * layer.rule1.lineColor=FFFF0000
     * layer.rule1.minScale=10000
     * layer.rule1.maxScale=50000
     * layer.rule1.infoline=CITY_NAME
     * layer.rule1.tooltip=ELEVATION
     * </pre>
     */
    public final static String RuleListProperty = "rules";
    public final static String RuleKeyProperty = "key";
    public final static String RuleOperatorProperty = "op";
    public final static String RuleValueProperty = "val";

    public final static String RuleActionRender = "render";
    public final static String RuleActionTooltip = "tooltip";
    public final static String RuleActionInfoline = "infoline";
    public final static String RuleActionLabel = "label";
    public final static String RuleActionMinScale = "minScale";
    public final static String RuleActionMaxScale = "maxScale";

    /**
     * Asks the Op class to evaluate the retrieved value against the Rules
     * value. The implementation will use the key to pull the testing value out
     * of the record.
     * 
     * @param record object to evaluate
     * @return true of the operation passed
     */
    public abstract boolean evaluate(T record);

    /**
     * Returns a String of concatenated record values.
     * 
     * @param fieldNames a list of string keys for fields to be used.
     * @param record The record object to look up values for the list of keys.
     * @return String of content
     */
    public abstract String getContent(List<String> fieldNames, T record);

    public void setProperties(String prefix, Properties props) {
        super.setProperties(prefix, props);
        prefix = PropUtils.getScopedPropertyPrefix(prefix);

        keyField = props.getProperty(prefix + RuleKeyProperty, keyField);
        tooltipFields = getStringFromFields(props.getProperty(prefix + RuleActionTooltip));
        infolineFields = getStringFromFields(props.getProperty(prefix + RuleActionInfoline));
        labelFields = getStringFromFields(props.getProperty(prefix + RuleActionLabel));

        RuleOp op = RuleOp.resolve(props.getProperty(prefix + RuleOperatorProperty));
        if (op != null) {
            this.op = op;
        }

        Object newVal = props.getProperty(prefix + RuleValueProperty);
        if (newVal != null) {
            val = newVal;
        }

        if (keyField == null) {
            Debug.output("No key for rule (" + prefix + ") found in properties.");
        }

        displayMinScale = PropUtils.floatFromProperties(props, prefix + RuleActionRender + "."
                + RuleActionMinScale, displayMinScale);
        displayMaxScale = PropUtils.floatFromProperties(props, prefix + RuleActionRender + "."
                + RuleActionMaxScale, displayMaxScale);
        labelMinScale = PropUtils.floatFromProperties(props, prefix + RuleActionLabel + "."
                + RuleActionMinScale, labelMinScale);
        labelMaxScale = PropUtils.floatFromProperties(props, prefix + RuleActionLabel + "."
                + RuleActionMaxScale, labelMaxScale);

        // Assume that the OMGraphic will be rendered, with defaults if not
        // specified. render has to be set to false to hide OMGraphic
        boolean renderProperties = PropUtils.booleanFromProperties(props, prefix + RuleActionRender, drawingAttributes != null);
        if (renderProperties) {
            if (drawingAttributes == null) {
                drawingAttributes = new DrawingAttributes();
            }
            drawingAttributes.setProperties(prefix, props);
        } else {
            drawingAttributes = null;
        }

    }

    public Properties getProperties(Properties props) {
        props = super.getProperties(props);

        String prefix = PropUtils.getScopedPropertyPrefix(this);
        props.put(prefix + RuleKeyProperty, PropUtils.unnull(keyField));

        if (tooltipFields != null && !tooltipFields.isEmpty()) {
            props.put(prefix + RuleActionTooltip, getFieldsAsString(tooltipFields));
        }
        if (infolineFields != null && !infolineFields.isEmpty()) {
            props.put(prefix + RuleActionInfoline, getFieldsAsString(infolineFields));
        }
        if (labelFields != null && !labelFields.isEmpty()) {
            props.put(prefix + RuleActionLabel, getFieldsAsString(labelFields));
        }

        if (this.op != null) {
            props.put(prefix + RuleOperatorProperty, this.op.getPropertyNotation());
        }
        if (val != null) {
            props.put(prefix + RuleValueProperty, PropUtils.unnull(val.toString()));
        }

        if (displayMinScale != Float.MIN_VALUE) {
            props.put(prefix + RuleActionRender + "." + RuleActionMinScale, Float.toString(displayMinScale));
        }
        if (displayMaxScale != Float.MAX_VALUE) {
            props.put(prefix + RuleActionRender + "." + RuleActionMaxScale, Float.toString(displayMaxScale));
        }
        if (labelMinScale != Float.MIN_VALUE) {
            props.put(prefix + RuleActionLabel + "." + RuleActionMinScale, Float.toString(labelMinScale));
        }
        if (labelMaxScale != Float.MAX_VALUE) {
            props.put(prefix + RuleActionLabel + "." + RuleActionMaxScale, Float.toString(labelMaxScale));
        }

        if (drawingAttributes != null) {
            props.put(prefix + RuleActionRender, Boolean.toString(true));
            drawingAttributes.getProperties(props);
        }

        return props;
    }

    /**
     * Evaluate the record against this rule.
     * 
     * @param record A Map of attributes for a particular OMGraphic/map object.
     *        The indices for the rule are indexes into this record.
     * @param omg The OMGraphic to evaluate.
     * @param proj The current map projection.
     * @return the OMGraphic if it should be drawn, null if it shouldn't.
     */
    public OMGraphic evaluate(T record, OMGraphic omg, Projection proj) {

        if (evaluate(record)) {

            float scale = 0f;

            if (proj != null) {
                scale = proj.getScale();

                if (scale < displayMinScale || scale > displayMaxScale) {
                    // We met the rule, it's telling us not to display.
                    return null;
                }
            }

            if (infolineFields != null) {
                omg.putAttribute(OMGraphicConstants.INFOLINE, getContent(infolineFields, record));
            }
            if (tooltipFields != null) {
                omg.putAttribute(OMGraphicConstants.TOOLTIP, getContent(tooltipFields, record));
            }
            if (labelFields != null && scale >= labelMinScale && scale <= labelMaxScale) {
                String curLabel = getContent(labelFields, record);

                OMTextLabeler label = new OMTextLabeler(curLabel, OMText.JUSTIFY_CENTER);
                // Needs to get added to the OMGraphic so it gets
                // generated with the projection at the right point.
                omg.putAttribute(OMGraphicConstants.LABEL, label);
            }

            if (drawingAttributes != null) {
                drawingAttributes.setTo(omg);
            }
            omg.setVisible(drawingAttributes != null);

            if (getLogger().isLoggable(Level.FINE)) {
                getLogger().fine(this.getPropertyPrefix() + " being assigned to "
                        + op.getClass().getName() + " " + keyField + " " + val + " vs "
                        + ((Map) record).get(keyField));

                omg.putAttribute("RULE", getPropertyPrefix());
            }

            return omg;
        }

        return null;
    }

    /**
     * Returns a String of concatenated record values.
     * 
     * @param fieldNames names of field properties
     * @return fields as single string
     */
    public String getFieldsAsString(List<String> fieldNames) {
        StringBuffer buf = new StringBuffer();
        if (fieldNames != null) {
            for (String field : fieldNames) {
                buf.append(PropUtils.unnull(field)).append(" ");
            }
        }
        // Might be more than just that last ""
        return buf.toString().trim();
    }

    /**
     * Create a List of Strings from a list of space separated strings.
     * 
     * @param fieldString
     * @return List if fieldString can be parsed, null if fieldString is null.
     */
    public List<String> getStringFromFields(String fieldString) {
        if (fieldString != null && !fieldString.isEmpty()) {
            return PropUtils.parseSpacedMarkers(fieldString);
        }
        return null;
    }

    public DrawingAttributes getDrawingAttribtues() {
        return drawingAttributes;
    }

    public void setDrawingAttributes(DrawingAttributes da) {
        this.drawingAttributes = da;
    }

    public float getDisplayMaxScale() {
        return displayMaxScale;
    }

    public void setDisplayMaxScale(float displayMaxScale) {
        this.displayMaxScale = displayMaxScale;
    }

    public float getDisplayMinScale() {
        return displayMinScale;
    }

    public void setDisplayMinScale(float displayMinScale) {
        this.displayMinScale = displayMinScale;
    }

    public String getKeyName() {
        return keyField;
    }

    public void setKeyName(String keyName) {
        this.keyField = keyName;
    }

    public List<String> getLabelFields() {
        return labelFields;
    }

    public void setLabelFields(List<String> labelFields) {
        this.labelFields = labelFields;
    }

    public List<String> getInfolineFields() {
        return infolineFields;
    }

    public void setInfolineFields(List<String> infolineFields) {
        this.infolineFields = infolineFields;
    }

    public List<String> getTooltipFields() {
        return tooltipFields;
    }

    public void setTooltipFields(List<String> tooltipFields) {
        this.tooltipFields = tooltipFields;
    }

    public float getLabelMaxScale() {
        return labelMaxScale;
    }

    public void setLabelMaxScale(float labelMaxScale) {
        this.labelMaxScale = labelMaxScale;
    }

    public float getLabelMinScale() {
        return labelMinScale;
    }

    public void setLabelMinScale(float labelMinScale) {
        this.labelMinScale = labelMinScale;
    }

    public RuleOp getOp() {
        return op;
    }

    public void setOp(RuleOp op) {
        this.op = op;
    }

    public Object getVal() {
        return val;
    }

    public void setVal(Object val) {
        this.val = val;
    }

    /**
     * Holder for this class's Logger. This allows for lazy initialization of
     * the logger.
     */
    private static final class LoggerHolder {
        /**
         * The logger for this class
         */
        private static final Logger LOGGER = Logger.getLogger(Rule.class.getName());

        /**
         * Prevent instantiation
         */
        private LoggerHolder() {
            throw new AssertionError("This should never be instantiated");
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

}
