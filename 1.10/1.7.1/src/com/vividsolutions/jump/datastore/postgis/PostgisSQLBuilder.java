package com.vividsolutions.jump.datastore.postgis;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jump.datastore.FilterQuery;
import com.vividsolutions.jump.datastore.SpatialReferenceSystemID;

/**
 * Creates SQL query strings for a PostGIS database
 */
public class PostgisSQLBuilder {

  private SpatialReferenceSystemID defaultSRID = null;
  private String[] colNames = null;

  public PostgisSQLBuilder(SpatialReferenceSystemID defaultSRID, String[] colNames) {
    this.defaultSRID = defaultSRID;
    this.colNames = colNames;
  }

  public String getSQL(FilterQuery query) {
    return buildQueryString(query);
  }

  private String buildQueryString(FilterQuery query) {
    StringBuilder qs = new StringBuilder();
    //HACK
    qs.append("SELECT ");
    qs.append(getColumnListSpecifier(colNames, query.getGeometryAttributeName()));
    qs.append(" FROM ");
    // fixed by mmichaud on 2010-05-27 for mixed case dataset names
    qs.append("\"").append(query.getDatasetName().replaceAll("\\.","\".\"")).append("\"");
    qs.append(" t WHERE ");
    qs.append(buildBoxFilter(query.getGeometryAttributeName(), query.getSRSName(), query.getFilterGeometry()));

    String whereCond = query.getCondition();
    if (whereCond != null) {
      qs.append(" AND ");
      qs.append(whereCond);
    }
    int limit = query.getLimit();
    if (limit != 0 && limit != Integer.MAX_VALUE) {
      qs.append(" LIMIT ").append(limit);
    }
    //System.out.println(qs);
    return qs.toString();
  }

  private String buildBoxFilter(String geometryColName, SpatialReferenceSystemID SRID, Geometry geom) {
    Envelope env = geom.getEnvelopeInternal();

    // Example of Postgis SQL: GEOM && SetSRID('BOX3D(191232 243117,191232 243119)'::box3d,-1);
    StringBuilder buf = new StringBuilder();
    // fixed by mmichaud on 2010-05-27 for mixed case geometryColName names
    buf.append("\"").append(geometryColName).append("\" && ST_SetSRID('BOX3D(");
    buf.append(env.getMinX()
               + " " + env.getMinY()
               + "," + env.getMaxX()
               + " " + env.getMaxY()
               );
    buf.append(")'::box3d,");
    // [mmichaud 2012-03-14] make windows srid homogeneous with geometry srid
    // in case it is not defined
    String srid = getSRID(SRID);
    srid = srid==null? "ST_SRID(\"" + geometryColName + "\")" : srid;
    buf.append(srid).append(")");
    return buf.toString();
  }

  private String getSRID(SpatialReferenceSystemID querySRID) {
    SpatialReferenceSystemID srid = defaultSRID;
    if (! querySRID.isNull())
      srid = querySRID;

    if (srid.isNull() || srid.getString().trim().length()==0)
      return null;
    else
      return srid.getString();
  }

  private String getColumnListSpecifier(String[] colNames, String geomColName) {
    // Added double quotes around each column name in order to read mixed case table names
    // correctly [mmichaud 2007-05-13]
    StringBuilder buf = new StringBuilder();
    // fixed by mmichaud using a patch from jaakko [2008-05-21]
    // query geomColName as geomColName instead of geomColName as geomColName + "_wkb"
    buf.append("ST_AsEWKB(\"").append(geomColName).append("\") as ").append("\"").append(geomColName).append("\"");
    for (String colName : colNames) {
      if (! geomColName.equalsIgnoreCase(colName)) {
        buf.append(",\"").append(colName).append("\"");
      }
    }
    return buf.toString();
  }
}