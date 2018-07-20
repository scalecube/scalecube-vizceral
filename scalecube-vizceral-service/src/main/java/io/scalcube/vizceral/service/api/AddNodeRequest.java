package io.scalcube.vizceral.service.api;

public class AddNodeRequest {

  private String name;
  private String region;
  private String displayName;

  public AddNodeRequest() {};

  public AddNodeRequest(String region, String name, String displayName) {
    this.name = name;
    this.region = region;
    this.displayName = displayName;
  }

  public String region() {
    return this.region;
  }

  public String name() {
    return this.name;
  }

  public String displayName() {
    return this.displayName;
  }

}
