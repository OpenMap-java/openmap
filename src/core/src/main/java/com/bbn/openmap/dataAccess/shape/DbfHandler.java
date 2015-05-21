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
import java.util.List;
import java.util.Properties;

import com.bbn.openmap.io.BinaryFile;
import com.bbn.openmap.io.FormatException;
import com.bbn.openmap.omGraphics.DrawingAttributes;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.rule.IndexRule;
import com.bbn.openmap.omGraphics.rule.Rule;
import com.bbn.openmap.omGraphics.rule.RuleHandler;
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
public class DbfHandler extends RuleHandler<List> {

    protected DbfFile dbf;
    protected List<Rule> rules;
    protected DrawingAttributes defaultDA;

    protected DbfHandler() {
        defaultDA = new DrawingAttributes();
    }

    public DbfHandler(String dbfFilePath) throws IOException, FormatException {
        this(new BinaryFile(dbfFilePath));
    }

    public DbfHandler(BinaryFile bf) throws IOException, FormatException {
        this();
        dbf = new DbfFile(bf);
        dbf.close();
    }

    public void setProperties(String prefix, Properties props) {
        super.setProperties(prefix, props);
        prefix = PropUtils.getScopedPropertyPrefix(prefix);

        defaultDA.setProperties(prefix, props);
    }

    public Properties getProperties(Properties props) {
        props = super.getProperties(props);

        String prefix = PropUtils.getScopedPropertyPrefix(this);
        defaultDA.getProperties(props);

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

    /**
     * Tells the DbfFile to close the file pointer to the data. Will reopen if
     * needed.
     */
    public void close() {
        if (dbf != null) {
            dbf.close();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.bbn.openmap.omGraphics.RuleHandler#createRule()
     */
    @Override
    public Rule createRule() {
        return new DbfRule(dbf);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.bbn.openmap.omGraphics.RuleHandler#getRecordDataForOMGraphic(com.
     * bbn.openmap.omGraphics.OMGraphic)
     */
    @Override
    public List getRecordDataForOMGraphic(OMGraphic omg) {
        try {
            return (List) dbf.getRecordData((Integer) omg.getAttribute(ShapeConstants.SHAPE_INDEX_ATTRIBUTE));
        } catch (IOException ioe) {

        } catch (FormatException fe) {

        }
        return null;
    }

    public class DbfRule extends IndexRule {
        DbfFile dbf;

        public DbfRule(DbfFile dbf) {
            this.dbf = dbf;
        }

        /**
         * A record List for the attributes of an OMGraphic might have a name
         * for each entry. Given a name, provide the index into the List to get
         * that attribute value.
         * 
         * @param columnName name of a attribute in a List, like a column name
         *        of a list of lists.
         * @return the int index of the entry in the record List.
         */
        public int getRecordColumnIndexForName(String columnName) {
            return dbf.getColumnIndexForName(columnName);
        }

        /**
         * Provide the title of the attribute at a specific entry.
         * 
         * @param index into the record List.
         * @return the record List name for that index.
         */
        public String getRecordColumnName(int index) {
            return dbf.getColumnName(index);
        }
    }

}
