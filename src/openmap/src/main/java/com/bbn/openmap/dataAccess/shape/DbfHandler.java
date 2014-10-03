//**********************************************************************
//
//<copyright>
//
//BBN Technologies
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
//$RCSfile: DbfHandler.java,v $
//$Revision: 1.7 $
//$Date: 2008/11/11 00:35:52 $
//$Author: dietrick $
//
//**********************************************************************

package com.bbn.openmap.dataAccess.shape;

import java.io.IOException;
import java.text.DecimalFormatSymbols;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Vector;

import com.bbn.openmap.OMComponent;
import com.bbn.openmap.io.BinaryFile;
import com.bbn.openmap.io.FormatException;
import com.bbn.openmap.omGraphics.DrawingAttributes;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicConstants;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.OMText;
import com.bbn.openmap.omGraphics.OMTextLabeler;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PropUtils;

/**
 * The DbfHandler manages OMGraphics based on DBF file settings. It uses Rules
 * to evaluate DBF record information for each OMGraphic that is given to it,
 * and can set rendering settings, labels, visibility and information text based
 * on those rules. If rules aren't defined, then the dbf file won't be read for
 * each entry and any evaluation attempt will just return the OMGraphic as it
 * was provided.
 * 
 * <P>
 * Properties as an example used within the ShapeLayer:
 * 
 * <pre>
 * 
 * neroads.class=com.bbn.openmap.layer.shape.ShapeLayer
 * neroads.prettyName=Roads
 * neroads.shapeFile=roads.shp
 * neroads.mouseModes=Gestures
 * # Rule marker names specified in space-separated list
 * neroads.rules=rule0 rule1
 * # global scale settings can be used so work is only performed within scale range of minScale/maxScale
 * neroads.maxScale=1000000f
 * 
 * # rule0 definition:
 * # CLASS_RTE is a DBF column name
 * neroads.rule0.key=CLASS_RTE
 * # operation, if key value is less than 2
 * neroads.rule0.op=lt
 * neroads.rule0.val=2
 * # If rule is met, then actions can be performed:
 * # Column names can be added together in a label by specifying them in a space-separated list
 * neroads.rule0.label=PREFIX PRETYPE NAME TYPE SUFFIX
 * # Labels can have scale limits imposed, so they don't appear if map scale is 
 * # greater than maxScale or less than minScale
 * neroads.rule0.label.maxScale=1000000
 * # Visibility can be controlled with respect to scale as well
 * neroads.rule0.render=true
 * neroads.rule0.render.maxScale=1000000
 * # Rendering attributes can be specified.
 * neroads.rule0.lineColor=FFFA73
 * neroads.rule0.lineWidth=4
 * neroads.rule0.mattingColor=55AAAAAA
 * 
 * # rule1 definition:
 * neroads.rule1.key=CLASS_RTE
 * neroads.rule1.op=all
 * neroads.rule1.label=PREFIX PRETYPE NAME TYPE SUFFIX
 * neroads.rule1.label.maxScale=200000
 * neroads.rule1.render=true
 * neroads.rule1.render.maxScale=500000
 * neroads.rule1.lineColor=FFFFFF
 * neroads.rule1.lineWidth=3
 * neroads.rule1.mattingColor=55AAAAAA
 * 
 * </pre>
 * 
 * @author dietrick
 */
