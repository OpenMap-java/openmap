package com.bbn.openmap.dataAccess.shape;

import java.util.ArrayList;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.logging.Logger;

import com.bbn.openmap.OMComponent;
import com.bbn.openmap.io.FormatException;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.util.PropUtils;

public class DbfTableModelFactory extends OMComponent implements ShapeConstants {

    public static Logger logger = Logger.getLogger("com.bbn.openmap.dataAccess.shape.DbfTableModelFactory");

    public final static String ColumnsProperty = "columns";
    /**
     * Spreadsheet format for column, as
     * name,type,length,decimal_places(optional)
     */
    public final static String DefinitionProperty = "definition";
    public final static String DefaultValueProperty = "default";

    protected ArrayList<Column> columns;

    public class Column {
        protected String name;
        protected byte type;
        protected byte decimalCount;
        protected int length;
        protected Object defaultValue;

        public Column(String definition) throws FormatException {
            StringTokenizer tok = new StringTokenizer(definition, ",");
            try {
                // Name
                name = tok.nextToken().trim();
                // Type
                type = (byte) tok.nextToken().trim().toUpperCase().charAt(0);
                if (!DbfTableModel.isValidType(type)) {
                    throw new FormatException("Type is not valid: " + type);
                }
                // Length
                length = Integer.parseInt(tok.nextToken().trim());

                if (DbfTableModel.isNumericalType(type)) {
                    decimalCount = Byte.parseByte(tok.nextToken().trim());
                }
            } catch (Exception e) {
                throw new FormatException(e.getMessage());
            }
        }

        public Column(String n, byte t, byte dc, int l, Object dv) {
            name = n;
            type = t;
            decimalCount = dc;
            length = l;
            defaultValue = dv;
        }

        public String getName() {
            return name;
        }

        public byte getType() {
            return type;
        }

        public byte getDecimalCount() {
            return decimalCount;
        }

        public int getLength() {
            return length;
        }

        public void setDefaultValue(String defaultVal) {
            if (DbfTableModel.isNumericalType(type)) {
                try {
                    this.defaultValue = new Double(defaultVal);
                } catch (NumberFormatException nfe) {
                    logger.warning("can't parse default value for " + name
                            + ", setting to 0");
                    this.defaultValue = new Double(0);
                }
            } else {
                this.defaultValue = defaultVal;
            }
        }

        public Object getDefaultValue() {
            return defaultValue;
        }

        public String toString() {
            return name + "," + ((char) type) + "," + length
                    + (DbfTableModel.isNumericalType(type) ? "," + decimalCount : "")
                    + ", dv: " + defaultValue;
        }
    }

    public DbfTableModelFactory() {

    }

    public void setColumns(ArrayList<Column> cols) {
        columns = cols;
    }

    public ArrayList<Column> getColumns() {
        return columns;
    }

    public void setProperties(String prefix, Properties props) {
        super.setProperties(prefix, props);
        logger.fine("parsing properties");
        prefix = PropUtils.getScopedPropertyPrefix(prefix);
        String markerList = props.getProperty(prefix + ColumnsProperty);
        Vector<String> colStrings = PropUtils.parseSpacedMarkers(markerList);
        columns = new ArrayList<Column>(colStrings.size());
        for (String colString : colStrings) {
            String colDef = props.getProperty(prefix + colString + "."
                    + DefinitionProperty);

            if (colDef != null) {
                try {
                    Column col = new Column(colDef);
                    col.setDefaultValue(props.getProperty(prefix + colString + "."
                            + DefaultValueProperty, ""));

                    columns.add(col);

                    logger.fine("parsed: " + col);
                } catch (FormatException fe) {
                    logger.warning("For column: " + colString + ", def: " + colDef + ": "
                            + fe.getMessage());
                }
            }
        }
    }

    public Properties getProperties(Properties props) {
        props = super.getProperties(props);

        return props;
    }

    public Properties getPropertyInfo(Properties props) {
        props = super.getPropertyInfo(props);
        return props;
    }

    public DbfTableModel createDbfTableModel() {
        DbfTableModel dbf = null;

        if (columns != null) {
            dbf = new DbfTableModel(getNumColumns());
            int i = 0;
            for (Column col : columns) {
                dbf.setColumnName(i, col.name);
                dbf.setDecimalCount(i, col.decimalCount);
                dbf.setLength(i, col.length);
                dbf.setType(i, col.type);
                i++;
            }
        }
        return dbf;
    }

    public ArrayList<Object> getNewDefaultRow() {
        ArrayList<Object> row = new ArrayList<Object>(getNumColumns());
        if (columns != null) {
            for (Column col : columns) {
                row.add(col.defaultValue);
            }
        }
        return row;
    }

    public int getNumColumns() {
        return columns != null ? columns.size() : 0;
    }

    public DbfTableModel createDbf(OMGraphicList omgl) {
        DbfTableModel dbf = createDbfTableModel();
        if (dbf != null) {
            omgl.putAttribute(DBF_ATTRIBUTE, dbf);
            for (int i = 0; i < omgl.size(); i++) {
                dbf.addRecord(getNewDefaultRow());
            }
        }
        return dbf;
    }
}
