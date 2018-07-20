package io.scalcube.vizceral.service.api;

import io.scalecube.cluster.membership.IdGenerator;

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
  private String id;

  public VizceralConnection() {}

  public VizceralConnection(Builder builder) {
    this.source = builder.source;
    this.target = builder.target;
    this.clazz = builder.clazz;
    this.metrics = builder.metrics;
    this.id = builder.id;
  }

  

  public String id() {
    return this.id;
  }
  
  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    public String id;
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
      this.id = IdGenerator.generateId();
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
  
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    VizceralConnection other = (VizceralConnection) obj;
    if (id == null) {
      if (other.id != null)
        return false;
    } else if (!id.equals(other.id))
      return false;
    return true;
  }

}
