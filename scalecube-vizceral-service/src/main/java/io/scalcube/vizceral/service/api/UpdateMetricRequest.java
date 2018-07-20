package io.scalcube.vizceral.service.api;

public class UpdateMetricRequest {

  private String region;
  private String name;
  private String source;
  private String target;
  private VizceralMetric metrics;


  public UpdateMetricRequest() {};

  public static class Builder {
    private String region;
    private String name;
    private String source;
    private String target;
    private VizceralMetric metrics;

    public UpdateMetricRequest build() {
      return new UpdateMetricRequest(this);
    }

    public Builder region(String region) {
      this.region = region;
      return this;
    }

    public Builder name(String name) {
      this.name = name;
      return this;
    }

    public Builder source(String source) {
      this.source = source;
      return this;
    }

    public Builder target(String target) {
      this.target = target;
      return this;
    }

    public Builder metrics(VizceralMetric metrics) {
      this.metrics = metrics;
      return this;
    }

  }

  public UpdateMetricRequest(Builder builder) {
    this.region = builder.region;
    this.name = builder.name;
    this.source = builder.source;
    this.target = builder.target;
    this.metrics = builder.metrics;
  }

  public static Builder builder() {
    return new Builder();
  }

  public String region() {
    return this.region;
  }

  public String name() {
    return this.name;
  }

  public String source() {
    return this.source;
  }

  public String target() {
    return this.target;
  }

  public VizceralMetric metrics() {
    return this.metrics;
  }

}
