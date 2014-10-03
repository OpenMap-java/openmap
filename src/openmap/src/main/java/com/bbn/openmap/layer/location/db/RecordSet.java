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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/location/db/RecordSet.java,v $
// $RCSfile: RecordSet.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:06:00 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.location.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The RecordSet object handles all the generic database retrieval for a SQL
 * query. The idea is that you set it up with a connection, and set the query
 * string that it will use to give to the database. Then, you iterate through
 * the result set to create the objects you want to:
 * <UL>
 * <LI>Set the connection and query.
 * <LI>Call next() repeatedly to see if there is data for another object.
 * <LI>Use getResultSet(), which provides the result set containing the current
 * record data. This can be done within the constructor of the data object you
 * are trying to create.
 * </UL>
 */
public class RecordSet {

    protected static Logger logger = Logger.getLogger("com.bbn.openmap.layer.location.db.RecordSet");

    /** Connection object that will be used to retrieve data. */
    protected Connection connection = null;
    /** The query string that will be executed on the database. */
    protected String queryString = null;

    private Statement stmt;
    private ResultSet rset;

    /**
     * Use this constructor if you want a little more control of the process.
     * You have to remember to set the query string and call getAllQuery()
     * before trying to iterate through the results.
     */
    public RecordSet(Connection inConnection) throws SQLException {
        this(inConnection, null);
    }

    /**
     * Does everything. If the connection and query are not null, then the
     * result set is ready for iteration after this object is created.
     */
    public RecordSet(Connection inConnection, String query) throws SQLException {
        connection = inConnection;
        queryString = query;
        getAllQuery();
    }

    /**
     * Executes "select * from 'tableName'", or whatever the queryString is set
     * to. If the connection is not null, and the queryString is not null, the
     * database is fed the query statement. The result is, hopefully, that the
     * rset (ResultSet) will be full of responses. The caller should then call
     * next() to iterate through the results and fetch the data.
     * 
     * @exception throws SQLException if something goes wrong with the query.
     */
    public void getAllQuery() throws SQLException {

        if (queryString != null && connection != null) {
            try {

                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("RecordSet calling database with query => " + queryString);
                }

                stmt = connection.createStatement();
                rset = stmt.executeQuery(queryString);
            } catch (SQLException sqlE) {
                throw new SQLException(queryString + " | " + sqlE.getMessage());
            }
        } else {
            logger.warning("Database parameters faulty!\n  query => " + queryString
                    + "\n  connection => " + connection);
        }
    }

    /**
     * This function should be called to prepare the result set with the next
     * record set of data. Then you feed this RecordSet object to the
     * constructor to a new data object.
     */
    public boolean next() throws SQLException {
        if (rset != null) {
            return rset.next();
        } else {
            return false;
        }
    }

    /**
     * Get the result set, after calling next. It should contain the current
     * record's data. You have to know how to call the items, and what the type
     * of each index is - since you set the query, you should know. You have to
     * pay attention to the return from the next() function, though. If next()
     * returns false, the result set won't contain valid data, or may be null.
     */
    public ResultSet getResultSet() {
        return rset;
    }

    public void close() throws SQLException {
        if (rset != null) {
            rset.close();
        }
        if (stmt != null) {
            stmt.close();
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection inConnection) {
        connection = inConnection;
    }

    public String getQueryString() {
        return queryString;
    }

    public void setQueryString(String inQueryString) {
        queryString = inQueryString;
    }
}