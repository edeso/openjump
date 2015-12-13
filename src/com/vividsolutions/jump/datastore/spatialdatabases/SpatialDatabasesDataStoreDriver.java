package com.vividsolutions.jump.datastore.spatialdatabases;

import com.vividsolutions.jump.datastore.DataStoreConnection;
import com.vividsolutions.jump.datastore.DataStoreDriver;
import com.vividsolutions.jump.datastore.mariadb.MariadbDSConnection;
import com.vividsolutions.jump.datastore.oracle.OracleDSConnection;
import com.vividsolutions.jump.datastore.postgis.PostgisDSConnection;
import com.vividsolutions.jump.datastore.spatialite.SpatialiteDSConnection;
import com.vividsolutions.jump.parameter.ParameterList;
import com.vividsolutions.jump.parameter.ParameterListSchema;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;

/**
 * A driver for supplying {@link SpatialDatabaseDSConnection}s
 */
public class SpatialDatabasesDataStoreDriver
    implements DataStoreDriver {

  /** TODO: I18N */
  public static final String PARAM_Server = "Server";
  public static final String PARAM_Port = "Port";
  public static final String PARAM_Instance = "Database";
  public static final String PARAM_User = "User";
  public static final String PARAM_Password = "Password";
  // For Spatialite
  public static final String PARAM_DB_File = "DB file";

  protected String driverName = null;
  protected String jdbcClass = null;
  protected String urlPrefix = null;

  protected String[] paramNames = null;
  protected Class[] paramClasses = null;
  protected ParameterListSchema schema = null;

  public SpatialDatabasesDataStoreDriver() {
    // Nicolas Ribot:
    //paramNames are no more static now they can be overloaded by child classes @link SpatialiteDataStoreDriver for instance
    paramNames = new String[]{
        PARAM_Server,
        PARAM_Port,
        PARAM_Instance,
        PARAM_User,
        PARAM_Password
      };
  // Nicolas Ribot: passed protected and not final to allow spatialiteDataStoreDriver to overload it
    paramClasses = new Class[]{
        String.class,
        Integer.class,
        String.class,
        String.class,
        String.class
      };
    // Nicolas Ribot: passed protected and not final to allow spatialiteDataStoreDriver to overload it
    schema = new ParameterListSchema(paramNames, paramClasses);
  }

  public String getDriverName() {
    return driverName;
  }

  public String getJdbcClass() {
    return jdbcClass;
  }

  public String getUrlPrefix() {
    return urlPrefix;
  }

  public String[] getParamNames() {
    return paramNames;
  }

  public Class[] getParamClasses() {
    return paramClasses;
  }

  public ParameterListSchema getSchema() {
    return schema;
  }

  @Override
  public String getName() {
    return driverName;
  }

  @Override
  public ParameterListSchema getParameterListSchema() {
    return schema;
  }

  @Override
  public DataStoreConnection createConnection(ParameterList params)
      throws Exception {
    String host = params.getParameterString(PARAM_Server);
    int port = params.getParameterInt(PARAM_Port);
    String database = params.getParameterString(PARAM_Instance);
    String user = params.getParameterString(PARAM_User);
    String password = params.getParameterString(PARAM_Password);

    String url
        = String.valueOf(new StringBuffer(urlPrefix).append(host).append(":").append(port).append("/").append(database));

    Driver driver = (Driver) Class.forName(this.getJdbcClass()).newInstance();
    DriverManager.registerDriver(driver);

    // mmichaud 2013-08-27 workaround for ticket #330
    String savePreferIPv4Stack = System.getProperty("java.net.preferIPv4Stack");
    String savePreferIPv6Addresses = System.getProperty("java.net.preferIPv6Addresses");
    System.setProperty("java.net.preferIPv4Stack", "true");
    System.setProperty("java.net.preferIPv6Addresses", "false");

    Connection conn = DriverManager.getConnection(url, user, password);

    if (savePreferIPv4Stack == null) {
      System.getProperties().remove("java.net.preferIPv4Stack");
    } else {
      System.setProperty("java.net.preferIPv4Stack", savePreferIPv4Stack);
    }
    if (savePreferIPv6Addresses == null) {
      System.getProperties().remove("java.net.preferIPv6Addresses");
    } else {
      System.setProperty("java.net.preferIPv6Addresses", savePreferIPv6Addresses);
    }
    //return new SpatialDatabasesDSConnection(conn);
    // TODO: clean inheritance...
    if (url.startsWith("jdbc:postgresql")) {
      return new PostgisDSConnection(conn);
    } else if (url.startsWith("jdbc:oracle")) {
      return new OracleDSConnection(conn);
    } else if (url.startsWith("jdbc:mysql")) {
      return new MariadbDSConnection(conn);
    } else if (url.startsWith("jdbc:sqlite")) {
      return new SpatialiteDSConnection(conn);
    } else {
      // TODO: should not pass here
      System.err.println("ERROR: Returning a SpatialDatabasesDSConnection for url: " + url + ". Should not happen...");
      return new SpatialDatabasesDSConnection(conn);
    }
  }

  @Override
  public boolean isAdHocQuerySupported() {
    return true;
  }

}