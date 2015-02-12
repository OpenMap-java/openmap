/* 
 * <copyright>
 *  Copyright 2015 BBN Technologies
 * </copyright>
 */
package com.bbn.openmap.omGraphics.rule;

import java.util.List;
import java.util.Properties;
import java.util.Vector;

import com.bbn.openmap.OMComponent;
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
public abstract class IndexRule extends Rule<List> {
    /**
     * The column index where the testing value can be found for the rule to
     * compare against the value.
     */
    protected int keyIndex = -1;
    protected int[] tooltipIndicies;
    protected int[] infolineIndicies;
    protected int[] labelIndicies;

    /**
     * A record List for the attributes of an OMGraphic might have a name for
     * each entry. Given a name, provide the index into the List to get that
     * attribute value. NOTE: This method will be overridden by subclasses that
     * read data and create attribute data structures.
     * 
     * @param columnName name of a attribute in a List, like a column name of a
     *        list of lists.
     * @return the int index of the entry in the record List.
     */
    public abstract int getRecordColumnIndexForName(String columnName);

    /**
     * Provide the title of the attribute at a specific entry. NOTE: This method
     * will be overridden by subclasses that read data and create attribute data
     * structures.
     * 
     * @param index into the record List.
     * @return the record List name for that index.
     */
    public abstract String getRecordColumnName(int index);

    /**
     * Asks the Op class to evaluate the provided value against the Rules value.
     * 
     * @param val
     * @return true of the operation passed
     */
    public boolean evaluate(List record) {
        Object recVal = record.get(keyIndex);
        return op.evaluate(this.val, recVal);
    }

    /**
     * Returns a String of concatenated record values. This method will work,
     * but another method can be called that takes indices that will save a
     * lookup step.
     * 
     * @param fieldNames name of columns
     * @param record List to use for return value
     * @return String of content
     */
    public String getContent(List<String> fieldNames, List record) {
        StringBuffer buf = new StringBuffer();
        if (fieldNames != null) {
            for (String field : fieldNames) {
                int index = getRecordColumnIndexForName(field);
                buf.append(PropUtils.unnull(record.get(index))).append(" ");
            }
        }
        // Might be more than just that last ""
        return buf.toString().trim();
    }

    public void setProperties(String prefix, Properties props) {
        super.setProperties(prefix, props);
        prefix = PropUtils.getScopedPropertyPrefix(prefix);

        String key = props.getProperty(prefix + RuleKeyProperty);
        keyIndex = getRecordColumnIndexForName(key);

        tooltipIndicies = getIndicies(tooltipFields);
        infolineIndicies = getIndicies(infolineFields);
        labelIndicies = getIndicies(labelFields);
    }

    /**
     * Evaluate the record against this rule.
     * 
     * @param record A List of attributes for a particular OMGraphic/map object.
     *        The indices for the rule are indexes into this record.
     * @param omg The OMGraphic to evaluate.
     * @param proj The current map projection.
     * @return the OMGraphic if it should be drawn, null if it shouldn't.
     */
    public OMGraphic evaluate(List record, OMGraphic omg, Projection proj) {

        if (evaluate(record)) {

            float scale = 0f;

            if (proj != null) {
                scale = proj.getScale();

                if (scale < displayMinScale || scale > displayMaxScale) {
                    // We met the rule, it's telling us not to display.
                    return null;
                }
            }

            if (infolineIndicies != null) {
                omg.putAttribute(OMGraphicConstants.INFOLINE, getContentFromIndicies(infolineIndicies, record));
            }
            if (tooltipIndicies != null) {
                omg.putAttribute(OMGraphicConstants.TOOLTIP, getContentFromIndicies(tooltipIndicies, record));
            }
            if (labelIndicies != null && scale >= labelMinScale && scale <= labelMaxScale) {
                String curLabel = getContentFromIndicies(labelIndicies, record);

                OMTextLabeler label = new OMTextLabeler(curLabel, OMText.JUSTIFY_CENTER);
                // Needs to get added to the OMGraphic so it gets
                // generated with the projection at the right point.
                omg.putAttribute(OMGraphicConstants.LABEL, label);
            }

            if (drawingAttributes != null) {
                drawingAttributes.setTo(omg);
            }
            omg.setVisible(drawingAttributes != null);

            return omg;
        }

        return null;
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

    /**
     * Given a prefix + ActionProperty, get the column names listed as the
     * property value and figure out what the indexes of the columns are.
     * 
     * @param fieldNames a List of Strings for property title
     * @return int array of column indexes in the dbf file reflecting the order
     *         and number of column names listed as the property value.
     */
    public int[] getIndicies(List<String> fieldNames) {
        int[] indicies = null;
        if (fieldNames != null) {
            int numCols = fieldNames.size();
            indicies = new int[numCols];
            int i = 0;
            for (String columnName : fieldNames) {
                indicies[i++] = getRecordColumnIndexForName(columnName);
            }
        }

        return indicies;
    }

    /**
     * Given a prefix + ActionProperty, get the column names listed as the
     * property value and figure out what the indexes of the columns are.
     * 
     * @param indicies int[] of column indexes in the dbf file reflecting the
     *        order and number of column names to be listed as a property value.
     * @return String for use in properties of space-separated column names.
     */
    public String getColumnNamesFromIndicies(int[] indicies) {
        StringBuffer buf = new StringBuffer();
        int numCols = indicies.length;
        for (int i = 0; i < numCols; i++) {
            buf.append(getRecordColumnName(indicies[i])).append(" ");
        }

        return buf.toString().trim();
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

    public int[] getTooltipIndicies() {
        return tooltipIndicies;
    }

    public void setTooltipIndicies(int[] tooltipIndicies) {
        this.tooltipIndicies = tooltipIndicies;
    }
}
