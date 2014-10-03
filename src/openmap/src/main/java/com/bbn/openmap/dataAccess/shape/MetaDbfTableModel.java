// Bart 20060831 -> i18n

// **********************************************************************
// 
// <copyright>
// 
//  BBN Technologies
//  10 Moulton Street
//  Cambridge, MA 02138
//  (617) 873-8000
// 
//  Copyright (C) BBNT Solutions LLC. All rights reserved.
// 
// </copyright>
// **********************************************************************
// 
// $Source: /home/cvs/nodus/src/com/bbn/openmap/dataAccess/shape/MetaDbfTableModel.java,v $
// $RCSfile: MetaDbfTableModel.java,v $
// $Revision: 1.3 $
// $Date: 2007-06-27 16:29:06 $
// $Author: jourquin $
// 
// **********************************************************************

package com.bbn.openmap.dataAccess.shape;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.bbn.openmap.Environment;
import com.bbn.openmap.I18n;
import com.bbn.openmap.dataAccess.shape.input.DbfInputStream;
import com.bbn.openmap.util.Debug;

/**
 * An extension of the DbfTableModel that allows editing of the format of the
 * TbfTableModel, allowing addition and deletion of columns of the
 * DbfTableModel. The original DbfTableModel column headers are scanned and put
 * into records, and edited as rows. Be careful with this.
 */
public class MetaDbfTableModel extends DbfTableModel implements ShapeConstants {

    /**
     * 
     */
    private static final long serialVersionUID = -8408741750134029139L;
    public final static int META_RECORDNAME_COLUMN_NUMBER = 0;
    public final static int META_TYPE_COLUMN_NUMBER = 1;
    public final static int META_LENGTH_COLUMN_NUMBER = 2;
    public final static int META_PLACES_COLUMN_NUMBER = 3;

    protected DbfTableModel source = null;
    /**
     * Keeps track of the original columns. If a name is changed the row will be
     * deleted in all the records.
     */
    protected int originalColumnNumber = 0;
    
    //  I18N mechanism
    static I18n i18n = Environment.getI18n();

    /**
     * Creates a blank DbfTableModel from the source DbfTableModel.
     * 
     * @param source the DbfTableModel to be modified.
     */
    @SuppressWarnings("unchecked")
    public MetaDbfTableModel(DbfTableModel source) {
        super(4); // these are the number of columns for Metadata
        init();
        setWritable(true);
        this.source = source;

        int numColumnCount = source.getColumnCount();

        originalColumnNumber = numColumnCount;

        for (int i = 0; i < numColumnCount; i++) {
            List<Object> record = new ArrayList<Object>();
            record.add(source.getColumnName(i));
            record.add(new Byte(source.getType(i)));
            record.add(new Integer(source.getLength(i)));
            record.add(new Integer(source.getDecimalCount(i)));
            addRecord(record);
            if (DEBUG)
                Debug.output("Adding record: " + record);
        }
    }

    /**
     * Set up the columns of this DbfTableModel, so the parameters of the source
     * header rows are listed.
     */
    protected void init() {
        _names[META_RECORDNAME_COLUMN_NUMBER] = i18n.get(MetaDbfTableModel.class, "Column_Name", "Column Name");        
        _names[META_TYPE_COLUMN_NUMBER] = i18n.get(MetaDbfTableModel.class, "Type_of_Data", "Type of Data");        
        _names[META_LENGTH_COLUMN_NUMBER] = i18n.get(MetaDbfTableModel.class, "Length_of_Field", "Length of Field");        
        _names[META_PLACES_COLUMN_NUMBER] = i18n.get(MetaDbfTableModel.class, "Decimal_Places", "Decimal Places");
        

        for (int i = 0; i < 4; i++) {

            _lengths[i] = (byte) 12;
            _decimalCounts[i] = (byte) 0;

            byte type;
            if (i < 2) {
                type = DBF_TYPE_CHARACTER.byteValue();
            } else {
                type = DBF_TYPE_NUMERIC.byteValue();
            }

            _types[i] = type;
        }
    }

