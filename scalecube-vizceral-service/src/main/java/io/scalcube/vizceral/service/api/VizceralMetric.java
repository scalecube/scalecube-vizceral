package io.scalcube.vizceral.service.api;

public class VizceralMetric {

  @Override
  public String toString() {
    return "VizceralMetric [danger=" + danger + ", normal=" + normal + "]";
  }

  Double danger = 0.0;
  Double normal = 0.0;

  public VizceralMetric(Double normal, Double danger) {
    this.danger = danger;
    this.normal = normal;
  }

  public VizceralMetric() {
  }

  public Double danger () {
    return this.danger;
  }
  
  public Double normal() {
    return this.normal;
  }

  public static VizceralMetric empty() {
    return new VizceralMetric();
  }
 
}
