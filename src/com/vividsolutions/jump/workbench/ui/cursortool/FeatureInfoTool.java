
/*
 * The Unified Mapping Platform (JUMP) is an extensible, interactive GUI 
 * for visualizing and manipulating spatial features with geometry and attributes.
 *
 * Copyright (C) 2003 Vivid Solutions
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 * 
 * For more information, contact:
 *
 * Vivid Solutions
 * Suite #1A
 * 2328 Government Street
 * Victoria BC  V8T 5G5
 * Canada
 *
 * (250)385-6040
 * www.vividsolutions.com
 */

package com.vividsolutions.jump.workbench.ui.cursortool;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Image;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.openjump.core.CheckOS;

import com.vividsolutions.jump.workbench.model.FenceLayerFinder;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.Layerable;
import com.vividsolutions.jump.workbench.model.WMSLayer;
import com.vividsolutions.jump.workbench.ui.InfoFrame;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import com.vividsolutions.wms.WMService;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.apache.commons.io.IOUtils;
import org.openjump.core.rasterimage.RasterImageLayer;
import org.openjump.core.rasterimage.RasterImageLayer.RasterDataNotFoundException;

public class FeatureInfoTool extends SpecifyFeaturesTool {

    public static final ImageIcon ICON = IconLoader.icon("information_20x20.png");
    public FeatureInfoTool() {
        setColor(Color.magenta);
    }

    public Icon getIcon() {
        return ICON;
    }

    public Cursor getCursor() {
      // [ede 03.2103] linux currently support only 2 color cursors
      Image i = !CheckOS.isLinux() ? IconLoader.image("information_cursor.png")
          : IconLoader.image("information_cursor_2color.gif");
      return createCursor(i);
    }
    @Override
    protected void gestureFinished() throws Exception {
        reportNothingToUndoYet();
        InfoFrame infoFrame = getTaskFrame().getInfoFrame();
        if (!wasShiftPressed()) {
            infoFrame.getModel().clear();
        }
        Map map = layerToSpecifiedFeaturesMap();
        Iterator i = map.keySet().iterator();
        while(i.hasNext()){
            Layer layer = (Layer) i.next();
            if (layer.getName().equals(FenceLayerFinder.LAYER_NAME)) {
                continue;
            }
            Collection features = (Collection) map.get(layer);
            infoFrame.getModel().add(layer, features);
        }
        
        Coordinate coord = getPanel().getViewport().toModelCoordinate(getViewSource());
        
        // WMS
        List<Layerable> wmsLay_l = getWorkbench().getContext().getLayerManager().getLayerables(WMSLayer.class);
        String response = "";
        for(Layerable lay : wmsLay_l) {
            WMSLayer wmsLayer = (WMSLayer) lay;
            
            String featInfoUrl = wmsLayer.getService().getCapabilities().getFeatureInfoURL();     
            String names = getWmsLayeNames(wmsLayer);
            
            Point2D point = getPanel().getViewport().toViewPoint(coord);
            Envelope bbox = getPanel().getViewport().getEnvelopeInModelCoordinates();
            
            if (featInfoUrl.contains("?")) {
                featInfoUrl += "&";
            } else {
                featInfoUrl += "?";
            }
            String version = wmsLayer.getWmsVersion();
            if (WMService.WMS_1_0_0.equals(version)) {
                featInfoUrl += "REQUEST=feature_info&WMTVER=1.0.0";
            } else if (WMService.WMS_1_1_0.equals(version) ||
                    WMService.WMS_1_1_1.equals(version) ||
                    WMService.WMS_1_3_0.equals(version)) {
                featInfoUrl += "REQUEST=GetFeatureInfo&SERVICE=WMS&VERSION=" + version;
            }            
            
            featInfoUrl += "&QUERY_LAYERS=" + names + "&LAYERS=" + names;
            if (WMService.WMS_1_3_0.equals(version)) {
                featInfoUrl += "&CRS=" + wmsLayer.getSRS() +
                        "&I=" + (int) point.getX() +
                        "&J=" + (int) point.getY();
            } else {
                featInfoUrl += "&SRS=" + wmsLayer.getSRS() +
                        "&X=" + (int) point.getX() +
                        "&Y=" + (int) point.getY();
            }
            
            featInfoUrl += "&WIDTH=" + getPanel().getWidth() +
                    "&HEIGHT=" + getPanel().getHeight() +
                    "&BBOX=" + bbox.getMinX() + "," + bbox.getMinY() + "," + bbox.getMaxX() + "," + bbox.getMaxY() +
                    "&STYLES=" +
                    "&FORMAT=" + wmsLayer.getFormat();
            
            if (!WMService.WMS_1_0_0.equals(version)) {
                try {
                    featInfoUrl += "&INFO_FORMAT=" + wmsLayer.getService().getCapabilities().getInfoFormat();
                } catch (IOException e) {
                    featInfoUrl += "&INFO_FORMAT=text/plain";
                }
            }
            
            URL url = stripXhtmlTags(featInfoUrl);
            
            String newLine = System.getProperty("line.separator");
            response = response.concat("+ ").concat(wmsLayer.getName()).concat(newLine);
            
            String wmsResponse;
            try {
                wmsResponse = IOUtils.toString(url.openStream());
                wmsResponse = cleanWmsResponse(wmsResponse);
            } catch(Exception ex) {
                wmsResponse = ex.toString();
                wmsResponse = wmsResponse.concat(newLine);
            }
            response = response.concat(wmsResponse);
            response = response.concat(newLine);
            
        };
        infoFrame.setWmsInfo(response);
        
        // Raster data
        List<Layerable> layerables_l = getWorkbench().getContext().getLayerManager().getLayerables(RasterImageLayer.class);
        
        String[] layerNames = new String[layerables_l.size()];
        String[] cellValues = new String[layerables_l.size()];
        
        for(int l=0; l<layerables_l.size(); l++) {
            if(layerables_l.get(l) instanceof RasterImageLayer){
                RasterImageLayer rasterImageLayer = (RasterImageLayer) layerables_l.get(l);
                if(rasterImageLayer != null) {

                    layerNames[l] = rasterImageLayer.getName();

                    try {
                        
                        cellValues[l] = "";
                        for(int b=0; b<rasterImageLayer.getNumBands(); b++) {
                            Double cellValue = rasterImageLayer.getCellValue(coord.x, coord.y, b);
                            if(cellValue != null) {
                                if(rasterImageLayer.isNoData(cellValue)) {
                                    cellValues[l] = Double.toString(Double.NaN);
                                } else {
                                    cellValues[l] = cellValues[l].concat(Double.toString(cellValue));
                                }
                            }
                            cellValues[l] = cellValues[l].concat(";");
                        }
                        
                    } catch(RasterDataNotFoundException ex) {
                        cellValues[l] = "???";
                    }
                }
            }
        }
            
        infoFrame.setRasterValues(layerNames, cellValues); 
        infoFrame.surface();
    }

