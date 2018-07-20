package io.scalcube.vizceral.service.api;

public class ReporterEvent {

  private String reporterId;
  
  private String region;
 
  private Node node;

  private VizceralConnection connection;

  public ReporterEvent() {};
  
  public ReporterEvent(String region, String reporterId, Node node) {
    this.region = region;
    this.node = node;
    this.reporterId = reporterId;
  }
  
  public ReporterEvent(String region, String reporterId, VizceralConnection connection) {
    this.connection = connection;
    this.reporterId = reporterId;
    this.region = region;
  }

  public String reporterId() {
    return reporterId;
  }
  
  public String region() {
    return region;
  }

  public Node node() {
    return node;
  }

  public VizceralConnection connection() {
    return connection;
  }

  @Override
  public String toString() {
    return "ReporterEvent [reporterId=" + reporterId + ", region=" + region + ", node=" + node + ", connection="
        + connection + "]";
  }
}