    /**
     * Remove the record at the index. This extension decreases the
     * originalColumnNumber which controls which rows[0] can be edited.
     */
    @Override
    public List<Object> remove(int columnIndex) {
        List<Object> ret = super.remove(columnIndex);
        if (columnIndex < originalColumnNumber) {
            originalColumnNumber--;
        }
        return ret;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        if (columnIndex == 0 && rowIndex < originalColumnNumber) {
            return false;
        } else {
            return writable;
        }
    }

    /**
     * Sets an object at a certain location. The type is translated from integer
     * values to names for easier use.
     */
    @Override
    public void setValueAt(Object object, int row, int column) {

        if (column == META_TYPE_COLUMN_NUMBER) {
            if (DBF_BINARY.equals(object) || DBF_TYPE_BINARY.equals(object)) {
                object = DBF_TYPE_BINARY;
            } else if (DBF_CHARACTER.equals(object)
                    || DBF_TYPE_CHARACTER.equals(object)) {
                object = DBF_TYPE_CHARACTER;
            } else if (DBF_DATE.equals(object) || DBF_TYPE_DATE.equals(object)) {
                object = DBF_TYPE_DATE;
            } else if (DBF_NUMERIC.equals(object)
                    || DBF_TYPE_NUMERIC.equals(object)) {
                object = DBF_TYPE_NUMERIC;
            } else if (DBF_LOGICAL.equals(object)
                    || DBF_TYPE_LOGICAL.equals(object)) {
                object = DBF_TYPE_LOGICAL;
            } else if (DBF_MEMO.equals(object) || DBF_TYPE_MEMO.equals(object)) {
                object = DBF_TYPE_MEMO;
            } else if (DBF_TIMESTAMP.equals(object)
                    || DBF_TYPE_TIMESTAMP.equals(object)) {
                object = DBF_TYPE_TIMESTAMP;
            } else if (DBF_LONG.equals(object) || DBF_TYPE_LONG.equals(object)) {
                object = DBF_TYPE_LONG;
            } else if (DBF_AUTOINCREMENT.equals(object)
                    || DBF_TYPE_AUTOINCREMENT.equals(object)) {
                object = DBF_TYPE_AUTOINCREMENT;
            } else if (DBF_FLOAT.equals(object)
                    || DBF_TYPE_FLOAT.equals(object)) {
                object = DBF_TYPE_FLOAT;
            } else if (DBF_DOUBLE.equals(object)
                    || DBF_TYPE_DOUBLE.equals(object)) {
                object = DBF_TYPE_DOUBLE;
            } else if (DBF_OLE.equals(object) || DBF_TYPE_OLE.equals(object)) {
                object = DBF_TYPE_OLE;
            } else {
                Debug.error("Rejected "
                        + object
                        + " as input. Use: \n    binary, character, date, boolean, memo, timestamp, long, autoincrement, float, double or OLE");
                return;
            }

            if (DEBUG) {
                Debug.output("New value set to " + object);
            }
        }

        super.setValueAt(object, row, column);
    }

