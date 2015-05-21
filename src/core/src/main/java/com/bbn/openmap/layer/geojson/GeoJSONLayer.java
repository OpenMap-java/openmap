/* 
 * <copyright>
 *  Copyright 2015 BBN Technologies
 * </copyright>
 */
package com.bbn.openmap.layer.geojson;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Properties;

import com.bbn.openmap.layer.OMGraphicHandlerLayer;
import com.bbn.openmap.layer.policy.ListResetPCPolicy;
import com.bbn.openmap.omGraphics.DrawingAttributes;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.rule.MapRule;
import com.bbn.openmap.omGraphics.rule.Rule;
import com.bbn.openmap.omGraphics.rule.RuleHandler;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.PropUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * GeoJSONLayer displays geometries loaded from a GeoJSON document.
 *
 * @author dietrick
 */
public class GeoJSONLayer extends OMGraphicHandlerLayer {

    public final static String SOURCE_PROPERTY = "source";

    /**
     * RuleHandler to apply to OMGraphics for rendering and action controls.
     */
    GeoJSONRuleHandler rules = new GeoJSONRuleHandler();
    /**
     * OMGraphics converted from JSON. Read once, then checked every projection
     * change.
     */
    OMGraphic convertedJSON = null;
    /**
     * The JSON source to display (URL).
     */
    String jsonFile = null;

    /**
     * Default rendering attributes for converted JSON OMGraphics before the
     * rules are applied.
     */
    DrawingAttributes defaultDrawingAttributes = DrawingAttributes.getDefaultClone();

    public GeoJSONLayer() {
        setProjectionChangePolicy(new ListResetPCPolicy(this));
        setMouseModeIDsForEvents(new String[] { "Gestures" });
    }

    public void setProperties(String prefix, Properties props) {
        super.setProperties(prefix, props);
        rules.setProperties(prefix, props);

        defaultDrawingAttributes.setProperties(prefix, props);

        String jsonFileString = props.getProperty(PropUtils.getScopedPropertyPrefix(prefix)
                + SOURCE_PROPERTY);
        if (jsonFileString != null) {
            setJsonFile(jsonFileString);
        }
    }

    public Properties getProperties(Properties props) {
        props = super.getProperties(props);
        rules.getProperties(props);
        defaultDrawingAttributes.getProperties(props);
        props.setProperty(PropUtils.getScopedPropertyPrefix(this) + SOURCE_PROPERTY, getJsonFile());
        return props;
    }

    protected OMGraphicList checkRules(OMGraphic omgraphic, OMGraphicList labelList, Projection proj) {
        OMGraphicList retList = null;

        if (omgraphic instanceof OMGraphicList) {

            OMGraphicList innerLoopList = new OMGraphicList();
            OMGraphic innerLoopTest = null;

            for (OMGraphic omg1 : (OMGraphicList) omgraphic) {
                // Doing this here might alleviate more checkRules calls and a
                // bunch of other instanceof checks.
                if (omg1 instanceof OMGraphicList) {
                    innerLoopTest = checkRules(omg1, labelList, proj);
                } else {
                    innerLoopTest = rules.evaluate(omg1, labelList, proj);

                    if (innerLoopTest != null) {
                        innerLoopTest.generate(proj);
                    }
                }

                if (innerLoopTest != null) {
                    innerLoopList.add(innerLoopTest);
                }
            }

            if (!innerLoopList.isEmpty()) {
                retList = innerLoopList;
            }
        } else {
            OMGraphic omg1 = rules.evaluate(omgraphic, labelList, proj);

            if (omg1 != null) {
                retList = new OMGraphicList();
                omg1.generate(proj);
                retList.add(omg1);
            }
        }

        return retList;
    }

    public OMGraphic loadJSON(String urlString) {
        if (urlString != null) {
            URL input;
            try {
                input = PropUtils.getResourceOrFileOrURL(urlString);

                InputStream inputStream = input.openStream();
                FeatureCollection featureCollection = new ObjectMapper().readValue(inputStream, FeatureCollection.class);

                OMGraphic omg = featureCollection.convert();
                defaultDrawingAttributes.setTo(omg);
                return omg;

            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        return null;
    }

    public OMGraphicList prepare() {
        OMGraphicList ret = new OMGraphicList();

        if (convertedJSON == null) {
            convertedJSON = loadJSON(jsonFile);
        }

        OMGraphicList labelList = new OMGraphicList();
        if (convertedJSON != null) {
            ret = checkRules(convertedJSON, labelList, getProjection());

            if (ret != null) {
                ret.add(labelList);
            }
        }

        return ret;
    }

    public String getToolTipTextFor(OMGraphic omg) {
        return (String) omg.getAttribute(OMGraphic.TOOLTIP);
    }

    public String getInfoText(OMGraphic omg) {
        return (String) omg.getAttribute(OMGraphic.INFOLINE);
    }

    /**
     * @return the jsonFile
     */
    public String getJsonFile() {
        return jsonFile;
    }

    /**
     * @param jsonFile the jsonFile to set
     */
    public void setJsonFile(String jsonFile) {
        this.jsonFile = jsonFile;
        convertedJSON = null;
    }

    /**
     * GeoJSONRuleHandler knows how to handle rules for the GeoJSON parameters.
     * The Attributes are stored directly into the OMGraphic attributes.
     *
     * @author dietrick
     */
    public class GeoJSONRuleHandler extends RuleHandler<Map> {

        /*
         * (non-Javadoc)
         * 
         * @see com.bbn.openmap.omGraphics.rule.RuleHandler#createRule()
         */
        @Override
        public Rule createRule() {
            return new MapRule();
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * com.bbn.openmap.omGraphics.rule.RuleHandler#getRecordDataForOMGraphic
         * (com.bbn.openmap.omGraphics.OMGraphic)
         */
        @Override
        public Map getRecordDataForOMGraphic(OMGraphic omg) {
            return omg.getAttributes();
        }

    }
}
