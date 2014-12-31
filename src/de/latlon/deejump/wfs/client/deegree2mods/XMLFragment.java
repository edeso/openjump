package de.latlon.deejump.wfs.client.deegree2mods;

import java.io.*;
import java.net.*;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.*;
import org.deegree.enterprise.*;
import org.xml.sax.*;

import de.latlon.deejump.wfs.client.*;

public class XMLFragment extends org.deegree.framework.xml.XMLFragment {

  /**
   * this override is necessary to have this method use the WFSHttpClient
   * which holds some general settings like the socket timeout
   */
  @Override
  public void load( URL url )
      throws IOException, SAXException {
    if (url == null) {
      throw new IllegalArgumentException("The given url may not be null");
    }

    String uri = url.toExternalForm();
    if (!uri.matches("^https?://.*")) {
      load(url.openStream(), uri);
      return;
    }
    // else try to use a proxy
    HttpClient client = new WFSHttpClient();
    WebUtils.enableProxyUsage(client, url);
    GetMethod get = new GetMethod(url.toExternalForm());
    client.executeMethod(get);
    load(get.getResponseBodyAsStream(), uri);
  }

}