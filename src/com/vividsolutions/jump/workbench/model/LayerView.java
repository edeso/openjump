package com.vividsolutions.jump.workbench.model;

import com.vividsolutions.jump.feature.FeatureCollection;

import java.util.Collection;

public class LayerView extends Layer {

  private Layer layer;

  public LayerView(Layer layer) {
    super(layer.getName(), layer.getBasicStyle().getFillColor(), layer.getFeatureCollectionWrapper(), layer.getLayerManager());
    boolean firingEvents = getLayerManager().isFiringEvents();
    getLayerManager().setFiringEvents(false);
    try {
      setName(getName().replaceAll(layer.getName(),"").trim());
    } finally {
      getLayerManager().setFiringEvents(firingEvents);
    }
    this.layer = layer;
  }


  @Override
  public void setFeatureCollection(FeatureCollection featureCollection) {

    ObservableFeatureCollection observableFeatureCollection = (ObservableFeatureCollection) featureCollection;

    observableFeatureCollection.add(new ObservableFeatureCollection.Listener() {

      public void featuresAdded(Collection features) {
        getLayerManager().fireFeaturesChanged(features, FeatureEventType.ADDED,
                LayerView.this);
      }

      public void featuresRemoved(Collection features) {
        getLayerManager().fireFeaturesChanged(features,
                FeatureEventType.DELETED, LayerView.this);
      }

    });

    super.setFeatureCollectionWrapper(observableFeatureCollection);
  }

  public Layer getLayer() {
    return layer;
  }

  public boolean isSelectable() {
    return false;
  }

  public boolean isEditable() {
    return false;
  }

  public void dispose() {}

}