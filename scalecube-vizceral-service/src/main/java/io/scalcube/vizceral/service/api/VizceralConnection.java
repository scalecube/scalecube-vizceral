package io.scalcube.vizceral.service.api;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.concurrent.atomic.AtomicBoolean;

public class VizceralConnection {

  String source;
  String target;
  VizceralMetric metrics;

  @JsonProperty(value = "class")
  ClassType clazz;

  @JsonIgnore
  AtomicBoolean dirty = new AtomicBoolean(false);

  public VizceralConnection() {}

  public VizceralConnection(Builder builder) {
    this.source = builder.source;
    this.target = builder.target;
    this.clazz = builder.clazz;
    this.metrics = builder.metrics;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    public String source;
    public String target;
    public ClassType clazz;
    public VizceralMetric metrics = VizceralMetric.empty();

    public Builder source(String source) {
      this.source = source;
      return this;
    }

    public Builder target(String target) {
      this.target = target;
      return this;
    };

    public Builder clazz(ClassType clazz) {
      this.clazz = clazz;
      return this;
    };

    public Builder metrics(VizceralMetric metrics) {
      this.metrics = metrics;
      return this;
    };


    public VizceralConnection build() {
      return new VizceralConnection(this);
    }

  }

  public String source() {
    return this.source;
  }

  public String target() {
    return this.target;
  }

  public ClassType clazz() {
    return this.clazz;
  }

  public VizceralMetric metrics() {
    return this.metrics;
  }

  public static Builder normal(String source, String target) {
    return VizceralConnection.builder()
        .source(source)
        .target(target)
        .clazz(ClassType.normal);
  }

  public void update(VizceralMetric metrics) {
    this.metrics = metrics;
    dirty.set(true);
  }

  public boolean touch() {
    return dirty.getAndSet(false);
  }
  
  @Override
  public String toString() {
    return "VizceralConnection [source=" + source + ", target=" + target + ", metrics=" + metrics + ", clazz=" + clazz
        + "]";
  }

  

}
