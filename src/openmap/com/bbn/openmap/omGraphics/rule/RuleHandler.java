/* 
 * <copyright>
 *  Copyright 2015 BBN Technologies
 * </copyright>
 */
package com.bbn.openmap.omGraphics.rule;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import com.bbn.openmap.OMComponent;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.OMTextLabeler;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.PropUtils;

/**
 * The RuleHandler manages a set of Rules and will evaluate OMGraphics against
 * them for a current projection.
 * <p>
 * For List rules, something like this:
 * 
 * <pre>
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
 * neroads.rule1.render.maxScale=500000
 * neroads.rule1.lineColor=FFFFFF
 * neroads.rule1.lineWidth=3
 * neroads.rule1.mattingColor=55AAAAAA
 * 
 * # The render attribute is assumed to be true.  You can hide OMGraphics by setting it to false.
 * </pre>
 * 
 * @author dietrick
 */
public abstract class RuleHandler<T> extends OMComponent {

    List<Rule> rules;

    /**
     * Create a Rule object that knows how to interpret properties to create the
     * proper indices into the record List.
     */
    public abstract Rule createRule();

    /**
     * Return a record Map for a particular OMGraphic, like a properties table.
     * 
     * @param omg OMGraphic being queried
     * @return Map of objects as attributes for the OMGraphic.
     */
    public abstract T getRecordDataForOMGraphic(OMGraphic omg);

    public void setProperties(String prefix, Properties props) {
        super.setProperties(prefix, props);
        prefix = PropUtils.getScopedPropertyPrefix(prefix);

        String rulesString = props.getProperty(prefix + Rule.RuleListProperty);
        Vector<String> keysV = PropUtils.parseSpacedMarkers(rulesString);

        if (keysV != null && !keysV.isEmpty()) {
            List<Rule> rules = Collections.synchronizedList(new LinkedList<Rule>());

            for (String ruleMarker : keysV) {
                Rule rule = createRule();
                rule.setProperties(prefix + ruleMarker, props);
                rules.add(rule);
            }

            setRules(rules);
        }
    }

    public Properties getProperties(Properties props) {
        props = super.getProperties(props);
        String prefix = PropUtils.getScopedPropertyPrefix(this);

        StringBuffer ruleList = new StringBuffer();
        int createdRuleNum = 1;

        for (Rule rule : getRules()) {
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
            props.put(prefix + Rule.RuleListProperty, ruleList.toString());
        }

        return props;
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
            rules = Collections.synchronizedList(new LinkedList<Rule>());
        }
        return rules;
    }

    /**
     * Used to help prevent consecutive repeat label values.
     */
    protected String lastLabel;

    /**
     * This is the main call that a layer would use to modify/update an
     * OMGraphic based on dbf file contents. Tries to retrieve the index from
     * the attributes of the OMGraphic, and then checks the index and OMGraphic
     * to see how/if it should be rendered, as determined by the rules.
     * 
     * @param omg the OMGraphic in question
     * @param labelList an OMGraphicList to add the label to, so it gets
     *        rendered on top.
     * @param proj the current map projection, for scale appropriateness
     *        determinations.
     * @return OMGraphic if it should be displayed, null if it shouldn't.
     */
    public OMGraphic evaluate(OMGraphic omg, OMGraphicList labelList, Projection proj) {

        // Just check for rules first - if no rules defined, don't bother
        // reading the attributes.
        List<Rule> rules = getRules();
        if (rules.isEmpty()) {
            return omg;
        }

        T record = getRecordDataForOMGraphic(omg);
        if (record == null) {
            return omg;
        }

        OMGraphic passedEval = null;

        for (Rule rule : rules) {

            passedEval = rule.evaluate(record, omg, proj);

            if (passedEval != null) {

                /**
                 * Let's do some stuff with a label to minimize the number of
                 * labels that might show up.
                 */
                Object labelObj = omg.getAttribute(OMGraphic.LABEL);
                if (labelObj instanceof OMTextLabeler) {
                    String curLabel = ((OMTextLabeler) labelObj).getData();

                    if (lastLabel == null
                            || (lastLabel != null && !lastLabel.equalsIgnoreCase(curLabel))) {
                        labelList.add((OMTextLabeler) labelObj);
                    } else {
                        // The Rule adds the label to the OMGraphic, we'll
                        // remove it so it doesn't get rendered underneath
                        omg.removeAttribute(OMGraphic.LABEL);
                    }

                    lastLabel = curLabel;
                }
                break;
            }

        }

        // Might be null
        return passedEval;
    }

}
