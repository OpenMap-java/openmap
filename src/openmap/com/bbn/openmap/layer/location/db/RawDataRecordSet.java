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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/location/db/RawDataRecordSet.java,v $
// $RCSfile: RawDataRecordSet.java,v $
// $Revision: 1.1.1.1 $
// $Date: 2003/02/14 21:35:48 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.layer.location.db;

import java.sql.*;
import java.io.*;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;
import com.bbn.openmap.util.Debug;

/**
 * This class is responsible for retrieving Raw Data from a table in
 * a Database given a key.  Users of this class should provide
 * Database connection, Table name, Column name that has the key and
 * column name that has the actual data.  
 * <P>
 * If you are going to use this, there are a couple of properties to set:<BR>
 * prefix.rawDataTableName=table name<BR>
 * prefix.rawDataColumnName=data column name<BR>
 * prefix.rawDataKeyColumnName=data key name<BR>
 *
 */
public class RawDataRecordSet {
    /** Connection object that will be used to retrieve data*/
    protected Connection connection;
    /** Table name from which data would be retrieved*/
    protected String tableName;
    /** Column name in the above table that has Raw Data*/
    protected String rawDataColumnName;
    /** Column name which has the key to lookup above data */
    protected String rawDataKeyColumnName;

    /** A hashtable to keep track of the byte arrays, using the key.
     *  This will reduce the calls to the database. */
    protected Hashtable byteCache = new Hashtable();

    public static final String tableNameProperty = "rawDataTableName";
    public static final String rawDataColumnNameProperty = "rawDataColumnName";
    public static final String rawDataKeyColumnNameProperty = "rawDataKeyColumnName";
    
    public RawDataRecordSet() {}

    public RawDataRecordSet(Connection inConnection){
	setConnection(inConnection);
    }
    
    public RawDataRecordSet(Connection inconnection, String prefix,
			    Properties properties){

	if (prefix != null) {
	    prefix = prefix + ".";
	} else {
	    prefix = "";
	}
	
	setConnection(inconnection);
	setTableName(properties.getProperty(prefix+tableNameProperty));
	setRawDataColumnName(properties.getProperty(prefix+rawDataColumnNameProperty));
	setRawDataKeyColumnName(properties.getProperty(prefix+rawDataKeyColumnNameProperty));
    }	

    /** Returns a byte[] array if successfull, null otherwise */
    public byte[] getRawData(String lookUpKey) throws SQLException {

	byte[] foundit = (byte[]) byteCache.get(lookUpKey.toLowerCase().intern());

	if (foundit != null){
	    return foundit;
	}

	String query = "Select " + rawDataColumnName + 
	    " from " + tableName+ 
	    " where " + rawDataKeyColumnName + 
	    " = '"+ lookUpKey.toLowerCase() +"' ";

	try {
	    Statement stmt = connection.createStatement ();
	    ResultSet rset = stmt.executeQuery(query);
	    rset.next();

	    // This is the only(first hence 1)
	    InputStream dbis  = rset.getBinaryStream(1); 
	    int chunksize = 4096;
	    byte barr[] = new byte[chunksize];
	    int imagelength = 0;;

	    try {
		imagelength = dbis.read(barr);
	    } catch(IOException ioE) {				
		System.err.println("ERROR - while reading raw data\n" + ioE.getMessage());
	    }
// 	    System.out.println("image length = " + imagelength);
	    byte image[] = new byte[imagelength];
	    System.arraycopy(barr,0,image,0,imagelength);

	    // close the resultSet
	    rset.close();		
	    // Close the statement
	    stmt.close();
	    byteCache.put(lookUpKey.toLowerCase().intern(), image);
	    return image;

	} catch(SQLException sqlE){			
// 	    throw new SQLException(sqlE.getMessage() + "\n"+ 
// 				   "SQL String " + query);
	    System.err.println("ERROR - " + sqlE.getMessage() + "\n" +  
			       "SQL String: " + query);
	    return null;
	}
    }

    public Connection getConnection(){
	return connection;
    }

    public void setConnection(Connection inConnection){
	connection = inConnection;
    }
    
    public String getTableName(){
	return tableName;
    }

    public void setTableName(String inTableName){
	tableName = inTableName;
    }
	
    public String getRawDataColumnName(){
	return rawDataColumnName;
    }
    
    public void setRawDataColumnName(String inrawDataColumnName){
	rawDataColumnName = inrawDataColumnName;
    }
    
    public String getRawDataKeyColumnName(){
	return rawDataKeyColumnName;
    }
	
    public void setRawDataKeyColumnName(String inrawDataKeyColumnName){
	rawDataKeyColumnName = inrawDataKeyColumnName;
    }
}