    /**
     * Retrieves a value for a specific column and row index
     * 
     * @return Object A value for a specific column and row index
     */
    @Override
    public Object getValueAt(int row, int column) {
        Object cell = super.getValueAt(row, column);

        if (column == META_TYPE_COLUMN_NUMBER) {
            if (DBF_TYPE_CHARACTER.equals(cell)) {
                cell = DBF_CHARACTER;
            } else if (DBF_TYPE_DATE.equals(cell)) {
                cell = DBF_DATE;
            } else if (DBF_TYPE_NUMERIC.equals(cell)) {
                return DBF_NUMERIC;
            } else if (DBF_TYPE_LOGICAL.equals(cell)) {
                cell = DBF_LOGICAL;
            } else if (DBF_TYPE_MEMO.equals(cell)) {
                cell = DBF_MEMO;
            } else if (DBF_TYPE_BINARY.equals(cell)) {
                cell = DBF_BINARY;
            } else if (DBF_TYPE_TIMESTAMP.equals(cell)) {
                cell = DBF_TIMESTAMP;
            } else if (DBF_TYPE_FLOAT.equals(cell)) {
                cell = DBF_FLOAT;
            } else if (DBF_TYPE_DOUBLE.equals(cell)) {
                cell = DBF_DOUBLE;
            } else if (DBF_TYPE_LONG.equals(cell)) {
                cell = DBF_LONG;
            } else if (DBF_TYPE_AUTOINCREMENT.equals(cell)) {
                cell = DBF_AUTOINCREMENT;
            } else if (DBF_TYPE_OLE.equals(cell)) {
                cell = DBF_OLE;
            }
            // Else just keep it what it is.
        }
        return cell;
    }

    /**
     * Create a new record, corresponding to a new column in the source
     * DbfTableModel. Filled in with standard things that can be edited.
     */
    @SuppressWarnings("unchecked")
    @Override
    public void addBlankRecord() {
        List<Object> record = new ArrayList<Object>();
        record.add(i18n.get(MetaDbfTableModel.class, "New_Column", "New Column"));        
        record.add(DBF_TYPE_CHARACTER);
        record.add(new Integer(12));
        record.add(new Integer(0));
        addRecord(record);
        if (DEBUG) {
            Debug.output("Adding record: " + record);
        }
    }

    /**
     * Decide what to do when the window closes.
     */
    @Override
    public void exitWindowClosed() {
        if (source != null && source.dirty) {
            int check = JOptionPane.showConfirmDialog(null,
                    i18n.get(MetaDbfTableModel.class, "Do_you_want_to_save_your_changes", "Do you want to save your changes?"),
                    i18n.get(MetaDbfTableModel.class, "Confirm_Close", "Confirm Close"),
                    JOptionPane.YES_NO_OPTION);
           
            
            if (check == JOptionPane.YES_OPTION) {
                fireTableStructureChanged();
            } else {
                source.cleanupChanges();
            }
        }

        super.exitWindowClosed();
    }

    public void showGUI(String filename) {
        if (frame == null) {
            frame = new JFrame(i18n.get(MetaDbfTableModel.class, "Editing_Attribute_File_Structure", "Editing Attribute File Structure"));            

            frame.getContentPane().add(getGUI(filename, MODIFY_ROW_MASK
                    | DONE_MASK),
                    BorderLayout.CENTER);

            JButton saveButton = new JButton(i18n.get(MetaDbfTableModel.class, "Save Changes", "Save Changes"));            
            saveButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    int check = JOptionPane.showConfirmDialog(null,
                            i18n.get(MetaDbfTableModel.class, "Are_you_sure_you_want_to_modify_the_table_format", 
                                     "Are you sure you want to modify the table format?"),
                            i18n.get(MetaDbfTableModel.class, "Confirm_Save", "Confirm Save"),
                            JOptionPane.OK_CANCEL_OPTION);
                                      
                    if (check == JOptionPane.YES_OPTION) {
                        fireTableStructureChanged();
                    }
                }
            });

            controlPanel.add(saveButton);
            frame.validate();

            frame.setSize(500, 300);
            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    // need a shutdown event to notify other gui beans
                    // and
                    // then exit.
                    exitWindowClosed();
                }
            });
        }
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        Debug.init();
        if (args.length < 1) {
            System.exit(0);
        }

        try {

            URL dbf = new URL(args[0]);
            DbfInputStream dis = new DbfInputStream(dbf.openStream());
            DbfTableModel dtm = new DbfTableModel(dis);

            MetaDbfTableModel mdtm = new MetaDbfTableModel(dtm);
            mdtm.showGUI(args[0]);
            mdtm.exitOnClose = true;

        } catch (Exception e) {
            Debug.error(e.getMessage());
            e.printStackTrace();
        }
    }
}