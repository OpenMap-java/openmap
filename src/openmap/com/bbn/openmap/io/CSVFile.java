// **********************************************************************
// 
// <copyright>
// 
//  BBN Technologies, a Verizon Company
//  10 Moulton Street
//  Cambridge, MA 02138
//  (617) 873-8000
// 
//  Copyright (C) BBNT Solutions LLC. All rights reserved.
// 
// </copyright>
// **********************************************************************
// 
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/io/CSVFile.java,v $
// $RCSfile: CSVFile.java,v $
// $Revision: 1.4 $
// $Date: 2004/01/26 18:18:08 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.io;

import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.CSVTokenizer;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.layer.util.LayerUtils;

import java.io.*;
import java.util.*;
import java.net.URL;
import java.net.MalformedURLException;

/**
 * The CSVFile holds on to the contents of a CSV file.
 *<P>
 * NOTE: By default, the numbers that are found in the CSV file are
 * converted to Doubles.  Use the load(boolean) method to control
 * this, especially if you are using the fields later as the key in a
 * Hashtable.  
 */
public class CSVFile {

    /** The location of the CSV file */
    public URL infoUrl;
    
    /** The records of the CSV file */
    protected Vector infoRecords = null;
    
    /** The header record, if there is one */
    protected Vector headerRecord = null;

    /** Whether file has a line of column headers. */
    protected boolean headersExist = true;

    /** 
     * Don't do anything special, since all defaults are set already 
     */
    public CSVFile(String name) throws MalformedURLException {
        infoUrl = LayerUtils.getResourceOrFileOrURL(null, name); 
    }
    
    /** 
     * Don't do anything special, since all defaults are set already 
     */
    public CSVFile(URL url) throws MalformedURLException {
        infoUrl = url; 
    }

    /** 
     * Set whether the first line should be considered as headers to
     * each column. 
     */
    public void setHeadersExist(boolean set) {
        headersExist = set;
    }
    
    /** 
     * Get whether the first line should be considered as headers to
     * each column. 
     */
    public boolean isHeadersExist() {
        return headersExist;
    }

    /**
     * Reads the numbers and stores them as Doubles.
     */
    public void loadData() {
        loadData(false);
    }

    /** 
     * Read the data in from the file, with the option of reading the
     * numbers in the files as strings.
     */
    public void loadData(boolean readNumbersAsStrings) {
        BufferedReader streamReader = null;
        Vector records = new Vector();

        try {
            Object token = null;
            boolean header_read = false;

            if (!headersExist) {
                header_read = true;
                headerRecord = new Vector();
            }

            // This lets the property be specified as a file name
            // even if it's not specified as file:/<name> in
            // the properties file.
            URL csvURL = infoUrl;
            streamReader = new BufferedReader(new InputStreamReader(csvURL.openStream()));
            CSVTokenizer csvt = new CSVTokenizer(streamReader, readNumbersAsStrings);
            String name = null;
            String abbreviation = null;
            int count = 0;
            token = csvt.token();
            while (!csvt.isEOF(token)) {
                count++;

                Vector rec_line = new Vector();
                while (!csvt.isNewline(token)) {
                    rec_line.addElement(token);
                    token = csvt.token();
                    if (csvt.isEOF(token)) break;
                }

                //  Don't add the header record, because we don't care
                //  about it.
                if (header_read) {
                    records.addElement(rec_line);
                } else if (headersExist) {
                    headerRecord = rec_line;
                    header_read = true;
                }
                
                if (Debug.debugging("csv")) {
                    Debug.output("CSVFile.read: " + rec_line);
                }

                token = csvt.token();
            }
        } catch (java.io.IOException ioe) {
            throw new com.bbn.openmap.util.HandleError(ioe);
        } catch (ArrayIndexOutOfBoundsException aioobe) {
            throw new com.bbn.openmap.util.HandleError(aioobe);
        }  catch (ClassCastException cce) {
            throw new com.bbn.openmap.util.HandleError(cce);
        }  
        
        try {         
            if (streamReader != null) {
                streamReader.close();
            }
        } catch(java.io.IOException ioe) {
            throw new com.bbn.openmap.util.HandleError(ioe);
        }
        infoRecords = records;

        if (Debug.debugging("csv")) {
            Debug.output("CSVFile: read in " + infoRecords.size() + " records");
        }
    }
   
    /**
     * Return a particular Vector representing the contents of a
     * record line in the CSV file.  The record choice is picked via
     * the record number given -zero is the first one, and the header
     * record, if it exists, does not count.  
     *
     * @param recordnumber the number of the record in the csv file.
     * @return Vector Vector of contents of record line.
     */
    public Vector getRecord(int recordnumber) {
        Vector vector;
        try { 
            vector = (Vector)infoRecords.elementAt(recordnumber);
        } catch (ArrayIndexOutOfBoundsException e) {
            Debug.error(infoUrl.toString() + 
                        ": Don't have information for shape record " + recordnumber);
            return null;
        }
        return vector;
    }

    /**
     * Return an iterator, that can be used to traverse the records of the file.
     *
     * @return Iterator
     */
    public Iterator iterator() {
        return infoRecords.iterator();
    }
}