public class DbfHandler
        extends OMComponent {

    protected DbfFile dbf;
    protected List<Rule> rules;
    protected DrawingAttributes defaultDA;

    /*
     * <pre> layer.tooltip=ELEVATION layer.infoline=CITY_NAME
     * layer.lineColor=FF000000 layer.actions=tooltip infoline
     * 
     * layer.rules=rule1 rule2 rule3 layer.rule1.key=CAPITAL
     * layer.rule1.op=equals layer.rule1.val=Y layer.rule1.actions=render
     * tooltip infoline layer.rule1.lineColor=FFFF0000 </pre>
     */
    public final static String RuleListProperty = "rules";
    public final static String RuleKeyColumnProperty = "key";
    public final static String RuleOperatorProperty = "op";
    public final static String RuleValueProperty = "val";

    public final static String RuleActionRender = "render";
    public final static String RuleActionTooltip = "tooltip";
    public final static String RuleActionInfoline = "infoline";
    public final static String RuleActionLabel = "label";
    public final static String RuleActionMinScale = "minScale";
    public final static String RuleActionMaxScale = "maxScale";

    protected DbfHandler() {
        defaultDA = new DrawingAttributes();
    }

    public DbfHandler(String dbfFilePath)
            throws IOException, FormatException {
        this(new BinaryFile(dbfFilePath));
    }

    public DbfHandler(BinaryFile bf)
            throws IOException, FormatException {
        this();
        dbf = new DbfFile(bf);
        dbf.close();
    }

    public void setProperties(String prefix, Properties props) {
        super.setProperties(prefix, props);
        prefix = PropUtils.getScopedPropertyPrefix(prefix);

        defaultDA.setProperties(prefix, props);

        String rulesString = props.getProperty(prefix + RuleListProperty);
        Vector<String> keysV = PropUtils.parseSpacedMarkers(rulesString);
        List<Rule> rules = getRules();
        for (Iterator<String> it = keysV.iterator(); it.hasNext();) {
            String ruleMarker = it.next();

            Rule rule = new Rule(dbf);
            rule.setProperties(prefix + ruleMarker, props);
            rules.add(rule);
        }
    }

    public Properties getProperties(Properties props) {
        props = super.getProperties(props);
        String prefix = PropUtils.getScopedPropertyPrefix(this);
        defaultDA.getProperties(props);
        StringBuffer ruleList = new StringBuffer();
        int createdRuleNum = 1;

        for (Iterator<Rule> it = getRuleIterator(); it.hasNext();) {
            Rule rule = it.next();
            String rulePrefix = rule.getPropertyPrefix();

            // For rules created programmatically without a prefix, need to
            // create one.
            if (rulePrefix == null) {
                rulePrefix = "createdRulePrefix" + (createdRuleNum++);
                rule.setPropertyPrefix(prefix + rulePrefix);
            }

            if (rulePrefix.startsWith(prefix)) {
                rulePrefix = rulePrefix.substring(prefix.length());
                if (rulePrefix.startsWith(".")) {
                    rulePrefix = rulePrefix.substring(1);
                }
            }

            ruleList.append(rulePrefix).append(" ");

            rule.getProperties(props);
        }

        if (ruleList.length() > 0) {
            props.put(prefix + RuleListProperty, ruleList.toString());
        }

        return props;
    }

    public DbfFile getDbf() {
        return dbf;
    }

    public void setDbf(DbfFile dbf) {
        this.dbf = dbf;
    }

    public DrawingAttributes getDefaultDA() {
        return defaultDA;
    }

    public void setDefaultDA(DrawingAttributes defaultDA) {
        this.defaultDA = defaultDA;
    }

    public void setRules(List<Rule> rules) {
        this.rules = rules;
    }

    public void addRule(Rule rule) {
        if (rule != null) {
            getRules().add(rule);
        }
    }

    public boolean removeRule(Rule rule) {
        if (rule != null) {
            return getRules().remove(rule);
        }

        return false;
    }

    public void clearRules() {
        getRules().clear();
    }

    public List<Rule> getRules() {
        if (rules == null) {
            rules = new Vector<Rule>();
        }
        return rules;
    }

    /**
     * Return an iterator for all of the Rules that the DbfHandler knows about.
     * 
     * @return Iterator over Rules
     */
    public Iterator<Rule> getRuleIterator() {
        return getRules().iterator();
    }

    /**
     * Tells the DbfFile to close the file pointer to the data. Will reopen if
     * needed.
     */
    public void close() {
        if (dbf != null) {
            dbf.close();
        }
    }

    /**
     * Used to help prevent consecutive repeat label values.
     */
    protected String lastLabel;

    /**
     * This is the main call that a layer would use to modify/update an
     * OMGraphic based on dbf file contents. Trys to retrieve the index from the
     * attributes of the OMGraphic, and then checks the index and OMGraphic to
     * see how/if it should be rendered, as determined by the rules.
     * 
     * @param omg the OMGraphic in question
     * @param labelList an OMGraphicList to add the label to, so it gets
     *        rendered on top.
     * @param proj the current map projection, for scale appropriateness
     *        determinations.
     * @return OMGraphic if it should be displayed, null if it shouldn't.
     */
    public OMGraphic evaluate(OMGraphic omg, OMGraphicList labelList, Projection proj) {
        Object obj = omg.getAttribute(ShapeConstants.SHAPE_INDEX_ATTRIBUTE);

        if (obj instanceof Integer) {
            Integer index = (Integer) obj;
            return evaluate(index.intValue(), omg, labelList, proj);
        }

        return omg;
    }

    /**
     * This is the main call that a layer would use to modify/update an
     * OMGraphic based on dbf file contents. Checks the index and OMGraphic to
     * see how/if it should be rendered, as determined by the rules.
     * 
     * @param index the index of the OMGraphic in the shape/dbf file.
     * @param omg the OMGraphic in question
     * @param labelList an OMGraphicList to add the label to, so it gets
     *        rendered on top.
     * @param proj the current map projection, for scale appropriateness
     *        determinations.
     * @return OMGraphic if it should be displayed, null if it shouldn't.
     */
    public OMGraphic evaluate(int index, OMGraphic omg, OMGraphicList labelList, Projection proj) {

        List<Rule> rules = getRules();
        if (rules.isEmpty()) {
            return omg;
        }

        try {
            List record = dbf.getRecordData(index);
            for (Iterator<Rule> it = getRuleIterator(); it.hasNext();) {
                Rule rule = (Rule) it.next();

                Object recVal = record.get(rule.keyIndex);
                if (rule.evaluate(recVal)) {

                    float scale = 0f;

                    if (proj != null) {
                        scale = proj.getScale();

                        if (scale < rule.displayMinScale || scale > rule.displayMaxScale) {
                            // We met the rule, it's telling us not to display.
                            return null;
                        }
                    }

                    if (rule.infolineIndicies != null) {
                        omg.putAttribute(OMGraphicConstants.INFOLINE, getContentFromIndicies(rule.infolineIndicies, record));
                    }
                    if (rule.tooltipIndicies != null) {
                        omg.putAttribute(OMGraphicConstants.TOOLTIP, getContentFromIndicies(rule.tooltipIndicies, record));
                    }
                    if (rule.labelIndicies != null && scale >= rule.labelMinScale && scale <= rule.labelMaxScale) {

                        String curLabel = getContentFromIndicies(rule.labelIndicies, record);

                        if (lastLabel == null || (lastLabel != null && !lastLabel.equalsIgnoreCase(curLabel))) {

                            OMTextLabeler label = new OMTextLabeler(curLabel, OMText.JUSTIFY_CENTER);
                            // Needs to get added to the OMGraphic so it gets
                            // generated with the projection at the right point.
                            omg.putAttribute(OMGraphicConstants.LABEL, label);
                            labelList.add(label);
                        }

                        lastLabel = curLabel;
                    }
                    if (rule.da != null) {
                        rule.da.setTo(omg);
                    }

                    break;
                }
            }
        } catch (IOException ioe) {

        } catch (FormatException fe) {

        }

        return omg;

    }

    /**
     * Returns a String of concatenated record values.
     * 
     * @param indicies column indexes of values to be concatenated in return
     *        value
     * @param record List to use for return value
     * @return String of content
     */
    public String getContentFromIndicies(int[] indicies, List record) {
        int numIndicies = indicies.length;
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < numIndicies; i++) {
            int val = indicies[i];
            if (val != -1) {
                buf.append((String) record.get(val)).append(" ");
            }
        }
        return buf.toString().trim();
    }

    public class Rule
            extends OMComponent {
        protected DbfFile dbf;
        /**
         * The column index where the testing value can be found for the rule to
         * compare against the value.
         */
        protected int keyIndex = -1;
        /**
         * The value that the query runs the operation against.
         */
        protected Object val;
        protected Op op = Op.NONE;

        /* set non-null if rendering action chosen for rule. */
        protected DrawingAttributes da;
        protected int[] tooltipIndicies;
        protected int[] infolineIndicies;
        protected int[] labelIndicies;

        protected float displayMinScale = Float.MIN_VALUE;
        protected float displayMaxScale = Float.MAX_VALUE;
        protected float labelMinScale = Float.MIN_VALUE;
        protected float labelMaxScale = Float.MAX_VALUE;

        public Rule(DbfFile dbfFile) {
            dbf = dbfFile;
        }

        public void setProperties(String prefix, Properties props) {
            super.setProperties(prefix, props);
            prefix = PropUtils.getScopedPropertyPrefix(prefix);

            String key = props.getProperty(prefix + RuleKeyColumnProperty);
            keyIndex = dbf.getColumnIndexForName(key);

            Op op = Op.resolve(props.getProperty(prefix + RuleOperatorProperty));
            if (op != null) {
                this.op = op;
            }

            Object newVal = props.getProperty(prefix + RuleValueProperty);
            if (newVal != null) {
                val = newVal;
            }

            if (key == null) {
                Debug.output("No key for rule (" + prefix + ") found in properties.");
            }

            displayMinScale =
                    PropUtils.floatFromProperties(props, prefix + RuleActionRender + "." + RuleActionMinScale, displayMinScale);
            displayMaxScale =
                    PropUtils.floatFromProperties(props, prefix + RuleActionRender + "." + RuleActionMaxScale, displayMaxScale);
            labelMinScale =
                    PropUtils.floatFromProperties(props, prefix + RuleActionLabel + "." + RuleActionMinScale, labelMinScale);
            labelMaxScale =
                    PropUtils.floatFromProperties(props, prefix + RuleActionLabel + "." + RuleActionMaxScale, labelMaxScale);

            tooltipIndicies = getIndicies(prefix + RuleActionTooltip, props);
            infolineIndicies = getIndicies(prefix + RuleActionInfoline, props);
            labelIndicies = getIndicies(prefix + RuleActionLabel, props);
            da = null;

            boolean renderProperties = PropUtils.booleanFromProperties(props, prefix + RuleActionRender, false);

            if (renderProperties) {
                da = new DrawingAttributes();
                da.setProperties(prefix, props);
            }

        }

        public Properties getProperties(Properties props) {
            props = super.getProperties(props);

            if (dbf == null) {
                return props;
            }

            String prefix = PropUtils.getScopedPropertyPrefix(this);
            props.put(prefix + RuleKeyColumnProperty, PropUtils.unnull(dbf.getColumnName(keyIndex)));
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

            if (tooltipIndicies != null && tooltipIndicies.length > 0) {
                props.put(prefix + RuleActionTooltip, getColumnNamesFromIndicies(tooltipIndicies));
            }
            if (infolineIndicies != null && infolineIndicies.length > 0) {
                props.put(prefix + RuleActionInfoline, getColumnNamesFromIndicies(infolineIndicies));
            }
            if (labelIndicies != null && labelIndicies.length > 0) {
                props.put(prefix + RuleActionLabel, getColumnNamesFromIndicies(labelIndicies));
            }

            if (da != null) {
                props.put(prefix + RuleActionRender, Boolean.toString(true));
                da.getProperties(props);
            }

            return props;
        }

        /**
         * Asks the Op class to evaluate the provided value against the Rules
         * value.
         * 
         * @param val
         * @return true of the operation passed
         */
        public boolean evaluate(Object val) {
            return op.evaluate(this.val, val);
        }

        /**
         * Given a prefix + ActionProperty, get the column names listed as the
         * property value and figure out what the indexes of the columns are.
         * 
         * @param actionProperty prefix + ActionProperty
         * @param props
         * @return int array of column indexes in the dbf file reflecting the
         *         order and number of column names listed as the property
         *         value.
         */
        public int[] getIndicies(String actionProperty, Properties props) {
            int[] indicies = null;
            String actionLabel = props.getProperty(actionProperty);
            if (actionLabel != null) {
                Vector<String> columnNames = PropUtils.parseSpacedMarkers(actionLabel);
                int numCols = columnNames.size();
                indicies = new int[numCols];

                for (int i = 0; i < numCols; i++) {
                    String columnName = (String) columnNames.get(i);
                    indicies[i] = dbf.getColumnIndexForName(columnName);
                }
            }

            return indicies;
        }

        /**
         * Given a prefix + ActionProperty, get the column names listed as the
         * property value and figure out what the indexes of the columns are.
         * 
         * @param indicies int[] of column indexes in the dbf file reflecting
         *        the order and number of column names to be listed as a
         *        property value.
         * @return String for use in properties of space-separated column names.
         */
        public String getColumnNamesFromIndicies(int[] indicies) {
            StringBuffer buf = new StringBuffer();
            int numCols = indicies.length;
            for (int i = 0; i < numCols; i++) {
                buf.append(dbf.getColumnName(indicies[i])).append(" ");
            }

            return buf.toString().trim();
        }

        public DrawingAttributes getDa() {
            return da;
        }

        public void setDa(DrawingAttributes da) {
            this.da = da;
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

        public int[] getInfolineIndicies() {
            return infolineIndicies;
        }

        public void setInfolineIndicies(int[] infolineIndicies) {
            this.infolineIndicies = infolineIndicies;
        }

        public int getKeyIndex() {
            return keyIndex;
        }

        public void setKeyIndex(int keyIndex) {
            this.keyIndex = keyIndex;
        }

        public int[] getLabelIndicies() {
            return labelIndicies;
        }

        public void setLabelIndicies(int[] labelIndicies) {
            this.labelIndicies = labelIndicies;
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

        public Op getOp() {
            return op;
        }

        public void setOp(Op op) {
            this.op = op;
        }

        public int[] getTooltipIndicies() {
            return tooltipIndicies;
        }

        public void setTooltipIndicies(int[] tooltipIndicies) {
            this.tooltipIndicies = tooltipIndicies;
        }

        public Object getVal() {
            return val;
        }

        public void setVal(Object val) {
            this.val = val;
        }

    }

    /**
     * The Op class (operation) is used by the Rules to evaluate a rule key
     * value against a OMGraphics key value.
     * 
     * @author dietrick
     */
    public abstract static class Op {

        protected String description;
        protected String propertyNotation;

        /**
         * equals: equals
         */
        public final static Op EQUALS = new Op("equals", "equals") {
            public boolean compare(int kvcr) {
                return kvcr == 0;
            }
        };
        /**
         * lt: less than
         */
        public final static Op LESS_THAN = new Op("less than", "lt") {
            public boolean compare(int kvcr) {
                return kvcr > 0;
            }
        };
        /**
         * lte: less than or equals
         */
        public final static Op LESS_THAN_EQUALS = new Op("less than or equals", "lte") {
            public boolean compare(int kvcr) {
                return kvcr == 0 || kvcr > 0;
            }
        };
        /**
         * gt: greater than
         */
        public final static Op GREATER_THAN = new Op("greater than", "gt") {
            public boolean compare(int kvcr) {
                return kvcr < 0;
            }
        };
        /**
         * gte: greater than or equals
         */
        public final static Op GREATER_THAN_EQUALS = new Op("greater than or equals", "gte") {
            public boolean compare(int kvcr) {
                return kvcr == 0 || kvcr < 0;
            }
        };
        /**
         * ne: not equals
         */
        public final static Op NOT_EQUALS = new Op("not equals", "ne") {
            public boolean compare(int kvcr) {
                return kvcr != 0;
            }
        };
        /**
         * noop: no-op (nothing passes rule)
         */
        public final static Op NONE = new Op("no-op", "noop") {
            public boolean compare(int kvcr) {
                return false;
            }
        };
        /**
         * all: all (everything passes rule)
         */
        public final static Op ALL = new Op("all", "all") {
            public boolean compare(int kvcr) {
                return true;
            }
        };
        /**
         * starts: starts with
         */
        public final static Op STARTS_WITH = new Op("starts with", "starts") {
            public boolean compare(int kvcr) {
                return kvcr == 0;
            }

            public boolean evaluate(Object key, Object val) {
                return (val.toString()).startsWith(key.toString());
            }
        };
        /**
         * ends: ends with
         */
        public final static Op ENDS_WITH = new Op("ends with", "ends") {
            public boolean compare(int kvcr) {
                return kvcr == 0;
            }

            public boolean evaluate(Object key, Object val) {
                return (val.toString()).endsWith(key.toString());
            }
        };

        public Op(String desc, String propNotation) {
            this.description = desc;
            this.propertyNotation = propNotation;
        }

        public boolean evaluate(Object key, Object val) {
            int compare = 0;
            if (key == null) {
                return true;
            }
            if (val instanceof String) {
                compare = ((String) key).compareTo(val.toString());
            } else if (val instanceof Double) {
                if (key instanceof String) {
                    java.text.DecimalFormat df = new java.text.DecimalFormat();
                    DecimalFormatSymbols dfs = new DecimalFormatSymbols(Locale.ENGLISH);
                    df.setDecimalFormatSymbols(dfs);
                    try {
                        key = new Double(df.parse((String) key).doubleValue());
                    } catch (java.text.ParseException pe) {
                    }
                }

                compare = ((Double) key).compareTo((Double) val);
            }

            return compare(compare);
        }

        public abstract boolean compare(int keyValcompareResult);

        public static Op[] POSSIBLES = new Op[] {
            EQUALS,
            GREATER_THAN,
            GREATER_THAN_EQUALS,
            LESS_THAN,
            LESS_THAN_EQUALS,
            NOT_EQUALS,
            NONE,
            ALL,
            STARTS_WITH,
            ENDS_WITH
        };

        public static Op resolve(String opString) {
            if (opString == null) {
                return null;
            }

            for (int i = 0; i < POSSIBLES.length; i++) {
                Op cur = POSSIBLES[i];
                if (cur.propertyNotation.equalsIgnoreCase(opString)) {
                    return cur;
                }
            }
            return null;
        }

        public String getDescription() {
            return description;
        }

        public String getPropertyNotation() {
            return propertyNotation;
        }

    }
}