    private String getWmsLayeNames(WMSLayer selLayer) {
        int i;
        String names = "";
        List<String> layerNames = selLayer.getLayerNames();
        for (i=0; i< layerNames.size(); ++i) {
            String name = (String) layerNames.get(i);
            try {
                name = URLEncoder.encode(name, "UTF-8");
            } catch (Exception ignored) {
            }
            names += name;
            if (i < layerNames.size() - 1) {
                names += ",";
            }
        }

        return names;
    }

    private URL stripXhtmlTags(String serverURL) throws Exception {

        File tmpFile = File.createTempFile("wms", "q");
        FileOutputStream cleanHtml = new FileOutputStream(tmpFile);
        boolean resOk = true;
        //String xsl = (String)getClass().getResource("clean.xsl").getContent();
        //System.out.println("Ecco l'xsl: "+xsl);
        Transformer pulizia = TransformerFactory.newInstance().newTransformer(
                new StreamSource(getClass().getResourceAsStream("clean.xsl")));
        try {
            pulizia.transform(new StreamSource(serverURL),
                    new StreamResult(cleanHtml));
        } catch (Exception te) {
            //System.out.println("XSLT Error: "+te.getMessage());
            resOk = false;
        } finally {
            cleanHtml.close();
        }
        // [DR] gestione file vuoti
        if (!resOk || !(new FileReader(tmpFile).ready())) {
            /*
            FileWriter noResponse = new FileWriter(tmpFile);
            noResponse.write("<html><body><h2>Risultati interrogazione</h2>"+
            "Il server non ha restituito alcun risultato.</body></html>");
            noResponse.close();
             */
            //gestione risposte non html (testuali)
            return new URL(serverURL);
        }
        return tmpFile.toURI().toURL();
    }
    
    private String cleanWmsResponse(String inputWms) {
        
        String pattern = "GetFeatureInfo results:";
        int index = inputWms.indexOf(pattern);
        if(index != -1) {
            int endIndex = index + pattern.length();
            inputWms = inputWms.substring(endIndex);
            if(inputWms.startsWith("\n\n")) {
                inputWms = inputWms.replaceFirst("\n\n", "");
            }
        }
        
        return inputWms;
    }
    
}
