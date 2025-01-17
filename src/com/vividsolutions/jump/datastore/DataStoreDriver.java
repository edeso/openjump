package com.vividsolutions.jump.datastore;

import java.sql.Driver;

import com.vividsolutions.jump.parameter.ParameterList;
import com.vividsolutions.jump.parameter.ParameterListSchema;

/**
 * A driver for a given type of datastore
 */
public interface DataStoreDriver {

    String REGISTRY_CLASSIFICATION = DataStoreDriver.class.getName();

    String getName();

    String getVersion();

    Driver getJdbcDriver();

    String getJdbcDriverVersion();

    ParameterListSchema getParameterListSchema();

    DataStoreConnection createConnection(ParameterList params) throws Exception;

    /**
     * @return a description of the driver
     */
    String toString();
    
    boolean isAdHocQuerySupported();    
}