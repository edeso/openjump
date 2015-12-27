package com.vividsolutions.jump.datastore.postgis;

import java.sql.Connection;

import com.vividsolutions.jump.datastore.DataStoreConnection;
import com.vividsolutions.jump.datastore.spatialdatabases.AbstractSpatialDatabasesDSDriver;
import com.vividsolutions.jump.parameter.ParameterList;

/**
 * A driver for supplying {@link SpatialDatabaseDSConnection}s
 */
public class PostgisDSDriver
    extends AbstractSpatialDatabasesDSDriver {

    public final static String JDBC_CLASS = "org.postgresql.Driver";

    public PostgisDSDriver() {
        this.driverName = "PostGIS";
        this.jdbcClass = JDBC_CLASS;
        this.urlPrefix = "jdbc:postgresql://";
    }
    
    /**
     * returns the right type of DataStoreConnection
     * @param params
     * @return
     * @throws Exception 
     */
    @Override
    public DataStoreConnection createConnection(ParameterList params)
        throws Exception {
        Connection conn = super.createJdbcConnection(params);
        return new PostgisDSConnection(conn);
    }
}